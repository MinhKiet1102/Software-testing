package com.milkyway.services;

import com.milkyway.pojo.JdbcUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Lớp hỗ trợ thiết lập cơ sở dữ liệu cho kiểm thử
 * Hỗ trợ tương thích giữa các cú pháp SQL khác nhau giữa MySQL và H2
 */
public class TestDatabaseSetup {
    
    /**
     * Tạo kết nối H2 database với các hàm tương thích MySQL
     * 
     * @return Kết nối đến H2 database
     * @throws SQLException nếu có lỗi khi kết nối database
     */
    public static Connection createH2Connection() throws SQLException {
        // Tạo tên database duy nhất để tránh xung đột giữa các bài test
        String dbName = "test_db_" + System.currentTimeMillis();
        
        // Tạo kết nối với chế độ tương thích MySQL và các thiết lập cần thiết
        Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", 
                "sa", "");
        
        // Thiết lập cho H2 
        setupH2ForTesting(connection);
        
        // Đặt kết nối vào JdbcUtils để các lớp khác có thể sử dụng
        JdbcUtils.setTestConnection(connection);
        
        return connection;
    }
    
    /**
     * Thiết lập các hàm tương thích để H2 có thể xử lý cú pháp MySQL
     * 
     * @param connection Kết nối H2
     * @throws SQLException nếu có lỗi khi thực hiện câu lệnh SQL
     */
    public static void setupH2ForTesting(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Bật chế độ theo dõi SQL để debug
            stmt.execute("SET TRACE_LEVEL_SYSTEM_OUT=3");
            
            // Tạo hàm BINARY để xử lý từ khóa BINARY của MySQL trong các câu truy vấn WHERE
            // Hàm này giúp H2 có thể xử lý câu truy vấn "WHERE BINARY username = ?" 
            stmt.execute("CREATE ALIAS IF NOT EXISTS BINARY FOR \"com.milkyway.services.TestDatabaseSetup.binaryFunction\"");
        }
    }
    
    /**
     * Hàm xử lý BINARY trong H2
     * Hàm này mô phỏng toán tử BINARY của MySQL, giúp thực hiện so sánh phân biệt chữ hoa chữ thường
     * 
     * @param input Chuỗi đầu vào
     * @return Chuỗi không thay đổi (H2 sẽ sử dụng nó trong so sánh phân biệt chữ hoa chữ thường)
     */
    public static String binaryFunction(String input) {
        return input;
    }
}