package com.receiptrens.repository;

import com.receiptrens.model.ShopUnit;
import com.receiptrens.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    boolean existsByShopUnit(ShopUnit shopUnit);

    @Query("SELECT DISTINCT s FROM Store s JOIN s.expenseCategories c "
            + "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Store> findByExpenseCategoryName(@Param("name") String name);
}
