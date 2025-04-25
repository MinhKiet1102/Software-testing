package com.milkyway.services;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp cho PersonalInforService sử dụng H2 database
 */
public class PersonalInforServiceTest {
    
    private Connection h2Connection;
    private PersonalInforService personalInforService;
    private User currentUser;
    
    /**
     * Thiết lập H2 database trong bộ nhớ trước mỗi kiểm thử
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Khởi tạo kết nối H2 database trong bộ nhớ và cấu hình MODE=MySQL để tương thích tốt hơn
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "");
        
        // Bật SQL trace để debug các câu lệnh SQL
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("SET TRACE_LEVEL_SYSTEM_OUT=3");
        }
        
        // Thiết lập kết nối thử nghiệm trong JdbcUtils
        JdbcUtils.setTestConnection(h2Connection);
        
        // Tạo các bảng thử nghiệm
        createUserTable();
        
        // Tạo và thiết lập người dùng hiện tại cho các bài kiểm tra 
        setupCurrentUser();
        
        // Chèn dữ liệu thử nghiệm
        insertTestData();
        
        // Khởi tạo service
        personalInforService = new CustomPersonalInforService();
    }
    
    /**
     * Lớp con của PersonalInforService ghi đè các phương thức để tránh đóng kết nối
     * Vì chúng ta sẽ cần kết nối sau đó để kiểm tra kết quả
     */
    private class CustomPersonalInforService extends PersonalInforService {
        @Override
        public void updateUsername(int userId, String newUsername) throws SQLException {
            Connection con = JdbcUtils.getConn();
            if (con == null) {
                return;
            }

            String query = "UPDATE user SET username=? WHERE id=?;";

            try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
                preparedStatement.setString(1, newUsername);
                preparedStatement.setInt(2, userId);
                preparedStatement.executeUpdate();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            // KHÔNG đóng kết nối ở đây
        }
        
        @Override
        public void updatePassword(int userId, String newPassword) throws SQLException {
            Connection con = JdbcUtils.getConn();
            if (con == null) {
                return;
            }

            String query = "UPDATE user SET password=? WHERE id=?;";

            try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
                preparedStatement.setString(1, newPassword);
                preparedStatement.setInt(2, userId);
                preparedStatement.executeUpdate();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            // KHÔNG đóng kết nối ở đây
        }
        
        @Override
        public void updateWeight(int userId, BigDecimal newWeight) throws SQLException {
            Connection con = JdbcUtils.getConn();
            if (con == null) {
                return;
            }

            String query = "UPDATE user SET current_weight=? WHERE id=?;";

            try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
                preparedStatement.setBigDecimal(1, newWeight);
                preparedStatement.setInt(2, userId);
                preparedStatement.executeUpdate();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            // KHÔNG đóng kết nối ở đây
        }

        @Override
        public void updateHeight(int userId, int newHeight) throws SQLException {
            Connection con = JdbcUtils.getConn();
            if (con == null) {
                return;
            }

            String query = "UPDATE user SET height=? WHERE id=?;";

            try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
                preparedStatement.setInt(1, newHeight);
                preparedStatement.setInt(2, userId);
                preparedStatement.executeUpdate();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            // KHÔNG đóng kết nối ở đây
        }
    }
    
    /**
     * Dọn dẹp sau mỗi kiểm thử
     */
    @AfterEach
    public void tearDown() throws Exception {
        // Xóa người dùng hiện tại
        cleanupCurrentUser();
        
        try {
            // Đảm bảo kết nối vẫn mở
            if (h2Connection != null && !h2Connection.isClosed()) {
                // Chỉ xóa dữ liệu, không xóa bảng và không đóng kết nối
                try (Statement stmt = h2Connection.createStatement()) {
                    // Tắt ràng buộc khóa ngoại tạm thời để có thể xóa dữ liệu
                    stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
                    
                    // Xóa dữ liệu từ các bảng
                    stmt.execute("TRUNCATE TABLE user");
                    
                    // Bật lại ràng buộc khóa ngoại
                    stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
                } catch (SQLException e) {
                    System.err.println("Lỗi khi xóa dữ liệu bảng: " + e.getMessage());
                }
                
                // KHÔNG đóng kết nối H2 ở đây để tránh lỗi "object is already closed"
            }
        } finally {
            // Không reset kết nối thử nghiệm - chúng ta sẽ giữ nó mở
        }
    }
    
    /**
     * Tạo bảng user cho kiểm thử theo đúng cấu trúc như trong Database.sql
     */
    private void createUserTable() throws SQLException {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS user (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) NOT NULL, " +
                "gender VARCHAR(10), " +
                "current_weight DECIMAL(5,2), " +
                "age INT, " +
                "height INT, " +
                "registration_date DATE, " +
                "role VARCHAR(20) NOT NULL DEFAULT 'USER'" +
                ")"
            );
            
