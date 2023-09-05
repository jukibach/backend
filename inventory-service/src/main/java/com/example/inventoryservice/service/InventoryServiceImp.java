package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.ErrorMessage;
import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.entity.Product;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.response.CommandInventoryResponse;
import com.example.inventoryservice.response.InventoryResponse;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Service
public class InventoryServiceImp implements InventoryService {

    private final String serverTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    WebClient.Builder webClientBuilder;
    @Autowired
    private ObservationRegistry observationRegistry;

    @Override
    public List<InventoryResponse> getInventories(int pageIndex, int pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort
                .by(sortBy)
                .descending();
        Pageable pagination = PageRequest.of(pageIndex, pageSize, sort);
        return inventoryRepository
                .findAll(pagination)
                .getContent()
                .stream()
                .map(value -> InventoryResponse
                        .builder()
                        .id(value.getId())
                        .quantity(value.getQuantity())
                        .skuCode(value.getSkuCode())
                        .isInStock(value.getQuantity() > 0)
                        .createdDate(value.getCreatedDate())
                        .updatedDate(value.getUpdatedDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public InventoryResponse getInventoryById(int id) {
        Optional<Inventory> inventory = inventoryRepository.findById((long) id);
        return inventory
                .map(value -> InventoryResponse
                        .builder()
                        .id(value.getId())
                        .quantity(value.getQuantity())
                        .skuCode(value.getSkuCode())
                        .isInStock(value.getQuantity() > 0)
                        .createdDate(value.getCreatedDate())
                        .updatedDate(value.getUpdatedDate())
                        .build())
                .orElse(null);
    }

    @Override
    public Inventory getInventoryBySKUCode(String code) {
        Optional<Inventory> inventory = Optional.ofNullable(inventoryRepository.findBySkuCode(code));
        return inventory.orElse(null);
    }

    @Override
    @Transactional
    public CommandInventoryResponse createInventory(InventoryRequest request) {
        Observation serviceObservation = Observation
                .createNotStarted("products-query-service-lookup", this.observationRegistry)
                .lowCardinalityKeyValue("http.url", "/api/products-query/serialNumbers/{serialNumber}")
                .highCardinalityKeyValue("http.full-url", "/api/products-query/serialNumbers/" + request.getSkuCode());

        return serviceObservation.observe(() -> {
            Optional<Product> product = Optional.ofNullable(getProductBySerialNumber(request.getSkuCode()));
            if(product.isEmpty()) {
                return null;
            }
            Inventory inventory = new Inventory();
            inventory.setSkuCode(request.getSkuCode());
            inventory.setQuantity(request.getQuantity());
            inventory.setCreatedDate(new Date());
            List<ErrorMessage> errors;
            try {
                errors = checkFields(inventory);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            Optional<Inventory> existingInventory = Optional.ofNullable(getInventoryBySKUCode(inventory.getSkuCode()));
            if(existingInventory.isPresent()) {
                ErrorMessage errorMessage = new ErrorMessage(BAD_REQUEST, "Duplicate SKU Code", serverTime);
                errors.add(errorMessage);
            }
            if(errors.size() > 0) {
                return new CommandInventoryResponse(errors);
            }
            Inventory result = inventoryRepository.save(inventory);
            return new CommandInventoryResponse(result);
        });

    }

    @Override
    public CommandInventoryResponse updateInventory(long id, InventoryRequest request) throws IllegalAccessException {
        Inventory existingInventory = inventoryRepository.findById(id).orElse(null);
        if(existingInventory == null) {
            return null;
        }
        existingInventory.setSkuCode(request.getSkuCode());
        existingInventory.setQuantity(request.getQuantity());
        List<ErrorMessage> errors = checkFields(existingInventory);
        Optional<Inventory> inventory = Optional.ofNullable(getInventoryBySKUCode(request.getSkuCode()));
        if(inventory.isPresent() && !existingInventory.getId().equals(inventory.get().getId())) {
            ErrorMessage errorMessage = new ErrorMessage(BAD_REQUEST, "Duplicate SKU Code", serverTime);
            errors.add(errorMessage);
        }
        if(errors.size() > 0) {
            return new CommandInventoryResponse(errors);
        }
        existingInventory.setUpdatedDate(new Date());
        Inventory result = inventoryRepository.save(existingInventory);
        return new CommandInventoryResponse(result);
    }

    private List<ErrorMessage> checkFields(Inventory inventory) throws IllegalAccessException {
        List<ErrorMessage> errors = new ArrayList<>();
        for (Field field : inventory.getClass().getDeclaredFields()) {
            field.setAccessible(true); // to allow the access of member attributes
            if(field.getName().equals("id") || field.getName().equals("updatedDate")) {
                continue;
            }
            Object attribute = field.get(inventory);
            if(attribute == null || attribute.equals("")) {
                ErrorMessage
                        errorMessage
                        = new ErrorMessage(NOT_FOUND, field.getName() + " does not exist", serverTime);
                errors.add(errorMessage);
            }
        }
        return errors;
    }

    private Product getProductBySerialNumber(String serialNumber) {
        return webClientBuilder
                .build()
                .get()
                .uri("http://product-query-service/api/products-query/serialNumbers/{serialNumber}", serialNumber)
                .retrieve()
                .bodyToMono(Product.class)
                .block();
    }
}
