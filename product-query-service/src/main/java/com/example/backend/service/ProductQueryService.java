package com.example.backend.service;

import com.example.backend.dto.ProductEvent;
import com.example.backend.entity.Product;

import java.util.List;

public interface ProductQueryService {
    public List<Product> getProducts(int pageIndex, int pageSize, String sortBy, String sortOrder);

    Product getProductById(int id);

    public Product getProductBySerialNumber(String serialNumber);

    public void processProductEvents(ProductEvent productEvent);
}
