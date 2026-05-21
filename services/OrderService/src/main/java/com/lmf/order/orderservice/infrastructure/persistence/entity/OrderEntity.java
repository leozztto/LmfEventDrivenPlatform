package com.lmf.order.orderservice.infrastructure.persistence.entity;

import com.lmf.order.orderservice.domain.model.order.OrderStatus;
import com.lmf.order.orderservice.infrastructure.persistence.entity.embedded.CustomerEmbeddable;
import com.lmf.order.orderservice.infrastructure.persistence.entity.embedded.PaymentInfoEmbeddable;
import com.lmf.order.orderservice.infrastructure.persistence.entity.embedded.ShippingAddressEmbeddable;
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

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "customerId", column = @Column(name = "customer_id")), @AttributeOverride(name = "name", column = @Column(name = "customer_name")), @AttributeOverride(name = "email", column = @Column(name = "customer_email")), @AttributeOverride(name = "phone", column = @Column(name = "customer_phone"))})
    private CustomerEmbeddable customer;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "street", column = @Column(name = "shipping_street")), @AttributeOverride(name = "number", column = @Column(name = "shipping_number")), @AttributeOverride(name = "city", column = @Column(name = "shipping_city")), @AttributeOverride(name = "zipCode", column = @Column(name = "shipping_zip_code")), @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))})
    private ShippingAddressEmbeddable shippingAddress;

    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "paymentMethod", column = @Column(name = "payment_method")), @AttributeOverride(name = "installments", column = @Column(name = "payment_installments")), @AttributeOverride(name = "paid_amount", column = @Column(name = "payment_paid_amount"))})
    private PaymentInfoEmbeddable paymentInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "orderEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItemsEntities = new ArrayList<>();

    public OrderEntity(UUID id, CustomerEmbeddable customer, ShippingAddressEmbeddable shippingAddress, PaymentInfoEmbeddable paymentInfo, OrderStatus orderStatus, BigDecimal totalAmount, OffsetDateTime createdAt) {
        this.id = id;
        this.customer = customer;
        this.shippingAddress = shippingAddress;
        this.paymentInfo = paymentInfo;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public void addItem(OrderItemEntity orderItemEntity) {
        orderItemEntity.linkToOrder(this);
        this.orderItemsEntities.add(orderItemEntity);
    }
}