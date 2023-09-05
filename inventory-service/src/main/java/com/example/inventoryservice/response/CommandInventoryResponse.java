package com.example.inventoryservice.response;

import com.example.inventoryservice.dto.ErrorMessage;
import com.example.inventoryservice.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandInventoryResponse {
    Inventory inventory;
    List<ErrorMessage> errorMessages;

    public CommandInventoryResponse(List<ErrorMessage> errors) {
        setErrorMessages(errors);
    }

    public CommandInventoryResponse(Inventory inventory) {
        setInventory(inventory);
    }

}
