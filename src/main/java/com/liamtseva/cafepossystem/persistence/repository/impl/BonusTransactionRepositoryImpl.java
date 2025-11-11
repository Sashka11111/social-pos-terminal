package com.liamtseva.cafepossystem.persistence.repository.impl;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.BonusTransaction;
import com.liamtseva.cafepossystem.persistence.entity.enums.BonusTransactionType;
import com.liamtseva.cafepossystem.persistence.repository.contract.BonusTransactionRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BonusTransactionRepositoryImpl implements BonusTransactionRepository {

    private final DataSource dataSource;

    public BonusTransactionRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public BonusTransaction findById(UUID id) throws EntityNotFoundException {
        String query = "SELECT * FROM BonusTransactions WHERE transaction_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToBonusTransaction(resultSet);
                } else {
                    throw new EntityNotFoundException("Bonus transaction with ID " + id + " not found");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Error finding bonus transaction with ID " + id, e);
        }
    }

    @Override
    public List<BonusTransaction> findByCardId(UUID cardId) {
        List<BonusTransaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM BonusTransactions WHERE card_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, cardId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapToBonusTransaction(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public List<BonusTransaction> findAll() {
        List<BonusTransaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM BonusTransactions";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                transactions.add(mapToBonusTransaction(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public BonusTransaction save(BonusTransaction bonusTransaction) {
        String query = bonusTransaction.id() == null
                ? "INSERT INTO BonusTransactions (transaction_id, card_id, order_id, amount, type, transaction_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE BonusTransactions SET card_id = ?, order_id = ?, amount = ?, type = ?, transaction_date = ?, notes = ? WHERE transaction_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            UUID id = bonusTransaction.id() == null ? UUID.randomUUID() : bonusTransaction.id();
            int index = 1;
            if (bonusTransaction.id() == null) {
                preparedStatement.setString(index++, id.toString());
            }
            preparedStatement.setString(index++, bonusTransaction.cardId().toString());
            preparedStatement.setString(index++, bonusTransaction.orderId() != null ? bonusTransaction.orderId().toString() : null);
            preparedStatement.setDouble(index++, bonusTransaction.amount());
            preparedStatement.setString(index++, bonusTransaction.type().name());
            preparedStatement.setTimestamp(index++, java.sql.Timestamp.valueOf(bonusTransaction.transactionDate()));
            preparedStatement.setString(index++, bonusTransaction.notes());
            if (bonusTransaction.id() != null) {
                preparedStatement.setString(index, id.toString());
            }
            preparedStatement.executeUpdate();
            return new BonusTransaction(id, bonusTransaction.cardId(), bonusTransaction.orderId(), bonusTransaction.amount(), bonusTransaction.type(), bonusTransaction.transactionDate(), bonusTransaction.notes());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteById(UUID id) throws EntityNotFoundException {
        String query = "DELETE FROM BonusTransactions WHERE transaction_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Bonus transaction with ID " + id + " not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private BonusTransaction mapToBonusTransaction(ResultSet resultSet) throws SQLException {
        String orderIdStr = resultSet.getString("order_id");
        return new BonusTransaction(
                UUID.fromString(resultSet.getString("transaction_id")),
                UUID.fromString(resultSet.getString("card_id")),
                orderIdStr == null ? null : UUID.fromString(orderIdStr),
                resultSet.getDouble("amount"),
                BonusTransactionType.valueOf(resultSet.getString("type")),
                resultSet.getTimestamp("transaction_date").toLocalDateTime(),
                resultSet.getString("notes")
        );
    }
}
