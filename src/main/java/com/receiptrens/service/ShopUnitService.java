package com.receiptrens.service;

import com.receiptrens.model.ShopUnit;
import com.receiptrens.repository.ShopUnitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShopUnitService {
    private final ShopUnitRepository repository;

    public ShopUnitService(ShopUnitRepository repository) {
        this.repository = repository;
    }

    public String save(ShopUnit shopUnit) {
        repository.save(shopUnit);
        return "Shop unit saved successfully";
    }

    public List<ShopUnit> findAll() {
        return repository.findAll();
    }

    public Optional<ShopUnit> findById(UUID id) {
        return repository.findById(id);
    }

    public String update(UUID id, ShopUnit shopUnit) {
        if (repository.findById(id).isEmpty()) {
            return "The shop unit with that id does not exist";
        }
        shopUnit.setId(id);
        repository.save(shopUnit);
        return "Shop unit updated successfully";
    }

    public String delete(UUID id) {
        if (repository.findById(id).isEmpty()) {
            return "The shop unit with that id does not exist";
        }
        repository.deleteById(id);
        return "Shop unit deleted successfully";
    }

    public List<ShopUnit> findFree() {
        return repository.findFreeShopUnits();
    }
}
