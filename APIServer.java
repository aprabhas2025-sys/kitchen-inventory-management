import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class APIServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        DBConnection.getConnection();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/health", APIServer::health);
        server.createContext("/api/suppliers", ex -> handleGet(ex, "SELECT supplier_id, supplier_name, contact_name, phone, email, address FROM suppliers ORDER BY supplier_id"));
        server.createContext("/api/ingredients", ex -> handleGet(ex, "SELECT ingredient_id, item_name, category, unit, quantity, reorder_level, unit_price, supplier_name, expiry_date FROM ingredient_details ORDER BY ingredient_id"));
        server.createContext("/api/purchase_orders", ex -> handleGet(ex, "SELECT order_id, item_name, supplier_name, order_date, quantity_ordered, unit_price, total_cost, status FROM order_summary ORDER BY order_id"));
        server.createContext("/api/usage_log", ex -> handleGet(ex, "SELECT log_id, item_name, unit, used_quantity, used_date, purpose, recorded_by FROM usage_details ORDER BY log_id DESC"));
        server.createContext("/api/low_stock_alert", ex -> handleGet(ex, "SELECT item_name, category, unit, quantity, reorder_level FROM low_stock_alert ORDER BY item_name"));
        server.createContext("/api/ingredient", APIServer::addIngredient);
        server.createContext("/api/supplier", APIServer::addSupplier);
        server.createContext("/api/order", APIServer::addOrder);
        server.createContext("/api/usage", APIServer::addUsage);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Kitchen Inventory API running at http://localhost:" + PORT);
        System.out.println("Keep this window open while using index-premium.html.");
    }

    private static void health(HttpExchange ex) throws IOException {
        sendJson(ex, 200, "{\"ok\":true,\"message\":\"API connected to MySQL\"}");
    }

    private static void handleGet(HttpExchange ex, String sql) throws IOException {
        if (handleOptions(ex)) return;
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"ok\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            sendJson(ex, 200, resultSetToJson(rs));
        } catch (Exception e) {
            sendJson(ex, 500, error(e.getMessage()));
        }
    }

    private static void addIngredient(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;
        try {
            Map<String, String> body = parseJson(readBody(ex));
            String sql = "INSERT INTO ingredients (item_name, category, unit, quantity, reorder_level, unit_price, supplier_id, expiry_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
                ps.setString(1, body.get("item_name"));
                ps.setString(2, body.getOrDefault("category", ""));
                ps.setString(3, body.get("unit"));
                ps.setDouble(4, parseDouble(body.get("quantity")));
                ps.setDouble(5, parseDouble(body.getOrDefault("reorder_level", "5")));
                ps.setDouble(6, parseDouble(body.getOrDefault("unit_price", "0")));
                setNullableInt(ps, 7, body.get("supplier_id"));
                setNullableDate(ps, 8, body.get("expiry_date"));
                ps.executeUpdate();
            }
            sendJson(ex, 200, "{\"ok\":true,\"message\":\"Ingredient saved in MySQL\"}");
        } catch (Exception e) {
            sendJson(ex, 500, error(e.getMessage()));
        }
    }

    private static void addSupplier(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;
        try {
            Map<String, String> body = parseJson(readBody(ex));
            String sql = "INSERT INTO suppliers (supplier_name, contact_name, phone, email, address) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
                ps.setString(1, body.get("supplier_name"));
                ps.setString(2, body.getOrDefault("contact_name", ""));
                ps.setString(3, body.getOrDefault("phone", ""));
                ps.setString(4, body.getOrDefault("email", ""));
                ps.setString(5, body.getOrDefault("address", ""));
                ps.executeUpdate();
            }
            sendJson(ex, 200, "{\"ok\":true,\"message\":\"Supplier saved in MySQL\"}");
        } catch (Exception e) {
            sendJson(ex, 500, error(e.getMessage()));
        }
    }

    private static void addOrder(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;
        try {
            Map<String, String> body = parseJson(readBody(ex));
            double qty = parseDouble(body.get("quantity_ordered"));
            double price = parseDouble(body.get("unit_price"));
            int ingredientId = Integer.parseInt(body.get("ingredient_id"));
            String sql = "INSERT INTO purchase_orders (ingredient_id, supplier_id, order_date, quantity_ordered, unit_price, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, ingredientId);
                ps.setInt(2, Integer.parseInt(body.get("supplier_id")));
                ps.setDate(3, Date.valueOf(body.get("order_date")));
                ps.setDouble(4, qty);
                ps.setDouble(5, price);
                ps.setString(6, body.getOrDefault("status", "Pending"));
                ps.setString(7, body.getOrDefault("notes", ""));
                ps.executeUpdate();
            }
            if ("Received".equalsIgnoreCase(body.getOrDefault("status", ""))) {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE ingredients SET quantity = quantity + ? WHERE ingredient_id = ?")) {
                    ps.setDouble(1, qty);
                    ps.setInt(2, ingredientId);
                    ps.executeUpdate();
                }
            }
            sendJson(ex, 200, "{\"ok\":true,\"message\":\"Order saved in MySQL\"}");
        } catch (Exception e) {
            sendJson(ex, 500, error(e.getMessage()));
        }
    }

    private static void addUsage(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;
        try {
            Map<String, String> body = parseJson(readBody(ex));
            int ingredientId = Integer.parseInt(body.get("ingredient_id"));
            double qty = parseDouble(body.get("used_quantity"));
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO usage_log (ingredient_id, used_quantity, used_date, purpose, recorded_by) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, ingredientId);
                ps.setDouble(2, qty);
                ps.setDate(3, Date.valueOf(body.get("used_date")));
                ps.setString(4, body.getOrDefault("purpose", ""));
                ps.setString(5, body.getOrDefault("recorded_by", ""));
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE ingredients SET quantity = GREATEST(quantity - ?, 0) WHERE ingredient_id = ?")) {
                ps.setDouble(1, qty);
                ps.setInt(2, ingredientId);
                ps.executeUpdate();
            }
            sendJson(ex, 200, "{\"ok\":true,\"message\":\"Usage saved and stock updated in MySQL\"}");
        } catch (Exception e) {
            sendJson(ex, 500, error(e.getMessage()));
        }
    }

    private static String resultSetToJson(ResultSet rs) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        StringBuilder out = new StringBuilder("[");
        boolean firstRow = true;
        while (rs.next()) {
            if (!firstRow) out.append(",");
            firstRow = false;
            out.append("{");
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (i > 1) out.append(",");
                String col = meta.getColumnLabel(i);
                Object val = rs.getObject(i);
                out.append("\"").append(jsonEscape(col)).append("\":");
                if (val == null) out.append("null");
                else if (val instanceof Number) out.append(val);
                else out.append("\"").append(jsonEscape(String.valueOf(val))).append("\"");
            }
            out.append("}");
        }
        out.append("]");
        return out.toString();
    }

    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        String body = json.trim();
        if (body.startsWith("{")) body = body.substring(1);
        if (body.endsWith("}")) body = body.substring(0, body.length() - 1);
        StringBuilder token = new StringBuilder();
        boolean inString = false;
        for (int i = 0; i <= body.length(); i++) {
            char ch = i == body.length() ? ',' : body.charAt(i);
            if (ch == '"' && (i == 0 || body.charAt(i - 1) != '\\')) inString = !inString;
            if (ch == ',' && !inString) {
                addPair(map, token.toString());
                token.setLength(0);
            } else {
                token.append(ch);
            }
        }
        return map;
    }

    private static void addPair(Map<String, String> map, String pair) {
        int split = pair.indexOf(':');
        if (split < 0) return;
        String key = cleanJsonValue(pair.substring(0, split));
        String value = cleanJsonValue(pair.substring(split + 1));
        if (!"null".equalsIgnoreCase(value)) map.put(key, value);
    }

    private static String cleanJsonValue(String value) {
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static void setNullableInt(PreparedStatement ps, int index, String value) throws Exception {
        if (value == null || value.isBlank() || "0".equals(value)) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, Integer.parseInt(value));
    }

    private static void setNullableDate(PreparedStatement ps, int index, String value) throws Exception {
        if (value == null || value.isBlank()) ps.setNull(index, Types.DATE);
        else ps.setDate(index, Date.valueOf(value));
    }

    private static double parseDouble(String value) {
        return value == null || value.isBlank() ? 0 : Double.parseDouble(value);
    }

    private static boolean handleOptions(HttpExchange ex) throws IOException {
        addCors(ex);
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            ex.close();
            return true;
        }
        return false;
    }

    private static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        addCors(ex);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String error(String message) {
        return "{\"ok\":false,\"message\":\"" + jsonEscape(message) + "\"}";
    }

    private static String jsonEscape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
