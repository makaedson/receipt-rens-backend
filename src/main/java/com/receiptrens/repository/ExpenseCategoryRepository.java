package com.receiptrens.repository;

import com.receiptrens.model.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, UUID> {
    @Query("SELECT c FROM ExpenseCategory c WHERE c.stores IS EMPTY")
    List<ExpenseCategory> findUnusedCategories();
}
