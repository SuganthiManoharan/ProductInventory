package com.inventory.product.controller;

import com.inventory.product.model.Product;
import com.inventory.product.service.ProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED); // 201 Created
    }

    @GetMapping("/getAllProducts")
    public List<Product> listAllProducts() {
        return productService.findAll(); // 200 OK by default
    }

    /**
     * List all products with pagination and sorting.
     * Accessible via: GET /products?page=0&size=10&sort=price,desc
     */
    @GetMapping
    public Page<Product> listAllProductsPaged(Pageable pageable) {
        return productService.findAllPaged(pageable);
    }

    /**
     * Search a products with pagination and sorting.
     * Accessible via: GET http://localhost:8080/products/search?name=lap
     */
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name) {
        return productService.searchByName(name);
    }

    // PUT /products/{id}/quantity
    @PutMapping("/{id}/quantity")
    public ResponseEntity<Product> updateProductQuantity(@PathVariable Long id, @RequestBody QuantityUpdate request) {
        Product updatedProduct = productService.updateQuantity(id, request.quantity());
        return ResponseEntity.ok(updatedProduct); // 200 OK
    }

    // GET /products/summary
    @GetMapping("/summary")
    public ProductService.InventorySummary getSummary() {

        return productService.getInventorySummary(); // 200 OK
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // Helper Record for quantity update request body
    public record QuantityUpdate(int quantity) {}
}