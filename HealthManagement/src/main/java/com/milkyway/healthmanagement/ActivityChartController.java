package com.milkyway.healthmanagement;

import com.milkyway.pojo.Exerciselog;
import com.milkyway.pojo.User;
import com.milkyway.services.ExerciseLogService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityChartController extends SwitchSceneController implements Initializable {
    @FXML
    private Label username;
    @FXML
    private Button logout_btn;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnMinimize;
    @FXML
    private LineChart<String, Number> activityLineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private Button btn7Days;
    @FXML
    private Button btn1Month;
    @FXML
    private Button btn3Months;

    private ExerciseLogService exerciseLogService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.exerciseLogService = new ExerciseLogService();
        
        // Set username
        displayUsername();
        
        // Setup window controls
        btnClose.setOnAction(e -> {
            System.exit(0);
        });
        btnMinimize.setOnAction(e -> {
            Stage stage = (Stage) btnMinimize.getScene().getWindow();
            stage.setIconified(true);
        });
        
        // Setup chart initial data (default: 7 days)
        filter7Days();
    }

    public void logout(ActionEvent event) {
        try {
            logout();
            // Không cần gọi switchToLogin vì trong phương thức logout đã có rồi
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void SwitchToActivityChart() {
        // Already on activity chart
    }

    @FXML
    public void filter7Days() {
        loadChartData(7);
        styleActiveFilterButton(btn7Days);
    }

    @FXML
    public void filter1Month() {
        loadChartData(30);
        styleActiveFilterButton(btn1Month);
    }

    @FXML
    public void filter3Months() {
        loadChartData(90);
        styleActiveFilterButton(btn3Months);
    }

    private void styleActiveFilterButton(Button activeButton) {
        // Reset all buttons to default style
        btn7Days.getStyleClass().remove("active-filter");
        btn1Month.getStyleClass().remove("active-filter");
        btn3Months.getStyleClass().remove("active-filter");
        
        // Add active style to the selected button
        activeButton.getStyleClass().add("active-filter");
    }

    private void loadChartData(int days) {
        // Clear previous data
        activityLineChart.getData().clear();
        
        // Get current date
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        // Chuyển đổi sang java.sql.Date thay vì java.util.Date
        Date startDateAsDate = Date.valueOf(startDate);
        Date endDateAsDate = Date.valueOf(endDate.plusDays(1));
        
        try {
            // Cần lấy user ID từ User.getCurrentUser()
            int userId = User.getCurrentUser().getId();
            List<Exerciselog> logs = exerciseLogService.getExerciseLogsByDateRange(userId, startDateAsDate, endDateAsDate);
            
            // Process data for chart
            Map<LocalDate, Integer> caloriesPerDay = new HashMap<>();
            Map<LocalDate, Integer> minutesPerDay = new HashMap<>();
            
            // Initialize all dates in range with zero values
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                caloriesPerDay.put(currentDate, 0);
                minutesPerDay.put(currentDate, 0);
                currentDate = currentDate.plusDays(1);
            }
            
            // Aggregate data by day
            for (Exerciselog log : logs) {
                // Chuyển đổi java.sql.Date sang LocalDate
                LocalDate logDate = null;
                if (log.getDatetime() instanceof java.sql.Date) {
                    logDate = ((java.sql.Date) log.getDatetime()).toLocalDate();
                } else if (log.getDatetime() instanceof java.util.Date) {
                    // Nếu là java.util.Date thì chuyển đổi khác
                    logDate = new java.sql.Date(log.getDatetime().getTime()).toLocalDate();
                }
                
                if (logDate != null) {
                    // Update calories (sử dụng getEnergyBurn thay vì getCalorie)
                    int calories = (int) Math.round(log.getEnergyBurn());
                    caloriesPerDay.put(logDate, caloriesPerDay.getOrDefault(logDate, 0) + calories);
                    
                    // Update minutes
                    minutesPerDay.put(logDate, minutesPerDay.getOrDefault(logDate, 0) + log.getDuration());
                }
            }
            
            // Create series for calories
            XYChart.Series<String, Number> caloriesSeries = new XYChart.Series<>();
            caloriesSeries.setName("Calo tiêu thụ");
            
            // Create series for minutes
            XYChart.Series<String, Number> minutesSeries = new XYChart.Series<>();
            minutesSeries.setName("Phút tập");
            
            // Add data points to series
            currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                String dateStr = currentDate.format(dateFormatter);
                caloriesSeries.getData().add(new XYChart.Data<>(dateStr, caloriesPerDay.get(currentDate)));
                minutesSeries.getData().add(new XYChart.Data<>(dateStr, minutesPerDay.get(currentDate)));
                currentDate = currentDate.plusDays(1);
            }
            
            // Add series to chart
            activityLineChart.getData().add(minutesSeries);
            activityLineChart.getData().add(caloriesSeries);
            
            // Apply custom styling to the series
            for (XYChart.Series<String, Number> series : activityLineChart.getData()) {
                if (series.getName().equals("Calo tiêu thụ")) {
                    series.getNode().setStyle("-fx-stroke: #ff5757;"); // Màu đỏ
                } else if (series.getName().equals("Phút tập")) {
                    series.getNode().setStyle("-fx-stroke: #1bac5a;"); // Màu xanh lá
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ActivityChartController.class.getName()).log(Level.SEVERE, 
                    "Lỗi khi tải dữ liệu biểu đồ", e);
            // Hiển thị thông báo lỗi cho người dùng
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu hoạt động. Vui lòng thử lại sau.");
        }
    }
}