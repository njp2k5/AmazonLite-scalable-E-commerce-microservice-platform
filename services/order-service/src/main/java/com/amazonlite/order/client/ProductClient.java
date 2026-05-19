package com.amazonlite.order.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class ProductClient {
    private final WebClient.Builder webClientBuilder;

    @Autowired
    public ProductClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Map getProductById(Long productId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://product-service/products/" + productId)
                    .retrieve()
                    .onStatus(
                        status -> status.value() == 404,
                        response -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"))
                    )
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product service unavailable");
        }
    }
}
