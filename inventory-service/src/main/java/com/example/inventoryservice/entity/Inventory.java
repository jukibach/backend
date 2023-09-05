package com.example.inventoryservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Inventory", indexes = {
        @Index(name = "inventory_id_idx", columnList = "id")
})
public class Inventory implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "skuCode", nullable = false, unique = true)
    private String skuCode;
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "created_date", nullable = false)
    private Date createdDate;
    private Date updatedDate;
}
