package com.lmf.inventory.inventoryservice.infrastructure.persistence.entity;

import com.lmf.inventory.inventoryservice.domain.model.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer reservedQuantity;

    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    @Version
    private Long version;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
