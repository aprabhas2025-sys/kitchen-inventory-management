// ============================================
// DBConnection.java
// JDBC Utility Class - Singleton Pattern
// Kitchen Inventory Management System
// ============================================

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ---- Configure these before running ----
    private static final String URL = "jdbc:mysql://localhost:3306/kitchen_inventory";
    private static final String USER = "root";
    private static final String PASSWORD = "prabhas";
    // ----------------------------------------

    private static Connection connection = null;

    // Private constructor - Singleton
    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
