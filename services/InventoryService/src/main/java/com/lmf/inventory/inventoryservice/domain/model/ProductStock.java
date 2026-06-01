package com.lmf.inventory.inventoryservice.domain.model;

import com.lmf.inventory.inventoryservice.domain.exception.InsufficientStockException;
import com.lmf.inventory.inventoryservice.domain.exception.InvalidStockException;
import lombok.Getter;

@Getter
public class ProductStock {

    private Integer availableQuantity;

    private Integer reservedQuantity;

    public ProductStock(Integer availableQuantity, Integer reservedQuantity) {

        if (availableQuantity < 0) {
            throw new InvalidStockException("Available quantity cannot be negative");
        }

        if (reservedQuantity < 0) {
            throw new InvalidStockException("Reserved quantity cannot be negative");
        }

        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    public void reserve(Integer quantity) {

        validateQuantity(quantity);

        if (availableQuantity < quantity) {

            throw new InsufficientStockException("Insufficient stock");
        }

        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    public void release(Integer quantity) {

        validateQuantity(quantity);

        if (reservedQuantity < quantity) {

            throw new InvalidStockException("Reserved quantity is insufficient");
        }

        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    public void confirm(Integer quantity) {

        validateQuantity(quantity);

        if (reservedQuantity < quantity) {

            throw new InvalidStockException("Reserved quantity is insufficient");
        }

        reservedQuantity -= quantity;
    }

    public void increase(Integer quantity) {

        validateQuantity(quantity);

        availableQuantity += quantity;
    }

    public void decrease(Integer quantity) {

        validateQuantity(quantity);

        if (availableQuantity < quantity) {

            throw new InsufficientStockException("Insufficient stock");
        }

        availableQuantity -= quantity;
    }

    private void validateQuantity(Integer quantity) {

        if (quantity == null || quantity <= 0) {

            throw new InvalidStockException("Quantity must be greater than zero");
        }
    }
}
