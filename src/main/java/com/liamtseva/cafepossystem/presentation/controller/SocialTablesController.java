package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.persistence.entity.Order;
import com.liamtseva.cafepossystem.persistence.entity.User;
import com.liamtseva.cafepossystem.persistence.repository.impl.OrderRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.repository.impl.UserRepositoryImpl;
import java.time.LocalDateTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.VBox;

public class SocialTablesController {

    @FXML
    private VBox socialOrdersContainer;

    @FXML
    private Label totalSocialOrdersLabel;

    @FXML
    private Label currentDateTimeLabel;

    @FXML
    private Label activeOrdersLabel;

    private OrderRepositoryImpl orderRepository;
    private UserRepositoryImpl userRepository;
    private ObservableList<Order> socialOrdersList;
    private ObservableList<User> userList;

    public SocialTablesController() {
        this.orderRepository = new OrderRepositoryImpl(new DatabaseConnection().getDataSource());
        this.userRepository = new UserRepositoryImpl(new DatabaseConnection().getDataSource());
        this.socialOrdersList = FXCollections.observableArrayList();
        this.userList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        loadUsers();
        loadSocialOrders();
        updateInfoPanel();
    }

    private void loadSocialOrders() {
        try {
            List<Order> allOrders = orderRepository.findAll();
            List<Order> socialOrders = allOrders.stream()
                .filter(order -> order.isSocial() &&
                    (order.status().name().equals("PENDING") || order.status().name().equals("CONFIRMED")))
                .collect(Collectors.toList());
            socialOrdersList.setAll(socialOrders);
            displaySocialOrders(socialOrders);
            updateInfoPanel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateInfoPanel() {
        if (totalSocialOrdersLabel != null) {
            totalSocialOrdersLabel.setText("Спільних столиків: " + socialOrdersList.size());
        }

        if (currentDateTimeLabel != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            currentDateTimeLabel.setText("Поточний час: " + LocalDateTime.now().format(formatter));
        }

        try {
            List<Order> allOrders = orderRepository.findAll();
            long activeOrders = allOrders.stream()
                .filter(order -> order.status().name().equals("PENDING") || order.status().name().equals("CONFIRMED"))
                .count();
            if (activeOrdersLabel != null) {
                activeOrdersLabel.setText("Активних замовлень: " + activeOrders);
            }
        } catch (Exception e) {
            if (activeOrdersLabel != null) {
                activeOrdersLabel.setText("Активних замовлень: --");
            }
        }
    }

    private void displaySocialOrders(List<Order> orders) {
        socialOrdersContainer.getChildren().clear();

        if (orders.isEmpty()) {
            Label noOrdersLabel = new Label("Наразі немає спільних столиків");
            noOrdersLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            socialOrdersContainer.getChildren().add(noOrdersLabel);
            return;
        }

        for (Order order : orders) {
            HBox orderCard = createOrderCard(order);
            socialOrdersContainer.getChildren().add(orderCard);
        }
    }

    private HBox createOrderCard(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/socialOrderCard.fxml"));
            HBox card = loader.load();
            card.setPrefHeight(90);

            Label userLabel = (Label) card.lookup("#userLabel");
            Label tableLabel = (Label) card.lookup("#tableLabel");
            Label timeLabel = (Label) card.lookup("#timeLabel");
            Label statusLabel = (Label) card.lookup("#statusLabel");
            Button joinButton = (Button) card.lookup("#joinButton");

            User user = findUserById(order.userId());
            userLabel.setText(user != null ? user.username() : "Невідомий");
            tableLabel.setText("Столик №" + (order.tableNumber() != null ? order.tableNumber() : "Немає"));
            timeLabel.setText(order.orderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            statusLabel.setText(order.status().getUkrainianName());
            joinButton.setOnAction(event -> joinTable(order));

            return card;
        } catch (IOException e) {
            e.printStackTrace();
            return new HBox();
        }
    }

    private void loadUsers() {
        try {
            List<User> users = userRepository.findAll();
            userList.setAll(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinTable(Order order) {
        try {
            Order updatedOrder = new Order(
                order.id(),
                order.userId(),
                order.orderDate(),
                order.totalAmount(),
                order.bonusesEarned(),
                order.bonusesUsed(),
                order.status(),
                order.notes(),
                false,
                order.tableNumber()
            );
            orderRepository.update(updatedOrder, orderRepository.findCartIdsByOrderId(order.id()));
            loadSocialOrders();
            AlertController.showAlert("Ви успішно приєдналися до спільного столика!");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при приєднанні: " + e.getMessage());
        }
    }

    private User findUserById(java.util.UUID id) {
        return userList.stream().filter(u -> u.id().equals(id)).findFirst().orElse(null);
    }
}
