package com.milkyway.services;

import com.milkyway.pojo.History;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp cho HistoryService sử dụng H2 database
 */
public class HistoryServiceTest {
    
    private Connection h2Connection;
    private HistoryService historyService;
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
        createHistoryTable();
        
        // Tạo và thiết lập người dùng hiện tại cho các bài kiểm tra 
        setupCurrentUser();
        
        // Chèn dữ liệu thử nghiệm
        insertTestData();
        
        // Khởi tạo service
        historyService = new HistoryService();
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
                    stmt.execute("TRUNCATE TABLE history");
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
     * Tạo bảng history cho kiểm thử theo đúng cấu trúc như trong Database.sql
     */
    private void createHistoryTable() throws SQLException {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS history (" +
                "history_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "history_date DATE NOT NULL, " +
                "history_weight DECIMAL(5,2) NOT NULL, " +
                "history_height INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE" +
                ")"
            );
        }
    }
    
    /**
     * Thiết lập người dùng hiện tại cho các bài kiểm thử
     */
    private void setupCurrentUser() throws Exception {
        // Tạo người dùng mới với ID = 2
        currentUser = new User(2);
        currentUser.setUsername("user");
        currentUser.setPassword("password");
        currentUser.setEmail("user@example.com");
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
            stmt.execute("TRUNCATE TABLE history");
            stmt.execute("TRUNCATE TABLE user");
            
            // Bật lại ràng buộc khóa ngoại
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
        
        // Chèn người dùng thử nghiệm
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO user (id, username, password, email, current_weight, height, role) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Người dùng admin
            stmt.setInt(1, 1);
            stmt.setString(2, "admin");
            stmt.setString(3, "password");
            stmt.setString(4, "admin@example.com");
            stmt.setBigDecimal(5, new BigDecimal("70.5"));
            stmt.setInt(6, 175);
            stmt.setString(7, "ADMIN");
            stmt.executeUpdate();
            
            // Người dùng bình thường
            stmt.setInt(1, 2);
            stmt.setString(2, "user");
            stmt.setString(3, "password");
            stmt.setString(4, "user@example.com");
            stmt.setBigDecimal(5, new BigDecimal("65.0"));
            stmt.setInt(6, 170);
            stmt.setString(7, "USER");
            stmt.executeUpdate();
        }
        
        // Lấy ngày hiện tại
        Date today = new Date();
        
        // Tính ngày trước đó 1 tháng
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.MONTH, -1);
        Date oneMonthAgo = cal.getTime();
        
        // Tính ngày trước đó 2 tháng
        cal.add(Calendar.MONTH, -1);
        Date twoMonthsAgo = cal.getTime();
        
        // Chèn lịch sử kiểm tra sức khỏe thử nghiệm
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO history (history_id, history_date, history_weight, history_height, user_id) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Lịch sử của người dùng admin - 2 tháng trước
            stmt.setInt(1, 1);
            stmt.setDate(2, new java.sql.Date(twoMonthsAgo.getTime()));
            stmt.setBigDecimal(3, new BigDecimal("72.0"));
            stmt.setInt(4, 175);
            stmt.setInt(5, 1);
            stmt.executeUpdate();
            
            // Lịch sử của người dùng admin - 1 tháng trước
            stmt.setInt(1, 2);
            stmt.setDate(2, new java.sql.Date(oneMonthAgo.getTime()));
            stmt.setBigDecimal(3, new BigDecimal("71.0"));
            stmt.setInt(4, 175);
            stmt.setInt(5, 1);
            stmt.executeUpdate();
            
            // Lịch sử của người dùng admin - hiện tại
            stmt.setInt(1, 3);
            stmt.setDate(2, new java.sql.Date(today.getTime()));
            stmt.setBigDecimal(3, new BigDecimal("70.5"));
            stmt.setInt(4, 175);
            stmt.setInt(5, 1);
            stmt.executeUpdate();
            
            // Lịch sử của người dùng thường - 2 tháng trước
            stmt.setInt(1, 4);
            stmt.setDate(2, new java.sql.Date(twoMonthsAgo.getTime()));
            stmt.setBigDecimal(3, new BigDecimal("67.0"));
            stmt.setInt(4, 170);
            stmt.setInt(5, 2);
            stmt.executeUpdate();
            
            // Lịch sử của người dùng thường - 1 tháng trước
            stmt.setInt(1, 5);
            stmt.setDate(2, new java.sql.Date(oneMonthAgo.getTime()));
            stmt.setBigDecimal(3, new BigDecimal("66.0"));
            stmt.setInt(4, 170);
            stmt.setInt(5, 2);
            stmt.executeUpdate();
            
            // Lịch sử của người dùng thường - hiện tại
            stmt.setInt(1, 6);
            stmt.setDate(2, new java.sql.Date(today.getTime()));
            stmt.setBigDecimal(3, new BigDecimal("65.0"));
            stmt.setInt(4, 170);
            stmt.setInt(5, 2);
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
            createHistoryTable();
            
            // Chèn lại dữ liệu thử nghiệm
            insertTestData();
        }
    }
    
    /**
     * Kiểm thử phương thức save - lưu lịch sử sức khỏe mới
     */
    @Test
    public void testSaveNewHistory() throws SQLException, Exception {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Chuẩn bị dữ liệu kiểm thử
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1); // Ngày mai
        Date tomorrow = cal.getTime();

        // Tạo lịch sử sức khỏe mới
        History history = new History();
        history.setHistoryDate(tomorrow);
        history.setHistoryWeight(new BigDecimal("64.5"));
        history.setHistoryHeight(171);
        history.setUserId(currentUser);
        
        // Gọi phương thức được kiểm thử
        historyService.save(history);
        
        // Kiểm tra xem lịch sử đã được lưu vào cơ sở dữ liệu chưa
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "SELECT * FROM history WHERE user_id = ? AND history_date = ?")) {
            
            stmt.setInt(1, currentUser.getId());
            stmt.setDate(2, new java.sql.Date(tomorrow.getTime()));
            
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Lịch sử sức khỏe phải tồn tại trong cơ sở dữ liệu");
            assertEquals(new BigDecimal("64.5").floatValue(), rs.getBigDecimal("history_weight").floatValue(), 0.001f, "Cân nặng phải khớp");
            assertEquals(171, rs.getInt("history_height"), "Chiều cao phải khớp");
            assertFalse(rs.next(), "Chỉ nên có một bản ghi cho ngày cụ thể");
        }
    }
    
    /**
     * Kiểm thử phương thức save - cập nhật lịch sử sức khỏe đã tồn tại
     */
    @Test
    public void testUpdateExistingHistory() throws SQLException, Exception {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lấy ngày hiện tại
        Date today = new Date();
        
        // Tìm lịch sử hiện tại của người dùng
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "SELECT * FROM history WHERE user_id = ? AND DATE(history_date) = CURRENT_DATE()")) {
                
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải có lịch sử sức khỏe cho ngày hôm nay");
            assertEquals(new BigDecimal("65.0").floatValue(), rs.getBigDecimal("history_weight").floatValue(), 0.001f, "Cân nặng ban đầu phải đúng");
            assertEquals(170, rs.getInt("history_height"), "Chiều cao ban đầu phải đúng");
        }
        
        // Tạo lịch sử sức khỏe mới với cùng ngày nhưng thông tin khác
        History updatedHistory = new History();
        updatedHistory.setHistoryDate(today);
        updatedHistory.setHistoryWeight(new BigDecimal("64.0")); // Thay đổi cân nặng
        updatedHistory.setHistoryHeight(171); // Thay đổi chiều cao
        updatedHistory.setUserId(currentUser);
        
        // Gọi phương thức cần kiểm thử
        historyService.save(updatedHistory);
        
        // Kiểm tra xem lịch sử đã được cập nhật trong cơ sở dữ liệu chưa
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "SELECT * FROM history WHERE user_id = ? AND DATE(history_date) = CURRENT_DATE()")) {
                
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Lịch sử sức khỏe phải tồn tại trong cơ sở dữ liệu");
            assertEquals(new BigDecimal("64.0").floatValue(), rs.getBigDecimal("history_weight").floatValue(), 0.001f, "Cân nặng phải được cập nhật");
            assertEquals(171, rs.getInt("history_height"), "Chiều cao phải được cập nhật");
            assertFalse(rs.next(), "Chỉ nên có một bản ghi cho ngày cụ thể");
        }
    }
    
    /**
     * Kiểm thử phương thức findAllByUserId
     */
    @Test
    public void testFindAllByUserId() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử
        List<History> histories = historyService.findAllByUserId(currentUser.getId());
        
        // Kiểm tra kết quả
        assertNotNull(histories, "Danh sách lịch sử không nên là null");
        assertEquals(3, histories.size(), "Phải có 3 bản ghi lịch sử cho người dùng hiện tại");
        
        // In ra thông tin lịch sử để debug
        System.out.println("Danh sách lịch sử người dùng " + currentUser.getId() + ":");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (History h : histories) {
            System.out.println("ID: " + h.getHistoryId() + 
                            ", Ngày: " + dateFormat.format(h.getHistoryDate()) + 
                            ", Cân nặng: " + h.getHistoryWeight() + 
                            ", Chiều cao: " + h.getHistoryHeight());
        }
        
        // Kiểm tra phương thức sắp xếp
        // History mới nhất nên ở đầu, cũ nhất ở cuối (giả định)
        if (histories.size() >= 3) {
            Date firstDate = histories.get(0).getHistoryDate();
            Date lastDate = histories.get(histories.size() - 1).getHistoryDate();
            
            // Kiểm tra thứ tự sắp xếp (giả định là mới nhất trước, cũ nhất sau)
            assertTrue(firstDate.compareTo(lastDate) >= 0, "Lịch sử nên được sắp xếp từ mới đến cũ");
        }
    }
    
    /**
     * Kiểm thử phương thức findAllByUserId khi không có dữ liệu
     */
    @Test
    public void testFindAllByUserIdNoData() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Xóa tất cả dữ liệu lịch sử của người dùng hiện tại
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "DELETE FROM history WHERE user_id = ?")) {
                
            stmt.setInt(1, currentUser.getId());
            stmt.executeUpdate();
        }
        
        // Gọi phương thức được kiểm thử
        List<History> histories = historyService.findAllByUserId(currentUser.getId());
        
        // Kiểm tra kết quả
        assertNotNull(histories, "Danh sách lịch sử không nên là null");
        assertEquals(0, histories.size(), "Danh sách lịch sử nên trống");
    }
    
    /**
     * Kiểm thử phương thức findLatestHistoryByUserId
     */
    @Test
    public void testFindLatestHistoryByUserId() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử
        History latestHistory = historyService.findLatestHistoryByUserId(currentUser.getId());
        
        // Kiểm tra kết quả
        assertNotNull(latestHistory, "Lịch sử mới nhất không nên là null");
        
        // Lấy ngày hiện tại và định dạng theo chuẩn yyyy-MM-dd
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());
        String latestDate = dateFormat.format(latestHistory.getHistoryDate());
        
        // Kiểm tra ngày có phải ngày hiện tại không
        assertEquals(today, latestDate, "Lịch sử mới nhất phải là ngày hiện tại");
        assertEquals(new BigDecimal("65.0").floatValue(), latestHistory.getHistoryWeight().floatValue(), 0.001f, "Cân nặng phải khớp");
        assertEquals(170, latestHistory.getHistoryHeight().intValue(), "Chiều cao phải khớp");
    }
    
    /**
     * Kiểm thử phương thức findLatestHistoryByUserId khi không có dữ liệu
     */
    @Test
    public void testFindLatestHistoryByUserIdNoData() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Xóa tất cả dữ liệu lịch sử của người dùng hiện tại
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "DELETE FROM history WHERE user_id = ?")) {
                
            stmt.setInt(1, currentUser.getId());
            stmt.executeUpdate();
        }
        
        // Gọi phương thức được kiểm thử
        History latestHistory = historyService.findLatestHistoryByUserId(currentUser.getId());
        
        // Kiểm tra kết quả
        assertNull(latestHistory, "Lịch sử mới nhất nên là null khi không có dữ liệu");
    }
    
    /**
     * Kiểm thử phương thức recordExists qua save method
     */
    @Test
    public void testRecordExists() throws SQLException, Exception {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lấy ngày hiện tại
        Date today = new Date();
        
        // Tạo lịch sử với ngày hiện tại (đã tồn tại trong cơ sở dữ liệu)
        History existingHistory = new History();
        existingHistory.setHistoryDate(today);
        existingHistory.setHistoryWeight(new BigDecimal("64.0"));
        existingHistory.setHistoryHeight(171);
        existingHistory.setUserId(currentUser);
        
        // Gọi phương thức save sẽ gọi đến recordExists bên trong
        historyService.save(existingHistory);
        
        // Kiểm tra trong cơ sở dữ liệu để xem có bao nhiêu bản ghi cho ngày hiện tại
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "SELECT COUNT(*) as count FROM history WHERE user_id = ? AND DATE(history_date) = CURRENT_DATE()")) {
                
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải có kết quả từ truy vấn COUNT");
            assertEquals(1, rs.getInt("count"), "Chỉ nên có một bản ghi cho ngày hiện tại");
        }
        
        // Tính ngày mai
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = cal.getTime();
        
        // Tạo lịch sử với ngày mai (chưa tồn tại trong cơ sở dữ liệu)
        History newHistory = new History();
        newHistory.setHistoryDate(tomorrow);
        newHistory.setHistoryWeight(new BigDecimal("63.5"));
        newHistory.setHistoryHeight(171);
        newHistory.setUserId(currentUser);
        
        // Gọi phương thức save sẽ gọi đến recordExists bên trong
        historyService.save(newHistory);
        
        // Kiểm tra trong cơ sở dữ liệu để xem có bản ghi cho ngày mai không
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "SELECT COUNT(*) as count FROM history WHERE user_id = ? AND history_date = ?")) {
                
            stmt.setInt(1, currentUser.getId());
            stmt.setDate(2, new java.sql.Date(tomorrow.getTime()));
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải có kết quả từ truy vấn COUNT");
            assertEquals(1, rs.getInt("count"), "Chỉ nên có một bản ghi cho ngày mai");
        }
    }
}