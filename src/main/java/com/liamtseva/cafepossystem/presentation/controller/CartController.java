package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.domain.security.AuthenticatedUser;
import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.persistence.entity.Cart;
import com.liamtseva.cafepossystem.persistence.entity.LoyaltyCard;
import com.liamtseva.cafepossystem.persistence.entity.MenuItem;
import com.liamtseva.cafepossystem.persistence.entity.Order;
import com.liamtseva.cafepossystem.persistence.entity.User;
import com.liamtseva.cafepossystem.persistence.entity.enums.OrderStatus;
import com.liamtseva.cafepossystem.persistence.repository.contract.CartRepository;
import com.liamtseva.cafepossystem.persistence.repository.contract.LoyaltyCardRepository;
import com.liamtseva.cafepossystem.persistence.repository.contract.MenuItemRepository;
import com.liamtseva.cafepossystem.persistence.repository.contract.OrderRepository;
import com.liamtseva.cafepossystem.persistence.repository.impl.CartRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.repository.impl.LoyaltyCardRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.repository.impl.MenuItemRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.repository.impl.OrderRepositoryImpl;
import com.liamtseva.cafepossystem.presentation.validation.CartValidator;
import com.liamtseva.cafepossystem.presentation.validation.OrderValidator;
import com.liamtseva.cafepossystem.presentation.validation.ValidationResult;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CartController {

    @FXML
    private Label cartLabel;

    @FXML
    private ScrollPane cartScrollPane;

    @FXML
    private GridPane cartGridPane;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Label bonusBalanceLabel;

    @FXML
    private TextField bonusToUseField;

    @FXML
    private Label finalAmountLabel;

    @FXML
    private TextArea notesArea;

    @FXML
    private Button placeOrderButton;
    @FXML
    private CheckBox socialCheckBox;

    @FXML
    private TextField tableNumberField;

    private CartRepository cartRepository;
    private MenuItemRepository menuItemRepository;
    private OrderRepository orderRepository;
    private LoyaltyCardRepository loyaltyCardRepository;
    private List<Cart> cartItems;
    private LoyaltyCard userLoyaltyCard;

    public CartController() {
        this.cartRepository = new CartRepositoryImpl(new DatabaseConnection().getDataSource());
        this.menuItemRepository = new MenuItemRepositoryImpl(new DatabaseConnection().getDataSource());
        this.orderRepository = new OrderRepositoryImpl(new DatabaseConnection().getDataSource());
        this.loyaltyCardRepository = new LoyaltyCardRepositoryImpl(new DatabaseConnection().getDataSource());
    }

    @FXML
    public void initialize() {
        if (placeOrderButton != null) {
            placeOrderButton.setOnAction(event -> placeOrder());
        }

        if (bonusToUseField != null) {
            bonusToUseField.textProperty().addListener((observable, oldValue, newValue) -> updateFinalAmount());
        }



        loadCartItems();
        loadLoyaltyCardInfo();

        if (cartGridPane != null && cartGridPane.getScene() != null) {
            cartGridPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    loadCartItems();
                }
            });
        }
    }

    public void loadCartItems() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            cartLabel.setText("Будь ласка, увійдіть у систему");
            updateTotalAmount(0.0);
            return;
        }
        List<Cart> allCartItems = cartRepository.findByUserId(currentUser.id());
        cartItems = allCartItems.stream().filter(cart -> !cart.isOrdered()).collect(Collectors.toList());
        cartGridPane.getChildren().clear();

        if (cartItems == null || cartItems.isEmpty()) {
            if (cartScrollPane != null) {
                cartScrollPane.setVisible(true);
                cartScrollPane.setManaged(true);
            }
            updateTotalAmount(0.0);
            return;
        } else {
            cartLabel.setText("");
            if (cartScrollPane != null) {
                cartScrollPane.setVisible(true);
                cartScrollPane.setManaged(true);
            }
        }

        int column = 0;
        int row = 0;
        int cardsPerRow = 1;
        double totalAmount = 0.0;

        cartGridPane.getColumnConstraints().clear();
        cartGridPane.getRowConstraints().clear();
        cartGridPane.setHgap(16);
        cartGridPane.setVgap(16);
        
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(100.0);
        columnConstraints.setMinWidth(520);
        columnConstraints.setPrefWidth(520);
        cartGridPane.getColumnConstraints().add(columnConstraints);
        
        for (int i = 0; i < cartItems.size(); i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(140);
            rowConstraints.setPrefHeight(150);
            rowConstraints.setMaxHeight(160);
            cartGridPane.getRowConstraints().add(rowConstraints);
        }

        for (Cart cartItem : cartItems) {
            if (cartItem == null) {
                cartLabel.setText("Помилка: Один із елементів кошика має невалідний формат.");
                continue;
            }

            MenuItem menuItem;
            try {
                menuItem = menuItemRepository.findById(cartItem.itemId());
                if (menuItem == null) {
                    cartLabel.setText("Помилка: Один із елементів кошика не знайдено.");
                    continue;
                }
            } catch (EntityNotFoundException e) {
                continue;
            }

            totalAmount += cartItem.subtotal();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cartItemCard.fxml"));
                HBox card = loader.load();
                CartItemCard controller = loader.getController();
                if (controller != null) {
                    controller.setCartItem(cartItem, menuItem);
                    controller.setParentController(this);
                    cartGridPane.add(card, column, row);
                    column++;
                    if (column >= cardsPerRow) {
                        column = 0;
                        row++;
                    }
                }
            } catch (IOException e) {
                cartLabel.setText("Помилка завантаження картки: " + e.getMessage());
            }
        }

        updateTotalAmount(totalAmount);
    }

    private void updateTotalAmount(double totalAmount) {
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(String.format("Загальна сума: %.2f грн", totalAmount));
        }
        updateFinalAmount();
    }

    private void loadLoyaltyCardInfo() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        try {
            userLoyaltyCard = loyaltyCardRepository.findByUserId(currentUser.id());
            if (bonusBalanceLabel != null) {
                bonusBalanceLabel.setText(String.format("Баланс бонусів: %.2f", userLoyaltyCard.balance()));
            }
        } catch (EntityNotFoundException e) {
            if (bonusBalanceLabel != null) {
                bonusBalanceLabel.setText("Бонусна карта відсутня");
            }
            userLoyaltyCard = null;
        }
        updateFinalAmount();
    }

    private void updateFinalAmount() {
        if (cartItems == null || cartItems.isEmpty()) {
            if (finalAmountLabel != null) {
                finalAmountLabel.setText("До сплати: 0.00 грн");
            }
            return;
        }

        double totalAmount = cartItems.stream().mapToDouble(Cart::subtotal).sum();
        double bonusToUse = 0.0;

        if (bonusToUseField != null && !bonusToUseField.getText().trim().isEmpty()) {
            try {
                bonusToUse = Double.parseDouble(bonusToUseField.getText().trim());
                if (userLoyaltyCard != null) {
                    if (bonusToUse > userLoyaltyCard.balance()) {
                        bonusToUse = userLoyaltyCard.balance();
                        bonusToUseField.setText(String.format("%.2f", bonusToUse));
                    }
                    if (bonusToUse > totalAmount) {
                        bonusToUse = totalAmount;
                        bonusToUseField.setText(String.format("%.2f", bonusToUse));
                    }
                    if (bonusToUse < 0) {
                        bonusToUse = 0;
                        bonusToUseField.setText("0");
                    }
                }
            } catch (NumberFormatException e) {
                bonusToUse = 0.0;
            }
        }

        double finalAmount = totalAmount - bonusToUse;
        if (finalAmountLabel != null) {
            finalAmountLabel.setText(String.format("До сплати: %.2f грн", finalAmount));
        }
    }

    private void placeOrder() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertController.showAlert("Будь ласка, увійдіть у систему");
            return;
        }

        if (cartItems == null || cartItems.isEmpty()) {
            AlertController.showAlert("Наразі Ваш кошик порожній");
            return;
        }

        for (Cart cartItem : cartItems) {
            ValidationResult cartValidationResult = CartValidator.isCartValid(cartItem, true);
            if (!cartValidationResult.isValid()) {
                AlertController.showAlert("Помилки валідації елемента кошика:\n" + cartValidationResult.getErrorMessage());
                return;
            }
        }

        String notes = notesArea.getText().trim();
        double totalAmount = cartItems.stream().mapToDouble(Cart::subtotal).sum();
        
        double bonusToUse = 0.0;
        if (bonusToUseField != null && !bonusToUseField.getText().trim().isEmpty()) {
            try {
                bonusToUse = Double.parseDouble(bonusToUseField.getText().trim());
                if (userLoyaltyCard != null) {
                    if (bonusToUse > userLoyaltyCard.balance()) {
                        AlertController.showAlert("Недостатньо бонусів на балансі");
                        return;
                    }
                    if (bonusToUse > totalAmount) {
                        AlertController.showAlert("Сума бонусів не може перевищувати загальну суму замовлення");
                        return;
                    }
                    if (bonusToUse < 0) {
                        AlertController.showAlert("Сума бонусів не може бути від'ємною");
                        return;
                    }
                } else if (bonusToUse > 0) {
                    AlertController.showAlert("У вас немає активної бонусної карти");
                    return;
                }
            } catch (NumberFormatException e) {
                AlertController.showAlert("Невірний формат суми бонусів");
                return;
            }
        }

        double bonusesEarned = (totalAmount - bonusToUse) * 0.05;
        
        List<String> cartIds = cartItems.stream()
            .map(cart -> cart.id().toString())
            .collect(Collectors.toList());

        String tableInput = tableNumberField.getText().trim();
        if (tableInput.isEmpty()) {
            AlertController.showAlert("Будь ласка, введіть номер столика");
            return;
        }

        int parsedTableNumber;
        try {
            parsedTableNumber = Integer.parseInt(tableInput);
            if (parsedTableNumber <= 0) {
                AlertController.showAlert("Номер столика має бути додатним числом");
                return;
            }
        } catch (NumberFormatException e) {
            AlertController.showAlert("Номер столика має бути цілим числом");
            return;
        }

        List<Order> allOrders = orderRepository.findAll();
        boolean tableOccupied = allOrders.stream()
            .anyMatch(order -> order.tableNumber() != null && parsedTableNumber == order.tableNumber() &&
                (order.status() == OrderStatus.PENDING || order.status() == OrderStatus.CONFIRMED));
        if (tableOccupied) {
            AlertController.showAlert("Столик №" + parsedTableNumber + " вже зайнятий. Оберіть інший столик.");
            return;
        }

        Integer tableNumber = parsedTableNumber;

        Order order = new Order(
            UUID.randomUUID(),
            currentUser.id(),
            LocalDateTime.now(),
            totalAmount,
            bonusesEarned,
            bonusToUse,
            OrderStatus.PENDING,
            notes.isEmpty() ? null : notes,
            socialCheckBox.isSelected(),
            tableNumber
        );

        ValidationResult orderValidationResult = OrderValidator.isOrderValid(order, false, (OrderRepositoryImpl) orderRepository);
        if (!orderValidationResult.isValid()) {
            AlertController.showAlert("Помилки валідації замовлення:\n" + orderValidationResult.getErrorMessage());
            return;
        }

        Order createdOrder = orderRepository.create(order, cartIds);
        if (createdOrder != null) {
            for (String cartId : cartIds) {
                try {
                    Cart cart = cartRepository.findById(UUID.fromString(cartId));
                    cartRepository.save(new Cart(cart.id(), cart.userId(), cart.itemId(), cart.quantity(), cart.subtotal(), true));
                } catch (EntityNotFoundException e) {
                    AlertController.showAlert("Не вдалося оновити статус елементів кошика");
                }
            }

            if (bonusToUse > 0 && userLoyaltyCard != null) {
                double newBalance = userLoyaltyCard.balance() - bonusToUse + bonusesEarned;
                LoyaltyCard updatedCard = new LoyaltyCard(
                    userLoyaltyCard.id(),
                    userLoyaltyCard.userId(),
                    userLoyaltyCard.cardNumber(),
                    newBalance,
                    userLoyaltyCard.isActive(),
                    userLoyaltyCard.createdAt()
                );
                loyaltyCardRepository.save(updatedCard);
            } else if (bonusesEarned > 0 && userLoyaltyCard != null) {
                double newBalance = userLoyaltyCard.balance() + bonusesEarned;
                LoyaltyCard updatedCard = new LoyaltyCard(
                    userLoyaltyCard.id(),
                    userLoyaltyCard.userId(),
                    userLoyaltyCard.cardNumber(),
                    newBalance,
                    userLoyaltyCard.isActive(),
                    userLoyaltyCard.createdAt()
                );
                loyaltyCardRepository.save(updatedCard);
            }

            notesArea.clear();
            bonusToUseField.clear();
            tableNumberField.clear();
            AlertController.showAlert(String.format("Замовлення успішно оформлено!\nБудь ласка, підійдіть до каси для оплати замовлення.", bonusToUse, bonusesEarned));
            loadCartItems();
            loadLoyaltyCardInfo();
        } else {
            cartLabel.setText("Помилка при оформленні замовлення");
        }
    }
}
