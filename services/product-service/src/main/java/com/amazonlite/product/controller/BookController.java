package com.amazonlite.product.controller;

import com.amazonlite.product.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Map<String, Object> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.getProducts(page, size);
    }

    @GetMapping("/search")
    public Map<String, Object> searchBooks(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.searchBooks(name, author, category, minPrice, maxPrice, page, size);
    }

    @GetMapping("/page")
    public Map<String, Object> getProductsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.getProductsPage(page, size);
    }

    @GetMapping("/{id}")
    public Object getProduct(@PathVariable Long id) {
        return bookService.getById(id).orElse(null);
    }

    @PatchMapping("/{id}/decrement-stock")
    public ResponseEntity<?> decrementStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid quantity");
        }
        try {
            int updatedStock = bookService.decrementStock(id, quantity);
            return ResponseEntity.ok(Map.of("stock", updatedStock));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }
    }
}
