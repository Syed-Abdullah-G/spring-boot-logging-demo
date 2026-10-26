package com.example.loggingdemo.controller;

import com.example.loggingdemo.model.Product;
import com.example.loggingdemo.service.ProductService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAll() {
        log.info("Received request to list all products");
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        log.info("Received request to get product id={}", id);
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody Product product) {
        log.info("Received request to create product name='{}'", product.getName());
        return productService.create(product);
    }

    @PatchMapping("/{id}/stock")
    public Product adjustStock(@PathVariable Long id, @RequestParam int delta) {
        log.info("Received request to adjust stock of product id={} by {}", id, delta);
        return productService.adjustStock(id, delta);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("Received request to delete product id={}", id);
        productService.delete(id);
    }
}
