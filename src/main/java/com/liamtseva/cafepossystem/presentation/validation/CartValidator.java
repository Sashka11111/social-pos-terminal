package com.liamtseva.cafepossystem.presentation.validation;

import com.liamtseva.cafepossystem.persistence.entity.Cart;

import java.util.ArrayList;
import java.util.List;

public class CartValidator {

    public static ValidationResult isCartValid(Cart cart, boolean isExisting) {
        if (cart == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Cart cannot be null");
            return new ValidationResult(false, errors);
        }

        List<String> errors = new ArrayList<>();

        if (isExisting && cart.id() == null) {
            errors.add("ID cannot be null for an existing cart");
        }

        if (cart.userId() == null) {
            errors.add("User ID cannot be null");
        }

        if (cart.itemId() == null) {
            errors.add("Item ID cannot be null");
        }

        ValidationResult quantityResult = isQuantityValid(cart.quantity());
        if (!quantityResult.isValid()) {
            errors.addAll(quantityResult.getErrors());
        }

        ValidationResult subtotalResult = isSubtotalValid(cart.subtotal());
        if (!subtotalResult.isValid()) {
            errors.addAll(subtotalResult.getErrors());
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static ValidationResult isQuantityValid(int quantity) {
        List<String> errors = new ArrayList<>();
        if (quantity <= 0) {
            errors.add("Quantity must be positive");
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static ValidationResult isSubtotalValid(double subtotal) {
        List<String> errors = new ArrayList<>();
        if (subtotal < 0) {
            errors.add("Subtotal cannot be negative");
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
