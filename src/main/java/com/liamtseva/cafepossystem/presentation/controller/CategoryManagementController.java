package com.liamtseva.cafepossystem.presentation.controller;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.Category;
import com.liamtseva.cafepossystem.persistence.repository.impl.CategoryRepositoryImpl;
import com.liamtseva.cafepossystem.persistence.connection.DatabaseConnection;
import com.liamtseva.cafepossystem.presentation.validation.CategoryValidator;
import com.liamtseva.cafepossystem.presentation.validation.ValidationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public class CategoryManagementController {

    @FXML
    private TextField nameField;
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
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, String> nameColumn;

    private CategoryRepositoryImpl categoryRepository;
    private ObservableList<Category> categoriesList;
    private Category selectedCategory;

    public CategoryManagementController() {
        this.categoryRepository = new CategoryRepositoryImpl(new DatabaseConnection().getDataSource());
        this.categoriesList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().categoryName()));

        loadCategories();

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> searchCategories(newValue));

        addButton.setOnAction(event -> addCategory());
        editButton.setOnAction(event -> editCategory());
        deleteButton.setOnAction(event -> deleteCategory());
        clearFieldsButton.setOnAction(event -> clearFields());

        categoryTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedCategory = newValue;
                populateFields(newValue);
                editButton.setDisable(newValue == null);
                deleteButton.setDisable(newValue == null);
            });
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            categoriesList.setAll(categories);
            categoryTable.setItems(categoriesList);
            if (categoriesList.isEmpty()) {
                categoryTable.setPlaceholder(new Label("Немає категорій"));
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при завантаженні категорій: " + e.getMessage());
        }
    }

    private void searchCategories(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadCategories();
            return;
        }

        List<Category> filteredCategories = categoriesList.stream()
            .filter(category -> category.categoryName().toLowerCase().contains(searchText.toLowerCase()))
            .toList();
        if (filteredCategories.isEmpty()) {
            categoryTable.setPlaceholder(new Label("Немає категорій"));
        } else {
            categoryTable.setPlaceholder(null);
        }
        categoryTable.setItems(FXCollections.observableArrayList(filteredCategories));
    }

    private void addCategory() {
        try {
            String name = nameField.getText().trim();

            if (name.isEmpty()) {
                AlertController.showAlert("Назва є обов'язковим полем і не може бути порожньою");
                return;
            }

            Category category = new Category(null, name);

            ValidationResult validationResult = CategoryValidator.isCategoryValid(category, false, categoryRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при додаванні категорії:\n" + validationResult.getErrorMessage());
                return;
            }

            Category savedCategory = categoryRepository.save(category);
            if (savedCategory != null) {
                loadCategories();
                clearFields();
                AlertController.showAlert("Категорію успішно додано!");
            } else {
                AlertController.showAlert("Не вдалося зберегти категорію. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при додаванні категорії: " + e.getMessage());
        }
    }

    private void editCategory() {
        if (selectedCategory == null) {
            AlertController.showAlert("Будь ласка, виберіть категорію для редагування");
            return;
        }

        try {
            String name = nameField.getText().trim();

            if (name.isEmpty()) {
                AlertController.showAlert("Назва є обов'язковим полем і не може бути порожньою");
                return;
            }

            Category updatedCategory = new Category(selectedCategory.id(), name);

            ValidationResult validationResult = CategoryValidator.isCategoryValid(updatedCategory, true, categoryRepository);
            if (!validationResult.isValid()) {
                AlertController.showAlert("Помилки при редагуванні категорії:\n" + validationResult.getErrorMessage());
                return;
            }

            Category savedCategory = categoryRepository.save(updatedCategory);
            if (savedCategory != null) {
                loadCategories();
                clearFields();
                AlertController.showAlert("Категорію успішно оновлено!");
            } else {
                AlertController.showAlert("Не вдалося оновити категорію. Спробуйте ще раз.");
            }
        } catch (Exception e) {
            AlertController.showAlert("Помилка при редагуванні категорії: " + e.getMessage());
        }
    }

    private void deleteCategory() {
        if (selectedCategory == null) {
            AlertController.showAlert("Будь ласка, виберіть категорію для видалення");
            return;
        }

        try {
            categoryRepository.deleteById(selectedCategory.id());
            loadCategories();
            clearFields();
            AlertController.showAlert("Категорію успішно видалено!");
        } catch (EntityNotFoundException e) {
            AlertController.showAlert("Категорію не знайдено в базі даних");
        } catch (Exception e) {
            AlertController.showAlert("Помилка при видаленні категорії: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        selectedCategory = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void populateFields(Category category) {
        if (category == null) {
            clearFields();
            return;
        }
        nameField.setText(category.categoryName());
    }
}