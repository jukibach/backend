package com.example.inventoryservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponse {
    private Long id;
    private Integer quantity;
    private String skuCode;
    private boolean isInStock;
    private Date createdDate;
    private Date updatedDate;

}
