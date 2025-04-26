package com.milkyway.services;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.NutritionGoal;
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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp cho NutritionGoalService sử dụng H2 database
 */
public class NutritionGoalServiceTest {
    
    private Connection h2Connection;
    private NutritionGoalService nutritionGoalService;
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
        createNutritionGoalTable();
        
        // Tạo và thiết lập người dùng hiện tại cho các bài kiểm tra 
        setupCurrentUser();
        
        // Chèn dữ liệu thử nghiệm
        insertTestData();
        
        // Khởi tạo service
        nutritionGoalService = new NutritionGoalService(h2Connection);
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
                    stmt.execute("TRUNCATE TABLE nutrition_goals");
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
     * Tạo bảng nutrition_goals cho kiểm thử
     */
    private void createNutritionGoalTable() throws SQLException {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS nutrition_goals (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "nutrition_type VARCHAR(50) NOT NULL, " +
                "goal_value DOUBLE NOT NULL, " +
                "unit VARCHAR(20) NOT NULL, " +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_nutritiongoal_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, " +
                "CONSTRAINT uq_user_nutrition_type UNIQUE (user_id, nutrition_type)" +
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
            stmt.execute("TRUNCATE TABLE nutrition_goals");
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
        
        // Chèn mục tiêu dinh dưỡng thử nghiệm
        Date now = new Date();
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO nutrition_goals (id, user_id, nutrition_type, goal_value, unit, created_date, modified_date) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Mục tiêu Calories của admin
            stmt.setInt(1, 1);
            stmt.setInt(2, 1);
            stmt.setString(3, "Calories");
            stmt.setDouble(4, 2000.0);
            stmt.setString(5, "kcal");
            stmt.setTimestamp(6, new java.sql.Timestamp(now.getTime()));
            stmt.setTimestamp(7, new java.sql.Timestamp(now.getTime()));
            stmt.executeUpdate();
            
            // Mục tiêu Protein của admin
            stmt.setInt(1, 2);
            stmt.setInt(2, 1);
            stmt.setString(3, "Protein");
            stmt.setDouble(4, 100.0);
            stmt.setString(5, "g");
            stmt.setTimestamp(6, new java.sql.Timestamp(now.getTime()));
            stmt.setTimestamp(7, new java.sql.Timestamp(now.getTime()));
            stmt.executeUpdate();
            
            // Mục tiêu Calories của user
            stmt.setInt(1, 3);
            stmt.setInt(2, 2);
            stmt.setString(3, "Calories");
            stmt.setDouble(4, 1800.0);
            stmt.setString(5, "kcal");
            stmt.setTimestamp(6, new java.sql.Timestamp(now.getTime()));
            stmt.setTimestamp(7, new java.sql.Timestamp(now.getTime()));
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
            createNutritionGoalTable();
            
            // Cập nhật service
            nutritionGoalService = new NutritionGoalService(h2Connection);
            
            // Chèn lại dữ liệu thử nghiệm
            insertTestData();
        }
    }

    /**
     * Kiểm thử phương thức getGoalsByUserId
     */
    @Test
    public void testGetGoalsByUserId() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử với ID người dùng là admin (ID = 1)
        List<NutritionGoal> adminGoals = nutritionGoalService.getGoalsByUserId(1);
        
        // Kiểm tra kết quả
        assertEquals(2, adminGoals.size(), "Admin phải có 2 mục tiêu dinh dưỡng");
        
        // Kiểm tra mục tiêu Calories
        boolean foundCalories = false;
        boolean foundProtein = false;
        
        for (NutritionGoal goal : adminGoals) {
            if ("Calories".equals(goal.getNutritionType())) {
                foundCalories = true;
                assertEquals(2000.0, goal.getGoalValue(), 0.001, "Giá trị mục tiêu Calories phải khớp");
                assertEquals("kcal", goal.getUnit(), "Đơn vị phải là kcal");
                assertEquals(1, goal.getUserId(), "ID người dùng phải là 1 (admin)");
            } else if ("Protein".equals(goal.getNutritionType())) {
                foundProtein = true;
                assertEquals(100.0, goal.getGoalValue(), 0.001, "Giá trị mục tiêu Protein phải khớp");
                assertEquals("g", goal.getUnit(), "Đơn vị phải là g");
                assertEquals(1, goal.getUserId(), "ID người dùng phải là 1 (admin)");
            }
        }
        
        assertTrue(foundCalories, "Phải có mục tiêu Calories");
        assertTrue(foundProtein, "Phải có mục tiêu Protein");
        
        // Gọi phương thức được kiểm thử với ID người dùng là user (ID = 2)
        List<NutritionGoal> userGoals = nutritionGoalService.getGoalsByUserId(2);
        
