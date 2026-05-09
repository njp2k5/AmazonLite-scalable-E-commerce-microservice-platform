package com.amazonlite.product.controller;

import com.amazonlite.product.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Map<String, Object> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return productService.getProducts(page, size);
    }

    @GetMapping("/{id}")
    public Object getProduct(@PathVariable Long id) {
        return productService.getById(id).orElse(null);
    }
}
