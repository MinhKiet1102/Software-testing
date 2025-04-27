/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        String sql = "SELECT id, username, password, email, gender, current_weight, age, height, registration_date, role FROM user WHERE BINARY username = ?";
        Connection conn = getConnection();
        if (conn == null) {
            return null;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");

                // So sánh mật khẩu nhập vào với mật khẩu đã hash
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                if (!passwordEncoder.matches(password, hashedPassword)) {
                    return null; // Sai mật khẩu
                }

                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        hashedPassword, // giữ hashed password trong user object
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
            if (user.getId() != null && user.getId() > 0) { // Update - fixed null check
                String query = "UPDATE user SET username=?, password=?, email=?, gender=?, current_weight=?, age=?, height=?, registration_date=?, role=? WHERE id=?";
                try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                    preparedStatement.setString(1, user.getUsername());
                    String hashedPassword = new BCryptPasswordEncoder().encode(user.getPassword());
                    preparedStatement.setString(2, hashedPassword);

                    preparedStatement.setString(3, user.getEmail());
                    preparedStatement.setString(4, user.getGender());

                    // Handle null current_weight
                    if (user.getCurrentWeight() != null) {
                        preparedStatement.setBigDecimal(5, user.getCurrentWeight());
                    } else {
                        preparedStatement.setNull(5, java.sql.Types.DECIMAL);
                    }

                    // Handle null age
                    if (user.getAge() != null) {
                        preparedStatement.setInt(6, user.getAge());
                    } else {
                        preparedStatement.setNull(6, java.sql.Types.INTEGER);
                    }

                    // Handle null height
                    if (user.getHeight() != null) {
                        preparedStatement.setInt(7, user.getHeight());
                    } else {
                        preparedStatement.setNull(7, java.sql.Types.INTEGER);
                    }

                    // Handle null registration date
                    if (user.getRegistrationDate() != null) {
                        preparedStatement.setDate(8, new java.sql.Date(user.getRegistrationDate().getTime()));
                    } else {
                        preparedStatement.setNull(8, java.sql.Types.DATE);
                    }

                    preparedStatement.setString(9, user.getRole() != null ? user.getRole() : "USER");
                    preparedStatement.setInt(10, user.getId());
                    preparedStatement.executeUpdate();
                }
            } else { // Create
                String query = "INSERT INTO user (username, password, email, gender, current_weight, age, height, registration_date, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, user.getUsername());
                    String hashedPassword = new BCryptPasswordEncoder().encode(user.getPassword());
                    preparedStatement.setString(2, hashedPassword);

                    preparedStatement.setString(3, user.getEmail());
                    preparedStatement.setString(4, user.getGender());

                    // Handle null current_weight
                    if (user.getCurrentWeight() != null) {
                        preparedStatement.setBigDecimal(5, user.getCurrentWeight());
                    } else {
                        preparedStatement.setNull(5, java.sql.Types.DECIMAL);
                    }

                    // Handle null age
                    if (user.getAge() != null) {
                        preparedStatement.setInt(6, user.getAge());
                    } else {
                        preparedStatement.setNull(6, java.sql.Types.INTEGER);
                    }

                    // Handle null height
                    if (user.getHeight() != null) {
                        preparedStatement.setInt(7, user.getHeight());
                    } else {
                        preparedStatement.setNull(7, java.sql.Types.INTEGER);
                    }

                    // Handle null registration date
                    if (user.getRegistrationDate() != null) {
                        preparedStatement.setDate(8, new java.sql.Date(user.getRegistrationDate().getTime()));
                    } else {
                        preparedStatement.setNull(8, java.sql.Types.DATE);
                    }

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

    public boolean usernameExists(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        try (Connection conn = JdbcUtils.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * Đếm số lượng người dùng có vai trò Admin trong hệ thống
     * 
     * @return Số lượng người dùng có vai trò Admin
     * @throws SQLException Nếu có lỗi khi truy vấn cơ sở dữ liệu
     */
    public int countAdminUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE role = 'ADMIN'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(LoginService.class.getName()).log(Level.SEVERE, 
                    "Lỗi đếm số người dùng Admin", ex);
            throw ex;
        }
        
        return 0;
    }
}
