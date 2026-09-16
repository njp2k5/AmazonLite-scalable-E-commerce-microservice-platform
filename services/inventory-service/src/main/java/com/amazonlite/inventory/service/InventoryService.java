package com.amazonlite.inventory.service;

import com.amazonlite.inventory.model.InventoryItem;
import com.amazonlite.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Optional<InventoryItem> getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Transactional
    public InventoryItem setInventory(Long productId, int quantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElse(new InventoryItem(productId, 0));
        item.setQuantity(quantity);
        return inventoryRepository.save(item);
    }

    @Transactional
    public boolean reserveStock(Long productId, int quantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (item.getQuantity() - item.getReservedQuantity() >= quantity) {
            item.setReservedQuantity(item.getReservedQuantity() + quantity);
            inventoryRepository.save(item);
            return true;
        }
        return false;
    }

    @Transactional
    public void releaseStock(Long productId, int quantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (item.getReservedQuantity() >= quantity) {
            item.setReservedQuantity(item.getReservedQuantity() - quantity);
            inventoryRepository.save(item);
        } else {
            throw new IllegalArgumentException("Cannot release more than reserved");
        }
    }

    @Transactional
    public void deductStock(Long productId, int quantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (item.getReservedQuantity() >= quantity && item.getQuantity() >= quantity) {
            item.setQuantity(item.getQuantity() - quantity);
            item.setReservedQuantity(item.getReservedQuantity() - quantity);
            inventoryRepository.save(item);
        } else {
            throw new IllegalArgumentException("Insufficient reserved stock to deduct");
        }
    }
}
