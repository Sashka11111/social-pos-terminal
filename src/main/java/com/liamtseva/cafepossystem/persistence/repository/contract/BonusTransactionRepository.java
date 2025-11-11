package com.liamtseva.cafepossystem.persistence.repository.contract;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.BonusTransaction;
import java.util.List;
import java.util.UUID;

public interface BonusTransactionRepository {
    BonusTransaction findById(UUID id) throws EntityNotFoundException;
    List<BonusTransaction> findByCardId(UUID cardId);
    List<BonusTransaction> findAll();
    BonusTransaction save(BonusTransaction bonusTransaction);
    void deleteById(UUID id) throws EntityNotFoundException;
}
