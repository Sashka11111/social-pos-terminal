package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.persistence.entity.Cart;
import com.liamtseva.cafepossystem.persistence.entity.MenuItem;
import com.liamtseva.cafepossystem.persistence.repository.contract.CartRepository;
import com.liamtseva.cafepossystem.persistence.repository.impl.CartRepositoryImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javafx.stage.StageStyle;

public class CartItemCard {

    @FXML
    private ImageView menuImage;

    @FXML
    private Label menuItemName;

    @FXML
    private Label menuItemPrice;

    @FXML
    private Label menuItemCalories;

    @FXML
    private Label quantityLabel;

    @FXML
    private Button deleteFromCartButton;

    private Cart cartItem;
    private MenuItem menuItem;
    private CartController parentController;
    private CartRepository cartRepository;

    public CartItemCard() {
        this.cartRepository = new CartRepositoryImpl(new DatabaseConnection().getDataSource());
    }

    public void setCartItem(Cart cartItem, MenuItem menuItem) {
        this.cartItem = cartItem;
        this.menuItem = menuItem;

        if (menuItemName != null) {
            menuItemName.setText(menuItem.name());
        }
        if (menuItemPrice != null) {
            menuItemPrice.setText(String.format("%.2f грн", cartItem.subtotal()));
        }
        if (menuItemCalories != null) {
            menuItemCalories.setText(menuItem.calories() != null ? menuItem.calories() + " ккал" : "0 ккал");
        }
        if (quantityLabel != null) {
            quantityLabel.setText(String.valueOf(cartItem.quantity()));
        }
        if (menuImage != null) {
            if (menuItem.image() != null && menuItem.image().length > 0) {
                menuImage.setImage(new Image(new ByteArrayInputStream(menuItem.image())));
            } else {
                menuImage.setImage(new Image(getClass().getResourceAsStream("/data/ingredients.png")));
            }
        }

        if (deleteFromCartButton != null) {
            deleteFromCartButton.setOnAction(event -> deleteFromCart());
        }
    }

    public void setParentController(CartController controller) {
        this.parentController = controller;
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

    @FXML
    private void deleteFromCart() {
        try {
            cartRepository.deleteById(cartItem.id());
            if (parentController != null) {
                parentController.loadCartItems();
            }
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Не вдалося видалити елемент з кошика");
        }
    }
}
