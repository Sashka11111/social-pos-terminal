package com.liamtseva.cafepossystem.persistence.entity;

import java.util.UUID;

public record MenuItem(
    UUID id,
    String name,
    String description,
    double price,
    Integer calories,
    byte[] image,
    String ingredients
) implements Entity,Comparable<MenuItem> {

  @Override
  public int compareTo(MenuItem o) {
    return this.name.compareTo(o.name);
  }

  @Override
  public UUID id() {
    return id;
  }
}
