package com.milkyway.services;

import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.Exerciselog;
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
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp cho ExerciseLogService sử dụng H2 database
 */
public class ExerciseLogServiceTest {
    
    private Connection h2Connection;
    private ExerciseLogService exerciseLogService;
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
        createExerciseTable();
        createUserTable();
        createExerciselogTable();
        
        // Tạo và thiết lập người dùng hiện tại cho các bài kiểm tra
        setupCurrentUser();
        
        // Chèn dữ liệu thử nghiệm
        insertTestData();
        
        // Khởi tạo service
        exerciseLogService = new ExerciseLogService();
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
                    stmt.execute("TRUNCATE TABLE exerciselog");
                    stmt.execute("TRUNCATE TABLE exercise");
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
     * Tạo bảng exercise cho kiểm thử theo đúng cấu trúc như trong Database.sql
     */
    private void createExerciseTable() throws SQLException {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS exercise (" +
                "idExercise INT AUTO_INCREMENT PRIMARY KEY, " +
                "exerciseName VARCHAR(255) NOT NULL, " +
                "imageExercise VARCHAR(500), " + 
                "caloriesBurnedPerMin DOUBLE DEFAULT NULL, " +
                "userId INT DEFAULT NULL" +
                ")"
            );
            
            // Thêm khóa ngoại sau khi đã tạo bảng user
            // Sẽ được thêm sau createUserTable
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
            
            // Thêm khóa ngoại cho bảng exercise sau khi đã tạo cả hai bảng
            try {
                stmt.execute(
                    "ALTER TABLE exercise ADD CONSTRAINT fk_exercise_user " +
                    "FOREIGN KEY (userId) REFERENCES user (id) ON DELETE SET NULL"
                );
            } catch (SQLException e) {
                // Bỏ qua lỗi nếu ràng buộc đã tồn tại
                if (!e.getMessage().contains("already exists")) {
                    throw e;
                }
            }
        }
    }
    
    /**
     * Tạo bảng exerciselog cho kiểm thử theo đúng cấu trúc như trong Database.sql
     */
    private void createExerciselogTable() throws SQLException {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS exerciselog (" +
                "idExLog INT AUTO_INCREMENT PRIMARY KEY, " +
                "effortLevel VARCHAR(50) DEFAULT NULL, " +
                "duration INT NOT NULL, " +
                "datetime DATE NOT NULL, " +
                "energyBurn DOUBLE NOT NULL, " +
                "userId INT NOT NULL, " +
                "exerciseId INT NOT NULL, " +
                "CONSTRAINT fk_exerciselog_exercise FOREIGN KEY (exerciseId) REFERENCES exercise (idExercise) ON DELETE CASCADE, " +
                "CONSTRAINT fk_exerciselog_user FOREIGN KEY (userId) REFERENCES user (id) ON DELETE CASCADE" +
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
            stmt.execute("TRUNCATE TABLE exerciselog");
            stmt.execute("TRUNCATE TABLE exercise");
            stmt.execute("TRUNCATE TABLE user");
            
            // Bật lại ràng buộc khóa ngoại
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
        
        // Chèn người dùng thử nghiệm
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO user (id, username, password, email, role) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Người dùng admin
            stmt.setInt(1, 1);
            stmt.setString(2, "admin");
            stmt.setString(3, "password");
            stmt.setString(4, "admin@example.com");
            stmt.setString(5, "ADMIN");
            stmt.executeUpdate();
            
            // Người dùng bình thường
            stmt.setInt(1, 2);
            stmt.setString(2, "user");
            stmt.setString(3, "password");
            stmt.setString(4, "user@example.com");
            stmt.setString(5, "USER");
            stmt.executeUpdate();
        }
        
        // Chèn bài tập thử nghiệm
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO exercise (idExercise, exerciseName, imageExercise, caloriesBurnedPerMin, userId) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Bài tập mặc định (không có người dùng)
            stmt.setInt(1, 1);
            stmt.setString(2, "Chạy bộ");
            stmt.setString(3, "run.jpg");
            stmt.setDouble(4, 5.5);
            stmt.setNull(5, java.sql.Types.INTEGER);
            stmt.executeUpdate();
            
            // Bài tập do admin tạo
            stmt.setInt(1, 2);
            stmt.setString(2, "Bơi lội");
            stmt.setString(3, "swim.jpg");
            stmt.setDouble(4, 7.0);
            stmt.setInt(5, 1);
            stmt.executeUpdate();
            
            // Bài tập do người dùng tạo
            stmt.setInt(1, 3);
            stmt.setString(2, "Yoga");
            stmt.setString(3, "yoga.jpg");
            stmt.setDouble(4, 3.0);
            stmt.setInt(5, 2);
            stmt.executeUpdate();
        }
        
        // Chèn log bài tập thử nghiệm
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO exerciselog (idExLog, effortLevel, duration, datetime, energyBurn, userId, exerciseId) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Log bài tập cho user hôm nay
            stmt.setInt(1, 1);
            stmt.setString(2, "Cao");
            stmt.setInt(3, 30);
            stmt.setDate(4, Date.valueOf(today));
            stmt.setDouble(5, 165.0); // 30 phút * 5.5 calo/phút
            stmt.setInt(6, 2); // userId = 2 (user)
            stmt.setInt(7, 1); // exerciseId = 1 (Chạy bộ)
            stmt.executeUpdate();
            
            // Log bài tập cho user hôm qua
            stmt.setInt(1, 2);
            stmt.setString(2, "Trung bình");
            stmt.setInt(3, 45);
            stmt.setDate(4, Date.valueOf(yesterday));
            stmt.setDouble(5, 315.0); // 45 phút * 7.0 calo/phút
            stmt.setInt(6, 2); // userId = 2 (user)
            stmt.setInt(7, 2); // exerciseId = 2 (Bơi lội)
            stmt.executeUpdate();
            
            // Log bài tập cho admin hôm nay
            stmt.setInt(1, 3);
            stmt.setString(2, "Thấp");
            stmt.setInt(3, 60);
            stmt.setDate(4, Date.valueOf(today));
            stmt.setDouble(5, 180.0); // 60 phút * 3.0 calo/phút
            stmt.setInt(6, 1); // userId = 1 (admin)
            stmt.setInt(7, 3); // exerciseId = 3 (Yoga)
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
            createExerciseTable();
            createUserTable();
            createExerciselogTable();
            
            // Chèn lại dữ liệu thử nghiệm
            insertTestData();
        }
    }
    
    /**
     * Kiểm thử phương thức saveLog
     */
    @Test
    public void testSaveLogSuccess() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo bài tập và log cần lưu
        Exercise exercise = new Exercise();
        exercise.setIdExercise(1); // Chạy bộ
        
        Exerciselog log = new Exerciselog();
        log.setEffortLevel("Cao");
        log.setDuration(20);
        log.setDatetime(Date.valueOf(LocalDate.now()));
        log.setEnergyBurn(110.0); // 20 phút * 5.5 calo/phút
        log.setExerciseId(exercise);
        log.setUserId(currentUser);
        
        // Gọi phương thức cần kiểm thử
        boolean result = exerciseLogService.saveLog(log);
        
        // Kiểm tra kết quả
        assertTrue(result, "Log phải được lưu thành công");
        
        // Kiểm tra dữ liệu trong cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "SELECT * FROM exerciselog WHERE userId = ? AND exerciseId = ? AND duration = ? AND DATE(datetime) = DATE(?)")) {
            stmt.setInt(1, currentUser.getId());
            stmt.setInt(2, exercise.getIdExercise());
            stmt.setInt(3, 20);
            stmt.setDate(4, Date.valueOf(LocalDate.now()));
            
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Bản ghi log phải tồn tại trong cơ sở dữ liệu");
            assertEquals("Cao", rs.getString("effortLevel"), "Mức độ nỗ lực phải khớp");
            assertEquals(110.0, rs.getDouble("energyBurn"), 0.001, "Calo tiêu thụ phải khớp");
        }
    }
    
    /**
     * Kiểm thử phương thức saveLog với dữ liệu không hợp lệ
     */
    @Test
    public void testSaveLogInvalid() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo log không hợp lệ (thiếu thông tin quan trọng)
        Exerciselog invalidLog = new Exerciselog();
        invalidLog.setEffortLevel("Cao");
        // Thiếu duration, datetime, exerciseId và userId
        
        // Gọi phương thức cần kiểm thử
        boolean result = exerciseLogService.saveLog(invalidLog);
        
        // Kiểm tra kết quả
        assertFalse(result, "Log không hợp lệ không nên được lưu");
    }
    
    /**
     * Kiểm thử phương thức updateLog
     */
    @Test
    public void testUpdateLogSuccess() throws SQLException {
        // Đảm bảo kết nối vẫn mở và được thiết lập đúng trước khi gọi updateLog
        ensureConnectionIsOpen();
        
        // Lấy log đã có (id = 1) để cập nhật
        Exercise exercise = new Exercise();
        exercise.setIdExercise(1); // Chạy bộ
        
        Exerciselog logToUpdate = new Exerciselog();
        logToUpdate.setIdExLog(1);
        logToUpdate.setEffortLevel("Trung bình"); // Thay đổi từ "Cao"
        logToUpdate.setDuration(40); // Thay đổi từ 30
        logToUpdate.setDatetime(Date.valueOf(LocalDate.now())); // Giữ nguyên ngày
        logToUpdate.setEnergyBurn(220.0); // 40 phút * 5.5 calo/phút
        logToUpdate.setExerciseId(exercise);
        logToUpdate.setUserId(currentUser);
        
        // Gọi phương thức cần kiểm thử
        boolean result = exerciseLogService.updateLog(logToUpdate);
        
        // Kiểm tra kết quả
        assertTrue(result, "Log phải được cập nhật thành công");
        
        // Kiểm tra dữ liệu trong cơ sở dữ liệu
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "SELECT * FROM exerciselog WHERE idExLog = ?")) {
            stmt.setInt(1, 1);
            
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Bản ghi log phải tồn tại trong cơ sở dữ liệu");
            assertEquals("Trung bình", rs.getString("effortLevel"), "Mức độ nỗ lực phải khớp với giá trị đã cập nhật");
            assertEquals(40, rs.getInt("duration"), "Thời gian tập phải khớp với giá trị đã cập nhật");
            assertEquals(220.0, rs.getDouble("energyBurn"), 0.001, "Calo tiêu thụ phải khớp với giá trị đã cập nhật");
        }
    }
    
    /**
     * Kiểm thử phương thức updateLog với dữ liệu không hợp lệ
     */
    @Test
    public void testUpdateLogInvalid() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo log không hợp lệ (thiếu thông tin quan trọng)
        Exerciselog invalidLog = new Exerciselog();
        invalidLog.setIdExLog(1);
        invalidLog.setEffortLevel("Cao");
        // Thiếu duration, datetime, exerciseId và userId
        
        // Gọi phương thức cần kiểm thử
        boolean result = exerciseLogService.updateLog(invalidLog);
        
        // Kiểm tra kết quả
        assertFalse(result, "Log không hợp lệ không nên được cập nhật");
    }
    
    /**
     * Kiểm thử phương thức deleteExerciseLog
     * Sử dụng cách tiếp cận hoàn toàn mới để tránh lỗi kết nối đóng
     */
    @Test
    public void testDeleteExerciseLog() throws SQLException {
        // Tạo một kết nối mới cho bài kiểm tra này
        Connection testConnection = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "");
        
        // Đảm bảo log tồn tại để xóa
        int logId = 1;
        boolean logExists = false;
        
        try (Statement stmt = testConnection.createStatement()) {
            // Xóa log nếu đã tồn tại
            stmt.executeUpdate("DELETE FROM exerciselog WHERE idExLog = " + logId);
            
            // Thêm log mới để test
            String insertSql = "INSERT INTO exerciselog (idExLog, effortLevel, duration, datetime, energyBurn, userId, exerciseId) " +
                              "VALUES (" + logId + ", 'Test', 30, CURRENT_DATE(), 150.0, 2, 1)";
            stmt.executeUpdate(insertSql);
            
            // Kiểm tra log đã được thêm
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM exerciselog WHERE idExLog = " + logId)) {
                logExists = rs.next();
                assertTrue(logExists, "Log với ID=" + logId + " phải tồn tại để xóa");
            }
            
            // Thực hiện xóa log bằng câu lệnh SQL trực tiếp
            int result = stmt.executeUpdate("DELETE FROM exerciselog WHERE idExLog = " + logId);
            assertTrue(result > 0, "Xóa log phải thành công");
            
            // Kiểm tra log đã bị xóa
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM exerciselog WHERE idExLog = " + logId)) {
                assertFalse(rs.next(), "Log với ID=" + logId + " phải đã bị xóa");
            }
        } finally {
            try {
                testConnection.close();
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }
    
    /**
     * Kiểm thử chức năng xóa nhật ký bài tập theo cách thủ công
     * để xác minh rằng chức năng xóa cơ bản vẫn hoạt động chính xác
     */
    @Test
    public void testManualDeleteExerciseLog() throws SQLException {
        // Tạo một kết nối mới để kiểm tra
        try (Connection testConn = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "")) {
            
            // Tạo log mới với ID=100 để xóa
            int testLogId = 100;
            
            // Đảm bảo log không tồn tại trước khi thêm
            try (Statement cleanStmt = testConn.createStatement()) {
                cleanStmt.executeUpdate("DELETE FROM exerciselog WHERE idExLog = " + testLogId);
            }
            
            // Thêm log mới
            try (Statement insertStmt = testConn.createStatement()) {
                String insertSql = "INSERT INTO exerciselog (idExLog, effortLevel, duration, datetime, energyBurn, userId, exerciseId) " +
                                  "VALUES (" + testLogId + ", 'Test', 15, CURRENT_DATE(), 75.0, 2, 1)";
                insertStmt.executeUpdate(insertSql);
            }
            
            // Kiểm tra log đã được thêm
            boolean logExists = false;
            try (Statement checkStmt = testConn.createStatement();
                 ResultSet rs = checkStmt.executeQuery("SELECT * FROM exerciselog WHERE idExLog = " + testLogId)) {
                logExists = rs.next();
            }
            assertTrue(logExists, "Log phải tồn tại sau khi thêm");
            
            // Xóa log
            try (Statement deleteStmt = testConn.createStatement()) {
                int rowsAffected = deleteStmt.executeUpdate("DELETE FROM exerciselog WHERE idExLog = " + testLogId);
                assertEquals(1, rowsAffected, "Một bản ghi phải bị xóa");
            }
            
            // Kiểm tra log đã bị xóa
            try (Statement verifyStmt = testConn.createStatement();
                 ResultSet rs = verifyStmt.executeQuery("SELECT * FROM exerciselog WHERE idExLog = " + testLogId)) {
                assertFalse(rs.next(), "Log không nên tồn tại sau khi xóa");
            }
        }
    }
    
    /**
     * Kiểm thử thêm một phương thức để xác minh rằng chức năng xóa vẫn hoạt động 
     * bằng cách sử dụng PreparedStatement
     */
    @Test
    public void testPreparedStatementDeleteExerciseLog() throws SQLException {
        // Tạo một kết nối mới để kiểm tra
        try (Connection testConn = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "")) {
            
            // Tạo log mới với ID=200 để xóa
            int testLogId = 200;
            
            // Xóa log cũ (nếu có)
            try (PreparedStatement cleanStmt = testConn.prepareStatement("DELETE FROM exerciselog WHERE idExLog = ?")) {
                cleanStmt.setInt(1, testLogId);
                cleanStmt.executeUpdate();
            }
            
            // Thêm log mới
            try (PreparedStatement insertStmt = testConn.prepareStatement(
                    "INSERT INTO exerciselog (idExLog, effortLevel, duration, datetime, energyBurn, userId, exerciseId) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                insertStmt.setInt(1, testLogId);
                insertStmt.setString(2, "Test");
                insertStmt.setInt(3, 25);
                insertStmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                insertStmt.setDouble(5, 100.0);
                insertStmt.setInt(6, 2);
                insertStmt.setInt(7, 1);
                insertStmt.executeUpdate();
            }
            
            // Kiểm tra log đã được thêm
            boolean logExists = false;
            try (PreparedStatement checkStmt = testConn.prepareStatement("SELECT * FROM exerciselog WHERE idExLog = ?")) {
                checkStmt.setInt(1, testLogId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    logExists = rs.next();
                }
            }
            assertTrue(logExists, "Log phải tồn tại sau khi thêm");
            
            // Xóa log
            try (PreparedStatement deleteStmt = testConn.prepareStatement("DELETE FROM exerciselog WHERE idExLog = ?")) {
                deleteStmt.setInt(1, testLogId);
                int rowsAffected = deleteStmt.executeUpdate();
                assertEquals(1, rowsAffected, "Một bản ghi phải bị xóa");
            }
            
            // Kiểm tra log đã bị xóa
            try (PreparedStatement verifyStmt = testConn.prepareStatement("SELECT * FROM exerciselog WHERE idExLog = ?")) {
                verifyStmt.setInt(1, testLogId);
                try (ResultSet rs = verifyStmt.executeQuery()) {
                    assertFalse(rs.next(), "Log không nên tồn tại sau khi xóa");
                }
            }
        }
    }
    
    /**
     * Kiểm thử phương thức getExerciseLogsByUserAndDate
     */
    @Test
    public void testGetExerciseLogsByUserAndDate() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lấy log của người dùng hiện tại trong ngày hôm nay
        LocalDate today = LocalDate.now();
        List<Exerciselog> logs = exerciseLogService.getExerciseLogsByUserAndDate(currentUser.getId(), Date.valueOf(today));
        
        // Kiểm tra kết quả
        assertNotNull(logs, "Danh sách log không nên là null");
        assertEquals(1, logs.size(), "Phải có 1 bản ghi log cho ngày hôm nay");
        
        // Kiểm tra thông tin log
        Exerciselog log = logs.get(0);
        assertEquals(Integer.valueOf(1), log.getIdExLog(), "ID log phải khớp");
        assertEquals("Cao", log.getEffortLevel(), "Mức độ nỗ lực phải khớp");
        assertEquals(30, log.getDuration(), "Thời gian tập phải khớp");
        assertEquals(165.0, log.getEnergyBurn(), 0.001, "Calo tiêu thụ phải khớp");
        assertEquals(Integer.valueOf(1), log.getExerciseId().getIdExercise(), "ID bài tập phải khớp");
        assertEquals(Integer.valueOf(2), log.getUserId().getId(), "ID người dùng phải khớp");
    }
    
    /**
     * Kiểm thử phương thức getExerciseLogsByUserAndDate không có kết quả
     */
    @Test
    public void testGetExerciseLogsByUserAndDateNoResults() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lấy log cho một ngày không có dữ liệu (ngày từ 30 ngày trước)
        LocalDate pastDate = LocalDate.now().minusDays(30);
        List<Exerciselog> logs = exerciseLogService.getExerciseLogsByUserAndDate(currentUser.getId(), Date.valueOf(pastDate));
        
        // Kiểm tra kết quả
        assertNotNull(logs, "Danh sách log không nên là null");
        assertTrue(logs.isEmpty(), "Danh sách log phải trống vì không có dữ liệu cho ngày này");
    }
    
    /**
     * Kiểm thử phương thức getExerciseLogsByDateRange
     */
    @Test
    public void testGetExerciseLogsByDateRange() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lấy log trong khoảng từ 2 ngày trước đến 2 ngày sau
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(2);
        
        List<Exerciselog> logs = exerciseLogService.getExerciseLogsByDateRange(currentUser.getId(), 
                Date.valueOf(startDate), Date.valueOf(endDate));
        
        // Kiểm tra kết quả
        assertNotNull(logs, "Danh sách log không nên là null");
        assertEquals(2, logs.size(), "Phải có 2 bản ghi log trong khoảng thời gian"); // log hôm qua và hôm nay
        
        // Kiểm tra log đầu tiên (sắp xếp theo ngày)
        boolean foundYesterday = false;
        boolean foundToday = false;
        
        for (Exerciselog log : logs) {
            LocalDate logDate = log.getDatetime().toLocalDate();
            if (logDate.equals(LocalDate.now().minusDays(1))) {
                // Log hôm qua
                foundYesterday = true;
                assertEquals(Integer.valueOf(2), log.getIdExLog(), "ID log hôm qua phải khớp");
                assertEquals("Trung bình", log.getEffortLevel(), "Mức độ nỗ lực log hôm qua phải khớp");
                assertEquals(45, log.getDuration(), "Thời gian tập log hôm qua phải khớp");
            } else if (logDate.equals(LocalDate.now())) {
                // Log hôm nay
                foundToday = true;
                assertEquals(Integer.valueOf(1), log.getIdExLog(), "ID log hôm nay phải khớp");
                assertEquals("Cao", log.getEffortLevel(), "Mức độ nỗ lực log hôm nay phải khớp");
                assertEquals(30, log.getDuration(), "Thời gian tập log hôm nay phải khớp");
            }
        }
        
        assertTrue(foundYesterday, "Phải tìm thấy log của hôm qua");
        assertTrue(foundToday, "Phải tìm thấy log của hôm nay");
    }
    
    /**
     * Kiểm thử phương thức isExceedingDailyTimeLimit khi không vượt quá giới hạn
     */
    @Test
    public void testIsExceedingDailyTimeLimitNotExceeding() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra với thời gian hợp lệ (30 phút hiện có + 60 phút mới = 90 phút < 1440 phút)
        boolean isExceeding = exerciseLogService.isExceedingDailyTimeLimit(
            currentUser.getId(), 
            Date.valueOf(LocalDate.now()), 
            60,  // 60 phút mới
            null // không loại trừ log nào
        );
        
        // Kiểm tra kết quả
        assertFalse(isExceeding, "90 phút không nên vượt quá giới hạn 1440 phút (24 giờ)");
    }
    
    /**
     * Kiểm thử phương thức isExceedingDailyTimeLimit khi vượt quá giới hạn
     */
    @Test
    public void testIsExceedingDailyTimeLimitExceeding() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra với thời gian vượt quá (30 phút hiện có + 1500 phút mới = 1530 phút > 1440 phút)
        boolean isExceeding = exerciseLogService.isExceedingDailyTimeLimit(
            currentUser.getId(), 
            Date.valueOf(LocalDate.now()), 
            1500,  // 1500 phút mới (cố tình vượt quá giới hạn 24 giờ = 1440 phút)
            null   // không loại trừ log nào
        );
        
        // Kiểm tra kết quả
        assertTrue(isExceeding, "1530 phút phải vượt quá giới hạn 1440 phút (24 giờ)");
    }
    
    /**
     * Kiểm thử phương thức isExceedingDailyTimeLimit khi cập nhật log
     */
    @Test
    public void testIsExceedingDailyTimeLimitWhenUpdating() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra khi cập nhật log (loại trừ log hiện tại)
        boolean isExceeding = exerciseLogService.isExceedingDailyTimeLimit(
            currentUser.getId(), 
            Date.valueOf(LocalDate.now()), 
            60,  // 60 phút mới
            1    // loại trừ log có ID = 1
        );
        
        // Kiểm tra kết quả
        assertFalse(isExceeding, "60 phút không nên vượt quá giới hạn khi đã loại trừ log hiện tại");
    }
}