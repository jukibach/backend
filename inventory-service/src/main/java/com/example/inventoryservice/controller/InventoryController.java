package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.response.CommandInventoryResponse;
import com.example.inventoryservice.response.InventoryResponse;
import com.example.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RequestMapping("/api/inventories")
@RestController
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(OK)
    public List<InventoryResponse> getInventories(
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String sortOrder) {
        return inventoryService.getInventories(pageIndex, pageSize, sortBy, sortOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getInventoryById(@PathVariable int id) {
        InventoryResponse response = inventoryService.getInventoryById(id);
        if(response == null) {
            return new ResponseEntity<>(NOT_FOUND);
        }
        return new ResponseEntity<>(response, OK);
    }

    @GetMapping("/sku-code/{skuCode}")
    public ResponseEntity<Object> getInventoryBySerialNumber(@PathVariable String skuCode) {
        Inventory inventory = inventoryService.getInventoryBySKUCode(skuCode);
        if(inventory == null) {
            return new ResponseEntity<>(NOT_FOUND);
        }
        return new ResponseEntity<>(inventory, OK);
    }

    @PostMapping
    @ResponseStatus(OK)
    public ResponseEntity<Object> createInventory(@RequestBody InventoryRequest request) throws IllegalAccessException {
        CommandInventoryResponse response = inventoryService.createInventory(request);
        if(response == null) {
            return new ResponseEntity<>("SKU does not exist in products! Please validate again!", BAD_REQUEST);
        }
        return new ResponseEntity<>(response, OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable long id, @RequestBody InventoryRequest request)
            throws IllegalAccessException {
        CommandInventoryResponse response = inventoryService.updateInventory(id, request);
        if(response == null) {
            return new ResponseEntity<>(NOT_FOUND);
        }
        return new ResponseEntity<>(response, OK);
    }
}