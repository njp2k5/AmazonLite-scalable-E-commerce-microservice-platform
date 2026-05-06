package com.amazonlite.gateway;

import com.amazonlite.gateway.filter.JwtValidationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtValidationFilter jwtValidationFilter) {
        return builder.routes()
                // Auth routes - public endpoints (no JWT validation)
                .route("auth-register", r -> r
                        .path("/auth/register")
                        .uri("http://product-service:8081"))
                .route("auth-login", r -> r
                        .path("/auth/login")
                        .uri("http://product-service:8081"))
                
                // Products route - protected endpoint (with JWT validation)
                .route("products", r -> r
                        .path("/api/products/**")
                        .filters(f -> f.filter(jwtValidationFilter.apply(new JwtValidationFilter.Config())))
                        .uri("http://product-service:8081"))
                .build();
    }
}
