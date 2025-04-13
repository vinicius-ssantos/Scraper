package org.vinissius.scraper.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.vinissius.scraper.model.Product;

import java.sql.*;
import java.util.List;

public class DatabaseManager {
    private String dbName;
    private final Gson gson = new GsonBuilder().create();

    public DatabaseManager(String dbName) {
        this.dbName = dbName;
    }

    private Connection connect() throws SQLException {
        // Usa o DriverManager do JDBC (java.sql)
        return java.sql.DriverManager.getConnection("jdbc:sqlite:" + dbName);
    }

    public void initDatabase() {
        // Tabela com todos os campos relevantes (use TEXT para tudo ou customizar)
        String sql = "CREATE TABLE IF NOT EXISTS products ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "asin TEXT,"
                + "title TEXT,"
                + "price TEXT,"
                + "rating TEXT,"
                + "review_count TEXT,"
                + "bullet_points TEXT,"
                + "product_description TEXT,"
                + "images TEXT,"
                + "seller_info TEXT,"
                + "executed_at TEXT,"
                + "url TEXT"
                + ");";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveProducts(List<Product> products) {
        String insertSQL = "INSERT INTO products ("
                + "asin, title, price, rating, review_count, bullet_points, product_description, images, seller_info, executed_at, url"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            for (Product p : products) {
                pstmt.setString(1, p.getAsin());
                pstmt.setString(2, p.getTitle());
                pstmt.setString(3, p.getPrice());
                pstmt.setString(4, p.getRating());
                pstmt.setString(5, p.getReviewCount());
                // bulletPoints e images em JSON
                pstmt.setString(6, p.getBulletPoints() != null ? gson.toJson(p.getBulletPoints()) : null);
                pstmt.setString(7, p.getProductDescription());
                pstmt.setString(8, p.getImages() != null ? gson.toJson(p.getImages()) : null);
                pstmt.setString(9, p.getSellerInfo());
                pstmt.setString(10, p.getExecutedAt());
                pstmt.setString(11, p.getUrl());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
