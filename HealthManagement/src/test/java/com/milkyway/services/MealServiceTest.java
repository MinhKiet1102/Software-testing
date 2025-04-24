package com.milkyway.services;

import com.milkyway.pojo.Food;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.Meal;
import com.milkyway.pojo.MealFood;
import com.milkyway.pojo.MealFoodPK;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp cho MealService sử dụng H2 database
 */
public class MealServiceTest {
    
    private Connection h2Connection;
    private MealService mealService;
    private User currentUser;
    private SimpleDateFormat dateFormat;
    
    /**
     * Thiết lập H2 database trong bộ nhớ trước mỗi kiểm thử
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Khởi tạo định dạng ngày tháng
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
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
        createFoodTable();
        createMealTable();
        createMealFoodTable();
        
        // Tạo và thiết lập người dùng hiện tại cho các bài kiểm tra 
        setupCurrentUser();
        
        // Chèn dữ liệu thử nghiệm
        insertTestData();
        
        // Khởi tạo service
        mealService = new MealService(h2Connection);
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
                    stmt.execute("TRUNCATE TABLE meal_food");
                    stmt.execute("TRUNCATE TABLE meal");
                    stmt.execute("TRUNCATE TABLE food");
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
     * Tạo bảng food cho kiểm thử
     */
    private void createFoodTable() throws SQLException {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS food (" +
                "idFood INT AUTO_INCREMENT PRIMARY KEY, " + 
                "foodName VARCHAR(255) NOT NULL, " +
                "calories DOUBLE NOT NULL, " +
                "protein DOUBLE, " +
                "carb DOUBLE, " +
                "fat DOUBLE, " +
                "sodium DOUBLE, " +
                "sugar DOUBLE" +
                ")"
            );
        }
    }
    
    /**
     * Tạo bảng meal cho kiểm thử
     */
    private void createMealTable() throws SQLException {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS meal (" +
                "idMeal INT AUTO_INCREMENT PRIMARY KEY, " +
                "nameMeal VARCHAR(255) NOT NULL, " +
                "totalCalories DOUBLE NOT NULL, " +
                "dateOfMeal TIMESTAMP NOT NULL, " +
                "userId INT NOT NULL, " +
                "CONSTRAINT fk_meal_user FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE" +
                ")"
            );
        }
    }
    
    /**
     * Tạo bảng meal_food cho kiểm thử
     */
    private void createMealFoodTable() throws SQLException {
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS meal_food (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " + // Thêm ID riêng cho mục đích dễ tìm kiếm và xóa
                "mealId INT NOT NULL, " +
                "foodId INT NOT NULL, " +
                "quantity INT NOT NULL, " +
                "unit VARCHAR(50) NOT NULL, " +
                "CONSTRAINT fk_mealfood_meal FOREIGN KEY (mealId) REFERENCES meal(idMeal) ON DELETE CASCADE, " +
                "CONSTRAINT fk_mealfood_food FOREIGN KEY (foodId) REFERENCES food(idFood) ON DELETE CASCADE, " +
                "CONSTRAINT uq_mealfood UNIQUE (mealId, foodId)" +
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
            stmt.execute("TRUNCATE TABLE meal_food");
            stmt.execute("TRUNCATE TABLE meal");
            stmt.execute("TRUNCATE TABLE food");
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
        
        // Chèn thực phẩm thử nghiệm
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO food (idFood, foodName, calories, protein, carb, fat, sodium, sugar) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Thực phẩm 1: Cơm
            stmt.setInt(1, 1);
            stmt.setString(2, "Cơm trắng");
            stmt.setDouble(3, 130.0);
            stmt.setDouble(4, 2.7);
            stmt.setDouble(5, 28.0);
            stmt.setDouble(6, 0.3);
            stmt.setDouble(7, 1.0);
            stmt.setDouble(8, 0.1);
            stmt.executeUpdate();
            
            // Thực phẩm 2: Thịt gà
            stmt.setInt(1, 2);
            stmt.setString(2, "Thịt gà");
            stmt.setDouble(3, 165.0);
            stmt.setDouble(4, 31.0);
            stmt.setDouble(5, 0.0);
            stmt.setDouble(6, 3.6);
            stmt.setDouble(7, 74.0);
            stmt.setDouble(8, 0.0);
            stmt.executeUpdate();
            
            // Thực phẩm 3: Sữa
            stmt.setInt(1, 3);
            stmt.setString(2, "Sữa tươi");
            stmt.setDouble(3, 42.0);
            stmt.setDouble(4, 3.4);
            stmt.setDouble(5, 5.0);
            stmt.setDouble(6, 1.0);
            stmt.setDouble(7, 44.0);
            stmt.setDouble(8, 5.0);
            stmt.executeUpdate();
        }
        
        // Chèn bữa ăn thử nghiệm
        Date today = new Date();
        Date yesterday = new Date(today.getTime() - 24*60*60*1000);
        
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO meal (idMeal, nameMeal, totalCalories, dateOfMeal, userId) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Bữa ăn 1: Bữa sáng hôm nay của user
            stmt.setInt(1, 1);
            stmt.setString(2, "Bữa sáng");
            stmt.setDouble(3, 172.0); // 130 (cơm) + 42 (sữa)
            stmt.setTimestamp(4, new java.sql.Timestamp(today.getTime()));
            stmt.setInt(5, 2); // userId = 2 (user)
            stmt.executeUpdate();
            
            // Bữa ăn 2: Bữa trưa hôm nay của user
            stmt.setInt(1, 2);
            stmt.setString(2, "Bữa trưa");
            stmt.setDouble(3, 295.0); // 130 (cơm) + 165 (thịt gà)
            stmt.setTimestamp(4, new java.sql.Timestamp(today.getTime()));
            stmt.setInt(5, 2); // userId = 2 (user)
            stmt.executeUpdate();
            
            // Bữa ăn 3: Bữa tối hôm qua của user
            stmt.setInt(1, 3);
            stmt.setString(2, "Bữa tối hôm qua");
            stmt.setDouble(3, 295.0); // 130 (cơm) + 165 (thịt gà)
            stmt.setTimestamp(4, new java.sql.Timestamp(yesterday.getTime()));
            stmt.setInt(5, 2); // userId = 2 (user)
            stmt.executeUpdate();
            
            // Bữa ăn 4: Bữa trưa hôm nay của admin
            stmt.setInt(1, 4);
            stmt.setString(2, "Bữa trưa của admin");
            stmt.setDouble(3, 172.0); // 130 (cơm) + 42 (sữa)
            stmt.setTimestamp(4, new java.sql.Timestamp(today.getTime()));
            stmt.setInt(5, 1); // userId = 1 (admin)
            stmt.executeUpdate();
        }
        
        // Chèn dữ liệu vào bảng meal_food
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "INSERT INTO meal_food (id, mealId, foodId, quantity, unit) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            // Bữa sáng của user: Cơm trắng
            stmt.setInt(1, 1);
            stmt.setInt(2, 1); // mealId = 1 (Bữa sáng)
            stmt.setInt(3, 1); // foodId = 1 (Cơm trắng)
            stmt.setInt(4, 1); // quantity = 1
            stmt.setString(5, "chén"); // unit
            stmt.executeUpdate();
            
            // Bữa sáng của user: Sữa tươi
            stmt.setInt(1, 2);
            stmt.setInt(2, 1); // mealId = 1 (Bữa sáng)
            stmt.setInt(3, 3); // foodId = 3 (Sữa tươi)
            stmt.setInt(4, 1); // quantity = 1
            stmt.setString(5, "ly"); // unit
            stmt.executeUpdate();
            
            // Bữa trưa của user: Cơm trắng
            stmt.setInt(1, 3);
            stmt.setInt(2, 2); // mealId = 2 (Bữa trưa)
            stmt.setInt(3, 1); // foodId = 1 (Cơm trắng)
            stmt.setInt(4, 1); // quantity = 1
            stmt.setString(5, "chén"); // unit
            stmt.executeUpdate();
            
            // Bữa trưa của user: Thịt gà
            stmt.setInt(1, 4);
            stmt.setInt(2, 2); // mealId = 2 (Bữa trưa)
            stmt.setInt(3, 2); // foodId = 2 (Thịt gà)
            stmt.setInt(4, 1); // quantity = 1
            stmt.setString(5, "miếng"); // unit
            stmt.executeUpdate();
            
            // Bữa tối hôm qua của user: Cơm trắng
            stmt.setInt(1, 5);
            stmt.setInt(2, 3); // mealId = 3 (Bữa tối hôm qua)
            stmt.setInt(3, 1); // foodId = 1 (Cơm trắng)
            stmt.setInt(4, 1); // quantity = 1
            stmt.setString(5, "chén"); // unit
            stmt.executeUpdate();
            
            // Bữa tối hôm qua của user: Thịt gà
            stmt.setInt(1, 6);
            stmt.setInt(2, 3); // mealId = 3 (Bữa tối hôm qua)
            stmt.setInt(3, 2); // foodId = 2 (Thịt gà)
            stmt.setInt(4, 1); // quantity = 1
            stmt.setString(5, "miếng"); // unit
            stmt.executeUpdate();
            
            // Bữa trưa của admin: Cơm trắng
            stmt.setInt(1, 7);
            stmt.setInt(2, 4); // mealId = 4 (Bữa trưa của admin)
            stmt.setInt(3, 1); // foodId = 1 (Cơm trắng)
            stmt.setInt(4, 1); // quantity = 1
            stmt.setString(5, "chén"); // unit
            stmt.executeUpdate();
            
            // Bữa trưa của admin: Sữa tươi
            stmt.setInt(1, 8);
            stmt.setInt(2, 4); // mealId = 4 (Bữa trưa của admin)
            stmt.setInt(3, 3); // foodId = 3 (Sữa tươi)
            stmt.setInt(4, 1); // quantity = 1
            stmt.setString(5, "ly"); // unit
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
            createFoodTable();
            createMealTable();
            createMealFoodTable();
            
            // Cập nhật service
            mealService = new MealService(h2Connection);
            
            // Chèn lại dữ liệu thử nghiệm
            insertTestData();
        }
    }
    
    /**
     * Lấy ngày hiện tại không có giờ phút giây
     */
    private Date getToday() throws ParseException {
        return dateFormat.parse(dateFormat.format(new Date()));
    }
    
    /**
     * Kiểm tra thực phẩm có tồn tại trong bữa ăn
     */
    private boolean mealContainsFood(Meal meal, String foodName) {
        Set<MealFood> mealFoods = meal.getMealFoodSet();
        if (mealFoods == null) {
            return false;
        }
        
        for (MealFood mealFood : mealFoods) {
            if (mealFood.getFood().getFoodName().equals(foodName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Lấy tổng số thực phẩm trong bữa ăn
     */
    private int countFoodsInMeal(Meal meal) {
        Set<MealFood> mealFoods = meal.getMealFoodSet();
        return mealFoods != null ? mealFoods.size() : 0;
    }
    
    /**
     * Kiểm thử phương thức getMealsByUserAndDate - lấy các bữa ăn của người dùng hôm nay
     */
    @Test
    public void testGetMealsByUserAndDate() throws SQLException, ParseException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lấy ngày hiện tại
        Date today = getToday();
        
        // Gọi phương thức được kiểm thử
        List<Meal> meals = mealService.getMealsByUserAndDate(currentUser.getId(), today);
        
        // In ra thông tin để debug
        System.out.println("Tìm thấy " + meals.size() + " bữa ăn cho người dùng ID=" + currentUser.getId() + " vào ngày " + dateFormat.format(today));
        for (Meal meal : meals) {
            System.out.println("  - Bữa ăn ID=" + meal.getIdMeal() + ", Tên: " + meal.getNameMeal() + 
                              ", Calo: " + meal.getTotalCalories() + 
                              ", Ngày: " + dateFormat.format(meal.getDateOfMeal()) +
                              ", Số thực phẩm: " + countFoodsInMeal(meal));
        }
        
        // Kiểm tra kết quả
        assertEquals(2, meals.size(), "Phải trả về 2 bữa ăn của người dùng trong ngày hôm nay");
        
        // Kiểm tra tên của các bữa ăn
        boolean foundBreakfast = false;
        boolean foundLunch = false;
        
        for (Meal meal : meals) {
            if ("Bữa sáng".equals(meal.getNameMeal())) {
                foundBreakfast = true;
                assertEquals(172.0, meal.getTotalCalories(), 0.001, "Tổng calo của bữa sáng phải khớp");
                assertEquals(2, countFoodsInMeal(meal), "Bữa sáng phải có 2 món ăn");
                assertTrue(mealContainsFood(meal, "Cơm trắng"), "Bữa sáng phải có cơm trắng");
                assertTrue(mealContainsFood(meal, "Sữa tươi"), "Bữa sáng phải có sữa tươi");
            } else if ("Bữa trưa".equals(meal.getNameMeal())) {
                foundLunch = true;
                assertEquals(295.0, meal.getTotalCalories(), 0.001, "Tổng calo của bữa trưa phải khớp");
                assertEquals(2, countFoodsInMeal(meal), "Bữa trưa phải có 2 món ăn");
                assertTrue(mealContainsFood(meal, "Cơm trắng"), "Bữa trưa phải có cơm trắng");
                assertTrue(mealContainsFood(meal, "Thịt gà"), "Bữa trưa phải có thịt gà");
            }
        }
        
        assertTrue(foundBreakfast, "Phải tìm thấy bữa sáng");
        assertTrue(foundLunch, "Phải tìm thấy bữa trưa");
    }
    
    /**
     * Kiểm thử phương thức getMealsByUserAndDate - không có bữa ăn nào vào một ngày cụ thể
     */
    @Test
    public void testGetMealsByUserAndDateNoMeals() throws SQLException, ParseException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo một ngày trong tương lai (không có bữa ăn)
        Date futureDate = dateFormat.parse("2025-12-31");
        
        // Gọi phương thức được kiểm thử
        List<Meal> meals = mealService.getMealsByUserAndDate(currentUser.getId(), futureDate);
        
        // Kiểm tra kết quả
        assertEquals(0, meals.size(), "Không nên có bữa ăn nào vào ngày trong tương lai");
    }
    
    /**
     * Kiểm thử phương thức getMealById - tìm bữa ăn theo ID
     */
    @Test
    public void testGetMealById() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // ID bữa ăn cần tìm (Bữa trưa của user)
        int mealId = 2;
        
        // Gọi phương thức được kiểm thử
        Meal meal = mealService.getMealById(mealId);
        
        // Kiểm tra kết quả
        assertNotNull(meal, "Bữa ăn phải được tìm thấy");
        assertEquals(mealId, meal.getIdMeal().intValue(), "ID bữa ăn phải khớp");
        assertEquals("Bữa trưa", meal.getNameMeal(), "Tên bữa ăn phải khớp");
        assertEquals(295.0, meal.getTotalCalories(), 0.001, "Tổng calo phải khớp");
        assertEquals(2, meal.getUserId().getId(), "ID người dùng phải khớp");
        assertEquals(2, countFoodsInMeal(meal), "Bữa ăn phải có 2 món");
        assertTrue(mealContainsFood(meal, "Cơm trắng"), "Bữa ăn phải có cơm trắng");
        assertTrue(mealContainsFood(meal, "Thịt gà"), "Bữa ăn phải có thịt gà");
    }
    
    /**
     * Kiểm thử phương thức getMealById - bữa ăn không tồn tại
     */
    @Test
    public void testGetMealByIdNotFound() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // ID bữa ăn không tồn tại
        int nonExistentMealId = 999;
        
        // Gọi phương thức được kiểm thử
        Meal meal = mealService.getMealById(nonExistentMealId);
        
        // Kiểm tra kết quả
        assertNull(meal, "Bữa ăn không tồn tại phải trả về null");
    }
    
    /**
     * Kiểm thử phương thức saveMeal - lưu một bữa ăn mới
     */
    @Test
    public void testSaveMeal() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Tạo bữa ăn mới để lưu
        Meal meal = new Meal();
        meal.setNameMeal("Bữa tối");
        meal.setTotalCalories(200.0);
        meal.setDateOfMeal(new Date());
        
        // Thêm người dùng vào bữa ăn
        User user = new User(2); // ID của user
        meal.setUserId(user);
        
        // In thông tin trước khi lưu
        System.out.println("Trước khi lưu: Bữa ăn " + meal.getNameMeal() + 
                         ", Calo: " + meal.getTotalCalories() + 
                         ", ID: " + meal.getIdMeal());
        
        // Gọi phương thức được kiểm thử
        Meal savedMeal = mealService.saveMeal(meal);
        
        // In thông tin sau khi lưu
        System.out.println("Sau khi lưu: Bữa ăn " + savedMeal.getNameMeal() + 
                         ", Calo: " + savedMeal.getTotalCalories() + 
                         ", ID: " + savedMeal.getIdMeal());
        
        // Kiểm tra kết quả
        assertNotNull(savedMeal, "Bữa ăn đã lưu không được null");
        assertNotNull(savedMeal.getIdMeal(), "ID bữa ăn phải được tạo");
        assertEquals("Bữa tối", savedMeal.getNameMeal(), "Tên bữa ăn phải khớp");
        assertEquals(200.0, savedMeal.getTotalCalories(), 0.001, "Tổng calo phải khớp");
        assertEquals(2, savedMeal.getUserId().getId(), "ID người dùng phải khớp");
        
        // Kiểm tra xem bữa ăn đã được lưu vào cơ sở dữ liệu chưa
        Meal retrievedMeal = mealService.getMealById(savedMeal.getIdMeal());
        assertNotNull(retrievedMeal, "Phải tìm được bữa ăn trong cơ sở dữ liệu");
        assertEquals("Bữa tối", retrievedMeal.getNameMeal(), "Tên bữa ăn phải khớp");
        assertEquals(200.0, retrievedMeal.getTotalCalories(), 0.001, "Tổng calo phải khớp");
        assertEquals(2, retrievedMeal.getUserId().getId(), "ID người dùng phải khớp");
    }
    
    /**
     * Kiểm thử phương thức addFoodToMeal - thêm thực phẩm vào bữa ăn
     */
    @Test
    public void testAddFoodToMeal() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // ID bữa ăn để thêm thực phẩm (Bữa sáng)
        int mealId = 1;
        
        // Kiểm tra bữa ăn trước khi thêm thực phẩm
        Meal mealBefore = mealService.getMealById(mealId);
        assertNotNull(mealBefore, "Bữa ăn phải tồn tại");
        int foodCountBefore = countFoodsInMeal(mealBefore);
        double caloriesBefore = mealBefore.getTotalCalories();
        
        System.out.println("Trước khi thêm: Bữa ăn có " + foodCountBefore + " món, Tổng calo: " + caloriesBefore);
        
        // Thêm thực phẩm mới (Thịt gà) vào bữa ăn
        int foodId = 2; // ID của Thịt gà
        int quantity = 1;
        String unit = "miếng";
        
        // Gọi phương thức được kiểm thử
        mealService.addFoodToMeal(mealId, foodId, quantity, unit);
        
        // Kiểm tra bữa ăn sau khi thêm thực phẩm
        Meal mealAfter = mealService.getMealById(mealId);
        assertNotNull(mealAfter, "Bữa ăn phải tồn tại sau khi thêm thực phẩm");
        int foodCountAfter = countFoodsInMeal(mealAfter);
        double caloriesAfter = mealAfter.getTotalCalories();
        
        System.out.println("Sau khi thêm: Bữa ăn có " + foodCountAfter + " món, Tổng calo: " + caloriesAfter);
        
        // Kiểm tra kết quả
        assertEquals(foodCountBefore + 1, foodCountAfter, "Số lượng thực phẩm phải tăng 1");
        assertTrue(mealContainsFood(mealAfter, "Thịt gà"), "Bữa ăn phải có thịt gà sau khi thêm");
        
        // Kiểm tra tổng calo được cập nhật đúng (Calo thịt gà = 165 * 1 = 165)
        assertEquals(caloriesBefore + 165.0, caloriesAfter, 0.001, "Tổng calo phải được cập nhật đúng");
    }
    
    /**
     * Kiểm thử phương thức deleteMealFood - xóa thực phẩm khỏi bữa ăn (theo mealId và foodId)
     */
    @Test
    public void testDeleteMealFood() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // ID bữa ăn (Bữa trưa)
        int mealId = 2;
        // ID thực phẩm cần xóa (Thịt gà)
        int foodId = 2;
        
        // Kiểm tra bữa ăn trước khi xóa thực phẩm
        Meal mealBefore = mealService.getMealById(mealId);
        assertNotNull(mealBefore, "Bữa ăn phải tồn tại");
        int foodCountBefore = countFoodsInMeal(mealBefore);
        double caloriesBefore = mealBefore.getTotalCalories();
        
        System.out.println("Trước khi xóa: Bữa ăn có " + foodCountBefore + " món, Tổng calo: " + caloriesBefore);
        assertTrue(mealContainsFood(mealBefore, "Thịt gà"), "Bữa ăn phải có thịt gà trước khi xóa");
        
        // Gọi phương thức được kiểm thử
        mealService.deleteMealFood(mealId, foodId);
        
        // Kiểm tra bữa ăn sau khi xóa thực phẩm
        Meal mealAfter = mealService.getMealById(mealId);
        assertNotNull(mealAfter, "Bữa ăn phải tồn tại sau khi xóa thực phẩm");
        int foodCountAfter = countFoodsInMeal(mealAfter);
        double caloriesAfter = mealAfter.getTotalCalories();
        
        System.out.println("Sau khi xóa: Bữa ăn có " + foodCountAfter + " món, Tổng calo: " + caloriesAfter);
        
        // Kiểm tra kết quả
        assertEquals(foodCountBefore - 1, foodCountAfter, "Số lượng thực phẩm phải giảm 1");
        assertFalse(mealContainsFood(mealAfter, "Thịt gà"), "Bữa ăn không còn thịt gà sau khi xóa");
        
        // Kiểm tra tổng calo được cập nhật đúng (Calo thịt gà = 165 * 1 = 165)
        assertEquals(caloriesBefore - 165.0, caloriesAfter, 0.001, "Tổng calo phải được cập nhật đúng");
    }
    
    /**
     * Kiểm thử phương thức updateMealFood - cập nhật thông tin thực phẩm trong bữa ăn
     */
    @Test
    public void testUpdateMealFood() throws SQLException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // ID bữa ăn (Bữa sáng)
        int mealId = 1;
        // ID thực phẩm cần cập nhật (Cơm trắng)
        int foodId = 1;
        
        // Kiểm tra bữa ăn trước khi cập nhật
        Meal mealBefore = mealService.getMealById(mealId);
        assertNotNull(mealBefore, "Bữa ăn phải tồn tại");
        double caloriesBefore = mealBefore.getTotalCalories();
        
        System.out.println("Trước khi cập nhật: Tổng calo: " + caloriesBefore);
        
        // Cập nhật số lượng thực phẩm (tăng từ 1 lên 2 chén cơm)
        int newQuantity = 2;
        String unit = "chén";
        
        // Gọi phương thức được kiểm thử
        mealService.updateMealFood(mealId, foodId, newQuantity, unit);
        
        // Kiểm tra bữa ăn sau khi cập nhật
        Meal mealAfter = mealService.getMealById(mealId);
        assertNotNull(mealAfter, "Bữa ăn phải tồn tại sau khi cập nhật");
        double caloriesAfter = mealAfter.getTotalCalories();
        
        System.out.println("Sau khi cập nhật: Tổng calo: " + caloriesAfter);
        
        // Kiểm tra tổng calo được cập nhật đúng (Thêm 1 chén cơm = 130 calo)
        assertEquals(caloriesBefore + 130.0, caloriesAfter, 0.001, "Tổng calo phải được cập nhật đúng");
        
        // Kiểm tra số lượng trong bảng meal_food
        try (PreparedStatement stmt = h2Connection.prepareStatement(
                "SELECT quantity FROM meal_food WHERE mealId = ? AND foodId = ?")) {
            stmt.setInt(1, mealId);
            stmt.setInt(2, foodId);
            
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Phải tìm thấy dữ liệu meal_food");
            assertEquals(newQuantity, rs.getInt("quantity"), "Số lượng phải được cập nhật đúng");
        }
    }
    
    /**
     * Kiểm thử phương thức getMealsByUserAndToday - lấy tất cả bữa ăn của người dùng đến ngày hiện tại
     */
    @Test
    public void testGetMealsByUserAndToday() throws SQLException, ParseException {
        // Đảm bảo kết nối vẫn mở
        ensureConnectionIsOpen();
        
        // Lấy ngày hiện tại
        Date today = getToday();
        
        // Gọi phương thức được kiểm thử
        List<Meal> meals = mealService.getMealsByUserAndToday(currentUser.getId(), today);
        
        // In ra thông tin để debug
        System.out.println("Tìm thấy " + meals.size() + " bữa ăn cho người dùng ID=" + currentUser.getId() + " đến ngày " + dateFormat.format(today));
        for (Meal meal : meals) {
            System.out.println("  - Bữa ăn ID=" + meal.getIdMeal() + ", Tên: " + meal.getNameMeal() + 
                              ", Calo: " + meal.getTotalCalories() + 
                              ", Ngày: " + dateFormat.format(meal.getDateOfMeal()) +
                              ", Số thực phẩm: " + countFoodsInMeal(meal));
        }
        
        // Kiểm tra kết quả
        assertEquals(3, meals.size(), "Phải trả về 3 bữa ăn của người dùng đến ngày hôm nay (2 hôm nay + 1 hôm qua)");
        
        // Kiểm tra có tìm thấy bữa tối hôm qua không
        boolean foundYesterdayDinner = false;
        for (Meal meal : meals) {
            if ("Bữa tối hôm qua".equals(meal.getNameMeal())) {
                foundYesterdayDinner = true;
                break;
            }
        }
        assertTrue(foundYesterdayDinner, "Phải tìm thấy bữa tối hôm qua");
    }
}