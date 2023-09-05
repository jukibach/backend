package com.example.backend.controller;

import com.example.backend.entity.Product;
import com.example.backend.service.ProductQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RequestMapping("/api/products")
@RestController
public class ProductQueryController {

    @Autowired
    private ProductQueryService queryService;

    @GetMapping
    public List<Product> fetchAllProducts(
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String sortOrder) {
        return queryService.getProducts(pageIndex, pageSize, sortBy, sortOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable int id) {
        Product product = queryService.getProductById(id);
        if(product == null) {
            return new ResponseEntity<>(NOT_FOUND);
        }
        return new ResponseEntity<>(product, OK);
    }

    @GetMapping("/serialNumbers/{serialNumber}")
    public Product getProductBySerialNumber(@PathVariable String serialNumber) {
        return queryService.getProductBySerialNumber(serialNumber);
    }
}