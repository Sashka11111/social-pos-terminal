package com.liamtseva.cafepossystem.persistence.entity;

import com.liamtseva.cafepossystem.persistence.entity.enums.BonusTransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record BonusTransaction(
    UUID id,
    UUID cardId,
    UUID orderId,
    double amount,
    BonusTransactionType type,
    LocalDateTime transactionDate,
    String notes
) implements Entity {

    @Override
    public UUID id() {
        return id;
    }
}
