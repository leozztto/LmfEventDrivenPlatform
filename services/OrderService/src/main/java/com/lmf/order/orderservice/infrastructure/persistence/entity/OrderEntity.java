package com.lmf.order.orderservice.infrastructure.persistence.entity;

import com.lmf.order.orderservice.domain.model.OrderStatus;
import jakarta.persistence.*;
        import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(
            mappedBy = "orderEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItemEntity> orderItemsEntities = new ArrayList<>();

    public OrderEntity(
            UUID id,
            UUID customerId,
            OrderStatus orderStatus,
            BigDecimal totalAmount,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public void addItem(OrderItemEntity orderItemEntity) {
        orderItemEntity.linkToOrder(this);
        this.orderItemsEntities.add(orderItemEntity);
    }
}