package com.amazonlite.product.service;

import com.amazonlite.product.model.Product;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();

    public ProductService() {
        for (long i = 1; i <= 50; i++) {
            products.add(new Product(i, "Product " + i, i * 10, 100));
        }
    }

    public Map<String, Object> getProducts(int page, int size) {
        int start = page * size;
        int end = Math.min(start + size, products.size());

        List<Product> content = products.subList(start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", products.size());

        return response;
    }

    public Product getById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}