package com.receiptrens.service;

import com.receiptrens.model.Shopper;
import com.receiptrens.repository.ShopperRepository;
import com.receiptrens.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShopperService {
    private final ShopperRepository shopperRepository;
    private final StoreRepository storeRepository;

    public ShopperService(ShopperRepository shopperRepository, StoreRepository storeRepository) {
        this.shopperRepository = shopperRepository;
        this.storeRepository = storeRepository;
    }

    public String save(Shopper shopper) {
        shopperRepository.save(shopper);
        return "Shopper saved successfully";
    }

    public List<Shopper> findAll() {
        return shopperRepository.findAll();
    }

    public Optional<Shopper> findById(UUID id) {
        return shopperRepository.findById(id);
    }

    public String update(UUID id, Shopper shopper) {
        if (shopperRepository.findById(id).isEmpty()) {
            return "The shopper with that id does not exist";
        }
        shopper.setId(id);
        shopperRepository.save(shopper);
        return "Shopper updated successfully";
    }

    public String delete(UUID id) {
        if (shopperRepository.findById(id).isEmpty()) {
            return "The shopper with that id does not exist";
        }
        shopperRepository.deleteById(id);
        return "Shopper deleted successfully";
    }

    public List<Shopper> findByCity(String city) {
        return shopperRepository.findByCityIgnoreCaseOrderByLastNameAsc(city);
    }

    public Optional<List<Shopper>> findByStore(UUID storeId) {
        if (!storeRepository.existsById(storeId)) {
            return Optional.empty();
        }
        return Optional.of(shopperRepository.findShoppersByStoreId(storeId));
    }
}
