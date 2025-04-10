package com.milkyway.healthmanagement;

import com.milkyway.pojo.FoodItem;
import com.milkyway.pojo.NutritionSummaryItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MealfoodController implements Initializable {

    private final ObservableList<NutritionSummaryItem> nutritionSummaryData = FXCollections.observableArrayList();
    @FXML
    private TableView<NutritionSummaryItem> nutritionSummaryTable;
    @FXML
    private TableColumn<NutritionSummaryItem, String> columnType;
    @FXML
    private TableColumn<NutritionSummaryItem, Double> columnCurrent;
    @FXML
    private TableColumn<NutritionSummaryItem, Double> columnGoal;
    @FXML
    private TableColumn<NutritionSummaryItem, Double> columnRemaining;
    // TableView các bữa ăn
    @FXML
    private Label breakfastCaloLabel;
    @FXML
    private Label lunchCaloLabel;
    @FXML
    private Label dinnerCaloLabel;

    @FXML
    private TableView<FoodItem> breakfastTableView;
    @FXML
    private TableView<FoodItem> lunchTableView;
    @FXML
    private TableView<FoodItem> dinnerTableView;

    @FXML
    private DatePicker consumeDatePicker;

    // Các cột - dùng chung nếu fx:id giống nhau
    @FXML
    private TableColumn<FoodItem, String> nameColumn;
    @FXML
    private TableColumn<FoodItem, Integer> caloColumn;
    @FXML
    private TableColumn<FoodItem, Double> carbColumn;
    @FXML
    private TableColumn<FoodItem, Double> lipidColumn;
    @FXML
    private TableColumn<FoodItem, Double> proteinColumn;
    @FXML
    private TableColumn<FoodItem, Integer> natriColumn;
    @FXML
    private TableColumn<FoodItem, Double> sugarColumn;

    private final ObservableList<FoodItem> breakfastData = FXCollections.observableArrayList();
    private final ObservableList<FoodItem> lunchData = FXCollections.observableArrayList();
    private final ObservableList<FoodItem> dinnerData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Liên kết bảng và dữ liệu
        breakfastTableView.setItems(breakfastData);
        lunchTableView.setItems(lunchData);
        dinnerTableView.setItems(dinnerData);

        // Gán cột cho từng bảng
        setupColumns(breakfastTableView);
        setupColumns(lunchTableView);
        setupColumns(dinnerTableView);
        nutritionSummaryTable.setItems(nutritionSummaryData);

// Map các cột (giống setupColumns cho bảng bữa ăn)
        columnType.setCellValueFactory(new PropertyValueFactory<>("type"));
        columnCurrent.setCellValueFactory(new PropertyValueFactory<>("current"));
        columnGoal.setCellValueFactory(new PropertyValueFactory<>("goal"));
        columnRemaining.setCellValueFactory(new PropertyValueFactory<>("remaining"));

    }

    private void updateNutritionSummary() {
        double totalCalo = 0, totalCarb = 0, totalFat = 0, totalProtein = 0, totalSodium = 0, totalSugar = 0;

        for (FoodItem item : breakfastData) {
            totalCalo += item.getCalo();
            totalCarb += item.getCarb();
            totalFat += item.getFat();
            totalProtein += item.getProtein();
            totalSodium += item.getSodium();
            totalSugar += item.getSugar();
        }
        for (FoodItem item : lunchData) {
            totalCalo += item.getCalo();
            totalCarb += item.getCarb();
            totalFat += item.getFat();
            totalProtein += item.getProtein();
            totalSodium += item.getSodium();
            totalSugar += item.getSugar();
        }
        for (FoodItem item : dinnerData) {
            totalCalo += item.getCalo();
            totalCarb += item.getCarb();
            totalFat += item.getFat();
            totalProtein += item.getProtein();
            totalSodium += item.getSodium();
            totalSugar += item.getSugar();
        }

        nutritionSummaryData.clear();
        nutritionSummaryData.add(new NutritionSummaryItem("Calo (cal)", totalCalo, 2000));  // giả sử goal 2000 calo
        nutritionSummaryData.add(new NutritionSummaryItem("Carbohydrate (g)", totalCarb, 300)); // goal 300g carb
        nutritionSummaryData.add(new NutritionSummaryItem("Lipid (g)", totalFat, 70)); // goal 70g fat
        nutritionSummaryData.add(new NutritionSummaryItem("Protein (g)", totalProtein, 50)); // goal 50g protein
        nutritionSummaryData.add(new NutritionSummaryItem("Natri (mg)", totalSodium, 2300)); // goal 2300mg natri
        nutritionSummaryData.add(new NutritionSummaryItem("Đường (g)", totalSugar, 50)); // goal 50g sugar
    }

    private int calculateTotalCalories(ObservableList<FoodItem> mealData) {
        int totalCalories = 0;
        for (FoodItem item : mealData) {
            totalCalories += item.getCalo();
        }
        return totalCalories;
    }

    private void setupColumns(TableView<FoodItem> tableView) {
        for (TableColumn<FoodItem, ?> column : tableView.getColumns()) {
            switch (column.getText()) {
                case "Tên Thức Ăn":
                    ((TableColumn<FoodItem, String>) column).setCellValueFactory(new PropertyValueFactory<>("name"));
                    break;
                case "Lượng Calo (cal)":
                    ((TableColumn<FoodItem, Integer>) column).setCellValueFactory(new PropertyValueFactory<>("calo"));
                    break;
                case "Carbohydrate (g)":
                    ((TableColumn<FoodItem, Double>) column).setCellValueFactory(new PropertyValueFactory<>("carb"));
                    break;
                case "Lipid (g)":
                    ((TableColumn<FoodItem, Double>) column).setCellValueFactory(new PropertyValueFactory<>("fat"));
                    break;
                case "Protein (g)":
                    ((TableColumn<FoodItem, Double>) column).setCellValueFactory(new PropertyValueFactory<>("protein"));
                    break;
                case "Natri (mg)":
                    ((TableColumn<FoodItem, Integer>) column).setCellValueFactory(new PropertyValueFactory<>("sodium"));
                    break;
                case "Đường (g)":
                    ((TableColumn<FoodItem, Double>) column).setCellValueFactory(new PropertyValueFactory<>("sugar"));
                    break;
            }

        }
    }

    // Hàm mở form nhập liệu
    public void openAddFoodForm(String mealType) {
        try {
            // 📌 Kiểm tra ngày tiêu thụ không được là tương lai
            if (consumeDatePicker.getValue() != null && consumeDatePicker.getValue().isAfter(java.time.LocalDate.now())) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText(null);
                alert.setContentText("Không thể thêm thực phẩm cho ngày trong tương lai!");
                alert.showAndWait();
                return; // Dừng mở form
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milkyway/healthmanagement/addFoodForm.fxml"));
            Parent root = loader.load();

            AddFoodFormController controller = loader.getController();
            controller.setMealType(mealType);
            controller.setListener(item -> {
                switch (mealType) {
                    case "breakfast":
                        breakfastData.add(item);
                        breakfastCaloLabel.setText("Tổng calo: " + calculateTotalCalories(breakfastData));
                        break;
                    case "lunch":
                        lunchData.add(item);
                        lunchCaloLabel.setText("Tổng calo: " + calculateTotalCalories(lunchData));
                        break;
                    case "dinner":
                        dinnerData.add(item);
                        dinnerCaloLabel.setText("Tổng calo: " + calculateTotalCalories(dinnerData));
                        break;
                }
                updateNutritionSummary();
            });

            Stage stage = new Stage();
            stage.setTitle("Thêm thức ăn cho " + mealType);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Gắn vào nút trong FXML
    @FXML
    public void handleAddBreakfast() {
        openAddFoodForm("breakfast");
    }

    @FXML
    public void handleAddLunch() {
        openAddFoodForm("lunch");
    }

    @FXML
    public void handleAddDinner() {
        openAddFoodForm("dinner");
    }

}
