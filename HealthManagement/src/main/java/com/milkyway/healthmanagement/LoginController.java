package com.milkyway.healthmanagement;

import com.milkyway.pojo.History;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import com.milkyway.services.HistoryService;
import com.milkyway.services.LoginService;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    private ActionEvent ev;
    private User createdAccount;

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
                User.setCurrentUser(user);
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đăng nhập thành công!");

                si_loginBtn.getScene().getWindow().hide();

                // Kiểm tra vai trò của người dùng và chuyển đến giao diện tương ứng
                Parent root;
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    // Người dùng có vai trò Admin, chuyển đến giao diện Admin
                    root = FXMLLoader.load(getClass().getResource("AdminDashboard.fxml"));
                } else {
                    // Người dùng có vai trò User, chuyển đến giao diện User thông thường
                    root = FXMLLoader.load(getClass().getResource("home.fxml"));
                }

                Stage stage = new Stage();
                stage.setTitle("Health Management System");
                Image icon = new Image(getClass().getResourceAsStream("/com/milkyway/healthmanagement/image/image.jpg"));
                stage.getIcons().add(icon);
                stage.setResizable(false);
                stage.setScene(new Scene(root));
                stage.initStyle(StageStyle.UNDECORATED);
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
            Date registration_date = new Date();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || gender == null || weight.isEmpty() || age.isEmpty() || height.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            if (password.length() < 8) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu phải có ít nhất 8 ký tự!");
                return;
            }

            BigDecimal weightValue;
            Integer ageValue;
            Integer heightValue;

            try {
                weightValue = new BigDecimal(weight);
                ageValue = Integer.parseInt(age);
                heightValue = Integer.parseInt(height);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đúng định dạng cho cân nặng, tuổi và chiều cao!");
                return;
            }

            User newUser = new User(0, username, password, email, gender, weightValue, ageValue, heightValue, registration_date);

            loginService.register(newUser);
            createdAccount = newUser;
            AccountCreatedSuccessfully(ev);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tài khoản đã được tạo!");
            switchToLoginForm();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi đăng ký!");
        }
    }

    @FXML
    public void AccountCreatedSuccessfully(ActionEvent ev) throws IOException, SQLException {
        if (createdAccount == null) {
            throw new IllegalStateException("Tài khoản chưa được tạo.");
        }

        try (Connection con = JdbcUtils.getConn()) {
            if (con == null) {
                return;
            }

            User user = new LoginService().getUserByUsername(createdAccount.getUsername());
            if (user == null) {
                throw new IllegalStateException("Không tìm thấy người dùng.");
            }

            History history = new History();
            history.setHistoryDate(new Date());
            history.setHistoryWeight(createdAccount.getCurrentWeight());
            history.setHistoryHeight(createdAccount.getHeight());
            history.setUserId(user);

            new HistoryService().save(history);
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
        ObservableList<String> genderList = FXCollections.observableArrayList("Nam", "Nữ");
        su_gender.setItems(genderList);
    }

}
