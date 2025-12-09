package com.inventory.product;

import com.inventory.product.model.Product;
import com.inventory.product.repository.ProductRepository;
import com.inventory.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @DataJpaTest configures JPA repositories and sets up an in-memory database (H2) by default.
@DataJpaTest
@Import(ProductService.class)
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    private Product createProduct(String name, int quantity, double price) {
        Product p = new Product();
        p.setName(name);
        p.setQuantity(quantity);
        p.setPrice(price);
        return p;
    }

    @BeforeEach
    void setUp() {
        // 1. Ensure a clean state before each test (H2 instance is generally fresh, but good practice)
        productRepository.deleteAll();
        productRepository.save(createProduct("Laptop", 10, 2000.00));
        productRepository.save(createProduct("Monitor LED", 2, 500.00));
        productRepository.save(createProduct("Keyboard", 5, 200.00));
        productRepository.save(createProduct("Webcam", 20, 500.00));
        productRepository.save(createProduct("Mouse", 0, 75.00)); // Out of Stock
    }

    // --- TEST CASES ---

    @Test
    void testFindAll_PagingAndSorting() {
        PageRequest pageable = PageRequest.of(0, 3, Sort.by("price").ascending());

        Page<Product> page = productRepository.findAll(pageable);

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(3);

        assertThat(page.getContent().get(0).getName()).isEqualTo("Mouse");
        assertThat(page.getContent().get(1).getName()).isEqualTo("Keyboard");

        assertEquals(500.00, page.getContent().get(2).getPrice(), "Third item is 500.00 price.");
    }

    @Test
    void testSearchByName() {

        List<Product> results = productRepository.findByNameContainingIgnoreCase("key");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Keyboard");
    }

    @Test
    void testDeleteProduct_Success() {

        Product laptop = productRepository.findByNameContainingIgnoreCase("Laptop").get(0);
        Long productId = laptop.getId();

        productService.deleteProduct(productId);

        assertFalse(productRepository.findById(productId).isPresent());
    }

    @Test
    void testGetInventorySummary_Success() {

        int expectedTotalQuantity = 37;

        double expectedTotalPriceSum = 3275.00;
        double expectedAveragePrice = 655.00;

        ProductService.InventorySummary summary = productService.getInventorySummary();

        assertEquals(5, summary.totalProducts());
        assertEquals(expectedTotalQuantity, summary.totalQuantity());
        assertEquals(expectedAveragePrice, summary.averagePrice());
        assertThat(summary.outOfStock()).hasSize(1);
    }
}