package test;

import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.Exerciselog;
import com.milkyway.pojo.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Date;
import java.time.LocalDate;

/**
 * Lớp kiểm thử cho đối tượng POJO Exerciselog
 * Lớp này kiểm tra tất cả chức năng của lớp Exerciselog, bao gồm:
 * - Tất cả các constructor hoạt động chính xác
 * - Tất cả các getter và setter hoạt động đúng
 * - Các phương thức như equals, hashCode và toString hoạt động như mong đợi
 */
public class ExerciseLogTest {
    
    /**
     * Kiểm tra constructor mặc định không tham số
     * Xác minh rằng tất cả các trường được khởi tạo là null hoặc giá trị mặc định
     */
    @Test
    public void testDefaultConstructor() {
        // Tạo đối tượng sử dụng constructor mặc định
        Exerciselog exerciseLog = new Exerciselog();
        
        // Xác minh tất cả các trường ban đầu đều là null hoặc giá trị mặc định
        assertNull(exerciseLog.getIdExLog(), "ID ban đầu phải là null");
        assertNull(exerciseLog.getEffortLevel(), "Mức độ nỗ lực ban đầu phải là null");
        assertEquals(0, exerciseLog.getDuration(), "Thời gian ban đầu phải là 0");
        assertEquals(0.0, exerciseLog.getEnergyBurn(), 0.001, "Lượng calo tiêu thụ ban đầu phải là 0.0");
        assertNull(exerciseLog.getDatetime(), "Thời gian ban đầu phải là null");
        assertNull(exerciseLog.getExerciseId(), "Bài tập ban đầu phải là null");
        assertNull(exerciseLog.getUserId(), "Người dùng ban đầu phải là null");
    }
    
    /**
     * Kiểm tra constructor chỉ nhận tham số ID
     * Xác minh ID được thiết lập đúng và các trường khác vẫn là null
     */
    @Test
    public void testConstructorWithId() {
        // Tạo giá trị ID kiểm tra
        Integer id = 1;
        
        // Tạo đối tượng với constructor chỉ có ID
        Exerciselog exerciseLog = new Exerciselog(id);
        
        // Xác minh ID được thiết lập đúng và trường khác vẫn là null
        assertEquals(id, exerciseLog.getIdExLog(), "ID phải khớp với tham số constructor");
        assertNull(exerciseLog.getEffortLevel(), "Mức độ nỗ lực vẫn phải là null");
        assertEquals(0, exerciseLog.getDuration(), "Thời gian vẫn phải là 0");
    }
    
    /**
     * Kiểm tra constructor với các tham số cơ bản
     * Xác minh các trường được thiết lập chính xác
     */
    @Test
    public void testConstructorWithBasicParams() {
        // Tạo giá trị kiểm tra
        Integer id = 1;
        int duration = 30;
        double energyBurn = 150.5;
        Date datetime = Date.valueOf(LocalDate.now());
        
        // Tạo đối tượng với constructor các tham số cơ bản
        Exerciselog exerciseLog = new Exerciselog(id, duration, energyBurn, datetime);
        
        // Xác minh các trường được thiết lập đúng
        assertEquals(id, exerciseLog.getIdExLog(), "ID phải khớp với tham số constructor");
        assertEquals(duration, exerciseLog.getDuration(), "Thời gian phải khớp với tham số constructor");
        assertEquals(energyBurn, exerciseLog.getEnergyBurn(), 0.001, "Lượng calo tiêu thụ phải khớp với tham số constructor");
        assertEquals(datetime, exerciseLog.getDatetime(), "Thời gian phải khớp với tham số constructor");
    }
    
    /**
     * Kiểm tra constructor với thêm tham số Exercise
     * Xác minh các trường được thiết lập chính xác
     */
    @Test
    public void testConstructorWithExercise() {
        // Tạo giá trị kiểm tra
        Integer id = 1;
        int duration = 30;
        double energyBurn = 150.5;
        Date datetime = Date.valueOf(LocalDate.now());
        Exercise exercise = new Exercise(1, "Chạy bộ");
        
        // Tạo đối tượng với constructor bao gồm Exercise
        Exerciselog exerciseLog = new Exerciselog(id, duration, energyBurn, datetime, exercise);
        
        // Xác minh các trường được thiết lập đúng
        assertEquals(id, exerciseLog.getIdExLog(), "ID phải khớp với tham số constructor");
        assertEquals(duration, exerciseLog.getDuration(), "Thời gian phải khớp với tham số constructor");
        assertEquals(energyBurn, exerciseLog.getEnergyBurn(), 0.001, "Lượng calo tiêu thụ phải khớp với tham số constructor");
        assertEquals(datetime, exerciseLog.getDatetime(), "Thời gian phải khớp với tham số constructor");
        assertEquals(exercise, exerciseLog.getExerciseId(), "Bài tập phải khớp với tham số constructor");
    }
    
