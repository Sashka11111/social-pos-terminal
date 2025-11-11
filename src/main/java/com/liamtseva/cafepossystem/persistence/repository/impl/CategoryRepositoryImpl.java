package com.liamtseva.cafepossystem.persistence.repository.impl;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.Category;
import com.liamtseva.cafepossystem.persistence.repository.contract.CategoryRepository;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategoryRepositoryImpl implements CategoryRepository {
  private final DataSource dataSource;

  public CategoryRepositoryImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Category findById(UUID id) throws EntityNotFoundException {
    String query = "SELECT * FROM Categories WHERE category_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, id.toString());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return mapToCategory(resultSet);
        } else {
          throw new EntityNotFoundException("Категорію з ID " + id + " не знайдено");
        }
      }
    } catch (SQLException e) {
      throw new EntityNotFoundException("Помилка під час пошуку категорії з ID " + id, e);
    }
  }

  @Override
  public Category findByName(String categoryName) throws EntityNotFoundException {
    String query = "SELECT * FROM Categories WHERE category_name = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, categoryName);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return mapToCategory(resultSet);
        } else {
          throw new EntityNotFoundException("Категорію з назвою " + categoryName + " не знайдено");
        }
      }
    } catch (SQLException e) {
      throw new EntityNotFoundException("Помилка під час пошуку категорії з назвою " + categoryName, e);
    }
  }

  @Override
  public List<Category> findAll() {
    List<Category> categories = new ArrayList<>();
    String query = "SELECT * FROM Categories";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      while (resultSet.next()) {
        categories.add(mapToCategory(resultSet));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return categories;
  }

  @Override
  public Category save(Category category) {
    String query = category.id() == null
        ? "INSERT INTO Categories (category_id, category_name) VALUES (?, ?)"
        : "UPDATE Categories SET category_name = ? WHERE category_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      UUID id = category.id() == null ? UUID.randomUUID() : category.id();
      int index = 1;
      if (category.id() == null) {
        preparedStatement.setString(index++, id.toString());
      }
      preparedStatement.setString(index++, category.categoryName());
      if (category.id() != null) {
        preparedStatement.setString(index, id.toString());
      }
      preparedStatement.executeUpdate();
      return new Category(id, category.categoryName());
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void deleteById(UUID id) throws EntityNotFoundException {
    String query = "DELETE FROM Categories WHERE category_id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setString(1, id.toString());
      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        throw new EntityNotFoundException("Категорію з ID " + id + " не знайдено");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private Category mapToCategory(ResultSet resultSet) throws SQLException {
    return new Category(
        UUID.fromString(resultSet.getString("category_id")),
        resultSet.getString("category_name")
    );
  }
}
