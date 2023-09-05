package com.example.backend.mapstruct;

import com.example.backend.dto.CreateProductRequest;
import com.example.backend.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProductMapper {

    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    Product createProductToProduct(CreateProductRequest request);
}
