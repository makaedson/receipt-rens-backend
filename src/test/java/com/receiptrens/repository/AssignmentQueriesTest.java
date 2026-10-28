package com.receiptrens.repository;

import com.receiptrens.model.ExpenseCategory;
import com.receiptrens.model.ExpenseEntry;
import com.receiptrens.model.Receipt;
import com.receiptrens.model.ReceiptStatus;
import com.receiptrens.model.ShopUnit;
import com.receiptrens.model.Shopper;
import com.receiptrens.model.Store;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class AssignmentQueriesTest {
    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Autowired
    private ExpenseEntryRepository expenseEntryRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ShopUnitRepository shopUnitRepository;

    @Autowired
    private ShopperRepository shopperRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void derivedAndJpqlQueriesReturnExpectedRows() {
        ExpenseCategory groceries = category("Groceries");
        ExpenseCategory unused = category("Pharmacy");
        ShopUnit occupiedUnit = unit(3);
        ShopUnit secondOccupiedUnit = unit(1);
        ShopUnit freeUnit = unit(2);
        Shopper firstShopper = shopper("edson.m", "Kigali", "Mugabo");
        Shopper secondShopper = shopper("aline.u", "kigali", "Uwase");
        Shopper otherShopper = shopper("jean.n", "Huye", "Niyonzima");
        Store firstStore = store("Market One", occupiedUnit, groceries);
        Store secondStore = store("Market Two", secondOccupiedUnit, groceries);

        LocalDate firstDate = LocalDate.of(2026, 10, 4);
        LocalDate secondDate = LocalDate.of(2026, 10, 12);
        LocalDate outsideDate = LocalDate.of(2026, 11, 2);
        Receipt firstUploaded = receipt(firstShopper, firstStore, firstDate, ReceiptStatus.UPLOADED);
        Receipt secondUploaded = receipt(firstShopper, firstStore, secondDate, ReceiptStatus.UPLOADED);
        receipt(firstShopper, firstStore, outsideDate, ReceiptStatus.APPROVED);
        receipt(otherShopper, secondStore, firstDate, ReceiptStatus.SCANNED);

        ExpenseEntry recentEntry = entry(firstUploaded, LocalDate.of(2026, 10, 18));
        ExpenseEntry olderEntry = entry(secondUploaded, LocalDate.of(2026, 10, 9));

        assertEquals(List.of("Mugabo", "Uwase"),
                shopperRepository.findByCityIgnoreCaseOrderByLastNameAsc("KIGALI")
                        .stream().map(Shopper::getLastName).toList());
        assertEquals(List.of(firstDate, secondDate),
                receiptRepository.findByStatusOrderByReceiptDateAsc(ReceiptStatus.UPLOADED)
                        .stream().map(Receipt::getReceiptDate).toList());
        assertEquals(3, receiptRepository.findByReceiptDateBetweenOrderByReceiptDateAsc(
                firstDate, secondDate).size());
        assertEquals(2, storeRepository.findByExpenseCategoryName("groc").size());
        assertEquals(List.of(freeUnit.getUnitNumber()),
                shopUnitRepository.findFreeShopUnits().stream().map(ShopUnit::getUnitNumber).toList());
        assertEquals(List.of(unused.getName()),
                expenseCategoryRepository.findUnusedCategories().stream().map(ExpenseCategory::getName).toList());
        assertEquals(1, shopperRepository.findShoppersByStoreId(firstStore.getId()).size());
        assertEquals(List.of(recentEntry.getId(), olderEntry.getId()),
                expenseEntryRepository.findByShopperUsername("edson.m").stream()
                        .map(ExpenseEntry::getId).toList());
        assertEquals(4, receiptRepository.countReceiptsByStatus().stream()
                .mapToLong(row -> ((Number) row[1]).longValue()).sum());
    }

    @Test
    void bulkUpdateRejectsEveryNonApprovedReceiptInOneUpdate() {
        Store store = store("Bulk Test Store", null);
        Shopper shopper = shopper("bulk.shopper", "Kigali", "Bulk");
        LocalDate date = LocalDate.of(2026, 10, 20);
        Receipt uploaded = receipt(shopper, store, date, ReceiptStatus.UPLOADED);
        Receipt scanned = receipt(shopper, store, date, ReceiptStatus.SCANNED);
        Receipt approved = receipt(shopper, store, date, ReceiptStatus.APPROVED);

        int updated = receiptRepository.rejectReceiptsForStoreOnDate(store.getId(), date);
        entityManager.flush();
        entityManager.clear();

        assertEquals(2, updated);
        assertEquals(ReceiptStatus.REJECTED, receiptRepository.findById(uploaded.getId()).orElseThrow().getStatus());
        assertEquals(ReceiptStatus.REJECTED, receiptRepository.findById(scanned.getId()).orElseThrow().getStatus());
        assertEquals(ReceiptStatus.APPROVED, receiptRepository.findById(approved.getId()).orElseThrow().getStatus());
    }

    private ExpenseCategory category(String name) {
        ExpenseCategory category = new ExpenseCategory();
        category.setName(name);
        return expenseCategoryRepository.save(category);
    }

    private ShopUnit unit(int unitNumber) {
        ShopUnit unit = new ShopUnit();
        unit.setUnitNumber(unitNumber);
        unit.setName("Unit " + unitNumber);
        return shopUnitRepository.save(unit);
    }

    private Shopper shopper(String username, String city, String lastName) {
        Shopper shopper = new Shopper();
        shopper.setUsername(username);
        shopper.setCity(city);
        shopper.setLastName(lastName);
        shopper.setFirstName(username);
        shopper.setEmail(username + "@example.test");
        return shopperRepository.save(shopper);
    }

    private Store store(String name, ShopUnit unit, ExpenseCategory... categories) {
        Store store = new Store();
        store.setStoreName(name);
        store.setOwnerName("Owner");
        store.setTinNumber(name);
        store.setShopUnit(unit);
        store.setExpenseCategories(List.of(categories));
        return storeRepository.save(store);
    }

    private Receipt receipt(Shopper shopper, Store store, LocalDate date, ReceiptStatus status) {
        Receipt receipt = new Receipt();
        receipt.setShopper(shopper);
        receipt.setStore(store);
        receipt.setReceiptDate(date);
        receipt.setStatus(status);
        return receiptRepository.save(receipt);
    }

    private ExpenseEntry entry(Receipt receipt, LocalDate date) {
        ExpenseEntry entry = new ExpenseEntry();
        entry.setReceipt(receipt);
        entry.setItemName("Item");
        entry.setAmount(100);
        entry.setEnteredDate(date);
        return expenseEntryRepository.save(entry);
    }
}
