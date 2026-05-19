package com.amazonlite.product.service;

import com.amazonlite.product.model.Product;
import com.amazonlite.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Map<String, Object> getProducts(int page, int size) {
        Page<Product> result = productRepository.findAll(PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("content", result.getContent());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());

        return response;
    }

    public Map<String, Object> getProductsPage(int page, int size) {
        Page<Product> result = productRepository.findAll(PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("content", result.getContent());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("hasNext", result.hasNext());
        response.put("hasPrevious", result.hasPrevious());

        return response;
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public int decrementStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        return product.getStock();
    }
}