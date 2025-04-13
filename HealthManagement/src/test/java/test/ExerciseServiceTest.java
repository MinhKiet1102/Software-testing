package test;

import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.services.ExerciseService;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
* Lớp kiểm thử cho ExerciseService
* Lớp này kiểm tra tất cả chức năng của lớp ExerciseService, bao gồm:
* - Lấy danh sách bài tập (có hoặc không có từ khóa tìm kiếm)
* - Lưu bài tập mới
* - Lấy bài tập theo tên
* 
* Chiến lược kiểm thử:
* - Sử dụng Mockito để giả lập các thành phần phụ thuộc như Connection, PreparedStatement, ResultSet
* - Sử dụng MockedStatic để giả lập hàm tĩnh JdbcUtils.getConn()
* - Kiểm tra các đường đi thành công, thất bại và xử lý ngoại lệ
* - Xác minh các câu lệnh SQL được tạo đúng và các tham số được truyền chính xác
*/
public class ExerciseServiceTest {
   
   @Mock
   private Connection mockConnection;        // Giả lập kết nối cơ sở dữ liệu
   
   @Mock
   private PreparedStatement mockPreparedStatement; // Giả lập câu lệnh SQL được chuẩn bị
   
   @Mock
   private CallableStatement mockCallableStatement; // Giả lập CallableStatement
   
   @Mock
   private Statement mockStatement;          // Giả lập câu lệnh SQL thông thường
   
   @Mock
   private ResultSet mockResultSet;          // Giả lập tập kết quả trả về từ truy vấn
   
   private ExerciseService exerciseService;  // Đối tượng cần kiểm thử
   
   private MockedStatic<JdbcUtils> mockedStatic; // Giả lập lớp tĩnh JdbcUtils
   
   /**
    * Thiết lập trước mỗi test case
    * Khởi tạo các mock và đối tượng ExerciseService
    * 
    * Quy trình:
    * 1. Khởi tạo các mock với annotation @Mock
    * 2. Mock phương thức tĩnh JdbcUtils.getConn() để trả về mockConnection
    * 3. Khởi tạo đối tượng ExerciseService để kiểm thử
    * 4. Thiết lập hành vi mặc định cho các mock connection
    */
   @BeforeEach
   public void setUp() throws SQLException {
       // Khởi tạo các đối tượng mock được định nghĩa với @Mock
       MockitoAnnotations.openMocks(this);
       
       // Giả lập phương thức tĩnh JdbcUtils.getConn()
       mockedStatic = Mockito.mockStatic(JdbcUtils.class);
       mockedStatic.when(JdbcUtils::getConn).thenReturn(mockConnection);
       
       // Khởi tạo đối tượng cần kiểm thử
       exerciseService = new ExerciseService();
       
       // Thiết lập hành vi mặc định cho các phương thức connection thường dùng
       when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
       when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
       when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
           .thenReturn(mockPreparedStatement);
   }
   
   /**
    * Dọn dẹp sau mỗi test case
    * Đóng các tài nguyên đã mở để tránh rò rỉ bộ nhớ
    */
   @AfterEach
   public void tearDown() {
       if (mockedStatic != null) {
           mockedStatic.close();  // Đóng giả lập tĩnh để tránh rò rỉ bộ nhớ
       }
   }
   
