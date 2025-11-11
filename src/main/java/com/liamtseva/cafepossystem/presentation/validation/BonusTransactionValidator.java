package com.liamtseva.cafepossystem.presentation.validation;

import com.liamtseva.cafepossystem.persistence.entity.BonusTransaction;
import java.util.ArrayList;
import java.util.List;

public class BonusTransactionValidator {

    public static ValidationResult isBonusTransactionValid(BonusTransaction bonusTransaction, boolean isExisting) {
        if (bonusTransaction == null) {
            List<String> errors = new ArrayList<>();
            errors.add("Транзакція бонусів не може бути відсутньою");
            return new ValidationResult(false, errors);
        }

        List<String> errors = new ArrayList<>();

        if (isExisting && bonusTransaction.id() == null) {
            errors.add("Ідентифікатор не може бути відсутнім для існуючої транзакції бонусів");
        }

        if (bonusTransaction.cardId() == null) {
            errors.add("Ідентифікатор картки не може бути відсутнім");
        }

        ValidationResult amountResult = isAmountValid(bonusTransaction.amount());
        if (!amountResult.isValid()) {
            errors.addAll(amountResult.getErrors());
        }

        if (bonusTransaction.type() == null) {
            errors.add("Тип транзакції не може бути відсутнім");
        }

        if (bonusTransaction.transactionDate() == null) {
            errors.add("Дата транзакції не може бути відсутньою");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static ValidationResult isAmountValid(double amount) {
        List<String> errors = new ArrayList<>();
        if (amount <= 0) {
            errors.add("Сума повинна бути більшою за 0");
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
