package com.liamtseva.cafepossystem.persistence.repository.contract;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.Cart;
import java.util.List;
import java.util.UUID;

public interface CartRepository {
    Cart findById(UUID id) throws EntityNotFoundException;
    List<Cart> findByUserId(UUID userId);
    List<Cart> findAll();
    Cart save(Cart cart);
    void deleteById(UUID id) throws EntityNotFoundException;
}
