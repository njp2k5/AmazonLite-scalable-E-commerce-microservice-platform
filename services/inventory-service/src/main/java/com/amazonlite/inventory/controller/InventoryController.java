package com.amazonlite.inventory.controller;

import com.amazonlite.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getInventory(@PathVariable Long productId) {
        return inventoryService.getInventory(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> setInventory(@PathVariable Long productId, @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null || quantity < 0) {
            return ResponseEntity.badRequest().body("Invalid quantity");
        }
        return ResponseEntity.ok(inventoryService.setInventory(productId, quantity));
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<?> reserveStock(@PathVariable Long productId, @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("Invalid quantity");
        }
        try {
            boolean reserved = inventoryService.reserveStock(productId, quantity);
            if (reserved) {
                return ResponseEntity.ok(Map.of("status", "RESERVED"));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", "OUT_OF_STOCK"));
            }
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{productId}/release")
    public ResponseEntity<?> releaseStock(@PathVariable Long productId, @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("Invalid quantity");
        }
        try {
            inventoryService.releaseStock(productId, quantity);
            return ResponseEntity.ok(Map.of("status", "RELEASED"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{productId}/deduct")
    public ResponseEntity<?> deductStock(@PathVariable Long productId, @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("Invalid quantity");
        }
        try {
            inventoryService.deductStock(productId, quantity);
            return ResponseEntity.ok(Map.of("status", "DEDUCTED"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
