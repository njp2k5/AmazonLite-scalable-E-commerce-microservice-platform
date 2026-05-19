package com.amazonlite.order.service;

import com.amazonlite.order.model.Order;
import org.springframework.security.access.AccessDeniedException;

public class AuthorizationHelper {
    public static void validateOrderAccess(Order order, Long userId, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return;
        }
        if ("CUSTOMER".equalsIgnoreCase(role) && order.getUserId().equals(userId)) {
            return;
        }
        throw new AccessDeniedException("Access denied to order");
    }
}