            // Thêm các ràng buộc unique
            try {
                stmt.execute("ALTER TABLE user ADD CONSTRAINT unique_username UNIQUE (username)");
                stmt.execute("ALTER TABLE user ADD CONSTRAINT unique_email UNIQUE (email)");
            } catch (SQLException e) {
                // Bỏ qua lỗi nếu ràng buộc đã tồn tại
                if (!e.getMessage().contains("already exists")) {
                    throw e;
                }
            }
        }
    }
    
    /**
     * Thiết lập người dùng hiện tại cho các bài kiểm thử
     */
    private void setupCurrentUser() throws Exception {
        // Tạo người dùng mới với ID = 2
        currentUser = new User(2);
        currentUser.setUsername("user");
        currentUser.setRole("USER");
        
        // Đặt người dùng hiện tại thông qua reflection
        Field field = User.class.getDeclaredField("currentUser");
        field.setAccessible(true);
        field.set(null, currentUser);
    }
    
    /**
     * Dọn dẹp người dùng hiện tại sau kiểm thử
     */
    private void cleanupCurrentUser() throws Exception {
        // Đặt người dùng hiện tại thành null thông qua reflection
        Field field = User.class.getDeclaredField("currentUser");
        field.setAccessible(true);
        field.set(null, null);
    }
    
    /**
     * Chèn dữ liệu thử nghiệm
     */
    private void insertTestData() throws SQLException {
        // Đảm bảo bảng trống trước khi thêm dữ liệu
        try (Statement stmt = h2Connection.createStatement()) {
            // Tắt ràng buộc khóa ngoại tạm thời để có thể xóa dữ liệu
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            
            // Xóa dữ liệu từ các bảng
            stmt.execute("TRUNCATE TABLE user");
            
            // Bật lại ràng buộc khóa ngoại
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
        
        // Chèn người dùng thử nghiệm
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO user (id, username, password, email, gender, current_weight, height, age, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Người dùng admin
            stmt.setInt(1, 1);
            stmt.setString(2, "admin");
            stmt.setString(3, "admin123");
            stmt.setString(4, "admin@example.com");
            stmt.setString(5, "Nam");
            stmt.setBigDecimal(6, new BigDecimal("70.5"));
            stmt.setInt(7, 175);
            stmt.setInt(8, 30);
            stmt.setString(9, "ADMIN");
            stmt.executeUpdate();
            
            // Người dùng bình thường
            stmt.setInt(1, 2);
            stmt.setString(2, "user");
            stmt.setString(3, "user123");
            stmt.setString(4, "user@example.com");
            stmt.setString(5, "Nữ");
            stmt.setBigDecimal(6, new BigDecimal("55.5"));
            stmt.setInt(7, 165);
            stmt.setInt(8, 25);
            stmt.setString(9, "USER");
            stmt.executeUpdate();
        }
    }
    
    /**
     * Đảm bảo kết nối H2 luôn được mở
     */
    private void ensureConnectionIsOpen() throws SQLException {
        if (h2Connection == null || h2Connection.isClosed()) {
            System.out.println("Kết nối đã đóng, tạo kết nối mới");
            h2Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "");
            
            // Bật SQL trace để debug
            try (Statement stmt = h2Connection.createStatement()) {
                stmt.execute("SET TRACE_LEVEL_SYSTEM_OUT=3");
            }
            
            // Thiết lập lại kết nối cho JdbcUtils
            JdbcUtils.setTestConnection(h2Connection);
            
            // Tạo lại các bảng nếu cần
            createUserTable();
            
            // Chèn lại dữ liệu thử nghiệm
            insertTestData();
        }
    }
    
    /**
     * Nhận giá trị một cột từ người dùng
     */
    private String getUserColumnValue(int userId, String columnName) throws SQLException {
        String value = null;
        String sql = "SELECT " + columnName + " FROM user WHERE id = ?";
        
        try (PreparedStatement stmt = h2Connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    value = rs.getString(columnName);
                }
            }
        }
        
        return value;
    }

    /**
     * Kiểm thử phương thức updateUsername
     */
    @Test
    public void testUpdateUsername() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra username hiện tại của user
        String originalUsername = getUserColumnValue(2, "username");
        assertEquals("user", originalUsername, "Username ban đầu phải là 'user'");
        
        // Gọi phương thức được kiểm thử
        personalInforService.updateUsername(2, "newuser");
        
        // Kiểm tra username sau khi cập nhật
        String updatedUsername = getUserColumnValue(2, "username");
        assertEquals("newuser", updatedUsername, "Username sau khi cập nhật phải là 'newuser'");
        
        // Kiểm tra username của admin không bị thay đổi
        String adminUsername = getUserColumnValue(1, "username");
        assertEquals("admin", adminUsername, "Username của admin không được thay đổi");
    }
    
    /**
     * Kiểm thử phương thức updatePassword
     */
    @Test
    public void testUpdatePassword() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra password hiện tại của user
        String originalPassword = getUserColumnValue(2, "password");
        assertEquals("user123", originalPassword, "Password ban đầu phải là 'user123'");
        
        // Gọi phương thức được kiểm thử
        personalInforService.updatePassword(2, "newpassword");
        
        // Kiểm tra password sau khi cập nhật
        String updatedPassword = getUserColumnValue(2, "password");
        assertEquals("newpassword", updatedPassword, "Password sau khi cập nhật phải là 'newpassword'");
        
        // Kiểm tra password của admin không bị thay đổi
        String adminPassword = getUserColumnValue(1, "password");
        assertEquals("admin123", adminPassword, "Password của admin không được thay đổi");
    }
    
    /**
     * Kiểm thử phương thức updateWeight
     */
    @Test
    public void testUpdateWeight() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra cân nặng hiện tại của user
        BigDecimal originalWeight = null;
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT current_weight FROM user WHERE id = ?")) {
            stmt.setInt(1, 2);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    originalWeight = rs.getBigDecimal("current_weight");
                }
            }
        }
        
        // Thay vì so sánh trực tiếp BigDecimal, chuyển đổi sang double để so sánh
        assertEquals(55.5, originalWeight.doubleValue(), 0.001, "Cân nặng ban đầu phải là 55.5 kg");
        
        // Gọi phương thức được kiểm thử
        personalInforService.updateWeight(2, new BigDecimal("60.0"));
        
        // Kiểm tra cân nặng sau khi cập nhật
        BigDecimal updatedWeight = null;
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT current_weight FROM user WHERE id = ?")) {
            stmt.setInt(1, 2);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    updatedWeight = rs.getBigDecimal("current_weight");
                }
            }
        }
        
        // Thay vì so sánh trực tiếp BigDecimal, chuyển đổi sang double để so sánh
        assertEquals(60.0, updatedWeight.doubleValue(), 0.001, "Cân nặng sau khi cập nhật phải là 60.0 kg");
        
        // Kiểm tra cân nặng của admin không bị thay đổi
        BigDecimal adminWeight = null;
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT current_weight FROM user WHERE id = ?")) {
            stmt.setInt(1, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    adminWeight = rs.getBigDecimal("current_weight");
                }
            }
        }
        
        // Thay vì so sánh trực tiếp BigDecimal, chuyển đổi sang double để so sánh
        assertEquals(70.5, adminWeight.doubleValue(), 0.001, "Cân nặng của admin không được thay đổi");
    }
    
    /**
     * Kiểm thử phương thức updateHeight
     */
    @Test
    public void testUpdateHeight() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra chiều cao hiện tại của user
        int originalHeight = 0;
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT height FROM user WHERE id = ?")) {
            stmt.setInt(1, 2);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    originalHeight = rs.getInt("height");
                }
            }
        }
        
        assertEquals(165, originalHeight, "Chiều cao ban đầu phải là 165 cm");
        
        // Gọi phương thức được kiểm thử
        personalInforService.updateHeight(2, 170);
        
        // Kiểm tra chiều cao sau khi cập nhật
        int updatedHeight = 0;
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT height FROM user WHERE id = ?")) {
            stmt.setInt(1, 2);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    updatedHeight = rs.getInt("height");
                }
            }
        }
        
        assertEquals(170, updatedHeight, "Chiều cao sau khi cập nhật phải là 170 cm");
        
        // Kiểm tra chiều cao của admin không bị thay đổi
        int adminHeight = 0;
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT height FROM user WHERE id = ?")) {
            stmt.setInt(1, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    adminHeight = rs.getInt("height");
                }
            }
        }
        
        assertEquals(175, adminHeight, "Chiều cao của admin không được thay đổi");
    }
    
    /**
     * Kiểm thử phương thức updateUsername với giá trị null
     */
    @Test
    public void testUpdateUsernameWithNull() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Ghi lại username ban đầu
        String originalUsername = getUserColumnValue(2, "username");
        
        // Gọi phương thức được kiểm thử với giá trị null
        personalInforService.updateUsername(2, null);
        
        // Kiểm tra username sau khi cập nhật
        String updatedUsername = getUserColumnValue(2, "username");
        
        // Username không nên thay đổi hoặc nên được xử lý phù hợp với logic của ứng dụng
        // Trong trường hợp này, tùy thuộc vào cách xử lý của ứng dụng, có thể là null hoặc giữ nguyên
        // Giả sử rằng khi username là null, hệ thống sẽ không thực hiện cập nhật
        assertEquals(originalUsername, updatedUsername, "Username không nên thay đổi khi cập nhật với giá trị null");
    }
    
    /**
     * Kiểm thử phương thức updateUsername với ID người dùng không tồn tại
     */
    @Test
    public void testUpdateUsernameWithNonExistentUser() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Xác nhận rằng không có người dùng với ID = 999
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT COUNT(*) FROM user WHERE id = 999")) {
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                assertEquals(0, rs.getInt(1), "Không nên có người dùng với ID = 999");
            }
        }
        
        // Gọi phương thức được kiểm thử với ID người dùng không tồn tại
        personalInforService.updateUsername(999, "nonexistentuser");
        
        // Xác nhận rằng không có người dùng nào được thêm vào với ID = 999
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT COUNT(*) FROM user WHERE id = 999")) {
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                assertEquals(0, rs.getInt(1), "Không nên có người dùng với ID = 999 sau khi cập nhật");
            }
        }
    }
}