/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.milkyway.healthmanagement;

import com.milkyway.pojo.History;
import com.milkyway.pojo.User;
import com.milkyway.services.HistoryService;
import com.milkyway.services.PersonalInforService;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class PersonalInforController extends SwitchSceneController implements Initializable {

    @FXML
    private PasswordField newConfirmPassword;

    @FXML
    private DatePicker newDateAddWeight;

    @FXML
    private PasswordField newPassword;

    @FXML
    private TextField newUsername;

    @FXML
    private TextField newWeight;

    @FXML
    private TextField newHeight;

    @FXML
    private Text CurrentBMI;

    @FXML
    private Text CurrentWeight;

    @FXML
    private Text ChangeRecommendation;

    @FXML
    private Text WeightChange;

    @FXML
    private Text CaloriesPerDay;

    @FXML
    private Text WeightStatus;

    @FXML
    private Text OptimalWeight;

    @FXML
    private Text totalEatenCaloriesText;

    @FXML
    private Text totalBurnedCaloriesText;

    @FXML
    private Text totalEnteredCalories;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnMinimize;

    @FXML
    private TableView<WeightHistoryRow> weightHistoryTable;

    @FXML
    private TableColumn<WeightHistoryRow, String> dateColumn;

    @FXML
    private TableColumn<WeightHistoryRow, String> weightColumn;

    @FXML
    private TableColumn<WeightHistoryRow, String> heightColumn;

    private ObservableList<WeightHistoryRow> weightHistoryData = FXCollections.observableArrayList();

    private User signedInUser;

    private PersonalInforService personalInforService = new PersonalInforService();

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void handleUpdateUserName() throws SQLException {
        int userId = User.getCurrentUser().getId();
        String newusername = newUsername.getText();

        if (newusername == null || newusername.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Tên đăng nhập không được để trống!");

            return;
        }

        personalInforService.updateUsername(userId, newusername);
        newUsername.clear();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thay đổi tên đăng nhập thành công!");
        User.getCurrentUser().setUsername(newusername);
        displayUsername();
    }

    public void handleUpdatePassword(ActionEvent event) throws IOException, SQLException {
        int userId = User.getCurrentUser().getId();

        String newpassword = newPassword.getText();
        String newconfirmPassword = newConfirmPassword.getText();

        if (newpassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu không được để trống!");
            return;
        }

        if (newconfirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Xác nhận mật khẩu không được để trống!");
            return;
        }

        if (!newpassword.equals(newconfirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Xác nhận mật khẩu không trùng khớp với mật khẩu!");
            return;
        }

        if (newpassword.length() < 8) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu phải có ít nhất 8 ký tự!");
            return;
        }

        personalInforService.updatePassword(userId, newpassword);
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật mật khẩu thành công!");

        newPassword.clear();
        newConfirmPassword.clear();
    }

    public void handleUpdateWeight() throws SQLException {
        int userId = User.getCurrentUser().getId();

        // Kiểm tra nếu người dùng để trống hoặc sai định dạng
        if (newDateAddWeight.getEditor().getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngày không được để trống!");
            return;
        }

        String inputDateStr = newDateAddWeight.getEditor().getText().trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate weightDate;
        try {
            weightDate = LocalDate.parse(inputDateStr, formatter);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Sai định dạng ngày! Định dạng đúng là MM/dd/yyyy.");
            return;
        }

        Date utilDate = Date.from(weightDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String dateWeight = newWeight.getText().trim();
        String heightText = newHeight.getText().trim();

        if (dateWeight.isEmpty() && heightText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Hãy nhập ít nhất một trong hai giá trị: cân nặng hoặc chiều cao.");
            return;
        }

        BigDecimal currentWeight = User.getCurrentUser().getCurrentWeight();
        Integer currentHeight = User.getCurrentUser().getHeight();

        if (!dateWeight.isEmpty()) {
            try {
                BigDecimal weightValue = new BigDecimal(dateWeight);
                if (weightValue.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập giá trị trọng lượng dương lớn hơn 0.");
                    return;
                }
                personalInforService.updateWeight(userId, weightValue);
                User.getCurrentUser().setCurrentWeight(weightValue);
                currentWeight = weightValue;
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập giá trị trọng lượng hợp lệ (dùng dấu . cho số thập phân).");
                return;
            }
        }

        if (!heightText.isEmpty()) {
            try {
                int heightValue = Integer.parseInt(heightText);
                if (heightValue <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập chiều cao dương hợp lệ.");
                    return;
                }
                personalInforService.updateHeight(userId, heightValue);
                User.getCurrentUser().setHeight(heightValue);
                currentHeight = heightValue;
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập chiều cao hợp lệ (đơn vị cm).");
                return;
            }
        }

        // Lưu lịch sử
        History history = new History();
        history.setHistoryDate(utilDate);
        history.setHistoryWeight(currentWeight);
        history.setHistoryHeight(currentHeight);
        history.setUserId(User.getCurrentUser());

        new HistoryService().save(history);

        updateWeightHistoryTable();
        updateBMIAndRecommendations();

        newWeight.clear();
        newHeight.clear();

        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin thành công!");
    }

    private void updateBMIAndRecommendations() {
        User user = User.getCurrentUser();
        if (user.getCurrentWeight() == null || user.getHeight() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Cân nặng hoặc chiều cao chưa được thiết lập.");
            return;
        }
        double bmi = user.calculateBMI();
        BigDecimal optimalWeight = user.calculateOptimalWeight();
        String changeRecommendation = user.determineWeightChangeRecommendation();

        // Cập nhật UI
        CurrentBMI.setText(String.valueOf(bmi));
        CurrentWeight.setText(String.valueOf(user.getCurrentWeight()));
        ChangeRecommendation.setText(changeRecommendation);
        WeightChange.setText(user.calculateWeightToLoseOrGain().toString());
        CaloriesPerDay.setText(String.valueOf(user.calculateCaloriesPerDay()));
        WeightStatus.setText(user.determineWeightStatus());
        OptimalWeight.setText(String.valueOf(optimalWeight));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {

            btnClose.setOnAction(event -> closeWindow(btnClose));
            btnMinimize.setOnAction(event -> minimizeWindow(btnMinimize));
            displayUsername();
            newDateAddWeight.setValue(LocalDate.now());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            newDateAddWeight.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? formatter.format(date) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    if (string == null || string.trim().isEmpty()) {
                        return null;
                    }
                    try {
                        return LocalDate.parse(string, formatter);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                }
            });

            signedInUser = User.getCurrentUser();

            initializeWeightHistoryTable();
            updateWeightHistoryTable();

            HistoryService history = new HistoryService();
            BigDecimal latestWeight = history.findLatestHistoryByUserId(User.getCurrentUser().getId()).getHistoryWeight();
            User.getCurrentUser().setCurrentWeight(latestWeight);

            double bmi = signedInUser.calculateBMI();
            BigDecimal weightToLoseOrGain = signedInUser.calculateWeightToLoseOrGain();
            int caloriesPerDay = signedInUser.calculateCaloriesPerDay();

            // Update UI elements
            CurrentBMI.setText(String.valueOf(bmi));
            CurrentWeight.setText(String.valueOf(signedInUser.getCurrentWeight()));
            ChangeRecommendation.setText(signedInUser.determineWeightChangeRecommendation());
            WeightChange.setText(weightToLoseOrGain.toString());
            CaloriesPerDay.setText(String.valueOf(caloriesPerDay));
            WeightStatus.setText(signedInUser.determineWeightStatus());
            OptimalWeight.setText(String.valueOf(signedInUser.calculateOptimalWeight()));

        } catch (SQLException ex) {
            Logger.getLogger(PersonalInforController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateWeightHistoryTable() throws SQLException {
        HistoryService history = new HistoryService();
        int userId = User.getCurrentUser().getId();
        List<History> historyList = history.findAllByUserId(userId);

        weightHistoryData.clear();
        for (History h : historyList) {
            WeightHistoryRow row = new WeightHistoryRow(
                    h.getHistoryDate().toString(),
                    String.valueOf(h.getHistoryWeight()),
                    String.valueOf(h.getHistoryHeight())
            );
            weightHistoryData.add(row);
        }

        weightHistoryTable.setItems(weightHistoryData);
    }

    private void initializeWeightHistoryTable() {
        weightHistoryTable.getColumns().clear();
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        heightColumn.setCellValueFactory(new PropertyValueFactory<>("height"));

        weightHistoryTable.getColumns().addAll(dateColumn, weightColumn, heightColumn);
    }

    public static class WeightHistoryRow {

        private final String date;
        private final String weight;
        private final String height;

        public WeightHistoryRow(String date, String weight, String height) {
            this.date = date;
            this.weight = weight;
            this.height = height;
        }

        public String getDate() {
            return date;
        }

        public String getWeight() {
            return weight;
        }

        public String getHeight() {
            return height;
        }
    }

}
