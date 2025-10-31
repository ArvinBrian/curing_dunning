DELETE FROM services WHERE customer_id = 1;

INSERT INTO services
(id, due_amount, next_due_date, service_name, status, customer_id, plan_type, billing_cycle_days,
 last_payment_date, next_payment_date, call_usage_minutes, data_usage_gb, sms_count)
VALUES
-- 1️⃣ Bill due in 2 days
(1, 499.00, NULL, 'Mobile Basic', 'ACTIVE', 1, 'POSTPAID', 30, '2025-09-29', '2025-10-31', 120, 2, 20),

-- 2️⃣ Bill due today
(2, 599.00, NULL, 'Mobile Gold', 'ACTIVE', 1, 'POSTPAID', 30, '2025-09-29', '2025-10-29', 150, 4, 30),

-- 3️⃣ Bill overdue by 4 days
(3, 699.00, NULL, 'Mobile Platinum', 'ACTIVE', 1, 'POSTPAID', 30, '2025-09-25', '2025-10-25', 200, 6, 40),

-- 4️⃣ Bill overdue by 10 days
(4, 799.00, NULL, 'Mobile Ultra', 'ACTIVE', 1, 'POSTPAID', 30, '2025-09-19', '2025-10-19', 250, 8, 50),

-- 5️⃣ Blocked account (no billing)
(5, 899.00, NULL, 'Mobile Legacy', 'BLOCKED', 1, 'POSTPAID', 30, '2025-09-10', '2025-10-10', 300, 10, 60),

-- 6️⃣ Active prepaid service
(6, 199.00, NULL, 'Data Booster', 'ACTIVE', 1, 'PREPAID', 30, '2025-10-20', '2025-11-20', 0, 12, 0),

-- 7️⃣ Prepaid expiring tomorrow
(7, 249.00, NULL, 'Prepaid 28-Day', 'ACTIVE', 1, 'PREPAID', 28, '2025-09-29', '2025-10-30', 0, 10, 0),

-- 8️⃣ Suspended prepaid
(8, 299.00, NULL, 'Voice Saver', 'SUSPENDED', 1, 'PREPAID', 30, '2025-09-25', '2025-10-25', 0, 0, 0),

-- 9️⃣ Fresh postpaid cycle
(9, 649.00, NULL, 'Mobile Premium', 'ACTIVE', 1, 'POSTPAID', 30, '2025-10-29', '2025-11-28', 50, 1, 10),

-- 🔟 Future cycle far ahead
(10, 749.00, NULL, 'Mobile Future', 'ACTIVE', 1, 'POSTPAID', 30, '2025-10-01', '2025-12-01', 100, 3, 25);
