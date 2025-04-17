package com.milkyway.healthmanagement;
import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.User;
import com.milkyway.services.ExerciseService;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class ExercisePageController extends SwitchSceneController implements Initializable {

    @FXML
    private Button btnClose;

    @FXML
    private Button btnMinimize;

    @FXML
    private GridPane gridPane;

    @FXML
    private ScrollPane scrollPanel;

    @FXML
    private TextField txtFind;
    
    @FXML
    private HBox exerciseListContent;
    
    @FXML
    private AnchorPane activityChartContent;
    
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

    // Thời gian hiện tại để lọc dữ liệu
    private int currentFilterDays = 7; // Mặc định hiển thị 7 ngày gần nhất

    @FXML
    private void loadExercise(String kw) throws IOException {
        ExerciseService es = new ExerciseService();
        try {
            gridPane.getChildren().clear();
            int col = 0, row = 0;
            List<Exercise> e = es.getExercise(kw);
            for (var i : e) {
                FXMLLoader fXMLLoader = new FXMLLoader();
                fXMLLoader.setLocation(getClass().getResource("/com/milkyway/healthmanagement/Exercise.fxml"));
                AnchorPane anchorPane = fXMLLoader.load();
                ExerciseController controller = fXMLLoader.getController();
                controller.setData(i);

                anchorPane.setOnMouseClicked(event -> {
                    try {
                        switchToExerciseDetail(i);
                    } catch (IOException ex) {
                        Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });

                if (col == 4) {
                    col = 0;
                    row++;
                }
                gridPane.add(anchorPane, ++col, row);
                GridPane.setMargin(anchorPane, new Insets(10));
            }
            addAddButtonToGrid(col, row);
        } catch (SQLException ex) {
            Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void addAddButtonToGrid(int col, int row) {
        // Chỉ hiển thị nút thêm mới nếu người dùng đã đăng nhập
        if (User.getCurrentUser() != null) {
            // Tạo Button chứa dấu cộng
            Button addButton = new Button();
            FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
            icon.setSize("24px");
            addButton.setGraphic(icon);
            addButton.setPrefSize(150, 150); // Kích thước giống các ô bài tập
            addButton.setStyle("-fx-background-color: #E0E0E0; -fx-border-color: #B0B0B0; -fx-border-width: 2px; -fx-border-radius: 10px;");

            addButton.setOnAction(event -> {
                try {
                    switchToExerciseDetail(null); // Truyền `null` khi thêm mới bài tập
                } catch (IOException ex) {
                    Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            AnchorPane addPane = new AnchorPane(addButton);
            AnchorPane.setTopAnchor(addButton, 10.0);
            AnchorPane.setLeftAnchor(addButton, 10.0);

            if (col == 4) {
                col = 0;
                row++;
            }
            gridPane.add(addPane, ++col, row);
            GridPane.setMargin(addPane, new Insets(10));
        }
    }

    private void switchToExerciseDetail(Exercise exercise) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milkyway/healthmanagement/ExerciseDetail.fxml"));
        Parent root = loader.load();

        // Tạo cửa sổ mới (Stage)
        Stage stage = new Stage();
        stage.setScene(new Scene(root, 700, 320));
        // Ẩn các nút điều khiển của cửa sổ
        stage.initStyle(StageStyle.UNDECORATED);
        ExerciseDetailController controller = loader.getController();
        controller.setExercise(exercise);

        stage.initModality(Modality.APPLICATION_MODAL);

        // Hiển thị cửa sổ
        stage.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnClose.setOnAction(event -> closeWindow(btnClose));
        btnMinimize.setOnAction(event -> minimizeWindow(btnMinimize));
        try {
            displayUsername();
            loadExercise(null);
        } catch (IOException ex) {
            Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.txtFind.textProperty().addListener((ev) -> {
            try {
                this.loadExercise(this.txtFind.getText());
            } catch (IOException ex) {
                Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // Khởi tạo biểu đồ khi ứng dụng được khởi động
        setupActivityChart();
    }
    
    /**
     * Phương thức chuyển đổi sang tab Danh sách bài tập
     */
    @FXML
    public void SwitchToExercisePage() {
        // Hiển thị tab danh sách bài tập
        exerciseListContent.setVisible(true);
        activityChartContent.setVisible(false);
    }
    
    /**
     * Phương thức chuyển đổi sang tab Thông tin bài tập
     */
    @FXML
    public void SwitchToExerciseLog() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milkyway/healthmanagement/ExerciseLog.fxml"));
            Parent root = loader.load();
            Scene scene = btnClose.getScene();
            scene.setRoot(root);
        } catch (IOException ex) {
            Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Phương thức chuyển đổi sang tab Hoạt động
     */
    @FXML
    public void SwitchToActivityChart() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milkyway/healthmanagement/ActivityChart.fxml"));
            Parent root = loader.load();
            Scene scene = btnClose.getScene();
            scene.setRoot(root);
        } catch (IOException ex) {
            Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Thiết lập biểu đồ hoạt động
     */
    private void setupActivityChart() {
        // Kiểm tra biểu đồ có tồn tại không trước khi truy cập
        if (activityLineChart != null) {
            // Cấu hình biểu đồ
            activityLineChart.setTitle("Thống Kê Hoạt Động Tập Luyện");
            activityLineChart.setAnimated(false);
            activityLineChart.setCreateSymbols(true);
            
            // Cấu hình trục x và y
            xAxis.setLabel("Ngày");
            yAxis.setLabel("Giá trị");
        }
    }
    
    /**
     * Phương thức để lọc dữ liệu cho 7 ngày gần nhất
     */
    @FXML
    public void filter7Days() {
        currentFilterDays = 7;
        loadActivityChartData(currentFilterDays);
    }
    
    /**
     * Phương thức để lọc dữ liệu cho 1 tháng gần nhất
     */
    @FXML
    public void filter1Month() {
        currentFilterDays = 30;
        loadActivityChartData(currentFilterDays);
    }
    
    /**
     * Phương thức để lọc dữ liệu cho 3 tháng gần nhất
     */
    @FXML
    public void filter3Months() {
        currentFilterDays = 90;
        loadActivityChartData(currentFilterDays);
    }
    
    /**
     * Nạp dữ liệu cho biểu đồ hoạt động
     * @param days Số ngày muốn hiển thị dữ liệu
     */
    private void loadActivityChartData(int days) {
        // Xóa dữ liệu cũ
        activityLineChart.getData().clear();
        
        try {
            // Tạo series cho phút tập luyện và calo tiêu thụ
            XYChart.Series<String, Number> minutesSeries = new XYChart.Series<>();
            minutesSeries.setName("Tổng số phút tập");
            
            XYChart.Series<String, Number> caloriesSeries = new XYChart.Series<>();
            caloriesSeries.setName("Tổng số calo tiêu thụ");
            
            // Nạp dữ liệu từ cơ sở dữ liệu
            // Mẫu dữ liệu (trong thực tế, dữ liệu này sẽ được lấy từ cơ sở dữ liệu)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            
            // Tạo dữ liệu mẫu cho biểu đồ
            for (int i = 0; i < days; i++) {
                LocalDate date = startDate.plusDays(i);
                String dateStr = date.format(formatter);
                
                // Thay thế bằng dữ liệu thực khi tích hợp với cơ sở dữ liệu
                // Giả lập dữ liệu: giá trị ngẫu nhiên cho phút tập và calo
                double minutes = Math.random() * 60 + 20; // 20-80 phút
                double calories = minutes * 5 + Math.random() * 100; // 100-500 calo
                
                minutesSeries.getData().add(new XYChart.Data<>(dateStr, minutes));
                caloriesSeries.getData().add(new XYChart.Data<>(dateStr, calories));
            }
            
            // Thêm series vào biểu đồ
            activityLineChart.getData().add(minutesSeries);
            activityLineChart.getData().add(caloriesSeries);
            
            // Cập nhật màu sắc của các đường
            String minutesColor = "#1bac5a"; // Màu xanh lá
            String caloriesColor = "#ff5757"; // Màu đỏ
            
            // Áp dụng màu sắc cho series
            minutesSeries.getNode().setStyle("-fx-stroke: " + minutesColor + ";");
            caloriesSeries.getNode().setStyle("-fx-stroke: " + caloriesColor + ";");
            
        } catch (Exception ex) {
            Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
