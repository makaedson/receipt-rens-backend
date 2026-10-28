package com.receiptrens.repository;

import com.receiptrens.model.ExpenseEntry;
import com.receiptrens.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExpenseEntryRepository extends JpaRepository<ExpenseEntry, UUID> {
    boolean existsByReceipt(Receipt receipt);

    @Query("SELECT e FROM ExpenseEntry e WHERE e.receipt.shopper.username = :username "
            + "ORDER BY e.enteredDate DESC")
    List<ExpenseEntry> findByShopperUsername(@Param("username") String username);
}
