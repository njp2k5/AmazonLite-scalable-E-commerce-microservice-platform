package com.amazonlite.product.controller;

import com.amazonlite.product.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/Books")
public class BookController {

    private final BookService BookService;

    public BookController(BookService BookService) {
        this.BookService = BookService;
    }

    @GetMapping
    public Map<String, Object> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return BookService.getProducts(page, size);
    }

    @GetMapping("/page")
    public Map<String, Object> getProductsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return BookService.getProductsPage(page, size);
    }

    @GetMapping("/{id}")
    public Object getProduct(@PathVariable Long id) {
        return BookService.getById(id).orElse(null);
    }

    @PatchMapping("/{id}/decrement-stock")
    public ResponseEntity<?> decrementStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid quantity");
        }
        try {
            int updatedStock = BookService.decrementStock(id, quantity);
            return ResponseEntity.ok(Map.of("stock", updatedStock));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }
    }
}
