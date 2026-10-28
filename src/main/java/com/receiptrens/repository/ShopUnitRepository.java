package com.receiptrens.repository;

import com.receiptrens.model.ShopUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopUnitRepository extends JpaRepository<ShopUnit, UUID> {
    Optional<ShopUnit> findByUnitNumber(int unitNumber);

    @Query("SELECT u FROM ShopUnit u WHERE NOT EXISTS "
            + "(SELECT s FROM Store s WHERE s.shopUnit = u) ORDER BY u.unitNumber")
    List<ShopUnit> findFreeShopUnits();
}
