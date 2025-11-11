package com.liamtseva.cafepossystem.persistence.repository.impl;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.Category;
import com.liamtseva.cafepossystem.persistence.entity.MenuItem;
import com.liamtseva.cafepossystem.persistence.repository.contract.MenuItemRepository;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MenuItemRepositoryImpl implements MenuItemRepository {
    private final DataSource dataSource;

    public MenuItemRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public MenuItem findById(UUID id) throws EntityNotFoundException {
        String query = "SELECT * FROM MenuItems WHERE item_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToMenuItem(resultSet);
                } else {
                    throw new EntityNotFoundException("Елемент меню з ID " + id + " не знайдено");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Помилка під час пошуку елемента меню з ID " + id, e);
        }
    }

    @Override
    public MenuItem findByName(String name) throws EntityNotFoundException {
        String query = "SELECT * FROM MenuItems WHERE name = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToMenuItem(resultSet);
                } else {
                    throw new EntityNotFoundException("Елемент меню з назвою " + name + " не знайдено");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Помилка під час пошуку елемента меню з назвою " + name, e);
        }
    }

    @Override
    public List<MenuItem> findAll() {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT * FROM MenuItems";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                items.add(mapToMenuItem(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public List<MenuItem> findByCategoryId(UUID categoryId) {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT m.* FROM MenuItems m JOIN ItemCategories ic ON m.item_id = ic.item_id WHERE ic.category_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, categoryId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapToMenuItem(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public List<MenuItem> findCartItemsByUserId(UUID userId) {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT m.* FROM MenuItems m JOIN Cart c ON m.item_id = c.item_id WHERE c.user_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapToMenuItem(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public MenuItem save(MenuItem menuItem) {
        String query = menuItem.id() == null
            ? "INSERT INTO MenuItems (item_id, name, description, price, calories, image, ingredients) VALUES (?, ?, ?, ?, ?, ?, ?)"
            : "UPDATE MenuItems SET name = ?, description = ?, price = ?, calories = ?, image = ?, ingredients = ? WHERE item_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            UUID id = menuItem.id() == null ? UUID.randomUUID() : menuItem.id();
            int index = 1;
            if (menuItem.id() == null) {
                preparedStatement.setString(index++, id.toString());
            }
            preparedStatement.setString(index++, menuItem.name());
            preparedStatement.setString(index++, menuItem.description());
            preparedStatement.setDouble(index++, menuItem.price());
            preparedStatement.setObject(index++, menuItem.calories());
            preparedStatement.setBytes(index++, menuItem.image());
            preparedStatement.setString(index++, menuItem.ingredients());
            if (menuItem.id() != null) {
                preparedStatement.setString(index, id.toString());
            }
            preparedStatement.executeUpdate();
            return new MenuItem(id, menuItem.name(), menuItem.description(), menuItem.price(), menuItem.calories(), menuItem.image(), menuItem.ingredients());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteById(UUID id) throws EntityNotFoundException {
        String query = "DELETE FROM MenuItems WHERE item_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Елемент меню з ID " + id + " не знайдено");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private MenuItem mapToMenuItem(ResultSet resultSet) throws SQLException {
        return new MenuItem(
            UUID.fromString(resultSet.getString("item_id")),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getDouble("price"),
            resultSet.getInt("calories") == 0 ? null : resultSet.getInt("calories"),
            resultSet.getBytes("image"),
            resultSet.getString("ingredients")
        );
    }
    @Override
    public List<Category> findCategoriesByItemId(UUID itemId) {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT c.* FROM Categories c " +
                       "JOIN ItemCategories ic ON c.category_id = ic.category_id " +
                       "WHERE ic.item_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, itemId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(new Category(
                        UUID.fromString(resultSet.getString("category_id")),
                        resultSet.getString("category_name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    @Override
    public void saveItemCategories(UUID itemId, List<UUID> categoryIds) {
        String query = "INSERT INTO ItemCategories (item_id, category_id) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (UUID categoryId : categoryIds) {
                preparedStatement.setString(1, itemId.toString());
                preparedStatement.setString(2, categoryId.toString());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void deleteItemCategories(UUID itemId) {
        String query = "DELETE FROM ItemCategories WHERE item_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, itemId.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void updateItemCategories(UUID itemId, List<UUID> categoryIds) {
        deleteItemCategories(itemId);
        saveItemCategories(itemId, categoryIds);
    }
}
