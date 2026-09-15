package com.amazonlite.product.repository;

import com.amazonlite.product.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    Optional<Book> findByName(String name);
    java.util.List<Book> findByIsBestsellerTrue();
    Optional<Book> findFirstByIsFeaturedTrue();
}
