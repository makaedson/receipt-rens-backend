package com.receiptrens.service;

import com.receiptrens.model.Receipt;
import com.receiptrens.model.ReceiptStatus;
import com.receiptrens.model.Store;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {
    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private ExpenseEntryRepository expenseEntryRepository;

    private ReceiptService service;

    @BeforeEach
    void setUp() {
        service = new ReceiptService(receiptRepository, expenseEntryRepository);
    }

    @Test
    void rejectsPastReceiptDatesWithoutSaving() {
        Receipt receipt = new Receipt();
        receipt.setReceiptDate(LocalDate.now().minusDays(1));

        assertEquals("Receipt date cannot be in the past", service.save(receipt));
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    void createsReceiptAsUploadedEvenWhenAnotherStatusWasSupplied() {
        Store store = new Store();
        Receipt receipt = new Receipt();
        receipt.setStore(store);
        receipt.setReceiptDate(LocalDate.now());
        receipt.setStatus(ReceiptStatus.APPROVED);
        when(receiptRepository.existsByStoreAndReceiptDateAndStatusNot(
                store, receipt.getReceiptDate(), ReceiptStatus.REJECTED)).thenReturn(false);

        assertEquals("Receipt saved successfully", service.save(receipt));

        ArgumentCaptor<Receipt> saved = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).save(saved.capture());
        assertEquals(ReceiptStatus.UPLOADED, saved.getValue().getStatus());
    }

    @Test
    void rejectsDuplicateNonRejectedReceipt() {
        Store store = new Store();
        Receipt receipt = new Receipt();
        receipt.setStore(store);
        receipt.setReceiptDate(LocalDate.now());
        when(receiptRepository.existsByStoreAndReceiptDateAndStatusNot(
                store, receipt.getReceiptDate(), ReceiptStatus.REJECTED)).thenReturn(true);

        assertEquals(
                "A receipt from this store already exists for that date",
                service.save(receipt)
        );
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    void allowsOnlyTheSpecifiedStatusTransitions() {
        UUID id = UUID.randomUUID();
        Receipt receipt = new Receipt();
        receipt.setStatus(ReceiptStatus.UPLOADED);
        when(receiptRepository.findById(id)).thenReturn(Optional.of(receipt));

        assertEquals("Receipt status updated successfully",
                service.updateStatus(id, ReceiptStatus.SCANNED));
        assertEquals(ReceiptStatus.SCANNED, receipt.getStatus());

        assertEquals("Receipt status updated successfully",
                service.updateStatus(id, ReceiptStatus.APPROVED));
        assertEquals(ReceiptStatus.APPROVED, receipt.getStatus());

        assertEquals("Cannot change status from APPROVED to UPLOADED",
                service.updateStatus(id, ReceiptStatus.UPLOADED));
        verify(receiptRepository, org.mockito.Mockito.times(2)).save(receipt);
    }

    @Test
    void generalUpdatePreservesExistingReceiptStatus() {
        UUID id = UUID.randomUUID();
        Receipt existing = new Receipt();
        existing.setStatus(ReceiptStatus.SCANNED);
        Receipt update = new Receipt();
        update.setStatus(ReceiptStatus.REJECTED);
        when(receiptRepository.findById(id)).thenReturn(Optional.of(existing));

        assertEquals("Receipt updated successfully", service.update(id, update));

        ArgumentCaptor<Receipt> saved = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).save(saved.capture());
        assertEquals(id, saved.getValue().getId());
        assertEquals(ReceiptStatus.SCANNED, saved.getValue().getStatus());
    }

    @Test
    void receiptWithExpenseEntriesCannotBeDeleted() {
        UUID id = UUID.randomUUID();
        Receipt receipt = new Receipt();
        when(receiptRepository.findById(id)).thenReturn(Optional.of(receipt));
        when(expenseEntryRepository.existsByReceipt(receipt)).thenReturn(true);

        assertEquals("That receipt has expense entries, delete them first", service.delete(id));
        verify(receiptRepository, never()).deleteById(id);
    }

    @Test
    void bulkCancellationReturnsTheAffectedRowCount() {
        UUID storeId = UUID.randomUUID();
        when(receiptRepository.rejectReceiptsForStoreOnDate(eq(storeId), any(LocalDate.class)))
                .thenReturn(3);

        assertEquals("3 receipts cancelled", service.cancelDay(storeId, LocalDate.of(2026, 10, 20)));
        verify(receiptRepository).rejectReceiptsForStoreOnDate(storeId, LocalDate.of(2026, 10, 20));
    }
}
