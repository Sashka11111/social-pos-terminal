package com.liamtseva.cafepossystem.persistence.repository.impl;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.Order;
import com.liamtseva.cafepossystem.persistence.entity.enums.OrderStatus;
import com.liamtseva.cafepossystem.persistence.repository.contract.OrderRepository;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderRepositoryImpl implements OrderRepository {
  private final DataSource dataSource;

  public OrderRepositoryImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Order findById(UUID id) throws EntityNotFoundException {
    String query = "SELECT * FROM Orders WHERE order_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, id.toString());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return mapToOrder(resultSet);
        } else {
          throw new EntityNotFoundException("Замовлення з ID " + id + " не знайдено");
        }
      }
    } catch (SQLException e) {
      throw new EntityNotFoundException("Помилка під час пошуку замовлення з ID " + id, e);
    }
  }

  @Override
  public List<Order> findAll() {
    List<Order> orders = new ArrayList<>();
    String query = "SELECT * FROM Orders";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      while (resultSet.next()) {
        orders.add(mapToOrder(resultSet));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Не вдалося отримати список замовлень", e);
    }
    return orders;
  }

  @Override
  public List<String> findCartIdsByOrderId(UUID orderId) {
    List<String> cartIds = new ArrayList<>();
    String query = "SELECT cart_id FROM OrderCartItems WHERE order_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, orderId.toString());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          cartIds.add(resultSet.getString("cart_id"));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Не вдалося отримати позиції кошика для замовлення " + orderId, e);
    }
    return cartIds;
  }
  @Override
  public List<Order> findByUserId(UUID userId) {
    List<Order> orders = new ArrayList<>();

      String query =
        "SELECT * FROM Orders WHERE user_id = ?";

    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(query)) {

        preparedStatement.setString(1, userId.toString());

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                orders.add(mapToOrder(resultSet));
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException("Не вдалося отримати замовлення користувача " + userId, e);
    }

    return orders;
  }
  @Override
  public Order create(Order order, List<String> cartIds) {
    String query = "INSERT INTO Orders (order_id, user_id, order_date, total_amount, bonuses_earned, bonuses_used, status, notes, is_social, table_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
        preparedStatement.setString(1, order.id().toString());
        preparedStatement.setString(2, order.userId().toString());
        preparedStatement.setTimestamp(3, java.sql.Timestamp.valueOf(order.orderDate()));
        preparedStatement.setDouble(4, order.totalAmount());
        preparedStatement.setDouble(5, order.bonusesEarned());
        preparedStatement.setDouble(6, order.bonusesUsed());
        preparedStatement.setString(7, order.status().name());
        preparedStatement.setString(8, order.notes());
        preparedStatement.setBoolean(9, order.isSocial());
        preparedStatement.setObject(10, order.tableNumber());
        int affectedRows = preparedStatement.executeUpdate();
        if (affectedRows > 0) {
            if (cartIds != null && !cartIds.isEmpty()) {
            String linkQuery = "INSERT INTO OrderCartItems (order_id, cart_id) VALUES (?, ?)";
            try (PreparedStatement linkStatement = connection.prepareStatement(linkQuery)) {
              for (String cartId : cartIds) {
                linkStatement.setString(1, order.id().toString());
                linkStatement.setString(2, cartId);
                linkStatement.executeUpdate();
              }
            }
          }
          connection.commit();
          return order;
        } else {
          connection.rollback();
          return null;
        }
      } catch (SQLException e) {
        connection.rollback();
        return null;
      }
    } catch (SQLException e) {
      return null;
    }
  }

  @Override
  public Order update(Order order, List<String> cartIds) throws EntityNotFoundException {
    String query = "UPDATE Orders SET user_id = ?, order_date = ?, total_amount = ?, bonuses_earned = ?, bonuses_used = ?, status = ?, notes = ?, is_social = ?, table_number = ? WHERE order_id = ?";
    try (Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
        preparedStatement.setString(1, order.userId().toString());
        preparedStatement.setTimestamp(2, java.sql.Timestamp.valueOf(order.orderDate()));
        preparedStatement.setDouble(3, order.totalAmount());
        preparedStatement.setDouble(4, order.bonusesEarned());
        preparedStatement.setDouble(5, order.bonusesUsed());
        preparedStatement.setString(6, order.status().name());
        preparedStatement.setString(7, order.notes());
        preparedStatement.setBoolean(8, order.isSocial());
        preparedStatement.setObject(9, order.tableNumber());
        preparedStatement.setString(10, order.id().toString());
        int affectedRows = preparedStatement.executeUpdate();
        if (affectedRows == 0) {
          connection.rollback();
          throw new EntityNotFoundException("Замовлення з ID " + order.id() + " не знайдено");
        }
          String deleteLinksQuery = "DELETE FROM OrderCartItems WHERE order_id = ?";
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteLinksQuery)) {
          deleteStatement.setString(1, order.id().toString());
          deleteStatement.executeUpdate();
        }
        if (cartIds != null && !cartIds.isEmpty()) {
          String linkQuery = "INSERT INTO OrderCartItems (order_id, cart_id) VALUES (?, ?)";
          try (PreparedStatement linkStatement = connection.prepareStatement(linkQuery)) {
            for (String cartId : cartIds) {
              linkStatement.setString(1, order.id().toString());
              linkStatement.setString(2, cartId);
              linkStatement.executeUpdate();
            }
          }
        }
        connection.commit();
        return order;
      } catch (SQLException e) {
        connection.rollback();
        return null;
      }
    } catch (SQLException e) {
      return null;
    }
  }

  @Override
  public void deleteById(UUID id) throws EntityNotFoundException {
    try (Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
        try {
        String deleteLinksQuery = "DELETE FROM OrderCartItems WHERE order_id = ?";
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteLinksQuery)) {
          deleteStatement.setString(1, id.toString());
          deleteStatement.executeUpdate();
        }
        String query = "DELETE FROM Orders WHERE order_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
          preparedStatement.setString(1, id.toString());
          int affectedRows = preparedStatement.executeUpdate();
          if (affectedRows == 0) {
            connection.rollback();
            throw new EntityNotFoundException("Замовлення з ID " + id + " не знайдено");
          }
        }
        connection.commit();
      } catch (SQLException e) {
        connection.rollback();
        throw new EntityNotFoundException("Помилка при видаленні замовлення з ID " + id, e);
      }
    } catch (SQLException e) {
      throw new EntityNotFoundException("Помилка транзакції при видаленні замовлення з ID " + id, e);
    }
  }
  private Order mapToOrder(ResultSet resultSet) throws SQLException {
    return new Order(
        UUID.fromString(resultSet.getString("order_id")),
        UUID.fromString(resultSet.getString("user_id")),
        resultSet.getTimestamp("order_date").toLocalDateTime(),
        resultSet.getDouble("total_amount"),
        resultSet.getDouble("bonuses_earned"),
        resultSet.getDouble("bonuses_used"),
        OrderStatus.valueOf(resultSet.getString("status")),
        resultSet.getString("notes"),
        resultSet.getBoolean("is_social"),
        (Integer) resultSet.getObject("table_number")
    );
  }
}
