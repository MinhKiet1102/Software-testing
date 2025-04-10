package com.milkyway.healthmanagement;

import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class LoginController implements Initializable {

    @FXML
    private AnchorPane login_form;

    @FXML
    private AnchorPane main_form;

    @FXML
    private Hyperlink si_createAccount;

    @FXML
    private Button si_loginBtn;

    @FXML
    private PasswordField si_password;

    @FXML
    private TextField si_username;

    @FXML
    private AnchorPane signup_form;

    @FXML
    private Hyperlink su_alreadyhaveAccount;

    @FXML
    private PasswordField su_password;

    @FXML
    private Button su_signupBtn;

    @FXML
    private TextField su_username;

    @FXML
    private DatePicker su_dayOfBirth;

    @FXML
    private TextField su_email;

    @FXML
    private TextField su_fullname;

    @FXML
    private TextField su_phoneNumber;

    @FXML
    private ComboBox<String> su_gender;

    @FXML
    private TextField  su_age;

    @FXML
    private TextField  su_height;

    @FXML
    private TextField  su_weight;

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    private Alert alert;

    public void loginAccount() {
        String selectData = "SELECT id, username, password, email FROM user WHERE username = ? AND password = ?";

        try (Connection connect = JdbcUtils.getConn(); PreparedStatement prepare = connect.prepareStatement(selectData)) {

            if (si_username.getText().isEmpty() || si_password.getText().isEmpty()) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng điền vào tất cả các ô trống");
                alert.showAndWait();
                return;
            }

            prepare.setString(1, si_username.getText());
            prepare.setString(2, si_password.getText());
            ResultSet result = prepare.executeQuery();

            if (result.next()) {
                // Lưu trữ thông tin người dùng sau khi đăng nhập thành công
                User.currentUser = new User(result.getInt("id"), result.getString("username"), result.getString("password"), result.getString("email"));

                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Message");
                alert.setHeaderText(null);
                alert.setContentText("Đăng nhập thành công!");
                alert.showAndWait();

                // HIDE LOGIN FORM
                si_loginBtn.getScene().getWindow().hide();

                // SHOW MAIN FORM
                Parent root = FXMLLoader.load(getClass().getResource("base.fxml"));
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

            } else {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Tên người dùng/Mật khẩu không đúng");
                alert.showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Có lỗi xảy ra khi kết nối cơ sở dữ liệu.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Có lỗi xảy ra.");
            alert.showAndWait();
        }
    }

    public void registerAccount() throws SQLException {
        String insertData = "INSERT INTO `user` (username, password, email, gender, current_weight, age, height, registration_date)"
                + " VALUES(?,?,?,?,?,?,?,?)";

        try (Connection connect = JdbcUtils.getConn()) {

            if (su_username.getText().isEmpty() || su_password.getText().isEmpty()
                    || su_email.getText().isEmpty() || su_gender.getValue().isEmpty()
                    || su_weight.getText().isEmpty() || su_age.getText().isEmpty() || su_height.getText().isEmpty()) {

                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng điền đầy đủ thông tin!");
                alert.showAndWait();
                return;
            }

            // Kiểm tra xem username đã tồn tại chưa
            String checkUsername = "SELECT username FROM `user` WHERE username = ?";
            try (PreparedStatement checkStmt = connect.prepareStatement(checkUsername)) {
                checkStmt.setString(1, su_username.getText());
                ResultSet result = checkStmt.executeQuery();

                if (result.next()) {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText(null);
                    alert.setContentText("Tên đăng nhập '" + su_username.getText() + "' đã tồn tại.");
                    alert.showAndWait();
                    return;
                }
            }

            // Kiểm tra độ dài mật khẩu
            if (su_password.getText().length() < 8) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Mật khẩu phải có ít nhất 8 ký tự!");
                alert.showAndWait();
                return;
            }

           

            Date date = new Date();
            java.sql.Date curDate = new java.sql.Date(date.getTime());

            // Chèn dữ liệu mới vào database
            try (PreparedStatement prepare = connect.prepareStatement(insertData)) {
                prepare.setString(1, su_username.getText());
                prepare.setString(2, su_password.getText());
                prepare.setString(3, su_email.getText());
                prepare.setString(4, su_gender.getValue());
                prepare.setString(5, su_weight.getText());
                prepare.setString(6, su_age.getText());
                prepare.setString(7, su_height.getText());
                prepare.setString(8, String.valueOf(curDate));

                prepare.executeUpdate();
            }

            // Hiển thị thông báo thành công
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Tài khoản đã được tạo thành công!");
            alert.showAndWait();

            // Xóa dữ liệu trên form
            su_username.setText("");
            su_password.setText("");
            su_email.setText("");

            // Chuyển về màn hình đăng nhập
            signup_form.setVisible(false);
            login_form.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchForm(ActionEvent event) {
        if (event.getSource() == si_createAccount) {
            signup_form.setVisible(true);
            login_form.setVisible(false);
        } else if (event.getSource() == su_alreadyhaveAccount) {
            signup_form.setVisible(false);
            login_form.setVisible(true);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> genderList = FXCollections.observableArrayList("Nam", "Nữ", "Khác");
        su_gender.setItems(genderList);
    }

}
