package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.persistence.entity.Category;
import com.liamtseva.cafepossystem.persistence.entity.MenuItem;
import com.liamtseva.cafepossystem.persistence.repository.impl.CategoryRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.repository.impl.MenuItemRepositoryImpl;
import com.liamtseva.cafepossystem.presentation.validation.MenuItemValidator;
import com.liamtseva.cafepossystem.presentation.validation.ValidationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import org.controlsfx.control.CheckComboBox;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;

public class MenuItemManagementController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private CheckComboBox<Category> categoryCheckComboBox;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearFieldsButton;
    @FXML
    private TableView<MenuItem> menuItemTable;
    @FXML
    private TableColumn<MenuItem, String> nameColumn;
    @FXML
    private TableColumn<MenuItem, String> priceColumn;
    @FXML
    private TableColumn<MenuItem, String> categoryColumn;
    @FXML
    private TableColumn<MenuItem, String> descriptionColumn;

    private MenuItemRepositoryImpl menuItemRepository;
    private CategoryRepositoryImpl categoryRepository;
    private ObservableList<MenuItem> menuItemList;
    private ObservableList<Category> categoryList;
    private MenuItem selectedMenuItem;

    public MenuItemManagementController() {
        this.menuItemRepository = new MenuItemRepositoryImpl(new DatabaseConnection().getDataSource());
        this.categoryRepository = new CategoryRepositoryImpl(new DatabaseConnection().getDataSource());
        this.menuItemList = FXCollections.observableArrayList();
        this.categoryList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));
        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().price())));
        categoryColumn.setCellValueFactory(cellData -> {
            List<Category> categories = menuItemRepository.findCategoriesByItemId(cellData.getValue().id());
            return new SimpleStringProperty(categories.stream().map(Category::categoryName).collect(java.util.stream.Collectors.joining(", ")));
        });
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().description()));

        loadMenuItems();
        loadCategories();

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchMenuItems(newValue));

        addButton.setOnAction(event -> addMenuItem());
        editButton.setOnAction(event -> editMenuItem());
        deleteButton.setOnAction(event -> deleteMenuItem());
        clearFieldsButton.setOnAction(event -> clearFields());

        menuItemTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedMenuItem = newValue;
                    populateFields(newValue);
                    editButton.setDisable(newValue == null);
                    deleteButton.setDisable(newValue == null);
                });

        categoryCheckComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category == null ? "" : category.categoryName();
            }

            @Override
            public Category fromString(String string) {
                return categoryList.stream().filter(c -> c.categoryName().equals(string)).findFirst().orElse(null);
            }
        });
    }

    private void loadMenuItems() {
        try {
            List<MenuItem> menuItems = menuItemRepository.findAll();
            menuItemList.setAll(menuItems);
            menuItemTable.setItems(menuItemList);
            if (menuItemList.isEmpty()) {
                menuItemTable.setPlaceholder(new Label("Немає елементів меню"));
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні елементів меню: " + e.getMessage());
        }
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            categoryList.setAll(categories);
            categoryCheckComboBox.getItems().addAll(categoryList);
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні категорій: " + e.getMessage());
        }
    }

    private void searchMenuItems(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadMenuItems();
            return;
        }

        List<MenuItem> filteredMenuItems = menuItemList.stream()
                .filter(menuItem -> menuItem.name().toLowerCase().contains(searchText.toLowerCase()))
                .toList();
        if (filteredMenuItems.isEmpty()) {
            menuItemTable.setPlaceholder(new Label("Немає елементів меню"));
        } else {
            menuItemTable.setPlaceholder(null);
        }
        menuItemTable.setItems(FXCollections.observableArrayList(filteredMenuItems));
    }

    private void addMenuItem() {
        try {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            String description = descriptionArea.getText().trim();
            List<Category> categories = categoryCheckComboBox.getCheckModel().getCheckedItems();

            if (name.isEmpty() || priceText.isEmpty() || categories.isEmpty()) {
                AlertController.showAlert("Назва, ціна та хоча б одна категорія є обов'язковими полями.");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                AlertController.showAlert("Ціна повинна бути числом.");
                return;
            }

            MenuItem menuItem = new MenuItem(null, name, description, price, null, null, null);

            ValidationResult validationResult = MenuItemValidator.isMenuItemValid(menuItem, false, menuItemRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при додаванні елемента меню:\n" + validationResult.getErrorMessage());
                return;
            }

            MenuItem savedMenuItem = menuItemRepository.save(menuItem);
            if (savedMenuItem != null) {
                menuItemRepository.saveItemCategories(savedMenuItem.id(), categories.stream().map(Category::id).toList());
                loadMenuItems();
                clearFields();
                AlertController.showAlert("Елемент меню успішно додано!");
            } else {
                AlertController.showAlert("Не вдалося зберегти елемент меню. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при додаванні елемента меню: " + e.getMessage());
        }
    }

    private void editMenuItem() {
        if (selectedMenuItem == null) {
            AlertController.showAlert("Будь ласка, виберіть елемент меню для редагування");
            return;
        }

        try {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            String description = descriptionArea.getText().trim();
            List<Category> categories = categoryCheckComboBox.getCheckModel().getCheckedItems();

            if (name.isEmpty() || priceText.isEmpty() || categories.isEmpty()) {
                AlertController.showAlert("Назва, ціна та хоча б одна категорія є обов'язковими полями.");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                AlertController.showAlert("Ціна повинна бути числом.");
                return;
            }

            MenuItem updatedMenuItem = new MenuItem(selectedMenuItem.id(), name, description, price, selectedMenuItem.calories(), selectedMenuItem.image(), selectedMenuItem.ingredients());

            ValidationResult validationResult = MenuItemValidator.isMenuItemValid(updatedMenuItem, true, menuItemRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при редагуванні елемента меню:\n" + validationResult.getErrorMessage());
                return;
            }

            MenuItem savedMenuItem = menuItemRepository.save(updatedMenuItem);
            if (savedMenuItem != null) {
                menuItemRepository.updateItemCategories(savedMenuItem.id(), categories.stream().map(Category::id).toList());
                loadMenuItems();
                clearFields();
                AlertController.showAlert("Елемент меню успішно оновлено!");
            } else {
                AlertController.showAlert("Не вдалося оновити елемент меню. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при редагуванні елемента меню: " + e.getMessage());
        }
    }

    private void deleteMenuItem() {
        if (selectedMenuItem == null) {
            AlertController.showAlert("Будь ласка, виберіть елемент меню для видалення");
            return;
        }

        try {
            menuItemRepository.deleteById(selectedMenuItem.id());
            loadMenuItems();
            clearFields();
            AlertController.showAlert("Елемент меню успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Елемент меню не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні елемента меню: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        priceField.clear();
        descriptionArea.clear();
        categoryCheckComboBox.getCheckModel().clearChecks();
        selectedMenuItem = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void populateFields(MenuItem menuItem) {
        if (menuItem == null) {
            clearFields();
            return;
        }
        nameField.setText(menuItem.name());
        priceField.setText(String.valueOf(menuItem.price()));
        descriptionArea.setText(menuItem.description());
        categoryCheckComboBox.getCheckModel().clearChecks();
        List<Category> categories = menuItemRepository.findCategoriesByItemId(menuItem.id());
        for (Category category : categories) {
            categoryCheckComboBox.getCheckModel().check(category);
        }
    }
}
