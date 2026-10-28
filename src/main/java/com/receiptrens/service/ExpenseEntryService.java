package com.receiptrens.service;

import com.receiptrens.model.ExpenseEntry;
import com.receiptrens.model.Receipt;
import com.receiptrens.model.ReceiptStatus;
import com.receiptrens.repository.ExpenseEntryRepository;
import com.receiptrens.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExpenseEntryService {
    private final ExpenseEntryRepository expenseEntryRepository;
    private final ReceiptRepository receiptRepository;

    public ExpenseEntryService(
            ExpenseEntryRepository expenseEntryRepository,
            ReceiptRepository receiptRepository
    ) {
        this.expenseEntryRepository = expenseEntryRepository;
        this.receiptRepository = receiptRepository;
    }

    public String save(ExpenseEntry entry, UUID receiptId) {
        Optional<Receipt> receiptResult = receiptRepository.findById(receiptId);
        if (receiptResult.isEmpty()) {
            return "The receipt with that id does not exist";
        }
        Receipt receipt = receiptResult.get();
        if (receipt.getStatus() != ReceiptStatus.APPROVED) {
            return "Expense entries are only for APPROVED receipts";
        }

        entry.setReceipt(receipt);
        entry.setEnteredDate(LocalDate.now());
        expenseEntryRepository.save(entry);
        return "Expense entry saved successfully";
    }

    public List<ExpenseEntry> findByShopper(String username) {
        return expenseEntryRepository.findByShopperUsername(username);
    }

    public String delete(UUID id) {
        if (expenseEntryRepository.findById(id).isEmpty()) {
            return "The expense entry with that id does not exist";
        }
        expenseEntryRepository.deleteById(id);
        return "Expense entry deleted successfully";
    }
}
