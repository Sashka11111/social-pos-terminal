package com.liamtseva.cafepossystem.presentation.validation;

import com.liamtseva.cafepossystem.persistence.entity.Order;
import com.liamtseva.cafepossystem.persistence.entity.enums.OrderStatus;
import com.liamtseva.cafepossystem.persistence.repository.impl.OrderRepositoryImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderValidator {
  private static final int MAX_NOTES_LENGTH = 500;
  private static final double MIN_TOTAL_AMOUNT = 0.01;
  private static final double MAX_TOTAL_AMOUNT = 100000.00;

  public static ValidationResult isOrderIdValid(UUID orderId, boolean isExisting) {
    if (isExisting && orderId == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Ідентифікатор замовлення не може бути відсутнім для існуючого замовлення");
      return new ValidationResult(false, errors);
    }
    return new ValidationResult(true);
  }

  public static ValidationResult isOrderDateValid(LocalDateTime orderDate) {
    List<String> errors = new ArrayList<>();
    if (orderDate == null) {
      errors.add("Дата замовлення не може бути відсутньою");
      return new ValidationResult(false, errors);
    }
    if (orderDate.isAfter(LocalDateTime.now())) {
      errors.add("Дата замовлення не може бути в майбутньому");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isTotalAmountValid(double totalAmount) {
    List<String> errors = new ArrayList<>();
    if (totalAmount < MIN_TOTAL_AMOUNT) {
      errors.add("Загальна сума (" + totalAmount + ") повинна бути не меншою за " + MIN_TOTAL_AMOUNT);
    }
    if (totalAmount > MAX_TOTAL_AMOUNT) {
      errors.add("Загальна сума (" + totalAmount + ") не може перевищувати " + MAX_TOTAL_AMOUNT);
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isStatusValid(OrderStatus status) {
    List<String> errors = new ArrayList<>();
    if (status == null) {
      errors.add("Статус замовлення не може бути відсутнім");
      return new ValidationResult(false, errors);
    }
    return new ValidationResult(true);
  }

  public static ValidationResult isNotesValid(String notes) {
    if (notes == null) {
      return new ValidationResult(true);
    }
    List<String> errors = new ArrayList<>();
    if (notes.length() > MAX_NOTES_LENGTH) {
      errors.add("Примітки (довжина: " + notes.length() + " символів) не можуть перевищувати " + MAX_NOTES_LENGTH + " символів");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isOrderValid(Order order, boolean isExisting, OrderRepositoryImpl repository) {
    if (order == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Замовлення не може бути відсутнім");
      return new ValidationResult(false, errors);
    }

    List<String> errors = new ArrayList<>();

      ValidationResult orderIdResult = isOrderIdValid(order.id(), isExisting);
    if (!orderIdResult.isValid()) {
      errors.addAll(orderIdResult.getErrors());
    }

      ValidationResult orderDateResult = isOrderDateValid(order.orderDate());
    if (!orderDateResult.isValid()) {
      errors.addAll(orderDateResult.getErrors());
    }

      ValidationResult totalAmountResult = isTotalAmountValid(order.totalAmount());
    if (!totalAmountResult.isValid()) {
      errors.addAll(totalAmountResult.getErrors());
    }

      ValidationResult statusResult = isStatusValid(order.status());
    if (!statusResult.isValid()) {
      errors.addAll(statusResult.getErrors());
    }

      ValidationResult notesResult = isNotesValid(order.notes());
    if (!notesResult.isValid()) {
      errors.addAll(notesResult.getErrors());
    }

    return new ValidationResult(errors.isEmpty(), errors);
  }
}
