package com.example.backend.service;

import com.example.backend.dto.CreateProductRequest;
import com.example.backend.dto.UpdateProductRequest;
import com.example.backend.response.ProductResponse;

public interface ProductCommandService {
    public ProductResponse createProduct(CreateProductRequest product) throws IllegalAccessException;
    public ProductResponse updateProduct(long id, UpdateProductRequest request) throws IllegalAccessException;
}
