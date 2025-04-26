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

/**
 * Kiểm thử tích hợp cho ExerciseService sử dụng H2 database
 */
public class ExerciseServiceTest {
    
    private Connection h2Connection;
    private ExerciseService exerciseService;
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
        exerciseService = new ExerciseService();
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
            stmt.execute("TRUNCATE TABLE user");
            stmt.execute("TRUNCATE TABLE exercise");
            
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
     * Kiểm thử phương thức getExercise không có từ khóa (trả về tất cả bài tập)
     */
    @Test
    public void testGetExerciseNoKeyword() throws SQLException {
        // Gọi phương thức được kiểm thử
        List<Exercise> exercises = exerciseService.getExercise(null);
        
        // Kiểm tra kết quả
        assertEquals(3, exercises.size(), "Phải trả về tất cả 3 bài tập");
        
        // Kiểm tra bài tập đầu tiên (mặc định)
        Exercise running = findExerciseByName(exercises, "Chạy bộ");
        assertNotNull(running, "Phải tìm thấy bài tập chạy bộ");
        assertEquals(5.5, running.getCaloriesBurnedPerMin(), 0.001, "Lượng calo phải khớp");
        assertNull(running.getUserId(), "Bài tập chạy bộ không nên có ID người dùng");
        
        // Kiểm tra bài tập thứ hai (của admin)
        Exercise swimming = findExerciseByName(exercises, "Bơi lội");
        assertNotNull(swimming, "Phải tìm thấy bài tập bơi lội");
        assertEquals(7.0, swimming.getCaloriesBurnedPerMin(), 0.001, "Lượng calo phải khớp");
        assertNotNull(swimming.getUserId(), "Bài tập bơi lội phải có ID của admin");
        assertEquals(1, swimming.getUserId().getId(), "Bài tập bơi lội phải thuộc về admin");
        
        // Kiểm tra bài tập thứ ba (của người dùng)
        Exercise yoga = findExerciseByName(exercises, "Yoga");
        assertNotNull(yoga, "Phải tìm thấy bài tập yoga");
        assertEquals(3.0, yoga.getCaloriesBurnedPerMin(), 0.001, "Lượng calo phải khớp");
        assertNotNull(yoga.getUserId(), "Bài tập Yoga phải có ID người dùng");
        assertEquals(2, yoga.getUserId().getId(), "Bài tập Yoga phải thuộc về người dùng hiện tại");
    }
    
    /**
     * Kiểm thử phương thức getExercise với bộ lọc từ khóa
     */
    @Test
    public void testGetExerciseWithKeyword() throws SQLException {
        // Gọi phương thức được kiểm thử với từ khóa "Yo"
        List<Exercise> exercises = exerciseService.getExercise("Yo");
        
        // Kiểm tra kết quả
        assertEquals(1, exercises.size(), "Chỉ nên trả về bài tập Yoga");
        assertEquals("Yoga", exercises.get(0).getExerciseName(), "Tên bài tập phải là Yoga");
        assertEquals(3.0, exercises.get(0).getCaloriesBurnedPerMin(), 0.001, "Lượng calo phải khớp");
        assertNotNull(exercises.get(0).getUserId(), "Bài tập phải có ID người dùng");
        assertEquals(2, exercises.get(0).getUserId().getId(), "Bài tập phải thuộc về người dùng hiện tại");
    }
    
    /**
     * Kiểm thử phương thức saveExercise - lưu bài tập mới thành công
     */
    @Test
    public void testSaveExerciseSuccess() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo bài tập để lưu
        Exercise exercise = new Exercise();
        exercise.setExerciseName("Đạp xe");
        exercise.setCaloriesBurnedPerMin(6.5);
        exercise.setImageExercise("cycling.jpg");
        
        // Thêm người dùng vào bài tập
        exercise.setUserId(currentUser);
        
        System.out.println("Bắt đầu kiểm thử lưu bài tập của người dùng");
        System.out.println("Trước khi lưu: " + exercise.getExerciseName() + 
                         ", Calo: " + exercise.getCaloriesBurnedPerMin() + 
                         ", ID: " + exercise.getIdExercise() +
                         ", Image: " + exercise.getImageExercise() +
                         ", UserID: " + (exercise.getUserId() != null ? exercise.getUserId().getId() : "null"));
        
