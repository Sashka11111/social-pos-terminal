package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.persistence.entity.Order;
import com.liamtseva.cafepossystem.persistence.entity.User;
import com.liamtseva.cafepossystem.persistence.entity.enums.OrderStatus;
import com.liamtseva.cafepossystem.persistence.repository.impl.OrderRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.repository.impl.UserRepositoryImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class OrderManagementController {

    @FXML
    private ComboBox<OrderStatus> statusComboBox;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button updateStatusButton;
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, String> userColumn;
    @FXML
    private TableColumn<Order, String> statusColumn;

    private OrderRepositoryImpl orderRepository;
    private UserRepositoryImpl userRepository;
    private ObservableList<Order> orderList;
    private ObservableList<User> userList;
    private Order selectedOrder;

    public OrderManagementController() {
        this.orderRepository = new OrderRepositoryImpl(new DatabaseConnection().getDataSource());
        this.userRepository = new UserRepositoryImpl(new DatabaseConnection().getDataSource());
        this.orderList = FXCollections.observableArrayList();
        this.userList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        userColumn.setCellValueFactory(cellData -> {
            User user = findUserById(cellData.getValue().userId());
            return new SimpleStringProperty(user != null ? user.username() : "N/A");
        });
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().status().toString()));

        loadOrders();
        loadUsers();

        statusComboBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchOrders(newValue));

        updateStatusButton.setOnAction(event -> updateOrderStatus());

        orderTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedOrder = newValue;
                    populateFields(newValue);
                });
    }

    private void loadOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            orderList.setAll(orders);
            orderTable.setItems(orderList);
            if (orderList.isEmpty()) {
                orderTable.setPlaceholder(new Label("Немає замовлень"));
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні замовлень: " + e.getMessage());
        }
    }

    private void searchOrders(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadOrders();
            return;
        }

        List<Order> filteredOrders = orderList.stream()
                .filter(order -> {
                    User user = findUserById(order.userId());
                    return String.valueOf(order.id()).contains(searchText) ||
                            (user != null && user.username().toLowerCase().contains(searchText.toLowerCase()));
                })
                .toList();
        if (filteredOrders.isEmpty()) {
            orderTable.setPlaceholder(new Label("Немає замовлень"));
        } else {
            orderTable.setPlaceholder(null);
        }
        orderTable.setItems(FXCollections.observableArrayList(filteredOrders));
    }

    private void updateOrderStatus() {
        if (selectedOrder == null) {
            AlertController.showAlert("Будь ласка, виберіть замовлення для оновлення статусу");
            return;
        }

        OrderStatus newStatus = statusComboBox.getValue();
        if (newStatus == null) {
            AlertController.showAlert("Будь ласка, виберіть новий статус для замовлення");
            return;
        }

        try {
            Order updatedOrder = new Order(selectedOrder.id(), selectedOrder.userId(), selectedOrder.orderDate(), selectedOrder.totalAmount(), selectedOrder.bonusesEarned(), selectedOrder.bonusesUsed(), newStatus, selectedOrder.notes(), selectedOrder.isSocial(), selectedOrder.tableNumber());
            orderRepository.update(updatedOrder, orderRepository.findCartIdsByOrderId(selectedOrder.id()));
            loadOrders();
            AlertController.showAlert("Статус замовлення успішно оновлено!");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при оновленні статусу замовлення: " + e.getMessage());
        }
    }

    private void populateFields(Order order) {
        if (order == null) {
            statusComboBox.getSelectionModel().clearSelection();
            return;
        }
        statusComboBox.setValue(order.status());
    }

    private void loadUsers() {
        try {
            List<User> users = userRepository.findAll();
            userList.setAll(users);
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні користувачів: " + e.getMessage());
        }
    }

    private User findUserById(java.util.UUID id) {
        if (id == null) {
            return null;
        }
        return userList.stream().filter(u -> u.id().equals(id)).findFirst().orElse(null);
    }
}
