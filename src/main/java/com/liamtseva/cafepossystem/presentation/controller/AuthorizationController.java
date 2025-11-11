package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.domain.security.AuthenticatedUser;
import com.liamtseva.cafepossystem.domain.security.PasswordHashing;
import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.persistence.entity.User;
import com.liamtseva.cafepossystem.persistence.repository.contract.UserRepository;
import com.liamtseva.cafepossystem.persistence.repository.impl.UserRepositoryImpl;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AuthorizationController {

    @FXML
    private Button authSignInButton;

    @FXML
    private Button authSingUpButton;

    @FXML
    private TextField loginTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView authImage;

    @FXML
    private Button btnClose;

    private UserRepository userRepository;

    public AuthorizationController() {
        this.userRepository = new UserRepositoryImpl(new DatabaseConnection().getDataSource());
    }

    private void switchScene(String fxmlPath) {
        Scene currentScene = authSignInButton.getScene();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        try {
            Parent root = loader.load();
            currentScene.setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void initialize() {
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(330, 500);
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        authImage.setClip(clip);
        btnClose.setOnAction(event -> {
            System.exit(0);
        });
        authSingUpButton.setOnAction(event -> {
            String loginText = loginTextField.getText().trim();
            String loginPassword = passwordField.getText().trim();

            if (!loginText.isEmpty() && !loginPassword.isEmpty()) {
                try {
                    User user = userRepository.findByUsername(loginText);
                    if (user != null) {
                        String hashedPassword = PasswordHashing.getInstance()
                            .hashedPassword(loginPassword);
                        if (user.password().equals(hashedPassword)) {
                            AuthenticatedUser.getInstance().setCurrentUser(user);
                            authSingUpButton.getScene().getWindow().hide();
                            FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/view/mainMenu.fxml"));
                            Parent root = loader.load();
                            Stage stage = new Stage();
                            stage.getIcons()
                                .add(new Image(getClass().getResourceAsStream("/data/icon.png")));
                            stage.setScene(new Scene(root));
                            stage.initStyle(StageStyle.UNDECORATED);
                            stage.setResizable(true);
                            stage.setMaximized(true);
                            stage.showAndWait();
                        } else {
                            AlertController.showAlert("Неправильний логін або пароль");
                        }
                    }
                } catch (EntityNotFoundException | IOException e) {
                    AlertController.showAlert("Неправильний логін або пароль");
                }
            } else {
                AlertController.showAlert("Будь ласка, введіть логін та пароль");
            }
        });

        authSignInButton.setOnAction(event -> switchScene("/view/registration.fxml"));

    }
}
