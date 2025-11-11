package com.liamtseva.cafepossystem.persistence.repository.contract;

import com.liamtseva.cafepossystem.domain.exception.EntityNotFoundException;
import com.liamtseva.cafepossystem.persistence.entity.Category;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository {
  Category findById(UUID id) throws EntityNotFoundException;
  Category findByName(String categoryName) throws EntityNotFoundException;
  List<Category> findAll();
  Category save(Category category);
  void deleteById(UUID id) throws EntityNotFoundException;
}
