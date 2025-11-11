package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.persistence.entity.LoyaltyCard;
import com.liamtseva.cafepossystem.persistence.entity.User;
import com.liamtseva.cafepossystem.persistence.repository.impl.LoyaltyCardRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.repository.impl.UserRepositoryImpl;
import com.liamtseva.cafepossystem.presentation.validation.LoyaltyCardValidator;
import com.liamtseva.cafepossystem.presentation.validation.ValidationResult;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import javafx.util.StringConverter;

public class LoyaltyCardManagementController {

    @FXML
    private TextField cardNumberField;
    @FXML
    private ComboBox<User> userComboBox;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearFieldsButton;
    @FXML
    private TableView<LoyaltyCard> loyaltyCardTable;
    @FXML
    private TableColumn<LoyaltyCard, String> cardNumberColumn;
    @FXML
    private TableColumn<LoyaltyCard, Number> balanceColumn;
    @FXML
    private TableColumn<LoyaltyCard, String> userColumn;

    private LoyaltyCardRepositoryImpl loyaltyCardRepository;
    private UserRepositoryImpl userRepository;
    private ObservableList<LoyaltyCard> loyaltyCardList;
    private ObservableList<User> userList;
    private LoyaltyCard selectedLoyaltyCard;

    public LoyaltyCardManagementController() {
        this.loyaltyCardRepository = new LoyaltyCardRepositoryImpl(new DatabaseConnection().getDataSource());
        this.userRepository = new UserRepositoryImpl(new DatabaseConnection().getDataSource());
        this.loyaltyCardList = FXCollections.observableArrayList();
        this.userList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        loadUsers();
        loadLoyaltyCards();

        cardNumberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().cardNumber()));
        balanceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().balance()));
        userColumn.setCellValueFactory(cellData -> {
            User user = findUserById(cellData.getValue().userId());
            return new SimpleStringProperty(user != null ? user.username() : "Не призначено");
        });

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchLoyaltyCards(newValue));

        addButton.setOnAction(event -> addLoyaltyCard());
        deleteButton.setOnAction(event -> deleteLoyaltyCard());
        clearFieldsButton.setOnAction(event -> clearFields());

        loyaltyCardTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedLoyaltyCard = newValue;
                    populateFields(newValue);
                    deleteButton.setDisable(newValue == null);
                });

        userComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : user.username();
            }

            @Override
            public User fromString(String string) {
                return userList.stream().filter(u -> u.username().equals(string)).findFirst().orElse(null);
            }
        });
    }

    private void loadLoyaltyCards() {
        try {
            List<LoyaltyCard> loyaltyCards = loyaltyCardRepository.findAll();
            loyaltyCardList.setAll(loyaltyCards);
            loyaltyCardTable.setItems(loyaltyCardList);
            if (loyaltyCardList.isEmpty()) {
                loyaltyCardTable.setPlaceholder(new Label("Немає карт лояльності"));
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні карт лояльності: " + e.getMessage());
        }
    }

    private void searchLoyaltyCards(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadLoyaltyCards();
            return;
        }

        List<LoyaltyCard> filteredLoyaltyCards = loyaltyCardList.stream()
                .filter(loyaltyCard -> loyaltyCard.cardNumber().toLowerCase().contains(searchText.toLowerCase()))
                .toList();
        if (filteredLoyaltyCards.isEmpty()) {
            loyaltyCardTable.setPlaceholder(new Label("Немає карт лояльності"));
        } else {
            loyaltyCardTable.setPlaceholder(null);
        }
        loyaltyCardTable.setItems(FXCollections.observableArrayList(filteredLoyaltyCards));
    }

    private void loadUsers() {
        try {
            List<User> users = userRepository.findAll();
            userList.setAll(users);
            userComboBox.setItems(userList);
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні користувачів: " + e.getMessage());
        }
    }

    private void addLoyaltyCard() {
        try {
            String cardNumber = cardNumberField.getText().trim();
            User selectedUser = userComboBox.getValue();

            if (cardNumber.isEmpty()) {
                AlertController.showAlert("Номер карти є обов'язковим полем.");
                return;
            }

            LoyaltyCard loyaltyCard = new LoyaltyCard(null, selectedUser != null ? selectedUser.id() : null, cardNumber, 0.0, true, null);

            ValidationResult validationResult = LoyaltyCardValidator.isLoyaltyCardValid(loyaltyCard, false, loyaltyCardRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при додаванні карти лояльності:\n" + validationResult.getErrorMessage());
                return;
            }

            LoyaltyCard savedLoyaltyCard = loyaltyCardRepository.save(loyaltyCard);
            if (savedLoyaltyCard != null) {
                loadLoyaltyCards();
                clearFields();
                AlertController.showAlert("Карту лояльності успішно додано!");
            } else {
                AlertController.showAlert("Не вдалося зберегти карту лояльності. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при додаванні карти лояльності: " + e.getMessage());
        }
    }

    private void deleteLoyaltyCard() {
        if (selectedLoyaltyCard == null) {
            AlertController.showAlert("Будь ласка, виберіть карту лояльності для видалення");
            return;
        }

        try {
            loyaltyCardRepository.deleteById(selectedLoyaltyCard.id());
            loadLoyaltyCards();
            clearFields();
            AlertController.showAlert("Карту лояльності успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Карту лояльності не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні карти лояльності: " + e.getMessage());
        }
    }

    private void clearFields() {
        cardNumberField.clear();
        userComboBox.getSelectionModel().clearSelection();
        selectedLoyaltyCard = null;
        deleteButton.setDisable(true);
    }

    private void populateFields(LoyaltyCard loyaltyCard) {
        if (loyaltyCard == null) {
            clearFields();
            return;
        }
        cardNumberField.setText(loyaltyCard.cardNumber());
        if (loyaltyCard.userId() != null) {
            userComboBox.setValue(findUserById(loyaltyCard.userId()));
        } else {
            userComboBox.getSelectionModel().clearSelection();
        }
    }

    private User findUserById(java.util.UUID id) {
        return userList.stream().filter(u -> u.id().equals(id)).findFirst().orElse(null);
    }
}