        // Debug: Kiểm tra xem user có tồn tại trong DB không
        try (Statement checkStmt = h2Connection.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT * FROM user WHERE id = 2")) {
            if (rs.next()) {
                System.out.println("User ID 2 tồn tại trong DB: " + rs.getString("username"));
            } else {
                System.out.println("User ID 2 KHÔNG tồn tại trong DB!");
            }
        }
        
        // Gọi phương thức được kiểm thử
        boolean result = exerciseService.saveExercise(exercise);
        
        System.out.println("Kết quả lưu: " + result);
        System.out.println("Sau khi lưu: ID = " + exercise.getIdExercise());
        
        // Kiểm tra
        assertTrue(result, "Bài tập phải được lưu thành công");
        assertNotNull(exercise.getIdExercise(), "Bài tập phải có ID được tạo");
        
        // Đảm bảo kết nối vẫn mở trước khi kiểm tra dữ liệu
        ensureConnectionIsOpen();
        
        // Debug: Kiểm tra tất cả bài tập trong database
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
        
        // Thay đổi cách tìm bài tập - tìm theo ID thay vì tên
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
        
        // Giữ lại cách kiểm tra theo tên để đảm bảo tính tương thích với bài kiểm thử gốc
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
    
    /**
     * Kiểm thử phương thức saveExercise - lưu bài tập không có người dùng (bài tập mặc định)
     */
    @Test
    public void testSaveExerciseWithNoUser() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo bài tập với constructor đầy đủ thay vì dùng các setter
        Exercise exercise = new Exercise(null, "Đi bộ", "walking.jpg", 3.0);
        
        System.out.println("Bắt đầu kiểm thử lưu bài tập không có người dùng");
        
        // In trạng thái bài tập trước khi lưu
        System.out.println("Trước khi lưu: " + exercise.getExerciseName() + 
                           ", Calo: " + exercise.getCaloriesBurnedPerMin() + 
                           ", ID: " + exercise.getIdExercise() +
                           ", Hình ảnh: " + exercise.getImageExercise());
        
        // Debug: Xem câu lệnh SQL dự kiến
        System.out.println("SQL dự kiến: INSERT INTO exercise (exerciseName, caloriesBurnedPerMin, imageExercise, userId) VALUES ('Đi bộ', 3.0, 'walking.jpg', NULL)");
        
        // Gọi phương thức được kiểm thử
        boolean result = exerciseService.saveExercise(exercise);
        
        // In kết quả và trạng thái sau khi lưu
        System.out.println("Kết quả lưu: " + result);
        System.out.println("Sau khi lưu: ID = " + exercise.getIdExercise());
        
