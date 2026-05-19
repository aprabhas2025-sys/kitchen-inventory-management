// ============================================
// KitchenInventoryApp.java
// Main Swing GUI Application
// Kitchen Inventory Management System
// VTU 4th Sem - Java + DBMS Project
// ============================================

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class KitchenInventoryApp extends JFrame {

    // ---- Color Palette ----
    private static final Color BG_DARK     = new Color(18, 18, 24);
    private static final Color BG_CARD     = new Color(28, 30, 40);
    private static final Color ACCENT      = new Color(255, 140, 50);
    private static final Color ACCENT2     = new Color(80, 200, 160);
    private static final Color TEXT_LIGHT  = new Color(235, 235, 240);
    private static final Color TEXT_DIM    = new Color(140, 140, 160);
    private static final Color TABLE_HDR   = new Color(40, 42, 58);
    private static final Color ROW_ALT     = new Color(32, 34, 48);

    private JTabbedPane tabbedPane;
    private Connection conn;

    // =====================================================
    //  CONSTRUCTOR
    // =====================================================
    public KitchenInventoryApp() {
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Database connection failed!\n" + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Kitchen Inventory Management System");
        setSize(1100, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_DARK);
        tabbedPane.setForeground(TEXT_LIGHT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TabbedPane.selected", BG_CARD);

        tabbedPane.addTab("🥦  Ingredients",      buildIngredientsPanel());
        tabbedPane.addTab("🚚  Suppliers",         buildSuppliersPanel());
        tabbedPane.addTab("🛒  Purchase Orders",   buildOrdersPanel());
        tabbedPane.addTab("📋  Usage Log",         buildUsagePanel());
        tabbedPane.addTab("⚠️  Low Stock Alerts",  buildLowStockPanel());

        add(tabbedPane, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // =====================================================
    //  HEADER
    // =====================================================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_CARD);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("🍽  Kitchen Inventory Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACCENT);

        JLabel subtitle = new JLabel("VTU 4th Sem | Java + MySQL Project");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_DIM);

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setBackground(BG_CARD);
        left.add(title);
        left.add(subtitle);

        JLabel dbStatus = new JLabel("● Connected to MySQL");
        dbStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dbStatus.setForeground(ACCENT2);

        header.add(left, BorderLayout.WEST);
        header.add(dbStatus, BorderLayout.EAST);
        return header;
    }

    // =====================================================
    //  FOOTER
    // =====================================================
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(BG_CARD);
        footer.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        JLabel lbl = new JLabel("Kitchen Inventory System  |  Data stored in MySQL  |  © 2026");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_DIM);
        footer.add(lbl);
        return footer;
    }

    // =====================================================
    //  HELPER: Styled Table
    // =====================================================
    private JScrollPane makeTablePane(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_LIGHT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(80, 200, 160, 80));
        table.setSelectionForeground(Color.WHITE);

        // Alternating row color
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? new Color(80, 200, 160, 80)
                                  : (row % 2 == 0 ? BG_CARD : ROW_ALT));
                setForeground(TEXT_LIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HDR);
        header.setForeground(ACCENT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
            .setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createLineBorder(new Color(50, 54, 70)));
        sp.getViewport().setBackground(BG_CARD);
        return sp;
    }

    // =====================================================
    //  HELPER: Styled Button
    // =====================================================
    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    // =====================================================
    //  HELPER: Styled Label
    // =====================================================
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_LIGHT);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    // =====================================================
    //  HELPER: Styled TextField
    // =====================================================
    private JTextField makeField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(new Color(38, 40, 55));
        tf.setForeground(TEXT_LIGHT);
        tf.setCaretColor(TEXT_LIGHT);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 64, 85)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return tf;
    }

    // =====================================================
    //  TAB 1 — INGREDIENTS
    // =====================================================
    private JPanel buildIngredientsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // ----- Table -----
        String[] cols = {"ID", "Item Name", "Category", "Unit", "Quantity",
                         "Reorder Level", "Unit Price (₹)", "Supplier", "Expiry Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        loadIngredients(model);

        // ----- Form -----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 54, 70)),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 8, 5, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfName     = makeField(12);
        JTextField tfCategory = makeField(10);
        JTextField tfUnit     = makeField(6);
        JTextField tfQty      = makeField(6);
        JTextField tfReorder  = makeField(6);
        JTextField tfPrice    = makeField(8);
        JTextField tfSupplier = makeField(6);
        JTextField tfExpiry   = makeField(10);

        Object[][] formFields = {
            {"Item Name *", tfName}, {"Category", tfCategory},
            {"Unit *",      tfUnit}, {"Quantity *", tfQty},
            {"Reorder Level", tfReorder}, {"Unit Price ₹ *", tfPrice},
            {"Supplier ID", tfSupplier}, {"Expiry (YYYY-MM-DD)", tfExpiry}
        };

        int row = 0, col = 0;
        for (Object[] ff : formFields) {
            gc.gridx = col * 2; gc.gridy = row;
            form.add(makeLabel((String) ff[0]), gc);
            gc.gridx = col * 2 + 1;
            form.add((Component) ff[1], gc);
            col++;
            if (col == 4) { col = 0; row++; }
        }

        JButton btnAdd     = makeButton("+ Add Item", ACCENT);
        JButton btnRefresh = makeButton("↻ Refresh", new Color(60, 100, 180));
        JButton btnDelete  = makeButton("✕ Delete Selected", new Color(200, 60, 60));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setBackground(BG_CARD);
        btns.add(btnAdd); btns.add(btnRefresh); btns.add(btnDelete);

        gc.gridx = 0; gc.gridy = row + 1; gc.gridwidth = 8;
        form.add(btns, gc);

        // ----- Actions -----
        btnAdd.addActionListener(e -> {
            try {
                String sql = "INSERT INTO ingredients (item_name,category,unit,quantity," +
                             "reorder_level,unit_price,supplier_id,expiry_date) VALUES (?,?,?,?,?,?,?,?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, tfName.getText().trim());
                ps.setString(2, tfCategory.getText().trim());
                ps.setString(3, tfUnit.getText().trim());
                ps.setDouble(4, Double.parseDouble(tfQty.getText().trim()));
                ps.setDouble(5, Double.parseDouble(tfReorder.getText().trim()));
                ps.setDouble(6, Double.parseDouble(tfPrice.getText().trim()));
                String sid = tfSupplier.getText().trim();
                if (sid.isEmpty()) ps.setNull(7, Types.INTEGER);
                else ps.setInt(7, Integer.parseInt(sid));
                String exp = tfExpiry.getText().trim();
                if (exp.isEmpty()) ps.setNull(8, Types.DATE);
                else ps.setDate(8, Date.valueOf(exp));
                ps.executeUpdate();
                showSuccess("Ingredient added successfully!");
                loadIngredients(model);
                for (JTextField tf : new JTextField[]{tfName, tfCategory, tfUnit,
                        tfQty, tfReorder, tfPrice, tfSupplier, tfExpiry}) tf.setText("");
            } catch (Exception ex) {
                showError("Error adding ingredient:\n" + ex.getMessage());
            }
        });

        btnRefresh.addActionListener(e -> loadIngredients(model));

        btnDelete.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) { showError("Select a row to delete."); return; }
            int id = (int) model.getValueAt(sel, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete ingredient ID " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    conn.prepareStatement("DELETE FROM ingredients WHERE ingredient_id=" + id)
                        .executeUpdate();
                    showSuccess("Deleted.");
                    loadIngredients(model);
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(makeTablePane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadIngredients(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            ResultSet rs = conn.createStatement()
                .executeQuery("SELECT * FROM ingredient_details ORDER BY ingredient_id");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ingredient_id"),
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getString("unit"),
                    rs.getDouble("quantity"),
                    rs.getDouble("reorder_level"),
                    "₹" + rs.getDouble("unit_price"),
                    rs.getString("supplier_name"),
                    rs.getDate("expiry_date")
                });
            }
        } catch (SQLException e) { showError("Load error: " + e.getMessage()); }
    }

    // =====================================================
    //  TAB 2 — SUPPLIERS
    // =====================================================
    private JPanel buildSuppliersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        String[] cols = {"ID", "Supplier Name", "Contact Person", "Phone", "Email", "Address"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        loadSuppliers(model);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 54, 70)),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 8, 5, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfName    = makeField(14);
        JTextField tfContact = makeField(12);
        JTextField tfPhone   = makeField(10);
        JTextField tfEmail   = makeField(14);
        JTextField tfAddress = makeField(20);

        Object[][] ff = {
            {"Supplier Name *", tfName, "Contact Person", tfContact},
            {"Phone",           tfPhone, "Email",          tfEmail}
        };

        int row = 0;
        for (Object[] f : ff) {
            for (int c = 0; c < 4; c += 2) {
                gc.gridx = c; gc.gridy = row;
                form.add(makeLabel((String) f[c]), gc);
                gc.gridx = c + 1;
                form.add((Component) f[c + 1], gc);
            }
            row++;
        }
        gc.gridx = 0; gc.gridy = row;
        form.add(makeLabel("Address"), gc);
        gc.gridx = 1; gc.gridwidth = 7;
        form.add(tfAddress, gc);
        gc.gridwidth = 1;

        JButton btnAdd     = makeButton("+ Add Supplier", ACCENT);
        JButton btnRefresh = makeButton("↻ Refresh", new Color(60, 100, 180));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setBackground(BG_CARD);
        btns.add(btnAdd); btns.add(btnRefresh);
        gc.gridx = 0; gc.gridy = row + 1; gc.gridwidth = 8;
        form.add(btns, gc);

        btnAdd.addActionListener(e -> {
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO suppliers (supplier_name,contact_name,phone,email,address) VALUES (?,?,?,?,?)");
                ps.setString(1, tfName.getText().trim());
                ps.setString(2, tfContact.getText().trim());
                ps.setString(3, tfPhone.getText().trim());
                ps.setString(4, tfEmail.getText().trim());
                ps.setString(5, tfAddress.getText().trim());
                ps.executeUpdate();
                showSuccess("Supplier added!");
                loadSuppliers(model);
                for (JTextField tf : new JTextField[]{tfName, tfContact, tfPhone, tfEmail, tfAddress})
                    tf.setText("");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        btnRefresh.addActionListener(e -> loadSuppliers(model));

        panel.add(form, BorderLayout.NORTH);
        panel.add(makeTablePane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadSuppliers(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            ResultSet rs = conn.createStatement()
                .executeQuery("SELECT * FROM suppliers ORDER BY supplier_id");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("supplier_id"),
                    rs.getString("supplier_name"),
                    rs.getString("contact_name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address")
                });
            }
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    // =====================================================
    //  TAB 3 — PURCHASE ORDERS
    // =====================================================
    private JPanel buildOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        String[] cols = {"Order ID", "Item", "Supplier", "Order Date",
                         "Qty Ordered", "Unit Price ₹", "Total Cost ₹", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        loadOrders(model);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 54, 70)),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 8, 5, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfIngId   = makeField(6);
        JTextField tfSupId   = makeField(6);
        JTextField tfDate    = makeField(12);
        JTextField tfQty     = makeField(8);
        JTextField tfPrice   = makeField(8);
        String[] statuses    = {"Pending", "Received", "Cancelled"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setBackground(new Color(38, 40, 55));
        cbStatus.setForeground(TEXT_LIGHT);
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        Object[][] ff = {
            {"Ingredient ID *", tfIngId},  {"Supplier ID *", tfSupId},
            {"Order Date *\n(YYYY-MM-DD)", tfDate}, {"Qty Ordered *", tfQty},
            {"Unit Price ₹ *", tfPrice},   {"Status", cbStatus}
        };

        int r = 0, c = 0;
        for (Object[] f : ff) {
            gc.gridx = c * 2; gc.gridy = r;
            form.add(makeLabel((String) f[0]), gc);
            gc.gridx = c * 2 + 1;
            form.add((Component) f[1], gc);
            c++;
            if (c == 3) { c = 0; r++; }
        }

        JButton btnAdd     = makeButton("+ Place Order", ACCENT);
        JButton btnRefresh = makeButton("↻ Refresh", new Color(60, 100, 180));
        JButton btnReceive = makeButton("✔ Mark Received", ACCENT2);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setBackground(BG_CARD);
        btns.add(btnAdd); btns.add(btnRefresh); btns.add(btnReceive);
        gc.gridx = 0; gc.gridy = r + 1; gc.gridwidth = 6;
        form.add(btns, gc);

        btnAdd.addActionListener(e -> {
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO purchase_orders (ingredient_id,supplier_id,order_date," +
                    "quantity_ordered,unit_price,status) VALUES (?,?,?,?,?,?)");
                ps.setInt(1, Integer.parseInt(tfIngId.getText().trim()));
                ps.setInt(2, Integer.parseInt(tfSupId.getText().trim()));
                ps.setDate(3, Date.valueOf(tfDate.getText().trim()));
                ps.setDouble(4, Double.parseDouble(tfQty.getText().trim()));
                ps.setDouble(5, Double.parseDouble(tfPrice.getText().trim()));
                ps.setString(6, (String) cbStatus.getSelectedItem());
                ps.executeUpdate();
                showSuccess("Purchase order placed!");
                loadOrders(model);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        btnRefresh.addActionListener(e -> loadOrders(model));

        btnReceive.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) { showError("Select an order row first."); return; }
            int ordId = (int) model.getValueAt(sel, 0);
            try {
                conn.prepareStatement(
                    "UPDATE purchase_orders SET status='Received' WHERE order_id=" + ordId)
                    .executeUpdate();
                showSuccess("Order marked as Received.");
                loadOrders(model);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(makeTablePane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadOrders(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            ResultSet rs = conn.createStatement()
                .executeQuery("SELECT * FROM order_summary ORDER BY order_id");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("order_id"),
                    rs.getString("item_name"),
                    rs.getString("supplier_name"),
                    rs.getDate("order_date"),
                    rs.getDouble("quantity_ordered"),
                    "₹" + rs.getDouble("unit_price"),
                    "₹" + rs.getDouble("total_cost"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    // =====================================================
    //  TAB 4 — USAGE LOG
    // =====================================================
    private JPanel buildUsagePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        String[] cols = {"Log ID", "Item Name", "Unit", "Used Quantity", "Date", "Purpose", "Recorded By"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        loadUsage(model);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 54, 70)),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 8, 5, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfIngId   = makeField(6);
        JTextField tfQty     = makeField(8);
        JTextField tfDate    = makeField(12);
        JTextField tfPurpose = makeField(18);
        JTextField tfBy      = makeField(14);

        Object[][] ff = {
            {"Ingredient ID *", tfIngId}, {"Used Quantity *", tfQty},
            {"Date * (YYYY-MM-DD)", tfDate}, {"Purpose", tfPurpose},
            {"Recorded By", tfBy}
        };

        int r = 0, c = 0;
        for (Object[] f : ff) {
            gc.gridx = c * 2; gc.gridy = r;
            form.add(makeLabel((String) f[0]), gc);
            gc.gridx = c * 2 + 1;
            form.add((Component) f[1], gc);
            c++;
            if (c == 3) { c = 0; r++; }
        }

        JButton btnLog     = makeButton("+ Log Usage", ACCENT);
        JButton btnRefresh = makeButton("↻ Refresh", new Color(60, 100, 180));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setBackground(BG_CARD);
        btns.add(btnLog); btns.add(btnRefresh);
        gc.gridx = 0; gc.gridy = r + 1; gc.gridwidth = 6;
        form.add(btns, gc);

        btnLog.addActionListener(e -> {
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO usage_log (ingredient_id,used_quantity,used_date,purpose,recorded_by)" +
                    " VALUES (?,?,?,?,?)");
                int ingId = Integer.parseInt(tfIngId.getText().trim());
                double usedQty = Double.parseDouble(tfQty.getText().trim());
                ps.setInt(1, ingId);
                ps.setDouble(2, usedQty);
                ps.setDate(3, Date.valueOf(tfDate.getText().trim()));
                ps.setString(4, tfPurpose.getText().trim());
                ps.setString(5, tfBy.getText().trim());
                ps.executeUpdate();

                // Deduct quantity from ingredients
                conn.prepareStatement(
                    "UPDATE ingredients SET quantity = quantity - " + usedQty +
                    " WHERE ingredient_id = " + ingId).executeUpdate();

                showSuccess("Usage logged and stock updated!");
                loadUsage(model);
                for (JTextField tf : new JTextField[]{tfIngId, tfQty, tfDate, tfPurpose, tfBy})
                    tf.setText("");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        btnRefresh.addActionListener(e -> loadUsage(model));

        panel.add(form, BorderLayout.NORTH);
        panel.add(makeTablePane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadUsage(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            ResultSet rs = conn.createStatement()
                .executeQuery("SELECT * FROM usage_details ORDER BY log_id DESC");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("log_id"),
                    rs.getString("item_name"),
                    rs.getString("unit"),
                    rs.getDouble("used_quantity"),
                    rs.getDate("used_date"),
                    rs.getString("purpose"),
                    rs.getString("recorded_by")
                });
            }
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    // =====================================================
    //  TAB 5 — LOW STOCK ALERTS
    // =====================================================
    private JPanel buildLowStockPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Warning banner
        JLabel warn = new JLabel("⚠  Items below their reorder level — restock these urgently!");
        warn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        warn.setForeground(new Color(255, 200, 60));
        warn.setOpaque(true);
        warn.setBackground(new Color(80, 55, 10));
        warn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        String[] cols = {"Item Name", "Category", "Unit", "Current Qty", "Reorder Level"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        // Highlight low stock rows in red
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? new Color(200, 80, 60) : new Color(80, 25, 20));
                setForeground(new Color(255, 180, 160));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        loadLowStock(model);

        JButton btnRefresh = makeButton("↻ Refresh Alerts", new Color(200, 80, 60));
        btnRefresh.addActionListener(e -> loadLowStock(model));
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setBackground(BG_DARK);
        top.add(warn, BorderLayout.CENTER);
        top.add(btnRefresh, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(makeTablePane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadLowStock(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            ResultSet rs = conn.createStatement()
                .executeQuery("SELECT * FROM low_stock_alert ORDER BY item_name");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getString("unit"),
                    rs.getDouble("quantity"),
                    rs.getDouble("reorder_level")
                });
            }
            if (model.getRowCount() == 0) {
                model.addRow(new Object[]{"✅ All stock levels are adequate!", "", "", "", ""});
            }
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    // =====================================================
    //  HELPER DIALOGS
    // =====================================================
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // =====================================================
    //  MAIN
    // =====================================================
    public static void main(String[] args) {
        // Apply system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(KitchenInventoryApp::new);
    }
}
