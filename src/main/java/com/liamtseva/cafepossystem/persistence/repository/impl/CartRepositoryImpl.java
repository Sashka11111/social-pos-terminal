package com.liamtseva.cafepossystem.persistence.repository.impl;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.Cart;
import com.liamtseva.cafepossystem.persistence.repository.contract.CartRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartRepositoryImpl implements CartRepository {

    private final DataSource dataSource;

    public CartRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Cart findById(UUID id) throws EntityNotFoundException {
        String query = "SELECT * FROM Cart WHERE cart_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToCart(resultSet);
                } else {
                    throw new EntityNotFoundException("Cart with ID " + id + " not found");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Error finding cart with ID " + id, e);
        }
    }

    @Override
    public List<Cart> findByUserId(UUID userId) {
        List<Cart> carts = new ArrayList<>();
        String query = "SELECT * FROM Cart WHERE user_id = ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Cart cart = mapToCart(resultSet);
                    carts.add(cart);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch carts for user " + userId, e);
        }
        return carts;
    }

    @Override
    public List<Cart> findAll() {
        List<Cart> carts = new ArrayList<>();
        String query = "SELECT * FROM Cart";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                carts.add(mapToCart(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch carts", e);
        }
        return carts;
    }

    @Override
    public Cart save(Cart cart) {
        boolean isUpdate = false;
        if (cart.id() != null) {
            try {
                findById(cart.id());
                isUpdate = true;
            } catch (EntityNotFoundException e) {
                isUpdate = false;
            }
        }
        
        String query = isUpdate
                ? "UPDATE Cart SET user_id = ?, item_id = ?, quantity = ?, subtotal = ?, is_ordered = ? WHERE cart_id = ?"
                : "INSERT INTO Cart (cart_id, user_id, item_id, quantity, subtotal, is_ordered) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            UUID id = cart.id() == null ? UUID.randomUUID() : cart.id();
            int index = 1;
            
            if (!isUpdate) {
                preparedStatement.setString(index++, id.toString());
            }
            
            preparedStatement.setString(index++, cart.userId().toString());
            preparedStatement.setString(index++, cart.itemId().toString());
            preparedStatement.setInt(index++, cart.quantity());
            preparedStatement.setDouble(index++, cart.subtotal());
            preparedStatement.setBoolean(index++, cart.isOrdered());
            
            if (isUpdate) {
                preparedStatement.setString(index, id.toString());
            }
            
            preparedStatement.executeUpdate();
            Cart savedCart = new Cart(id, cart.userId(), cart.itemId(), cart.quantity(), cart.subtotal(), cart.isOrdered());
            return savedCart;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public void deleteById(UUID id) throws EntityNotFoundException {
        String query = "DELETE FROM Cart WHERE cart_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Cart with ID " + id + " not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Cart mapToCart(ResultSet resultSet) throws SQLException {
        return new Cart(
                UUID.fromString(resultSet.getString("cart_id")),
                UUID.fromString(resultSet.getString("user_id")),
                UUID.fromString(resultSet.getString("item_id")),
                resultSet.getInt("quantity"),
                resultSet.getDouble("subtotal"),
                resultSet.getBoolean("is_ordered")
        );
    }
}