        // Kiểm tra kết quả
        assertEquals(1, userGoals.size(), "User phải có 1 mục tiêu dinh dưỡng");
        assertEquals("Calories", userGoals.get(0).getNutritionType(), "Loại dinh dưỡng phải là Calories");
        assertEquals(1800.0, userGoals.get(0).getGoalValue(), 0.001, "Giá trị mục tiêu phải khớp");
        assertEquals("kcal", userGoals.get(0).getUnit(), "Đơn vị phải là kcal");
        assertEquals(2, userGoals.get(0).getUserId(), "ID người dùng phải là 2 (user)");
    }
    
    /**
     * Kiểm thử phương thức getGoalById
     */
    @Test
    public void testGetGoalById() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử với ID mục tiêu = 1 (Calories của admin)
        NutritionGoal goal = nutritionGoalService.getGoalById(1);
        
        // Kiểm tra kết quả
        assertNotNull(goal, "Mục tiêu phải được tìm thấy");
        assertEquals(1, goal.getId(), "ID mục tiêu phải khớp");
        assertEquals("Calories", goal.getNutritionType(), "Loại dinh dưỡng phải là Calories");
        assertEquals(2000.0, goal.getGoalValue(), 0.001, "Giá trị mục tiêu phải khớp");
        assertEquals("kcal", goal.getUnit(), "Đơn vị phải là kcal");
        assertEquals(1, goal.getUserId(), "ID người dùng phải là 1 (admin)");
    }
    
    /**
     * Kiểm thử phương thức getGoalById với ID không tồn tại
     */
    @Test
    public void testGetGoalByIdNotFound() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử với ID không tồn tại
        NutritionGoal goal = nutritionGoalService.getGoalById(999);
        
        // Kiểm tra kết quả
        assertNull(goal, "Mục tiêu không tồn tại phải trả về null");
    }
    
    /**
     * Kiểm thử phương thức getGoalByUserIdAndType
     */
    @Test
    public void testGetGoalByUserIdAndType() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử với ID người dùng = 1 (admin) và loại = Calories
        NutritionGoal goal = nutritionGoalService.getGoalByUserIdAndType(1, "Calories");
        
        // Kiểm tra kết quả
        assertNotNull(goal, "Mục tiêu phải được tìm thấy");
        assertEquals("Calories", goal.getNutritionType(), "Loại dinh dưỡng phải là Calories");
        assertEquals(2000.0, goal.getGoalValue(), 0.001, "Giá trị mục tiêu phải khớp");
        assertEquals("kcal", goal.getUnit(), "Đơn vị phải là kcal");
        assertEquals(1, goal.getUserId(), "ID người dùng phải là 1 (admin)");
    }
    
    /**
     * Kiểm thử phương thức getGoalByUserIdAndType với loại không tồn tại
     */
    @Test
    public void testGetGoalByUserIdAndTypeNotFound() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử với loại không tồn tại
        NutritionGoal goal = nutritionGoalService.getGoalByUserIdAndType(2, "Fat");
        
        // Kiểm tra kết quả
        assertNull(goal, "Mục tiêu không tồn tại phải trả về null");
    }
    
    /**
     * Kiểm thử phương thức addNutritionGoal
     */
    @Test
    public void testAddNutritionGoal() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo mục tiêu mới để thêm
        NutritionGoal newGoal = new NutritionGoal();
        newGoal.setUserId(2); // user
        newGoal.setNutritionType("Carbs");
        newGoal.setGoalValue(250.0);
        newGoal.setUnit("g");
        newGoal.setCreatedDate(new Date());
        newGoal.setModifiedDate(new Date());
        
        // Gọi phương thức được kiểm thử
        int goalId = nutritionGoalService.addNutritionGoal(newGoal);
        
        // Kiểm tra kết quả
        assertTrue(goalId > 0, "ID mục tiêu phải được tạo và lớn hơn 0");
        
        // Kiểm tra mục tiêu có được lưu vào cơ sở dữ liệu không
        NutritionGoal savedGoal = nutritionGoalService.getGoalById(goalId);
        assertNotNull(savedGoal, "Mục tiêu phải tồn tại trong cơ sở dữ liệu");
        assertEquals("Carbs", savedGoal.getNutritionType(), "Loại dinh dưỡng phải là Carbs");
        assertEquals(250.0, savedGoal.getGoalValue(), 0.001, "Giá trị mục tiêu phải khớp");
        assertEquals("g", savedGoal.getUnit(), "Đơn vị phải là g");
        assertEquals(2, savedGoal.getUserId(), "ID người dùng phải là 2 (user)");
    }
    
    /**
     * Kiểm thử phương thức updateNutritionGoal
     */
    @Test
    public void testUpdateNutritionGoal() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lấy mục tiêu hiện có để cập nhật (Calories của user)
        NutritionGoal goal = nutritionGoalService.getGoalById(3);
        assertNotNull(goal, "Mục tiêu phải tồn tại trước khi cập nhật");
        
        // Cập nhật thông tin
        goal.setGoalValue(2200.0); // Tăng giá trị mục tiêu
        goal.setUnit("kcal"); // Giữ nguyên đơn vị
        goal.setModifiedDate(new Date()); // Cập nhật ngày sửa đổi
        
        // Gọi phương thức được kiểm thử
        boolean result = nutritionGoalService.updateNutritionGoal(goal);
        
        // Kiểm tra kết quả
        assertTrue(result, "Cập nhật phải thành công");
        
        // Kiểm tra mục tiêu đã được cập nhật trong cơ sở dữ liệu chưa
        NutritionGoal updatedGoal = nutritionGoalService.getGoalById(3);
        assertNotNull(updatedGoal, "Mục tiêu phải tồn tại sau khi cập nhật");
        assertEquals(2200.0, updatedGoal.getGoalValue(), 0.001, "Giá trị mục tiêu phải được cập nhật");
        assertEquals("kcal", updatedGoal.getUnit(), "Đơn vị phải khớp");
        assertEquals("Calories", updatedGoal.getNutritionType(), "Loại dinh dưỡng không được thay đổi");
        assertEquals(2, updatedGoal.getUserId(), "ID người dùng không được thay đổi");
    }
    
    /**
     * Kiểm thử phương thức deleteNutritionGoal
     */
    @Test
    public void testDeleteNutritionGoal() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra mục tiêu tồn tại trước khi xóa
        NutritionGoal goalBeforeDelete = nutritionGoalService.getGoalById(2); // Protein của admin
        assertNotNull(goalBeforeDelete, "Mục tiêu phải tồn tại trước khi xóa");
        
        // Gọi phương thức được kiểm thử
        boolean result = nutritionGoalService.deleteNutritionGoal(2);
        
        // Kiểm tra kết quả
        assertTrue(result, "Xóa phải thành công");
        
        // Kiểm tra mục tiêu đã bị xóa khỏi cơ sở dữ liệu chưa
        NutritionGoal goalAfterDelete = nutritionGoalService.getGoalById(2);
        assertNull(goalAfterDelete, "Mục tiêu phải bị xóa khỏi cơ sở dữ liệu");
    }
    
    /**
     * Kiểm thử phương thức saveOrUpdateGoal - tạo mới
     */
    @Test
    public void testSaveOrUpdateGoalCreate() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử để tạo mục tiêu mới (Fat cho user)
        nutritionGoalService.saveOrUpdateGoal(2, "Fat", 70.0, "g");
        
        // Kiểm tra mục tiêu mới đã được tạo chưa
        NutritionGoal newGoal = nutritionGoalService.getGoalByUserIdAndType(2, "Fat");
        assertNotNull(newGoal, "Mục tiêu mới phải được tạo");
        assertEquals("Fat", newGoal.getNutritionType(), "Loại dinh dưỡng phải là Fat");
        assertEquals(70.0, newGoal.getGoalValue(), 0.001, "Giá trị mục tiêu phải khớp");
        assertEquals("g", newGoal.getUnit(), "Đơn vị phải là g");
        assertEquals(2, newGoal.getUserId(), "ID người dùng phải là 2 (user)");
    }
    
    /**
     * Kiểm thử phương thức saveOrUpdateGoal - cập nhật
     */
    @Test
    public void testSaveOrUpdateGoalUpdate() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Gọi phương thức được kiểm thử để cập nhật mục tiêu hiện có (Calories của user)
        nutritionGoalService.saveOrUpdateGoal(2, "Calories", 2500.0, "kcal");
        
        // Kiểm tra mục tiêu đã được cập nhật chưa
        NutritionGoal updatedGoal = nutritionGoalService.getGoalByUserIdAndType(2, "Calories");
        assertNotNull(updatedGoal, "Mục tiêu phải tồn tại");
        assertEquals("Calories", updatedGoal.getNutritionType(), "Loại dinh dưỡng phải là Calories");
        assertEquals(2500.0, updatedGoal.getGoalValue(), 0.001, "Giá trị mục tiêu phải được cập nhật");
        assertEquals("kcal", updatedGoal.getUnit(), "Đơn vị phải là kcal");
        assertEquals(2, updatedGoal.getUserId(), "ID người dùng phải là 2 (user)");
    }
}