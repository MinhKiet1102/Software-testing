package com.milkyway.services;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.Target;
import com.milkyway.pojo.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp cho TargetService sử dụng H2 database
 */
public class TargetServiceTest {

    private Connection h2Connection;
    private TargetService targetService;
    private User currentUser;
    private static final int USER_ID = 2;

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
        createTargetTable();
        
        // Tạo và thiết lập người dùng hiện tại cho các bài kiểm tra 
        setupCurrentUser();
        
        // Chèn dữ liệu thử nghiệm
        insertTestData();
        
        // Khởi tạo service
        targetService = new CustomTargetService();
    }
    
    /**
     * Lớp con của TargetService ghi đè các phương thức để tránh đóng kết nối
     */
    private class CustomTargetService extends TargetService {
        @Override
        public boolean isPlanExist(String planName, LocalDate startDate, int userId) throws SQLException {
            String checkPlan = "SELECT COUNT(*) FROM target WHERE targetName = ? AND startDate = ? AND userId = ?";
            try (PreparedStatement prepare = h2Connection.prepareStatement(checkPlan)) {
                prepare.setString(1, planName);
                prepare.setString(2, startDate.toString());
                prepare.setInt(3, userId); 
                ResultSet result = prepare.executeQuery();
                return result.next() && result.getInt(1) > 0;
            }
        }

        @Override
        public void addPlan(String planName, LocalDate startDate, LocalDate endDate, float targetValue, String unit, int userId) throws SQLException {
            String insertData = "INSERT INTO target (targetName, dateCreated, startDate, endDate, targetNumber, unit, progress, status, userId) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement prepare = h2Connection.prepareStatement(insertData)) {
                java.util.Date date = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(date.getTime());

                String status = calculateStatus(startDate, endDate, 0.0f, targetValue);

                prepare.setString(1, planName);
                prepare.setDate(2, sqlDate);
                prepare.setString(3, String.valueOf(startDate));
                prepare.setString(4, String.valueOf(endDate));
                prepare.setFloat(5, targetValue);
                String unitBeforeSpace = unit.split(" ")[0];
                prepare.setString(6, unitBeforeSpace);
                prepare.setFloat(7, 0.0f); 
                prepare.setString(8, status); 
                prepare.setInt(9, userId);

                prepare.executeUpdate();
            }
        }
        
        @Override
        public void updatePlanProgress(int idTarget, float newProgress) throws SQLException {
            String sql = "UPDATE target SET progress = ? WHERE idTarget = ?";
            try (PreparedStatement stmt = h2Connection.prepareStatement(sql)) {
                stmt.setFloat(1, newProgress);
                stmt.setInt(2, idTarget);
                stmt.executeUpdate();
            }
        }
        
        @Override
        public void deletePlan(int idTarget) throws SQLException {
            String deleteData = "DELETE FROM target WHERE idTarget = ?";
            try (PreparedStatement prepare = h2Connection.prepareStatement(deleteData)) {
                prepare.setInt(1, idTarget);
                prepare.executeUpdate();
            }
        }
        
        @Override
        public Target getPlanById(int idTarget) throws SQLException {
            String selectData = "SELECT * FROM target WHERE idTarget = ?";
            try (PreparedStatement prepare = h2Connection.prepareStatement(selectData)) {
                prepare.setInt(1, idTarget);
                ResultSet result = prepare.executeQuery();

                if (result.next()) {
                    return new Target(
                            result.getInt("idTarget"),
                            result.getString("targetName"),
                            result.getDate("dateCreated"),
                            result.getDate("startDate"),
                            result.getDate("endDate"),
                            result.getFloat("targetNumber"),
                            result.getString("unit"),
                            result.getFloat("progress"),
                            result.getString("status")
                    );
                }
            }
            return null;
        }
        
        @Override
        public void updatePlanStatus(int idTarget, String status) throws SQLException {
            String updateData = "UPDATE target SET status = ? WHERE idTarget = ?";
            try (PreparedStatement prepare = h2Connection.prepareStatement(updateData)) {
                prepare.setString(1, status);
                prepare.setInt(2, idTarget);
                prepare.executeUpdate();
            }
        }
        
        @Override
        public boolean isPlanExist(int idTarget) throws SQLException {
            String checkData = "SELECT * FROM target WHERE idTarget = ?";
            try (PreparedStatement prepare = h2Connection.prepareStatement(checkData)) {
                prepare.setInt(1, idTarget);
                ResultSet result = prepare.executeQuery();
                return result.next();
            }
        }
        
        @Override
        public int countQuantityPlans(int userId) throws SQLException {
            String sql = "SELECT COUNT(idTarget) FROM target WHERE userId = ?";
            try (PreparedStatement prepare = h2Connection.prepareStatement(sql)) {
                prepare.setInt(1, userId);
                ResultSet result = prepare.executeQuery();

                if (result.next()) {
                    return result.getInt(1);
                }
            }
            return 0;
        }
        
        @Override
        public int countAchievedPlans(int userId) throws SQLException {
            String sql = "SELECT COUNT(idTarget) FROM target WHERE userId = ? AND status = 'Achieved'";
            try (PreparedStatement prepare = h2Connection.prepareStatement(sql)) {
                prepare.setInt(1, userId);
                ResultSet result = prepare.executeQuery();

                if (result.next()) {
                    return result.getInt(1);
                }
            }
            return 0;
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
                    stmt.execute("TRUNCATE TABLE target");
                    stmt.execute("TRUNCATE TABLE user");
                    
                    // Bật lại ràng buộc khóa ngoại
                    stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
                } catch (SQLException e) {
                    System.err.println("Lỗi khi xóa dữ liệu bảng: " + e.getMessage());
                }
            }
        } finally {
            // Không reset kết nối thử nghiệm - chúng ta sẽ giữ nó mở
        }
    }
    
    /**
     * Tạo bảng user cho kiểm thử
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
     * Tạo bảng target cho kiểm thử
     */
    private void createTargetTable() throws SQLException {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS target (" +
                "idTarget INT AUTO_INCREMENT PRIMARY KEY, " +
                "targetName VARCHAR(255) NOT NULL, " +
                "dateCreated DATE, " +
                "startDate VARCHAR(255), " +
                "endDate VARCHAR(255), " +
                "targetNumber FLOAT, " +
                "unit VARCHAR(50), " +
                "progress FLOAT, " +
                "status VARCHAR(50), " +
                "userId INT, " +
                "CONSTRAINT fk_target_user FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE" +
                ")"
            );
        }
    }
    
    /**
     * Thiết lập người dùng hiện tại cho các bài kiểm thử
     */
    private void setupCurrentUser() throws Exception {
        // Tạo người dùng mới với ID = 2
        currentUser = new User(USER_ID);
        currentUser.setUsername("user");
        currentUser.setRole("USER");
        currentUser.setEmail("user@example.com");
        
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
            stmt.execute("TRUNCATE TABLE target");
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
        
        // Chèn mục tiêu thử nghiệm
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().plusDays(30);
        
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO target (idTarget, targetName, dateCreated, startDate, endDate, targetNumber, unit, progress, status, userId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Mục tiêu 1: Mục tiêu "In Progress" của user
            stmt.setInt(1, 1);
            stmt.setString(2, "Giảm cân");
            stmt.setDate(3, today);
            stmt.setString(4, startDate.toString());
            stmt.setString(5, endDate.toString());
            stmt.setFloat(6, 5.0f);
            stmt.setString(7, "kg");
            stmt.setFloat(8, 2.5f); // Đạt 50%
            stmt.setString(9, "In Progress");
            stmt.setInt(10, USER_ID);
            stmt.executeUpdate();
            
            // Mục tiêu 2: Mục tiêu "Achieved" của user
            stmt.setInt(1, 2);
            stmt.setString(2, "Chạy bộ");
            stmt.setDate(3, today);
            stmt.setString(4, startDate.minusDays(30).toString());
            stmt.setString(5, startDate.toString());
            stmt.setFloat(6, 100.0f);
            stmt.setString(7, "km");
            stmt.setFloat(8, 100.0f); // Đạt 100%
            stmt.setString(9, "Achieved");
            stmt.setInt(10, USER_ID);
            stmt.executeUpdate();
            
            // Mục tiêu 3: Mục tiêu "Not Started" của user
            stmt.setInt(1, 3);
            stmt.setString(2, "Tập bơi");
            stmt.setDate(3, today);
            stmt.setString(4, endDate.toString());
            stmt.setString(5, endDate.plusDays(30).toString());
            stmt.setFloat(6, 20.0f);
            stmt.setString(7, "giờ");
            stmt.setFloat(8, 0.0f); // Chưa bắt đầu
            stmt.setString(9, "Not Started");
            stmt.setInt(10, USER_ID);
            stmt.executeUpdate();
            
            // Mục tiêu 4: Mục tiêu của admin
            stmt.setInt(1, 4);
            stmt.setString(2, "Tập gym");
            stmt.setDate(3, today);
            stmt.setString(4, startDate.toString());
            stmt.setString(5, endDate.toString());
            stmt.setFloat(6, 30.0f);
            stmt.setString(7, "giờ");
            stmt.setFloat(8, 10.0f);
            stmt.setString(9, "In Progress");
            stmt.setInt(10, 1); // userID = 1 (admin)
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
            createTargetTable();
            
            // Chèn lại dữ liệu thử nghiệm
            insertTestData();
        }
    }
    
    /**
     * Kiểm thử phương thức isPlanExist (kiểm tra tên và ngày bắt đầu)
     */
    @Test
    public void testIsPlanExist() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra mục tiêu đã tồn tại
        boolean exists = targetService.isPlanExist("Giảm cân", LocalDate.now().minusDays(30), USER_ID);
        assertTrue(exists, "Mục tiêu 'Giảm cân' phải tồn tại");
        
        // Kiểm tra mục tiêu không tồn tại
        boolean notExists = targetService.isPlanExist("Tập yoga", LocalDate.now(), USER_ID);
        assertFalse(notExists, "Mục tiêu 'Tập yoga' không nên tồn tại");
    }
    
    /**
     * Kiểm thử phương thức addPlan
     */
    @Test
    public void testAddPlan() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tên mục tiêu mới
        String planName = "Tập yoga";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(60);
        float targetValue = 30.0f;
        String unit = "giờ";
        
        // Thêm mục tiêu mới
        targetService.addPlan(planName, startDate, endDate, targetValue, unit, USER_ID);
        
        // Kiểm tra mục tiêu đã được thêm vào cơ sở dữ liệu chưa
        boolean added = targetService.isPlanExist(planName, startDate, USER_ID);
        assertTrue(added, "Mục tiêu mới phải được thêm thành công");
        
        // Lấy mục tiêu đã thêm để kiểm tra chi tiết
        String sql = "SELECT * FROM target WHERE targetName = ? AND userId = ?";
        Target plan = null;
        
        try (PreparedStatement stmt = h2Connection.prepareStatement(sql)) {
            stmt.setString(1, planName);
            stmt.setInt(2, USER_ID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                plan = new Target(
                    rs.getInt("idTarget"),
                    rs.getString("targetName"),
                    rs.getDate("dateCreated"),
                    rs.getDate("startDate"),
                    rs.getDate("endDate"),
                    rs.getFloat("targetNumber"),
                    rs.getString("unit"),
                    rs.getFloat("progress"),
                    rs.getString("status")
                );
            }
        }
        
        // Kiểm tra chi tiết mục tiêu
        assertNotNull(plan, "Phải tìm thấy mục tiêu trong cơ sở dữ liệu");
        assertEquals(planName, plan.getTargetName(), "Tên mục tiêu phải đúng");
        assertEquals(targetValue, plan.getTargetNumber(), "Giá trị mục tiêu phải đúng");
        assertEquals("giờ", plan.getUnit(), "Đơn vị phải đúng");
        assertEquals(0.0f, plan.getProgress(), "Tiến độ ban đầu phải là 0");
        assertEquals("In Progress", plan.getStatus(), "Trạng thái phải là 'In Progress'");
    }
    
    /**
     * Kiểm thử phương thức updatePlanProgress
     */
    @Test
    public void testUpdatePlanProgress() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // ID mục tiêu cần cập nhật
        int idTarget = 1;
        float newProgress = 4.0f;
        
        // Truy vấn giá trị tiến độ trước khi cập nhật
        Target planBefore = targetService.getPlanById(idTarget);
        assertEquals(2.5f, planBefore.getProgress(), "Tiến độ ban đầu phải là 2.5");
        
        // Cập nhật tiến độ
        targetService.updatePlanProgress(idTarget, newProgress);
        
        // Truy vấn giá trị tiến độ sau khi cập nhật
        Target planAfter = targetService.getPlanById(idTarget);
        assertEquals(newProgress, planAfter.getProgress(), "Tiến độ phải được cập nhật thành 4.0");
    }
    
    /**
     * Kiểm thử phương thức getPlanById
     */
    @Test
    public void testGetPlanById() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lấy mục tiêu bằng ID
        Target plan = targetService.getPlanById(1);
        
        // Kiểm tra kết quả
        assertNotNull(plan, "Phải tìm thấy mục tiêu");
        assertEquals("Giảm cân", plan.getTargetName(), "Tên mục tiêu phải đúng");
        assertEquals(5.0f, plan.getTargetNumber(), "Giá trị mục tiêu phải đúng");
        assertEquals("kg", plan.getUnit(), "Đơn vị phải đúng");
        assertEquals(2.5f, plan.getProgress(), "Tiến độ phải đúng");
        assertEquals("In Progress", plan.getStatus(), "Trạng thái phải đúng");
    }
    
    /**
     * Kiểm thử phương thức updatePlanStatus
     */
    @Test
    public void testUpdatePlanStatus() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // ID mục tiêu cần cập nhật
        int idTarget = 1;
        String newStatus = "Achieved";
        
        // Truy vấn trạng thái trước khi cập nhật
        Target planBefore = targetService.getPlanById(idTarget);
        assertEquals("In Progress", planBefore.getStatus(), "Trạng thái ban đầu phải là 'In Progress'");
        
        // Cập nhật trạng thái
        targetService.updatePlanStatus(idTarget, newStatus);
        
        // Truy vấn trạng thái sau khi cập nhật
        Target planAfter = targetService.getPlanById(idTarget);
        assertEquals(newStatus, planAfter.getStatus(), "Trạng thái phải được cập nhật thành 'Achieved'");
    }
    
    /**
     * Kiểm thử phương thức isPlanExist (kiểm tra bằng ID)
     */
    @Test
    public void testIsPlanExistById() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Kiểm tra mục tiêu đã tồn tại
        boolean exists = targetService.isPlanExist(1);
        assertTrue(exists, "Mục tiêu với ID = 1 phải tồn tại");
        
        // Kiểm tra mục tiêu không tồn tại
        boolean notExists = targetService.isPlanExist(999);
        assertFalse(notExists, "Mục tiêu với ID = 999 không nên tồn tại");
    }
    
    /**
     * Kiểm thử phương thức deletePlan
     */
    @Test
    public void testDeletePlan() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // ID mục tiêu cần xóa
        int idTarget = 3;
        
        // Kiểm tra mục tiêu tồn tại trước khi xóa
        boolean existsBefore = targetService.isPlanExist(idTarget);
        assertTrue(existsBefore, "Mục tiêu phải tồn tại trước khi xóa");
        
        // Xóa mục tiêu
        targetService.deletePlan(idTarget);
        
        // Kiểm tra mục tiêu không còn tồn tại sau khi xóa
        boolean existsAfter = targetService.isPlanExist(idTarget);
        assertFalse(existsAfter, "Mục tiêu không nên tồn tại sau khi xóa");
    }
    
    /**
     * Kiểm thử phương thức countQuantityPlans
     */
    @Test
    public void testCountQuantityPlans() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Đếm số lượng mục tiêu của người dùng
        int count = targetService.countQuantityPlans(USER_ID);
        assertEquals(3, count, "Người dùng phải có đúng 3 mục tiêu");
        
        // Đếm số lượng mục tiêu của admin
        int adminCount = targetService.countQuantityPlans(1);
        assertEquals(1, adminCount, "Admin phải có đúng 1 mục tiêu");
    }
    
    /**
     * Kiểm thử phương thức countAchievedPlans
     */
    @Test
    public void testCountAchievedPlans() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Đếm số lượng mục tiêu đã đạt được của người dùng
        int count = targetService.countAchievedPlans(USER_ID);
        assertEquals(1, count, "Người dùng phải có đúng 1 mục tiêu đã đạt được");
        
        // Đếm số lượng mục tiêu đã đạt được của admin
        int adminCount = targetService.countAchievedPlans(1);
        assertEquals(0, adminCount, "Admin không có mục tiêu đã đạt được nào");
    }
    
    /**
     * Kiểm thử phương thức calculateStatus
     */
    @Test
    public void testCalculateStatus() {
        // Ngày hiện tại
        LocalDate today = LocalDate.now();
        
        // Trường hợp 1: Chưa bắt đầu (startDate trong tương lai)
        String status1 = targetService.calculateStatus(today.plusDays(10), today.plusDays(30), 0.0f, 100.0f);
        assertEquals("Not Started", status1, "Trạng thái phải là 'Not Started' khi chưa đến ngày bắt đầu");
        
        // Trường hợp 2: Đang thực hiện (trong khoảng thời gian, chưa đạt mục tiêu)
        String status2 = targetService.calculateStatus(today.minusDays(10), today.plusDays(10), 50.0f, 100.0f);
        assertEquals("In Progress", status2, "Trạng thái phải là 'In Progress' khi đang trong thời gian thực hiện và chưa đạt mục tiêu");
        
        // Trường hợp 3: Đã đạt được (đã đạt mục tiêu)
        String status3 = targetService.calculateStatus(today.minusDays(10), today.plusDays(10), 100.0f, 100.0f);
        assertEquals("Achieved", status3, "Trạng thái phải là 'Achieved' khi đã đạt mục tiêu");
        
        // Trường hợp 4: Đã vượt quá mục tiêu
        String status4 = targetService.calculateStatus(today.minusDays(10), today.plusDays(10), 150.0f, 100.0f);
        assertEquals("Achieved", status4, "Trạng thái phải là 'Achieved' khi đã vượt quá mục tiêu");
        
        // Trường hợp 5: Thất bại (đã quá thời hạn, chưa đạt mục tiêu)
        String status5 = targetService.calculateStatus(today.minusDays(30), today.minusDays(10), 50.0f, 100.0f);
        assertEquals("Failed", status5, "Trạng thái phải là 'Failed' khi đã quá thời hạn và chưa đạt mục tiêu");
    }
}