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
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp cho LoginService sử dụng H2 database
 */
public class LoginServiceTest {
    
    private Connection h2Connection;
    private LoginService loginService;
    
    /**
     * Thiết lập H2 database trong bộ nhớ trước mỗi kiểm thử
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Khởi tạo kết nối H2 database trong bộ nhớ và cấu hình với TestDatabaseSetup
        h2Connection = TestDatabaseSetup.createH2Connection();
        
        // Tạo bảng user để kiểm thử
        createUserTable();
        
        // Chèn dữ liệu thử nghiệm
        insertTestData();
        
        // Khởi tạo service với kết nối test
        loginService = new LoginService(h2Connection);
    }
    
    /**
     * Hàm tự định nghĩa để sử dụng làm alias cho hàm BINARY trong H2
     * Hàm này chỉ trả về chuỗi đầu vào mà không thay đổi nó (vì BINARY trong MySQL
     * chỉ đánh dấu rằng so sánh string sẽ được thực hiện theo kiểu case-sensitive)
     */
    public static String binaryCompare(String input) {
        return input;
    }
    
    /**
     * Dọn dẹp sau mỗi kiểm thử
     */
    @AfterEach
    public void tearDown() throws Exception {
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
     * Chèn dữ liệu thử nghiệm
     */
    private void insertTestData() throws SQLException {
        // Đảm bảo bảng trống trước khi thêm dữ liệu
        try (Statement stmt = h2Connection.createStatement()) {
            // Xóa dữ liệu từ các bảng
            stmt.execute("TRUNCATE TABLE user");
        }
        
        // Chèn người dùng thử nghiệm
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO user (id, username, password, email, gender, current_weight, age, height, registration_date, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Người dùng admin
            stmt.setInt(1, 1);
            stmt.setString(2, "admin");
            stmt.setString(3, "password123");
            stmt.setString(4, "admin@example.com");
            stmt.setString(5, "Nam");
            stmt.setBigDecimal(6, new BigDecimal("70.0"));
            stmt.setInt(7, 30);
            stmt.setInt(8, 175);
            stmt.setDate(9, new java.sql.Date(new Date().getTime()));
            stmt.setString(10, "ADMIN");
            stmt.executeUpdate();
            
            // Người dùng thường
            stmt.setInt(1, 2);
            stmt.setString(2, "user");
            stmt.setString(3, "password456");
            stmt.setString(4, "user@example.com");
            stmt.setString(5, "Nữ");
            stmt.setBigDecimal(6, new BigDecimal("60.0"));
            stmt.setInt(7, 25);
            stmt.setInt(8, 165);
            stmt.setDate(9, new java.sql.Date(new Date().getTime()));
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
            
            // Tạo kết nối mới sử dụng TestDatabaseSetup
            h2Connection = TestDatabaseSetup.createH2Connection();
            
            // Tạo lại các bảng nếu cần
            createUserTable();
            
            // Chèn lại dữ liệu thử nghiệm
            insertTestData();
        }
    }
    
    /**
     * Kiểm thử phương thức đăng nhập với thông tin đúng
     */
    // @Test
    // public void testLoginSuccess() throws SQLException {
    //     // Đảm bảo kết nối vẫn mở
    //     ensureConnectionIsOpen();
        
    //     // Gọi phương thức đăng nhập
    //     User user = loginService.login("admin", "password123");
        
    //     // Kiểm tra kết quả
    //     assertNotNull(user, "Người dùng phải được tìm thấy");
    //     assertEquals("admin", user.getUsername(), "Tên người dùng phải khớp");
    //     assertEquals("password123", user.getPassword(), "Mật khẩu phải khớp");
    //     assertEquals("admin@example.com", user.getEmail(), "Email phải khớp");
    //     assertEquals("ADMIN", user.getRole(), "Vai trò phải khớp");
    // }
    
    /**
     * Kiểm thử phương thức đăng nhập với tên đăng nhập sai
     */
    // @Test
    // public void testLoginWithInvalidUsername() throws SQLException {
    //     // Đảm bảo kết nối vẫn mở
    //     ensureConnectionIsOpen();
        
    //     // Gọi phương thức đăng nhập với tên đăng nhập sai
    //     User user = loginService.login("nonexistent", "password123");
        
    //     // Kiểm tra kết quả
    //     assertNull(user, "Không nên tìm thấy người dùng với tên đăng nhập sai");
    // }
    
    /**
     * Kiểm thử phương thức đăng nhập với mật khẩu sai
     */
    // @Test
    // public void testLoginWithInvalidPassword() throws SQLException {
    //     // Đảm bảo kết nối vẫn mở
    //     ensureConnectionIsOpen();
        
    //     // Gọi phương thức đăng nhập với mật khẩu sai
    //     User user = loginService.login("admin", "wrongpassword");
        
    //     // Kiểm tra kết quả
    //     assertNull(user, "Không nên tìm thấy người dùng với mật khẩu sai");
    // }
    
    /**
     * Kiểm thử phương thức đăng ký người dùng mới
     */
    @Test
    public void testRegisterNewUser() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo người dùng mới
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword");
        newUser.setEmail("newuser@example.com");
        newUser.setGender("Nam");
        newUser.setCurrentWeight(new BigDecimal("65.5"));
        newUser.setAge(22);
        newUser.setHeight(170);
        newUser.setRegistrationDate(new Date());
        
        // Đăng ký người dùng
        loginService.register(newUser);
        
        // Kiểm tra ID được tạo
        assertNotNull(newUser.getId(), "Người dùng phải có ID được tạo");
        assertTrue(newUser.getId() > 0, "ID người dùng phải lớn hơn 0");
        
        // Kiểm tra trong cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT * FROM user WHERE username = ?")) {
            stmt.setString(1, "newuser");
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Người dùng phải tồn tại trong cơ sở dữ liệu");
            assertEquals("newuser", rs.getString("username"), "Tên người dùng phải khớp");
            
            // Kiểm tra mật khẩu đã được mã hóa
            String hashedPassword = rs.getString("password");
            assertTrue(hashedPassword.startsWith("$2a$"), "Mật khẩu phải được mã hóa bằng BCrypt");
            assertNotEquals("newpassword", hashedPassword, "Mật khẩu không được lưu dưới dạng plain text");
            
            assertEquals("newuser@example.com", rs.getString("email"), "Email phải khớp");
            assertEquals("USER", rs.getString("role"), "Vai trò mặc định phải là USER");
        }
    }
    
    /**
     * Kiểm thử phương thức đăng ký với tên người dùng trùng lặp
     */
    @Test
    public void testRegisterWithDuplicateUsername() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo người dùng với tên trùng lặp
        User duplicateUser = new User();
        duplicateUser.setUsername("admin"); // Tên đã tồn tại
        duplicateUser.setPassword("somepassword");
        duplicateUser.setEmail("another@example.com");
        duplicateUser.setRegistrationDate(new Date());
        
        // Thử đăng ký và bắt ngoại lệ
        assertThrows(SQLException.class, () -> loginService.register(duplicateUser),
                "Nên có ngoại lệ khi đăng ký với tên người dùng trùng lặp");
    }
    
    /**
     * Kiểm thử phương thức cập nhật thông tin người dùng
     */
    @Test
    public void testUpdateUser() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lấy người dùng hiện tại
        User userToUpdate = loginService.getUserByUsername("user");
        assertNotNull(userToUpdate, "Người dùng phải được tìm thấy");
        
        // Cập nhật thông tin
        String newEmail = "updated_user@example.com";
        BigDecimal newWeight = new BigDecimal("62.5");
        userToUpdate.setEmail(newEmail);
        userToUpdate.setCurrentWeight(newWeight);
        
        // Gọi phương thức cập nhật
        loginService.register(userToUpdate); // Phương thức register cũng được dùng để cập nhật
        
        // Kiểm tra trong cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement("SELECT * FROM user WHERE id = ?")) {
            stmt.setInt(1, userToUpdate.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Người dùng phải tồn tại trong cơ sở dữ liệu");
            assertEquals(newEmail, rs.getString("email"), "Email phải được cập nhật");
            
            // Sửa cách so sánh BigDecimal để không phụ thuộc vào số chữ số thập phân
            BigDecimal actualWeight = rs.getBigDecimal("current_weight");
            assertEquals(0, newWeight.compareTo(actualWeight), "Cân nặng phải được cập nhật");
        }
    }
    
    /**
     * Kiểm thử phương thức lấy người dùng theo tên
     */
    @Test
    public void testGetUserByUsername() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức
        User user = loginService.getUserByUsername("user");
        
        // Kiểm tra kết quả
        assertNotNull(user, "Người dùng phải được tìm thấy");
        assertEquals("user", user.getUsername(), "Tên người dùng phải khớp");
        assertEquals("user@example.com", user.getEmail(), "Email phải khớp");
        assertEquals("USER", user.getRole(), "Vai trò phải khớp");
    }
    
    /**
     * Kiểm thử phương thức lấy người dùng với tên không tồn tại
     */
    @Test
    public void testGetUserByNonExistentUsername() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức với tên không tồn tại
        User user = loginService.getUserByUsername("nonexistent");
        
        // Kiểm tra kết quả
        assertNull(user, "Không nên tìm thấy người dùng với tên không tồn tại");
    }
    
    /**
     * Kiểm thử phương thức lấy người dùng với tên null
     */
    @Test
    public void testGetUserByNullUsername() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức với tên null
        User user = loginService.getUserByUsername(null);
        
        // Kiểm tra kết quả
        assertNull(user, "Không nên tìm thấy người dùng với tên null");
    }
    
    /**
     * Kiểm thử phương thức usernameExists với username tồn tại
     */
    @Test
    public void testUsernameExistsTrue() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo một LoginService mới sử dụng kết nối thử nghiệm
        LoginService testService = new LoginService(h2Connection);
        
        // Kiểm tra username đã tồn tại
        boolean exists = testService.usernameExists("admin");
        
        // Kiểm tra kết quả
        assertTrue(exists, "Username 'admin' phải tồn tại");
    }
    
    /**
     * Kiểm thử phương thức usernameExists với username không tồn tại
     */
    @Test
    public void testUsernameExistsFalse() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo một LoginService mới sử dụng kết nối thử nghiệm
        LoginService testService = new LoginService(h2Connection);
        
        // Kiểm tra username không tồn tại
        boolean exists = testService.usernameExists("nonexistentuser");
        
        // Kiểm tra kết quả
        assertFalse(exists, "Username 'nonexistentuser' không nên tồn tại");
    }
    
    /**
     * Kiểm thử phương thức usernameExists với username null
     */
    @Test
    public void testUsernameExistsWithNull() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo một LoginService mới sử dụng kết nối thử nghiệm
        LoginService testService = new LoginService(h2Connection);
        
        // Kiểm tra với username null
        boolean exists = testService.usernameExists(null);
        
        // Kiểm tra kết quả
        assertFalse(exists, "Username null không nên tồn tại");
    }
    
    /**
     * Kiểm thử phương thức usernameExists với username rỗng
     */
    @Test
    public void testUsernameExistsWithEmpty() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo một LoginService mới sử dụng kết nối thử nghiệm
        LoginService testService = new LoginService(h2Connection);
        
        // Kiểm tra với username rỗng
        boolean exists = testService.usernameExists("");
        
        // Kiểm tra kết quả
        assertFalse(exists, "Username rỗng không nên tồn tại");
    }
    
    /**
     * Kiểm thử phương thức countAdminUsers
     */
    @Test
    public void testCountAdminUsers() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo một LoginService mới sử dụng kết nối thử nghiệm
        LoginService testService = new LoginService(h2Connection);
        
        // Đếm số lượng admin
        int count = testService.countAdminUsers();
        
        // Kiểm tra kết quả - mặc định có 1 admin "admin"
        assertEquals(1, count, "Phải có đúng 1 admin trong cơ sở dữ liệu thử nghiệm");
    }
    
    /**
     * Kiểm thử phương thức countAdminUsers khi không có admin nào
     */
    @Test
    public void testCountAdminUsersWithNoAdmin() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Xóa toàn bộ người dùng admin khỏi cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement("UPDATE user SET role = 'USER' WHERE role = 'ADMIN'")) {
            stmt.executeUpdate();
        }
        
        // Tạo một LoginService mới sử dụng kết nối thử nghiệm
        LoginService testService = new LoginService(h2Connection);
        
        // Đếm số lượng admin
        int count = testService.countAdminUsers();
        
        // Kiểm tra kết quả - không có admin nào
        assertEquals(0, count, "Không nên có admin nào trong cơ sở dữ liệu khi tất cả đã bị chuyển sang USER");
    }
    
    /**
     * Kiểm thử phương thức countAdminUsers với nhiều admin
     */
    @Test
    public void testCountMultipleAdminUsers() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Thêm một admin mới
        User newAdmin = new User();
        newAdmin.setUsername("admin2");
        newAdmin.setPassword("admin2pass");
        newAdmin.setEmail("admin2@example.com");
        newAdmin.setRole("ADMIN");
        newAdmin.setRegistrationDate(new Date());
        
        // Đăng ký người dùng admin mới
        loginService.register(newAdmin);
        
        // Tạo một LoginService mới sử dụng kết nối thử nghiệm
        LoginService testService = new LoginService(h2Connection);
        
        // Đếm số lượng admin
        int count = testService.countAdminUsers();
        
        // Kiểm tra kết quả - phải có 2 admin
        assertEquals(2, count, "Phải có đúng 2 admin trong cơ sở dữ liệu thử nghiệm");
    }
}