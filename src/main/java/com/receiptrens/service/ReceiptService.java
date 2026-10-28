package com.receiptrens.service;

import com.receiptrens.model.Receipt;
import com.receiptrens.model.ReceiptStatus;
import com.receiptrens.repository.ExpenseEntryRepository;
import com.receiptrens.repository.ReceiptRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final ExpenseEntryRepository expenseEntryRepository;

    public ReceiptService(
            ReceiptRepository receiptRepository,
            ExpenseEntryRepository expenseEntryRepository
    ) {
        this.receiptRepository = receiptRepository;
        this.expenseEntryRepository = expenseEntryRepository;
    }

    public String save(Receipt receipt) {
        if (receipt.getReceiptDate().isBefore(LocalDate.now())) {
            return "Receipt date cannot be in the past";
        }
        if (receiptRepository.existsByStoreAndReceiptDateAndStatusNot(
                receipt.getStore(), receipt.getReceiptDate(), ReceiptStatus.REJECTED)) {
            return "A receipt from this store already exists for that date";
        }
        receipt.setStatus(ReceiptStatus.UPLOADED);
        receiptRepository.save(receipt);
        return "Receipt saved successfully";
    }

    public List<Receipt> findAll() {
        return receiptRepository.findAll();
    }

    public Optional<Receipt> findById(UUID id) {
        return receiptRepository.findById(id);
    }

    public String update(UUID id, Receipt receipt) {
        Optional<Receipt> currentResult = receiptRepository.findById(id);
        if (currentResult.isEmpty()) {
            return "The receipt with that id does not exist";
        }
        receipt.setId(id);
        receipt.setStatus(currentResult.get().getStatus());
        receiptRepository.save(receipt);
        return "Receipt updated successfully";
    }

    public String delete(UUID id) {
        Optional<Receipt> receipt = receiptRepository.findById(id);
        if (receipt.isEmpty()) {
            return "The receipt with that id does not exist";
        }
        if (expenseEntryRepository.existsByReceipt(receipt.get())) {
            return "That receipt has expense entries, delete them first";
        }
        receiptRepository.deleteById(id);
        return "Receipt deleted successfully";
    }

    public List<Receipt> findByStatus(ReceiptStatus status) {
        return receiptRepository.findByStatusOrderByReceiptDateAsc(status);
    }

    public List<Receipt> findBetween(LocalDate start, LocalDate end) {
        return receiptRepository.findByReceiptDateBetweenOrderByReceiptDateAsc(start, end);
    }

    public String updateStatus(UUID id, ReceiptStatus requestedStatus) {
        Optional<Receipt> receiptResult = receiptRepository.findById(id);
        if (receiptResult.isEmpty()) {
            return "The receipt with that id does not exist";
        }

        Receipt receipt = receiptResult.get();
        ReceiptStatus currentStatus = receipt.getStatus();
        boolean allowed = currentStatus == ReceiptStatus.UPLOADED
                && (requestedStatus == ReceiptStatus.SCANNED || requestedStatus == ReceiptStatus.REJECTED)
                || currentStatus == ReceiptStatus.SCANNED
                && (requestedStatus == ReceiptStatus.APPROVED || requestedStatus == ReceiptStatus.REJECTED);
        if (!allowed) {
            return "Cannot change status from " + currentStatus + " to " + requestedStatus;
        }

        receipt.setStatus(requestedStatus);
        receiptRepository.save(receipt);
        return "Receipt status updated successfully";
    }

    public List<Object[]> countByStatus() {
        return receiptRepository.countReceiptsByStatus();
    }

    @Transactional
    public String cancelDay(UUID storeId, LocalDate date) {
        int count = receiptRepository.rejectReceiptsForStoreOnDate(storeId, date);
        return count + " receipts cancelled";
    }

    public Page<Receipt> findPage(Pageable pageable) {
        return receiptRepository.findAll(pageable);
    }

    public long countByStatus(ReceiptStatus status) {
        return receiptRepository.countByStatus(status);
    }

    public long countByReceiptDate(LocalDate date) {
        return receiptRepository.countByReceiptDate(date);
    }
}
