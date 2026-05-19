package com.amazonlite.product.controller;

import com.amazonlite.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/page")
    public Map<String, Object> getProductsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return productService.getProductsPage(page, size);
    }

    @GetMapping("/{id}")
    public Object getProduct(@PathVariable Long id) {
        return productService.getById(id).orElse(null);
    }

    @PatchMapping("/{id}/decrement-stock")
    public ResponseEntity<?> decrementStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid quantity");
        }
        try {
            int updatedStock = productService.decrementStock(id, quantity);
            return ResponseEntity.ok(Map.of("stock", updatedStock));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
    }
}
