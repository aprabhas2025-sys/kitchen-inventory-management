-- ============================================
-- KITCHEN INVENTORY MANAGEMENT SYSTEM
-- MySQL Database Schema
-- VTU 4th Sem - DBMS Project
-- ============================================

CREATE DATABASE IF NOT EXISTS kitchen_inventory;
USE kitchen_inventory;

-- ============================================
-- TABLE 1: Suppliers
-- ============================================
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id   INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(100) NOT NULL,
    contact_name  VARCHAR(100),
    phone         VARCHAR(15),
    email         VARCHAR(100),
    address       TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABLE 2: Ingredients / Items
-- ============================================
CREATE TABLE IF NOT EXISTS ingredients (
    ingredient_id   INT AUTO_INCREMENT PRIMARY KEY,
    item_name       VARCHAR(100) NOT NULL,
    category        VARCHAR(50),          -- e.g. Vegetable, Spice, Dairy
    unit            VARCHAR(20) NOT NULL, -- e.g. kg, litre, pieces
    quantity        DOUBLE NOT NULL DEFAULT 0,
    reorder_level   DOUBLE NOT NULL DEFAULT 5,
    unit_price      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    supplier_id     INT,
    expiry_date     DATE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON DELETE SET NULL
);

-- ============================================
-- TABLE 3: Purchase Orders
-- ============================================
CREATE TABLE IF NOT EXISTS purchase_orders (
    order_id        INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id   INT NOT NULL,
    supplier_id     INT NOT NULL,
    order_date      DATE NOT NULL,
    quantity_ordered DOUBLE NOT NULL,
    unit_price      DECIMAL(10,2) NOT NULL,
    total_cost      DECIMAL(10,2) GENERATED ALWAYS AS (quantity_ordered * unit_price) STORED,
    status          ENUM('Pending','Received','Cancelled') DEFAULT 'Pending',
    notes           TEXT,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id)
        ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON DELETE CASCADE
);

-- ============================================
-- TABLE 4: Usage / Consumption Log
-- ============================================
CREATE TABLE IF NOT EXISTS usage_log (
    log_id          INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id   INT NOT NULL,
    used_quantity   DOUBLE NOT NULL,
    used_date       DATE NOT NULL,
    purpose         VARCHAR(150),         -- e.g. "Dinner service", "Breakfast prep"
    recorded_by     VARCHAR(100),
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id)
        ON DELETE CASCADE
);

-- ============================================
-- SAMPLE DATA
-- ============================================

INSERT INTO suppliers (supplier_name, contact_name, phone, email, address) VALUES
('FreshFarm Supplies',  'Ravi Kumar',   '9876543210', 'ravi@freshfarm.com',   'Bengaluru, KA'),
('SpiceWorld Co.',      'Meena Rao',    '9845612378', 'meena@spiceworld.com', 'Mysuru, KA'),
('DairyBest Ltd.',      'Arjun Sharma', '9900112233', 'arjun@dairybest.com',  'Mangaluru, KA');

INSERT INTO ingredients (item_name, category, unit, quantity, reorder_level, unit_price, supplier_id, expiry_date) VALUES
('Tomatoes',      'Vegetable', 'kg',     20.0, 5.0,  25.00, 1, '2026-05-30'),
('Onions',        'Vegetable', 'kg',     15.0, 5.0,  20.00, 1, '2026-06-10'),
('Turmeric',      'Spice',     'grams', 500.0, 100.0, 5.00, 2, '2027-01-01'),
('Red Chilli',    'Spice',     'grams', 300.0, 100.0, 8.00, 2, '2027-03-01'),
('Milk',          'Dairy',     'litre',  10.0, 3.0,  55.00, 3, '2026-05-22'),
('Butter',        'Dairy',     'kg',      5.0, 1.0, 480.00, 3, '2026-07-01'),
('Rice',          'Grain',     'kg',     40.0, 10.0, 50.00, 1, '2027-12-01'),
('Cooking Oil',   'Oil',       'litre',   8.0, 2.0, 150.00, 2, '2026-11-01');

INSERT INTO purchase_orders (ingredient_id, supplier_id, order_date, quantity_ordered, unit_price, status, notes) VALUES
(1, 1, '2026-05-01', 30.0, 25.00, 'Received', 'Monthly stock'),
(5, 3, '2026-05-10', 20.0, 55.00, 'Received', 'Weekly dairy order'),
(3, 2, '2026-05-12', 1000.0, 5.00, 'Pending',  'Bulk spice purchase'),
(7, 1, '2026-05-15', 50.0, 50.00, 'Received', 'Rice restock');

INSERT INTO usage_log (ingredient_id, used_quantity, used_date, purpose, recorded_by) VALUES
(1, 5.0,   '2026-05-15', 'Lunch service',    'Chef Anand'),
(5, 2.0,   '2026-05-15', 'Breakfast prep',   'Chef Priya'),
(7, 10.0,  '2026-05-16', 'Dinner service',   'Chef Anand'),
(3, 50.0,  '2026-05-16', 'Curry preparation','Chef Priya'),
(6, 0.5,   '2026-05-17', 'Bakery section',   'Chef Ritu');

-- ============================================
-- USEFUL VIEWS
-- ============================================

-- View: Low stock alert
CREATE OR REPLACE VIEW low_stock_alert AS
SELECT item_name, category, unit, quantity, reorder_level
FROM ingredients
WHERE quantity <= reorder_level;

-- View: Full ingredient details with supplier name
CREATE OR REPLACE VIEW ingredient_details AS
SELECT i.ingredient_id, i.item_name, i.category, i.unit,
       i.quantity, i.reorder_level, i.unit_price, i.expiry_date,
       s.supplier_name
FROM ingredients i
LEFT JOIN suppliers s ON i.supplier_id = s.supplier_id;

-- View: Purchase order summary
CREATE OR REPLACE VIEW order_summary AS
SELECT po.order_id, i.item_name, s.supplier_name,
       po.order_date, po.quantity_ordered, po.unit_price,
       po.total_cost, po.status
FROM purchase_orders po
JOIN ingredients i ON po.ingredient_id = i.ingredient_id
JOIN suppliers s   ON po.supplier_id   = s.supplier_id;

-- View: Usage log with ingredient names
CREATE OR REPLACE VIEW usage_details AS
SELECT ul.log_id, i.item_name, i.unit,
       ul.used_quantity, ul.used_date, ul.purpose, ul.recorded_by
FROM usage_log ul
JOIN ingredients i ON ul.ingredient_id = i.ingredient_id;
