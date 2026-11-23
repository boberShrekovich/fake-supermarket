package ru.psu.catalogservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import ru.psu.catalogservice.dto.ProductRequest;
import ru.psu.catalogservice.dto.ProductResponse;
import ru.psu.catalogservice.dto.StockReduceRequest;
import ru.psu.catalogservice.service.ProductService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Async
    @GetMapping
    public CompletableFuture<ResponseEntity<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return CompletableFuture.completedFuture(new ResponseEntity<>(products, HttpStatus.OK));
    }

    @Async
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<ProductResponse>> getProductById(@PathVariable("id") Long id) {
        ProductResponse product = productService.getProductById(id);
        return CompletableFuture.completedFuture(new ResponseEntity<>(product, HttpStatus.OK));
    }

    @Async
    @PostMapping
    public CompletableFuture<ResponseEntity<ProductResponse>> createProduct(@RequestBody ProductRequest productRequest) {
        ProductResponse product = productService.createProduct(productRequest);
        return CompletableFuture.completedFuture(new ResponseEntity<>(product, HttpStatus.CREATED));
    }

    @Async
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<ProductResponse>> updateProduct(@PathVariable("id") Long id, @RequestBody ProductRequest productRequest) {
        ProductResponse product = productService.updateProduct(id, productRequest);
        return CompletableFuture.completedFuture(new ResponseEntity<>(product, HttpStatus.OK));
    }

    @Async
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<ProductResponse>> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @Async
    @PostMapping("/reduce-stock")
    public CompletableFuture<ResponseEntity<Void>> reduceStock(@RequestBody List<StockReduceRequest> items) {
        productService.reduceStock(items);
        return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.OK));
    }
}
