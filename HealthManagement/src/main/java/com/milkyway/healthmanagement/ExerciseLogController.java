/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.milkyway.healthmanagement;


import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.Exerciselog;
import com.milkyway.service.ExerciseLogService;
import com.milkyway.service.ExerciseService;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author ASUS
 */
public class ExerciseLogController extends SwitchSceneController implements Initializable {

    @FXML
    TableView<ExerciseTableRow> tbExercise;

    @FXML
    private DatePicker dtpDateOfExercise;

    @FXML
    private TextField txtFind;

    @FXML
    private Label txtTotal;

    private ObservableList<ExerciseTableRow> exerciseData = FXCollections.observableArrayList();

    private void loadTableView() {
        TableColumn colName = new TableColumn("Bài Tập");
        colName.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        colName.setPrefWidth(300);

        TableColumn colTime = new TableColumn("Thời Gian");
        colTime.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        colTime.setPrefWidth(200);

        TableColumn colSumCalo = new TableColumn("Tổng Calo Tiêu Thụ");
        colSumCalo.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        colSumCalo.setPrefWidth(200);

        TableColumn colAction = new TableColumn("Hành Động");
        colAction.setCellValueFactory(new PropertyValueFactory<>("deleteButton"));
        colAction.setPrefWidth(200);
        colAction.setCellFactory((e -> {
            Button btn = new Button("Xóa");
            btn.setOnAction((evt) -> {
                try {
                    int id = ((Exercise)((TableRow)((Button)evt.getSource()).getParent().getParent()).getItem()).getIdExercise();
                    ExerciseLogService els = new ExerciseLogService();
                    els.deleteExerciseLog(id);
                    this.showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Xóa thành công!!!");
                    loadData("");
                } catch (SQLException ex) {
                    Logger.getLogger(ExerciseLogController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            
            TableCell cell = new TableCell();
            cell.setGraphic(btn);
            return cell;
        }));
        this.tbExercise.getColumns().addAll(colName, colTime, colSumCalo, colAction);
    }

    public void loadData(String kw) {
//    try {
//        // Lấy userId của người đăng nhập
//        int userId = LoggedInUser.getUserId();
//
//        // Gọi service để lấy danh sách bài tập của user
//        ExerciseLogService els = new ExerciseLogService();
//        List<Exerciselog> logs = els.getExercisesByUser(userId, kw);
//
//        // Chuyển đổi dữ liệu thành dạng ObservableList để hiển thị trên TableView
//        exerciseData.clear();
//        for (Exerciselog log : logs) {
//            Exercise exercise = log.getExerciseId();
//            String exerciseName = exercise.getExerciseName();
//            String interval = log.getDuration() + " phút";
//            String caloriesConsumed = log.getEnergyBurn() + " kcal";
//            
//            Button deleteButton = new Button("Xóa");
//            deleteButton.setOnAction(evt -> {
//                try {
//                    els.deleteExerciseLog(log.getIdExLog());
//                    this.showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Xóa thành công!");
//                    loadData(kw); // Load lại dữ liệu sau khi xóa
//                } catch (SQLException ex) {
//                    Logger.getLogger(ExerciseLogController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            });
//
//            ExerciseTableRow row = new ExerciseTableRow(exerciseName, interval, caloriesConsumed, deleteButton, log);
//            exerciseData.add(row);
//        }
//
//        // Gán dữ liệu cho TableView
//        tbExercise.setItems(exerciseData);
//
//        // Tính tổng calo tiêu thụ
//        double totalCalories = logs.stream().mapToDouble(Exerciselog::getEnergyBurn).sum();
//        txtTotal.setText("Tổng calo tiêu thụ: " + totalCalories + " kcal");
//
//    }catch(SQLException ex) {
//        Logger.getLogger(ExerciseLogController.class.getName()).log(Level.SEVERE, null, ex);
//    }
}
    
 
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class ExerciseTableRow {

        private final String exerciseName;
        private final String interval;
        private final String caloriesConsumed;
        private final Button deleteButton;
        private final Exerciselog userExercise;

        public String getExerciseName() {
            return exerciseName;
        }

        public String getInterval() {
            return interval;
        }

        public String getCaloriesConsumed() {
            return caloriesConsumed;
        }

        public Button getDeleteButton() {
            return deleteButton;
        }

        public ExerciseTableRow(String exerciseName, String interval, String caloriesConsumed, Button deleteButton, Exerciselog userExercise) {
            this.exerciseName = exerciseName;
            this.interval = interval;
            this.caloriesConsumed = caloriesConsumed;
            this.deleteButton = deleteButton;
            this.userExercise = userExercise;
        }

        public Exerciselog getUserExercise() {
            return userExercise;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.loadTableView();
    }

}
