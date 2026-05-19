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

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtValidationFilter jwtValidationFilter, OrderAuthFilter orderAuthFilter) {
                return builder.routes()
                // Auth routes - public endpoints (no JWT validation)
                .route("auth", r -> r
                        .path("/auth/**")
                        .uri("http://product-service:8081"))

                // Public product browsing routes - no JWT validation required
                .route("products-public", r -> r
                        .path("/api/products/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://product-service:8081"))

                // Protected routes - JWT validation required
                .route("cart-protected", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f.stripPrefix(1).filter(jwtValidationFilter.apply(new JwtValidationFilter.Config())))
                        .uri("http://product-service:8081"))
                .route("buy-protected", r -> r
                        .path("/api/buy/**")
                        .filters(f -> f.stripPrefix(1).filter(jwtValidationFilter.apply(new JwtValidationFilter.Config())))
                        .uri("http://product-service:8081"))
                // Order-service protected routes
                .route("orders-protected", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.stripPrefix(1)
                            .filter(jwtValidationFilter.apply(new JwtValidationFilter.Config()))
                            .filter(orderAuthFilter.apply(new OrderAuthFilter.Config())))
                        .uri("http://order-service:8083"))
                .build();
    }
}
