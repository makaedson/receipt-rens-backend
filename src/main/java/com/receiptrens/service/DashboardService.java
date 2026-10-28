package com.receiptrens.service;

import com.receiptrens.model.ReceiptStatus;
import com.receiptrens.repository.ExpenseEntryRepository;
import com.receiptrens.repository.ShopUnitRepository;
import com.receiptrens.repository.ShopperRepository;
import com.receiptrens.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {
    private final ShopperRepository shopperRepository;
    private final StoreRepository storeRepository;
    private final ShopUnitRepository shopUnitRepository;
    private final ReceiptService receiptService;
    private final ExpenseEntryRepository expenseEntryRepository;

    public DashboardService(
            ShopperRepository shopperRepository,
            StoreRepository storeRepository,
            ShopUnitRepository shopUnitRepository,
            ReceiptService receiptService,
            ExpenseEntryRepository expenseEntryRepository
    ) {
        this.shopperRepository = shopperRepository;
        this.storeRepository = storeRepository;
        this.shopUnitRepository = shopUnitRepository;
        this.receiptService = receiptService;
        this.expenseEntryRepository = expenseEntryRepository;
    }

    public Map<String, Long> getSummary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("shoppers", shopperRepository.count());
        summary.put("stores", storeRepository.count());
        summary.put("freeShopUnits", (long) shopUnitRepository.findFreeShopUnits().size());
        summary.put("receiptsToday", receiptService.countByReceiptDate(LocalDate.now()));
        summary.put("uploaded", receiptService.countByStatus(ReceiptStatus.UPLOADED));
        summary.put("expenseEntries", expenseEntryRepository.count());
        return summary;
    }
}
