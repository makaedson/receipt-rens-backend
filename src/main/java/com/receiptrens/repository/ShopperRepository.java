package com.receiptrens.repository;

import com.receiptrens.model.Shopper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ShopperRepository extends JpaRepository<Shopper, UUID> {
    List<Shopper> findByCityIgnoreCaseOrderByLastNameAsc(String city);

    @Query("SELECT DISTINCT r.shopper FROM Receipt r WHERE r.store.id = :storeId")
    List<Shopper> findShoppersByStoreId(@Param("storeId") UUID storeId);
}
