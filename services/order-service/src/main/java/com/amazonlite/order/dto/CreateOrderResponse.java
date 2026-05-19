package com.amazonlite.order.dto;

import java.math.BigDecimal;

public class CreateOrderResponse {
    private Long orderId;
    private String status;
    private BigDecimal totalPrice;

    public CreateOrderResponse(Long orderId, String status, BigDecimal totalPrice) {
        this.orderId = orderId;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public Long getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}
