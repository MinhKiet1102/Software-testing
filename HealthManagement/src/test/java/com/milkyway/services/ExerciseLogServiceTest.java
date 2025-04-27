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

public class ExerciseLogServiceTest {
    
    private Connection h2Connection;
    private ExerciseLogService exerciseLogService;
    private User currentUser;
    
    @BeforeEach
    public void setUp() throws Exception {
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "");
        
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("SET TRACE_LEVEL_SYSTEM_OUT=3");
        }
        
        // Thiết lập kết nối thử nghiệm trong JdbcUtils
        JdbcUtils.setTestConnection(h2Connection);
        
        createExerciseTable();
        createUserTable();
        createExerciselogTable();
        setupCurrentUser();
        insertTestData();
        
        exerciseLogService = new ExerciseLogService();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        cleanupCurrentUser();
        
        try {
            if (h2Connection != null && !h2Connection.isClosed()) {
                try (Statement stmt = h2Connection.createStatement()) {
                    stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
                    
                    stmt.execute("TRUNCATE TABLE exerciselog");
                    stmt.execute("TRUNCATE TABLE exercise");
                    stmt.execute("TRUNCATE TABLE user");
                    
                    stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
                } catch (SQLException e) {
                    System.err.println("Lỗi khi xóa dữ liệu bảng: " + e.getMessage());
                }
                
            }
        } finally {
        }
    }
    

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
            
        }
    }
    
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
            
            try {
                stmt.execute("ALTER TABLE user ADD CONSTRAINT unique_username UNIQUE (username)");
                stmt.execute("ALTER TABLE user ADD CONSTRAINT unique_email UNIQUE (email)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists")) {
                    throw e;
                }
            }
            
            try {
                stmt.execute(
                    "ALTER TABLE exercise ADD CONSTRAINT fk_exercise_user " +
                    "FOREIGN KEY (userId) REFERENCES user (id) ON DELETE SET NULL"
                );
            } catch (SQLException e) {
                if (!e.getMessage().contains("already exists")) {
                    throw e;
                }
            }
        }
    }
    
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
    
    private void setupCurrentUser() throws Exception {
        currentUser = new User(2);
        currentUser.setUsername("user");
        currentUser.setRole("USER");
        
        Field field = User.class.getDeclaredField("currentUser");
        field.setAccessible(true);
        field.set(null, currentUser);
    }
    
    private void cleanupCurrentUser() throws Exception {
        Field field = User.class.getDeclaredField("currentUser");
        field.setAccessible(true);
        field.set(null, null);
    }
    
    private void insertTestData() throws SQLException {
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
        
        // Chèn data user
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO user (id, username, password, email, role) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, 1);
            stmt.setString(2, "admin");
            stmt.setString(3, "password");
            stmt.setString(4, "admin@example.com");
            stmt.setString(5, "ADMIN");
            stmt.executeUpdate();
            
            stmt.setInt(1, 2);
            stmt.setString(2, "user");
            stmt.setString(3, "password");
            stmt.setString(4, "user@example.com");
            stmt.setString(5, "USER");
            stmt.executeUpdate();
        }
        
        // Chèn bài tập 
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO exercise (idExercise, exerciseName, imageExercise, caloriesBurnedPerMin, userId) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Bài tập mặc định
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
            stmt.setDouble(5, 165.0); 
            stmt.setInt(6, 2); 
            stmt.setInt(7, 1); 
            stmt.executeUpdate();
            
            stmt.setInt(1, 2);
            stmt.setString(2, "Trung bình");
            stmt.setInt(3, 45);
            stmt.setDate(4, Date.valueOf(yesterday));
            stmt.setDouble(5, 315.0);
            stmt.setInt(6, 2); 
            stmt.setInt(7, 2);
            stmt.executeUpdate();
            
            stmt.setInt(1, 3);
            stmt.setString(2, "Thấp");
            stmt.setInt(3, 60);
            stmt.setDate(4, Date.valueOf(today));
            stmt.setDouble(5, 180.0); 
            stmt.setInt(6, 1); 
            stmt.setInt(7, 3);
            stmt.executeUpdate();
        }
    }

    private void ensureConnectionIsOpen() throws SQLException {
        if (h2Connection == null || h2Connection.isClosed()) {
            System.out.println("Kết nối đã đóng, tạo kết nối mới");
            h2Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "");
            
            // Bật SQL trace để debug
            try (Statement stmt = h2Connection.createStatement()) {
                stmt.execute("SET TRACE_LEVEL_SYSTEM_OUT=3");
            }
            
            JdbcUtils.setTestConnection(h2Connection);

            createExerciseTable();
            createUserTable();
            createExerciselogTable();
            insertTestData();
        }
    }

    @Test
    public void testSaveLogSuccess() throws SQLException {
        ensureConnectionIsOpen();
        
        Exercise exercise = new Exercise();
        exercise.setIdExercise(1); 
        
        Exerciselog log = new Exerciselog();
        log.setEffortLevel("Cao");
        log.setDuration(20);
        log.setDatetime(Date.valueOf(LocalDate.now()));
        log.setEnergyBurn(110.0); 
        log.setExerciseId(exercise);
        log.setUserId(currentUser);
        
        boolean result = exerciseLogService.saveLog(log);
        assertTrue(result, "Log phải được lưu thành công");
        
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
    
    @Test
    public void testSaveLogInvalid() throws SQLException {
        ensureConnectionIsOpen();

        Exerciselog invalidLog = new Exerciselog();
        invalidLog.setEffortLevel("Cao");

        boolean result = exerciseLogService.saveLog(invalidLog);
        assertFalse(result, "Log không hợp lệ không nên được lưu");
    }
    
    @Test
    public void testUpdateLogSuccess() throws SQLException {
        ensureConnectionIsOpen();
        
        Exercise exercise = new Exercise();
        exercise.setIdExercise(1); 
        
        Exerciselog logToUpdate = new Exerciselog();
        logToUpdate.setIdExLog(1);
        logToUpdate.setEffortLevel("Trung bình"); 
        logToUpdate.setDuration(40); 
        logToUpdate.setDatetime(Date.valueOf(LocalDate.now()));
        logToUpdate.setEnergyBurn(220.0);
        logToUpdate.setExerciseId(exercise);
        logToUpdate.setUserId(currentUser);
        
        boolean result = exerciseLogService.updateLog(logToUpdate);
        
        assertTrue(result, "Log phải được cập nhật thành công");

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
    
    @Test
    public void testUpdateLogInvalid() throws SQLException {
        ensureConnectionIsOpen();

        Exerciselog invalidLog = new Exerciselog();
        invalidLog.setIdExLog(1);
        invalidLog.setEffortLevel("Cao");

        boolean result = exerciseLogService.updateLog(invalidLog);

        assertFalse(result, "Log không hợp lệ không nên được cập nhật");
    }
    
    @Test
    public void testDeleteExerciseLog() throws SQLException {
        Connection testConnection = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "");
        
        int logId = 1;
        boolean logExists = false;
        
        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate("DELETE FROM exerciselog WHERE idExLog = " + logId);
            
            String insertSql = "INSERT INTO exerciselog (idExLog, effortLevel, duration, datetime, energyBurn, userId, exerciseId) " +
                              "VALUES (" + logId + ", 'Test', 30, CURRENT_DATE(), 150.0, 2, 1)";
            stmt.executeUpdate(insertSql);
            
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM exerciselog WHERE idExLog = " + logId)) {
                logExists = rs.next();
                assertTrue(logExists, "Log với ID=" + logId + " phải tồn tại để xóa");
            }
            
            int result = stmt.executeUpdate("DELETE FROM exerciselog WHERE idExLog = " + logId);
            assertTrue(result > 0, "Xóa log phải thành công");
            
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
    
    @Test
    public void testManualDeleteExerciseLog() throws SQLException {
        try (Connection testConn = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "")) {
            
            int testLogId = 100;
            
            try (Statement cleanStmt = testConn.createStatement()) {
                cleanStmt.executeUpdate("DELETE FROM exerciselog WHERE idExLog = " + testLogId);
            }

            try (Statement insertStmt = testConn.createStatement()) {
                String insertSql = "INSERT INTO exerciselog (idExLog, effortLevel, duration, datetime, energyBurn, userId, exerciseId) " +
                                  "VALUES (" + testLogId + ", 'Test', 15, CURRENT_DATE(), 75.0, 2, 1)";
                insertStmt.executeUpdate(insertSql);
            }
            
            boolean logExists = false;
            try (Statement checkStmt = testConn.createStatement();
                 ResultSet rs = checkStmt.executeQuery("SELECT * FROM exerciselog WHERE idExLog = " + testLogId)) {
                logExists = rs.next();
            }
            assertTrue(logExists, "Log phải tồn tại sau khi thêm");
            
            try (Statement deleteStmt = testConn.createStatement()) {
                int rowsAffected = deleteStmt.executeUpdate("DELETE FROM exerciselog WHERE idExLog = " + testLogId);
                assertEquals(1, rowsAffected, "Một bản ghi phải bị xóa");
            }
            
            try (Statement verifyStmt = testConn.createStatement();
                 ResultSet rs = verifyStmt.executeQuery("SELECT * FROM exerciselog WHERE idExLog = " + testLogId)) {
                assertFalse(rs.next(), "Log không nên tồn tại sau khi xóa");
            }
        }
    }
    
    @Test
    public void testPreparedStatementDeleteExerciseLog() throws SQLException {
        try (Connection testConn = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "")) {
            
            int testLogId = 200;
            try (PreparedStatement cleanStmt = testConn.prepareStatement("DELETE FROM exerciselog WHERE idExLog = ?")) {
                cleanStmt.setInt(1, testLogId);
                cleanStmt.executeUpdate();
            }

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
            
            boolean logExists = false;
            try (PreparedStatement checkStmt = testConn.prepareStatement("SELECT * FROM exerciselog WHERE idExLog = ?")) {
                checkStmt.setInt(1, testLogId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    logExists = rs.next();
                }
            }
            assertTrue(logExists, "Log phải tồn tại sau khi thêm");
            
            try (PreparedStatement deleteStmt = testConn.prepareStatement("DELETE FROM exerciselog WHERE idExLog = ?")) {
                deleteStmt.setInt(1, testLogId);
                int rowsAffected = deleteStmt.executeUpdate();
                assertEquals(1, rowsAffected, "Một bản ghi phải bị xóa");
            }
            
            try (PreparedStatement verifyStmt = testConn.prepareStatement("SELECT * FROM exerciselog WHERE idExLog = ?")) {
                verifyStmt.setInt(1, testLogId);
                try (ResultSet rs = verifyStmt.executeQuery()) {
                    assertFalse(rs.next(), "Log không nên tồn tại sau khi xóa");
                }
            }
        }
    }
    
    @Test
    public void testGetExerciseLogsByUserAndDate() throws SQLException {
        ensureConnectionIsOpen();

        LocalDate today = LocalDate.now();
        List<Exerciselog> logs = exerciseLogService.getExerciseLogsByUserAndDate(currentUser.getId(), Date.valueOf(today));
        
        assertNotNull(logs, "Danh sách log không nên là null");
        assertEquals(1, logs.size(), "Phải có 1 bản ghi log cho ngày hôm nay");

        Exerciselog log = logs.get(0);
        assertEquals(Integer.valueOf(1), log.getIdExLog(), "ID log phải khớp");
        assertEquals("Cao", log.getEffortLevel(), "Mức độ nỗ lực phải khớp");
        assertEquals(30, log.getDuration(), "Thời gian tập phải khớp");
        assertEquals(165.0, log.getEnergyBurn(), 0.001, "Calo tiêu thụ phải khớp");
        assertEquals(Integer.valueOf(1), log.getExerciseId().getIdExercise(), "ID bài tập phải khớp");
        assertEquals(Integer.valueOf(2), log.getUserId().getId(), "ID người dùng phải khớp");
    }
    
    @Test
    public void testGetExerciseLogsByUserAndDateNoResults() throws SQLException {
        ensureConnectionIsOpen();
        
        LocalDate pastDate = LocalDate.now().minusDays(30);
        List<Exerciselog> logs = exerciseLogService.getExerciseLogsByUserAndDate(currentUser.getId(), Date.valueOf(pastDate));

        assertNotNull(logs, "Danh sách log không nên là null");
        assertTrue(logs.isEmpty(), "Danh sách log phải trống vì không có dữ liệu cho ngày này");
    }
    

    @Test
    public void testGetExerciseLogsByDateRange() throws SQLException {
        ensureConnectionIsOpen();

        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(2);
        
        List<Exerciselog> logs = exerciseLogService.getExerciseLogsByDateRange(currentUser.getId(), 
                Date.valueOf(startDate), Date.valueOf(endDate));
        
        assertNotNull(logs, "Danh sách log không nên là null");
        assertEquals(2, logs.size(), "Phải có 2 bản ghi log trong khoảng thời gian"); 

        boolean foundYesterday = false;
        boolean foundToday = false;
        
        for (Exerciselog log : logs) {
            LocalDate logDate = log.getDatetime().toLocalDate();
            if (logDate.equals(LocalDate.now().minusDays(1))) {

                foundYesterday = true;
                assertEquals(Integer.valueOf(2), log.getIdExLog(), "ID log hôm qua phải khớp");
                assertEquals("Trung bình", log.getEffortLevel(), "Mức độ nỗ lực log hôm qua phải khớp");
                assertEquals(45, log.getDuration(), "Thời gian tập log hôm qua phải khớp");
            } else if (logDate.equals(LocalDate.now())) {

                foundToday = true;
                assertEquals(Integer.valueOf(1), log.getIdExLog(), "ID log hôm nay phải khớp");
                assertEquals("Cao", log.getEffortLevel(), "Mức độ nỗ lực log hôm nay phải khớp");
                assertEquals(30, log.getDuration(), "Thời gian tập log hôm nay phải khớp");
            }
        }
        
        assertTrue(foundYesterday, "Phải tìm thấy log của hôm qua");
        assertTrue(foundToday, "Phải tìm thấy log của hôm nay");
    }
    
   
    @Test
    public void testIsExceedingDailyTimeLimitNotExceeding() throws SQLException {
        ensureConnectionIsOpen();
        
        boolean isExceeding = exerciseLogService.isExceedingDailyTimeLimit(
            currentUser.getId(), Date.valueOf(LocalDate.now()), 60, null);
        assertFalse(isExceeding, "90 phút không nên vượt quá giới hạn 1440 phút (24 giờ)");
    }
    

    @Test
    public void testIsExceedingDailyTimeLimitExceeding() throws SQLException {
        ensureConnectionIsOpen();
        
        boolean isExceeding = exerciseLogService.isExceedingDailyTimeLimit(
            currentUser.getId(), 
            Date.valueOf(LocalDate.now()), 1500,  null);
        
        assertTrue(isExceeding, "1530 phút phải vượt quá giới hạn 1440 phút (24 giờ)");
    }

    @Test
    public void testIsExceedingDailyTimeLimitWhenUpdating() throws SQLException {
        ensureConnectionIsOpen();
 
        boolean isExceeding = exerciseLogService.isExceedingDailyTimeLimit(
            currentUser.getId(), 
            Date.valueOf(LocalDate.now()), 60,  1    );
        
        assertFalse(isExceeding, "60 phút không nên vượt quá giới hạn khi đã loại trừ log hiện tại");
    }
}