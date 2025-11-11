package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.domain.security.AuthenticatedUser;
import com.liamtseva.cafepossystem.persistence.entity.User;
import com.liamtseva.cafepossystem.persistence.entity.enums.Role;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainMenuController {

    @FXML
    private Button menuButton;

    @FXML
    private Button changeAccountButton;

    @FXML
    private Button ordersButton;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button cartButton;

    @FXML
    private Button categoryManagementButton;

    @FXML
    private Button menuItemManagementButton;

    @FXML
    private Button userManagementButton;

    @FXML
    private Button loyaltyCardManagementButton;

    @FXML
    private Button orderManagementButton;

    @FXML
    private Button socialTablesButton;

    @FXML
    private StackPane stackPane;

    private double xOffset = 0;
    private double yOffset = 0;

    private Button selectedButton;

    @FXML
    void initialize() {
        showReservation();
        setActiveButton(menuButton);

        menuButton.setOnAction(event -> showMenuPage());
        cartButton.setOnAction(event -> showCartPage());
        ordersButton.setOnAction(event -> showOrdersPage());
        socialTablesButton.setOnAction(event -> showSocialTablesPage());
        categoryManagementButton.setOnAction(event -> showCategoryManagementPage());
        menuItemManagementButton.setOnAction(event -> showMenuItemManagementPage());
        userManagementButton.setOnAction(event -> showUserManagementPage());
        loyaltyCardManagementButton.setOnAction(event -> showLoyaltyCardManagementPage());
        orderManagementButton.setOnAction(event -> showOrderManagementPage());
        changeAccountButton.setOnAction(event -> handleChangeAccountAction());

        User currentUser = AuthenticatedUser.getInstance().getCurrentUser();
        if (currentUser.role() != Role.ADMIN) {
            categoryManagementButton.setVisible(false);
            categoryManagementButton.setManaged(false);
            menuItemManagementButton.setVisible(false);
            menuItemManagementButton.setManaged(false);
            userManagementButton.setVisible(false);
            userManagementButton.setManaged(false);
            loyaltyCardManagementButton.setVisible(false);
            loyaltyCardManagementButton.setManaged(false);
            orderManagementButton.setVisible(false);
            orderManagementButton.setManaged(false);
        }

        Platform.runLater(() -> {
            Stage primaryStage = (Stage) contentArea.getScene().getWindow();
            addDragListeners(primaryStage.getScene().getRoot());
            moveStackPane(menuButton);
        });
    }

    private void setActiveButton(Button button) {
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("selected");
        }
        button.getStyleClass().add("selected");
        selectedButton = button;
    }

    private void moveStackPane(Button button) {
        if (button == null || stackPane == null || stackPane.getParent() == null) {
            return;
        }

        double buttonHeight = button.getHeight() > 0 ? button.getHeight() : button.prefHeight(-1);
        stackPane.setPrefHeight(buttonHeight);
        stackPane.setMinHeight(buttonHeight);
        stackPane.setMaxHeight(buttonHeight);

        double buttonSceneY = button.localToScene(0, 0).getY();
        double parentSceneY = stackPane.getParent().localToScene(0, 0).getY();
        double targetY = buttonSceneY - parentSceneY;

        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.25), stackPane);
        transition.setToY(targetY);
        transition.setToX(0);
        transition.play();
    }

    private void showMenuPage() {
        moveStackPane(menuButton);
        setActiveButton(menuButton);
        loadFXML("menu.fxml");
    }

    private void showCartPage() {
        moveStackPane(cartButton);
        setActiveButton(cartButton);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cart.fxml"));
            Parent fxml = loader.load();

            CartController cartController = loader.getController();
            if (cartController != null) {
                cartController.loadCartItems();
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(fxml);
        } catch (IOException ex) {
            Logger.getLogger(MainMenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showOrdersPage() {
    moveStackPane(ordersButton);
    setActiveButton(ordersButton);
    loadFXML("orders.fxml");
    }

    private void showSocialTablesPage() {
        moveStackPane(socialTablesButton);
        setActiveButton(socialTablesButton);
        loadFXML("socialTables.fxml");
    }

    private void showCategoryManagementPage() {
        moveStackPane(categoryManagementButton);
        setActiveButton(categoryManagementButton);
        loadFXML("categoryManagement.fxml");
    }

    private void showMenuItemManagementPage() {
        moveStackPane(menuItemManagementButton);
        setActiveButton(menuItemManagementButton);
        loadFXML("menuItemManagement.fxml");
    }

    private void showUserManagementPage() {
        moveStackPane(userManagementButton);
        setActiveButton(userManagementButton);
        loadFXML("userManagement.fxml");
    }

    private void showLoyaltyCardManagementPage() {
        moveStackPane(loyaltyCardManagementButton);
        setActiveButton(loyaltyCardManagementButton);
        loadFXML("loyaltyCardManagement.fxml");
    }

    private void showOrderManagementPage() {
        moveStackPane(orderManagementButton);
        setActiveButton(orderManagementButton);
        loadFXML("orderManagement.fxml");
    }

    private void loadFXML(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFileName));
            Parent fxml = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(fxml);
        } catch (IOException ex) {
            Logger.getLogger(MainMenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showReservation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu.fxml"));
            AnchorPane bookingsAnchorPane = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(bookingsAnchorPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDragListeners(Parent root) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            Stage stage = (Stage) ((Parent) event.getSource()).getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    private void handleChangeAccountAction() {
        try {
            Stage currentStage = (Stage) changeAccountButton.getScene().getWindow();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/authorization.fxml"));
                    Parent root = loader.load();

                    Stage loginStage = new Stage();
                    loginStage.getIcons().add(new Image(getClass().getResourceAsStream("/data/icon.png")));
                    loginStage.initStyle(StageStyle.UNDECORATED);
                    loginStage.setResizable(true);
                    loginStage.setMaximized(true);
                    Scene scene = new Scene(root);
                    scene.getRoot().setOpacity(0.0);
                    loginStage.setScene(scene);
                    loginStage.show();

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), scene.getRoot());
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    currentStage.close();
                } catch (IOException ex) {
                    Logger.getLogger(MainMenuController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            fadeOut.play();
        } catch (Exception ex) {
            Logger.getLogger(MainMenuController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
