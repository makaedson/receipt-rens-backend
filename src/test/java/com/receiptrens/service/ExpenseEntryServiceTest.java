package com.receiptrens.service;

import com.receiptrens.model.ExpenseEntry;
import com.receiptrens.model.Receipt;
import com.receiptrens.model.ReceiptStatus;
import com.receiptrens.repository.ExpenseEntryRepository;
import com.receiptrens.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ExpenseEntryServiceTest {
    @Mock
    private ExpenseEntryRepository expenseEntryRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    private ExpenseEntryService service;

    @BeforeEach
    void setUp() {
        service = new ExpenseEntryService(expenseEntryRepository, receiptRepository);
    }

    @Test
    void refusesEntriesForReceiptsThatAreNotApproved() {
        UUID receiptId = UUID.randomUUID();
        Receipt receipt = new Receipt();
        receipt.setStatus(ReceiptStatus.SCANNED);
        when(receiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));

        assertEquals(
                "Expense entries are only for APPROVED receipts",
                service.save(new ExpenseEntry(), receiptId)
        );
        verify(expenseEntryRepository, never()).save(any(ExpenseEntry.class));
    }

    @Test
    void savesEntryForApprovedReceiptWithTodayDate() {
        UUID receiptId = UUID.randomUUID();
        Receipt receipt = new Receipt();
        receipt.setStatus(ReceiptStatus.APPROVED);
        ExpenseEntry entry = new ExpenseEntry();
        when(receiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));

        assertEquals("Expense entry saved successfully", service.save(entry, receiptId));

        ArgumentCaptor<ExpenseEntry> saved = ArgumentCaptor.forClass(ExpenseEntry.class);
        verify(expenseEntryRepository).save(saved.capture());
        assertEquals(LocalDate.now(), saved.getValue().getEnteredDate());
        assertEquals(receipt, saved.getValue().getReceipt());
    }
}
