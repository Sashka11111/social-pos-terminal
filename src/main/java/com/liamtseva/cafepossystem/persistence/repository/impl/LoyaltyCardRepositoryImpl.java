package com.liamtseva.cafepossystem.persistence.repository.impl;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.LoyaltyCard;
import com.liamtseva.cafepossystem.persistence.repository.contract.LoyaltyCardRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoyaltyCardRepositoryImpl implements LoyaltyCardRepository {

    private final DataSource dataSource;

    public LoyaltyCardRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public LoyaltyCard findById(UUID id) throws EntityNotFoundException {
        String query = "SELECT * FROM LoyaltyCards WHERE card_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToLoyaltyCard(resultSet);
                } else {
                    throw new EntityNotFoundException("Loyalty card with ID " + id + " not found");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Error finding loyalty card with ID " + id, e);
        }
    }

    @Override
    public LoyaltyCard findByUserId(UUID userId) throws EntityNotFoundException {
        String query = "SELECT * FROM LoyaltyCards WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToLoyaltyCard(resultSet);
                } else {
                    throw new EntityNotFoundException("Loyalty card for user ID " + userId + " not found");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Error finding loyalty card for user ID " + userId, e);
        }
    }

    @Override
    public List<LoyaltyCard> findAll() {
        List<LoyaltyCard> loyaltyCards = new ArrayList<>();
        String query = "SELECT * FROM LoyaltyCards";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                loyaltyCards.add(mapToLoyaltyCard(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loyaltyCards;
    }

    @Override
    public LoyaltyCard save(LoyaltyCard loyaltyCard) {
    LocalDateTime createdAt = loyaltyCard.createdAt() != null ? loyaltyCard.createdAt() : LocalDateTime.now();
    String query = loyaltyCard.id() == null
    ? "INSERT INTO LoyaltyCards (card_id, user_id, card_number, balance, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE LoyaltyCards SET user_id = ?, card_number = ?, balance = ?, is_active = ? WHERE card_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            UUID id = loyaltyCard.id() == null ? UUID.randomUUID() : loyaltyCard.id();
            int index = 1;
            if (loyaltyCard.id() == null) {
                preparedStatement.setString(index++, id.toString());
                if (loyaltyCard.userId() != null) {
                    preparedStatement.setString(index++, loyaltyCard.userId().toString());
                } else {
                    preparedStatement.setObject(index++, null);
                }
                preparedStatement.setString(index++, loyaltyCard.cardNumber());
                preparedStatement.setDouble(index++, loyaltyCard.balance());
                preparedStatement.setBoolean(index++, loyaltyCard.isActive());
                preparedStatement.setTimestamp(index, java.sql.Timestamp.valueOf(createdAt));
            } else {
                if (loyaltyCard.userId() != null) {
                    preparedStatement.setString(index++, loyaltyCard.userId().toString());
                } else {
                    preparedStatement.setObject(index++, null);
                }
                preparedStatement.setString(index++, loyaltyCard.cardNumber());
                preparedStatement.setDouble(index++, loyaltyCard.balance());
                preparedStatement.setBoolean(index++, loyaltyCard.isActive());
                preparedStatement.setString(index, id.toString());
            }
            preparedStatement.executeUpdate();
            return new LoyaltyCard(id, loyaltyCard.userId(), loyaltyCard.cardNumber(), loyaltyCard.balance(), loyaltyCard.isActive(), createdAt);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteById(UUID id) throws EntityNotFoundException {
        String query = "DELETE FROM LoyaltyCards WHERE card_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Loyalty card with ID " + id + " not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private LoyaltyCard mapToLoyaltyCard(ResultSet resultSet) throws SQLException {
        String userIdStr = resultSet.getString("user_id");
        UUID userId = null;
        try {
            if (userIdStr != null) {
                userId = UUID.fromString(userIdStr);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Попередження: Не вдалося розпарсити невалідний user_id: " + userIdStr);
        }
        return new LoyaltyCard(
                UUID.fromString(resultSet.getString("card_id")),
                userId,
                resultSet.getString("card_number"),
                resultSet.getDouble("balance"),
                resultSet.getBoolean("is_active"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
