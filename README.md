# Kitchen Inventory Management System
## VTU 4th Sem — Java + DBMS Combined Project

---

## 📁 Files in This Project

| File | Purpose |
|------|---------|
| `kitchen_inventory_db.sql` | MySQL schema + sample data |
| `DBConnection.java` | JDBC utility class (Singleton) |
| `KitchenInventoryApp.java` | Main Swing GUI application |

---

## 🗄 Step 1: Set Up the MySQL Database

1. Open **MySQL Workbench** or the MySQL command line.
2. Run the SQL file:
   ```sql
   source /path/to/kitchen_inventory_db.sql;
   ```
   OR copy-paste its contents into Workbench and execute.

3. Verify tables were created:
   ```sql
   USE kitchen_inventory;
   SHOW TABLES;
   ```
   You should see: `suppliers`, `ingredients`, `purchase_orders`, `usage_log`

---

## ☕ Step 2: Configure JDBC in DBConnection.java

Open `DBConnection.java` and update:
```java
private static final String URL      = "jdbc:mysql://localhost:3306/kitchen_inventory";
private static final String USER     = "root";
private static final String PASSWORD = "your_mysql_password_here";
```

---

## 📦 Step 3: Add MySQL JDBC Driver

Download **mysql-connector-j** (e.g., `mysql-connector-j-8.x.x.jar`) from:
👉 https://dev.mysql.com/downloads/connector/j/

**In VS Code / IntelliJ**: Add the JAR to your project's classpath.

**Command line compile:**
```bash
javac -cp .;mysql-connector-j-8.x.x.jar DBConnection.java KitchenInventoryApp.java
```

---

## ▶ Step 4: Run the Application

```bash
java -cp .;mysql-connector-j-8.x.x.jar KitchenInventoryApp
```

*(On Linux/Mac use `:` instead of `;` in the classpath)*

---

## 🖥 Features

### Tab 1 — 🥦 Ingredients
- View all ingredients in a table
- Add new ingredients (name, category, unit, quantity, price, supplier, expiry)
- Delete ingredients
- Data stored in `ingredients` table

### Tab 2 — 🚚 Suppliers
- View all suppliers
- Add new supplier (name, contact, phone, email, address)
- Data stored in `suppliers` table

### Tab 3 — 🛒 Purchase Orders
- View all purchase orders with item + supplier names
- Place new orders
- Mark orders as "Received"
- Total cost auto-calculated in MySQL
- Data stored in `purchase_orders` table

### Tab 4 — 📋 Usage / Consumption Log
- Log ingredient usage (quantity, date, purpose, chef name)
- **Automatically deducts from ingredient stock**
- Data stored in `usage_log` table

### Tab 5 — ⚠ Low Stock Alerts
- Shows all ingredients where current quantity ≤ reorder level
- Uses a MySQL VIEW (`low_stock_alert`)

---

## 🗃 Database Tables

### `suppliers`
| Column | Type |
|--------|------|
| supplier_id (PK) | INT AUTO_INCREMENT |
| supplier_name | VARCHAR(100) |
| contact_name | VARCHAR(100) |
| phone | VARCHAR(15) |
| email | VARCHAR(100) |
| address | TEXT |

### `ingredients`
| Column | Type |
|--------|------|
| ingredient_id (PK) | INT AUTO_INCREMENT |
| item_name | VARCHAR(100) |
| category | VARCHAR(50) |
| unit | VARCHAR(20) |
| quantity | DOUBLE |
| reorder_level | DOUBLE |
| unit_price | DECIMAL(10,2) |
| supplier_id (FK) | INT → suppliers |
| expiry_date | DATE |

### `purchase_orders`
| Column | Type |
|--------|------|
| order_id (PK) | INT AUTO_INCREMENT |
| ingredient_id (FK) | INT → ingredients |
| supplier_id (FK) | INT → suppliers |
| order_date | DATE |
| quantity_ordered | DOUBLE |
| unit_price | DECIMAL(10,2) |
| total_cost | GENERATED (stored) |
| status | ENUM(Pending/Received/Cancelled) |

### `usage_log`
| Column | Type |
|--------|------|
| log_id (PK) | INT AUTO_INCREMENT |
| ingredient_id (FK) | INT → ingredients |
| used_quantity | DOUBLE |
| used_date | DATE |
| purpose | VARCHAR(150) |
| recorded_by | VARCHAR(100) |

---

## 🧪 Java Concepts Used (VTU Syllabus)

| Concept | Where Used |
|---------|-----------|
| **JDBC** | `DBConnection.java`, all DB operations in `KitchenInventoryApp.java` |
| **Swing GUI** | `JFrame`, `JTable`, `JButton`, `JTextField`, `JTabbedPane`, `JScrollPane` |
| **Exception Handling** | `try-catch` blocks around all DB operations |
| **OOP** | Separate `DBConnection` class (Singleton), encapsulated methods per tab |
| **PreparedStatement** | Prevents SQL injection in all INSERT queries |

---

## 💡 Tips for Viva

- **Why JDBC?** — Standard Java API to connect to relational databases.
- **Why PreparedStatement?** — Prevents SQL injection and is more efficient than Statement.
- **What is a VIEW?** — A saved SQL query. We use it for `ingredient_details`, `order_summary`, `low_stock_alert`, `usage_details`.
- **What is a Foreign Key?** — Links `ingredient_id` in `purchase_orders` to `ingredients` table, ensuring referential integrity.
- **Generated column?** — `total_cost` in `purchase_orders` is automatically computed as `quantity_ordered * unit_price` by MySQL.
