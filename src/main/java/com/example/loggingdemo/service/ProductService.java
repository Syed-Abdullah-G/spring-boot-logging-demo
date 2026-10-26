package com.example.loggingdemo.service;

import com.example.loggingdemo.exception.InvalidProductException;
import com.example.loggingdemo.exception.ProductNotFoundException;
import com.example.loggingdemo.model.Product;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business logic backed by an in-memory map, so the demo needs no database.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    public ProductService() {
        save(new Product(null, "Mechanical Keyboard", new BigDecimal("89.99"), 25));
        save(new Product(null, "USB-C Hub", new BigDecimal("34.50"), 3));
        log.info("Seeded {} sample products", products.size());
    }

    public List<Product> findAll() {
        log.debug("Fetching all products, current count={}", products.size());
        return new ArrayList<>(products.values());
    }

    public Product findById(Long id) {
        log.debug("Looking up product id={}", id);
        Product product = products.get(id);
        if (product == null) {
            // The exception is logged once, in GlobalExceptionHandler, to avoid duplicate log lines.
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    public Product create(Product request) {
        log.debug("Validating new product: name={}, price={}, stock={}",
                request.getName(), request.getPrice(), request.getStock());
        validate(request);

        Product saved = save(new Product(null, request.getName().trim(), request.getPrice(), request.getStock()));
        log.info("Created product id={} name='{}'", saved.getId(), saved.getName());
        warnIfLowStock(saved);
        return saved;
    }

    public Product adjustStock(Long id, int delta) {
        Product product = findById(id);
        int newStock = product.getStock() + delta;
        log.debug("Adjusting stock for product id={}: {} + ({}) = {}", id, product.getStock(), delta, newStock);

        if (newStock < 0) {
            throw new InvalidProductException(
                    "Insufficient stock for product " + id + ": have " + product.getStock() + ", change " + delta);
        }

        product.setStock(newStock);
        log.info("Stock updated for product id={}, newStock={}", id, newStock);
        warnIfLowStock(product);
        return product;
    }

    public void delete(Long id) {
        if (products.remove(id) == null) {
            throw new ProductNotFoundException(id);
        }
        log.info("Deleted product id={}", id);
    }

    private Product save(Product product) {
        product.setId(idSequence.incrementAndGet());
        products.put(product.getId(), product);
        return product;
    }

    private void validate(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new InvalidProductException("Product name must not be blank");
        }
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new InvalidProductException("Product price must be greater than zero");
        }
        if (product.getStock() < 0) {
            throw new InvalidProductException("Product stock must not be negative");
        }
    }

    private void warnIfLowStock(Product product) {
        if (product.getStock() < LOW_STOCK_THRESHOLD) {
            log.warn("Low stock for product id={} name='{}': {} left (threshold={})",
                    product.getId(), product.getName(), product.getStock(), LOW_STOCK_THRESHOLD);
        }
    }
}
