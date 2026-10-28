package com.receiptrens.controller;

import com.receiptrens.model.Receipt;
import com.receiptrens.model.ReceiptStatus;
import com.receiptrens.service.ReceiptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {
    private final ReceiptService service;

    public ReceiptController(ReceiptService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Receipt receipt) {
        String message = service.save(receipt);
        if (message.equals("Receipt saved successfully")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } else if (message.equals("Receipt date cannot be in the past")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        } else if (message.equals("A receipt from this store already exists for that date")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.badRequest().body(message);
        }
    }

    @GetMapping("/all")
    public List<Receipt> findAll() {
        return service.findAll();
    }

    @GetMapping("/by-status")
    public List<Receipt> findByStatus(@RequestParam ReceiptStatus status) {
        return service.findByStatus(status);
    }

    @GetMapping("/between")
    public List<Receipt> findBetween(@RequestParam String start, @RequestParam String end) {
        return service.findBetween(LocalDate.parse(start), LocalDate.parse(end));
    }

    @GetMapping("/stats/by-status")
    public List<Object[]> countByStatus() {
        return service.countByStatus();
    }

    @GetMapping("/page")
    public Page<Receipt> findPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "receiptDate,desc") String sort
    ) {
        String[] sortOptions = sort.split(",", 2);
        Sort ordering = Sort.by(Sort.Direction.fromString(sortOptions[1]), sortOptions[0]);
        Pageable pageable = PageRequest.of(page, size, ordering);
        return service.findPage(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        return service.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The receipt with that id does not exist"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> update(@PathVariable UUID id, @RequestBody Receipt receipt) {
        String message = service.update(id, receipt);
        if (message.equals("Receipt updated successfully")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String message = service.delete(id);
        if (message.equals("Receipt deleted successfully")) {
            return ResponseEntity.ok(message);
        } else if (message.equals("That receipt has expense entries, delete them first")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable UUID id,
            @RequestParam ReceiptStatus status
    ) {
        String message = service.updateStatus(id, status);
        if (message.equals("The receipt with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.startsWith("Cannot change status from ")) {
            return ResponseEntity.badRequest().body(message);
        } else {
            return ResponseEntity.ok(message);
        }
    }

    @PatchMapping("/cancel-day")
    public ResponseEntity<String> cancelDay(
            @RequestParam UUID storeId,
            @RequestParam LocalDate date
    ) {
        String message = service.cancelDay(storeId, date);
        if (message.equals("The store with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else {
            return ResponseEntity.ok(message);
        }
    }
}
