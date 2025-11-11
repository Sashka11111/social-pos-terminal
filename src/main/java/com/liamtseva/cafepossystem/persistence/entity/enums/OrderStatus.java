package com.liamtseva.cafepossystem.persistence.entity.enums;

public enum OrderStatus {
    PENDING("В обробці"),
    CONFIRMED("Підтверджено"),
    DELIVERED("Доставлено"),
    CANCELLED("Скасовано");

    private final String ukrainianName;

    OrderStatus(String ukrainianName) {
        this.ukrainianName = ukrainianName;
    }

    public String getUkrainianName() {
        return ukrainianName;
    }

    @Override
    public String toString() {
        return ukrainianName;
    }
}