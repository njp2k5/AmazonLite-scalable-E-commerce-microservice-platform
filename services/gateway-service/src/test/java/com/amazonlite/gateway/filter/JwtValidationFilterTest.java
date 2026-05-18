package com.amazonlite.gateway.filter;

import com.amazonlite.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtValidationFilterTest {

    @Test
    void shouldRejectRequestsWithoutBearerToken() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtValidationFilter filter = new JwtValidationFilter(jwtUtil);
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/buy/1").build());
        GatewayFilterChain chain = ex -> Mono.empty();

        filter.apply(new JwtValidationFilter.Config()).filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldForwardValidatedClaimsAsHeaders() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("buyer@example.com");
        when(claims.get("role", String.class)).thenReturn("CUSTOMER");
        when(claims.get("userId")).thenReturn(42L);
        when(jwtUtil.validateToken("valid-token")).thenReturn(claims);

        JwtValidationFilter filter = new JwtValidationFilter(jwtUtil);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/cart/42")
                        .header("Authorization", "Bearer valid-token")
                        .build()
        );

        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            forwardedExchange.set(ex);
            return Mono.empty();
        };

        filter.apply(new JwtValidationFilter.Config()).filter(exchange, chain).block();

        assertThat(forwardedExchange.get()).isNotNull();
        assertThat(forwardedExchange.get().getRequest().getHeaders().getFirst("X-User-Email")).isEqualTo("buyer@example.com");
        assertThat(forwardedExchange.get().getRequest().getHeaders().getFirst("X-User-Role")).isEqualTo("CUSTOMER");
        assertThat(forwardedExchange.get().getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("42");
    }
}
