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
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

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

    private void handleDatePickerAction(ActionEvent event) {
        LocalDate selectedDate = newDateAddWeight.getValue();
        System.out.println("Selected date: " + selectedDate);
    }

    public void handleUpdateWeight() throws SQLException {
        int userId = User.getCurrentUser().getId();
        LocalDate weightDate = newDateAddWeight.getValue();

        if (weightDate == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngày không được để trống!");
            return;
        }

        Date utilDate = Date.from(weightDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String dateWeight = newWeight.getText();
        String heightText = newHeight.getText();

        if (dateWeight.isEmpty() && (heightText == null || heightText.isEmpty())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Hãy nhập ít nhất một trong hai giá trị: cân nặng hoặc chiều cao.");
            return;
        }

        // Lấy giá trị hiện tại từ cơ sở dữ liệu hoặc từ đối tượng User
        BigDecimal currentWeight = User.getCurrentUser().getCurrentWeight();
        Integer currentHeight = User.getCurrentUser().getHeight();

        // Xử lý cập nhật cân nặng nếu có
        if (!dateWeight.isEmpty()) {
            try {
                BigDecimal weightValue = new BigDecimal(dateWeight);
                if (weightValue.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập giá trị trọng số dương khác không.");
                    return;
                }
                personalInforService.updateWeight(userId, weightValue);
                User.getCurrentUser().setCurrentWeight(weightValue);
                currentWeight = weightValue;
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập giá trị trọng lượng hợp lệ (sử dụng dấu . cho số thập phân).");
                return;
            }
        }

        // Xử lý cập nhật chiều cao nếu có
        if (heightText != null && !heightText.isEmpty()) {
            try {
                Integer heightValue = Integer.parseInt(heightText);
                if (heightValue <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập chiều cao dương hợp lệ.");
                    return;
                }
                personalInforService.updateHeight(userId, heightValue);
                User.getCurrentUser().setHeight(heightValue);
                currentHeight = heightValue;
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập giá trị chiều cao hợp lệ(Đơn vị đo là cm).");
                return;
            }
        }

        // Thêm dữ liệu vào bảng history
        History history = new History();
        history.setHistoryDate(utilDate);
        history.setHistoryWeight(currentWeight); // Sử dụng giá trị hiện tại
        history.setHistoryHeight(currentHeight); // Sử dụng giá trị hiện tại
        history.setUserId(User.getCurrentUser());

        HistoryService historyService = new HistoryService();
        historyService.save(history);

        // Cập nhật bảng hiển thị
        updateWeightHistoryTable();

        // Cập nhật BMI và các thông tin liên quan
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

//    private double calculateAndDisplayTotalEatenCalories() {
//        UserFoodDaoImpl userFoodDao =new UserFoodDaoImpl();
//
//        int userId = User.currentUser.getId();
//
//        LocalDate foodDate = LocalDate.now();
//        Instant instant = foodDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//        Date utilDate = Date.from(instant);
//
//        double totalCalories= userFoodDao.calculateTotalCalories(userId,utilDate);
//        totalEatenCaloriesText.setText(String.valueOf(totalCalories));
//        return  totalCalories;
//    }
//    private double calculateAndDisplayTotalBurnedCalories() {
//        UserExerciseDaoImpl userExerciseDao = new UserExerciseDaoImpl();
//
//        int userId = User.currentUser.getId();
//
//        LocalDate exerciseDate = LocalDate.now();
//        Instant instant = exerciseDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//        Date utilDate = Date.from(instant);
//        double totalCalories= userExerciseDao.calculateTotalBurnedCalories(userId,utilDate);
//        totalBurnedCaloriesText.setText(String.valueOf(totalCalories));
//        return totalCalories;
//    }
//    private void calculateAndDisplayTotalEnteredCalories(){
//        double enteringBodyCalories= calculateAndDisplayTotalEatenCalories()-calculateAndDisplayTotalBurnedCalories();
//        totalEnteredCalories.setText(String.valueOf(enteringBodyCalories));
//
//    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            displayUsername();

            newDateAddWeight.setValue(LocalDate.now());

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

//        calculateAndDisplayTotalEatenCalories();
//        calculateAndDisplayTotalBurnedCalories();
//        calculateAndDisplayTotalEnteredCalories();
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
