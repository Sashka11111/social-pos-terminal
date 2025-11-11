package com.liamtseva.cafepossystem.persistence.entity;

import java.util.UUID;

public record Cart(
    UUID id,
    UUID userId,
    UUID itemId,
    int quantity,
    double subtotal,
    boolean isOrdered
) implements Entity {

    @Override
    public UUID id() {
        return id;
    }
}
