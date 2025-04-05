/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.milkyway.healthmanagement;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private Label username;

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleUpdateUserName() throws SQLException {
        int userId = User.currentUser.getId();
        String newusername = newUsername.getText();

        if (newusername == null || newusername.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Tên đăng nhập không được để trống!");

            return;
        }

        updateUsername(userId, newusername);
        newUsername.clear();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thay đổi tên đăng nhập thành công!");
        User.currentUser.setUsername(newusername);
    }

    public void updateUsername(int userId, String newUsername) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return;
        }

        String query = "UPDATE user SET username=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, newUsername);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void handleUpdatePassword(ActionEvent event) throws IOException, SQLException {
        int userId = User.currentUser.getId();

        String newpassword = newPassword.getText();
        String newconfirmPassword = newConfirmPassword.getText();

        if (newpassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu không được để trống!");
            return;
        }

        if (!newpassword.equals(newconfirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Xác nhận mật khẩu không được để trống!");
            return;
        }

        // Update password
        updatePassword(userId, newpassword);
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật mật khẩu thành công!");

        newPassword.clear();
        newConfirmPassword.clear();

    }

    public void updatePassword(int userId, String newPassword) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return;
        }

        String query = "UPDATE user SET password=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private void handleDatePickerAction(ActionEvent event) {
        LocalDate selectedDate = newDateAddWeight.getValue();
        System.out.println("Selected date: " + selectedDate);
    }

    public void handleUpdateWeight() throws SQLException {
        int userId = User.currentUser.getId();
        LocalDate weightDate = newDateAddWeight.getValue();

        if (weightDate == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngày không được để trống!");
            return;
        }

        Instant instant = weightDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date utilDate = Date.from(instant);

        String dateWeight = newWeight.getText();
        if (dateWeight.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Cân nặng mới không được để trống!");
            return;
        }

        try {
            BigDecimal weightValue = new BigDecimal(dateWeight);

            if (weightValue.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập giá trị trọng số dương khác không.");
                return;
            }

            updateWeight(userId, weightValue);
            newDateAddWeight.setValue(null);
            newWeight.clear();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thay đổi cân nặng thành công!");
            User.currentUser.setCurrentWeight(weightValue);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập giá trị trọng lượng hợp lệ.");
        }
    }

    public void updateWeight(int userId, BigDecimal newWeight) throws SQLException {
        Connection con = JdbcUtils.getConn();
        if (con == null) {
            return;
        }

        String query = "UPDATE user SET current_weight=? WHERE id=?;";

        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setBigDecimal(1, newWeight);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        displayUsername();
        
        newDateAddWeight.setValue(LocalDate.now());
        
    }

}
