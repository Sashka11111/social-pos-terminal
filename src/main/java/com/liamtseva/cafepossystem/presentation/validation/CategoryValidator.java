package com.liamtseva.cafepossystem.presentation.validation;

import com.liamtseva.cafepossystem.persistence.entity.Category;
import com.liamtseva.cafepossystem.persistence.repository.impl.CategoryRepositoryImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class CategoryValidator {
  private static final int MIN_NAME_LENGTH = 2;
  private static final int MAX_NAME_LENGTH = 50;

  private static final String NAME_PATTERN = "^[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ\\s-]+$";

  public static ValidationResult isCategoryIdValid(UUID categoryId, boolean isExisting) {
    if (isExisting && categoryId == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Ідентифікатор категорії не може бути відсутнім для існуючої категорії");
      return new ValidationResult(false, errors);
    }
    return new ValidationResult(true);
  }

  public static ValidationResult isNameValid(String name) {
    List<String> errors = new ArrayList<>();
    if (name == null) {
      errors.add("Назва категорії не може бути відсутньою");
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

  public static ValidationResult isNameUnique(String name, UUID categoryId, CategoryRepositoryImpl repository) {
    ValidationResult nameValidation = isNameValid(name);
    if (!nameValidation.isValid()) {
      return nameValidation;
    }

    List<String> errors = new ArrayList<>();
    try {
        Category existingCategory = null;
      try {
        existingCategory = repository.findByName(name);
      } catch (Exception e) {
          if (!e.getMessage().contains("не знайдено")) {
          throw e;
          }
      }

        if (existingCategory != null) {
            if (categoryId == null || !existingCategory.id().equals(categoryId)) {
          errors.add("Назва \"" + name + "\" вже використовується іншою категорією");
          return new ValidationResult(false, errors);
        }
      }
    } catch (Exception e) {
        errors.add("Помилка перевірки унікальності назви: " + e.getMessage());
      return new ValidationResult(false, errors);
    }

    return new ValidationResult(true);
  }

  public static ValidationResult isCategoryValid(Category category, boolean isExisting, CategoryRepositoryImpl repository) {
    if (category == null) {
      List<String> errors = new ArrayList<>();
      errors.add("Категорія не може бути відсутньою");
      return new ValidationResult(false, errors);
    }

    List<String> errors = new ArrayList<>();

      ValidationResult categoryIdResult = isCategoryIdValid(category.id(), isExisting);
    if (!categoryIdResult.isValid()) {
      errors.addAll(categoryIdResult.getErrors());
    }

      ValidationResult nameResult = isNameValid(category.categoryName());
    if (!nameResult.isValid()) {
      errors.addAll(nameResult.getErrors());
    }

      ValidationResult nameUniqueResult = isNameUnique(category.categoryName(), category.id(), repository);
    if (!nameUniqueResult.isValid()) {
      errors.addAll(nameUniqueResult.getErrors());
    }

      return new ValidationResult(errors.isEmpty(), errors);
  }
}
