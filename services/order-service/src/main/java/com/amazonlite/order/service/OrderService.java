package com.amazonlite.order.service;

import com.amazonlite.order.dto.CreateOrderRequest;
import com.amazonlite.order.dto.CreateOrderResponse;
import com.amazonlite.order.model.Order;
import com.amazonlite.order.model.OrderItem;
import com.amazonlite.order.model.OrderStatus;
import com.amazonlite.order.repository.OrderRepository;
import com.amazonlite.order.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonlite.order.client.ProductClient;
import com.amazonlite.order.client.ProductStockClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductClient productClient;

    @Autowired
    private ProductStockClient productStockClient;

    @Autowired
    private com.amazonlite.order.event.OrderEventPublisher orderEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Transactional
    public CreateOrderResponse createOrder(Long userId, CreateOrderRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
        }
        try {
            // 1. fetch product
            Map product = productClient.getProductById(request.getProductId());
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
            }
            // 2. validate stock
            Integer stock = null;
            try {
                stock = (Integer) product.get("stock");
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Invalid product response");
            }
            BigDecimal unitPrice;
            try {
                unitPrice = new BigDecimal(product.get("price").toString());
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Invalid product price");
            }
            if (stock == null || stock < request.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock");
            }
            // 3. call decrement-stock
            productStockClient.decrementStock(request.getProductId(), request.getQuantity());
            // 4. create order
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));
            Order order = new Order();
            order.setUserId(userId);
            order.setStatus(OrderStatus.PENDING);
            order.setTotalPrice(totalPrice);
            final Order savedOrder = orderRepository.save(order);
            // 5. create order item
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(request.getProductId());
            item.setQuantity(request.getQuantity());
            item.setUnitPrice(unitPrice);
            orderItemRepository.save(item);
            // 6. commit (handled by @Transactional)
            CreateOrderResponse response = new CreateOrderResponse(savedOrder.getId(), savedOrder.getStatus().name(), savedOrder.getTotalPrice());

            // Publish OrderCreatedEvent after transaction commits
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                            String corr = org.slf4j.MDC.get("correlationId");
                            com.amazonlite.shared.events.OrderCreatedEvent event = new com.amazonlite.shared.events.OrderCreatedEvent(
                                String.valueOf(savedOrder.getId()), String.valueOf(savedOrder.getUserId()), savedOrder.getTotalPrice(), java.time.Instant.now(), corr);
                            orderEventPublisher.publishOrderCreated(event);
                    } catch (Exception ex) {
                            logger.error("Exception while publishing OrderCreatedEvent for orderId={}", savedOrder.getId(), ex);
                    }
                }
            });

            return response;
        } catch (Exception e) {
            // Rollback handled by @Transactional
            throw e;
        }
    }
    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    public Order getOrderByIdWithAuth(Long orderId, Long userId, String userRole) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        Order order = orderOpt.get();
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return order;
        }
        if ("CUSTOMER".equalsIgnoreCase(userRole) && order.getUserId().equals(userId)) {
            return order;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long userId, String userRole) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        Order order = orderOpt.get();
        boolean isOwner = order.getUserId().equals(userId);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        if (!(isOwner || isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot cancel shipped or delivered order");
        }
        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED) {
            order.setStatus(OrderStatus.CANCELLED);
            return orderRepository.save(order);
        }
        // If already cancelled, just return
        return order;
    }
}
