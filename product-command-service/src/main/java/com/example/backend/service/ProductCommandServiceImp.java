package com.example.backend.service;

import com.example.backend.cache.CacheClass;
import com.example.backend.dto.CreateProductRequest;
import com.example.backend.dto.ErrorMessage;
import com.example.backend.dto.ProductEvent;
import com.example.backend.dto.UpdateProductRequest;
import com.example.backend.entity.Product;
import com.example.backend.mapstruct.ProductMapper;
import com.example.backend.repository.ProductRepository;
import com.example.backend.response.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;


@Service
public class ProductCommandServiceImp implements ProductCommandService {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductMapper mapper;
    @Autowired
    CacheClass cacheClass;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public ProductResponse createProduct(CreateProductRequest request) throws IllegalAccessException {
        Product product = mapper.createProductToProduct(request);
        List<ErrorMessage> errors = checkFields(product);
        if(errors.size() > 0) {
            return new ProductResponse(errors);
        }
        Product result = cacheClass.saveProduct(product);
        ProductEvent event = new ProductEvent("CreateProduct", result);
        kafkaTemplate.send("product-event-topic", event);
        return new ProductResponse(result);
    }

    @Override
    public ProductResponse updateProduct(long id, UpdateProductRequest request) throws IllegalAccessException {
        Product existingProduct = productRepository.findById(id).orElse(null);
        if(existingProduct == null) {
            return null;
        }
        existingProduct.setName(request.getName());
        existingProduct.setSerialNumber(request.getSerialNumber());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setUpdatedDate(new Date());
        List<ErrorMessage> errors = checkFields(existingProduct);
        if(errors.size() > 0) {
            return new ProductResponse(errors);
        }
        Product result = cacheClass.saveProduct(existingProduct);
        ProductEvent event = new ProductEvent("UpdateProduct", result);
        kafkaTemplate.send("product-event-topic", event);
        return new ProductResponse(result);
    }

    private List<ErrorMessage> checkFields(Product product) throws IllegalAccessException {
        List<ErrorMessage> errors = new ArrayList<>();
        String serverTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        Product existingProduct = productRepository.findBySerialNumber(product.getSerialNumber());
        if(existingProduct != null && !existingProduct.getId().equals(product.getId())) {
            ErrorMessage errorMessage = new ErrorMessage(NOT_FOUND, "Serial number already existed", serverTime);
            errors.add(errorMessage);
        }
        for (Field field : product.getClass().getDeclaredFields()) {
            field.setAccessible(true); // to allow the access of member attributes
            if(field.getName().equals("id") || field.getName().equals("updatedDate")) {
                continue;
            }
            Object attribute = field.get(product);
            if(attribute == null || attribute.equals("")) {
                ErrorMessage
                        errorMessage
                        = new ErrorMessage(NOT_FOUND, field.getName() + " does not exist", serverTime);
                errors.add(errorMessage);
            }
        }
        return errors;
    }
}
