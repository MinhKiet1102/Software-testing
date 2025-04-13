package com.milkyway.healthmanagement;

import com.milkyway.pojo.NutritionGoal;
import com.milkyway.pojo.User;
import com.milkyway.services.NutritionGoalService;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.milkyway.pojo.JdbcUtils;

public class EditNutritionGoalsController implements Initializable {
    
    @FXML
    private TextField caloriesField;
    
    @FXML
    private TextField carbsField;
    
    @FXML
    private TextField proteinField;
    
    @FXML
    private TextField fatField;
    
    @FXML
    private TextField sodiumField;
    
    @FXML
    private TextField sugarField;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private User currentUser;
    private NutritionGoalService nutritionGoalService;
    private Map<String, NutritionGoal> userGoals;
    
    private final String[] nutritionTypes = {"Calories", "Carbohydrate", "Protein", "Fat", "Sodium", "Sugar"};
    private final String[] nutritionUnits = {"kcal", "g", "g", "g", "mg", "g"};
    private final double[] defaultGoalValues = {2000, 275, 50, 65, 2300, 50};
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Connection conn = JdbcUtils.getConn();
            nutritionGoalService = new NutritionGoalService(conn);
            currentUser = User.getCurrentUser();
            
            if (currentUser == null) {
                showAlert("Error", "No user logged in", Alert.AlertType.ERROR);
                closeDialog();
                return;
            }
            
            loadUserNutritionGoals();
            populateFields();
            
            // Đảm bảo áp dụng CSS sau khi scene được tạo
            saveButton.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    try {
                        String cssPath = getClass().getResource("/com/milkyway/healthmanagement/style/nutrition-goals.css").toExternalForm();
                        newValue.getStylesheets().add(cssPath);
                        System.out.println("CSS applied: " + cssPath);
                    } catch (Exception e) {
                        System.err.println("Error applying CSS: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            
        } catch (SQLException ex) {
            System.err.println("Error initializing EditNutritionGoalsController: " + ex.getMessage());
            showAlert("Database Error", "Could not connect to database: " + ex.getMessage(), Alert.AlertType.ERROR);
            closeDialog();
        }
    }
    
    private void loadUserNutritionGoals() throws SQLException {
        userGoals = new HashMap<>();
        if (currentUser != null) {
            for (NutritionGoal goal : nutritionGoalService.getGoalsByUserId(currentUser.getId())) {
                userGoals.put(goal.getNutritionType(), goal);
            }
        }
    }
    
