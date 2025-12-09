package com.inventory.product.service;

import com.inventory.product.exception.InvalidInputException;
import com.inventory.product.model.Product;
import com.inventory.product.repository.ProductRepository;
import com.inventory.product.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Creates a new product
     */
    public Product createProduct(Product product) {
        if (product.getPrice() == null || product.getPrice() < 0)
        {
            throw new InvalidInputException("Price cannot be empty: ");
        }else if (product.getQuantity() == null || product.getQuantity() < 0){
            throw new InvalidInputException("Quantity cannot be negative: ");
        }

        return productRepository.save(product);
    }


    /**
     * Finds all products with pagination support.
     */
    public Page<Product> findAllPaged(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    // Non-paged findAll
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // Search products by name (case-insensitive)
    public List<Product> searchByName(String name) {

        return productRepository.findByNameContainingIgnoreCase(name);
    }

    // Update product quantity
    @Transactional
    public Product updateQuantity(Long id, int newQuantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (newQuantity < 0) {
            throw new InvalidInputException("Quantity cannot be negative: " + newQuantity);
        }

        product.setQuantity(newQuantity);
        return productRepository.save(product);
    }

    //  DELETE /products/{id}: Delete product
    @Transactional
    public void deleteProduct(Long id) {
        // Find the product first to ensure it exists before attempting deletion
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // Get inventory
    public InventorySummary getInventorySummary() {
        List<Product> allProducts = productRepository.findAll();

        long totalProducts = allProducts.size();
        int totalQuantity = allProducts.stream().mapToInt(Product::getQuantity).sum();

        // Calculate Average Price
        double totalPriceSum = allProducts.stream().mapToDouble(Product::getPrice).sum();
        double averagePrice = totalProducts > 0 ? totalPriceSum / totalProducts : 0.0;

        // List Out-of-Stock Products
        List<OutOfStockItem> outOfStock = allProducts.stream()
                .filter(p -> p.getQuantity() == 0)
                .map(p -> new OutOfStockItem(p.getId(), p.getName()))
                .collect(Collectors.toList());
        return new InventorySummary(totalProducts, totalQuantity, averagePrice, outOfStock);
    }

    //records for OutOfStockItem
    public record OutOfStockItem(Long id, String name) {}

    //records for InventorySummary
    public record InventorySummary(long totalProducts, int totalQuantity, double averagePrice, List<OutOfStockItem> outOfStock) {}
}