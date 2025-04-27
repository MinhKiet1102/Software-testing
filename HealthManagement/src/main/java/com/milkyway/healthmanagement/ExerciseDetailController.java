package com.milkyway.healthmanagement;

import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.Exerciselog;
import com.milkyway.pojo.User;
import com.milkyway.services.ExerciseLogService;
import com.milkyway.services.ExerciseService;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
    
    @FXML
    private Button btnMinimize;
    
    @FXML
    private Button btnClose;

    private ExerciseService exerciseService = new ExerciseService();
    private Exercise exercise;
    private boolean isAddingNew = false;

    // ID của bản ghi log nếu đang chỉnh sửa
    private Integer exerciseLogId = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtEffort.getItems().addAll("Nhẹ", "Vừa", "Nặng");
        dtpDate.setValue(LocalDate.now());
        caloriesBox.setVisible(false);

        btnCancel.setOnAction(event -> handleCancel(event));
        
        btnMinimize.setOnAction(event -> minimizeWindow(btnMinimize));
        btnClose.setOnAction(event -> closeWindow(btnClose));

        txtExercise.setOnMouseClicked((MouseEvent event) -> {
            if (isAddingNew) {
                caloriesBox.setVisible(true);
            }
        });
    }
    private boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    @FXML
    public void handleMinimize(ActionEvent event) {
        minimizeWindow(btnMinimize);
    }
    
    @FXML
    public void handleClose(ActionEvent event) {
        closeWindow(btnClose);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (showConfirmationDialog("Xác nhận hủy bỏ", "Bạn có chắc chắn muốn hủy bỏ thay đổi?")) {
            closeWindow(btnCancel);
        }
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

    public void setExerciseLogData(Exerciselog log) {
        if (log != null) {
            this.exerciseLogId = log.getIdExLog();

            txtDuration.setText(String.valueOf(log.getDuration()));
            
            if (log.getEffortLevel() != null && !log.getEffortLevel().isEmpty()) {
                txtEffort.setValue(log.getEffortLevel());
            }
            
            if (log.getDatetime() != null) {
                LocalDate localDate = log.getDatetime().toLocalDate();
                dtpDate.setValue(localDate);
            }
        }
    }

    @FXML
    private void saveExercise(ActionEvent event) {
        if (!showConfirmationDialog("Xác nhận lưu", "Bạn có chắc chắn muốn lưu thông tin này?")) {
            return; 
        }
        
        try {
            double caloriesPerMinute = exercise.getCaloriesBurnedPerMin();
            if (txtExercise.getText().matches(".*[!@#$%^&*()_+={}|;':\",.<>?`~].*")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên bài tập không được chứa kí tự đặc biệt!");
                txtExercise.requestFocus();
                return;
            }
            if (txtExercise.getText() == null || txtExercise.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên bài tập không được để trống!");
                txtExercise.requestFocus();
                return;
            }
            String exerciseName = txtExercise.getText();
            
            if (isAddingNew && exerciseService.exerciseNameExists(exerciseName)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Bài tập với tên \"" + exerciseName + "\" đã tồn tại!\nVui lòng chọn tên khác.");
                txtExercise.requestFocus();
                return;
            }
            
            if (txtDuration.getText() == null || txtDuration.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời gian không được để trống!");
                txtDuration.requestFocus();
                return;
            }
            if (!txtDuration.getText().matches("[0-9]+")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời gian phải là số!");
                txtDuration.requestFocus();
                return;
            }
            int duration = Integer.parseInt(txtDuration.getText());
            if (txtEffort.getValue() == null || txtEffort.getValue().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mức độ nỗ lực không được để trống!");
                txtEffort.requestFocus();
                return;
            }
            String effortLevel = txtEffort.getValue();
            if (dtpDate.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Ngày không được để trống!");
                dtpDate.requestFocus();
                return;
            }
            // chỉ cho chọn ngày trong quá khứ
            if (dtpDate.getValue().isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Ngày không được chọn trong tương lai!");
                dtpDate.requestFocus();
                return;
            }
            LocalDate selectedDate = dtpDate.getValue();
            Date date = java.sql.Date.valueOf(selectedDate);

            // Nếu exercise chưa có ID thì là bài tập mới => thêm vào database
            if (exercise.getIdExercise() == null) {
                if (txtCaloriesPerMinute.getText() == null || txtCaloriesPerMinute.getText().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lượng calo tiêu thụ không được để trống!");
                    txtCaloriesPerMinute.requestFocus();
                    return;
                }
                if (!txtCaloriesPerMinute.getText().matches("[0-9]+(\\.[0-9]+)?")) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lượng calo tiêu thụ phải là số!");
                    txtCaloriesPerMinute.requestFocus();
                    return;
                }
                
                caloriesPerMinute = Double.parseDouble(txtCaloriesPerMinute.getText());
                
                if (caloriesPerMinute > 100) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Cảnh báo giá trị calo");
                    confirmAlert.setHeaderText("Giá trị calo có vẻ cao bất thường");
                    confirmAlert.setContentText("Bạn đã nhập " + caloriesPerMinute + " calo/phút, đây là một giá trị rất cao.\n" +
                                                "Các hoạt động thông thường tiêu thụ khoảng 3-15 calo/phút.\n\n" +
                                                "Bạn có chắc chắn muốn sử dụng giá trị này không?");
                    
                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isPresent() && result.get() != ButtonType.OK) {
                        txtCaloriesPerMinute.requestFocus();
                        return; 
                    }
                }

                Exercise newExercise = new Exercise();
                newExercise.setExerciseName(exerciseName);
                newExercise.setCaloriesBurnedPerMin(caloriesPerMinute);
                newExercise.setImageExercise("");
                newExercise.setUserId(User.getCurrentUser());
                exerciseService.saveExercise(newExercise);

                Exercise insertedExercise = exerciseService.getExerciseByName(exerciseName);
                exercise = insertedExercise; // cập nhật lại biến exercise hiện tại
            }
            
            double caloriesBurned = duration * caloriesPerMinute;
            caloriesBurned = Math.round(caloriesBurned * 100.0) / 100.0;
            
            ExerciseLogService logService = new ExerciseLogService();
            boolean logSaved;
            
            try {
                if (exerciseLogId != null) {
                    Exerciselog exerciseLog = new Exerciselog();
                    exerciseLog.setIdExLog(exerciseLogId);
                    exerciseLog.setDatetime(date);
                    exerciseLog.setExerciseId(exercise);
                    exerciseLog.setEffortLevel(effortLevel);
                    exerciseLog.setEnergyBurn(caloriesBurned);
                    exerciseLog.setDuration(duration);
                    exerciseLog.setUserId(User.getCurrentUser());
                    
                    logSaved = logService.updateLog(exerciseLog);
                    if (logSaved) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Dữ liệu đã được cập nhật thành công!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi Cập Nhật", "Không thể cập nhật nhật ký tập luyện.");
                        return;
                    }
                } else {
                    Exerciselog exerciseLog = new Exerciselog();
                    exerciseLog.setDatetime(date);
                    exerciseLog.setExerciseId(exercise);
                    exerciseLog.setEffortLevel(effortLevel);
                    exerciseLog.setEnergyBurn(caloriesBurned);
                    exerciseLog.setDuration(duration);
                    exerciseLog.setUserId(User.getCurrentUser());
                    
                    logSaved = logService.saveLog(exerciseLog);
                    if (logSaved) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Dữ liệu đã được lưu thành công!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi Lưu", "Không thể lưu nhật ký tập luyện (service trả về false).");
                        return;
                    }
                }
                
                closeWindow(btnCancel);
            } catch (SQLException ex) {
                if (ex.getMessage().contains("Tổng thời gian tập luyện trong ngày không được vượt quá 24 giờ")) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Vượt Giới Hạn", 
                            "Tổng thời gian tập luyện trong ngày không được vượt quá 24 giờ (1440 phút).\n" +
                            "Vui lòng giảm thời gian hoặc chọn ngày khác.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi SQL", "Lỗi cơ sở dữ liệu: " + ex.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu dữ liệu: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }
}
