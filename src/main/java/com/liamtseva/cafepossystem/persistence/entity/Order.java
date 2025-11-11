package com.liamtseva.cafepossystem.persistence.entity;

import com.liamtseva.cafepossystem.persistence.entity.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record Order(
    UUID id,
    UUID userId,
    LocalDateTime orderDate,
    double totalAmount,
    double bonusesEarned,
    double bonusesUsed,
    OrderStatus status,
    String notes,
    boolean isSocial,
    Integer tableNumber
) implements Entity, Comparable<Order> {

  @Override
  public int compareTo(Order o) {
    return this.orderDate.compareTo(o.orderDate);
  }

  @Override
  public UUID id() {
    return id;
  }
}
