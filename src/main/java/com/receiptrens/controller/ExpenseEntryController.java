package com.receiptrens.controller;

import com.receiptrens.model.ExpenseEntry;
import com.receiptrens.service.ExpenseEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expense-entries")
public class ExpenseEntryController {
    private final ExpenseEntryService service;

    public ExpenseEntryController(ExpenseEntryService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(
            @RequestParam UUID receiptId,
            @RequestBody ExpenseEntry entry
    ) {
        String message = service.save(entry, receiptId);
        if (message.equals("Expense entry saved successfully")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } else if (message.equals("The receipt with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("Expense entries are only for APPROVED receipts")) {
            return ResponseEntity.badRequest().body(message);
        } else {
            return ResponseEntity.badRequest().body(message);
        }
    }

    @GetMapping("/shopper/{username}")
    public List<ExpenseEntry> findByShopper(@PathVariable String username) {
        return service.findByShopper(username);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String message = service.delete(id);
        if (message.equals("Expense entry deleted successfully")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }
}