   /**
    * Kiểm tra phương thức getExercise không có từ khóa tìm kiếm
    * 
    * Kỳ vọng: Phương thức trả về danh sách tất cả các bài tập
    * 
    * Kịch bản kiểm thử:
    * 1. Thiết lập mockResultSet để giả lập 2 kết quả bài tập
    * 2. Gọi exerciseService.getExercise(null) để lấy tất cả bài tập
    * 3. Kiểm tra số lượng và nội dung các bài tập trả về
    * 4. Xác minh câu SQL được gọi đúng và không có tham số nào được thiết lập
    */
   @Test
   public void testGetExerciseWithoutKeyword() throws SQLException {
       // Thiết lập mock để giả lập kết quả truy vấn
       when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
       when(mockResultSet.next()).thenReturn(true, true, false); // Giả lập có 2 kết quả
       
       // Thiết lập dữ liệu giả cho từng kết quả
       when(mockResultSet.getInt("idExercise")).thenReturn(1, 2);
       when(mockResultSet.getString("exerciseName")).thenReturn("Chạy bộ", "Đạp xe");
       when(mockResultSet.getString("imageExercise")).thenReturn("run.png", "bike.png");
       when(mockResultSet.getFloat("caloriesBurnedPerMin")).thenReturn(5.5f, 7.0f);
       
       // Thực thi phương thức cần kiểm thử - không có từ khóa tìm kiếm
       List<Exercise> result = exerciseService.getExercise(null);
       
       // Kiểm tra kết quả tổng quát
       assertNotNull(result, "Danh sách bài tập không được null");
       assertEquals(2, result.size(), "Danh sách phải có 2 bài tập");
       
       // Kiểm tra chi tiết dữ liệu bài tập đầu tiên
       assertEquals(1, result.get(0).getIdExercise(), "ID bài tập đầu tiên không đúng");
       assertEquals("Chạy bộ", result.get(0).getExerciseName(), "Tên bài tập đầu tiên không đúng");
       assertEquals("run.png", result.get(0).getImageExercise(), "Đường dẫn hình ảnh bài tập đầu tiên không đúng");
       assertEquals(5.5f, result.get(0).getCaloriesBurnedPerMin(), 0.01, "Lượng calo tiêu thụ bài tập đầu tiên không đúng");
       
       // Kiểm tra chi tiết dữ liệu bài tập thứ hai
       assertEquals(2, result.get(1).getIdExercise(), "ID bài tập thứ hai không đúng");
       assertEquals("Đạp xe", result.get(1).getExerciseName(), "Tên bài tập thứ hai không đúng");
       
       // Xác minh câu SQL được gọi đúng và không có tham số nào
       verify(mockConnection).prepareCall("SELECT * FROM exercise");
       verify(mockPreparedStatement, never()).setString(anyInt(), anyString());
   }
   
   /**
    * Kiểm tra phương thức getExercise có từ khóa tìm kiếm
    * 
    * Kỳ vọng: Phương thức trả về danh sách các bài tập phù hợp với từ khóa
    * 
    * Kịch bản kiểm thử:
    * 1. Thiết lập từ khóa tìm kiếm "chạy"
    * 2. Thiết lập mockResultSet để giả lập 1 kết quả bài tập phù hợp
    * 3. Gọi exerciseService.getExercise(keyword) để tìm bài tập
    * 4. Kiểm tra số lượng và nội dung bài tập trả về
    * 5. Xác minh câu SQL được gọi đúng và tham số từ khóa được thiết lập
    */
   @Test
   public void testGetExerciseWithKeyword() throws SQLException {
       // Thiết lập từ khóa tìm kiếm
       String keyword = "chạy";
       
       // Thiết lập mock
       when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
       when(mockResultSet.next()).thenReturn(true, false); // Giả lập có 1 kết quả
       
       // Thiết lập dữ liệu giả cho kết quả tìm kiếm
       when(mockResultSet.getInt("idExercise")).thenReturn(1);
       when(mockResultSet.getString("exerciseName")).thenReturn("Chạy bộ");
       when(mockResultSet.getString("imageExercise")).thenReturn("run.png");
       when(mockResultSet.getFloat("caloriesBurnedPerMin")).thenReturn(5.5f);
       
       // Thực thi phương thức cần kiểm thử - có từ khóa tìm kiếm
       List<Exercise> result = exerciseService.getExercise(keyword);
       
       // Kiểm tra kết quả
       assertNotNull(result, "Danh sách bài tập không được null");
       assertEquals(1, result.size(), "Danh sách phải có 1 bài tập");
       assertEquals("Chạy bộ", result.get(0).getExerciseName(), "Tên bài tập không đúng");
       
       // Xác minh câu SQL được gọi đúng và tham số được thiết lập
       verify(mockConnection).prepareCall("SELECT * FROM exercise WHERE exerciseName like concat ('%', ?, '%')");
       verify(mockPreparedStatement).setString(1, keyword); // Xác minh từ khóa được truyền vào câu SQL
   }
   
