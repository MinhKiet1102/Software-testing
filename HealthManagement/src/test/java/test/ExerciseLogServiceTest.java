package test;

import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.Exerciselog;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import com.milkyway.services.ExerciseLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Lớp kiểm thử cho ExerciseLogService
 * Lớp này kiểm tra tất cả chức năng của lớp ExerciseLogService, bao gồm:
 * - Lưu nhật ký tập luyện
 * - Xóa nhật ký tập luyện
 * - Lấy nhật ký tập luyện theo người dùng và ngày
 * 
 * Chiến lược kiểm thử:
 * - Sử dụng Mockito để giả lập các thành phần phụ thuộc như Connection, PreparedStatement, ResultSet
 * - Sử dụng MockedStatic để giả lập hàm tĩnh JdbcUtils.getConn()
 * - Kiểm tra các đường đi thành công, thất bại và xử lý ngoại lệ
 * - Xác minh các tương tác với database được thực hiện đúng cách
 */
public class ExerciseLogServiceTest {
    
    @Mock
    private Connection mockConnection; // Giả lập kết nối cơ sở dữ liệu
    
    @Mock
    private PreparedStatement mockStatement; // Giả lập câu lệnh SQL được chuẩn bị
    
    @Mock
    private ResultSet mockResultSet; // Giả lập tập kết quả trả về từ truy vấn
    
    private ExerciseLogService exerciseLogService; // Đối tượng cần kiểm thử
    
    private AutoCloseable closeable; // Quản lý việc đóng tài nguyên sau mỗi kiểm thử
    
