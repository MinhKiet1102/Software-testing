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
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.SQLException;
import java.sql.Connection;
import java.time.LocalDate;
import java.sql.Date;
import com.milkyway.pojo.Exerciselog;
import com.milkyway.pojo.Target;
import com.milkyway.pojo.User;
import com.milkyway.pojo.MealFood;
import com.milkyway.pojo.Food;
import com.milkyway.pojo.Meal;
import com.milkyway.pojo.MealFoodPK;
import com.milkyway.services.ExerciseLogService;
import com.milkyway.services.TargetService;
import com.milkyway.services.MealService;
import com.milkyway.pojo.JdbcUtils;

import javafx.application.Platform;
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
    private TableView<Meal> tbMeal;
    
    private ObservableList<Meal> mealData = FXCollections.observableArrayList();

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
        createFoodChart();
        loadTableViewTarget();
        
        // Thiết lập cấu trúc bảng trước
        loadTableViewMeal();
        
        try {
            loadData(User.getCurrentUser().getId());
            
            // Tải dữ liệu bữa ăn sau khi đã thiết lập cấu trúc bảng
            if (User.getCurrentUser() != null) {
                loadMealData(User.getCurrentUser().getId());
            }
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
            
            boolean hasData = false;
            
            // Tạo dữ liệu cho biểu đồ
            for (Map.Entry<String, Double> entry : exerciseSummary.entrySet()) {
                if (entry.getValue() > 0) {
                    hasData = true;
                    PieChart.Data slice = new PieChart.Data(
                        entry.getKey() + " (" + String.format("%.1f", entry.getValue()) + " calo)", 
                        entry.getValue()
                    );
                    chartExercise.getData().add(slice);
                
                    //tôi muốn khi hover vào từng phần của biểu đồ thì hiển thị thông tin chi tiết về bài tập
                    Tooltip tooltip = new Tooltip(entry.getKey() + ": " + String.format("%.1f", entry.getValue()) + " calo");
                    Tooltip.install(slice.getNode(), tooltip);
                }
            }
            
            // Nếu không có dữ liệu, hiển thị một mẫu để biểu đồ không trống
            if (!hasData) {
                chartExercise.getData().add(new PieChart.Data("Không có dữ liệu", 1));
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
            
            // Hiển thị một biểu đồ mẫu khi có lỗi
            chartExercise.getData().clear();
            chartExercise.getData().add(new PieChart.Data("Không có dữ liệu", 1));
        }
    }

    /**
     * Tạo biểu đồ tròn hiển thị tổng hợp dinh dưỡng từ tất cả các bữa ăn của người dùng 
     * từ trước đến ngày hiện tại
     * Biểu đồ sẽ hiển thị các loại dinh dưỡng như: Carbohydrate, Protein, Lipid, Sugar, và Natri
     */
    private void createFoodChart() {
        try {
            System.out.println("Bắt đầu tạo biểu đồ dinh dưỡng...");
            Connection conn = null;
            try {
                // Tạo kết nối trực tiếp đến cơ sở dữ liệu
                conn = JdbcUtils.getConn();
                System.out.println("Kết nối cơ sở dữ liệu thành công");
            } catch (SQLException ex) {
                System.err.println("Lỗi kết nối cơ sở dữ liệu: " + ex.getMessage());
                ex.printStackTrace();
                return; // Dừng lại nếu không thể kết nối
            }
            
            MealService mealService = new MealService(conn);
            
            // Lấy ngày hiện tại để làm tham số cho việc lấy dữ liệu bữa ăn
            LocalDate today = LocalDate.now();
            Date sqlDate = Date.valueOf(today);
            
            // Khởi tạo map để lưu trữ tổng lượng các loại chất dinh dưỡng
            Map<String, Double> nutritionSummary = new HashMap<>();
            nutritionSummary.put("Carbohydrate", 0.0);  // Đơn vị: gram (g)
            nutritionSummary.put("Protein", 0.0);       // Đơn vị: gram (g)
            nutritionSummary.put("Lipid", 0.0);         // Đơn vị: gram (g)
            nutritionSummary.put("Sugar", 0.0);         // Đơn vị: gram (g)
            nutritionSummary.put("Natri", 0.0);         // Đơn vị: milligram (mg)
            
            // Lấy danh sách tất cả bữa ăn của người dùng từ trước đến ngày hiện tại
            try {
                if (User.getCurrentUser() != null) {
                    List<Meal> meals = mealService.getMealsByUserAndToday(User.getCurrentUser().getId(), sqlDate);
                    System.out.println("Tìm thấy " + meals.size() + " bữa ăn cho người dùng " + 
                                      User.getCurrentUser().getId() + " đến ngày " + sqlDate);
                    
                    // Tính tổng lượng dinh dưỡng từ tất cả các bữa ăn
                    for (Meal meal : meals) {
                        System.out.println("Đang xử lý bữa ăn: " + meal.getNameMeal() + ", ID: " + meal.getIdMeal());
                        if (meal.getMealFoodSet() != null) {
                            for (MealFood mealFood : meal.getMealFoodSet()) {
                                Food food = mealFood.getFood();
                                if (food != null) {
                                    // Lấy số lượng từ mealFood
                                    double quantity = mealFood.getQuantity();
                                    System.out.println("  - Thực phẩm: " + food.getFoodName() + ", Số lượng: " + quantity);
                                    
                                    // Cộng dồn lượng dinh dưỡng
                                    if (food.getCarb() != null) {
                                        nutritionSummary.put("Carbohydrate", nutritionSummary.get("Carbohydrate") + food.getCarb() * quantity);
                                    }
                                    
                                    if (food.getProtein() != null) {
                                        nutritionSummary.put("Protein", nutritionSummary.get("Protein") + food.getProtein() * quantity);
                                    }
                                    
                                    if (food.getFat() != null) {
                                        nutritionSummary.put("Lipid", nutritionSummary.get("Lipid") + food.getFat() * quantity);
                                    }
                                    
                                    if (food.getSugar() != null) {
                                        nutritionSummary.put("Sugar", nutritionSummary.get("Sugar") + food.getSugar() * quantity);
                                    }
                                    
                                    if (food.getSodium() != null) {
                                        nutritionSummary.put("Natri", nutritionSummary.get("Natri") + food.getSodium() * quantity);
                                    }
                                } else {
                                    System.out.println("  - Food là null cho MealFood");
                                }
                            }
                        } else {
                            System.out.println("  - Bữa ăn không có thực phẩm nào");
                        }
                    }
                    
                    // In ra tổng dinh dưỡng để debug
                    for (Map.Entry<String, Double> entry : nutritionSummary.entrySet()) {
                        System.out.println("Tổng " + entry.getKey() + ": " + entry.getValue());
                    }
                } else {
                    System.out.println("Người dùng chưa đăng nhập");
                }
            } catch (Exception e) {
                Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, "Không thể lấy dữ liệu bữa ăn", e);
                e.printStackTrace();
                // Vẫn tiếp tục vẽ biểu đồ với dữ liệu trống
            }
            
            // Xóa dữ liệu cũ trong biểu đồ
            chartFood.getData().clear();
            
            // Biến để theo dõi xem có dữ liệu thực tế không
            boolean hasData = false;
            
            // Tạo dữ liệu cho biểu đồ
            for (Map.Entry<String, Double> entry : nutritionSummary.entrySet()) {
                // Chỉ thêm dữ liệu vào biểu đồ nếu có giá trị > 0
                if (entry.getValue() > 0) {
                    hasData = true;
                    // Tạo một phần (slice) của biểu đồ tròn
                    String unit = entry.getKey().equals("Natri") ? "mg" : "g";
                    PieChart.Data slice = new PieChart.Data(
                        entry.getKey() + " (" + String.format("%.1f", entry.getValue()) + " " + unit + ")", 
                        entry.getValue()
                    );
                    chartFood.getData().add(slice);
                
                    // Thêm tooltip khi hover chuột vào từng phần của biểu đồ để hiển thị thông tin chi tiết
                    Tooltip tooltip = new Tooltip(entry.getKey() + ": " + String.format("%.1f", entry.getValue()) + " " + unit);
                    tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                    
                    // Đảm bảo rằng chúng ta không thực hiện thao tác này trước khi JavaFX hoàn thành quá trình hiển thị
                    final PieChart.Data sliceFinal = slice;
                    Platform.runLater(() -> {
                        if (sliceFinal.getNode() != null) {
                            Tooltip.install(sliceFinal.getNode(), tooltip);
                            
                            // Thêm màu riêng cho từng loại dinh dưỡng để dễ phân biệt
                            switch(entry.getKey()) {
                                case "Carbohydrate":
                                    sliceFinal.getNode().setStyle("-fx-pie-color: #FFA726;"); // Màu cam
                                    break;
                                case "Protein":
                                    sliceFinal.getNode().setStyle("-fx-pie-color: #66BB6A;"); // Màu xanh lá
                                    break;
                                case "Lipid":
                                    sliceFinal.getNode().setStyle("-fx-pie-color: #EF5350;"); // Màu đỏ
                                    break;
                                case "Sugar":
                                    sliceFinal.getNode().setStyle("-fx-pie-color: #42A5F5;"); // Màu xanh dương
                                    break;
                                case "Natri":
                                    sliceFinal.getNode().setStyle("-fx-pie-color: #AB47BC;"); // Màu tím
                                    break;
                            }
                        }
                    });
                }
            }
            
            // Nếu không có dữ liệu, hiển thị một mẫu để biểu đồ không trống
            if (!hasData) {
                chartFood.getData().add(new PieChart.Data("Không có dữ liệu", 1));
            }

            // Thêm các thuộc tính hiển thị cho biểu đồ
            chartFood.setTitle("Tổng Hợp Dinh Dưỡng");
            chartFood.setLabelLineLength(10);
            chartFood.setLabelsVisible(true);
            chartFood.setStartAngle(90);
            chartFood.setClockwise(true);
            
            // Thêm chú thích chung cho biểu đồ (legend)
            chartFood.setLegendVisible(true);
            
            System.out.println("Hoàn thành việc tạo biểu đồ chartFood");
            
        } catch (Exception ex) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu dinh dưỡng: " + ex.getMessage());
            
            // Hiển thị một biểu đồ mẫu khi có lỗi - chỉ hiển thị thông báo "Không có dữ liệu"
            chartFood.getData().clear();
            chartFood.getData().add(new PieChart.Data("Không có dữ liệu", 1));
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

    /**
     * Thiết lập cấu trúc bảng hiển thị dữ liệu bữa ăn
     */
    private void loadTableViewMeal() {
        // Tạo cột ngày của bữa ăn
        TableColumn<Meal, String> colDate = new TableColumn<>("Ngày");
        colDate.setCellValueFactory(cellData -> {
            Meal meal = cellData.getValue();
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = dateFormat.format(meal.getDateOfMeal());
            return new SimpleStringProperty(formattedDate);
        });
        colDate.setPrefWidth(111);
        
        // Tạo cột tên bữa ăn
        TableColumn<Meal, String> colMealName = new TableColumn<>("Bữa Ăn");
        colMealName.setCellValueFactory(new PropertyValueFactory<>("nameMeal"));
        colMealName.setPrefWidth(111);
        
        // Tạo cột chi tiết bữa ăn (danh sách thực phẩm)
        TableColumn<Meal, String> colMealDetail = new TableColumn<>("Chi Tiết Bữa Ăn");
        colMealDetail.setCellValueFactory(cellData -> {
            Meal meal = cellData.getValue();
            StringBuilder details = new StringBuilder();
            if (meal.getMealFoodSet() != null) {
                for (MealFood mealFood : meal.getMealFoodSet()) {
                    if (details.length() > 0) {
                        details.append(", ");
                    }
                    if (mealFood.getFood() != null) {
                        details.append(mealFood.getFood().getFoodName())
                              .append(" (")
                              .append(mealFood.getQuantity())
                              .append(" ")
                              .append(mealFood.getUnit())
                              .append(")");
                    }
                }
            }
            return new SimpleStringProperty(details.toString());
        });
        colMealDetail.setPrefWidth(288);
        
        // Tạo cột tổng lượng calo
        TableColumn<Meal, String> colCalories = new TableColumn<>("Tổng Lượng Calo");
        colCalories.setCellValueFactory(cellData -> {
            Meal meal = cellData.getValue();
            return new SimpleStringProperty(String.format("%.1f cal", meal.getTotalCalories()));
        });
        colCalories.setPrefWidth(128);
        
        // Tạo cột tổng protein
        TableColumn<Meal, String> colProtein = new TableColumn<>("Protein");
        colProtein.setCellValueFactory(cellData -> {
            Meal meal = cellData.getValue();
            double totalProtein = 0;
            if (meal.getMealFoodSet() != null) {
                for (MealFood mealFood : meal.getMealFoodSet()) {
                    if (mealFood.getFood() != null && mealFood.getFood().getProtein() != null) {
                        totalProtein += mealFood.getFood().getProtein() * mealFood.getQuantity();
                    }
                }
            }
            return new SimpleStringProperty(String.format("%.1f g", totalProtein));
        });
        colProtein.setPrefWidth(105);
        
        // Tạo cột tổng chất béo
        TableColumn<Meal, String> colFat = new TableColumn<>("Chất Béo");
        colFat.setCellValueFactory(cellData -> {
            Meal meal = cellData.getValue();
            double totalFat = 0;
            if (meal.getMealFoodSet() != null) {
                for (MealFood mealFood : meal.getMealFoodSet()) {
                    if (mealFood.getFood() != null && mealFood.getFood().getFat() != null) {
                        totalFat += mealFood.getFood().getFat() * mealFood.getQuantity();
                    }
                }
            }
            return new SimpleStringProperty(String.format("%.1f g", totalFat));
        });
        colFat.setPrefWidth(110);
        
        // Tạo cột tổng carbohydrate
        TableColumn<Meal, String> colCarb = new TableColumn<>("Carbohydrate");
        colCarb.setCellValueFactory(cellData -> {
            Meal meal = cellData.getValue();
            double totalCarb = 0;
            if (meal.getMealFoodSet() != null) {
                for (MealFood mealFood : meal.getMealFoodSet()) {
                    if (mealFood.getFood() != null && mealFood.getFood().getCarb() != null) {
                        totalCarb += mealFood.getFood().getCarb() * mealFood.getQuantity();
                    }
                }
            }
            return new SimpleStringProperty(String.format("%.1f g", totalCarb));
        });
        colCarb.setPrefWidth(121);
        
        // Xóa các cột hiện tại và thêm các cột mới
        tbMeal.getColumns().clear();
        tbMeal.getColumns().addAll(colDate, colMealName, colMealDetail, colCalories, colProtein, colFat, colCarb);
        
        // Thiết lập dữ liệu
        tbMeal.setItems(mealData);
    }

    private void loadMealData(int userId) {
        try {
            // Tạo kết nối cơ sở dữ liệu
            Connection conn = JdbcUtils.getConn();
            MealService mealService = new MealService(conn);
            
            // Lấy ngày hiện tại
            LocalDate today = LocalDate.now();
            Date sqlDate = Date.valueOf(today);
                        
            // Lấy tất cả bữa ăn từ trước đến ngày hiện tại
            List<Meal> meals = mealService.getMealsByUserAndToday(userId, sqlDate);
            
            System.out.println("Tìm thấy " + meals.size() + " bữa ăn từ trước đến nay");
            
            // In thông tin về tất cả bữa ăn đã được tìm thấy
            System.out.println("Danh sách bữa ăn trước khi sắp xếp:");
            for (Meal meal : meals) {
                System.out.println("Bữa ăn: " + meal.getNameMeal() + ", Ngày: " + meal.getDateOfMeal() 
                    + ", Calories: " + meal.getTotalCalories());
                
                // In thông tin chi tiết về thực phẩm trong bữa ăn
                if (meal.getMealFoodSet() != null) {
                    for (MealFood mealFood : meal.getMealFoodSet()) {
                        Food food = mealFood.getFood();
                        if (food != null) {
                            System.out.println("  - Thực phẩm: " + food.getFoodName() + 
                                ", Số lượng: " + mealFood.getQuantity() + " " + mealFood.getUnit() +
                                ", Calories: " + food.getCalories());
                        }
                    }
                }
            }
            
            // Sắp xếp bữa ăn theo ngày (tăng dần) - từ cũ đến mới 
            meals.sort((meal1, meal2) -> meal1.getDateOfMeal().compareTo(meal2.getDateOfMeal()));
            
            System.out.println("Danh sách bữa ăn sau khi sắp xếp tăng dần theo ngày:");
            for (Meal meal : meals) {
                System.out.println("Bữa ăn: " + meal.getNameMeal() + ", Ngày: " + meal.getDateOfMeal() 
                    + ", Calories: " + meal.getTotalCalories());
            }
            
            // Xóa dữ liệu cũ và thêm dữ liệu mới
            mealData.clear();
            mealData.addAll(meals);
            
            System.out.println("Số lượng bữa ăn đã thêm vào mealData: " + mealData.size());
            
            // Cập nhật bảng
            Platform.runLater(() -> {
                tbMeal.setItems(mealData);
                tbMeal.refresh();
                System.out.println("Đã cập nhật bảng tbMeal với " + tbMeal.getItems().size() + " bữa ăn");
                
                // Nếu bảng không có dữ liệu, hiển thị thông báo 
                if (mealData.isEmpty()) {
                    tbMeal.setPlaceholder(new Label("Không có dữ liệu bữa ăn nào từ trước đến nay"));
                }
            });
            
        } catch (SQLException ex) {
            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu bữa ăn: " + ex.getMessage());
            tbMeal.setPlaceholder(new Label("Lỗi khi tải dữ liệu"));
        }
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
