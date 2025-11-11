package com.liamtseva.cafepossystem.presentation.validation;

import com.liamtseva.cafepossystem.persistence.entity.MenuItem;
import com.liamtseva.cafepossystem.persistence.repository.impl.MenuItemRepositoryImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class MenuItemValidator {
  private static final int MIN_NAME_LENGTH = 2;
  private static final int MAX_NAME_LENGTH = 50;
  private static final int MAX_DESCRIPTION_LENGTH = 255;
  private static final double MIN_PRICE = 0.01;
  private static final double MAX_PRICE = 10000.00;
  private static final int MIN_CALORIES = 0;
  private static final int MAX_CALORIES = 10000;

  private static final String NAME_PATTERN = "^[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ\\s-]+$";

  public static ValidationResult isItemIdValid(UUID itemId, boolean isExisting) {
    if (isExisting && itemId == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Ідентифікатор елемента меню не може бути відсутнім для існуючого елемента");
      return new ValidationResult(false, errors);
    }
    return new ValidationResult(true);
  }

  public static ValidationResult isNameValid(String name) {
    List<String> errors = new ArrayList<>();
    if (name == null) {
      errors.add("Назва не може бути відсутньою");
      return new ValidationResult(false, errors);
    }
    if (name.length() < MIN_NAME_LENGTH) {
      errors.add("Назва \"" + name + "\" повинна містити щонайменше " + MIN_NAME_LENGTH + " символи");
    }
    if (name.length() > MAX_NAME_LENGTH) {
      errors.add("Назва \"" + name + "\" не може перевищувати " + MAX_NAME_LENGTH + " символів");
    }
    if (!Pattern.matches(NAME_PATTERN, name)) {
      errors.add("Назва \"" + name + "\" може містити лише літери, пробіли та дефіси");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isNameUnique(String name, UUID itemId, MenuItemRepositoryImpl repository) {
    ValidationResult nameValidation = isNameValid(name);
    if (!nameValidation.isValid()) {
      return nameValidation;
    }

    List<String> errors = new ArrayList<>();
    try {
        MenuItem existingItem = null;
      try {
        existingItem = repository.findByName(name);
      } catch (Exception e) {
          if (!e.getMessage().contains("не знайдено")) {
          throw e;
          }
      }

        if (existingItem != null) {
        if (itemId == null || !existingItem.id().equals(itemId)) {
          errors.add("Назва \"" + name + "\" вже використовується іншим пунктом меню");
          return new ValidationResult(false, errors);
        }
      }
    } catch (Exception e) {
        errors.add("Помилка перевірки унікальності назви: " + e.getMessage());
      return new ValidationResult(false, errors);
    }

      return new ValidationResult(true);
  }

  public static ValidationResult isDescriptionValid(String description) {
    if (description == null) {
      return new ValidationResult(true);
    }
    List<String> errors = new ArrayList<>();
    if (description.length() > MAX_DESCRIPTION_LENGTH) {
      errors.add("Опис (довжина: " + description.length() + " символів) не може перевищувати " + MAX_DESCRIPTION_LENGTH + " символів");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isPriceValid(double price) {
    List<String> errors = new ArrayList<>();
    if (price < MIN_PRICE) {
      errors.add("Ціна (" + price + ") повинна бути не меншою за " + MIN_PRICE);
    }
    if (price > MAX_PRICE) {
      errors.add("Ціна (" + price + ") не може перевищувати " + MAX_PRICE);
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isCaloriesValid(Integer calories) {
    if (calories == null) {
      return new ValidationResult(true);
    }
    List<String> errors = new ArrayList<>();
    if (calories < MIN_CALORIES) {
      errors.add("Калорії (" + calories + ") не можуть бути меншими за " + MIN_CALORIES);
    }
    if (calories > MAX_CALORIES) {
      errors.add("Калорії (" + calories + ") не можуть перевищувати " + MAX_CALORIES);
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isIngredientsValid(String ingredients) {
    if (ingredients == null) {
      return new ValidationResult(true);
    }
    List<String> errors = new ArrayList<>();
    if (ingredients.length() > MAX_DESCRIPTION_LENGTH) {
      errors.add("Список інгредієнтів (довжина: " + ingredients.length() + " символів) не може перевищувати " + MAX_DESCRIPTION_LENGTH + " символів");
    }
    return new ValidationResult(errors.isEmpty(), errors);
  }

  public static ValidationResult isMenuItemValid(MenuItem menuItem, boolean isExisting, MenuItemRepositoryImpl repository) {
    if (menuItem == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Елемент меню не може бути відсутнім");
      return new ValidationResult(false, errors);
    }

    List<String> errors = new ArrayList<>();

      ValidationResult itemIdResult = isItemIdValid(menuItem.id(), isExisting);
    if (!itemIdResult.isValid()) {
      errors.addAll(itemIdResult.getErrors());
    }

      ValidationResult nameResult = isNameValid(menuItem.name());
    if (!nameResult.isValid()) {
      errors.addAll(nameResult.getErrors());
    }

      ValidationResult nameUniqueResult = isNameUnique(menuItem.name(), menuItem.id(), repository);
    if (!nameUniqueResult.isValid()) {
      errors.addAll(nameUniqueResult.getErrors());
    }

      ValidationResult descriptionResult = isDescriptionValid(menuItem.description());
    if (!descriptionResult.isValid()) {
      errors.addAll(descriptionResult.getErrors());
    }

      ValidationResult priceResult = isPriceValid(menuItem.price());
    if (!priceResult.isValid()) {
      errors.addAll(priceResult.getErrors());
    }

      ValidationResult caloriesResult = isCaloriesValid(menuItem.calories());
    if (!caloriesResult.isValid()) {
      errors.addAll(caloriesResult.getErrors());
    }

      ValidationResult ingredientsResult = isIngredientsValid(menuItem.ingredients());
    if (!ingredientsResult.isValid()) {
      errors.addAll(ingredientsResult.getErrors());
    }

      return new ValidationResult(errors.isEmpty(), errors);
  }
}
