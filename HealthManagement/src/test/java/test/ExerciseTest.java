package test;

import com.milkyway.pojo.Exercise;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import com.milkyway.pojo.Exerciselog;

/**
 * Lớp kiểm thử cho đối tượng POJO Exercise
 * Lớp này kiểm tra tất cả chức năng của lớp Exercise, bao gồm:
 * - Tất cả các constructor hoạt động chính xác
 * - Tất cả các getter và setter hoạt động đúng
 * - Các phương thức như equals, hashCode và toString hoạt động như mong đợi
 */
public class ExerciseTest {

    /**
     * Kiểm tra constructor mặc định không tham số
     * Xác minh rằng tất cả các trường được khởi tạo là null
     */
    @Test
    public void testDefaultConstructor() {
        // Tạo đối tượng sử dụng constructor mặc định
        Exercise exercise = new Exercise();
        
        // Xác minh tất cả các trường ban đầu đều là null
        assertNull(exercise.getIdExercise(), "ID ban đầu phải là null");
        assertNull(exercise.getExerciseName(), "Tên bài tập ban đầu phải là null");
        assertNull(exercise.getImageExercise(), "Hình ảnh ban đầu phải là null");
        assertNull(exercise.getCaloriesBurnedPerMin(), "Lượng calorie ban đầu phải là null");
        assertNull(exercise.getExerciselogSet(), "Tập ExerciselogSet ban đầu phải là null");
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
        Exercise exercise = new Exercise(id);
        
        // Xác minh ID được thiết lập đúng và trường khác vẫn là null
        assertEquals(id, exercise.getIdExercise(), "ID phải khớp với tham số constructor");
        assertNull(exercise.getExerciseName(), "Tên bài tập vẫn phải là null");
    }

    /**
     * Kiểm tra constructor nhận tham số ID và tên
     * Xác minh cả hai trường được thiết lập đúng
     */
    @Test
    public void testConstructorWithIdAndName() {
        // Tạo giá trị kiểm tra
        Integer id = 1;
        String name = "Running";
        
        // Tạo đối tượng với constructor ID và tên
        Exercise exercise = new Exercise(id, name);
        
        // Xác minh các trường được thiết lập đúng
        assertEquals(id, exercise.getIdExercise(), "ID phải khớp với tham số constructor");
        assertEquals(name, exercise.getExerciseName(), "Tên bài tập phải khớp với tham số constructor");
    }

    /**
     * Kiểm tra constructor nhận tất cả các tham số
     * Xác minh tất cả các trường được thiết lập đúng
     */
    @Test
    public void testConstructorWithAllParams() {
        // Tạo giá trị kiểm tra cho tất cả các trường
        Integer id = 1;
        String name = "Running";
        String image = "running.jpg";
        double calories = 10.5;
        
        // Tạo đối tượng với constructor đầy đủ
        Exercise exercise = new Exercise(id, name, image, calories);
        
        // Xác minh tất cả các trường được thiết lập đúng
        assertEquals(id, exercise.getIdExercise(), "ID phải khớp với tham số constructor");
        assertEquals(name, exercise.getExerciseName(), "Tên bài tập phải khớp với tham số constructor");
        assertEquals(image, exercise.getImageExercise(), "Hình ảnh phải khớp với tham số constructor");
        assertEquals(calories, exercise.getCaloriesBurnedPerMin(), 0.001, "Lượng calorie phải khớp với delta cho so sánh số thực");
    }