    /**
     * Thiết lập trước mỗi test case
     * Khởi tạo các mock và đối tượng ExerciseLogService
     * 
     * Chú ý: ExerciseLogService không có constructor phức tạp,
     * nên chúng ta khởi tạo trực tiếp, còn các đối tượng phụ thuộc
     * sẽ được mock khi cần thiết trong từng phương thức kiểm thử
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Khởi tạo các đối tượng mock sử dụng annotation @Mock
        closeable = MockitoAnnotations.openMocks(this);
        exerciseLogService = new ExerciseLogService();
    }
    
    /**
     * Dọn dẹp sau mỗi test case
     * Đóng các tài nguyên đã mở để tránh rò rỉ bộ nhớ
     */
    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }
    
    /**
     * Kiểm tra phương thức saveLog với dữ liệu hợp lệ
     * 
     * Kỳ vọng: Phương thức trả về true khi lưu thành công
     * 
     * Kịch bản kiểm thử:
     * 1. Tạo đối tượng Exerciselog với dữ liệu hợp lệ
     * 2. Mock JdbcUtils.getConn() để trả về kết nối giả
     * 3. Thiết lập mockStatement.executeUpdate() trả về 1 (thành công)
     * 4. Gọi exerciseLogService.saveLog() và kiểm tra kết quả
     * 5. Xác minh các tham số được truyền đúng vào PreparedStatement
     */
    @Test
    public void testSaveLogWithValidData() throws SQLException {
        // Chuẩn bị dữ liệu kiểm thử - Tạo nhật ký với thông tin đầy đủ
        Exerciselog log = new Exerciselog();
        log.setEffortLevel("Cao");           // Mức độ nỗ lực
        log.setDuration(30);                 // Thời gian tập (phút)
        log.setDatetime(Date.valueOf(LocalDate.now())); // Ngày tập
        log.setEnergyBurn(150.5);            // Lượng calo đã đốt
        
        Exercise exercise = new Exercise(1, "Chạy bộ"); // Bài tập liên kết
        log.setExerciseId(exercise);
        
        User user = new User(1);             // Người dùng liên kết
        log.setUserId(user);
        
        // Thiết lập mock JdbcUtils và Connection
        // try-with-resources đảm bảo MockedStatic được đóng sau khi sử dụng
        try (var mocked = mockStatic(JdbcUtils.class)) {
            mocked.when(JdbcUtils::getConn).thenReturn(mockConnection);
            
            // Thiết lập hành vi mong muốn cho mockConnection và mockStatement
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(1); // 1 dòng bị ảnh hưởng = thành công
            
            // Thực thi phương thức cần kiểm thử
            boolean result = exerciseLogService.saveLog(log);
            
            // Kiểm tra kết quả - Kỳ vọng phương thức trả về true
            assertTrue(result, "Phương thức saveLog phải trả về true khi lưu thành công");
            
            // Xác minh các tương tác - Đảm bảo các tham số được truyền đúng
            verify(mockStatement).setString(1, log.getEffortLevel());  // Tham số 1: efforyLevel
            verify(mockStatement).setInt(2, log.getDuration());        // Tham số 2: duration
            verify(mockStatement).setDate(eq(3), any(java.sql.Date.class)); // Tham số 3: datetime
            verify(mockStatement).setDouble(4, log.getEnergyBurn());   // Tham số 4: energyBurn
            verify(mockStatement).setInt(5, log.getExerciseId().getIdExercise()); // Tham số 5: idExercise
            verify(mockStatement).setInt(6, log.getUserId().getId());  // Tham số 6: userId
            verify(mockStatement).executeUpdate(); // Xác minh executeUpdate() được gọi
        }
    }
    
    /**
     * Kiểm tra phương thức saveLog với dữ liệu không hợp lệ
     * 
     * Kỳ vọng: Phương thức trả về false khi dữ liệu không hợp lệ
     * 
     * Kịch bản kiểm thử:
     * 1. Kiểm tra nhiều trường hợp dữ liệu không hợp lệ:
     *   a. log là null
     *   b. log không có exercise
     *   c. log không có user
     *   d. log có duration <= 0
     * 2. Gọi exerciseLogService.saveLog() và kiểm tra kết quả
     * 
     * Chú ý: Phương thức này không cần mock JdbcUtils vì validation
     * sẽ fail trước khi gọi các phương thức database
     */
    @Test
    public void testSaveLogWithInvalidData() throws SQLException {
        // Trường hợp log là null - Kỳ vọng: trả về false
        boolean result1 = exerciseLogService.saveLog(null);
        assertFalse(result1, "Phương thức saveLog phải trả về false khi log là null");
        
        // Trường hợp không có exercise - Kỳ vọng: trả về false
        Exerciselog log2 = new Exerciselog();
        log2.setEffortLevel("Cao");
        log2.setDuration(30);
        log2.setDatetime(Date.valueOf(LocalDate.now()));
        log2.setUserId(new User(1));
        // Không thiết lập exerciseId
        
        boolean result2 = exerciseLogService.saveLog(log2);
        assertFalse(result2, "Phương thức saveLog phải trả về false khi không có exercise");
        
        // Trường hợp không có user - Kỳ vọng: trả về false
        Exerciselog log3 = new Exerciselog();
        log3.setEffortLevel("Cao");
        log3.setDuration(30);
        log3.setDatetime(Date.valueOf(LocalDate.now()));
        log3.setExerciseId(new Exercise(1));
        // Không thiết lập userId
        
        boolean result3 = exerciseLogService.saveLog(log3);
        assertFalse(result3, "Phương thức saveLog phải trả về false khi không có user");
        
        // Trường hợp duration <= 0 - Kỳ vọng: trả về false
        Exerciselog log4 = new Exerciselog();
        log4.setEffortLevel("Cao");
        log4.setDuration(0); // Giá trị không hợp lệ cho duration
        log4.setDatetime(Date.valueOf(LocalDate.now()));
        log4.setExerciseId(new Exercise(1));
        log4.setUserId(new User(1));
        
        boolean result4 = exerciseLogService.saveLog(log4);
        assertFalse(result4, "Phương thức saveLog phải trả về false khi duration <= 0");
    }
    
    /**
     * Kiểm tra phương thức saveLog khi xảy ra lỗi SQL
     * 
     * Kỳ vọng: Phương thức ném SQLException khi có lỗi xảy ra
     * 
     * Kịch bản kiểm thử:
     * 1. Tạo đối tượng Exerciselog với dữ liệu hợp lệ
     * 2. Mock JdbcUtils.getConn() để trả về kết nối giả
     * 3. Thiết lập mockStatement.executeUpdate() ném SQLException
     * 4. Gọi exerciseLogService.saveLog() và kiểm tra ngoại lệ
     */
    @Test
    public void testSaveLogWithSQLException() throws SQLException {
        // Chuẩn bị dữ liệu kiểm thử
        Exerciselog log = new Exerciselog();
        log.setEffortLevel("Cao");
        log.setDuration(30);
        log.setDatetime(Date.valueOf(LocalDate.now()));
        log.setEnergyBurn(150.5);
        log.setExerciseId(new Exercise(1));
        log.setUserId(new User(1));
        
        // Thiết lập mock để ném SQLException
        try (var mocked = mockStatic(JdbcUtils.class)) {
            mocked.when(JdbcUtils::getConn).thenReturn(mockConnection);
            
            // Thiết lập mockStatement ném SQLException khi gọi executeUpdate()
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenThrow(new SQLException("Lỗi kết nối cơ sở dữ liệu"));
            
            // Kiểm tra ngoại lệ - Kỳ vọng throws SQLException
            assertThrows(SQLException.class, () -> exerciseLogService.saveLog(log), 
                    "Phương thức saveLog phải ném SQLException khi có lỗi SQL");
        }
    }
    
    /**
     * Kiểm tra phương thức deleteExerciseLog với ID hợp lệ
     * 
     * Kỳ vọng: Phương thức thực hiện xóa thành công (không ném ngoại lệ)
     * 
     * Kịch bản kiểm thử:
     * 1. Thiết lập ID của nhật ký cần xóa
     * 2. Mock JdbcUtils.getConn() để trả về kết nối giả
     * 3. Thiết lập mockStatement.executeUpdate() trả về 1 (thành công)
     * 4. Gọi exerciseLogService.deleteExerciseLog() và kiểm tra không có ngoại lệ
     * 5. Xác minh tham số ID được truyền đúng vào PreparedStatement
     */
    @Test
    public void testDeleteExerciseLogWithValidId() throws SQLException {
        int logId = 1; // ID của nhật ký cần xóa
        
        // Thiết lập mock
        try (var mocked = mockStatic(JdbcUtils.class)) {
            mocked.when(JdbcUtils::getConn).thenReturn(mockConnection);
            
            // Thiết lập hành vi mong muốn
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(1); // 1 dòng bị ảnh hưởng = thành công
            
            // Thực thi phương thức và kiểm tra không có ngoại lệ
            assertDoesNotThrow(() -> exerciseLogService.deleteExerciseLog(logId), 
                    "Phương thức deleteExerciseLog không được ném ngoại lệ với ID hợp lệ");
            
            // Xác minh tương tác - Đảm bảo ID được truyền đúng
            verify(mockStatement).setInt(1, logId);
            verify(mockStatement).executeUpdate();
        }
    }
    
    /**
     * Kiểm tra phương thức deleteExerciseLog khi xảy ra lỗi SQL
     * 
     * Kỳ vọng: Phương thức ném SQLException khi có lỗi xảy ra
     * 
     * Kịch bản kiểm thử:
     * 1. Thiết lập ID của nhật ký cần xóa
     * 2. Mock JdbcUtils.getConn() để trả về kết nối giả
     * 3. Thiết lập mockStatement.executeUpdate() ném SQLException
     * 4. Gọi exerciseLogService.deleteExerciseLog() và kiểm tra ngoại lệ
     */
    @Test
    public void testDeleteExerciseLogWithSQLException() throws SQLException {
        int logId = 1; // ID của nhật ký cần xóa
        
        // Thiết lập mock để ném SQLException
        try (var mocked = mockStatic(JdbcUtils.class)) {
            mocked.when(JdbcUtils::getConn).thenReturn(mockConnection);
            
            // Thiết lập mockStatement ném SQLException khi gọi executeUpdate()
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenThrow(new SQLException("Lỗi kết nối cơ sở dữ liệu"));
            
            // Kiểm tra ngoại lệ - Kỳ vọng throws SQLException
            assertThrows(SQLException.class, () -> exerciseLogService.deleteExerciseLog(logId), 
                    "Phương thức deleteExerciseLog phải ném SQLException khi có lỗi SQL");
        }
    }
    
    /**
     * Kiểm tra phương thức getExerciseLogsByUserAndDate với dữ liệu hợp lệ
     * 
     * Kỳ vọng: Phương thức trả về danh sách nhật ký tập luyện
     * 
     * Kịch bản kiểm thử:
     * 1. Thiết lập userId và filterDate
     * 2. Mock JdbcUtils.getConn() để trả về kết nối giả
     * 3. Thiết lập mockResultSet trả về dữ liệu giả (2 kết quả)
     * 4. Gọi exerciseLogService.getExerciseLogsByUserAndDate() 
     * 5. Kiểm tra kết quả và nội dung của các đối tượng trả về
     * 6. Xác minh các tham số được truyền đúng vào PreparedStatement
     */
    @Test
    public void testGetExerciseLogsByUserAndDate() throws SQLException {
        int userId = 1; // ID của người dùng
        Date filterDate = Date.valueOf(LocalDate.now()); // Ngày lọc
        
        // Thiết lập mock
        try (var mocked = mockStatic(JdbcUtils.class)) {
            mocked.when(JdbcUtils::getConn).thenReturn(mockConnection);
            
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false); // Giả lập có 2 kết quả
            
            // Thiết lập dữ liệu giả cho mockResultSet
            // Dữ liệu cho kết quả thứ nhất và thứ hai (next() trả về true 2 lần)
            when(mockResultSet.getInt("idExLog")).thenReturn(1, 2);
            when(mockResultSet.getString("effortLevel")).thenReturn("Cao", "Trung bình");
            when(mockResultSet.getInt("duration")).thenReturn(30, 45);
            when(mockResultSet.getTimestamp("datetime")).thenReturn(new java.sql.Timestamp(filterDate.getTime()), 
                    new java.sql.Timestamp(filterDate.getTime()));
            when(mockResultSet.getDouble("energyBurn")).thenReturn(150.5, 200.0);
            when(mockResultSet.getInt("idExercise")).thenReturn(1, 2);
            when(mockResultSet.getString("exerciseName")).thenReturn("Chạy bộ", "Đạp xe");
            when(mockResultSet.getDouble("caloriesBurnedPerMin")).thenReturn(5.0, 7.5);
            when(mockResultSet.getInt("id")).thenReturn(userId, userId);
            
            // Cung cấp kết quả cho phương thức hasColumn
            // Giả lập cột caloriesBurnedPerMin tồn tại trong kết quả truy vấn
            when(mockResultSet.findColumn("caloriesBurnedPerMin")).thenReturn(8);
            
            // Thực thi phương thức
            List<Exerciselog> result = exerciseLogService.getExerciseLogsByUserAndDate(userId, filterDate);
            
            // Kiểm tra kết quả
            assertNotNull(result, "Kết quả không được là null");
            assertEquals(2, result.size(), "Phải trả về đúng số lượng kết quả");
            
            // Kiểm tra nội dung kết quả đầu tiên
            Exerciselog log1 = result.get(0);
            assertEquals(1, log1.getIdExLog(), "ID của log thứ nhất không đúng");
            assertEquals("Cao", log1.getEffortLevel(), "Mức độ nỗ lực của log thứ nhất không đúng");
            assertEquals(30, log1.getDuration(), "Thời gian của log thứ nhất không đúng");
            assertEquals(150.5, log1.getEnergyBurn(), 0.01, "Lượng calo tiêu thụ của log thứ nhất không đúng");
            assertEquals("Chạy bộ", log1.getExerciseId().getExerciseName(), "Tên bài tập của log thứ nhất không đúng");
            
            // Kiểm tra nội dung kết quả thứ hai
            Exerciselog log2 = result.get(1);
            assertEquals(2, log2.getIdExLog(), "ID của log thứ hai không đúng");
            assertEquals("Trung bình", log2.getEffortLevel(), "Mức độ nỗ lực của log thứ hai không đúng");
            assertEquals(45, log2.getDuration(), "Thời gian của log thứ hai không đúng");
            assertEquals(200.0, log2.getEnergyBurn(), 0.01, "Lượng calo tiêu thụ của log thứ hai không đúng");
            assertEquals("Đạp xe", log2.getExerciseId().getExerciseName(), "Tên bài tập của log thứ hai không đúng");
            
            // Xác minh tương tác - Đảm bảo các tham số được truyền đúng
            verify(mockStatement).setInt(1, userId);
            verify(mockStatement).setDate(eq(2), any(Date.class));
            verify(mockStatement).executeQuery();
        }
    }
    
    /**
     * Kiểm tra phương thức getExerciseLogsByUserAndDate khi không có ngày lọc
     * 
     * Kỳ vọng: Phương thức trả về danh sách nhật ký tập luyện (không lọc theo ngày)
     * 
     * Kịch bản kiểm thử:
     * 1. Thiết lập userId và filterDate=null (không lọc theo ngày)
     * 2. Mock JdbcUtils.getConn() để trả về kết nối giả
     * 3. Thiết lập mockResultSet trả về dữ liệu giả (1 kết quả)
     * 4. Gọi exerciseLogService.getExerciseLogsByUserAndDate() 
     * 5. Kiểm tra kết quả và nội dung của đối tượng trả về
     * 6. Xác minh không gọi setDate() khi filterDate là null
     */
    @Test
    public void testGetExerciseLogsByUserAndDateWithNullDate() throws SQLException {
        int userId = 1; // ID của người dùng
        
        // Thiết lập mock
        try (var mocked = mockStatic(JdbcUtils.class)) {
            mocked.when(JdbcUtils::getConn).thenReturn(mockConnection);
            
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false); // Giả lập có 1 kết quả
            
            // Thiết lập dữ liệu giả
            when(mockResultSet.getInt("idExLog")).thenReturn(1);
            when(mockResultSet.getString("effortLevel")).thenReturn("Cao");
            when(mockResultSet.getInt("duration")).thenReturn(30);
            Date today = Date.valueOf(LocalDate.now());
            when(mockResultSet.getTimestamp("datetime")).thenReturn(new java.sql.Timestamp(today.getTime()));
            when(mockResultSet.getDouble("energyBurn")).thenReturn(150.5);
            when(mockResultSet.getInt("idExercise")).thenReturn(1);
            when(mockResultSet.getString("exerciseName")).thenReturn("Chạy bộ");
            when(mockResultSet.getDouble("caloriesBurnedPerMin")).thenReturn(5.0);
            when(mockResultSet.getInt("id")).thenReturn(userId);
            
            // Cung cấp kết quả cho phương thức hasColumn
            when(mockResultSet.findColumn("caloriesBurnedPerMin")).thenReturn(8);
            
            // Thực thi phương thức với filterDate = null
            List<Exerciselog> result = exerciseLogService.getExerciseLogsByUserAndDate(userId, null);
            
            // Kiểm tra kết quả
            assertNotNull(result, "Kết quả không được là null");
            assertEquals(1, result.size(), "Phải trả về đúng số lượng kết quả");
            
            // Kiểm tra nội dung kết quả
            Exerciselog log = result.get(0);
            assertEquals(1, log.getIdExLog(), "ID của log không đúng");
            assertEquals("Cao", log.getEffortLevel(), "Mức độ nỗ lực của log không đúng");
            assertEquals(30, log.getDuration(), "Thời gian của log không đúng");
            assertEquals(150.5, log.getEnergyBurn(), 0.01, "Lượng calo tiêu thụ của log không đúng");
            assertEquals("Chạy bộ", log.getExerciseId().getExerciseName(), "Tên bài tập của log không đúng");
            
            // Xác minh tương tác - Chỉ thiết lập userId, không thiết lập filterDate
            verify(mockStatement).setInt(1, userId);
            verify(mockStatement, never()).setDate(eq(2), any(Date.class)); // Không gọi setDate khi filterDate là null
            verify(mockStatement).executeQuery();
        }
    }
    
    /**
     * Kiểm tra phương thức getExerciseLogsByUserAndDate khi xảy ra lỗi SQL
     * 
     * Kỳ vọng: Phương thức ném SQLException khi có lỗi xảy ra
     * 
     * Kịch bản kiểm thử:
     * 1. Thiết lập userId và filterDate
     * 2. Mock JdbcUtils.getConn() để trả về kết nối giả
     * 3. Thiết lập mockStatement.executeQuery() ném SQLException
     * 4. Gọi exerciseLogService.getExerciseLogsByUserAndDate() và kiểm tra ngoại lệ
     */
    @Test
    public void testGetExerciseLogsByUserAndDateWithSQLException() throws SQLException {
        int userId = 1; // ID của người dùng
        Date filterDate = Date.valueOf(LocalDate.now()); // Ngày lọc
        
        // Thiết lập mock để ném SQLException
        try (var mocked = mockStatic(JdbcUtils.class)) {
            mocked.when(JdbcUtils::getConn).thenReturn(mockConnection);
            
            // Thiết lập mockStatement ném SQLException khi gọi executeQuery()
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenThrow(new SQLException("Lỗi kết nối cơ sở dữ liệu"));
            
            // Kiểm tra ngoại lệ - Kỳ vọng throws SQLException
            assertThrows(SQLException.class, 
                    () -> exerciseLogService.getExerciseLogsByUserAndDate(userId, filterDate), 
                    "Phương thức getExerciseLogsByUserAndDate phải ném SQLException khi có lỗi SQL");
        }
    }
}
