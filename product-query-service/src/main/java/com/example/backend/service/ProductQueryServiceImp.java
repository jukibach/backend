package com.example.backend.service;

import com.example.backend.dto.ProductEvent;
import com.example.backend.entity.Product;
import com.example.backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class ProductQueryServiceImp implements ProductQueryService {

    @Autowired
    ProductRepository productRepository;

    @Override
    public List<Product> getProducts(int pageIndex, int pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort
                .by(sortBy)
                .descending();
        Pageable pagination = PageRequest.of(pageIndex, pageSize, sort);
        return productRepository.findAll(pagination).getContent();
    }

    @Override
    @Cacheable(value = "product", key = "#id", unless = "#result == null")
    public Product getProductById(int id) {
        Optional<Product> product = productRepository.findById((long) id);
        return product.orElse(null);
    }

    @Override
    public Product getProductBySerialNumber(String serialNumber) {
        Optional<Product> product = Optional.ofNullable(productRepository.findBySerialNumber(serialNumber));
        return product.orElse(null);
    }

    @Transactional
    @KafkaListener(topics = "product-event-topic", groupId = "product-event-group")
    public void processProductEvents(ProductEvent productEvent) {
        Product product = productEvent.getProduct();
        if(productEvent.getEventType().equals("CreateProduct")) {
            productRepository.save(product);
        }
        if(productEvent.getEventType().equals("UpdateProduct")) {
            Product existingProduct = productRepository.findById(product.getId()).get();
            existingProduct.setName(product.getName());
            existingProduct.setSerialNumber(product.getSerialNumber());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setUpdatedDate(new Date());
            productRepository.save(existingProduct);
        }
    }
}
