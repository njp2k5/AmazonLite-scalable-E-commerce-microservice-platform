package com.amazonlite.order.controller;

import com.amazonlite.order.dto.CreateOrderRequest;
import com.amazonlite.order.dto.CreateOrderResponse;
import com.amazonlite.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.amazonlite.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public Page<Order> getMyOrders(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getOrdersByUserId(userId, pageable);
    }
}
