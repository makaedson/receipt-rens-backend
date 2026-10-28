param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body
    )

    $parameters = @{
        Method      = $Method
        Uri         = "$BaseUrl$Path"
        ErrorAction = "Stop"
    }
    if ($null -ne $Body) {
        $parameters.ContentType = "application/json"
        $parameters.Body = ConvertTo-Json -InputObject $Body -Depth 10
    }
    Invoke-RestMethod @parameters
}

$categoryNames = @("Groceries", "Electronics", "Clothing", "Pharmacy")
foreach ($name in $categoryNames) {
    Invoke-Api -Method Post -Path "/api/expense-categories/save" -Body @{ name = $name } | Out-Null
}
$categories = Invoke-Api -Method Get -Path "/api/expense-categories/all" -Body $null
$categoryByName = @{}
foreach ($category in $categories) {
    $categoryByName[$category.name] = $category
}

foreach ($number in 1..4) {
    Invoke-Api -Method Post -Path "/api/shop-units/save" -Body @{
        unitNumber = $number
        name       = "Shop unit $number"
    } | Out-Null
}
$units = Invoke-Api -Method Get -Path "/api/shop-units/all" -Body $null
$unitByNumber = @{}
foreach ($unit in $units) {
    $unitByNumber[[string]$unit.unitNumber] = $unit
}

$shopperData = @(
    @{ firstName = "Edson"; lastName = "Mugabo"; username = "edson.m"; email = "edson@example.test"; city = "Kigali" },
    @{ firstName = "Aline"; lastName = "Uwase"; username = "aline.u"; email = "aline@example.test"; city = "Kigali" },
    @{ firstName = "Jean"; lastName = "Niyonzima"; username = "jean.n"; email = "jean@example.test"; city = "Huye" },
    @{ firstName = "Grace"; lastName = "Mutesi"; username = "grace.m"; email = "grace@example.test"; city = "Musanze" },
    @{ firstName = "Eric"; lastName = "Habimana"; username = "eric.h"; email = "eric@example.test"; city = "Rubavu" }
)
foreach ($shopper in $shopperData) {
    Invoke-Api -Method Post -Path "/api/shoppers/save" -Body $shopper | Out-Null
}
$shoppers = Invoke-Api -Method Get -Path "/api/shoppers/all" -Body $null
$shopperByUsername = @{}
foreach ($shopper in $shoppers) {
    $shopperByUsername[$shopper.username] = $shopper
}

$storeData = @(
    @{
        storeName = "Kigali Market"; ownerName = "Marie"; tinNumber = "TIN-001"
        shopUnit = @{ id = $unitByNumber["1"].id }
        expenseCategories = @(@{ id = $categoryByName["Groceries"].id }, @{ id = $categoryByName["Clothing"].id })
    },
    @{
        storeName = "City Electronics"; ownerName = "Claude"; tinNumber = "TIN-002"
        shopUnit = @{ id = $unitByNumber["2"].id }
        expenseCategories = @(@{ id = $categoryByName["Electronics"].id })
    },
    @{
        storeName = "Family Store"; ownerName = "Diane"; tinNumber = "TIN-003"
        shopUnit = @{ id = $unitByNumber["3"].id }
        expenseCategories = @(@{ id = $categoryByName["Groceries"].id })
    }
)
foreach ($store in $storeData) {
    Invoke-Api -Method Post -Path "/api/stores/save" -Body $store | Out-Null
}
$stores = Invoke-Api -Method Get -Path "/api/stores/all" -Body $null
$storeByName = @{}
foreach ($store in $stores) {
    $storeByName[$store.storeName] = $store
}

$firstDate = (Get-Date).Date.AddDays(1)
$receiptPlan = @(
    @{ store = "Kigali Market"; shopper = "edson.m"; status = "UPLOADED"; amount = 12600 },
    @{ store = "City Electronics"; shopper = "edson.m"; status = "SCANNED"; amount = 54000 },
    @{ store = "Family Store"; shopper = "edson.m"; status = "APPROVED"; amount = 8200 },
    @{ store = "Kigali Market"; shopper = "aline.u"; status = "REJECTED"; amount = 9500 },
    @{ store = "City Electronics"; shopper = "aline.u"; status = "APPROVED"; amount = 37500 },
    @{ store = "Family Store"; shopper = "jean.n"; status = "UPLOADED"; amount = 4600 },
    @{ store = "Kigali Market"; shopper = "grace.m"; status = "SCANNED"; amount = 13300 },
    @{ store = "City Electronics"; shopper = "eric.h"; status = "APPROVED"; amount = 28900 },
    @{ store = "Family Store"; shopper = "edson.m"; status = "REJECTED"; amount = 7100 },
    @{ store = "Kigali Market"; shopper = "edson.m"; status = "UPLOADED"; amount = 15800 }
)

for ($index = 0; $index -lt $receiptPlan.Count; $index++) {
    $plan = $receiptPlan[$index]
    $date = $firstDate.AddDays($index).ToString("yyyy-MM-dd")
    $store = $storeByName[$plan.store]
    Invoke-Api -Method Post -Path "/api/receipts/save" -Body @{
        receiptDate = $date
        totalAmount = $plan.amount
        status      = "APPROVED"
        store       = @{ id = $store.id }
        shopper     = @{ id = $shopperByUsername[$plan.shopper].id }
    } | Out-Null

    $receipts = Invoke-Api -Method Get -Path "/api/receipts/all" -Body $null
    $receipt = $receipts | Where-Object {
        $_.receiptDate -eq $date -and $_.store.id -eq $store.id
    } | Select-Object -First 1
    if ($null -eq $receipt) {
        throw "Could not find the receipt created for $date at $($plan.store)"
    }

    if ($plan.status -eq "SCANNED" -or $plan.status -eq "APPROVED") {
        Invoke-Api -Method Patch -Path "/api/receipts/$($receipt.id)/status?status=SCANNED" -Body $null | Out-Null
    }
    if ($plan.status -eq "APPROVED") {
        Invoke-Api -Method Patch -Path "/api/receipts/$($receipt.id)/status?status=APPROVED" -Body $null | Out-Null
    } elseif ($plan.status -eq "REJECTED") {
        Invoke-Api -Method Patch -Path "/api/receipts/$($receipt.id)/status?status=REJECTED" -Body $null | Out-Null
    }

    if ($index -eq 2) {
        Invoke-Api -Method Post -Path "/api/expense-entries/save?receiptId=$($receipt.id)" -Body @{
            itemName = "Rice 5kg"
            note     = "Seed shopping expense"
            amount   = 7500
        } | Out-Null
    }
}

Write-Output "Seeded $($categories.Count) categories, $($units.Count) shop units, $($stores.Count) stores, $($shoppers.Count) shoppers, and $($receiptPlan.Count) receipts."
Write-Output "The October 20, 2026 duplicate-receipt sample is intentionally omitted: that date is in the past and the safe-save endpoint correctly rejects it."