    /**
     * Kiểm tra setter và getter cho trường idExLog
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetIdExLog() {
        // Tạo đối tượng và giá trị kiểm tra
        Exerciselog exerciseLog = new Exerciselog();
        Integer id = 10;
        
        // Kiểm tra setter
        exerciseLog.setIdExLog(id);
        
        // Xác minh getter trả về giá trị chính xác
        assertEquals(id, exerciseLog.getIdExLog(), "getIdExLog phải trả về giá trị đã thiết lập bởi setIdExLog");
    }
    
    /**
     * Kiểm tra setter và getter cho trường effortLevel
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetEffortLevel() {
        // Tạo đối tượng và giá trị kiểm tra
        Exerciselog exerciseLog = new Exerciselog();
        String effortLevel = "Cao";
        
        // Kiểm tra setter
        exerciseLog.setEffortLevel(effortLevel);
        
        // Xác minh getter trả về giá trị chính xác
        assertEquals(effortLevel, exerciseLog.getEffortLevel(), "getEffortLevel phải trả về giá trị đã thiết lập bởi setEffortLevel");
    }
    
    /**
     * Kiểm tra setter và getter cho trường duration
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetDuration() {
        // Tạo đối tượng và giá trị kiểm tra
        Exerciselog exerciseLog = new Exerciselog();
        int duration = 45;
        
        // Kiểm tra setter
        exerciseLog.setDuration(duration);
        
        // Xác minh getter trả về giá trị chính xác
        assertEquals(duration, exerciseLog.getDuration(), "getDuration phải trả về giá trị đã thiết lập bởi setDuration");
    }
    
    /**
     * Kiểm tra setter và getter cho trường datetime
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetDatetime() {
        // Tạo đối tượng và giá trị kiểm tra
        Exerciselog exerciseLog = new Exerciselog();
        Date datetime = Date.valueOf(LocalDate.now());
        
        // Kiểm tra setter
        exerciseLog.setDatetime(datetime);
        
        // Xác minh getter trả về giá trị chính xác
        assertEquals(datetime, exerciseLog.getDatetime(), "getDatetime phải trả về giá trị đã thiết lập bởi setDatetime");
    }
    
    /**
     * Kiểm tra setter và getter cho trường energyBurn
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetEnergyBurn() {
        // Tạo đối tượng và giá trị kiểm tra
        Exerciselog exerciseLog = new Exerciselog();
        double energyBurn = 200.75;
        
        // Kiểm tra setter
        exerciseLog.setEnergyBurn(energyBurn);
        
        // Xác minh getter trả về giá trị chính xác
        assertEquals(energyBurn, exerciseLog.getEnergyBurn(), 0.001, "getEnergyBurn phải trả về giá trị đã thiết lập bởi setEnergyBurn");
    }
    
    /**
     * Kiểm tra setter và getter cho trường exerciseId
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetExerciseId() {
        // Tạo đối tượng và giá trị kiểm tra
        Exerciselog exerciseLog = new Exerciselog();
        Exercise exercise = new Exercise(1, "Đạp xe");
        
        // Kiểm tra setter
        exerciseLog.setExerciseId(exercise);
        
        // Xác minh getter trả về giá trị chính xác
        assertEquals(exercise, exerciseLog.getExerciseId(), "getExerciseId phải trả về giá trị đã thiết lập bởi setExerciseId");
    }
    
    /**
     * Kiểm tra setter và getter cho trường userId
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetUserId() {
        // Tạo đối tượng và giá trị kiểm tra
        Exerciselog exerciseLog = new Exerciselog();
        User user = new User(1);
        
        // Kiểm tra setter
        exerciseLog.setUserId(user);
        
        // Xác minh getter trả về giá trị chính xác
        assertEquals(user, exerciseLog.getUserId(), "getUserId phải trả về giá trị đã thiết lập bởi setUserId");
    }
    
    /**
     * Kiểm tra phương thức equals và hashCode
     * Xác minh rằng:
     * - Các đối tượng có cùng ID được coi là bằng nhau
     * - Các đối tượng có ID khác nhau không bằng nhau
     * - Một đối tượng không bằng null hoặc các kiểu khác
     * - Một đối tượng bằng chính nó
     */
    @Test
    public void testEqualsAndHashCode() {
        // Tạo đối tượng kiểm tra
        Exerciselog log1 = new Exerciselog(1);
        Exerciselog log2 = new Exerciselog(1);
        Exerciselog log3 = new Exerciselog(2);
        
        // Kiểm tra phương thức equals
        assertEquals(log1, log2, "Các đối tượng có cùng ID phải bằng nhau");
        assertNotEquals(log1, log3, "Các đối tượng có ID khác nhau không được bằng nhau");
        assertNotEquals(log1, null, "Đối tượng không được bằng null");
        assertNotEquals(log1, new Object(), "Exerciselog không được bằng đối tượng của lớp khác");
        assertEquals(log1, log1, "Đối tượng phải bằng chính nó");
        
        // Kiểm tra tính nhất quán của hashCode
        assertEquals(log1.hashCode(), log2.hashCode(), "Các đối tượng bằng nhau phải có cùng hashCode");
    }
    
    /**
     * Kiểm tra phương thức toString
     * Xác minh chuỗi biểu diễn chứa ID của đối tượng
     */
    @Test
    public void testToString() {
        // Tạo đối tượng kiểm tra
        Integer id = 1;
        Exerciselog exerciseLog = new Exerciselog(id);
        
        // Lấy chuỗi biểu diễn
        String toString = exerciseLog.toString();
        
        // Xác minh chuỗi không null và chứa ID
        assertNotNull(toString, "toString không được trả về null");
        assertTrue(toString.contains(id.toString()), "toString phải chứa ID của đối tượng");
    }
}
