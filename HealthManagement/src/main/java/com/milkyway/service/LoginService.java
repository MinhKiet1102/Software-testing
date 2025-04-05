/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.service;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Admin
 */
public class LoginService {

    public User login(String username, String password) throws SQLException {
    String sql = "SELECT id, username, password, email, gender, current_weight, age, height, registration_date FROM user WHERE username = ? AND password = ?";
    try (Connection conn = JdbcUtils.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("gender"),
                rs.getBigDecimal("current_weight"),
                rs.getInt("age"),
                rs.getInt("height"),
                rs.getDate("registration_date")
            );
        }
    }
    return null;
}


//    public boolean register(User user, String gender, BigDecimal weight, int age, int height) throws SQLException {
//        String checkUserSql = "SELECT username FROM user WHERE username = ?";
//        String insertSql = "INSERT INTO user (username, password, email, gender, current_weight, age, height, registration_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
//
//        try (Connection conn = JdbcUtils.getConn(); PreparedStatement checkStmt = conn.prepareStatement(checkUserSql); PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
//
//            checkStmt.setString(1, user.getUsername());
//            ResultSet result = checkStmt.executeQuery();
//            if (result.next()) {
//                return false; // Tên đăng nhập đã tồn tại
//            }
//            insertStmt.setString(1, user.getUsername());
//            insertStmt.setString(2, user.getPassword());
//            insertStmt.setString(3, user.getEmail());
//            insertStmt.setString(4, gender);
//            insertStmt.setBigDecimal(5, weight);
//            insertStmt.setInt(6, age);
//            insertStmt.setInt(7, height);
//            insertStmt.setDate(8, new java.sql.Date(System.currentTimeMillis()));
//
//            insertStmt.executeUpdate();
//            return true; 
//        }
//    }
    public void save(User user) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return;
        }

        if (user.getId() > 0) { // Update
            String query = "UPDATE user SET username=?, password=?, email=?, gender=?, current_weight=?, age=?, height=?, registration_date=? WHERE id=?;";
            try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getPassword());
                preparedStatement.setString(3, user.getEmail());
                preparedStatement.setString(4, user.getGender());
                preparedStatement.setBigDecimal(5, user.getCurrentWeight());
                preparedStatement.setInt(6, user.getAge());
                preparedStatement.setInt(7, user.getHeight());
                preparedStatement.setInt(8, user.getId());
                java.sql.Date sqlRegistrationDate = new java.sql.Date(user.getRegistrationDate().getTime());
                preparedStatement.setDate(9, sqlRegistrationDate);
                preparedStatement.executeUpdate();
            } catch (SQLException se) {
                se.printStackTrace();
            } finally {
                try {
                    con.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        } else { // Create
            String query = "INSERT INTO user (username, password, email, gender, current_weight, age, height, registration_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement preparedStatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getPassword());
                preparedStatement.setString(3, user.getEmail());
                preparedStatement.setString(4, user.getGender());
                preparedStatement.setBigDecimal(5, user.getCurrentWeight());
                preparedStatement.setInt(6, user.getAge());
                preparedStatement.setInt(7, user.getHeight());
                java.sql.Date sqlRegistrationDate = new java.sql.Date(user.getRegistrationDate().getTime());
                preparedStatement.setDate(8, sqlRegistrationDate);

                preparedStatement.executeUpdate();

                // LẤY ID MỚI VÀ GÁN LẠI
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }

            } catch (SQLException se) {
                se.printStackTrace();
            }

        }
    }

    public User getUserByUsername(String username) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return null;
        }

        String query = "SELECT * FROM user WHERE username=?;";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("email"),
                        resultSet.getString("gender"),
                        resultSet.getBigDecimal("current_weight"),
                        resultSet.getInt("age"),
                        resultSet.getInt("height"),
                        resultSet.getDate("registration_date")
                );
            }

        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return null;
    }
}
