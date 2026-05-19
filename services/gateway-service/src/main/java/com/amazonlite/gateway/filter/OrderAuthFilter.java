package com.amazonlite.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;

@Component
public class OrderAuthFilter extends AbstractGatewayFilterFactory<OrderAuthFilter.Config> {
    public OrderAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();
            String method = request.getMethodValue();
            String role = request.getHeaders().getFirst("X-User-Role");

            // POST /api/orders -> CUSTOMER only
            if (HttpMethod.POST.matches(method) && path.matches("/api/orders/?$")) {
                if (!"CUSTOMER".equalsIgnoreCase(role)) {
                    return respondWithError(exchange, HttpStatus.FORBIDDEN, "Only CUSTOMER can create orders");
                }
            }
            // GET /api/orders/me -> CUSTOMER only
            else if (HttpMethod.GET.matches(method) && path.matches("/api/orders/me/?$")) {
                if (!"CUSTOMER".equalsIgnoreCase(role)) {
                    return respondWithError(exchange, HttpStatus.FORBIDDEN, "Only CUSTOMER can view their orders");
                }
            }
            // GET /api/orders/{id} and PATCH /api/orders/{id}/cancel -> CUSTOMER or ADMIN
            else if ((HttpMethod.GET.matches(method) && path.matches("/api/orders/\\d+$")) ||
                     (HttpMethod.PATCH.matches(method) && path.matches("/api/orders/\\d+/cancel$"))) {
                if (!"CUSTOMER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
                    return respondWithError(exchange, HttpStatus.FORBIDDEN, "Only CUSTOMER or ADMIN allowed");
                }
            }
            // Deny all other /api/orders/**
            else if (path.startsWith("/api/orders")) {
                return respondWithError(exchange, HttpStatus.FORBIDDEN, "Access denied");
            }
            return chain.filter(exchange);
        };
    }

    private Mono<Void> respondWithError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        String errorResponse = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());
        DataBuffer dataBuffer = response.bufferFactory().wrap(errorResponse.getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }

    public static class Config {}
}
