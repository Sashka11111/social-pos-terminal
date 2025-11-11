package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.persistence.entity.Cart;
import com.liamtseva.cafepossystem.persistence.entity.MenuItem;
import com.liamtseva.cafepossystem.persistence.entity.Order;
import com.liamtseva.cafepossystem.persistence.repository.contract.CartRepository;
import com.liamtseva.cafepossystem.persistence.repository.contract.MenuItemRepository;
import com.liamtseva.cafepossystem.persistence.repository.contract.OrderRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class OrderDetailsController implements Initializable {

    @FXML
    private Label orderDateLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label notesLabel;
    @FXML
    private VBox itemsContainer;
    @FXML
    private Button closeButton;

    private final Order order;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private Stage stage;

    public OrderDetailsController(Order order, OrderRepository orderRepository, CartRepository cartRepository, MenuItemRepository menuItemRepository) {
        this.order = order;
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (order == null) {
            Label errorLabel = new Label("Помилка: замовлення не передано");
            errorLabel.getStyleClass().add("order-error-text");
            itemsContainer.getChildren().add(errorLabel);
            return;
        }

        orderDateLabel.setText(order.orderDate().toString());
        totalAmountLabel.setText(String.format("%.2f грн", order.totalAmount()));
        statusLabel.setText(order.status().getUkrainianName());
        notesLabel.setText(order.notes() != null ? order.notes() : "—");

        loadOrderItems();
        closeButton.setOnAction(event -> closeWindow());
    }
    
    private void loadOrderItems() {
        List<String> cartIds = orderRepository.findCartIdsByOrderId(order.id());
        if (cartIds.isEmpty()) {
            Label noItemsLabel = new Label("Немає товарів у замовленні");
            noItemsLabel.getStyleClass().add("order-no-items");
            itemsContainer.getChildren().add(noItemsLabel);
        } else {
            for (String cartIdStr : cartIds) {
                try {
                    UUID cartId = UUID.fromString(cartIdStr);
                    Cart cartItem = cartRepository.findById(cartId);
                    if (cartItem != null) {
                        MenuItem menuItem = menuItemRepository.findById(cartItem.itemId());
                        VBox itemCard = createItemCard(menuItem, cartItem);
                        itemsContainer.getChildren().add(itemCard);
                    }
                } catch (Exception e) {
                    Label errorLabel = new Label("Помилка завантаження товару");
                    errorLabel.getStyleClass().add("order-error-text");
                    itemsContainer.getChildren().add(errorLabel);
                }
            }
        }
    }
    
    private VBox createItemCard(MenuItem menuItem, Cart cartItem) {
        VBox card = new VBox(8);
        card.getStyleClass().add("order-item-card");
        
        HBox nameRow = new HBox(8);
        Label nameIcon = new Label("");
        nameIcon.getStyleClass().add("order-item-icon");
        Label nameLabel = new Label(menuItem != null ? menuItem.name() : "[Видалений товар]");
        nameLabel.getStyleClass().add("order-item-name");
        nameRow.getChildren().addAll(nameIcon, nameLabel);
        
        HBox detailsRow = new HBox(16);
        Label quantityLabel = new Label("Кількість: " + cartItem.quantity());
        quantityLabel.getStyleClass().add("order-item-quantity");
        Label priceLabel = new Label(String.format("%.2f грн", cartItem.subtotal()));
        priceLabel.getStyleClass().add("order-item-price");
        detailsRow.getChildren().addAll(quantityLabel, priceLabel);
        
        card.getChildren().addAll(nameRow, detailsRow);
        return card;
    }

    @FXML
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
