package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.response.CommandInventoryResponse;
import com.example.inventoryservice.response.InventoryResponse;

import java.util.List;

public interface InventoryService {
    public List<InventoryResponse> getInventories(int pageIndex, int pageSize, String sortBy, String sortOrder);

    public InventoryResponse getInventoryById(int id);

    public Inventory getInventoryBySKUCode(String code);

    public CommandInventoryResponse createInventory(InventoryRequest request) throws IllegalAccessException;

    public CommandInventoryResponse updateInventory(long id, InventoryRequest request) throws IllegalAccessException;
}
