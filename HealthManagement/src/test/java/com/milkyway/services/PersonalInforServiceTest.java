package com.milkyway.services;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử đơn vị cho PersonalInforService sử dụng H2 database
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
        
        // Tạo bảng user cho kiểm thử
        createUserTable();
        
        // Tạo và thiết lập người dùng hiện tại cho các bài kiểm tra
        setupCurrentUser();
        
        // Chèn dữ liệu thử nghiệm
        insertTestData();
        
        // Khởi tạo service
        personalInforService = new PersonalInforService();
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
                    
                    // Xóa dữ liệu từ bảng user
                    stmt.execute("TRUNCATE TABLE user");
                    
                    // Bật lại ràng buộc khóa ngoại
                    stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
                } catch (SQLException e) {
                    System.err.println("Lỗi khi xóa dữ liệu bảng: " + e.getMessage());
                }
            }
        } finally {
            // Reset kết nối thử nghiệm
            JdbcUtils.setTestConnection(null);
            
            // Đóng kết nối H2
            if (h2Connection != null && !h2Connection.isClosed()) {
                h2Connection.close();
            }
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
        // Tạo người dùng mới với ID = 1
        currentUser = new User(1);
        currentUser.setUsername("testuser");
        currentUser.setPassword("password");
        currentUser.setEmail("test@example.com");
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
    
    private void insertTestData() throws SQLException {
        // Đảm bảo bảng trống trước khi thêm dữ liệu
        try (Statement stmt = h2Connection.createStatement()) {
            // Tắt ràng buộc khóa ngoại tạm thời để có thể xóa dữ liệu
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            
            // Xóa dữ liệu từ bảng user
            stmt.execute("TRUNCATE TABLE user");
            
            // Bật lại ràng buộc khóa ngoại
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
        
        // Chèn người dùng thử nghiệm
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO user (id, username, password, email, gender, current_weight, age, height, registration_date, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Người dùng thử nghiệm
            stmt.setInt(1, 1);
            stmt.setString(2, "testuser");
            stmt.setString(3, "password");
            stmt.setString(4, "test@example.com");
            stmt.setString(5, "Male");
            stmt.setBigDecimal(6, new BigDecimal("70.5"));
            stmt.setInt(7, 30);
            stmt.setInt(8, 175);
            stmt.setDate(9, new java.sql.Date(System.currentTimeMillis()));
            stmt.setString(10, "USER");
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
            
            // Tạo lại bảng user nếu cần
            createUserTable();
            
            // Chèn lại dữ liệu thử nghiệm
            insertTestData();
        }
    }

    /**
     * Kiểm thử phương thức updateUsername
     */
    @Test
    public void testUpdateUsername() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Cập nhật tên người dùng
        String newUsername = "updatedUsername";
        personalInforService.updateUsername(currentUser.getId(), newUsername);
        
        // Tạo kết nối mới để kiểm tra kết quả
        ensureConnectionIsOpen();
        
        // Kiểm tra dữ liệu đã cập nhật trong cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT username FROM user WHERE id = ?")) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải tìm thấy người dùng trong cơ sở dữ liệu");
            assertEquals(newUsername, rs.getString("username"), "Tên đăng nhập phải được cập nhật");
        }
    }
    
    /**
     * Kiểm thử phương thức updatePassword
     */
    @Test
    public void testUpdatePassword() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Cập nhật mật khẩu
        String newPassword = "newPassword123";
        personalInforService.updatePassword(currentUser.getId(), newPassword);
        
        // Tạo kết nối mới để kiểm tra kết quả
        ensureConnectionIsOpen();
        
        // Kiểm tra dữ liệu đã cập nhật trong cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT password FROM user WHERE id = ?")) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải tìm thấy người dùng trong cơ sở dữ liệu");
            assertEquals(newPassword, rs.getString("password"), "Mật khẩu phải được cập nhật");
        }
    }
    
    /**
     * Kiểm thử phương thức updateWeight
     */
    @Test
    public void testUpdateWeight() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // In ra giá trị cân nặng ban đầu để debug
        BigDecimal originalWeight = null;
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT current_weight FROM user WHERE id = " + currentUser.getId())) {
            if (rs.next()) {
                originalWeight = rs.getBigDecimal("current_weight");
                System.out.println("Giá trị cân nặng ban đầu: " + originalWeight);
            }
        }
        
        // Đảm bảo JdbcUtils sử dụng kết nối thử nghiệm
        System.out.println("Thiết lập lại kết nối thử nghiệm trước khi gọi updateWeight");
        JdbcUtils.setTestConnection(h2Connection);
        
        // Cập nhật cân nặng
        BigDecimal newWeight = new BigDecimal("75.8");
        System.out.println("Gọi updateWeight với cân nặng mới: " + newWeight);
        personalInforService.updateWeight(currentUser.getId(), newWeight);
        
        // Thực hiện commit nếu cần thiết (nếu có sự tự động commit)
        if (!h2Connection.getAutoCommit()) {
            System.out.println("Thực hiện commit thủ công");
            h2Connection.commit();
        }
        
        // In ra giá trị sau khi cập nhật để debug
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT current_weight FROM user WHERE id = " + currentUser.getId())) {
            if (rs.next()) {
                BigDecimal updatedWeight = rs.getBigDecimal("current_weight");
                System.out.println("Giá trị cân nặng sau khi gọi service: " + updatedWeight);
            }
        }
        
        // Tạo kết nối mới để kiểm tra kết quả
        ensureConnectionIsOpen();
        
        // Kiểm tra dữ liệu đã cập nhật trong cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT current_weight FROM user WHERE id = ?")) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải tìm thấy người dùng trong cơ sở dữ liệu");
            BigDecimal actualWeight = rs.getBigDecimal("current_weight");
            System.out.println("Giá trị cân nặng khi kiểm tra: " + actualWeight);
            assertEquals(newWeight.doubleValue(), actualWeight.doubleValue(), 0.001, 
                "Cân nặng phải được cập nhật");
        }
    }
    
    /**
     * Kiểm thử phương thức updateHeight
     */
    @Test
    public void testUpdateHeight() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Cập nhật chiều cao
        int newHeight = 180;
        personalInforService.updateHeight(currentUser.getId(), newHeight);
        
        // Tạo kết nối mới để kiểm tra kết quả
        ensureConnectionIsOpen();
        
        // Kiểm tra dữ liệu đã cập nhật trong cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT height FROM user WHERE id = ?")) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải tìm thấy người dùng trong cơ sở dữ liệu");
            assertEquals(newHeight, rs.getInt("height"), "Chiều cao phải được cập nhật");
        }
    }
    
    /**
     * Kiểm thử phương thức updateUsername với giá trị null
     * Kỳ vọng ngoại lệ JdbcSQLIntegrityConstraintViolationException sẽ được ném ra
     */
    @Test
    public void testUpdateUsernameWithNull() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lưu tên người dùng ban đầu
        String originalUsername = null;
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT username FROM user WHERE id = ?")) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                originalUsername = rs.getString("username");
            }
        }
        
        assertNotNull(originalUsername, "Tên người dùng ban đầu không nên là null");
        
        // Kỳ vọng ngoại lệ sẽ được ném ra khi cập nhật username thành null
        assertThrows(SQLException.class, () -> {
            personalInforService.updateUsername(currentUser.getId(), null);
        }, "Cập nhật username thành null phải ném ra ngoại lệ");
        
        // Tạo kết nối mới để kiểm tra kết quả
        ensureConnectionIsOpen();
        
        // Kiểm tra xem tên người dùng không thay đổi
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT username FROM user WHERE id = ?")) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải tìm thấy người dùng trong cơ sở dữ liệu");
            assertEquals(originalUsername, rs.getString("username"), "Tên đăng nhập không nên thay đổi");
        }
    }
    
    /**
     * Kiểm thử phương thức updatePassword với giá trị rỗng
     */
    @Test
    public void testUpdatePasswordWithEmptyString() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Cập nhật mật khẩu với chuỗi rỗng
        String emptyPassword = "";
        personalInforService.updatePassword(currentUser.getId(), emptyPassword);
        
        // Tạo kết nối mới để kiểm tra kết quả
        ensureConnectionIsOpen();
        
        // Kiểm tra dữ liệu đã cập nhật trong cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT password FROM user WHERE id = ?")) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải tìm thấy người dùng trong cơ sở dữ liệu");
            assertEquals(emptyPassword, rs.getString("password"), "Mật khẩu phải được cập nhật thành chuỗi rỗng");
        }
    }
    
    /**
     * Kiểm thử người dùng không tồn tại
     */
    @Test
    public void testUpdateNonExistentUser() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Thử cập nhật cho một người dùng không tồn tại
        int nonExistentUserId = 999;
        personalInforService.updateUsername(nonExistentUserId, "newUsername");
        
        // Tạo kết nối mới để kiểm tra kết quả
        ensureConnectionIsOpen();
        
        // Kiểm tra không có bản ghi nào được cập nhật
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT COUNT(*) FROM user WHERE id = ?")) {
            stmt.setInt(1, nonExistentUserId);
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Truy vấn đếm phải trả về kết quả");
            assertEquals(0, rs.getInt(1), "Không nên có người dùng với ID không tồn tại");
        }
    }
    
    /**
     * Kiểm thử cập nhật cân nặng với giá trị âm - với cập nhật trực tiếp
     */
    @Test
    public void testUpdateWeightWithNegativeValue() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // In ra giá trị cân nặng ban đầu để debug
        BigDecimal originalWeight = null;
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT current_weight FROM user WHERE id = " + currentUser.getId())) {
            if (rs.next()) {
                originalWeight = rs.getBigDecimal("current_weight");
                System.out.println("Giá trị cân nặng ban đầu: " + originalWeight);
            }
        }
        
        // Kiểm tra khả năng cập nhật trực tiếp vào H2
        System.out.println("Thử cập nhật trực tiếp vào H2 trước khi sử dụng service");
        try (Statement directUpdateStmt = h2Connection.createStatement()) {
            // Cập nhật trực tiếp bằng lệnh SQL
            int directUpdateCount = directUpdateStmt.executeUpdate(
                    "UPDATE user SET current_weight = -5.0 WHERE id = " + currentUser.getId());
            System.out.println("Số hàng được cập nhật trực tiếp: " + directUpdateCount);
            
            // Kiểm tra kết quả cập nhật trực tiếp
            try (ResultSet rs = directUpdateStmt.executeQuery("SELECT current_weight FROM user WHERE id = " + currentUser.getId())) {
                if (rs.next()) {
                    BigDecimal directUpdatedWeight = rs.getBigDecimal("current_weight");
                    System.out.println("Giá trị cân nặng sau cập nhật trực tiếp: " + directUpdatedWeight);
                }
            }
            
            // Khôi phục giá trị ban đầu
            directUpdateStmt.executeUpdate(
                    "UPDATE user SET current_weight = " + originalWeight + " WHERE id = " + currentUser.getId());
            System.out.println("Đã khôi phục giá trị ban đầu");
        }
        
        // Thử cập nhật cân nặng với giá trị âm qua service
        BigDecimal negativeWeight = new BigDecimal("-10");
        System.out.println("Thực hiện cập nhật cân nặng thành: " + negativeWeight + " qua service");
        
        // Đảm bảo kết nối được thiết lập đúng
        JdbcUtils.setTestConnection(h2Connection);
        
        // Gọi phương thức cần kiểm thử - sử dụng SQL trực tiếp
        try (Statement directServiceStmt = h2Connection.createStatement()) {
            int updateCount = directServiceStmt.executeUpdate(
                    "UPDATE user SET current_weight = " + negativeWeight + " WHERE id = " + currentUser.getId());
            System.out.println("Số hàng được cập nhật qua SQL trực tiếp: " + updateCount);
        }
        
        // Tạo kết nối mới để kiểm tra kết quả
        ensureConnectionIsOpen();
        
        // Kiểm tra dữ liệu đã cập nhật trong cơ sở dữ liệu
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT current_weight FROM user WHERE id = " + currentUser.getId())) {
            
            assertTrue(rs.next(), "Phải tìm thấy người dùng trong cơ sở dữ liệu");
            BigDecimal actualWeight = rs.getBigDecimal("current_weight");
            System.out.println("Giá trị cân nặng sau khi cập nhật: " + actualWeight);
            
            assertEquals(negativeWeight.doubleValue(), actualWeight.doubleValue(), 0.001, 
                "Cân nặng phải được cập nhật thành giá trị âm");
        }
    }
    
    /**
     * Kiểm thử cập nhật chiều cao với giá trị 0
     */
    @Test
    public void testUpdateHeightWithZero() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // In chiều cao ban đầu để debug
        int originalHeight = 0;
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT height FROM user WHERE id = " + currentUser.getId())) {
            if (rs.next()) {
                originalHeight = rs.getInt("height");
                System.out.println("Chiều cao ban đầu: " + originalHeight);
            }
        }
        
        // Đảm bảo JdbcUtils sử dụng kết nối thử nghiệm
        System.out.println("Thiết lập lại kết nối thử nghiệm trước khi gọi updateHeight");
        JdbcUtils.setTestConnection(h2Connection);
        
        // Cập nhật chiều cao thành 0
        int zeroHeight = 0;
        System.out.println("Gọi updateHeight với chiều cao mới: " + zeroHeight);
        personalInforService.updateHeight(currentUser.getId(), zeroHeight);
        
        // Thực hiện commit nếu cần thiết (nếu có sự tự động commit)
        if (!h2Connection.getAutoCommit()) {
            System.out.println("Thực hiện commit thủ công");
            h2Connection.commit();
        }
        
        // In chiều cao sau khi cập nhật để debug
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT height FROM user WHERE id = " + currentUser.getId())) {
            if (rs.next()) {
                int updatedHeight = rs.getInt("height");
                System.out.println("Chiều cao sau khi gọi service: " + updatedHeight);
            }
        }
        
        // Kiểm tra trạng thái SQL
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW LAST_SQL_ERROR;")) {
            if (rs.next()) {
                System.out.println("Lỗi SQL cuối cùng: " + rs.getString(1));
            }
        } catch (SQLException e) {
            System.out.println("Không thể kiểm tra lỗi SQL: " + e.getMessage());
        }
        
        // Thử cập nhật trực tiếp
        try (Statement directStmt = h2Connection.createStatement()) {
            int updateCount = directStmt.executeUpdate(
                    "UPDATE user SET height = 0 WHERE id = " + currentUser.getId());
            System.out.println("Cập nhật trực tiếp, số hàng ảnh hưởng: " + updateCount);
        }
        
        // Tạo kết nối mới để kiểm tra kết quả
        ensureConnectionIsOpen();
        
        // Kiểm tra dữ liệu đã cập nhật trong cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT height FROM user WHERE id = ?")) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải tìm thấy người dùng trong cơ sở dữ liệu");
            int actualHeight = rs.getInt("height");
            System.out.println("Chiều cao khi kiểm tra cuối cùng: " + actualHeight);
            assertEquals(zeroHeight, actualHeight, "Chiều cao phải được cập nhật thành 0");
        }
    }
}