    private void populateFields() {
        caloriesField.setText(String.valueOf(getGoalValue("Calories")));
        carbsField.setText(String.valueOf(getGoalValue("Carbohydrate")));
        proteinField.setText(String.valueOf(getGoalValue("Protein")));
        fatField.setText(String.valueOf(getGoalValue("Fat")));
        sodiumField.setText(String.valueOf(getGoalValue("Sodium")));
        sugarField.setText(String.valueOf(getGoalValue("Sugar")));
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
    
    private String getUnit(String nutritionType) {
        for (int i = 0; i < nutritionTypes.length; i++) {
            if (nutritionTypes[i].equals(nutritionType)) {
                return nutritionUnits[i];
            }
        }
        return "";
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            Map<String, Double> newValues = new HashMap<>();
            
            newValues.put("Calories", parseTextField(caloriesField, "Calories"));
            newValues.put("Carbohydrate", parseTextField(carbsField, "Carbohydrate"));
            newValues.put("Protein", parseTextField(proteinField, "Protein"));
            newValues.put("Fat", parseTextField(fatField, "Fat"));
            newValues.put("Sodium", parseTextField(sodiumField, "Sodium"));
            newValues.put("Sugar", parseTextField(sugarField, "Sugar"));
            
            saveGoals(newValues);
            closeDialog();
            
        } catch (NumberFormatException e) {
            // Error already shown in parseTextField
        } catch (SQLException e) {
            showAlert("Database Error", "Could not save nutrition goals: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private double parseTextField(TextField field, String nutritionType) throws NumberFormatException {
        try {
            // Kiểm tra trường hợp rỗng
            String text = field.getText();
            if (text == null || text.trim().isEmpty()) {
                showAlert("Lỗi dữ liệu", 
                        "Giá trị cho " + nutritionType + " không được để trống.", 
                        Alert.AlertType.ERROR);
                field.requestFocus();
                throw new NumberFormatException("Value is empty");
            }
            
            // Kiểm tra có phải là số không
            double value;
            try {
                value = Double.parseDouble(text);
            } catch (NumberFormatException e) {
                showAlert("Dữ liệu không hợp lệ", 
                        "Vui lòng nhập một số hợp lệ cho " + nutritionType + ".", 
                        Alert.AlertType.ERROR);
                field.requestFocus();
                throw e;
            }
            
            // Kiểm tra giá trị âm
            if (value < 0) {
                showAlert("Dữ liệu không hợp lệ", 
                        "Giá trị cho " + nutritionType + " không được âm.", 
                        Alert.AlertType.ERROR);
                field.requestFocus();
                throw new NumberFormatException("Value cannot be negative");
            }

            // Kiểm tra giá trị quá lớn so với hợp lý
            // Đặt các giới hạn cao nhất hợp lý cho từng loại dinh dưỡng
            boolean isTooLarge = false;
            String message = "";
            
            switch(nutritionType) {
                case "Calories":
                    if (value > 10000) { // Một người bình thường hiếm khi tiêu thụ trên 10000 calo/ngày
                        isTooLarge = true;
                        message = "Giá trị calo không nên vượt quá 10000 kcal/ngày.";
                    }
                    break;
                case "Carbohydrate":
                    if (value > 1000) { // Giới hạn carb hợp lý
                        isTooLarge = true;
                        message = "Giá trị carbohydrate không nên vượt quá 1000g/ngày.";
                    }
                    break;
                case "Protein":
                    if (value > 500) { // Giới hạn protein hợp lý
                        isTooLarge = true;
                        message = "Giá trị protein không nên vượt quá 500g/ngày.";
                    }
                    break;
                case "Fat":
                    if (value > 500) { // Giới hạn chất béo hợp lý
                        isTooLarge = true;
                        message = "Giá trị chất béo không nên vượt quá 500g/ngày.";
                    }
                    break;
                case "Sodium":
                    if (value > 10000) { // Giới hạn natri hợp lý
                        isTooLarge = true;
                        message = "Giá trị natri không nên vượt quá 10000mg/ngày.";
                    }
                    break;
                case "Sugar":
                    if (value > 500) { // Giới hạn đường hợp lý
                        isTooLarge = true;
                        message = "Giá trị đường không nên vượt quá 500g/ngày.";
                    }
                    break;
            }
            
            if (isTooLarge) {
                // Hiển thị cảnh báo nhưng vẫn cho phép người dùng tiếp tục
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText("Giá trị có vẻ cao");
                alert.setContentText(message + "\n\nBạn có chắc chắn muốn đặt giá trị này không?");
                
                ButtonType buttonYes = new ButtonType("Có");
                ButtonType buttonNo = new ButtonType("Không");
                
                alert.getButtonTypes().setAll(buttonYes, buttonNo);
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonNo) {
                    field.requestFocus();
                    throw new NumberFormatException("Value too large");
                }
            }
            
            return value;
        } catch (NumberFormatException e) {
            throw e;
        }
    }
    
    private void saveGoals(Map<String, Double> newValues) throws SQLException {
        for (String type : nutritionTypes) {
            double value = newValues.get(type);
            String unit = getUnit(type);
            
            if (userGoals.containsKey(type)) {
                // Update existing goal
                NutritionGoal goal = userGoals.get(type);
                goal.setGoalValue(value);
                nutritionGoalService.updateNutritionGoal(goal);
            } else {
                // Create new goal
                NutritionGoal goal = new NutritionGoal();
                goal.setUserId(currentUser.getId());
                goal.setNutritionType(type);
                goal.setGoalValue(value);
                goal.setUnit(unit);
                nutritionGoalService.addNutritionGoal(goal);
            }
        }
        
        showAlert("Success", "Nutrition goals saved successfully!", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}