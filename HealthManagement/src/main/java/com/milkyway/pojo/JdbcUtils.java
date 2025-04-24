/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.pojo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author admin
 */
public class JdbcUtils {
    // Biến tĩnh để lưu trữ kết nối tùy chỉnh cho việc kiểm thử
    private static Connection testConnection = null;
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    public static Connection getConn() throws SQLException {
        // Nếu có kết nối thử nghiệm, trả về nó
        if (testConnection != null) {
            return testConnection;
        }
        // Nếu không, trả về kết nối thông thường
        return DriverManager.getConnection("jdbc:mysql://localhost/healthmanagementdb", "root", "123456");
    }
    
    /**
     * Phương thức này chỉ dùng cho mục đích kiểm thử
     * Thiết lập kết nối tùy chỉnh cho việc kiểm thử
     * @param conn Kết nối cơ sở dữ liệu để sử dụng cho các lệnh gọi getConn() tiếp theo
     */
    public static void setTestConnection(Connection conn) {
        testConnection = conn;
    }
    
    /**
     * Đặt lại kết nối về trạng thái ban đầu
     * Gọi phương thức này sau khi hoàn thành kiểm thử
     */
    public static void resetTestConnection() {
        testConnection = null;
    }
}
