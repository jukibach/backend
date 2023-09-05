package com.example.backend.cache;

import com.example.backend.entity.Product;
import com.example.backend.repository.ProductRepository;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;

@Component
public class CacheClass {

    @Resource
    ProductRepository productRepository;

    @CachePut(value = "product", key = "#product.id")
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
