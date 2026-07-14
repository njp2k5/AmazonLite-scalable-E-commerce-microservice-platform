package com.amazonlite.product.service;

import com.amazonlite.product.model.Book;
import com.amazonlite.product.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
public class BookService {

    private final BookRepository BookRepository;

    public BookService(BookRepository BookRepository) {
        this.BookRepository = BookRepository;
    }

    public Map<String, Object> getProducts(int page, int size) {
        Page<Book> result = BookRepository.findAll(PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("content", result.getContent());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());

        return response;
    }

    public Map<String, Object> getProductsPage(int page, int size) {
        Page<Book> result = BookRepository.findAll(PageRequest.of(page, size));

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

    public Optional<Book> getById(Long id) {
        return BookRepository.findById(id);
    }

    @Transactional
    public int decrementStock(Long id, int quantity) {
        Book Book = BookRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        if (Book.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        Book.setStock(Book.getStock() - quantity);
        BookRepository.save(Book);
        return Book.getStock();
    }
}
