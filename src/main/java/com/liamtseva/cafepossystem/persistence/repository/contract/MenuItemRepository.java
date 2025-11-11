package com.liamtseva.cafepossystem.persistence.repository.contract;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.Category;
import com.liamtseva.cafepossystem.persistence.entity.MenuItem;
import java.util.List;
import java.util.UUID;

public interface MenuItemRepository {
  MenuItem findById(UUID id) throws EntityNotFoundException;
  MenuItem findByName(String name) throws EntityNotFoundException;
  List<MenuItem> findAll();
  List<MenuItem> findByCategoryId(UUID categoryId);
  List<MenuItem> findCartItemsByUserId(UUID userId);
  List<Category> findCategoriesByItemId(UUID itemId);
  MenuItem save(MenuItem menuItem);
  void deleteById(UUID id) throws EntityNotFoundException;
  void saveItemCategories(UUID itemId, List<UUID> categoryIds);
  void deleteItemCategories(UUID itemId);
  void updateItemCategories(UUID itemId, List<UUID> categoryIds);
}
