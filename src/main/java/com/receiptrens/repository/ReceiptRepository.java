package com.receiptrens.repository;

import com.receiptrens.model.Receipt;
import com.receiptrens.model.ReceiptStatus;
import com.receiptrens.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {
    List<Receipt> findByStatusOrderByReceiptDateAsc(ReceiptStatus status);

    List<Receipt> findByReceiptDateBetweenOrderByReceiptDateAsc(LocalDate start, LocalDate end);

    boolean existsByStoreAndReceiptDateAndStatusNot(Store store, LocalDate receiptDate, ReceiptStatus status);

    long countByStatus(ReceiptStatus status);

    long countByReceiptDate(LocalDate receiptDate);

    @Query("SELECT r.status, COUNT(r) FROM Receipt r GROUP BY r.status")
    List<Object[]> countReceiptsByStatus();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Receipt r SET r.status = com.receiptrens.model.ReceiptStatus.REJECTED "
            + "WHERE r.store.id = :storeId AND r.receiptDate = :date "
            + "AND r.status <> com.receiptrens.model.ReceiptStatus.APPROVED")
    int rejectReceiptsForStoreOnDate(@Param("storeId") UUID storeId, @Param("date") LocalDate date);
}
