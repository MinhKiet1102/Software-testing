/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.service;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Admin
 */
public class LoginService {
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, email FROM user WHERE username = ? AND password = ?";
        try (Connection conn = JdbcUtils.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("email"));
            }
        }
        return null; 
    }

    public boolean register(User user, String gender, String weight, String age, String height) throws SQLException {
        String checkUserSql = "SELECT username FROM user WHERE username = ?";
        String insertSql = "INSERT INTO user (username, password, email, gender, current_weight, age, height, registration_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = JdbcUtils.getConn(); PreparedStatement checkStmt = conn.prepareStatement(checkUserSql); PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setString(1, user.getUsername());
            ResultSet result = checkStmt.executeQuery();
            if (result.next()) {
                return false; // Tên đăng nhập đã tồn tại
            }
            insertStmt.setString(1, user.getUsername());
            insertStmt.setString(2, user.getPassword());
            insertStmt.setString(3, user.getEmail());
            insertStmt.setString(4, gender);
            insertStmt.setString(5, weight);
            insertStmt.setString(6, age);
            insertStmt.setString(7, height);
            insertStmt.setDate(8, new java.sql.Date(System.currentTimeMillis()));

            insertStmt.executeUpdate();
            return true; 
        }
    }
}
