package com.milkyway.healthmanagement;

import com.milkyway.pojo.Food;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.Meal;
import com.milkyway.pojo.MealFood;
import com.milkyway.pojo.NutritionGoal;
import com.milkyway.pojo.User;
import com.milkyway.services.MealService;
import com.milkyway.services.NutritionGoalService;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MealfoodController extends SwitchSceneController implements Initializable {
    
    @FXML
    private DatePicker consumeDatePicker;
    
    @FXML
    private TableView<MealFood> breakfastTableView, lunchTableView, dinnerTableView;
    
    @FXML
    private TableColumn<MealFood, String> BnameColumn, LnameColumn, DnameColumn;

    @FXML
    private TableColumn<MealFood, String> BquantityColumn, LquantityColumn, DquantityColumn;
    
    @FXML
    private TableColumn<MealFood, Number> BcaloColumn, BcarbColumn, BlipidColumn, BproColumn, BnaColumn, BsugarColumn;
    
    @FXML
    private TableColumn<MealFood, Number> LcaloColumn, LcarbColumn, LlipidColumn, LproColumn, LnaColumn, LsugarColumn;
    
    @FXML
    private TableColumn<MealFood, Number> DcaloColumn, DcarbColumn, DlipidColumn, DproColumn, DnaColumn, DsugarColumn;
    
    @FXML
    private TableColumn<MealFood, Void> BactionColumn, LactionColumn, DactionColumn;
    
    @FXML
    private Label breakfastCaloLabel, lunchCaloLabel, dinnerCaloLabel;
    
    @FXML
    private TableView<NutritionSummary> nutritionSummaryTable;
    
    @FXML
    private TableColumn<NutritionSummary, String> columnType;
    
    @FXML
    private TableColumn<NutritionSummary, Number> columnCurrent, columnGoal, columnRemaining;
    
    @FXML
    private Button btnClose;

    @FXML
    private Button btnMinimize;

    private User currentUser;
    private LocalDate selectedDate;
    private MealService mealService;
    private NutritionGoalService nutritionGoalService;
    private Map<String, NutritionGoal> userGoals;
    
    private final String[] nutritionTypes = {"Calories", "Carbohydrate", "Protein", "Fat", "Sodium", "Sugar"};
    private final String[] nutritionUnits = {"kcal", "g", "g", "g", "mg", "g"};
    private final double[] defaultGoalValues = {2000, 275, 50, 65, 2300, 50};
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            btnClose.setOnAction(event -> closeWindow(btnClose));
            btnMinimize.setOnAction(event -> minimizeWindow(btnMinimize));
            Connection conn = JdbcUtils.getConn();
            mealService = new MealService(conn);
            nutritionGoalService = new NutritionGoalService(conn);
            
            currentUser = User.getCurrentUser();
            
            if (currentUser == null) {
                showAlert("Error", "No user logged in", Alert.AlertType.ERROR);
                return;
            }
            
            selectedDate = LocalDate.now();
            consumeDatePicker.setValue(selectedDate);
            
            setupTableColumns();
            loadUserNutritionGoals();
            loadMealData();
            
            consumeDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                selectedDate = newVal;
                loadMealData();
            });
            
        } catch (SQLException ex) {
            System.err.println("Error initializing MealfoodController: " + ex.getMessage());
            showAlert("Database Error", "Could not connect to database: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void loadUserNutritionGoals() throws SQLException {
        userGoals = new HashMap<>();
        List<NutritionGoal> goals = nutritionGoalService.getGoalsByUserId(currentUser.getId());
        for (NutritionGoal goal : goals) {
            userGoals.put(goal.getNutritionType(), goal);
        }
    }
    
    private double getGoalValue(String nutritionType) {
        if (userGoals != null && userGoals.containsKey(nutritionType)) {
            return userGoals.get(nutritionType).getGoalValue();
        }
        for (int i = 0; i < nutritionTypes.length; i++) {
            if (nutritionTypes[i].equals(nutritionType)) {
                return defaultGoalValues[i];
            }
        }
        return 0;
    }
    
    private String getGoalUnit(String nutritionType) {
        if (userGoals != null && userGoals.containsKey(nutritionType)) {
            return userGoals.get(nutritionType).getUnit();
        }
        for (int i = 0; i < nutritionTypes.length; i++) {
            if (nutritionTypes[i].equals(nutritionType)) {
                return nutritionUnits[i];
            }
        }
        return "";
    }
    
    private void setupTableColumns() {
        // Setup breakfast columns
        setupColumn(BnameColumn, "Tên Thức Ăn", 150);
        setupColumn(BquantityColumn, "Số lượng/Đơn vị", 100);
        setupColumn(BcaloColumn, "Lượng Calo (cal)", 150);
        setupColumn(BcarbColumn, "Carbohydrate (g)", 120);
        setupColumn(BlipidColumn, "Lipid (g)", 100);
        setupColumn(BproColumn, "Protein (g)", 100);
        setupColumn(BnaColumn, "Natri (mg)", 100);
        setupColumn(BsugarColumn, "Đường (g)", 100);
        
        BnameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFood().getFoodName()));
        BquantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuantity() + " " + cellData.getValue().getUnit()));
        BcaloColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getCalories()));
        BcarbColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getCarb() != null ? cellData.getValue().getFood().getCarb() : 0));
        BlipidColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getFat() != null ? cellData.getValue().getFood().getFat() : 0));
        BproColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getProtein() != null ? cellData.getValue().getFood().getProtein() : 0));
        BnaColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getSodium() != null ? cellData.getValue().getFood().getSodium() : 0));
        BsugarColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getSugar() != null ? cellData.getValue().getFood().getSugar() : 0));
        
        setupActionColumn(BactionColumn, "Breakfast");
        
        // Setup lunch columns
        setupColumn(LnameColumn, "Tên Thức Ăn", 150);
        setupColumn(LquantityColumn, "Số lượng/Đơn vị", 100);
        setupColumn(LcaloColumn, "Lượng Calo (cal)", 150);
        setupColumn(LcarbColumn, "Carbohydrate (g)", 120);
        setupColumn(LlipidColumn, "Lipid (g)", 100);
        setupColumn(LproColumn, "Protein (g)", 100);
        setupColumn(LnaColumn, "Natri (mg)", 100);
        setupColumn(LsugarColumn, "Đường (g)", 100);
        
        LnameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFood().getFoodName()));
        LquantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuantity() + " " + cellData.getValue().getUnit()));
        LcaloColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getCalories()));
        LcarbColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getCarb() != null ? cellData.getValue().getFood().getCarb() : 0));
        LlipidColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getFat() != null ? cellData.getValue().getFood().getFat() : 0));
        LproColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getProtein() != null ? cellData.getValue().getFood().getProtein() : 0));
        LnaColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getSodium() != null ? cellData.getValue().getFood().getSodium() : 0));
        LsugarColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getSugar() != null ? cellData.getValue().getFood().getSugar() : 0));
        
        setupActionColumn(LactionColumn, "Lunch");
        
        // Setup dinner columns
        setupColumn(DnameColumn, "Tên Thức Ăn", 150);
        setupColumn(DquantityColumn, "Số lượng/Đơn vị", 100);
        setupColumn(DcaloColumn, "Lượng Calo (cal)", 150);
        setupColumn(DcarbColumn, "Carbohydrate (g)", 120);
        setupColumn(DlipidColumn, "Lipid (g)", 100);
        setupColumn(DproColumn, "Protein (g)", 100);
        setupColumn(DnaColumn, "Natri (mg)", 100);
        setupColumn(DsugarColumn, "Đường (g)", 100);
        
        DnameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFood().getFoodName()));
        DquantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuantity() + " " + cellData.getValue().getUnit()));
        DcaloColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getCalories()));
        DcarbColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getCarb() != null ? cellData.getValue().getFood().getCarb() : 0));
        DlipidColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getFat() != null ? cellData.getValue().getFood().getFat() : 0));
        DproColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getProtein() != null ? cellData.getValue().getFood().getProtein() : 0));
        DnaColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getSodium() != null ? cellData.getValue().getFood().getSodium() : 0));
        DsugarColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getFood().getSugar() != null ? cellData.getValue().getFood().getSugar() : 0));
        
        setupActionColumn(DactionColumn, "Dinner");
        
        // Setup nutrition summary columns
        if (nutritionSummaryTable != null) {
            setupColumn(columnType, "Loại chỉ số", 220);
            setupColumn(columnCurrent, "Giá trị hiện tại", 200);
            setupColumn(columnGoal, "Mục tiêu hàng ngày", 200);
            setupColumn(columnRemaining, "Còn lại", 200);
            
            columnType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNutritionType()));
            columnCurrent.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCurrentValue()));
            columnGoal.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getGoalValue()));
            columnRemaining.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getRemainingValue()));
        }
    }
    
    private <S, T> void setupColumn(TableColumn<S, T> column, String title, double prefWidth) {
        column.setText(title);
        column.setPrefWidth(prefWidth);
        column.setMinWidth(50);
        column.setResizable(true);
    }
    
    private void setupActionColumn(TableColumn<MealFood, Void> column, String mealType) {
        setupColumn(column, "Hành động", 150); // Tăng kích thước cột để chứa 2 nút
        
        column.setCellFactory(col -> {
            return new javafx.scene.control.TableCell<MealFood, Void>() {
                private final Button deleteButton = new Button("Xóa");
                private final Button editButton = new Button("Sửa");
                private final javafx.scene.layout.HBox buttonsBox = new javafx.scene.layout.HBox(5); // HBox để chứa 2 nút
                
                {
                    deleteButton.getStyleClass().add("delete-btn");
                    deleteButton.setMinWidth(60);
                    
                    editButton.getStyleClass().add("edit-btn");
                    editButton.setMinWidth(60);
                    
                    buttonsBox.getChildren().addAll(editButton, deleteButton);
                    
                    deleteButton.setOnAction(event -> {
                        MealFood mealFood = getTableView().getItems().get(getIndex());
                        handleDeleteFood(mealFood, mealType);
                    });
                    
                    editButton.setOnAction(event -> {
                        MealFood mealFood = getTableView().getItems().get(getIndex());
                        handleEditFood(mealFood, mealType);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonsBox);
                    }
                }
            };
        });
    }
    
    /**
     * Xóa món ăn khỏi bữa ăn
     * 
     * @param mealFood Đối tượng MealFood cần xóa
     * @param mealType Loại bữa ăn (Breakfast, Lunch, Dinner)
     */
    private void handleDeleteFood(MealFood mealFood, String mealType) {
        try {
            // Hiển thị hộp thoại xác nhận
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Bạn có chắc chắn muốn xóa thức ăn này khỏi " + getMealTypeInVietnamese(mealType) + "?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Xóa món ăn khỏi bữa ăn
                int mealId = mealFood.getMealFoodPK().getMealId();
                int foodId = mealFood.getMealFoodPK().getFoodId();
                mealService.deleteMealFood(mealId, foodId);
                
                // Làm mới dữ liệu
                loadMealData();
                
                showAlert("Thành công", "Đã xóa thức ăn khỏi " + getMealTypeInVietnamese(mealType), Alert.AlertType.INFORMATION);
            }
        } catch (SQLException ex) {
            System.err.println("Error deleting meal food: " + ex.getMessage());
            showAlert("Lỗi", "Không thể xóa thức ăn: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Chỉnh sửa thông tin của món ăn
     * 
     * @param mealFood Đối tượng MealFood cần chỉnh sửa
     * @param mealType Loại bữa ăn (Breakfast, Lunch, Dinner)
     */
    private void handleEditFood(MealFood mealFood, String mealType) {
        try {
            Dialog<Map<String, Object>> dialog = new Dialog<>();
            dialog.setTitle("Chỉnh sửa thức ăn");
            dialog.setHeaderText("Chỉnh sửa " + mealFood.getFood().getFoodName() + " trong " + getMealTypeInVietnamese(mealType));
            
            ButtonType saveButtonType = new ButtonType("Lưu", ButtonType.OK.getButtonData());
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
            
            // Tạo TextField cho tên món ăn
            TextField nameField = new TextField(mealFood.getFood().getFoodName());
            
            // Tạo TextField cho số lượng
            TextField quantityField = new TextField(String.valueOf(mealFood.getQuantity()));
            quantityField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            // Tạo ComboBox cho đơn vị
            ComboBox<String> unitComboBox = new ComboBox<>();
            unitComboBox.getItems().addAll("g", "ml", "phần", "quả");
            unitComboBox.setValue(mealFood.getUnit());
            
            // Tạo TextField cho calories
            TextField caloriesField = new TextField(String.valueOf(mealFood.getFood().getCalories()));
            caloriesField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            // Tạo TextField cho carbohydrates
            TextField carbField = new TextField(mealFood.getFood().getCarb() != null ? 
                                             String.valueOf(mealFood.getFood().getCarb()) : "0");
            carbField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            // Tạo TextField cho protein
            TextField proteinField = new TextField(mealFood.getFood().getProtein() != null ? 
                                               String.valueOf(mealFood.getFood().getProtein()) : "0");
            proteinField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            // Tạo TextField cho chất béo
            TextField fatField = new TextField(mealFood.getFood().getFat() != null ? 
                                           String.valueOf(mealFood.getFood().getFat()) : "0");
            fatField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            // Tạo TextField cho natri
            TextField sodiumField = new TextField(mealFood.getFood().getSodium() != null ? 
                                             String.valueOf(mealFood.getFood().getSodium()) : "0");
            sodiumField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            // Tạo TextField cho đường
            TextField sugarField = new TextField(mealFood.getFood().getSugar() != null ? 
                                            String.valueOf(mealFood.getFood().getSugar()) : "0");
            sugarField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            // Thêm các trường vào grid
            int rowIndex = 0;
            
            grid.add(new Label("Tên thức ăn:"), 0, rowIndex);
            grid.add(nameField, 1, rowIndex++);
            
            grid.add(new Label("Số lượng:"), 0, rowIndex);
            grid.add(quantityField, 1, rowIndex++);
            
            grid.add(new Label("Đơn vị:"), 0, rowIndex);
            grid.add(unitComboBox, 1, rowIndex++);
            
            grid.add(new Label("Lượng calo (cal):"), 0, rowIndex);
            grid.add(caloriesField, 1, rowIndex++);
            
            grid.add(new Label("Carbohydrate (g):"), 0, rowIndex);
            grid.add(carbField, 1, rowIndex++);
            
            grid.add(new Label("Protein (g):"), 0, rowIndex);
            grid.add(proteinField, 1, rowIndex++);
            
            grid.add(new Label("Chất béo (g):"), 0, rowIndex);
            grid.add(fatField, 1, rowIndex++);
            
            grid.add(new Label("Natri (mg):"), 0, rowIndex);
            grid.add(sodiumField, 1, rowIndex++);
            
            grid.add(new Label("Đường (g):"), 0, rowIndex);
            grid.add(sugarField, 1, rowIndex++);
            
            dialog.getDialogPane().setContent(grid);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    Map<String, Object> results = new HashMap<>();
                    
                    // Kiểm tra trường tên thức ăn có trống không
                    String foodName = nameField.getText().trim();
                    if (foodName.isEmpty()) {
                        showAlert("Lỗi", "Tên thức ăn không được để trống", Alert.AlertType.ERROR);
                        return null;
                    }
                    
                    // Kiểm tra trường số lượng có trống không
                    String quantityText = quantityField.getText().trim();
                    if (quantityText.isEmpty()) {
                        showAlert("Lỗi", "Số lượng không được để trống", Alert.AlertType.ERROR);
                        return null;
                    }
                    
                    // Kiểm tra các trường khác có trống không
                    if (caloriesField.getText().trim().isEmpty()) {
                        showAlert("Lỗi", "Lượng calo không được để trống", Alert.AlertType.ERROR);
                        return null;
                    }
                    
                    try {
                        int quantity = Integer.parseInt(quantityText);
                        if (quantity <= 0) {
                            showAlert("Lỗi", "Số lượng phải lớn hơn 0", Alert.AlertType.ERROR);
                            return null;
                        }
                        
                        Double calories = Double.parseDouble(caloriesField.getText().trim());
                        Double carb = Double.parseDouble(carbField.getText().trim());
                        Double protein = Double.parseDouble(proteinField.getText().trim());
                        Double fat = Double.parseDouble(fatField.getText().trim());
                        Double sodium = Double.parseDouble(sodiumField.getText().trim());
                        Double sugar = Double.parseDouble(sugarField.getText().trim());
                        
                        // Kiểm tra giá trị của calo và các dinh dưỡng khác
                        if (calories < 0) {
                            showAlert("Lỗi", "Lượng calo không thể âm", Alert.AlertType.ERROR);
                            return null;
                        }
                        
                        if (carb < 0 || protein < 0 || fat < 0 || sodium < 0 || sugar < 0) {
                            showAlert("Lỗi", "Các giá trị dinh dưỡng không thể âm", Alert.AlertType.ERROR);
                            return null;
                        }
                        
                        // Lưu tất cả các giá trị vào map results
                        results.put("foodName", foodName);
                        results.put("quantity", quantity);
                        results.put("unit", unitComboBox.getValue());
                        results.put("calories", calories);
                        results.put("carb", carb);
                        results.put("protein", protein);
                        results.put("fat", fat);
                        results.put("sodium", sodium);
                        results.put("sugar", sugar);
                        
                        return results;
                    } catch (NumberFormatException e) {
                        showAlert("Lỗi", "Vui lòng nhập số hợp lệ cho tất cả các trường số", Alert.AlertType.ERROR);
                        return null;
                    }
                }
                return null;
            });
            
            Optional<Map<String, Object>> result = dialog.showAndWait();
            
            if (result.isPresent()) {
                Map<String, Object> values = result.get();
                String newFoodName = (String) values.get("foodName");
                int newQuantity = (int) values.get("quantity");
                String newUnit = (String) values.get("unit");
                double newCalories = (double) values.get("calories");
                double newCarb = (double) values.get("carb");
                double newProtein = (double) values.get("protein");
                double newFat = (double) values.get("fat");
                double newSodium = (double) values.get("sodium");
                double newSugar = (double) values.get("sugar");
                
                // Cập nhật thông tin món ăn
                int mealId = mealFood.getMealFoodPK().getMealId();
                int foodId = mealFood.getMealFoodPK().getFoodId();
                
                // Cập nhật thông tin trong cơ sở dữ liệu
                mealService.updateMealFoodComplete(mealId, foodId, newFoodName, newQuantity, newUnit, 
                                              newCalories, newCarb, newProtein, newFat, newSodium, newSugar);
                
                // Làm mới dữ liệu
                loadMealData();
                
                showAlert("Thành công", "Đã cập nhật thông tin thức ăn trong " + getMealTypeInVietnamese(mealType), Alert.AlertType.INFORMATION);
            }
        } catch (SQLException ex) {
            System.err.println("Error updating meal food: " + ex.getMessage());
            showAlert("Lỗi", "Không thể cập nhật thông tin thức ăn: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    public void loadMealData() {
        try {
            if (currentUser == null) {
                return;
            }
            
            Date date = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            List<Meal> meals = mealService.getMealsByUserAndDate(currentUser.getId(), date);
            
            List<MealFood> breakfastFoods = new ArrayList<>();
            List<MealFood> lunchFoods = new ArrayList<>();
            List<MealFood> dinnerFoods = new ArrayList<>();
            
            double breakfastCalories = 0;
            double lunchCalories = 0;
            double dinnerCalories = 0;
            
            for (Meal meal : meals) {
                String mealName = meal.getNameMeal().toLowerCase();
                
                if (mealName.contains("breakfast") || mealName.contains("sáng")) {
                    for (MealFood mf : meal.getMealFoodSet()) {
                        breakfastFoods.add(mf);
                        // Chỉ lấy giá trị calories, không nhân với quantity
                        breakfastCalories += mf.getFood().getCalories();
                    }
                } else if (mealName.contains("lunch") || mealName.contains("trưa")) {
                    for (MealFood mf : meal.getMealFoodSet()) {
                        lunchFoods.add(mf);
                        // Chỉ lấy giá trị calories, không nhân với quantity
                        lunchCalories += mf.getFood().getCalories();
                    }
                } else if (mealName.contains("dinner") || mealName.contains("tối")) {
                    for (MealFood mf : meal.getMealFoodSet()) {
                        dinnerFoods.add(mf);
                        // Chỉ lấy giá trị calories, không nhân với quantity
                        dinnerCalories += mf.getFood().getCalories();
                    }
                }
            }
            
            String formattedDate = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            if (breakfastFoods.isEmpty()) {
                createEmptyTableMessage(breakfastTableView, "Không có dữ liệu cho ngày " + formattedDate + " của bữa sáng");
            } else {
                breakfastTableView.setItems(FXCollections.observableArrayList(breakfastFoods));
            }
            
            if (lunchFoods.isEmpty()) {
                createEmptyTableMessage(lunchTableView, "Không có dữ liệu cho ngày " + formattedDate + " của bữa trưa");
            } else {
                lunchTableView.setItems(FXCollections.observableArrayList(lunchFoods));
            }
            
            if (dinnerFoods.isEmpty()) {
                createEmptyTableMessage(dinnerTableView, "Không có dữ liệu cho ngày " + formattedDate + " của bữa tối");
            } else {
                dinnerTableView.setItems(FXCollections.observableArrayList(dinnerFoods));
            }
            
            breakfastCaloLabel.setText(String.format("Tổng calo: %.0f", breakfastCalories));
            lunchCaloLabel.setText(String.format("Tổng calo: %.0f", lunchCalories));
            dinnerCaloLabel.setText(String.format("Tổng calo: %.0f", dinnerCalories));
            
            updateNutritionSummary(breakfastFoods, lunchFoods, dinnerFoods);
            
        } catch (SQLException ex) {
            System.err.println("Error loading meal data: " + ex.getMessage());
            showAlert("Database Error", "Could not load meal data: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private <T> void createEmptyTableMessage(TableView<T> tableView, String message) {
        tableView.setPlaceholder(new Label(message));
        tableView.setItems(FXCollections.observableArrayList());
    }
    
    private void updateNutritionSummary(List<MealFood> breakfastFoods, List<MealFood> lunchFoods, List<MealFood> dinnerFoods) {
        if (nutritionSummaryTable == null) {
            return;
        }
        
        double totalCalories = 0;
        double totalCarbs = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalSodium = 0;
        double totalSugar = 0;
        
        List<List<MealFood>> allMeals = List.of(breakfastFoods, lunchFoods, dinnerFoods);
        for (List<MealFood> mealFoods : allMeals) {
            for (MealFood mf : mealFoods) {
                Food food = mf.getFood();
                int quantity = mf.getQuantity();
                
                totalCalories += food.getCalories() * quantity;
                totalCarbs += (food.getCarb() != null ? food.getCarb() : 0) * quantity;
                totalProtein += (food.getProtein() != null ? food.getProtein() : 0) * quantity;
                totalFat += (food.getFat() != null ? food.getFat() : 0) * quantity;
                totalSodium += (food.getSodium() != null ? food.getSodium() : 0) * quantity;
                totalSugar += (food.getSugar() != null ? food.getSugar() : 0) * quantity;
            }
        }
        
        double caloriesGoal = getGoalValue("Calories");
        double carbsGoal = getGoalValue("Carbohydrate");
        double proteinGoal = getGoalValue("Protein");
        double fatGoal = getGoalValue("Fat");
        double sodiumGoal = getGoalValue("Sodium");
        double sugarGoal = getGoalValue("Sugar");
        
        List<NutritionSummary> summaries = new ArrayList<>();
        summaries.add(new NutritionSummary("Calories", totalCalories, caloriesGoal, getGoalUnit("Calories")));
        summaries.add(new NutritionSummary("Carbohydrate", totalCarbs, carbsGoal, getGoalUnit("Carbohydrate")));
        summaries.add(new NutritionSummary("Protein", totalProtein, proteinGoal, getGoalUnit("Protein")));
        summaries.add(new NutritionSummary("Fat", totalFat, fatGoal, getGoalUnit("Fat")));
        summaries.add(new NutritionSummary("Sodium", totalSodium, sodiumGoal, getGoalUnit("Sodium")));
        summaries.add(new NutritionSummary("Sugar", totalSugar, sugarGoal, getGoalUnit("Sugar")));
        
        nutritionSummaryTable.setItems(FXCollections.observableArrayList(summaries));
    }
    
    @FXML
    private void handleEditNutritionGoals(ActionEvent event) {
        try {
            // Kiểm tra người dùng hiện tại
            if (currentUser == null) {
                showAlert("Lỗi", "Không có người dùng đăng nhập.", Alert.AlertType.ERROR);
                return;
            }
            
            Dialog<Map<String, Double>> dialog = new Dialog<>();
            dialog.setTitle("Chỉnh sửa mục tiêu dinh dưỡng");
            dialog.setHeaderText("Đặt mục tiêu dinh dưỡng hàng ngày của bạn");
            
            ButtonType saveButtonType = new ButtonType("Lưu", ButtonType.OK.getButtonData());
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
            
            Map<String, TextField> goalTextFields = new HashMap<>();
            
            for (int i = 0; i < nutritionTypes.length; i++) {
                String type = nutritionTypes[i];
                String unit = getGoalUnit(type);
                double currentGoal = getGoalValue(type);
                
                TextField goalField = new TextField(String.valueOf(currentGoal));
                goalTextFields.put(type, goalField);
                
                // Thêm TextFormatter để chỉ cho phép nhập số và dấu chấm
                goalField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                    String newText = change.getControlNewText();
                    // Cho phép số và dấu chấm
                    if (newText.matches("^\\d*\\.?\\d*$")) {
                        return change;
                    }
                    return null;
                }));
                
                grid.add(new Label(type + " (" + unit + "):"), 0, i);
                grid.add(goalField, 1, i);
            }
            
            dialog.getDialogPane().setContent(grid);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    Map<String, Double> results = new HashMap<>();
                    boolean hasError = false;
                    
                    for (String type : nutritionTypes) {
                        TextField field = goalTextFields.get(type);
                        String text = field.getText();
                        
                        // Kiểm tra trường hợp rỗng
                        if (text == null || text.trim().isEmpty()) {
                            showAlert("Lỗi dữ liệu", 
                                    "Giá trị cho " + type + " không được để trống.", 
                                    Alert.AlertType.ERROR);
                            hasError = true;
                            break;
                        }
                        
                        try {
                            double value = Double.parseDouble(text);
                            
                            // Kiểm tra giá trị âm
                            if (value < 0) {
                                showAlert("Lỗi", "Giá trị mục tiêu không được âm", Alert.AlertType.ERROR);
                                hasError = true;
                                break;
                            }
                            
                            // Kiểm tra giá trị quá lớn
                            boolean isTooLarge = false;
                            String message = "";
                            
                            switch(type) {
                                case "Calories":
                                    if (value > 10000) { 
                                        isTooLarge = true;
                                        message = "Giá trị calo không nên vượt quá 10000 kcal/ngày.";
                                    }
                                    break;
                                case "Carbohydrate":
                                    if (value > 1000) {
                                        isTooLarge = true;
                                        message = "Giá trị carbohydrate không nên vượt quá 1000g/ngày.";
                                    }
                                    break;
                                case "Protein":
                                    if (value > 500) {
                                        isTooLarge = true;
                                        message = "Giá trị protein không nên vượt quá 500g/ngày.";
                                    }
                                    break;
                                case "Fat":
                                    if (value > 500) {
                                        isTooLarge = true;
                                        message = "Giá trị chất béo không nên vượt quá 500g/ngày.";
                                    }
                                    break;
                                case "Sodium":
                                    if (value > 10000) {
                                        isTooLarge = true;
                                        message = "Giá trị natri không nên vượt quá 10000mg/ngày.";
                                    }
                                    break;
                                case "Sugar":
                                    if (value > 500) {
                                        isTooLarge = true;
                                        message = "Giá trị đường không nên vượt quá 500g/ngày.";
                                    }
                                    break;
                            }
                            
                            if (isTooLarge) {
                                // Hiển thị cảnh báo
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Cảnh báo");
                                alert.setHeaderText("Giá trị có vẻ cao");
                                alert.setContentText(message + "\n\nBạn có chắc chắn muốn đặt giá trị này không?");
                                
                                ButtonType buttonYes = new ButtonType("Có");
                                ButtonType buttonNo = new ButtonType("Không");
                                
                                alert.getButtonTypes().setAll(buttonYes, buttonNo);
                                
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == buttonNo) {
                                    hasError = true;
                                    break;
                                }
                            }
                            
                            results.put(type, value);
                        } catch (NumberFormatException e) {
                            showAlert("Lỗi", "Giá trị không hợp lệ cho " + type, Alert.AlertType.ERROR);
                            hasError = true;
                            break;
                        }
                    }
                    
                    return hasError ? null : results;
                }
                return null;
            });
            
            Optional<Map<String, Double>> result = dialog.showAndWait();
            
            if (result.isPresent()) {
                Map<String, Double> newGoals = result.get();
                
                for (int i = 0; i < nutritionTypes.length; i++) {
                    String type = nutritionTypes[i];
                    double value = newGoals.get(type);
                    String unit = nutritionUnits[i];
                    
                    try {
                        nutritionGoalService.saveOrUpdateGoal(currentUser.getId(), type, value, unit);
                    } catch (SQLException ex) {
                        System.err.println("Error saving nutrition goal: " + ex.getMessage());
                        showAlert("Lỗi", "Không thể lưu mục tiêu " + type + ": " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
                
                loadUserNutritionGoals();
                loadMealData();
                
                showAlert("Thành công", "Đã cập nhật mục tiêu dinh dưỡng", Alert.AlertType.INFORMATION);
            }
            
        } catch (Exception ex) {
            System.err.println("Error editing nutrition goals: " + ex.getMessage());
            showAlert("Lỗi", "Không thể chỉnh sửa mục tiêu dinh dưỡng: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Handle the "Add food" button for breakfast
     */
    @FXML
    private void handleAddBreakfast(ActionEvent event) {
        openAddFoodForm("Breakfast");
    }
    
    /**
     * Handle the "Add food" button for lunch
     */
    @FXML
    private void handleAddLunch(ActionEvent event) {
        openAddFoodForm("Lunch");
    }
    
    /**
     * Handle the "Add food" button for dinner
     */
    @FXML
    private void handleAddDinner(ActionEvent event) {
        openAddFoodForm("Dinner");
    }
    
    /**
     * Open the Add Food form for the specified meal type
     */
    private void openAddFoodForm(String mealType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addFoodForm.fxml"));
            Parent root = loader.load();
            
            // Get controller and set necessary data
            AddFoodFormController controller = loader.getController();
            controller.setMealType(mealType);
            controller.setCurrentUser(currentUser);
            controller.setSelectedDate(selectedDate);
            controller.setMealController(this);
            
            // Create a new stage for the form
            Stage stage = new Stage();
            stage.setTitle("Thêm thức ăn cho " + getMealTypeInVietnamese(mealType));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
            stage.show();
            
        } catch (IOException ex) {
            System.err.println("Error opening add food form: " + ex.getMessage());
            showAlert("Lỗi", "Không thể mở form thêm thức ăn: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Convert meal type to Vietnamese
     */
    private String getMealTypeInVietnamese(String mealType) {
        switch(mealType) {
            case "Breakfast":
                return "bữa sáng";
            case "Lunch":
                return "bữa trưa";
            case "Dinner":
                return "bữa tối";
            default:
                return mealType;
        }
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Xử lý sự kiện khi người dùng nhấn nút ngày trước đó (◀)
     */
    @FXML
    private void handlePrevDate(ActionEvent event) {
        // Lấy ngày hiện tại từ DatePicker và trừ đi 1 ngày
        LocalDate currentDate = consumeDatePicker.getValue();
        LocalDate previousDate = currentDate.minusDays(1);
        
        // Cập nhật giá trị DatePicker
        consumeDatePicker.setValue(previousDate);
        
        // selectedDate sẽ được cập nhật tự động thông qua Listener đã thiết lập trong initialize()
    }
    
    /**
     * Xử lý sự kiện khi người dùng nhấn nút ngày tiếp theo (▶)
     */
    @FXML
    private void handleNextDate(ActionEvent event) {
        // Lấy ngày hiện tại từ DatePicker và cộng thêm 1 ngày
        LocalDate currentDate = consumeDatePicker.getValue();
        LocalDate nextDate = currentDate.plusDays(1);
        
        // Kiểm tra xem ngày mới có phải là ngày trong tương lai không
        LocalDate today = LocalDate.now();
        if (nextDate.isAfter(today)) {
            showAlert("Thông báo", "Không thể chọn ngày trong tương lai.", Alert.AlertType.WARNING);
            return;
        }
        
        // Cập nhật giá trị DatePicker
        consumeDatePicker.setValue(nextDate);
        
        // selectedDate sẽ được cập nhật tự động thông qua Listener đã thiết lập trong initialize()
    }
    
    public static class NutritionSummary {
        private String nutritionType;
        private double currentValue;
        private double goalValue;
        private String unit;
        
        public NutritionSummary(String nutritionType, double currentValue, double goalValue, String unit) {
            this.nutritionType = nutritionType;
            this.currentValue = currentValue;
            this.goalValue = goalValue;
            this.unit = unit;
        }
        
        public String getNutritionType() {
            return nutritionType;
        }
        
        public double getCurrentValue() {
            return currentValue;
        }
        
        public double getGoalValue() {
            return goalValue;
        }
        
        public String getUnit() {
            return unit;
        }
        
        public double getRemainingValue() {
            return Math.max(0, goalValue - currentValue);
        }
        
        public String getFormattedCurrentValue() {
            return String.format("%.1f %s", currentValue, unit);
        }
        
        public String getFormattedGoalValue() {
            return String.format("%.1f %s", goalValue, unit);
        }
        
        public String getFormattedRemainingValue() {
            return String.format("%.1f %s", getRemainingValue(), unit);
        }
    }
}
