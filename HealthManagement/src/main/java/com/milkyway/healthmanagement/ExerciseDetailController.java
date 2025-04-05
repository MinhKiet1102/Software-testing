package com.milkyway.healthmanagement;

import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.Exerciselog;
import com.milkyway.pojo.User;
import com.milkyway.service.ExerciseLogService;
import com.milkyway.service.ExerciseService;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
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

        caloriesBox.setVisible(false);

        btnCancel.setOnAction(event -> closeWindow());

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
            String exerciseName = txtExercise.getText();
            System.err.println(exerciseName);
            String effortLevel = txtEffort.getValue();
            double caloriesPerMinute = Double.parseDouble(txtCaloriesPerMinute.getText());
            int duration = Integer.parseInt(txtDuration.getText());

            LocalDate selectedDate = dtpDate.getValue();
            if (selectedDate == null) {
                System.out.println("Chưa chọn ngày!");
                return;
            }
            Date date = java.sql.Date.valueOf(selectedDate);

            // Nếu exercise chưa có ID thì là bài tập mới => thêm vào database
            if (exercise.getIdExercise() == null) {
                Exercise newExercise = new Exercise();
                newExercise.setExerciseName(exerciseName);
                newExercise.setCaloriesBurnedPerMin(caloriesPerMinute);
                newExercise.setImageExercise("");

                exerciseService.saveExercise(newExercise);

                // Sau khi thêm thì mình phải lấy lại thông tin (vì ID mới được sinh ra trong DB)
                Exercise insertedExercise = exerciseService.getExerciseByName(exerciseName);
                exercise = insertedExercise; // cập nhật lại biến exercise hiện tại
            }

            // Tạo ExerciseLog dựa trên exercise
            Exerciselog exerciseLog = new Exerciselog();
            exerciseLog.setDatetime(date);
            exerciseLog.setExerciseId(exercise);
            exerciseLog.setEffortLevel(effortLevel);
            exerciseLog.setEnergyBurn(caloriesPerMinute * duration);
            exerciseLog.setDuration(duration);
            exerciseLog.setUserId(User.getCurrentUser());

            ExerciseLogService logService = new ExerciseLogService();
            logService.saveLog(exerciseLog);

            boolean logSaved = logService.saveLog(exerciseLog);

            if (logSaved) {
                showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Dữ liệu đã được lưu thành công!");
                closeWindow(); 
            } else {
                // Thông báo lỗi nếu saveLog trả về false (nhưng không ném exception)
                showAlert(Alert.AlertType.ERROR, "Lỗi Lưu", "Không thể lưu nhật ký tập luyện (service trả về false).");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
