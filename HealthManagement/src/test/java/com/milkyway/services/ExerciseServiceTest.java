package com.milkyway.services;

import com.milkyway.pojo.Exercise;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExerciseServiceTest {
    
    private Connection h2Connection;
    private ExerciseService exerciseService;
    private User currentUser;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Khởi tạo kết nối H2 database và cấu hình MODE=MySQL để tương thích
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "");
        
        // Bật SQL trace để debug các câu lệnh SQL
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("SET TRACE_LEVEL_SYSTEM_OUT=3");
        }
        
        JdbcUtils.setTestConnection(h2Connection);

        createExerciseTable();
        createUserTable();
        createExerciselogTable();
        setupCurrentUser();
        insertTestData();
        
        exerciseService = new ExerciseService();
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
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            
            stmt.execute("TRUNCATE TABLE user");
            stmt.execute("TRUNCATE TABLE exercise");
            
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
        
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
        
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO exercise (idExercise, exerciseName, imageExercise, caloriesBurnedPerMin, userId) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, 1);
            stmt.setString(2, "Chạy bộ");
            stmt.setString(3, "run.jpg");
            stmt.setDouble(4, 5.5);
            stmt.setNull(5, java.sql.Types.INTEGER);
            stmt.executeUpdate();
            
            stmt.setInt(1, 2);
            stmt.setString(2, "Bơi lội");
            stmt.setString(3, "swim.jpg");
            stmt.setDouble(4, 7.0);
            stmt.setInt(5, 1);
            stmt.executeUpdate();
            
            stmt.setInt(1, 3);
            stmt.setString(2, "Yoga");
            stmt.setString(3, "yoga.jpg");
            stmt.setDouble(4, 3.0);
            stmt.setInt(5, 2);
            stmt.executeUpdate();
        }
    }
    

    private void ensureConnectionIsOpen() throws SQLException {
        if (h2Connection == null || h2Connection.isClosed()) {
            System.out.println("Kết nối đã đóng, tạo kết nối mới");
            h2Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;NON_KEYWORDS=USER", "sa", "");
            
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
    public void testGetExerciseNoKeyword() throws SQLException {
        List<Exercise> exercises = exerciseService.getExercise(null);
        
        assertEquals(3, exercises.size(), "Phải trả về tất cả 3 bài tập");
        
        Exercise running = findExerciseByName(exercises, "Chạy bộ");
        assertNotNull(running, "Phải tìm thấy bài tập chạy bộ");
        assertEquals(5.5, running.getCaloriesBurnedPerMin(), 0.001, "Lượng calo phải khớp");
        assertNull(running.getUserId(), "Bài tập chạy bộ không nên có ID người dùng");
        
        Exercise swimming = findExerciseByName(exercises, "Bơi lội");
        assertNotNull(swimming, "Phải tìm thấy bài tập bơi lội");
        assertEquals(7.0, swimming.getCaloriesBurnedPerMin(), 0.001, "Lượng calo phải khớp");
        assertNotNull(swimming.getUserId(), "Bài tập bơi lội phải có ID của admin");
        assertEquals(1, swimming.getUserId().getId(), "Bài tập bơi lội phải thuộc về admin");
        
        Exercise yoga = findExerciseByName(exercises, "Yoga");
        assertNotNull(yoga, "Phải tìm thấy bài tập yoga");
        assertEquals(3.0, yoga.getCaloriesBurnedPerMin(), 0.001, "Lượng calo phải khớp");
        assertNotNull(yoga.getUserId(), "Bài tập Yoga phải có ID người dùng");
        assertEquals(2, yoga.getUserId().getId(), "Bài tập Yoga phải thuộc về người dùng hiện tại");
    }
    
    @Test
    public void testGetExerciseWithKeyword() throws SQLException {
        List<Exercise> exercises = exerciseService.getExercise("Yo");
        
        assertEquals(1, exercises.size(), "Chỉ nên trả về bài tập Yoga");
        assertEquals("Yoga", exercises.get(0).getExerciseName(), "Tên bài tập phải là Yoga");
        assertEquals(3.0, exercises.get(0).getCaloriesBurnedPerMin(), 0.001, "Lượng calo phải khớp");
        assertNotNull(exercises.get(0).getUserId(), "Bài tập phải có ID người dùng");
        assertEquals(2, exercises.get(0).getUserId().getId(), "Bài tập phải thuộc về người dùng hiện tại");
    }
    

    @Test
    public void testSaveExerciseSuccess() throws SQLException {
        ensureConnectionIsOpen();
        
        Exercise exercise = new Exercise();
        exercise.setExerciseName("Đạp xe");
        exercise.setCaloriesBurnedPerMin(6.5);
        exercise.setImageExercise("cycling.jpg");
        
        exercise.setUserId(currentUser);
        
        System.out.println("Bắt đầu kiểm thử lưu bài tập của người dùng");
        System.out.println("Trước khi lưu: " + exercise.getExerciseName() + 
                         ", Calo: " + exercise.getCaloriesBurnedPerMin() + 
                         ", ID: " + exercise.getIdExercise() +
                         ", Image: " + exercise.getImageExercise() +
                         ", UserID: " + (exercise.getUserId() != null ? exercise.getUserId().getId() : "null"));
        
        try (Statement checkStmt = h2Connection.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT * FROM user WHERE id = 2")) {
            if (rs.next()) {
                System.out.println("User ID 2 tồn tại trong DB: " + rs.getString("username"));
            } else {
                System.out.println("User ID 2 KHÔNG tồn tại trong DB!");
            }
        }
        
        boolean result = exerciseService.saveExercise(exercise);
        
        System.out.println("Kết quả lưu: " + result);
        System.out.println("Sau khi lưu: ID = " + exercise.getIdExercise());
        
        assertTrue(result, "Bài tập phải được lưu thành công");
        assertNotNull(exercise.getIdExercise(), "Bài tập phải có ID được tạo");
        
        ensureConnectionIsOpen();
        
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM exercise")) {
            
            System.out.println("Danh sách tất cả bài tập trong database:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("idExercise") + 
                                  ", Tên: " + rs.getString("exerciseName") + 
                                  ", Calo: " + rs.getDouble("caloriesBurnedPerMin") + 
                                  ", Image: " + rs.getString("imageExercise") +
                                  ", UserID: " + rs.getObject("userId"));
            }
        }
        
        if (exercise.getIdExercise() != null) {
            try (PreparedStatement stmt = h2Connection.prepareStatement(
                    "SELECT * FROM exercise WHERE idExercise = ?")) {
                    
                stmt.setInt(1, exercise.getIdExercise());
                ResultSet rs = stmt.executeQuery();
                
                boolean found = rs.next();
                System.out.println("Tìm thấy bài tập theo ID " + exercise.getIdExercise() + ": " + found);
                
                if (found) {
                    System.out.println("DB record: ID=" + rs.getInt("idExercise") + 
                                      ", Tên=" + rs.getString("exerciseName") + 
                                      ", Calo=" + rs.getDouble("caloriesBurnedPerMin") + 
                                      ", Image=" + rs.getString("imageExercise") +
                                      ", userId=" + rs.getObject("userId"));
                    
                    assertTrue(found, "Bài tập phải tồn tại trong cơ sở dữ liệu");
                    assertEquals("Đạp xe", rs.getString("exerciseName"), "Tên bài tập phải khớp");
                    assertEquals(6.5, rs.getDouble("caloriesBurnedPerMin"), 0.001, "Lượng calo phải khớp");
                    assertEquals(2, rs.getInt("userId"), "ID người dùng phải khớp");
                }
            }
        }
        
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "SELECT * FROM exercise WHERE exerciseName = ?")) {
                
            stmt.setString(1, "Đạp xe");
            ResultSet rs = stmt.executeQuery();
            
            boolean found = rs.next();
            System.out.println("Tìm thấy bài tập theo tên 'Đạp xe': " + found);
            
            if (found) {
                System.out.println("DB record: ID=" + rs.getInt("idExercise") + 
                                  ", Tên=" + rs.getString("exerciseName") + 
                                  ", Calo=" + rs.getDouble("caloriesBurnedPerMin") + 
                                  ", Image=" + rs.getString("imageExercise") +
                                  ", userId=" + rs.getObject("userId"));
            }
            
            assertTrue(found, "Bài tập phải tồn tại trong cơ sở dữ liệu");
            assertEquals("Đạp xe", rs.getString("exerciseName"), "Tên bài tập phải khớp");
            assertEquals(6.5, rs.getDouble("caloriesBurnedPerMin"), 0.001, "Lượng calo phải khớp");
            assertEquals(2, rs.getInt("userId"), "ID người dùng phải khớp");
        }
    }
    

    @Test
    public void testSaveExerciseWithNoUser() throws SQLException {
        ensureConnectionIsOpen();
        
        Exercise exercise = new Exercise(null, "Đi bộ", "walking.jpg", 3.0);
        
        System.out.println("Bắt đầu kiểm thử lưu bài tập không có người dùng");
        
        System.out.println("Trước khi lưu: " + exercise.getExerciseName() + 
                           ", Calo: " + exercise.getCaloriesBurnedPerMin() + 
                           ", ID: " + exercise.getIdExercise() +
                           ", Hình ảnh: " + exercise.getImageExercise());
        
        System.out.println("SQL dự kiến: INSERT INTO exercise (exerciseName, caloriesBurnedPerMin, imageExercise, userId) VALUES ('Đi bộ', 3.0, 'walking.jpg', NULL)");
        
        boolean result = exerciseService.saveExercise(exercise);
        
        System.out.println("Kết quả lưu: " + result);
        System.out.println("Sau khi lưu: ID = " + exercise.getIdExercise());
        
        if (!result) {
            ensureConnectionIsOpen();
            try (Statement stmt = h2Connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM exercise WHERE exerciseName = 'Đi bộ'")) {
                
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("Số lượng bài tập 'Đi bộ' trong database: " + count);
                    if (count > 0) {
                        System.out.println("Dữ liệu đã được lưu nhưng không lấy được ID");
                    }
                }
            }
        }
        
        assertTrue(result, "Bài tập phải được lưu thành công");
        assertNotNull(exercise.getIdExercise(), "Bài tập phải có ID được tạo");
        
        ensureConnectionIsOpen();
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "SELECT * FROM exercise WHERE exerciseName = ?")) {
                
            stmt.setString(1, "Đi bộ");
            ResultSet rs = stmt.executeQuery();
            
            boolean found = rs.next();
            System.out.println("Tìm thấy trong DB: " + found);
            
            if (found) {
                System.out.println("DB record: ID=" + rs.getInt("idExercise") + 
                                  ", Tên=" + rs.getString("exerciseName") + 
                                  ", Calo=" + rs.getDouble("caloriesBurnedPerMin") + 
                                  ", Image=" + rs.getString("imageExercise") +
                                  ", userId=" + rs.getObject("userId"));
            }
            
            assertTrue(found, "Bài tập phải tồn tại trong cơ sở dữ liệu");
            assertEquals("Đi bộ", rs.getString("exerciseName"), "Tên bài tập phải khớp");
            assertEquals(3.0, rs.getDouble("caloriesBurnedPerMin"), 0.001, "Lượng calo phải khớp");
            assertTrue(rs.getObject("userId") == null, "ID người dùng phải là NULL");
        }
    }
    

    @Test
    public void testGetExerciseByNameFound() throws SQLException {
        ensureConnectionIsOpen();
        
        Exercise exercise = exerciseService.getExerciseByName("Chạy bộ");
        
        assertNotNull(exercise, "Bài tập phải được tìm thấy");
        assertEquals(Integer.valueOf(1), exercise.getIdExercise(), "ID bài tập phải khớp");
        assertEquals("Chạy bộ", exercise.getExerciseName(), "Tên bài tập phải khớp");
        assertEquals(5.5, exercise.getCaloriesBurnedPerMin(), 0.001, "Lượng calo phải khớp");
    }
    
    @Test
    public void testGetExerciseByNameNotFound() throws SQLException {
        ensureConnectionIsOpen();
        
        Exercise exercise = exerciseService.getExerciseByName("Bài tập không tồn tại");
        
        assertNull(exercise, "Không nên tìm thấy bài tập");
    }
    

    @Test
    public void testGetExerciseByNameNullOrEmpty() throws SQLException {
        ensureConnectionIsOpen();
        
        Exercise resultNull = exerciseService.getExerciseByName(null);
        assertNull(resultNull, "Tên null phải trả về null");
        
        Exercise resultEmpty = exerciseService.getExerciseByName("");
        assertNull(resultEmpty, "Tên rỗng phải trả về null");
        
        Exercise resultWhitespace = exerciseService.getExerciseByName("   ");
        assertNull(resultWhitespace, "Tên chỉ chứa khoảng trắng phải trả về null");
    }
    
    @Test
    public void testIsExerciseInUseWithUsedExercise() throws SQLException {
        ensureConnectionIsOpen();
        
        int usedExerciseId = 1;
        
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO exerciselog (idExLog, effortLevel, duration, datetime, energyBurn, userId, exerciseId) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, 100);
            stmt.setString(2, "Cao");
            stmt.setInt(3, 30);
            stmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            stmt.setDouble(5, 165.0);
            stmt.setInt(6, 2);
            stmt.setInt(7, usedExerciseId);
            stmt.executeUpdate();
        }
        
        boolean isInUse = exerciseService.isExerciseInUse(usedExerciseId);

        assertTrue(isInUse, "Bài tập đã được sử dụng trong exerciselog nên phải trả về true");
    }
    
    @Test
    public void testIsExerciseInUseWithUnusedExercise() throws SQLException {
        ensureConnectionIsOpen();
        
        try (PreparedStatement stmt = h2Connection.prepareStatement("DELETE FROM exerciselog WHERE exerciseId = ?")) {
            stmt.setInt(1, 2);
            stmt.executeUpdate();
        }
        
        boolean isInUse = exerciseService.isExerciseInUse(2);
        
        assertFalse(isInUse, "Bài tập chưa được sử dụng trong exerciselog nên phải trả về false");
    }

    @Test
    public void testIsExerciseInUseWithNonExistentExercise() throws SQLException {
        ensureConnectionIsOpen();
        
        boolean isInUse = exerciseService.isExerciseInUse(999);
        
        assertFalse(isInUse, "Bài tập không tồn tại nên phải trả về false");
    }
    

    private Exercise findExerciseByName(List<Exercise> exercises, String name) {
        for (Exercise exercise : exercises) {
            if (exercise.getExerciseName().equals(name)) {
                return exercise;
            }
        }
        return null;
    }
}