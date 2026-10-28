# ReceiptRens

ReceiptRens is a Java REST API for tracking shoppers, stores, shopping receipts, and expense entries. It is a backend-only project for the Web Technology and Internet mid-term assignment.

**Student:** MUGABO Edson  
**Student ID:** 27502  
**Project:** ReceiptRens

## Technology

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Postman

## Project structure

```text
src/main/java/com/receiptrens/
  controller/    REST endpoints
  model/         JPA entities and ReceiptStatus
  repository/    Spring Data derived methods and JPQL
  service/       CRUD and business rules
src/main/resources/application.properties
src/test/java/   business-rule unit tests
scripts/         reproducible API seed script
receipt-rens-midterm.postman_collection.json
```

## Database setup

Install PostgreSQL and create an empty database named `receipt_rens`. For example, from a PostgreSQL shell:

```sql
CREATE DATABASE receipt_rens;
```

The application reads its database settings from environment variables:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/receipt_rens` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | empty |

Set `DB_PASSWORD` (and, if needed, the other values) in your local environment. Do not put database credentials in source control. Hibernate is configured with `ddl-auto=update`; SQL logging is enabled as required by the assignment.

## Run the application

Install Java 21, start PostgreSQL, and create the database. The included Maven wrapper downloads the required Maven version on its first run. From this directory run:

```powershell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-password"
.\mvnw.cmd spring-boot:run
```

The API listens at `http://localhost:8080`.

## Endpoint overview

All endpoints use the `/api` prefix.

| Resource | CRUD endpoints |
|---|---|
| Expense categories | `/expense-categories/save`, `/all`, `/{id}`, `/update/{id}`, `/delete/{id}` |
| Shop units | `/shop-units/save`, `/all`, `/{id}`, `/update/{id}`, `/delete/{id}` |
| Stores | `/stores/save`, `/all`, `/{id}`, `/update/{id}`, `/delete/{id}` |
| Shoppers | `/shoppers/save`, `/all`, `/{id}`, `/update/{id}`, `/delete/{id}` |
| Receipts | `/receipts/save`, `/all`, `/{id}`, `/update/{id}`, `/delete/{id}` |

Assignment queries and business rules:

- `GET /api/shoppers/by-city?city=kigali`
- `GET /api/receipts/by-status?status=UPLOADED`
- `GET /api/receipts/between?start=2026-10-01&end=2026-10-31`
- `GET /api/stores/by-expense-category?name=groc`
- `GET /api/shop-units/free`
- `GET /api/expense-categories/unused`
- `GET /api/shoppers/of-store/{storeId}`
- `PATCH /api/receipts/{id}/status?status=SCANNED`
- `PUT /api/stores/{storeId}/expense-categories/{expenseCategoryId}`
- `PATCH /api/stores/{storeId}/shop-unit?unitNumber=4`
- `POST /api/expense-entries/save?receiptId={receiptId}`
- `GET /api/expense-entries/shopper/{username}`
- `DELETE /api/expense-entries/delete/{id}`
- `GET /api/receipts/stats/by-status`
- `PATCH /api/receipts/cancel-day?storeId={storeId}&date=2026-10-20`
- `GET /api/dashboard/summary` (bonus)
- `GET /api/receipts/page?page=0&size=5&sort=receiptDate,desc` (bonus)

## Seed data and the October date conflict

Run the API first, then run the seed script against a **fresh database**:

```powershell
.\scripts\seed-data.ps1
```

It creates four categories, four shop units, three stores, five shoppers, ten receipts, all four receipt statuses, and one expense entry through the application's save endpoints. It picks future dates so the receipt endpoint's past-date rule remains active.

The paper also asks for receipts dated October 20, 2026, but the safe-save rule rejects dates before today. On October 28, 2026 those two requirements conflict: the application correctly rejects an October 20 save. The API and seed script do not weaken that production rule or inject database rows. If the lecturer needs the October 20 bulk-cancellation example, ask for approval to prepare test data through a separate test-only database setup; do not use that workaround in production.

After explicit lecturer approval, `scripts/lecturer-approved-october-fixture.sql` can add the two October 20 examples to a dedicated database whose name contains `test`. It is never run by the application or regular seed script and refuses to run against a database without `test` in its name.

## Postman

Import `receipt-rens-midterm.postman_collection.json` in Postman. Set `baseUrl` to `http://localhost:8080`, and fill the ID variables after creating the corresponding records. Run the A2 requests first, then the query and business-rule examples. The collection includes both successful requests and expected error cases.

The API uses direct entity request and response bodies, as required by the assignment. For related records, provide their existing UUIDs in the nested `shopUnit`, `shopper`, `store`, and `expenseCategories` objects.

## Tests

Run the automated tests with:

```powershell
.\mvnw.cmd test
```

The tests cover receipt date/duplicate checks, receipt status transitions, preserving status during general updates, protecting receipts with expense entries, bulk-cancellation counts, approved-only expense-entry creation, and repository query behavior. Repository tests use an in-memory H2 database; a running PostgreSQL instance is still needed to verify the configured PostgreSQL connection.
