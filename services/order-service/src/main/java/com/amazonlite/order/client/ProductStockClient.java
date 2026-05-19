package com.amazonlite.order.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class ProductStockClient {
    private final WebClient.Builder webClientBuilder;

    @Autowired
    public ProductStockClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public int decrementStock(Long productId, int quantity) {
        try {
            Map<String, Integer> req = Map.of("quantity", quantity);
            Map resp = webClientBuilder.build()
                    .patch()
                    .uri("http://product-service/products/" + productId + "/decrement-stock")
                    .bodyValue(req)
                    .retrieve()
                    .onStatus(status -> status.value() == 409,
                        response -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock")))
                    .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")))
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            return (int) resp.get("stock");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product service unavailable");
        }
    }
}
