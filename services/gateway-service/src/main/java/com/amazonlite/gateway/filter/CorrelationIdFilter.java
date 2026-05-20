package com.amazonlite.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter extends AbstractGatewayFilterFactory<CorrelationIdFilter.Config> {

    public CorrelationIdFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String existing = request.getHeaders().getFirst("X-Correlation-Id");
            String correlationId = existing == null || existing.isBlank() ? UUID.randomUUID().toString() : existing;

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Correlation-Id", correlationId)
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            return chain.filter(modifiedExchange).doOnSuccess(aVoid -> {
                // ensure response header contains correlation id
                modifiedExchange.getResponse().getHeaders().add("X-Correlation-Id", correlationId);
            });
        };
    }

    public static class Config {
        // no properties for now
    }
}
