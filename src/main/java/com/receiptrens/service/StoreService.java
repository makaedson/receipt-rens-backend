package com.receiptrens.service;

import com.receiptrens.model.ExpenseCategory;
import com.receiptrens.model.ShopUnit;
import com.receiptrens.model.Store;
import com.receiptrens.repository.ExpenseCategoryRepository;
import com.receiptrens.repository.ShopUnitRepository;
import com.receiptrens.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StoreService {
    private final StoreRepository storeRepository;
    private final ShopUnitRepository shopUnitRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    public StoreService(
            StoreRepository storeRepository,
            ShopUnitRepository shopUnitRepository,
            ExpenseCategoryRepository expenseCategoryRepository
    ) {
        this.storeRepository = storeRepository;
        this.shopUnitRepository = shopUnitRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
    }

    public String save(Store store) {
        storeRepository.save(store);
        return "Store saved successfully";
    }

    public List<Store> findAll() {
        return storeRepository.findAll();
    }

    public Optional<Store> findById(UUID id) {
        return storeRepository.findById(id);
    }

    public String update(UUID id, Store store) {
        if (storeRepository.findById(id).isEmpty()) {
            return "The store with that id does not exist";
        }
        store.setId(id);
        storeRepository.save(store);
        return "Store updated successfully";
    }

    public String delete(UUID id) {
        if (storeRepository.findById(id).isEmpty()) {
            return "The store with that id does not exist";
        }
        storeRepository.deleteById(id);
        return "Store deleted successfully";
    }

    public List<Store> findByExpenseCategory(String name) {
        return storeRepository.findByExpenseCategoryName(name);
    }

    @Transactional
    public String addExpenseCategory(UUID storeId, UUID categoryId) {
        Optional<Store> storeResult = storeRepository.findById(storeId);
        if (storeResult.isEmpty()) {
            return "The store with that id does not exist";
        }

        Optional<ExpenseCategory> categoryResult = expenseCategoryRepository.findById(categoryId);
        if (categoryResult.isEmpty()) {
            return "That expense category does not exist";
        }

        Store store = storeResult.get();
        ExpenseCategory category = categoryResult.get();
        if (store.getExpenseCategories() == null) {
            store.setExpenseCategories(new ArrayList<>());
        }
        if (store.getExpenseCategories().contains(category)) {
            return "The store already has that expense category";
        }

        store.getExpenseCategories().add(category);
        storeRepository.save(store);
        return "Expense category added to the store";
    }

    public String moveToShopUnit(UUID storeId, int unitNumber) {
        Optional<Store> storeResult = storeRepository.findById(storeId);
        if (storeResult.isEmpty()) {
            return "The store with that id does not exist";
        }

        Optional<ShopUnit> unitResult = shopUnitRepository.findByUnitNumber(unitNumber);
        if (unitResult.isEmpty()) {
            return "The shop unit with that unitNumber does not exist";
        }

        Store store = storeResult.get();
        ShopUnit unit = unitResult.get();
        if (storeRepository.existsByShopUnit(unit)
                && (store.getShopUnit() == null || !store.getShopUnit().getId().equals(unit.getId()))) {
            return "That shop unit is taken by another store";
        }

        store.setShopUnit(unit);
        storeRepository.save(store);
        return "Shop unit changed successfully";
    }
}
