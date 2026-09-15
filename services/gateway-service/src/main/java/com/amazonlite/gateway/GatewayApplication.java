package com.amazonlite.gateway;

import com.amazonlite.gateway.filter.JwtValidationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import com.amazonlite.gateway.filter.OrderAuthFilter;

@SpringBootApplication
public class GatewayApplication {

    @org.springframework.beans.factory.annotation.Value("${app.services.product-service-url:lb://product-service}")
    private String productServiceUrl;

    @org.springframework.beans.factory.annotation.Value("${app.services.order-service-url:lb://order-service}")
    private String orderServiceUrl;

    @org.springframework.beans.factory.annotation.Value("${app.services.notification-service-url:lb://notification-service}")
    private String notificationServiceUrl;

    @org.springframework.beans.factory.annotation.Value("${app.services.auth-service-url:lb://auth-service}")
    private String authServiceUrl;

    @org.springframework.beans.factory.annotation.Value("${app.services.cart-service-url:lb://cart-service}")
    private String cartServiceUrl;

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtValidationFilter jwtValidationFilter, OrderAuthFilter orderAuthFilter) {
        return builder.routes()
                // Auth routes - public endpoints (no JWT validation)
                .route("auth", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(authServiceUrl))

                // Public product browsing routes - no JWT validation required
                .route("products-public", r -> r
                        .path("/api/products/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(productServiceUrl))

                // OpenAPI Docs route for product-service
                .route("product-service-api-docs", r -> r
                        .path("/product-service/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri(productServiceUrl))

                // OpenAPI Docs route for order-service
                .route("order-service-api-docs", r -> r
                        .path("/order-service/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri(orderServiceUrl))

                // OpenAPI Docs route for notification-service
                .route("notification-service-api-docs", r -> r
                        .path("/notification-service/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri(notificationServiceUrl))

                // Protected routes - JWT validation required
                .route("cart-protected", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f.stripPrefix(1).filter(jwtValidationFilter.apply(new JwtValidationFilter.Config())))
                        .uri(cartServiceUrl))
                .route("buy-protected", r -> r
                        .path("/api/buy/**")
                        .filters(f -> f.stripPrefix(1).filter(jwtValidationFilter.apply(new JwtValidationFilter.Config())))
                        .uri(productServiceUrl))
                // Order-service protected routes
                .route("orders-protected", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.stripPrefix(1)
                            .filter(jwtValidationFilter.apply(new JwtValidationFilter.Config()))
                            .filter(orderAuthFilter.apply(new OrderAuthFilter.Config())))
                        .uri(orderServiceUrl))
                .build();
    }
}
