package com.receiptrens.controller;

import com.receiptrens.model.Store;
import com.receiptrens.service.StoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
public class StoreController {
    private final StoreService service;

    public StoreController(StoreService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Store store) {
        String message = service.save(store);
        if (message.equals("Store saved successfully")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } else {
            return ResponseEntity.badRequest().body(message);
        }
    }

    @GetMapping("/all")
    public List<Store> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        return service.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The store with that id does not exist"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> update(@PathVariable UUID id, @RequestBody Store store) {
        String message = service.update(id, store);
        if (message.equals("Store updated successfully")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String message = service.delete(id);
        if (message.equals("Store deleted successfully")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @GetMapping("/by-expense-category")
    public List<Store> findByExpenseCategory(@RequestParam String name) {
        return service.findByExpenseCategory(name);
    }

    @PutMapping("/{storeId}/expense-categories/{expenseCategoryId}")
    public ResponseEntity<String> addExpenseCategory(
            @PathVariable UUID storeId,
            @PathVariable UUID expenseCategoryId
    ) {
        String message = service.addExpenseCategory(storeId, expenseCategoryId);
        if (message.equals("The store with that id does not exist")
                || message.equals("That expense category does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("The store already has that expense category")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else if (message.equals("Expense category added to the store")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.badRequest().body(message);
        }
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{storeId}/shop-unit")
    public ResponseEntity<String> moveToShopUnit(
            @PathVariable UUID storeId,
            @RequestParam int unitNumber
    ) {
        String message = service.moveToShopUnit(storeId, unitNumber);
        if (message.equals("The store with that id does not exist")
                || message.equals("The shop unit with that unitNumber does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("That shop unit is taken by another store")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else if (message.equals("Shop unit changed successfully")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.badRequest().body(message);
        }
    }
}
