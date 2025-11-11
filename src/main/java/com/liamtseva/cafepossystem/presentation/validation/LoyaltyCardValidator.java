package com.liamtseva.cafepossystem.presentation.validation;

import com.liamtseva.cafepossystem.persistence.entity.LoyaltyCard;
import com.liamtseva.cafepossystem.persistence.repository.impl.LoyaltyCardRepositoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LoyaltyCardValidator {

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d+$");

    public static ValidationResult isLoyaltyCardValid(LoyaltyCard loyaltyCard, boolean isExisting, LoyaltyCardRepositoryImpl repository) {
        if (loyaltyCard == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Картка лояльності не може бути порожньою.");
            return new ValidationResult(false, errors);
        }

        List<String> errors = new ArrayList<>();

        if (isExisting && loyaltyCard.id() == null) {
            errors.add("Ідентифікатор (ID) не може бути порожнім для існуючої картки лояльності.");
        }

        if (loyaltyCard.userId() == null) {
            errors.add("Ідентифікатор користувача не може бути порожнім.");
        }

        ValidationResult cardNumberResult = isCardNumberValid(loyaltyCard.cardNumber());
        if (!cardNumberResult.isValid()) {
            errors.addAll(cardNumberResult.getErrors());
        }

        ValidationResult balanceResult = isBalanceValid(loyaltyCard.balance());
        if (!balanceResult.isValid()) {
            errors.addAll(balanceResult.getErrors());
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static ValidationResult isCardNumberValid(String cardNumber) {
        List<String> errors = new ArrayList<>();
        if (cardNumber == null || cardNumber.isEmpty()) {
            errors.add("Номер картки не може бути порожнім.");
        } else if (!CARD_NUMBER_PATTERN.matcher(cardNumber).matches()) {
        errors.add("Номер картки має містити тільки цифри.");
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static ValidationResult isBalanceValid(double balance) {
        List<String> errors = new ArrayList<>();
        if (balance < 0) {
            errors.add("Баланс не може бути від’ємним.");
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
