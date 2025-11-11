package com.liamtseva.cafepossystem.persistence.repository.contract;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.LoyaltyCard;
import java.util.List;
import java.util.UUID;

public interface LoyaltyCardRepository {
    LoyaltyCard findById(UUID id) throws EntityNotFoundException;
    LoyaltyCard findByUserId(UUID userId) throws EntityNotFoundException;
    List<LoyaltyCard> findAll();
    LoyaltyCard save(LoyaltyCard loyaltyCard);
    void deleteById(UUID id) throws EntityNotFoundException;
}
