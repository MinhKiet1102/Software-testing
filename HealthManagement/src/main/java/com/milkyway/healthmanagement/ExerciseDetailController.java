package com.milkyway.healthmanagement;

import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.Exerciselog;
import com.milkyway.pojo.User;
import com.milkyway.services.ExerciseLogService;
import com.milkyway.services.ExerciseService;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author ASUS
 */
public class ExerciseDetailController extends SwitchSceneController implements Initializable {

    @FXML
    private DatePicker dtpDate;

    @FXML
    private TextField txtDuration;

    @FXML
    private ComboBox<String> txtEffort;

    @FXML
    private TextField txtExercise;

    @FXML
    private TextField txtCaloriesPerMinute;

    @FXML
    private HBox caloriesBox;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    private ExerciseService exerciseService = new ExerciseService();
    private Exercise exercise;
    private boolean isAddingNew = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtEffort.getItems().addAll("Nhẹ", "Vừa", "Nặng");
        //mặc định của dtpDate là ngày hiện tại
        dtpDate.setValue(LocalDate.now());
        caloriesBox.setVisible(false);

        btnCancel.setOnAction(event -> closeWindow(btnCancel));

        // Khi click vào txtExercise, nếu đang thêm mới thì hiện ô nhập calo
        txtExercise.setOnMouseClicked((MouseEvent event) -> {
            if (isAddingNew) {
                caloriesBox.setVisible(true);
            }
        });
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;

        if (exercise == null) {
            this.exercise = new Exercise();
            isAddingNew = true;
            txtExercise.setEditable(true);
            txtExercise.clear();
            caloriesBox.setVisible(true);
        } else {
            isAddingNew = false;
            txtExercise.setText(exercise.getExerciseName());
            txtExercise.setEditable(false);
            txtCaloriesPerMinute.setText(String.valueOf(exercise.getCaloriesBurnedPerMin()));
            caloriesBox.setVisible(false);
        }
    }

    @FXML
    private void saveExercise(ActionEvent event) {
        try {
            double caloriesPerMinute = exercise.getCaloriesBurnedPerMin();
            //kiểm tra exerciseName có chứa kí tự đặc biệt hay không, exerciseName được phép sử dụng tiếng Việt và khoảng trắng
            if (txtExercise.getText().matches(".*[!@#$%^&*()_+={}|;':\",.<>?`~].*")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên bài tập không được chứa kí tự đặc biệt!");
                // sau khi thông báo lỗi thì cho người dùng nhập lại
                txtExercise.requestFocus();
                return;
            }
            // kiểm tra exerciseName có null hay không hoặc có chứa kí tự đặc biệt hay không
            if (txtExercise.getText() == null || txtExercise.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên bài tập không được để trống!");
                // sau khi thông báo lỗi thì cho người dùng nhập lại
                txtExercise.requestFocus();
                return;
            }
            String exerciseName = txtExercise.getText();
            // kiểm tra duration có null hay không hoặc có chứa kí tự đặc biệt hay không
            if (txtDuration.getText() == null || txtDuration.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời gian không được để trống!");
                // sau khi thông báo lỗi thì cho người dùng nhập lại
                txtDuration.requestFocus();
                return;
            }
            // kiểm tra duration có phải là số hay không
            if (!txtDuration.getText().matches("[0-9]+")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời gian phải là số!");
                // sau khi thông báo lỗi thì cho người dùng nhập lại
                txtDuration.requestFocus();
                return;
            }
            int duration = Integer.parseInt(txtDuration.getText());
            // kiểm tra effortLevel có null hay không hoặc có chứa kí tự đặc biệt hay không
            if (txtEffort.getValue() == null || txtEffort.getValue().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mức độ nỗ lực không được để trống!");
                // sau khi thông báo lỗi thì cho người dùng nhập lại
                txtEffort.requestFocus();
                return;
            }
            String effortLevel = txtEffort.getValue();
            // kiểm tra dtpDate có null hay không hoặc có chứa kí tự đặc biệt hay không
            if (dtpDate.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Ngày không được để trống!");
                // sau khi thông báo lỗi thì cho người dùng nhập lại
                dtpDate.requestFocus();
                return;
            }
            // chỉ cho chọn ngày trong quá khứ
            if (dtpDate.getValue().isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Ngày không được chọn trong tương lai!");
                // sau khi thông báo lỗi thì cho người dùng nhập lại
                dtpDate.requestFocus();
                return;
            }
            LocalDate selectedDate = dtpDate.getValue();
            Date date = java.sql.Date.valueOf(selectedDate);

            // Nếu exercise chưa có ID thì là bài tập mới => thêm vào database
            if (exercise.getIdExercise() == null) {
                // kiểm tra caloriesPerMinute có null hay không hoặc có chứa kí tự đặc biệt hay không
                if (txtCaloriesPerMinute.getText() == null || txtCaloriesPerMinute.getText().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lượng calo tiêu thụ không được để trống!");
                    // sau khi thông báo lỗi thì cho người dùng nhập lại
                    txtCaloriesPerMinute.requestFocus();
                    return;
                }
                // kiểm tra caloriesPerMinute có phải là số hay không (cho phép nhập số thực)
                if (!txtCaloriesPerMinute.getText().matches("[0-9]+(\\.[0-9]+)?")) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lượng calo tiêu thụ phải là số!");
                    // sau khi thông báo lỗi thì cho người dùng nhập lại
                    txtCaloriesPerMinute.requestFocus();
                    return;
                }
                
                caloriesPerMinute = Double.parseDouble(txtCaloriesPerMinute.getText());

                Exercise newExercise = new Exercise();
                newExercise.setExerciseName(exerciseName);
                newExercise.setCaloriesBurnedPerMin(caloriesPerMinute);
                newExercise.setImageExercise("");

                exerciseService.saveExercise(newExercise);

                // Sau khi thêm thì mình phải lấy lại thông tin (vì ID mới được sinh ra trong DB)
                Exercise insertedExercise = exerciseService.getExerciseByName(exerciseName);
                exercise = insertedExercise; // cập nhật lại biến exercise hiện tại
            }
            //tính toán caloriesBurned và chỉ lấy tối đa 2 chữ số thập phân
            double caloriesBurned = duration * caloriesPerMinute;
            caloriesBurned = Math.round(caloriesBurned * 100.0) / 100.0;
            // Tạo ExerciseLog dựa trên exercise
            Exerciselog exerciseLog = new Exerciselog();
            exerciseLog.setDatetime(date);
            exerciseLog.setExerciseId(exercise);
            exerciseLog.setEffortLevel(effortLevel);
            exerciseLog.setEnergyBurn(caloriesBurned);
            exerciseLog.setDuration(duration);
            exerciseLog.setUserId(User.getCurrentUser());
            
            ExerciseLogService logService = new ExerciseLogService();
            
            boolean logSaved = logService.saveLog(exerciseLog);

            if (logSaved) {
                showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Dữ liệu đã được lưu thành công!");
                closeWindow(btnCancel);
            } else {

                showAlert(Alert.AlertType.ERROR, "Lỗi Lưu", "Không thể lưu nhật ký tập luyện (service trả về false).");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
