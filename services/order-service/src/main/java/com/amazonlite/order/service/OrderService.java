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
import com.amazonlite.order.client.ProductClient;
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

    @Transactional
    public CreateOrderResponse createOrder(Long userId, CreateOrderRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
        }

        // Fetch product from product-service using ProductClient
        Map product = productClient.getProductById(request.getProductId());
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
        }
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
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(totalPrice);
        order = orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(unitPrice);
        orderItemRepository.save(item);

        return new CreateOrderResponse(order.getId(), order.getStatus().name(), order.getTotalPrice());
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
}
