package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.User;
import com.liamtseva.cafepossystem.persistence.entity.enums.Role;
import com.liamtseva.cafepossystem.persistence.repository.contract.UserRepository;
import com.liamtseva.cafepossystem.persistence.repository.impl.UserRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.presentation.validation.UserValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserManagementController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button changeRoleButton;
    @FXML
    private Button clearFieldsButton;

    private UserRepository userRepository;
    private ObservableList<User> usersList;
    private User selectedUser;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserManagementController() {
        this.userRepository = new UserRepositoryImpl(new DatabaseConnection().getDataSource());
        this.usersList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().username()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().email()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().role().name()));
        roleComboBox.setConverter(new StringConverter<Role>() {
            @Override
            public String toString(Role role) {
                return role == null ? null : role.name();
            }

            @Override
            public Role fromString(String string) {
                return string == null ? null : Role.valueOf(string);
            }
        });
        roleComboBox.getItems().addAll(Role.values());

        loadUsers();

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchUsers(newValue));

        addButton.setOnAction(event -> addUser());
        editButton.setOnAction(event -> editUser());
        deleteButton.setOnAction(event -> deleteUser());
        changeRoleButton.setOnAction(event -> changeRole());
        clearFieldsButton.setOnAction(event -> clearFields());

        userTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedUser = newValue;
                populateFields(newValue);
                editButton.setDisable(newValue == null);
                deleteButton.setDisable(newValue == null);
                changeRoleButton.setDisable(newValue == null);
            });
    }

    private void loadUsers() {
        try {
            List<User> users = userRepository.findAll();
            usersList.setAll(users);
            userTable.setItems(usersList);
            if (usersList.isEmpty()) {
                userTable.setPlaceholder(new Label("Немає користувачів"));
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні користувачів: " + e.getMessage());
        }
    }

    private void searchUsers(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadUsers();
            return;
        }

        List<User> filteredUsers = usersList.stream()
            .filter(user -> user.username().toLowerCase().contains(searchText.toLowerCase()) ||
                user.email().toLowerCase().contains(searchText.toLowerCase()) ||
                user.role().name().toLowerCase().contains(searchText.toLowerCase()))
            .toList();
        if (filteredUsers.isEmpty()) {
            userTable.setPlaceholder(new Label("Немає користувачів"));
        } else {
            userTable.setPlaceholder(null);
        }
        userTable.setItems(FXCollections.observableArrayList(filteredUsers));
    }

    private void addUser() {
        try {
            String username = nameField.getText().trim();
            String email = emailField.getText().trim();
            Role role = roleComboBox.getValue();

            if (username.isEmpty()) {
                AlertController.showAlert("Ім'я користувача є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (email.isEmpty()) {
                AlertController.showAlert("Email є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (role == null) {
                AlertController.showAlert("Роль є обов'язковим полем і не може бути порожньою");
                return;
            }

            if (!UserValidator.isUsernameValid(username)) {
                AlertController.showAlert("Ім'я користувача невалідне або вже існує");
                return;
            }
            if (!UserValidator.isEmailValid(email)) {
                AlertController.showAlert("Невірний формат email або email перевищує 100 символів");
                return;
            }

            try {
                userRepository.findByUsername(username);
                AlertController.showAlert("Користувач з ім'ям " + username + " уже існує");
                return;
            } catch (EntityNotFoundException e) {
            }
            for (User user : userRepository.findAll()) {
                if (user.email().equalsIgnoreCase(email)) {
                    AlertController.showAlert("Користувач з email " + email + " уже існує");
                    return;
                }
            }

            User user = new User(null, username, "defaultPassword", role, email, LocalDateTime.now());

            userRepository.addUser(user);
            loadUsers();
            clearFields();
            AlertController.showAlert("Користувача успішно додано!");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при додаванні користувача: " + e.getMessage());
        }
    }

    private void editUser() {
        if (selectedUser == null) {
            AlertController.showAlert("Будь ласка, виберіть користувача для редагування");
            return;
        }

        try {
            String username = nameField.getText().trim();
            String email = emailField.getText().trim();
            Role role = roleComboBox.getValue();

            if (username.isEmpty()) {
                AlertController.showAlert("Ім'я користувача є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (email.isEmpty()) {
                AlertController.showAlert("Email є обов'язковим полем і не може бути порожнім");
                return;
            }
            if (role == null) {
                AlertController.showAlert("Роль є обов'язковим полем і не може бути порожньою");
                return;
            }

            if (!UserValidator.isUsernameValid(username)) {
                AlertController.showAlert("Ім'я користувача невалідне");
                return;
            }
            if (!UserValidator.isEmailValid(email)) {
                AlertController.showAlert("Невірний формат email або email перевищує 100 символів");
                return;
            }

            if (!username.equals(selectedUser.username())) {
                try {
                    userRepository.findByUsername(username);
                    AlertController.showAlert("Користувач з ім'ям " + username + " уже існує");
                    return;
                } catch (EntityNotFoundException e) {
                }
            }
            for (User user : userRepository.findAll()) {
                if (!user.id().equals(selectedUser.id()) && user.email().equalsIgnoreCase(email)) {
                    AlertController.showAlert("Користувач з email " + email + " уже існує");
                    return;
                }
            }

            User updatedUser = new User(selectedUser.id(), username, selectedUser.password(), role, email, selectedUser.createdAt());

            userRepository.deleteUser(selectedUser.username());
            userRepository.addUser(updatedUser);
            loadUsers();
            clearFields();
            AlertController.showAlert("Користувача успішно оновлено!");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при редагуванні користувача: " + e.getMessage());
        }
    }

    private void changeRole() {
        if (selectedUser == null) {
            AlertController.showAlert("Будь ласка, виберіть користувача для зміни ролі");
            return;
        }

        try {
            Role newRole = roleComboBox.getValue();
            if (newRole == null) {
                AlertController.showAlert("Роль є обов'язковим полем і не може бути порожньою");
                return;
            }
            if (newRole.equals(selectedUser.role())) {
                AlertController.showAlert("Обрана роль збігається з поточною");
                return;
            }

            userRepository.updateUserRole(selectedUser.username(), newRole);
            loadUsers();
            clearFields();
            AlertController.showAlert("Роль користувача успішно змінено!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteUser() {
        if (selectedUser == null) {
            AlertController.showAlert("Будь ласка, виберіть користувача для видалення");
            return;
        }

        try {
            userRepository.deleteUser(selectedUser.username());
            loadUsers();
            clearFields();
            AlertController.showAlert("Користувача успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Користувача не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні користувача: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        selectedUser = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void populateFields(User user) {
        if (user == null) {
            clearFields();
            return;
        }
        nameField.setText(user.username());
        emailField.setText(user.email());
        roleComboBox.setValue(user.role());
    }
}
