package com.receiptrens.service;

import com.receiptrens.model.ExpenseCategory;
import com.receiptrens.repository.ExpenseCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExpenseCategoryService {
    private final ExpenseCategoryRepository repository;

    public ExpenseCategoryService(ExpenseCategoryRepository repository) {
        this.repository = repository;
    }

    public String save(ExpenseCategory category) {
        repository.save(category);
        return "Expense category saved successfully";
    }

    public List<ExpenseCategory> findAll() {
        return repository.findAll();
    }

    public Optional<ExpenseCategory> findById(UUID id) {
        return repository.findById(id);
    }

    public String update(UUID id, ExpenseCategory category) {
        if (repository.findById(id).isEmpty()) {
            return "The expense category with that id does not exist";
        }
        category.setId(id);
        repository.save(category);
        return "Expense category updated successfully";
    }

    public String delete(UUID id) {
        if (repository.findById(id).isEmpty()) {
            return "The expense category with that id does not exist";
        }
        repository.deleteById(id);
        return "Expense category deleted successfully";
    }

    public List<ExpenseCategory> findUnused() {
        return repository.findUnusedCategories();
    }
}