    /**
     * Kiểm tra setter và getter cho trường idExercise
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetIdExercise() {
        // Tạo đối tượng và giá trị kiểm tra
        Exercise exercise = new Exercise();
        Integer id = 10;
        
        // Kiểm tra setter
        exercise.setIdExercise(id);
        
        // Xác minh getter trả về giá trị chính xác
        assertEquals(id, exercise.getIdExercise(), "getIdExercise phải trả về giá trị đã thiết lập bởi setIdExercise");
    }

    /**
     * Kiểm tra setter và getter cho trường exerciseName
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetExerciseName() {
        // Tạo đối tượng và giá trị kiểm tra
        Exercise exercise = new Exercise();
        String name = "Swimming";
        
        // Kiểm tra setter
        exercise.setExerciseName(name);
        
        // Xác minh getter trả về giá trị chính xác
        assertEquals(name, exercise.getExerciseName(), "getExerciseName phải trả về giá trị đã thiết lập bởi setExerciseName");
    }

    /**
     * Kiểm tra setter và getter cho trường imageExercise
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetImageExercise() {
        // Tạo đối tượng và giá trị kiểm tra
        Exercise exercise = new Exercise();
        String image = "swimming.jpg";
        
        // Kiểm tra setter
        exercise.setImageExercise(image);
        
        // Xác minh getter trả về giá trị chính xác
        assertEquals(image, exercise.getImageExercise(), "getImageExercise phải trả về giá trị đã thiết lập bởi setImageExercise");
    }

    /**
     * Kiểm tra setter và getter cho trường caloriesBurnedPerMin
     * Xác minh giá trị được lưu trữ và truy xuất chính xác
     * Sử dụng delta cho so sánh số thực
     */
    @Test
    public void testSetAndGetCaloriesBurnedPerMin() {
        // Tạo đối tượng và giá trị kiểm tra
        Exercise exercise = new Exercise();
        Double calories = 15.75;
        
        // Kiểm tra setter
        exercise.setCaloriesBurnedPerMin(calories);
        
        // Xác minh getter trả về giá trị chính xác với delta cho so sánh số thực
        assertEquals(calories, exercise.getCaloriesBurnedPerMin(), 0.001, 
                "getCaloriesBurnedPerMin phải trả về giá trị đã thiết lập bởi setCaloriesBurnedPerMin");
    }

    /**
     * Kiểm tra setter và getter cho trường exerciselogSet
     * Xác minh rằng tập hợp được lưu trữ và truy xuất chính xác
     */
    @Test
    public void testSetAndGetExerciselogSet() {
        // Tạo đối tượng và tập hợp rỗng
        Exercise exercise = new Exercise();
        Set<Exerciselog> logs = new HashSet<>();
        
        // Kiểm tra setter
        exercise.setExerciselogSet(logs);
        
        // Xác minh getter trả về tập hợp không null và rỗng
        assertNotNull(exercise.getExerciselogSet(), "getExerciselogSet không được trả về null");
        assertTrue(exercise.getExerciselogSet().isEmpty(), "Tập hợp trả về phải rỗng");
    }
    
    /**
     * Kiểm tra phương thức equals và hashCode
     * Xác minh rằng:
     * - Các đối tượng có cùng ID được coi là bằng nhau
     * - Các đối tượng có ID khác nhau không bằng nhau
     * - Một đối tượng không bằng null hoặc các kiểu khác
     * - Một đối tượng bằng chính nó
     * - hashCode nhất quán với equals
     */
    @Test
    public void testEqualsAndHashCode() {
        // Tạo đối tượng kiểm tra
        Exercise exercise1 = new Exercise(1, "Running");
        Exercise exercise2 = new Exercise(1, "Running");
        Exercise exercise3 = new Exercise(2, "Swimming");
        
        // Kiểm tra phương thức equals cho các trường hợp khác nhau
        assertEquals(exercise1, exercise2, "Các đối tượng có cùng ID phải bằng nhau");
        assertNotEquals(exercise1, exercise3, "Các đối tượng có ID khác nhau không được bằng nhau");
        assertNotEquals(null, exercise1, "Đối tượng không được bằng null");
        assertNotEquals(exercise1, new Object(), "Exercise không được bằng đối tượng của lớp khác");
        assertEquals(exercise1, exercise1, "Đối tượng phải bằng chính nó");
        
        // Kiểm tra tính nhất quán của hashCode với equals
        assertEquals(exercise1.hashCode(), exercise2.hashCode(), 
                "Các đối tượng bằng nhau phải có cùng hashCode");
    }
    
    /**
     * Kiểm tra phương thức toString
     * Xác minh rằng chuỗi biểu diễn:
     * - Không phải null
     * - Chứa ID của đối tượng
     */
    @Test
    public void testToString() {
        // Tạo đối tượng kiểm tra
        Exercise exercise = new Exercise(1, "Running");
        
        // Lấy chuỗi biểu diễn
        String toString = exercise.toString();
        
        // Xác minh chuỗi không phải null và chứa ID
        assertNotNull(toString, "toString không được trả về null");
        assertTrue(toString.contains("1"), "toString phải chứa ID của đối tượng");
    }
}