        // Kiểm tra kết quả lưu trước
        if (!result) {
            // Nếu lưu không thành công, kiểm tra database xem có thật sự không có bản ghi nào
            ensureConnectionIsOpen();
            try (Statement stmt = h2Connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM exercise WHERE exerciseName = 'Đi bộ'")) {
                
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("Số lượng bài tập 'Đi bộ' trong database: " + count);
                    // Nếu thực sự có bản ghi thì có thể lỗi nằm ở việc lấy generated key
                    if (count > 0) {
                        System.out.println("Dữ liệu đã được lưu nhưng không lấy được ID");
                    }
                }
            }
        }
        
        // Kiểm tra
        assertTrue(result, "Bài tập phải được lưu thành công");
        assertNotNull(exercise.getIdExercise(), "Bài tập phải có ID được tạo");
        
        // Kiểm tra xem bài tập đã được lưu vào cơ sở dữ liệu chưa
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
    
    /**
     * Kiểm thử phương thức getExerciseByName khi tìm thấy bài tập
     */
    @Test
    public void testGetExerciseByNameFound() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử
        Exercise exercise = exerciseService.getExerciseByName("Chạy bộ");
        
        // Kiểm tra
        assertNotNull(exercise, "Bài tập phải được tìm thấy");
        assertEquals(Integer.valueOf(1), exercise.getIdExercise(), "ID bài tập phải khớp");
        assertEquals("Chạy bộ", exercise.getExerciseName(), "Tên bài tập phải khớp");
        assertEquals(5.5, exercise.getCaloriesBurnedPerMin(), 0.001, "Lượng calo phải khớp");
    }
    
    /**
     * Kiểm thử phương thức getExerciseByName khi không tìm thấy bài tập
     */
    @Test
    public void testGetExerciseByNameNotFound() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử
        Exercise exercise = exerciseService.getExerciseByName("Bài tập không tồn tại");
        
        // Kiểm tra
        assertNull(exercise, "Không nên tìm thấy bài tập");
    }
    
    /**
     * Kiểm thử phương thức getExerciseByName với tên null hoặc rỗng
     */
    @Test
    public void testGetExerciseByNameNullOrEmpty() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra với tên null
        Exercise resultNull = exerciseService.getExerciseByName(null);
        assertNull(resultNull, "Tên null phải trả về null");
        
        // Kiểm tra với tên rỗng
        Exercise resultEmpty = exerciseService.getExerciseByName("");
        assertNull(resultEmpty, "Tên rỗng phải trả về null");
        
        // Kiểm tra với tên chỉ chứa khoảng trắng
        Exercise resultWhitespace = exerciseService.getExerciseByName("   ");
        assertNull(resultWhitespace, "Tên chỉ chứa khoảng trắng phải trả về null");
    }
    
    /**
     * Kiểm thử phương thức isExerciseInUse khi bài tập đang được sử dụng trong exerciselog
     */
    @Test
    public void testIsExerciseInUseWithUsedExercise() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // ID của bài tập "Chạy bộ" đã được sử dụng trong log (xem insertTestData)
        int usedExerciseId = 1;
        
        // Thêm exerciselog sử dụng bài tập này
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO exerciselog (idExLog, effortLevel, duration, datetime, energyBurn, userId, exerciseId) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, 100);
            stmt.setString(2, "Cao");
            stmt.setInt(3, 30);
            stmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            stmt.setDouble(5, 165.0);
            stmt.setInt(6, 2); // currentUser.getId()
            stmt.setInt(7, usedExerciseId);
            stmt.executeUpdate();
        }
        
        // Gọi phương thức được kiểm thử
        boolean isInUse = exerciseService.isExerciseInUse(usedExerciseId);
        
        // Kiểm tra kết quả
        assertTrue(isInUse, "Bài tập đã được sử dụng trong exerciselog nên phải trả về true");
    }
    
    /**
     * Kiểm thử phương thức isExerciseInUse khi bài tập chưa được sử dụng trong exerciselog
     */
    @Test
    public void testIsExerciseInUseWithUnusedExercise() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Xóa tất cả exerciselog liên quan đến bài tập "Bơi lội" (ID = 2) nếu có
        try (PreparedStatement stmt = h2Connection.prepareStatement("DELETE FROM exerciselog WHERE exerciseId = ?")) {
            stmt.setInt(1, 2);
            stmt.executeUpdate();
        }
        
        // Gọi phương thức được kiểm thử
        boolean isInUse = exerciseService.isExerciseInUse(2);
        
        // Kiểm tra kết quả
        assertFalse(isInUse, "Bài tập chưa được sử dụng trong exerciselog nên phải trả về false");
    }
    
    /**
     * Kiểm thử phương thức isExerciseInUse với ID bài tập không tồn tại
     */
    @Test
    public void testIsExerciseInUseWithNonExistentExercise() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử với ID không tồn tại
        boolean isInUse = exerciseService.isExerciseInUse(999);
        
        // Kiểm tra kết quả - phải trả về false vì ID không tồn tại
        assertFalse(isInUse, "Bài tập không tồn tại nên phải trả về false");
    }
    
    /**
     * Phương thức hỗ trợ tìm bài tập theo tên trong danh sách
     */
    private Exercise findExerciseByName(List<Exercise> exercises, String name) {
        for (Exercise exercise : exercises) {
            if (exercise.getExerciseName().equals(name)) {
                return exercise;
            }
        }
        return null;
    }
}