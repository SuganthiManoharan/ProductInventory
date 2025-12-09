package com.inventory.product.repository;

import com.inventory.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Custom query method for case-insensitive search by name
    List<Product> findByNameContainingIgnoreCase(String name);

    // List products where quantity is 0
    List<Product> findByQuantity(int quantity);
}