   /**
    * Kiểm tra phương thức saveExercise với bài tập hợp lệ
    * 
    * Kỳ vọng: Phương thức trả về true khi lưu thành công
    * 
    * Kịch bản kiểm thử:
    * 1. Tạo đối tượng Exercise với dữ liệu hợp lệ
    * 2. Thiết lập mockPreparedStatement.executeUpdate() trả về 1 (thành công)
    * 3. Thiết lập mockGeneratedKeys để giả lập ID được sinh tự động
    * 4. Gọi exerciseService.saveExercise() và kiểm tra kết quả
    * 5. Xác minh ID được gán cho đối tượng Exercise và các tham số được truyền đúng
    */
   @Test
   public void testSaveExerciseSuccess() throws SQLException {
       // Tạo đối tượng Exercise cần lưu
       Exercise exercise = new Exercise();
       exercise.setExerciseName("Nhảy dây");
       exercise.setCaloriesBurnedPerMin(8.0);
       
       // Thiết lập mock cho câu lệnh insert
       when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 dòng bị ảnh hưởng = thành công
       
       // Thiết lập mock cho kết quả ID được sinh tự động
       ResultSet mockGeneratedKeys = mock(ResultSet.class);
       when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
       when(mockGeneratedKeys.next()).thenReturn(true);
       when(mockGeneratedKeys.getInt(1)).thenReturn(3); // ID được sinh = 3
       
       // Thực thi phương thức cần kiểm thử
       boolean result = exerciseService.saveExercise(exercise);
       
       // Kiểm tra kết quả
       assertTrue(result, "Phương thức phải trả về true khi lưu thành công");
       assertEquals(3, exercise.getIdExercise(), "ID được tạo không được gán cho đối tượng");
       
       // Xác minh các tham số được truyền đúng
       verify(mockPreparedStatement).setString(1, "Nhảy dây");
       verify(mockPreparedStatement).setDouble(2, 8.0);
       verify(mockPreparedStatement).executeUpdate();
   }
   
   /**
    * Kiểm tra phương thức saveExercise khi có lỗi xảy ra (không có dòng nào bị ảnh hưởng)
    * 
    * Kỳ vọng: Phương thức trả về false khi lưu thất bại
    * 
    * Kịch bản kiểm thử:
    * 1. Tạo đối tượng Exercise với dữ liệu hợp lệ
    * 2. Thiết lập mockPreparedStatement.executeUpdate() trả về 0 (không thành công)
    * 3. Gọi exerciseService.saveExercise() và kiểm tra kết quả
    * 4. Xác minh executeUpdate() được gọi
    */
   @Test
   public void testSaveExerciseFail() throws SQLException {
       // Tạo đối tượng Exercise cần lưu
       Exercise exercise = new Exercise();
       exercise.setExerciseName("Nhảy dây");
       exercise.setCaloriesBurnedPerMin(8.0);
       
       // Thiết lập mock - executeUpdate() trả về 0 (không có dòng nào bị ảnh hưởng)
       when(mockPreparedStatement.executeUpdate()).thenReturn(0);
       
       // Thực thi phương thức cần kiểm thử
       boolean result = exerciseService.saveExercise(exercise);
       
       // Kiểm tra kết quả - Kỳ vọng false khi lưu thất bại
       assertFalse(result, "Phương thức phải trả về false khi lưu thất bại");
       
       // Xác minh executeUpdate() được gọi
       verify(mockPreparedStatement).executeUpdate();
   }
   
