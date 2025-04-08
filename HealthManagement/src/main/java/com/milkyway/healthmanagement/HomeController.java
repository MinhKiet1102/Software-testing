/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.healthmanagement;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.SQLException;
import com.milkyway.pojo.Exerciselog;
import com.milkyway.pojo.Target;
import com.milkyway.pojo.User;
import com.milkyway.services.ExerciseLogService;
import com.milkyway.services.TargetService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author Admin
 */
public class HomeController extends SwitchSceneController implements Initializable{

    @FXML
    private Button btnClose;

    @FXML
    private Button btnMinimize;

    @FXML
    private PieChart chartExercise;
    
    @FXML
    private PieChart chartFood;

    @FXML
    private Button logout_btn;

    @FXML
    private TableView<?> tbMeal;

    @FXML
    private Label txtAim;

    @FXML
    private Label txtAimName;

    @FXML
    private Label txtEndDate;

    @FXML
    private Label txtStartDate;

    @FXML
    private Label txtUnit;

    @FXML
    private TableView<Target> tbTarget;

    private ObservableList<Target> targetData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnClose.setOnAction(event -> closeWindow(btnClose));
        btnMinimize.setOnAction(event -> minimizeWindow(btnMinimize));
        
        displayUsername();
        createExerciseChart();
        loadTableViewTarget();
        try {
            loadData(User.getCurrentUser().getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }   

    private void createExerciseChart() {
        try {
            // Lấy dữ liệu của người dùng hiện tại
            ExerciseLogService exerciseLogService = new ExerciseLogService();
            List<Exerciselog> logs = exerciseLogService.getExerciseLogsByUserAndDate(User.getCurrentUser().getId(), null);
            
            // Nhóm dữ liệu theo loại bài tập và tính tổng năng lượng đã đốt
            Map<String, Double> exerciseSummary = new HashMap<>();
            
            for (Exerciselog log : logs) {
                String exerciseName = log.getExerciseId().getExerciseName();
                Double energyBurn = log.getEnergyBurn();
                
                exerciseSummary.put(
                    exerciseName, 
                    exerciseSummary.getOrDefault(exerciseName, 0.0) + energyBurn
                );
            }
            
            // Xóa dữ liệu cũ trong biểu đồ
            chartExercise.getData().clear();
            
            // Tạo dữ liệu cho biểu đồ
            for (Map.Entry<String, Double> entry : exerciseSummary.entrySet()) {
                PieChart.Data slice = new PieChart.Data(
                    entry.getKey() + " (" + String.format("%.1f", entry.getValue()) + " calo)", 
                    entry.getValue()
                );
                chartExercise.getData().add(slice);
            
            //tôi muốn khi hover vào từng phần của biểu đồ thì hiển thị thông tin chi tiết về bài tập
                Tooltip tooltip = new Tooltip(entry.getKey() + ": " + String.format("%.1f", entry.getValue()) + " calo");
                Tooltip.install(slice.getNode(), tooltip);
            }

            // Thêm tiêu đề cho biểu đồ
            chartExercise.setTitle("Biểu đồ bài tập của bạn");
            
            // Thêm các thuộc tính hiển thị khác nếu cần
            chartExercise.setLabelLineLength(10);
            chartExercise.setLabelsVisible(true);
            chartExercise.setStartAngle(90);
            
        } catch (SQLException ex) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu bài tập: " + ex.getMessage());
        }
    }

    private void loadTableViewTarget() {
        TableColumn<Target, String> colName = new TableColumn<>("Mục Tiêu");
        colName.setCellValueFactory(new PropertyValueFactory<>("targetName"));
        colName.setPrefWidth(100);

        // Định dạng ngày bắt đầu
        TableColumn<Target, String> colStart = new TableColumn<>("Ngày Bắt Đầu");
        colStart.setCellValueFactory(cellData -> {
            Target target = cellData.getValue();
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = dateFormat.format(target.getStartDate());
            return new SimpleStringProperty(formattedDate);
        });
        colStart.setPrefWidth(100);

        // Định dạng ngày kết thúc
        TableColumn<Target, String> colEnd = new TableColumn<>("Ngày Kết Thúc");
        colEnd.setCellValueFactory(cellData -> {
            Target target = cellData.getValue();
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = dateFormat.format(target.getEndDate());
            return new SimpleStringProperty(formattedDate);
        });
        colEnd.setPrefWidth(100);

        // Phần trăm mức độ hoàn thành của Target
        TableColumn<Target, String> colPercent = new TableColumn<>("Tiến Độ");
        colPercent.setCellValueFactory(cellData -> {
            Target target = cellData.getValue();
            int percent = (int) ((target.getProgress() / target.getTargetNumber()) * 100);
            return new SimpleStringProperty(percent + "%");
        });
        colPercent.setPrefWidth(70);

        // Thêm các cột vào TableView
        tbTarget.getColumns().clear();
        tbTarget.getColumns().addAll(colName, colStart, colEnd, colPercent);
        tbTarget.setItems(targetData);
    }

    public void loadData(int userId) throws SQLException {
        //chỉ hiển thị các mục tiêu có trang thái là "In Progress"
        TargetService targetService = new TargetService();
        List<Target> targets = targetService.getTargetByUserId(userId, "In Progress");
        targetData.clear(); 
        for (Target target : targets) {
            //chỉ hiển thị các mục tiêu có trang thái là "In Progress"
            if (target.getStatus().equals("In Progress")) {
                targetData.add(target);
            }
        }
        tbTarget.setItems(targetData);
    }
}
