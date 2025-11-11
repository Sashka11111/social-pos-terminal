package com.liamtseva.cafepossystem.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoyaltyCard(
    UUID id,
    UUID userId,
    String cardNumber,
    double balance,
    boolean isActive,
    LocalDateTime createdAt
) implements Entity {

  @Override
  public UUID id() {
    return id;
  }
}
