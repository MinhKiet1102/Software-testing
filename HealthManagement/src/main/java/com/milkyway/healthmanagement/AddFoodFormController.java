package com.milkyway.healthmanagement;

import com.milkyway.pojo.Food;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.Meal;
import com.milkyway.pojo.User;
import com.milkyway.services.MealService;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the add food form
 */
public class AddFoodFormController implements Initializable {
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField quantityField;
    
    @FXML
    private ComboBox<String> unitComboBox;
    
    @FXML
    private TextField caloField;
    
    @FXML
    private TextField carbField;
    
    @FXML
    private TextField lipidField;
    
    @FXML
    private TextField proteinField;
    
    @FXML
    private TextField natriField;
    
    @FXML
    private TextField sugarField;
    
    private String mealType;
    private User currentUser;
    private LocalDate selectedDate;
    private MealfoodController mealController;
    private Connection conn;
    private MealService mealService;
    
    /**
     * Initializes the controller
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            conn = JdbcUtils.getConn();
            mealService = new MealService(conn);
            
            // Thiết lập giá trị mặc định
            quantityField.setText("100");
            
            // Thiết lập các giá trị cho ComboBox đơn vị
            unitComboBox.getItems().addAll("g", "ml", "phần/cái");
            unitComboBox.setValue("g");
            
            // Thiết lập các TextFormatter để chỉ cho phép nhập số
            quantityField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            caloField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            carbField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            lipidField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            proteinField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            natriField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
            sugarField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    return change;
                }
                return null;
            }));
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi khởi tạo AddFoodFormController: " + ex.getMessage());
            showAlert("Lỗi", "Không thể kết nối đến cơ sở dữ liệu: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Handle the save button action
     */
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }
        
        try {
            // Save food to database
            int foodId = saveFoodToDatabase();
            if (foodId <= 0) {
                showAlert("Lỗi", "Không thể lưu thức ăn mới vào cơ sở dữ liệu", Alert.AlertType.ERROR);
                return;
            }
            
            // Get or create a meal for the current date and meal type
            int mealId = getOrCreateMeal();
            if (mealId <= 0) {
                showAlert("Lỗi", "Không thể tạo bữa ăn", Alert.AlertType.ERROR);
                return;
            }
            
            // Lấy giá trị số lượng và đơn vị từ form
            double quantity = Double.parseDouble(quantityField.getText().trim());
            String unit = unitComboBox.getValue();
            
            // Add food to meal
            mealService.addFoodToMeal(mealId, foodId, quantity, unit);
            
            // Show success message
            showAlert("Thành công", "Thức ăn đã được thêm vào " + getMealTypeInVietnamese(mealType), Alert.AlertType.INFORMATION);
            
            // Refresh the main view
            if (mealController != null) {
                mealController.loadMealData();
            }
            
            // Close the window
            closeWindow();
            
        } catch (SQLException ex) {
            System.err.println("Lỗi khi lưu thức ăn: " + ex.getMessage());
            showAlert("Lỗi cơ sở dữ liệu", "Không thể lưu thức ăn: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Handle the cancel button action
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }
    
    /**
     * Validate form inputs
     * 
     * @return true if inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        // Check if name is provided
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Lỗi nhập liệu", "Vui lòng nhập tên thức ăn", Alert.AlertType.ERROR);
            nameField.requestFocus();
            return false;
        }
        
        // Check if quantity is provided and valid
        try {
            double quantity = Double.parseDouble(quantityField.getText().trim());
            if (quantity <= 0) {
                showAlert("Lỗi nhập liệu", "Số lượng phải lớn hơn 0", Alert.AlertType.ERROR);
                quantityField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi nhập liệu", "Số lượng phải là một số hợp lệ", Alert.AlertType.ERROR);
            quantityField.requestFocus();
            return false;
        }
        
        // Check if unit is selected
        if (unitComboBox.getValue() == null || unitComboBox.getValue().trim().isEmpty()) {
            showAlert("Lỗi nhập liệu", "Vui lòng chọn đơn vị", Alert.AlertType.ERROR);
            unitComboBox.requestFocus();
            return false;
        }
        
        // Check if calorie is provided and valid
        try {
            double calories = Double.parseDouble(caloField.getText().trim());
            if (calories < 0) {
                showAlert("Lỗi nhập liệu", "Lượng calo không thể âm", Alert.AlertType.ERROR);
                caloField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi nhập liệu", "Lượng calo phải là một số", Alert.AlertType.ERROR);
            caloField.requestFocus();
            return false;
        }
        
        // Optional fields: carb, lipid, protein, natri, sugar
        // If provided, they must be valid numbers
        if (!validateOptionalNumberField(carbField, "Carbohydrate")) return false;
        if (!validateOptionalNumberField(lipidField, "Lipid")) return false;
        if (!validateOptionalNumberField(proteinField, "Protein")) return false;
        if (!validateOptionalNumberField(natriField, "Natri")) return false;
        if (!validateOptionalNumberField(sugarField, "Đường")) return false;
        
        return true;
    }
    
    /**
     * Validate an optional number field
     * 
     * @param field The field to validate
     * @param fieldName The name of the field for error messages
     * @return true if valid, false otherwise
     */
    private boolean validateOptionalNumberField(TextField field, String fieldName) {
        if (!field.getText().trim().isEmpty()) {
            try {
                double value = Double.parseDouble(field.getText().trim());
                if (value < 0) {
                    showAlert("Lỗi nhập liệu", fieldName + " không thể âm", Alert.AlertType.ERROR);
                    field.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Lỗi nhập liệu", fieldName + " phải là một số", Alert.AlertType.ERROR);
                field.requestFocus();
                return false;
            }
        }
        return true;
    }
    
    /**
     * Save the food to the database
     * 
     * @return The ID of the newly created food, or -1 if an error occurred
     */
    private int saveFoodToDatabase() throws SQLException {
        String sql = "INSERT INTO food (foodName, calories, protein, carb, fat, sodium, sugar) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nameField.getText().trim());
            stmt.setDouble(2, Double.parseDouble(caloField.getText().trim()));
            
            // Set optional fields, or null if not provided
            if (!proteinField.getText().trim().isEmpty()) {
                stmt.setDouble(3, Double.parseDouble(proteinField.getText().trim()));
            } else {
                stmt.setNull(3, java.sql.Types.DOUBLE);
            }
            
            if (!carbField.getText().trim().isEmpty()) {
                stmt.setDouble(4, Double.parseDouble(carbField.getText().trim()));
            } else {
                stmt.setNull(4, java.sql.Types.DOUBLE);
            }
            
            if (!lipidField.getText().trim().isEmpty()) {
                stmt.setDouble(5, Double.parseDouble(lipidField.getText().trim()));
            } else {
                stmt.setNull(5, java.sql.Types.DOUBLE);
            }
            
            // Add sodium field
            if (!natriField.getText().trim().isEmpty()) {
                stmt.setDouble(6, Double.parseDouble(natriField.getText().trim()));
            } else {
                stmt.setNull(6, java.sql.Types.DOUBLE);
            }
            
            // Add sugar field
            if (!sugarField.getText().trim().isEmpty()) {
                stmt.setDouble(7, Double.parseDouble(sugarField.getText().trim()));
            } else {
                stmt.setNull(7, java.sql.Types.DOUBLE);
            }
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Tạo thức ăn thất bại, không có dòng nào được thêm");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Tạo thức ăn thất bại, không lấy được ID");
                }
            }
        }
    }
    
    /**
     * Get existing meal or create a new one for the selected date and meal type
     * 
     * @return The meal ID
     */
    private int getOrCreateMeal() throws SQLException {
        // First check if a meal for this date and type already exists
        String checkSql = "SELECT idMeal FROM meal WHERE userId = ? AND DATE(dateOfMeal) = ? AND nameMeal = ?";
        
        Date date = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, currentUser.getId());
            checkStmt.setDate(2, sqlDate);
            checkStmt.setString(3, mealType);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idMeal");
                }
            }
        }
        
        // If no meal exists, create a new one
        String createSql = "INSERT INTO meal (nameMeal, totalCalories, dateOfMeal, userId) VALUES (?, 0, ?, ?)";
        
        try (PreparedStatement createStmt = conn.prepareStatement(createSql, Statement.RETURN_GENERATED_KEYS)) {
            createStmt.setString(1, mealType);
            createStmt.setTimestamp(2, new java.sql.Timestamp(date.getTime()));
            createStmt.setInt(3, currentUser.getId());
            
            int affectedRows = createStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Tạo bữa ăn thất bại, không có dòng nào được thêm");
            }
            
            try (ResultSet generatedKeys = createStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Tạo bữa ăn thất bại, không lấy được ID");
                }
            }
        }
    }
    
    /**
     * Close the form window
     */
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Get Vietnamese name for meal type
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
    
    /**
     * Set the meal type
     */
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
    
    /**
     * Set the current user
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    /**
     * Set the selected date
     */
    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }
    
    /**
     * Set the meal controller for refreshing data
     */
    public void setMealController(MealfoodController mealController) {
        this.mealController = mealController;
    }
}
