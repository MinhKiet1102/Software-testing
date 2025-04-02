package com.milkyway.healthmanagement;

import com.milkyway.pojo.User;
import com.milkyway.service.LoginService;
import java.net.URL;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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
    private TextField su_email;

    @FXML
    private ComboBox<String> su_gender;

    @FXML
    private TextField su_age;

    @FXML
    private TextField su_height;

    @FXML
    private TextField su_weight;

    private Alert alert;
    private final LoginService loginService = new LoginService();

    public void loginAccount() {
        try {
            String username = si_username.getText();
            String password = si_password.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            User user = loginService.login(username, password);
            if (user != null) {
                User.currentUser = user;
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đăng nhập thành công!");

                si_loginBtn.getScene().getWindow().hide();
                Parent root = FXMLLoader.load(getClass().getResource("home.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Health Mangement System");
                Image icon = new Image(getClass().getResourceAsStream("/com/milkyway/healthmanagement/image/image.jpg"));
                stage.getIcons().add(icon);
                stage.setResizable(false);
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Sai tên đăng nhập hoặc mật khẩu!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi đăng nhập!");
        }
    }

    public void registerAccount() {
        try {
            String username = su_username.getText();
            String password = su_password.getText();
            String email = su_email.getText();
            String gender = su_gender.getValue();
            String weight = su_weight.getText();
            String age = su_age.getText();
            String height = su_height.getText();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || gender == null || weight.isEmpty() || age.isEmpty() || height.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            if (password.length() < 8) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu phải có ít nhất 8 ký tự!");
                return;
            }

            User newUser = new User(0, username, password, email);
            boolean success = loginService.register(newUser, gender, weight, age, height);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tài khoản đã được tạo!");
                switchToLoginForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập đã tồn tại!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi đăng ký!");
        }
    }

    public void switchForm(ActionEvent event) {
        if (event.getSource() == si_createAccount) {
            signup_form.setVisible(true);
            login_form.setVisible(false);
        } else if (event.getSource() == su_alreadyhaveAccount) {
            switchToLoginForm();
        }
    }

    private void switchToLoginForm() {
        signup_form.setVisible(false);
        login_form.setVisible(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> genderList = FXCollections.observableArrayList("Nam", "Nữ", "Khác");
        su_gender.setItems(genderList);
    }

}
