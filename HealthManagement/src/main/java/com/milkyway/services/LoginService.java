/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.services;

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
    
    // Kết nối CSDL được truyền từ bên ngoài cho kiểm thử
    private final Connection testConn;
    
    /**
     * Constructor mặc định sử dụng kết nối từ JdbcUtils
     */
    public LoginService() {
        this.testConn = null;
    }
    
    /**
     * Constructor nhận kết nối từ bên ngoài, chủ yếu dùng cho kiểm thử
     * 
     * @param conn Kết nối cơ sở dữ liệu
     */
    public LoginService(Connection conn) {
        this.testConn = conn;
    }
    
    /**
     * Lấy kết nối dựa trên ngữ cảnh
     * Nếu đang trong môi trường kiểm thử thì sử dụng kết nối được truyền vào
     * Nếu không thì lấy kết nối mới từ JdbcUtils
     * 
     * @return Kết nối CSDL
     * @throws SQLException nếu không thể tạo kết nối
     */
    private Connection getConnection() throws SQLException {
        return testConn != null ? testConn : JdbcUtils.getConn();
    }

    public User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, email, gender, current_weight, age, height, registration_date, role FROM user WHERE username = ? AND password = ?";
        // Nếu đang kiểm thử, sử dụng kết nối đã được truyền vào
        Connection conn = getConnection();
        if (conn == null) {
            return null;
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
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
                user.setRole(rs.getString("role"));
                return user;
            }
        } finally {
            // Chỉ đóng kết nối nếu không phải là kết nối kiểm thử
            if (testConn == null && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void register(User user) throws SQLException {
        // Nếu đang kiểm thử, sử dụng kết nối đã được truyền vào
        Connection conn = getConnection();
        if (conn == null) {
            return;
        }
        
        try {
            if (user.getId() > 0) { // Update
                String query = "UPDATE user SET username=?, password=?, email=?, gender=?, current_weight=?, age=?, height=?, registration_date=?, role=? WHERE id=?";
                try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                    preparedStatement.setString(1, user.getUsername());
                    preparedStatement.setString(2, user.getPassword());
                    preparedStatement.setString(3, user.getEmail());
                    preparedStatement.setString(4, user.getGender());
                    preparedStatement.setBigDecimal(5, user.getCurrentWeight());
                    preparedStatement.setInt(6, user.getAge());
                    preparedStatement.setInt(7, user.getHeight());
                    preparedStatement.setDate(8, new java.sql.Date(user.getRegistrationDate().getTime()));
                    preparedStatement.setString(9, user.getRole() != null ? user.getRole() : "USER");
                    preparedStatement.setInt(10, user.getId());
                    preparedStatement.executeUpdate();
                }
            } else { // Create
                String query = "INSERT INTO user (username, password, email, gender, current_weight, age, height, registration_date, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, user.getUsername());
                    preparedStatement.setString(2, user.getPassword());
                    preparedStatement.setString(3, user.getEmail());
                    preparedStatement.setString(4, user.getGender());
                    preparedStatement.setBigDecimal(5, user.getCurrentWeight());
                    preparedStatement.setInt(6, user.getAge());
                    preparedStatement.setInt(7, user.getHeight());
                    preparedStatement.setDate(8, new java.sql.Date(user.getRegistrationDate().getTime()));
                    preparedStatement.setString(9, user.getRole() != null ? user.getRole() : "USER");
                    preparedStatement.executeUpdate();

                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            user.setId(generatedKeys.getInt(1));
                        }
                    }
                }
            }
        } finally {
            // Chỉ đóng kết nối nếu không phải là kết nối kiểm thử
            if (testConn == null && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public User getUserByUsername(String username) throws SQLException {
        // Nếu đang kiểm thử, sử dụng kết nối đã được truyền vào
        Connection conn = getConnection();
        if (conn == null) {
            return null;
        }

        String query = "SELECT * FROM user WHERE username=?;";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                User user = new User(resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("email"),
                        resultSet.getString("gender"),
                        resultSet.getBigDecimal("current_weight"),
                        resultSet.getInt("age"),
                        resultSet.getInt("height"),
                        resultSet.getDate("registration_date")
                );
                user.setRole(resultSet.getString("role"));
                return user;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            // Chỉ đóng kết nối nếu không phải là kết nối kiểm thử
            if (testConn == null && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
