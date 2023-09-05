package com.example.backend.response;

import com.example.backend.dto.ErrorMessage;
import com.example.backend.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    Product product;
    List<ErrorMessage> errorMessages;

    public ProductResponse(List<ErrorMessage> errors) {
        setErrorMessages(errors);
    }

    public ProductResponse(Product product) {
        setProduct(product);
    }
}
