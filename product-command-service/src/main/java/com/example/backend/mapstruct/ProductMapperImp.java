package com.example.backend.mapstruct;

import com.example.backend.dto.CreateProductRequest;
import com.example.backend.entity.Product;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ProductMapperImp implements ProductMapper {
    @Override
    public Product createProductToProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setSerialNumber(request.getSerialNumber());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCreatedDate(new Date());
        return product;
    }
}
