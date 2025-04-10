package com.milkyway.healthmanagement;

import com.milkyway.pojo.FoodItem;
import com.milkyway.service.FoodItemService;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddFoodFormController implements Initializable {

    @FXML
    private TextField nameField;
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

    @FXML
    private void handleSave() {
        try {
            // 📌 1. Kiểm tra các ô không được để trống
            if (nameField.getText().isEmpty() || caloField.getText().isEmpty() || carbField.getText().isEmpty()
                    || lipidField.getText().isEmpty() || proteinField.getText().isEmpty()
                    || natriField.getText().isEmpty() || sugarField.getText().isEmpty()) {

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng nhập đầy đủ thông tin dinh dưỡng!");
                alert.showAndWait();
                return;
            }

            // 📌 2. Kiểm tra định dạng số
          
            double carb, fat, protein, sugar,calo, sodium;

            try {
                calo = Integer.parseInt(caloField.getText());
                carb = Double.parseDouble(carbField.getText());
                fat = Double.parseDouble(lipidField.getText());
                protein = Double.parseDouble(proteinField.getText());
                sodium = Integer.parseInt(natriField.getText());
                sugar = Double.parseDouble(sugarField.getText());
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi nhập liệu");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng nhập đúng định dạng số cho các ô dinh dưỡng!");
                alert.showAndWait();
                return;
            }

            // 📌 3. Kiểm tra giá trị hợp lý (không âm, không quá lớn)
            if (calo < 0 || carb < 0 || fat < 0 || protein < 0 || sodium < 0 || sugar < 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText(null);
                alert.setContentText("Không được nhập số âm!");
                alert.showAndWait();
                return;
            }

            if (calo < 10) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText(null);
                alert.setContentText("Lượng calo quá thấp, vui lòng kiểm tra lại!");
                alert.showAndWait();
                return;
            }

            if (calo > 5000 || carb > 1000 || fat > 500 || protein > 500 || sodium > 6000 || sugar > 300) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText(null);
                alert.setContentText("Giá trị dinh dưỡng vượt mức hợp lý! Vui lòng kiểm tra lại.");
                alert.showAndWait();
                return;
            }

            // 📌 4. Tạo đối tượng FoodItem
            String name = nameField.getText().trim();
            FoodItem newItem = new FoodItem(name, calo, carb, fat, protein, sodium, sugar);

            // 📌 5. Gửi dữ liệu cho listener (controller cha)
            if (listener != null) {
                listener.onFoodSubmitted(newItem);
            }

            // 📌 6. Hiện thông báo lưu thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Lưu thực phẩm thành công!");
            alert.showAndWait();

            // 📌 7. Đóng cửa sổ form
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Có lỗi xảy ra, vui lòng thử lại!");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
    private FoodSubmitListener listener;

    public void setListener(FoodSubmitListener listener) {
        this.listener = listener;

    }

    public interface FoodSubmitListener {

        void onFoodSubmitted(FoodItem item);
    }

    private String mealType;

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nameField.setPromptText("Ví dụ: Cơm trắng");
        caloField.setPromptText("calo (vd: 200)");
        carbField.setPromptText("carb (g)");
        lipidField.setPromptText("fat (g)");
        proteinField.setPromptText("protein (g)");
        natriField.setPromptText("natri (mg)");
        sugarField.setPromptText("đường (g)");
    }
}
