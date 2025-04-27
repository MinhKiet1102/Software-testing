package com.milkyway.services;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.Target;
import com.milkyway.pojo.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TargetServiceTest {

    private Connection h2Connection;
    private TargetService targetService;
    
    @BeforeEach
    public void setUp() throws SQLException {
        // Thiết lập H2 database trong bộ nhớ sử dụng lớp tiện ích TestDatabaseSetup
        h2Connection = TestDatabaseSetup.createH2Connection();
        
        // Tạo schema cho các bảng kiểm thử
        try (Statement stmt = h2Connection.createStatement()) {
            // Tạo bảng user (cần thiết cho khóa ngoại) - Đặt "user" trong ngoặc kép vì nó là từ khóa trong H2
            stmt.execute("CREATE TABLE \"user\" (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL," +
                    "password VARCHAR(100) NOT NULL," +
                    "email VARCHAR(100)," +
                    "role VARCHAR(10) NOT NULL" +
                    ")");
            
            // Tạo bảng target với auto-increment bắt đầu từ một số cao hơn
            stmt.execute("CREATE TABLE target (" +
                    "idTarget INT AUTO_INCREMENT PRIMARY KEY," +
                    "targetName VARCHAR(100) NOT NULL," +
                    "dateCreated DATE NOT NULL," +
                    "startDate DATE NOT NULL," +
                    "endDate DATE NOT NULL," +
                    "targetNumber FLOAT NOT NULL," +
                    "unit VARCHAR(20) NOT NULL," +
                    "progress FLOAT NOT NULL," +
                    "status ENUM('Not Started', 'In Progress', 'Achieved', 'Failed', 'Cancelled') NOT NULL," +
                    "userId INT," +
                    "FOREIGN KEY (userId) REFERENCES \"user\"(id)" +
                    ")");
            
            // Chèn người dùng thử nghiệm
            stmt.execute("INSERT INTO \"user\" (id, username, password, email, role) " +
                    "VALUES (1, 'testuser', 'password', 'test@example.com', 'user')");
            
            // Chèn các mục tiêu mẫu
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            LocalDate tomorrow = today.plusDays(1);
            LocalDate nextWeek = today.plusDays(7);
            
            // Sử dụng các giá trị idTarget cụ thể để tránh xung đột auto-increment
            stmt.execute("INSERT INTO target (idTarget, targetName, dateCreated, startDate, endDate, targetNumber, unit, progress, status, userId) " +
                    "VALUES (1, 'Test Plan 1', '" + today + "', '" + yesterday + "', '" + nextWeek + "', 100.0, 'kg', 50.0, 'In Progress', 1)");
            
            stmt.execute("INSERT INTO target (idTarget, targetName, dateCreated, startDate, endDate, targetNumber, unit, progress, status, userId) " +
                    "VALUES (2, 'Achieved Plan', '" + today + "', '" + yesterday + "', '" + tomorrow + "', 100.0, 'steps', 100.0, 'Achieved', 1)");
            
            stmt.execute("INSERT INTO target (idTarget, targetName, dateCreated, startDate, endDate, targetNumber, unit, progress, status, userId) " +
                    "VALUES (3, 'Not Started Plan', '" + today + "', '" + tomorrow + "', '" + nextWeek + "', 150.0, 'mins', 0.0, 'Not Started', 1)");
            
            // Đặt auto-increment bắt đầu sau các bản ghi đã chèn thủ công
            stmt.execute("ALTER TABLE target ALTER COLUMN idTarget RESTART WITH 4");
        }
        
        // Tạo target service
        targetService = new TargetService();
        
        // Đặt môi trường kiểm thử để tránh hiển thị cảnh báo UI
        TargetService.setTestEnvironment(true);
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        // Reset test environment flag
        TargetService.setTestEnvironment(false);
        
        // Close connection and reset the test connection
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
        JdbcUtils.resetTestConnection();
    }

    @Test
    public void testSetTestEnvironment() {
        // Test setting test environment
        TargetService.setTestEnvironment(true);
        assertTrue(TargetService.isTestEnvironment());
        
        TargetService.setTestEnvironment(false);
        assertFalse(TargetService.isTestEnvironment());
        
        // Reset to true for the remaining tests
        TargetService.setTestEnvironment(true);
    }


    @Test
    public void testIsPlanExistByPlanName() throws SQLException {
        // Test plan exists
        boolean result = targetService.isPlanExist("Test Plan 1", LocalDate.now().minusDays(1), 1);
        assertTrue(result);
        
        // Test plan doesn't exist
        result = targetService.isPlanExist("NonExistentPlan", LocalDate.now(), 1);
        assertFalse(result);
    }
    
    @Test
    public void testAddPlan() throws SQLException {
        // Test adding a plan - we should get ID 4 due to auto-increment starting at 4
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);
        targetService.addPlan("New Test Plan", startDate, endDate, 100.0f, "kg", 1);
        
        // Verify plan was added - search by name instead of ID
        String query = "SELECT COUNT(*) FROM target WHERE targetName = 'New Test Plan'";
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
        
        // Verify plan details
        query = "SELECT * FROM target WHERE targetName = 'New Test Plan'";
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next());
            assertEquals(4, rs.getInt("idTarget")); // Expected ID is 4
            assertEquals(startDate.toString(), rs.getString("startDate"));
            assertEquals(endDate.toString(), rs.getString("endDate"));
            assertEquals(100.0f, rs.getFloat("targetNumber"));
            assertEquals("kg", rs.getString("unit"));
            assertEquals(0.0f, rs.getFloat("progress"));
            assertEquals(1, rs.getInt("userId"));
        }
    }
    
    @Test
    public void testCalculateStatus() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextWeek = today.plusDays(7);
        
        // Test "Achieved" status
        assertEquals("Achieved", targetService.calculateStatus(yesterday, nextWeek, 100.0f, 100.0f));
        
        // Test "Not Started" status
        assertEquals("Not Started", targetService.calculateStatus(tomorrow, nextWeek, 0.0f, 100.0f));
        
        // Test "Failed" status
        assertEquals("Failed", targetService.calculateStatus(yesterday.minusDays(10), yesterday, 50.0f, 100.0f));
        
        // Test "In Progress" status
        assertEquals("In Progress", targetService.calculateStatus(yesterday, tomorrow, 50.0f, 100.0f));
    }
    
    @Test
    public void testGetOldEndDate() throws SQLException {
        // Test getting old end date
        LocalDate result = targetService.getOldEndDate(1);
        assertNotNull(result);
        assertEquals(LocalDate.now().plusDays(7), result);
        
        // Test when no result is found
        result = targetService.getOldEndDate(99); // Non-existent ID
        assertNull(result);
    }
    
    @Test
    public void testGetDateCreated() throws SQLException {
        // Test getting date created
        String result = targetService.getDateCreated(1);
        assertNotNull(result);
        assertTrue(result.contains(LocalDate.now().toString()));
        
        // Test when no result is found
        result = targetService.getDateCreated(99); // Non-existent ID
        assertNull(result);
    }
    
    @Test
    public void testUpdatePlan() throws SQLException {
        // Test updating a plan
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);
        // Get current date created
        String dateCreated = targetService.getDateCreated(1);
        
        targetService.updatePlan(1, "Updated Plan", startDate, endDate, dateCreated, 120.0f, "kg");
        
        // Verify plan was updated
        String query = "SELECT * FROM target WHERE idTarget = 1";
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next());
            assertEquals("Updated Plan", rs.getString("targetName"));
            assertEquals(startDate.toString(), rs.getString("startDate"));
            assertEquals(endDate.toString(), rs.getString("endDate"));
            assertEquals(120.0f, rs.getFloat("targetNumber"));
            assertEquals("kg", rs.getString("unit"));
        }
    }
    
    @Test
    public void testUpdatePlanProgress() throws SQLException {
        // Test updating plan progress
        targetService.updatePlanProgress(1, 75.0f);
        
        // Verify progress was updated
        String query = "SELECT progress FROM target WHERE idTarget = 1";
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next());
            assertEquals(75.0f, rs.getFloat("progress"));
        }
    }
    
    @Test
    public void testGetPlansForCurrentUser() throws SQLException {
        // Test getting all plans
        ObservableList<Target> result = targetService.getPlansForCurrentUser(1, false);
        assertEquals(3, result.size());
        
        // Test getting only achieved plans
        result = targetService.getPlansForCurrentUser(1, true);
        assertEquals(1, result.size());
        assertEquals("Achieved Plan", result.get(0).getTargetName());
    }
    
    @Test
    public void testIsPlanExistById() throws SQLException {
        // Test plan exists
        boolean result = targetService.isPlanExist(1);
        assertTrue(result);
        
        // Test plan doesn't exist
        result = targetService.isPlanExist(99); // Non-existent ID
        assertFalse(result);
    }
    
    @Test
    public void testDeletePlan() throws SQLException {
        // Test deleting a plan
        targetService.deletePlan(1);
        
        // Verify plan was deleted
        String query = "SELECT COUNT(*) FROM target WHERE idTarget = 1";
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
    }
    
    @Test
    public void testCountQuantityPlans() throws SQLException {
        // Test counting plans (we added 3 plans in setup)
        int result = targetService.countQuantityPlans(1);
        assertEquals(3, result);
        
        // Test no plans found for non-existent user
        result = targetService.countQuantityPlans(99);
        assertEquals(0, result);
    }
    
    @Test
    public void testCountAchievedPlans() throws SQLException {
        // Test counting achieved plans (1 plan is achieved)
        int result = targetService.countAchievedPlans(1);
        assertEquals(1, result);
        
        // Test no achieved plans found for non-existent user
        result = targetService.countAchievedPlans(99);
        assertEquals(0, result);
    }
    
    @Test
    public void testGetPlanById() throws SQLException {
        // Test getting plan by id
        Target result = targetService.getPlanById(1);
        assertNotNull(result);
        assertEquals(1, result.getIdTarget());
        assertEquals("Test Plan 1", result.getTargetName());
        assertEquals("In Progress", result.getStatus());
        
        // Test plan not found
        result = targetService.getPlanById(99); // Non-existent ID
        assertNull(result);
    }
    
    @Test
    public void testUpdatePlanStatus() throws SQLException {
        // Test updating plan status
        targetService.updatePlanStatus(1, "Achieved");
        
        // Verify status was updated
        String query = "SELECT status FROM target WHERE idTarget = 1";
        try (Statement stmt = h2Connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            assertTrue(rs.next());
            assertEquals("Achieved", rs.getString("status"));
        }
    }
    
    @Test
    public void testGetStatusList() throws SQLException {
        // Test getting status list
        List<String> result = targetService.getStatusList();
        assertEquals(5, result.size());
        assertTrue(result.contains("Not Started"));
        assertTrue(result.contains("In Progress"));
        assertTrue(result.contains("Achieved"));
        assertTrue(result.contains("Failed"));
        assertTrue(result.contains("Cancelled"));
    }
    @Test
    public void testCheckMidCycleProgress() {
        // Create test target
        Target target = new Target();
        target.setStatus("In Progress");
        target.setTargetNumber(100.0f);
        target.setProgress(25.0f);
        
        // Set dates for testing
        java.sql.Date startDate = java.sql.Date.valueOf(LocalDate.now().minusDays(15));
        java.sql.Date endDate = java.sql.Date.valueOf(LocalDate.now().plusDays(15));
        target.setStartDate(startDate);
        target.setEndDate(endDate);
        
        // Method doesn't return anything, just test that it runs without exceptions
        targetService.checkMidCycleProgress(target);
        
        // Test with null target
        targetService.checkMidCycleProgress(null);
        
        // Test with different status
        target.setStatus("Failed");
        targetService.checkMidCycleProgress(target);
    }
    
    @Test
    public void testGetPlanByName() throws SQLException {
        // Test getting plan by name
        Target result = targetService.getPlanByName("Test Plan 1");
        assertNotNull(result);
        assertEquals("Test Plan 1", result.getTargetName());
        
        // Test plan not found
        result = targetService.getPlanByName("Nonexistent Plan");
        assertNull(result);
    }
    
    @Test
    public void testCalculateStatusFromTarget() {
        // Create test target
        Target target = new Target();
        
        // Test "Cancelled" status
        target.setStatus("Cancelled");
        assertEquals("Cancelled", targetService.calculateStatus(target));
        
        // Setup for other statuses
        java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
        java.sql.Date yesterday = java.sql.Date.valueOf(LocalDate.now().minusDays(1));
        java.sql.Date tomorrow = java.sql.Date.valueOf(LocalDate.now().plusDays(1));
        java.sql.Date nextWeek = java.sql.Date.valueOf(LocalDate.now().plusDays(7));
        
        // Test "Achieved" status
        target.setStatus("In Progress");
        target.setStartDate(yesterday);
        target.setEndDate(nextWeek);
        target.setProgress(100.0f);
        target.setTargetNumber(100.0f);
        assertEquals("Achieved", targetService.calculateStatus(target));
        
        // Test "Failed" status
        target.setStartDate(yesterday);
        target.setEndDate(yesterday); // End date in the past
        target.setProgress(50.0f);
        target.setTargetNumber(100.0f);
        assertEquals("Failed", targetService.calculateStatus(target));
        
        // Test "In Progress" status
        target.setStartDate(yesterday);
        target.setEndDate(tomorrow);
        target.setProgress(50.0f);
        target.setTargetNumber(100.0f);
        assertEquals("In Progress", targetService.calculateStatus(target));
        
        // Test "Not Started" status
        target.setStartDate(tomorrow);
        target.setEndDate(nextWeek);
        target.setProgress(0.0f);
        target.setTargetNumber(100.0f);
        assertEquals("Not Started", targetService.calculateStatus(target));
    }
    
    @Test
    public void testGetTargetByUserId() throws SQLException {
        // Test getting targets by user id and status
        List<Target> result = targetService.getTargetByUserId(1, "In Progress");
        assertEquals(1, result.size());
        assertEquals("Test Plan 1", result.get(0).getTargetName());
        
        // Test getting targets with a different status
        result = targetService.getTargetByUserId(1, "Achieved");
        assertEquals(1, result.size());
        assertEquals("Achieved Plan", result.get(0).getTargetName());
        
        // Test with invalid user ID
        result = targetService.getTargetByUserId(99, "In Progress");
        assertEquals(0, result.size());
    }
    
    @Test
    public void testCheckMidCycleProgressAndSendEmailWithDifferentStatuses() {
        // Create test target and user
        Target target = new Target();
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        
        // Test with Failed status
        target.setStatus("Failed");
        assertFalse(targetService.checkMidCycleProgressAndSendEmail(target, user));
        
        // Test with Not Started status
        target.setStatus("Not Started");
        assertFalse(targetService.checkMidCycleProgressAndSendEmail(target, user));
        
        // Test with Cancelled status
        target.setStatus("Cancelled");
        assertFalse(targetService.checkMidCycleProgressAndSendEmail(target, user));
        
        // Set up valid target data but with dates that don't trigger the email
        target.setStatus("In Progress");
        target.setTargetNumber(100.0f);
        target.setProgress(60.0f); // Progress > 50%
        target.setUnit("kg");
        
        java.sql.Date startDate = java.sql.Date.valueOf(LocalDate.now().minusDays(15));
        java.sql.Date endDate = java.sql.Date.valueOf(LocalDate.now().plusDays(15));
        target.setStartDate(startDate);
        target.setEndDate(endDate);
        
        // Should return false because progress is > 50%
        assertFalse(targetService.checkMidCycleProgressAndSendEmail(target, user));
        
        // Test with progress = 0 (shouldn't trigger email if we're in early stages)
        target.setStatus("In Progress");
        target.setTargetNumber(100.0f);
        target.setProgress(0.0f);
        target.setUnit("kg");
        
        // Set dates where we're still in early stages (before mid-cycle)
        startDate = java.sql.Date.valueOf(LocalDate.now().minusDays(1)); // Just started
        endDate = java.sql.Date.valueOf(LocalDate.now().plusDays(30));   // Long period ahead
        target.setStartDate(startDate);
        target.setEndDate(endDate);
        
        // Should return false because we're not yet at mid-cycle
        assertFalse(targetService.checkMidCycleProgressAndSendEmail(target, user));
    }
    
    @Test
    public void testCheckMidCycleProgressWithInvalidData() {
        // Create test target
        Target target = new Target();
        target.setStatus("In Progress");
        
        // Test with targetNumber <= 0
        target.setTargetNumber(0.0f);
        target.setProgress(0.0f);
        
        java.sql.Date startDate = java.sql.Date.valueOf(LocalDate.now().minusDays(15));
        java.sql.Date endDate = java.sql.Date.valueOf(LocalDate.now().plusDays(15));
        target.setStartDate(startDate);
        target.setEndDate(endDate);
        
        // Method doesn't return anything, just verify it doesn't throw exceptions
        targetService.checkMidCycleProgress(target);
        
        // Test with proper targetNumber but null dates
        target.setTargetNumber(100.0f);
        target.setStartDate(null);
        target.setEndDate(null);
        
        targetService.checkMidCycleProgress(target);
    }
    
    @Test
    public void testCheckMidCycleProgressWithHighProgressPercentage() {
        // Create test target with high progress percentage
        Target target = new Target();
        target.setStatus("In Progress");
        target.setTargetNumber(100.0f);
        target.setProgress(80.0f); // Progress > 50%
        
        java.sql.Date startDate = java.sql.Date.valueOf(LocalDate.now().minusDays(15));
        java.sql.Date endDate = java.sql.Date.valueOf(LocalDate.now().plusDays(15));
        target.setStartDate(startDate);
        target.setEndDate(endDate);
        
        // Should not trigger warning because progress is high
        targetService.checkMidCycleProgress(target);
        // There's no assertion here because we're testing that no exception is thrown
        // and no alert is triggered in test mode
    }
}