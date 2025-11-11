package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.domain.security.AuthenticatedUser;
import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.persistence.entity.Cart;
import com.liamtseva.cafepossystem.persistence.entity.MenuItem;
import com.liamtseva.cafepossystem.persistence.entity.User;
import com.liamtseva.cafepossystem.persistence.repository.contract.CartRepository;
import com.liamtseva.cafepossystem.persistence.repository.impl.CartRepositoryImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javafx.stage.StageStyle;

public class MenuItemCard {

    @FXML
    private ImageView menuImage;

    @FXML
    private Label menuItemName;

    @FXML
    private Label menuItemPrice;

    @FXML
    private Label menuItemCalories;

    @FXML
    private Spinner<Integer> quantity;

    @FXML
    private Button addToCartButton;

    private MenuController parentController;
    private MenuItem menuItem;
    private CartRepository cartRepository;

    public MenuItemCard() {
        this.cartRepository = new CartRepositoryImpl(new DatabaseConnection().getDataSource());
    }

    @FXML
    private void initialize() {
        if (quantity != null) {
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
            quantity.setValueFactory(valueFactory);
        }

        if (addToCartButton != null) {
            addToCartButton.setOnAction(event -> addToCart());
        }
    }

    public void setMenuItem(MenuItem item) {
        this.menuItem = item;
        if (menuItemName != null) {
            menuItemName.setText(item.name());
        }
        if (menuItemPrice != null) {
            menuItemPrice.setText(String.format("%.2f грн", item.price()));
        }
        if (menuItemCalories != null) {
            menuItemCalories.setText(item.calories() != null ? item.calories() + " ккал" : "0 ккал");
        }
        if (menuImage != null) {
            if (item.image() != null && item.image().length > 0) {
                menuImage.setImage(new Image(new ByteArrayInputStream(item.image())));
            } else {
                menuImage.setImage(new Image(getClass().getResourceAsStream("/data/ingredients.png")));
            }
        }
    }

    public void setParentController(MenuController parentController) {
        this.parentController = parentController;
    }

    private int getQuantity() {
        if (quantity == null) {
            return 0;
        }
        try {
            return quantity.getValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private void addToCart() {
        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertController.showAlert("Будь ласка, увійдіть у систему, щоб додати елемент до кошика.");
            return;
        }

        int qty = getQuantity();
        if (qty <= 0) {
            AlertController.showAlert("Виберіть коректну кількість (більше 0).");
            return;
        }

        List<Cart> userCartItems = cartRepository.findByUserId(currentUser.id());
        boolean itemExistsInCart = userCartItems.stream()
            .filter(cartItem -> !cartItem.isOrdered())
            .anyMatch(cartItem -> cartItem.itemId().equals(menuItem.id()));

        if (itemExistsInCart) {
            AlertController.showAlert("Цей елемент уже є у Вашому кошику!");
            return;
        }

        if (parentController != null) {
            parentController.addToCart(menuItem, qty);
            AlertController.showAlert("Товар додано до кошика!");
        } else {
            AlertController.showAlert("Помилка: parentController є null");
        }
    }

    @FXML
    private void showDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menuItemDetails.fxml"));

            Scene scene = new Scene(loader.load(), 600, 600);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/data/icon.png")));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.setResizable(false);

            MenuItemDetailsController controller = loader.getController();
            controller.setMenuItem(menuItem);

            stage.showAndWait();
        } catch (IOException e) {
            AlertController.showAlert("Не вдалося завантажити деталі страви");
        }
    }
}
