package com.liamtseva.cafepossystem.persistence.repository.contract;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.Order;

import java.util.List;
import java.util.UUID;

public interface OrderRepository {
  Order findById(UUID id) throws EntityNotFoundException;
  List<Order> findAll();
  List<String> findCartIdsByOrderId(UUID orderId);
  List<Order> findByUserId(UUID userId);
  Order create(Order order, List<String> cartIds); // Додаємо cartIds для зв’язків
  Order update(Order order, List<String> cartIds) throws EntityNotFoundException; // Новий метод update
  void deleteById(UUID id) throws EntityNotFoundException;
}
