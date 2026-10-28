-- Use only in a dedicated throwaway test database and only with lecturer approval.
-- This fixture bypasses the API solely to test the October 20, 2026 paper scenario.
BEGIN;

DO $$
BEGIN
    IF current_database() NOT LIKE '%test%' THEN
        RAISE EXCEPTION 'Refusing to insert test receipts outside a test database';
    END IF;
END
$$;

INSERT INTO receipt (id, receipt_date, status, total_amount, shopper_id, store_id)
SELECT gen_random_uuid(), DATE '2026-10-20', 'UPLOADED', 12000, shopper.id, store.id
FROM shopper
JOIN store ON store.store_name = 'Kigali Market'
WHERE shopper.username = 'edson.m'
  AND NOT EXISTS (
      SELECT 1 FROM receipt existing
      WHERE existing.store_id = store.id
        AND existing.receipt_date = DATE '2026-10-20'
        AND existing.status = 'UPLOADED'
  );

INSERT INTO receipt (id, receipt_date, status, total_amount, shopper_id, store_id)
SELECT gen_random_uuid(), DATE '2026-10-20', 'REJECTED', 9000, shopper.id, store.id
FROM shopper
JOIN store ON store.store_name = 'Kigali Market'
WHERE shopper.username = 'edson.m'
  AND NOT EXISTS (
      SELECT 1 FROM receipt existing
      WHERE existing.store_id = store.id
        AND existing.receipt_date = DATE '2026-10-20'
        AND existing.status = 'REJECTED'
  );

COMMIT;
