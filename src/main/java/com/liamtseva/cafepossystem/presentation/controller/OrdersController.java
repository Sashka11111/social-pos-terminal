package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.domain.security.AuthenticatedUser;
import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.persistence.entity.Order;
import com.liamtseva.cafepossystem.persistence.entity.User;
import com.liamtseva.cafepossystem.persistence.entity.enums.OrderStatus;
import com.liamtseva.cafepossystem.persistence.repository.contract.CartRepository;
import com.liamtseva.cafepossystem.persistence.repository.contract.MenuItemRepository;
import com.liamtseva.cafepossystem.persistence.repository.contract.OrderRepository;
import com.liamtseva.cafepossystem.persistence.repository.impl.CartRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.repository.impl.MenuItemRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.repository.impl.OrderRepositoryImpl;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import javafx.stage.StageStyle;

public class OrdersController {

    @FXML
    private TextField searchTextField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Order> ordersTableView;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    @FXML
    private TableColumn<Order, Double> totalAmountColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private TableColumn<Order, String> notesColumn;

    @FXML
    private TableColumn<Order, Void> detailsColumn;
    @FXML
    private TableColumn<Order, Boolean> socialColumn;
    @FXML
    private TableColumn<Order, Integer> tableNumberColumn;

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final ObservableList<Order> ordersList;

    public OrdersController() {
        this.orderRepository = new OrderRepositoryImpl(new DatabaseConnection().getDataSource());
        this.cartRepository = new CartRepositoryImpl(new DatabaseConnection().getDataSource());
        this.menuItemRepository = new MenuItemRepositoryImpl(new DatabaseConnection().getDataSource());
        this.ordersList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupStatusComboBox();
        setupTableColumns();
        loadOrders();

        statusComboBox.setOnAction(event -> loadOrders());
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> loadOrders());
    }

    private void setupStatusComboBox() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList("Усі статуси");
        statusOptions.addAll(Arrays.stream(OrderStatus.values())
            .map(OrderStatus::getUkrainianName)
            .collect(Collectors.toList()));
        statusComboBox.setItems(statusOptions);
        statusComboBox.setValue("Усі статуси");
    }

    private void setupTableColumns() {
        orderDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().orderDate().toString()));
        totalAmountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().totalAmount()).asObject());
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().status().getUkrainianName()));
        notesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().notes() != null ? cellData.getValue().notes() : "—"));
        tableNumberColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().tableNumber()));
        socialColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isSocial()).asObject());
        socialColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Так" : "Ні");
                }
            }
        });

        detailsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewDetailsButton = new Button("Деталі");
            private final Button cancelButton = new Button("Скасувати");
            private final VBox buttonBox = new VBox(8, viewDetailsButton, cancelButton);

            {
                buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
                viewDetailsButton.getStyleClass().add("details-button");
                cancelButton.getStyleClass().add("cancel-button");
                viewDetailsButton.setMaxWidth(Double.MAX_VALUE);
                cancelButton.setMaxWidth(Double.MAX_VALUE);

                viewDetailsButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    viewOrderDetails(order);
                });

                cancelButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    cancelOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    if (order.status() != OrderStatus.PENDING) {
                        cancelButton.setDisable(true);
                    } else {
                        cancelButton.setDisable(false);
                    }
                    setGraphic(buttonBox);
                }
            }
        });
    }

    private void loadOrders() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            messageLabel.setText("Будь ласка, увійдіть у систему");
            ordersList.clear();
            return;
        }

        List<Order> orders;
        String selectedStatus = statusComboBox.getValue();
        if (selectedStatus == null || selectedStatus.equals("Усі статуси")) {
            orders = orderRepository.findByUserId(currentUser.id());
        } else {
            OrderStatus status = Arrays.stream(OrderStatus.values())
                .filter(s -> s.getUkrainianName().equals(selectedStatus))
                .findFirst()
                .orElse(null);

            if (status != null) {
                List<Order> allOrders = orderRepository.findByUserId(currentUser.id());
                orders = allOrders.stream()
                    .filter(order -> order.status().equals(status))
                    .collect(Collectors.toList());
            } else {
                orders = orderRepository.findByUserId(currentUser.id());
            }
        }

        String searchId = searchTextField.getText().trim().toLowerCase();
        if (!searchId.isEmpty()) {
            orders = orders.stream()
                .filter(order -> {
                    String date = order.orderDate().toString().toLowerCase();
                    String amount = String.valueOf(order.totalAmount()).toLowerCase();
                    String status = order.status().getUkrainianName().toLowerCase();
                    String notes = order.notes() != null ? order.notes().toLowerCase() : "";
                    String table = order.tableNumber() != null ? order.tableNumber().toString().toLowerCase() : "";

                    return date.contains(searchId) ||
                        amount.contains(searchId) ||
                        status.contains(searchId) ||
                        notes.contains(searchId) ||
                        table.contains(searchId);
                })
                .toList();
        }

        ordersList.clear();
        if (orders.isEmpty()) {
            messageLabel.setText("У Вас поки немає замовлень");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        } else {
            messageLabel.setText("");
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
            ordersList.addAll(orders);
        }

        if (ordersList.isEmpty()) {
            ordersTableView.setPlaceholder(new Label("Наразі таких замовлень немає"));
        }

        ordersTableView.setItems(ordersList);
    }

    private void cancelOrder(Order order) {
        if (!order.status().equals(OrderStatus.PENDING)) {
            messageLabel.setText("Скасування можливо лише для замовлень у статусі 'В обробці'");
            return;
        }

        try {
            Order updatedOrder = new Order(
                order.id(),
                order.userId(),
                order.orderDate(),
                order.totalAmount(),
                order.bonusesEarned(),
                order.bonusesUsed(),
                OrderStatus.CANCELLED,
                order.notes(),
                order.isSocial(),
                order.tableNumber()
            );
            List<String> cartIds = orderRepository.findCartIdsByOrderId(order.id());
            Order result = orderRepository.update(updatedOrder, cartIds);

            if (result != null) {
                AlertController.showAlert("Замовлення успішно скасовано");
                loadOrders();
            } else {
                AlertController.showAlert("Помилка при скасуванні замовлення");
            }
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Замовлення не знайдено");
        }
    }

    private void viewOrderDetails(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/orderDetails.fxml"));
            loader.setControllerFactory(param -> new OrderDetailsController(order, orderRepository, cartRepository, menuItemRepository));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 700, 650));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/data/icon.png")));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.getIcons().add(new Image(getClass().getResourceAsStream("/data/icon.png")));

            OrderDetailsController controller = loader.getController();
            controller.setStage(stage);

            stage.showAndWait();
        } catch (IOException e) {
            AlertController.showAlert("Помилка при завантаженні деталей замовлення: " + e.getMessage());
        } catch (Exception e) {
            AlertController.showAlert("Помилка при отриманні деталей замовлення: " + e.getMessage());
        }
    }
}
