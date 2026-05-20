package com.amazonlite.order.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter((request, next) -> {
                    String corr = MDC.get("correlationId");
                    if (corr != null && !corr.isBlank()) {
                        ClientRequest filtered = ClientRequest.from(request)
                                .header("X-Correlation-Id", corr)
                                .build();
                        return next.exchange(filtered);
                    }
                    return next.exchange(request);
                });
    }
}