   /**
    * Kiểm tra phương thức saveExercise khi SQLException được ném ra
    * 
    * Kỳ vọng: Phương thức trả về false khi xảy ra lỗi SQL
    * 
    * Kịch bản kiểm thử:
    * 1. Tạo đối tượng Exercise với dữ liệu hợp lệ
    * 2. Thiết lập mockPreparedStatement.executeUpdate() ném SQLException
    * 3. Gọi exerciseService.saveExercise() và kiểm tra kết quả
    * 4. Xác minh kết quả là false (lưu thất bại)
    */
   @Test
   public void testSaveExerciseWithSQLException() throws SQLException {
       // Tạo đối tượng Exercise cần lưu
       Exercise exercise = new Exercise();
       exercise.setExerciseName("Nhảy dây");
       exercise.setCaloriesBurnedPerMin(8.0);
       
       // Thiết lập mock để ném SQLException khi gọi executeUpdate()
       when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));
       
       // Thực thi phương thức cần kiểm thử
       boolean result = exerciseService.saveExercise(exercise);
       
       // Kiểm tra kết quả - Kỳ vọng false khi có lỗi SQL
       assertFalse(result, "Phương thức phải trả về false khi có lỗi SQL");
   }
   
   /**
    * Kiểm tra phương thức getExerciseByName với tên hợp lệ và tìm thấy kết quả
    * 
    * Kỳ vọng: Phương thức trả về đối tượng Exercise khi tìm thấy
    * 
    * Kịch bản kiểm thử:
    * 1. Thiết lập tên bài tập cần tìm
    * 2. Thiết lập mockResultSet để giả lập tìm thấy bài tập
    * 3. Thiết lập mocks để xử lý kiểm tra tồn tại cột
    * 4. Gọi exerciseService.getExerciseByName() và kiểm tra kết quả
    * 5. Xác minh các thuộc tính của đối tượng trả về và tham số được truyền đúng
    */
   @Test
   public void testGetExerciseByNameFound() throws SQLException {
       // Thiết lập tên bài tập cần tìm
       String name = "Chạy bộ";
       
       // Thiết lập mock để giả lập tìm thấy kết quả
       when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
       when(mockResultSet.next()).thenReturn(true); // Có kết quả
       
       // Thiết lập dữ liệu giả cho kết quả
       when(mockResultSet.getInt("idExercise")).thenReturn(1);
       when(mockResultSet.getString("exerciseName")).thenReturn("Chạy bộ");
       when(mockResultSet.getDouble("caloriesBurnedPerMin")).thenReturn(5.5);
       
       // Đảm bảo rằng phương thức hasColumn() hoạt động đúng
       // Giả lập cột caloriesBurnedPerMin tồn tại trong kết quả truy vấn
       when(mockResultSet.findColumn("caloriesBurnedPerMin")).thenReturn(3); // Cột tồn tại
       when(mockResultSet.findColumn("Lượng calo tiêu thụ:"))
           .thenThrow(new SQLException("Column not found"));
       
       // Thực thi phương thức cần kiểm thử
       Exercise result = exerciseService.getExerciseByName(name);
       
       // Kiểm tra kết quả
       assertNotNull(result, "Kết quả phải khác null khi tìm thấy bài tập");
       assertEquals(1, result.getIdExercise(), "ID bài tập không đúng");
       assertEquals("Chạy bộ", result.getExerciseName(), "Tên bài tập không đúng");
       assertEquals(5.5, result.getCaloriesBurnedPerMin(), 0.01, "Lượng calo tiêu thụ không đúng");
       
       // Xác minh tham số được truyền đúng - đảm bảo đã trim tên bài tập
       verify(mockPreparedStatement).setString(1, name.trim());
   }
   
   /**
    * Kiểm tra phương thức getExerciseByName với tên không tồn tại
    * 
    * Kỳ vọng: Phương thức trả về null khi không tìm thấy
    * 
    * Kịch bản kiểm thử:
    * 1. Thiết lập tên bài tập không tồn tại
    * 2. Thiết lập mockResultSet.next() trả về false (không có kết quả)
    * 3. Gọi exerciseService.getExerciseByName() và kiểm tra kết quả
    * 4. Xác minh kết quả là null và tham số được truyền đúng
    */
   @Test
   public void testGetExerciseByNameNotFound() throws SQLException {
       // Thiết lập tên bài tập không tồn tại
       String name = "Không tồn tại";
       
       // Thiết lập mock để giả lập không tìm thấy kết quả
       when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
       when(mockResultSet.next()).thenReturn(false); // Không có kết quả
       
       // Thực thi phương thức cần kiểm thử
       Exercise result = exerciseService.getExerciseByName(name);
       
       // Kiểm tra kết quả - Kỳ vọng null khi không tìm thấy
       assertNull(result, "Kết quả phải là null khi không tìm thấy bài tập");
       
       // Xác minh tham số được truyền đúng - đảm bảo đã trim tên bài tập
       verify(mockPreparedStatement).setString(1, name.trim());
   }
   
   /**
    * Kiểm tra phương thức getExerciseByName với tên là null hoặc rỗng
    * 
    * Kỳ vọng: Phương thức trả về null đối với tên không hợp lệ
    * 
    * Kịch bản kiểm thử:
    * 1. Kiểm tra 3 trường hợp: null, chuỗi rỗng, chuỗi chỉ chứa khoảng trắng
    * 2. Với mỗi trường hợp, gọi exerciseService.getExerciseByName() và kiểm tra kết quả
    * 3. Xác minh không có tương tác với database khi tên không hợp lệ
    */
   @Test
   public void testGetExerciseByNameWithNullOrEmpty() throws SQLException {
       // Test với tên là null
       Exercise result1 = exerciseService.getExerciseByName(null);
       assertNull(result1, "Kết quả phải là null khi tên bài tập là null");
       
       // Test với tên là chuỗi rỗng
       Exercise result2 = exerciseService.getExerciseByName("");
       assertNull(result2, "Kết quả phải là null khi tên bài tập là chuỗi rỗng");
       
       // Test với tên chỉ chứa khoảng trắng
       Exercise result3 = exerciseService.getExerciseByName("   ");
       assertNull(result3, "Kết quả phải là null khi tên bài tập chỉ chứa khoảng trắng");
       
       // Xác minh không có tương tác với database khi tên không hợp lệ
       // Không gọi prepareStatement khi tên bài tập không hợp lệ
       verify(mockConnection, never()).prepareStatement(anyString());
   }
   
   /**
    * Kiểm tra phương thức getExerciseByName khi SQLException được ném ra
    * 
    * Kỳ vọng: Phương thức ném lại SQLException
    * 
    * Kịch bản kiểm thử:
    * 1. Thiết lập tên bài tập hợp lệ
    * 2. Thiết lập mockPreparedStatement.executeQuery() ném SQLException
    * 3. Gọi exerciseService.getExerciseByName() và kiểm tra ngoại lệ
    * 4. Xác minh tham số được truyền đúng
    */
   @Test
   public void testGetExerciseByNameWithSQLException() throws SQLException {
       // Thiết lập tên bài tập cần tìm
       String name = "Chạy bộ";
       
       // Thiết lập mock để ném SQLException khi gọi executeQuery()
       when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));
       
       // Thực thi phương thức và kiểm tra ngoại lệ
       // Kỳ vọng: Phương thức ném lại SQLException
       assertThrows(SQLException.class, () -> {
           exerciseService.getExerciseByName(name);
       }, "Phương thức phải ném SQLException khi có lỗi database");
       
       // Xác minh tham số được truyền đúng - đảm bảo đã trim tên bài tập
       verify(mockPreparedStatement).setString(1, name.trim());
   }
}
