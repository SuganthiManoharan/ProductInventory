package com.inventory.product;

import com.inventory.product.model.Product;
import com.inventory.product.repository.ProductRepository;
import com.inventory.product.service.ProductService;
import com.inventory.product.service.ProductService.InventorySummary;
import com.inventory.product.service.ProductService.OutOfStockItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Initializes Mockito mocks
class ProductServiceUnitTest {

    @Mock // Creates a mock instance of the repository
    private ProductRepository productRepository;

    @InjectMocks // Injects the mocks into the service instance
    private ProductService productService;

    // Sample data setup
    private Product apple;
    private Product orange;
    private Product banana;
    private Product grapes;

    @BeforeEach
    void setUp() {
        // Data: Apple (100 qty, 500 price)
        apple = new Product();
        apple.setId(1L);
        apple.setName("Apple");
        apple.setQuantity(100);
        apple.setPrice(500.00);

        // Data: Orange (200 qty, 400 price)
        orange = new Product();
        orange.setId(2L);
        orange.setName("Orange");
        orange.setQuantity(200);
        orange.setPrice(400.00);

        // Data: Banana (300 qty, 450 price)
        banana = new Product();
        banana.setId(3L);
        banana.setName("Banana");
        banana.setQuantity(300);
        banana.setPrice(450.00);

        // Data: Grapes (0 qty, 100 price) - OUT OF STOCK
        grapes = new Product();
        grapes.setId(4L);
        grapes.setName("Grapes");
        grapes.setQuantity(0);
        grapes.setPrice(100.00);
    }

    @Test
    void testGetInventorySummary_Success() {
        List<Product> mockProducts = Arrays.asList(apple, orange, banana, grapes);
        when(productRepository.findAll()).thenReturn(mockProducts);
        // Total Quantity: 100 + 200 + 300 + 0 = 600
        int expectedTotalQuantity = 600;

        // Total Price Sum: 500 + 400 + 450 + 100 = 1450.00
        double totalPriceSum = 1450.00;

        // Average Price: 1450.00 / 4 = 362.50
        double expectedAveragePrice = 362.50;

        // Out of Stock: 1 (Grapes)

        InventorySummary summary = productService.getInventorySummary();

        // Assert
        assertEquals(4, summary.totalProducts(), "Total products count should be 4.");
        assertEquals(expectedTotalQuantity, summary.totalQuantity(), "Total quantity should be 600.");

        // Use delta for double comparison
        assertEquals(expectedAveragePrice, summary.averagePrice(), 0.001, "Average price calculation is incorrect.");

        // Assert Out-of-Stock List
        assertEquals(1, summary.outOfStock().size(), "There should be one out-of-stock product.");
        OutOfStockItem outOfStockItem = summary.outOfStock().get(0);
        assertEquals(4L, outOfStockItem.id(), "Out-of-stock product ID should be 4 (Grapes).");
        assertEquals("Grapes", outOfStockItem.name(), "Out-of-stock product name should be Grapes.");
    }

    @Test
    void testGetInventorySummary_EmptyInventory() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        InventorySummary summary = productService.getInventorySummary();

        assertEquals(0, summary.totalProducts(), "Total products should be zero.");
        assertEquals(0, summary.totalQuantity(), "Total quantity should be zero.");
        assertEquals(0.0, summary.averagePrice(), 0.001, "Average price should be zero.");
        assertTrue(summary.outOfStock().isEmpty(), "Out-of-stock list should be empty.");
    }
}