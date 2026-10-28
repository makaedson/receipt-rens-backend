package com.receiptrens.controller;

import com.receiptrens.model.Shopper;
import com.receiptrens.service.ShopperService;
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
@RequestMapping("/api/shoppers")
public class ShopperController {
    private final ShopperService service;

    public ShopperController(ShopperService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Shopper shopper) {
        String message = service.save(shopper);
        if (message.equals("Shopper saved successfully")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } else {
            return ResponseEntity.badRequest().body(message);
        }
    }

    @GetMapping("/all")
    public List<Shopper> findAll() {
        return service.findAll();
    }

    @GetMapping("/by-city")
    public List<Shopper> findByCity(@RequestParam String city) {
        return service.findByCity(city);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        return service.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The shopper with that id does not exist"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> update(@PathVariable UUID id, @RequestBody Shopper shopper) {
        String message = service.update(id, shopper);
        if (message.equals("Shopper updated successfully")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String message = service.delete(id);
        if (message.equals("Shopper deleted successfully")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @GetMapping("/of-store/{storeId}")
    public ResponseEntity<?> findByStore(@PathVariable UUID storeId) {
        return service.findByStore(storeId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The store with that id does not exist"));
    }
}
