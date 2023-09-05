package com.example.backend.controller;

import com.example.backend.dto.CreateProductRequest;
import com.example.backend.dto.UpdateProductRequest;
import com.example.backend.response.ProductResponse;
import com.example.backend.service.ProductCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/products")
public class ProductCommandController {

    @Autowired
    private ProductCommandService commandService;

    @PostMapping
    @ResponseStatus(OK)
    public ProductResponse createProduct(@RequestBody CreateProductRequest product) throws IllegalAccessException {
        return commandService.createProduct(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable long id, @RequestBody UpdateProductRequest request)
            throws IllegalAccessException {
        ProductResponse updateProduct = commandService.updateProduct(id, request);
        if(updateProduct == null) {
            return new ResponseEntity<>(NOT_FOUND);
        }
        return new ResponseEntity<>(updateProduct, OK);
    }
}