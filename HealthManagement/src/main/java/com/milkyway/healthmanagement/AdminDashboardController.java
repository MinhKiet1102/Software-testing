package com.milkyway.healthmanagement;

import com.milkyway.healthmanagement.HomeController;
import com.milkyway.healthmanagement.SwitchSceneController;
import com.milkyway.pojo.Exercise;
import com.milkyway.pojo.JdbcUtils;
import com.milkyway.pojo.User;
import com.milkyway.services.ExerciseService;
import com.milkyway.services.LoginService;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;


public class AdminDashboardController extends SwitchSceneController implements Initializable {

    public void switchScene(String fxml) throws IOException {
        super.switchToScene(fxml);
    }
    
    @FXML
    private TabPane adminTabPane;
    
    @FXML
    private Label username;
    
    @FXML
    private Label lblTotalUsers;
    
    @FXML
    private Label lblTotalExercises;
    
    @FXML
    private Label lblTotalFoods;
    
    @FXML
    private Label lblNewUsers;
    
    @FXML
    private Button btnClose;
    
    @FXML
    private Button btnMinimize;
    
    @FXML
    private BarChart<String, Number> userRegistrationChart;
    
    @FXML
    private TableView<User> tblNewUsers;
    
    @FXML
    private TableColumn<User, String> colUsername;
    
    @FXML
    private TableColumn<User, String> colEmail;
    
    @FXML
    private TableColumn<User, Date> colRegDate;
  
    @FXML
    private TextField txtSearchUser;
    
    @FXML
    private TableView<User> tblUsers;
    
    @FXML
    private TableColumn<User, Integer> colUserID;
    
    @FXML
    private TableColumn<User, String> colUserUsername;
    
    @FXML
    private TableColumn<User, String> colUserEmail;
    
    @FXML
    private TableColumn<User, String> colUserGender;
    
    @FXML
    private TableColumn<User, Integer> colUserAge;
    
    @FXML
    private TableColumn<User, Integer> colUserHeight;
    
    @FXML
    private TableColumn<User, Double> colUserWeight;
    
    @FXML
    private TableColumn<User, String> colUserRole;
    
    @FXML
    private TableColumn<User, Void> colUserActions;

    @FXML
    private TextField txtSearchExercise;
    
    @FXML
    private TableView<Exercise> tblExercises;
    
    @FXML
    private TableColumn<Exercise, Integer> colExerciseID;
    
    @FXML
    private TableColumn<Exercise, String> colExerciseName;
    
    @FXML
    private TableColumn<Exercise, Double> colExerciseCalories;
    
    @FXML
    private TableColumn<Exercise, String> colExerciseImage;
    
    @FXML
    private TableColumn<Exercise, Void> colExerciseActions;

    @FXML
    private PieChart chartPopularExercises;
    
    @FXML
    private BarChart<String, Number> chartExerciseTypes;
    
    @FXML
    private PieChart chartUserAgeDistribution;
    
    @FXML
    private PieChart chartUserGenderDistribution;
    
    private LoginService loginService;
    private ExerciseService exerciseService;
    
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<User> newUsersList = FXCollections.observableArrayList();
    private ObservableList<Exercise> exerciseList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            loginService = new LoginService();
            exerciseService = new ExerciseService();
            
            // Hiển thị tên người dùng hiện tại (Admin)
            if (User.getCurrentUser() != null) {
                username.setText(User.getCurrentUser().getUsername());
            }
            
            // Thiết lập các nút điều khiển cửa sổ
            setupWindowControls();
            
            // Thiết lập tab mặc định
            adminTabPane.getSelectionModel().select(0);
            
            // Tải dữ liệu tổng quan
            loadDashboardData();
            
            // Thiết lập các bảng
            setupUserTable();
            setupExerciseTable();
            
            // Tải dữ liệu cho các bảng
            loadUserData();
            loadExerciseData();
            
            // Tải dữ liệu cho các biểu đồ
            loadUserCharts();
            loadExerciseCharts();
            
        } catch (Exception ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khởi tạo giao diện Admin: " + ex.getMessage());
        }
    }
    
    /**
     * Thiết lập các nút đóng và thu nhỏ cửa sổ
     */
    private void setupWindowControls() {
        btnClose.setOnAction(e -> {
            Stage stage = (Stage) btnClose.getScene().getWindow();
            stage.close();
        });
        
        btnMinimize.setOnAction(e -> {
            Stage stage = (Stage) btnMinimize.getScene().getWindow();
            stage.setIconified(true);
        });
    }
    
    /**
     * Tải dữ liệu tổng quan cho dashboard
     */
    private void loadDashboardData() {
        try {
            Connection conn = JdbcUtils.getConn();
            
            // Đếm tổng số người dùng
            String sqlCountUsers = "SELECT COUNT(*) FROM user";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCountUsers)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    lblTotalUsers.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // Đếm tổng số bài tập
            String sqlCountExercises = "SELECT COUNT(*) FROM exercise";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCountExercises)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    lblTotalExercises.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // Đếm tổng số thực phẩm
            String sqlCountFoods = "SELECT COUNT(*) FROM food";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCountFoods)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    lblTotalFoods.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // Đếm người dùng mới trong 30 ngày qua
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            String sqlNewUsers = "SELECT COUNT(*) FROM user WHERE registration_date >= ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlNewUsers)) {
                stmt.setDate(1, Date.valueOf(thirtyDaysAgo));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    lblNewUsers.setText(String.valueOf(rs.getInt(1)));
                }
            }
            
            // Tải dữ liệu người dùng mới cho bảng
            loadNewUsersTable(conn, thirtyDaysAgo);
            
            // Tạo biểu đồ đăng ký người dùng theo thời gian
            createUserRegistrationChart(conn);
            
        } catch (SQLException ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu tổng quan: " + ex.getMessage());
        }
    }
    
    /**
     * Tải dữ liệu người dùng mới cho bảng
     */
    private void loadNewUsersTable(Connection conn, LocalDate thirtyDaysAgo) throws SQLException {
        newUsersList.clear();
        
        String sql = "SELECT * FROM user WHERE registration_date >= ? ORDER BY registration_date DESC LIMIT 20";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(thirtyDaysAgo));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("gender"),
                    rs.getBigDecimal("current_weight"),
                    rs.getInt("age"),
                    rs.getInt("height"),
                    rs.getDate("registration_date")
                );
                user.setRole(rs.getString("role"));
                newUsersList.add(user);
            }
        }
        
        // Thiết lập các cột cho bảng người dùng mới
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRegDate.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        
        // Định dạng hiển thị ngày
        colRegDate.setCellFactory(column -> {
            return new TableCell<User, Date>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item.toLocalDate()));
                    }
                }
            };
        });
        
        tblNewUsers.setItems(newUsersList);
    }
    
    /**
     * Tạo biểu đồ đăng ký người dùng theo thời gian
     */
    private void createUserRegistrationChart(Connection conn) throws SQLException {
        // Xóa dữ liệu cũ
        userRegistrationChart.getData().clear();
        
        // Tạo series mới cho biểu đồ
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số lượng người dùng đăng ký");
        
        // Lấy dữ liệu đăng ký người dùng theo tháng trong 6 tháng gần nhất
        String sql = "SELECT YEAR(registration_date) as year, MONTH(registration_date) as month, COUNT(*) as count " +
                     "FROM user " +
                     "WHERE registration_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                     "GROUP BY YEAR(registration_date), MONTH(registration_date) " +
                     "ORDER BY year, month";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int year = rs.getInt("year");
                int month = rs.getInt("month");
                int count = rs.getInt("count");
                
                String monthYear = String.format("%02d/%d", month, year);
                series.getData().add(new XYChart.Data<>(monthYear, count));
            }
        }
        
        userRegistrationChart.getData().add(series);
    }
    
    /**
     * Thiết lập bảng hiển thị danh sách người dùng
     */
    private void setupUserTable() {
        colUserID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colUserAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colUserHeight.setCellValueFactory(new PropertyValueFactory<>("height"));
        colUserWeight.setCellValueFactory(new PropertyValueFactory<>("currentWeight"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        // Tạo cột hành động (sửa, xóa)
        colUserActions.setCellFactory(createActionButtonCellFactory("user"));
    }
    
    /**
     * Thiết lập bảng hiển thị danh sách bài tập
     */
    private void setupExerciseTable() {
        colExerciseID.setCellValueFactory(new PropertyValueFactory<>("idExercise"));
        colExerciseName.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        colExerciseCalories.setCellValueFactory(new PropertyValueFactory<>("caloriesBurnedPerMin"));
        colExerciseImage.setCellValueFactory(new PropertyValueFactory<>("imageExercise"));
        
        // Tạo cột hành động (sửa, xóa)
        colExerciseActions.setCellFactory(createActionButtonCellFactory("exercise"));
    }
    
    /**
     * Tải dữ liệu người dùng
     */
    private void loadUserData() {
        try {
            Connection conn = JdbcUtils.getConn();
            userList.clear();
            
            String sql = "SELECT * FROM user ORDER BY id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("gender"),
                        rs.getBigDecimal("current_weight"),
                        rs.getInt("age"),
                        rs.getInt("height"),
                        rs.getDate("registration_date")
                    );
                    user.setRole(rs.getString("role"));
                    userList.add(user);
                }
            }
            
            tblUsers.setItems(userList);
            
        } catch (SQLException ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu người dùng: " + ex.getMessage());
        }
    }
    
    /**
     * Tải dữ liệu bài tập
     */
    private void loadExerciseData() {
        try {
            exerciseList.clear();
            List<Exercise> exercises = exerciseService.getExercise(null);
            exerciseList.addAll(exercises);
            tblExercises.setItems(exerciseList);
            
        } catch (SQLException ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu bài tập: " + ex.getMessage());
        }
    }
    
    /**
     * Tạo các nút hành động cho bảng
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Callback<TableColumn<T, Void>, TableCell<T, Void>> createActionButtonCellFactory(String type) {
        return new Callback<TableColumn<T, Void>, TableCell<T, Void>>() {
            @Override
            public TableCell<T, Void> call(TableColumn<T, Void> param) {
                if ("user".equals(type)) {
                    return new TableCell<T, Void>() {
                        private final Button editButton = new Button("Sửa");
                        private final Button deleteButton = new Button("Xóa");
                        private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5, editButton, deleteButton);

                        {
                            editButton.getStyleClass().add("small-button");
                            deleteButton.getStyleClass().add("small-button");
                            
                            editButton.setOnAction(event -> {
                                User user = (User) getTableView().getItems().get(getIndex());
                                editUser(user);
                            });
                            
                            deleteButton.setOnAction(event -> {
                                User user = (User) getTableView().getItems().get(getIndex());
                                deleteUser(user);
                            });
                        }

                        @Override
                        protected void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(empty ? null : pane);
                        }
                    };
                } else if ("exercise".equals(type)) {
                    return new TableCell<T, Void>() {
                        private final Button editButton = new Button("Sửa");
                        private final Button deleteButton = new Button("Xóa");
                        private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5, editButton, deleteButton);

                        {
                            editButton.getStyleClass().add("small-button");
                            deleteButton.getStyleClass().add("small-button");
                            
                            editButton.setOnAction(event -> {
                                Exercise exercise = (Exercise) getTableView().getItems().get(getIndex());
                                editExercise(exercise);
                            });
                            
                            deleteButton.setOnAction(event -> {
                                Exercise exercise = (Exercise) getTableView().getItems().get(getIndex());
                                deleteExercise(exercise);
                            });
                        }

                        @Override
                        protected void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(empty ? null : pane);
                        }
                    };
                }
                return null;
            }
        };
    }
    
    /**
     * Chỉnh sửa thông tin người dùng
     */
    private void editUser(User user) {
        try {
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Chỉnh sửa người dùng");
            dialog.setHeaderText("Chỉnh sửa thông tin cho người dùng: " + user.getUsername());

            // Tạo các nút OK và Cancel
            ButtonType buttonTypeOk = new ButtonType("Lưu", ButtonType.OK.getButtonData());
            ButtonType buttonTypeCancel = new ButtonType("Hủy", ButtonType.CANCEL.getButtonData());
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

            // Tạo lưới giao diện
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            TextField txtUsername = new TextField(user.getUsername());
            TextField txtEmail = new TextField(user.getEmail());
            TextField txtGender = new TextField(user.getGender() != null ? user.getGender() : "");
            TextField txtAge = new TextField(user.getAge() != null ? String.valueOf(user.getAge()) : "");
            TextField txtHeight = new TextField(user.getHeight() != null ? String.valueOf(user.getHeight()) : "");
            TextField txtWeight = new TextField(user.getCurrentWeight() != null ? String.valueOf(user.getCurrentWeight()) : "");
            
            javafx.scene.control.ComboBox<String> cbRole = new javafx.scene.control.ComboBox<>();
            cbRole.getItems().addAll("USER", "ADMIN");
            cbRole.setValue(user.getRole() != null ? user.getRole() : "USER");

            grid.add(new Label("Tên đăng nhập:"), 0, 0);
            grid.add(txtUsername, 1, 0);
            grid.add(new Label("Email:"), 0, 1);
            grid.add(txtEmail, 1, 1);
            grid.add(new Label("Giới tính:"), 0, 2);
            grid.add(txtGender, 1, 2);
            grid.add(new Label("Tuổi:"), 0, 3);
            grid.add(txtAge, 1, 3);
            grid.add(new Label("Chiều cao:"), 0, 4);
            grid.add(txtHeight, 1, 4);
            grid.add(new Label("Cân nặng:"), 0, 5);
            grid.add(txtWeight, 1, 5);
            grid.add(new Label("Vai trò:"), 0, 6);
            grid.add(cbRole, 1, 6);

            dialog.getDialogPane().setContent(grid);
            Platform.runLater(() -> txtUsername.requestFocus());

            // Chuyển đổi kết quả từ dialog thành đối tượng User
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOk) {
                    try {
                        user.setUsername(txtUsername.getText());
                        user.setEmail(txtEmail.getText());
                        user.setGender(txtGender.getText());
                        user.setAge(Integer.parseInt(txtAge.getText()));
                        user.setHeight(Integer.parseInt(txtHeight.getText()));
                        user.setCurrentWeight(new java.math.BigDecimal(txtWeight.getText()));
                        user.setRole(cbRole.getValue());
                        return user;
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đúng định dạng số cho tuổi, chiều cao và cân nặng.");
                        return null;
                    }
                }
                return null;
            });

            Optional<User> result = dialog.showAndWait();
            
            result.ifPresent(updatedUser -> {
                try {
                    loginService.register(updatedUser); // Sử dụng method register để cập nhật thông tin
                    loadUserData();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin người dùng thành công.");
                } catch (SQLException e) {
                    Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, e);
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật thông tin người dùng: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, e);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form chỉnh sửa: " + e.getMessage());
        }
    }
    
    /**
     * Xóa người dùng
     */
    private void deleteUser(User user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận");
        confirmation.setHeaderText("Xóa người dùng");
        confirmation.setContentText("Bạn có chắc muốn xóa người dùng " + user.getUsername() + "?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Connection conn = JdbcUtils.getConn();
                String sql = "DELETE FROM user WHERE id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, user.getId());
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        userList.remove(user);
                        tblUsers.refresh();
                        loadDashboardData(); // Refresh dashboard counts
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa người dùng thành công");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa người dùng");
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa người dùng: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Thêm người dùng mới
     */
    @FXML
    private void addNewUser(ActionEvent event) {
        try {
            User newUser = new User();
            newUser.setRegistrationDate(new java.util.Date()); // Ngày hiện tại
            
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Thêm người dùng mới");
            dialog.setHeaderText("Nhập thông tin người dùng mới");

            ButtonType buttonTypeOk = new ButtonType("Lưu", ButtonType.OK.getButtonData());
            ButtonType buttonTypeCancel = new ButtonType("Hủy", ButtonType.CANCEL.getButtonData());
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            TextField txtUsername = new TextField();
            TextField txtPassword = new TextField();
            TextField txtEmail = new TextField();
            
            // Sử dụng ComboBox cho giới tính với hai giá trị Nam và Nữ
            javafx.scene.control.ComboBox<String> cbGender = new javafx.scene.control.ComboBox<>();
            cbGender.getItems().addAll("Nam", "Nữ");
            cbGender.setValue("Nam"); // Giá trị mặc định
            
            TextField txtAge = new TextField();
            TextField txtHeight = new TextField();
            TextField txtWeight = new TextField();
            
            javafx.scene.control.ComboBox<String> cbRole = new javafx.scene.control.ComboBox<>();
            cbRole.getItems().addAll("USER", "ADMIN");
            cbRole.setValue("USER");

            grid.add(new Label("Tên đăng nhập:"), 0, 0);
            grid.add(txtUsername, 1, 0);
            grid.add(new Label("Mật khẩu:"), 0, 1);
            grid.add(txtPassword, 1, 1);
            grid.add(new Label("Email:"), 0, 2);
            grid.add(txtEmail, 1, 2);
            grid.add(new Label("Giới tính:"), 0, 3);
            grid.add(cbGender, 1, 3);
            grid.add(new Label("Tuổi:"), 0, 4);
            grid.add(txtAge, 1, 4);
            grid.add(new Label("Chiều cao:"), 0, 5);
            grid.add(txtHeight, 1, 5);
            grid.add(new Label("Cân nặng:"), 0, 6);
            grid.add(txtWeight, 1, 6);
            grid.add(new Label("Vai trò:"), 0, 7);
            grid.add(cbRole, 1, 7);

            dialog.getDialogPane().setContent(grid);
            Platform.runLater(() -> txtUsername.requestFocus());

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOk) {
                    try {
                        // Kiểm tra các trường bắt buộc
                        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty() || txtEmail.getText().isEmpty()) {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập, mật khẩu và email không được để trống.");
                            return null;
                        }
                        
                        newUser.setUsername(txtUsername.getText());
                        newUser.setPassword(txtPassword.getText());
                        newUser.setEmail(txtEmail.getText());
                        newUser.setGender(cbGender.getValue());
                        
                        if (!txtAge.getText().isEmpty()) {
                            newUser.setAge(Integer.parseInt(txtAge.getText()));
                        }
                        
                        if (!txtHeight.getText().isEmpty()) {
                            newUser.setHeight(Integer.parseInt(txtHeight.getText()));
                        }
                        
                        if (!txtWeight.getText().isEmpty()) {
                            newUser.setCurrentWeight(new java.math.BigDecimal(txtWeight.getText()));
                        }
                        
                        newUser.setRole(cbRole.getValue());
                        
                        return newUser;
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đúng định dạng số cho tuổi, chiều cao và cân nặng.");
                        return null;
                    }
                }
                return null;
            });

            Optional<User> result = dialog.showAndWait();
            
            result.ifPresent(user -> {
                try {
                    loginService.register(user);
                    loadUserData();
                    loadDashboardData(); // Refresh dashboard counts
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm người dùng mới thành công.");
                } catch (SQLException e) {
                    Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, e);
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm người dùng mới: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, e);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form thêm người dùng: " + e.getMessage());
        }
    }
    
    /**
     * Tìm kiếm người dùng
     */
    @FXML
    private void searchUser(ActionEvent event) {
        String searchText = txtSearchUser.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            tblUsers.setItems(userList);
            return;
        }
        
        ObservableList<User> filteredList = FXCollections.observableArrayList();
        
        for (User user : userList) {
            if ((user.getUsername() != null && user.getUsername().toLowerCase().contains(searchText)) || 
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText))) {
                filteredList.add(user);
            }
        }
        
        tblUsers.setItems(filteredList);
    }
    
    /**
     * Làm mới danh sách người dùng
     */
    @FXML
    private void refreshUserList(ActionEvent event) {
        txtSearchUser.clear();
        loadUserData();
    }
    
    /**
     * Chỉnh sửa bài tập
     */
    private void editExercise(Exercise exercise) {
        try {
            Dialog<Exercise> dialog = new Dialog<>();
            dialog.setTitle("Chỉnh sửa bài tập");
            dialog.setHeaderText("Chỉnh sửa thông tin cho bài tập: " + exercise.getExerciseName());

            ButtonType buttonTypeOk = new ButtonType("Lưu", ButtonType.OK.getButtonData());
            ButtonType buttonTypeCancel = new ButtonType("Hủy", ButtonType.CANCEL.getButtonData());
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            TextField txtExerciseName = new TextField(exercise.getExerciseName());
            TextField txtCalories = new TextField(String.valueOf(exercise.getCaloriesBurnedPerMin()));
            
            javafx.scene.control.ComboBox<String> cbType = new javafx.scene.control.ComboBox<>();
            cbType.getItems().addAll("Cardio", "Strength", "Flexibility", "Balance");
            cbType.setValue(exercise.getExerciseType() != null ? exercise.getExerciseType() : "Strength");

            // Thêm control để hiển thị hình ảnh hiện tại
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
            imageView.setFitHeight(150);
            imageView.setFitWidth(150);
            imageView.setPreserveRatio(true);
            
            // Hiển thị hình ảnh hiện tại nếu có
            if (exercise.getImageExercise() != null && !exercise.getImageExercise().isEmpty()) {
                try {
                    java.io.File imageFile = new java.io.File(exercise.getImageExercise());
                    if (imageFile.exists()) {
                        imageView.setImage(new javafx.scene.image.Image(imageFile.toURI().toString()));
                    }
                } catch (Exception e) {
                    Logger.getLogger(AdminDashboardController.class.getName()).log(Level.WARNING, "Không thể tải hình ảnh", e);
                }
            }
            
            // Nút để chọn hình ảnh mới
            Button btnChooseImage = new Button("Chọn hình ảnh");
            
            // Biến để lưu trữ đường dẫn hình ảnh mới
            final java.util.concurrent.atomic.AtomicReference<String> newImagePath = new java.util.concurrent.atomic.AtomicReference<>(exercise.getImageExercise());
            
            btnChooseImage.setOnAction(e -> {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("Chọn hình ảnh");
                fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Hình ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif")
                );
                
                java.io.File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
                if (selectedFile != null) {
                    try {
                        // Tạo thư mục images nếu chưa tồn tại
                        java.io.File imagesDir = new java.io.File("src/main/resources/com/milkyway/healthmanagement/images");
                        if (!imagesDir.exists()) {
                            imagesDir.mkdirs();
                        }
                        
                        // Tạo tên file mới để tránh trùng lặp
                        String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                        java.io.File destFile = new java.io.File(imagesDir, fileName);
                        
                        // Copy file hình ảnh đến thư mục images
                        java.nio.file.Files.copy(
                            selectedFile.toPath(),
                            destFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                        );
                        
                        // Cập nhật đường dẫn hình ảnh
                        newImagePath.set("/com/milkyway/healthmanagement/images/" + fileName);
                        
                        // Hiển thị hình ảnh mới
                        imageView.setImage(new javafx.scene.image.Image(destFile.toURI().toString()));
                        
                    } catch (Exception ex) {
                        Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lên hình ảnh: " + ex.getMessage());
                    }
                }
            });
            
            javafx.scene.layout.VBox imageBox = new javafx.scene.layout.VBox(10, imageView, btnChooseImage);
            imageBox.setAlignment(javafx.geometry.Pos.CENTER);

            grid.add(new Label("Tên bài tập:"), 0, 0);
            grid.add(txtExerciseName, 1, 0);
            grid.add(new Label("Calo tiêu thụ/phút:"), 0, 1);
            grid.add(txtCalories, 1, 1);
            grid.add(new Label("Loại bài tập:"), 0, 2);
            grid.add(cbType, 1, 2);
            grid.add(new Label("Hình ảnh:"), 0, 3);
            grid.add(imageBox, 1, 3);

            dialog.getDialogPane().setContent(grid);
            Platform.runLater(() -> txtExerciseName.requestFocus());

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOk) {
                    try {
                        if (txtExerciseName.getText().isEmpty()) {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên bài tập không được để trống.");
                            return null;
                        }
                        
                        exercise.setExerciseName(txtExerciseName.getText());
                        exercise.setCaloriesBurnedPerMin(Double.parseDouble(txtCalories.getText()));
                        exercise.setExerciseType(cbType.getValue());
                        exercise.setImageExercise(newImagePath.get());
                        
                        return exercise;
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đúng định dạng số cho calo.");
                        return null;
                    }
                }
                return null;
            });

            Optional<Exercise> result = dialog.showAndWait();
            
            result.ifPresent(updatedExercise -> {
                try {
                    Connection conn = JdbcUtils.getConn();
                    String sql = "UPDATE exercise SET exerciseName = ?, caloriesBurnedPerMin = ?, imageExercise = ? WHERE idExercise = ?";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, updatedExercise.getExerciseName());
                        stmt.setDouble(2, updatedExercise.getCaloriesBurnedPerMin());
                        stmt.setString(3, updatedExercise.getImageExercise());
                        stmt.setInt(4, updatedExercise.getIdExercise());
                        
                        int rowsAffected = stmt.executeUpdate();
                        
                        if (rowsAffected > 0) {
                            loadExerciseData();
                            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật bài tập thành công.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật bài tập.");
                        }
                    }
                } catch (SQLException e) {
                    Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, e);
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật bài tập: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, e);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form chỉnh sửa: " + e.getMessage());
        }
    }
    
    /**
     * Xóa bài tập
     */
    private void deleteExercise(Exercise exercise) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận");
        confirmation.setHeaderText("Xóa bài tập");
        confirmation.setContentText("Bạn có chắc muốn xóa bài tập " + exercise.getExerciseName() + "?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Connection conn = JdbcUtils.getConn();
                String sql = "DELETE FROM exercise WHERE idExercise = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, exercise.getIdExercise());
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        exerciseList.remove(exercise);
                        tblExercises.refresh();
                        loadDashboardData(); // Refresh dashboard counts
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa bài tập thành công");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa bài tập");
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa bài tập: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Thêm bài tập mới
     */
    @FXML
    private void addNewExercise(ActionEvent event) {
        try {
            Exercise exercise = new Exercise();
            
            Dialog<Exercise> dialog = new Dialog<>();
            dialog.setTitle("Thêm bài tập mới");
            dialog.setHeaderText("Nhập thông tin bài tập mới");

            ButtonType buttonTypeOk = new ButtonType("Lưu", ButtonType.OK.getButtonData());
            ButtonType buttonTypeCancel = new ButtonType("Hủy", ButtonType.CANCEL.getButtonData());
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            TextField txtExerciseName = new TextField();
            TextField txtCalories = new TextField();

            // Thêm controls để upload hình ảnh
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
            imageView.setFitHeight(115);
            imageView.setFitWidth(115);
            imageView.setPreserveRatio(true);
            
            // Biến lưu trữ đường dẫn hình ảnh
            final java.util.concurrent.atomic.AtomicReference<String> imagePath = new java.util.concurrent.atomic.AtomicReference<>(null);
            final java.util.concurrent.atomic.AtomicReference<java.io.File> selectedImageFile = new java.util.concurrent.atomic.AtomicReference<>(null);
            
            // Nút chọn hình ảnh
            Button btnChooseImage = new Button("Chọn hình ảnh");
            btnChooseImage.setOnAction(e -> {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("Chọn hình ảnh cho bài tập");
                fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Hình ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif")
                );
                
                java.io.File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
                if (selectedFile != null) {
                    try {
                        selectedImageFile.set(selectedFile);
                        
                        // Hiển thị hình ảnh xem trước có kích thước 115x115
                        javafx.scene.image.Image originalImage = new javafx.scene.image.Image(selectedFile.toURI().toString(), 115, 115, true, true);
                        imageView.setImage(originalImage);
                        
                    } catch (Exception ex) {
                        Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải hình ảnh: " + ex.getMessage());
                    }
                }
            });
            
            javafx.scene.layout.VBox imageBox = new javafx.scene.layout.VBox(10, imageView, btnChooseImage);
            imageBox.setAlignment(javafx.geometry.Pos.CENTER);

            grid.add(new Label("Tên bài tập:"), 0, 0);
            grid.add(txtExerciseName, 1, 0);
            grid.add(new Label("Calo tiêu thụ/phút:"), 0, 1);
            grid.add(txtCalories, 1, 1);
            grid.add(new Label("Hình ảnh:"), 0, 2);
            grid.add(imageBox, 1, 2);

            dialog.getDialogPane().setContent(grid);
            Platform.runLater(() -> txtExerciseName.requestFocus());

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOk) {
                    try {
                        // Kiểm tra các trường thông tin bắt buộc
                        if (txtExerciseName.getText().isEmpty() || txtCalories.getText().isEmpty()) {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên bài tập và calo tiêu thụ không được để trống.");
                            return null;
                        }
                        
                        exercise.setExerciseName(txtExerciseName.getText());
                        exercise.setCaloriesBurnedPerMin(Double.parseDouble(txtCalories.getText()));
                        
                        // Xử lý hình ảnh nếu có chọn hình
                        if (selectedImageFile.get() != null) {
                            try {
                                // Tạo thư mục images nếu chưa tồn tại
                                java.io.File imagesDir = new java.io.File("src/main/resources/com/milkyway/healthmanagement/image");
                                if (!imagesDir.exists()) {
                                    imagesDir.mkdirs();
                                }
                                
                                // Tạo tên file dựa trên tên bài tập
                                String exerciseName = txtExerciseName.getText().trim();
                                // Xử lý tên file: loại bỏ ký tự đặc biệt, khoảng trắng thay bằng dấu gạch dưới
                                String fileName = exerciseName.replaceAll("[^a-zA-Z0-9\\s]", "")
                                                           .replaceAll("\\s+", "_") + ".jpg";
                                
                                java.io.File destFile = new java.io.File(imagesDir, fileName);
                                
                                // Sao chép file hình ảnh vào thư mục đích
                                java.nio.file.Files.copy(
                                    selectedImageFile.get().toPath(),
                                    destFile.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                                );
                                
                                // Lưu đường dẫn tương đối để lưu vào database
                                imagePath.set(fileName);
                                exercise.setImageExercise(imagePath.get());
                                
                            } catch (Exception ex) {
                                Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
                                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xử lý hình ảnh: " + ex.getMessage());
                                return null;
                            }
                        }
                        
                        // Lưu thông tin người dùng đã tạo bài tập
                        exercise.setUserId(User.getCurrentUser());
                        
                        return exercise;
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đúng định dạng số cho calo.");
                        return null;
                    }
                }
                return null;
            });

            Optional<Exercise> result = dialog.showAndWait();
            
            result.ifPresent(newExercise -> {
                try {
                    // Lưu bài tập mới vào cơ sở dữ liệu
                    Connection conn = JdbcUtils.getConn();
                    String sql = "INSERT INTO exercise (exerciseName, caloriesBurnedPerMin, imageExercise, userId) VALUES (?, ?, ?, ?)";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, newExercise.getExerciseName());
                        stmt.setDouble(2, newExercise.getCaloriesBurnedPerMin());
                        stmt.setString(3, newExercise.getImageExercise());
                        
                        // Lưu ID của người dùng tạo bài tập
                        if (newExercise.getUserId() != null) {
                            stmt.setInt(4, newExercise.getUserId().getId());
                        } else {
                            stmt.setNull(4, java.sql.Types.INTEGER);
                        }
                        
                        int rowsAffected = stmt.executeUpdate();
                        
                        if (rowsAffected > 0) {
                            // Lấy ID được sinh tự động
                            ResultSet generatedKeys = stmt.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                newExercise.setIdExercise(generatedKeys.getInt(1));
                                exerciseList.add(newExercise);
                                tblExercises.refresh();
                                loadDashboardData(); // Refresh dashboard counts
                            }
                            
                            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm bài tập mới thành công.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm bài tập mới.");
                        }
                    }
                } catch (SQLException e) {
                    Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, e);
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm bài tập mới: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, e);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form thêm bài tập: " + e.getMessage());
        }
    }
    
    /**
     * Tìm kiếm bài tập
     */
    @FXML
    private void searchExercise(ActionEvent event) {
        String searchText = txtSearchExercise.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            tblExercises.setItems(exerciseList);
            return;
        }
        
        ObservableList<Exercise> filteredList = FXCollections.observableArrayList();
        
        for (Exercise exercise : exerciseList) {
            if (exercise.getExerciseName() != null && exercise.getExerciseName().toLowerCase().contains(searchText)) {
                filteredList.add(exercise);
            }
        }
        
        tblExercises.setItems(filteredList);
    }
    
    /**
     * Làm mới danh sách bài tập
     */
    @FXML
    private void refreshExerciseList(ActionEvent event) {
        txtSearchExercise.clear();
        loadExerciseData();
    }
    
    /**
     * Tạo biểu đồ thống kê người dùng
     */
    private void loadUserCharts() {
        try {
            Connection conn = JdbcUtils.getConn();
            
            // Biểu đồ phân bố người dùng theo độ tuổi
            chartUserAgeDistribution.getData().clear();
            Map<String, Integer> ageGroups = new HashMap<>();
            ageGroups.put("< 18", 0);
            ageGroups.put("18-25", 0);
            ageGroups.put("26-35", 0);
            ageGroups.put("36-50", 0);
            ageGroups.put("> 50", 0);
            
            for (User user : userList) {
                if (user.getAge() != null) {
                    int age = user.getAge();
                    if (age < 18) {
                        ageGroups.put("< 18", ageGroups.get("< 18") + 1);
                    } else if (age <= 25) {
                        ageGroups.put("18-25", ageGroups.get("18-25") + 1);
                    } else if (age <= 35) {
                        ageGroups.put("26-35", ageGroups.get("26-35") + 1);
                    } else if (age <= 50) {
                        ageGroups.put("36-50", ageGroups.get("36-50") + 1);
                    } else {
                        ageGroups.put("> 50", ageGroups.get("> 50") + 1);
                    }
                }
            }
            
            for (Map.Entry<String, Integer> entry : ageGroups.entrySet()) {
                PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
                chartUserAgeDistribution.getData().add(slice);
            }
            
            // Biểu đồ phân bố người dùng theo giới tính
            chartUserGenderDistribution.getData().clear();
            Map<String, Integer> genderGroups = new HashMap<>();
            genderGroups.put("Nam", 0);
            genderGroups.put("Nữ", 0);
            genderGroups.put("Khác", 0);
            
            for (User user : userList) {
                String gender = user.getGender();
                if (gender == null || gender.isEmpty()) {
                    genderGroups.put("Khác", genderGroups.get("Khác") + 1);
                } else if ("Nam".equalsIgnoreCase(gender)) {
                    genderGroups.put("Nam", genderGroups.get("Nam") + 1);
                } else if ("Nữ".equalsIgnoreCase(gender)) {
                    genderGroups.put("Nữ", genderGroups.get("Nữ") + 1);
                } else {
                    genderGroups.put("Khác", genderGroups.get("Khác") + 1);
                }
            }
            
            for (Map.Entry<String, Integer> entry : genderGroups.entrySet()) {
                PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
                chartUserGenderDistribution.getData().add(slice);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Tạo biểu đồ thống kê bài tập
     */
    private void loadExerciseCharts() {
        try {
            // Biểu đồ bài tập phổ biến nhất
            chartPopularExercises.getData().clear();
            
            Connection conn = JdbcUtils.getConn();
            String sql = "SELECT e.exerciseName, COUNT(el.idExLog) as count " +
                         "FROM exercise e " +
                         "JOIN exerciselog el ON e.idExercise = el.exerciseId " +
                         "GROUP BY e.idExercise, e.exerciseName " +
                         "ORDER BY count DESC " +
                         "LIMIT 5";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String exerciseName = rs.getString("exerciseName");
                    int count = rs.getInt("count");
                    
                    PieChart.Data slice = new PieChart.Data(exerciseName + " (" + count + ")", count);
                    chartPopularExercises.getData().add(slice);
                }
            }
            
            // Biểu đồ theo loại bài tập
            chartExerciseTypes.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Số lượng bài tập");
            
            // Giả lập dữ liệu cho biểu đồ (vì bảng exercise không có cột exerciseType)
            Map<String, Integer> exerciseTypes = new HashMap<>();
            exerciseTypes.put("Cardio", 0);
            exerciseTypes.put("Strength", 0);
            exerciseTypes.put("Flexibility", 0);
            exerciseTypes.put("Balance", 0);
            
            // Phân loại đơn giản dựa trên tên bài tập
            for (Exercise exercise : exerciseList) {
                String name = exercise.getExerciseName().toLowerCase();
                if (name.contains("chạy") || name.contains("đạp") || name.contains("bơi") || 
                    name.contains("run") || name.contains("swim") || name.contains("cardio")) {
                    exerciseTypes.put("Cardio", exerciseTypes.get("Cardio") + 1);
                } else if (name.contains("tạ") || name.contains("nâng") || name.contains("push") || 
                         name.contains("pull") || name.contains("weight") || name.contains("strength")) {
                    exerciseTypes.put("Strength", exerciseTypes.get("Strength") + 1);
                } else if (name.contains("yoga") || name.contains("stretch") || name.contains("giãn")) {
                    exerciseTypes.put("Flexibility", exerciseTypes.get("Flexibility") + 1);
                } else {
                    exerciseTypes.put("Balance", exerciseTypes.get("Balance") + 1);
                }
            }
            
            for (Map.Entry<String, Integer> entry : exerciseTypes.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            
            chartExerciseTypes.getData().add(series);
            
        } catch (SQLException ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    /**
     * Làm mới dữ liệu cho các biểu đồ thống kê
     */
    @FXML
    private void refreshReportData(ActionEvent event) {
        try {
            loadDashboardData();
            loadUserCharts();
            loadExerciseCharts();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã làm mới dữ liệu báo cáo thành công.");
        } catch (Exception ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể làm mới dữ liệu: " + ex.getMessage());
        }
    }
   

    @FXML
    private void switchToAdminDashboard() {
        // Đã ở trang dashboard nên không cần làm gì
        adminTabPane.getSelectionModel().select(0);
    }

    @FXML
    private void switchToUserManagement() {
        adminTabPane.getSelectionModel().select(1);
    }

    @FXML
    private void switchToExerciseManagement() {
        adminTabPane.getSelectionModel().select(2);
    }

    @FXML
    private void switchToDataAnalysis() {
        adminTabPane.getSelectionModel().select(3);
    }

    @FXML
    private void switchToUserMode() {
        // Chuyển về giao diện người dùng
        try {
            switchScene("home.fxml");
        } catch (IOException ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void logout() {
        try {
            // Đặt người dùng hiện tại thành null
            User.setCurrentUser(null);
            
            // Chuyển đến màn hình đăng nhập
            switchScene("Login.fxml");
        } catch (Exception ex) {
            Logger.getLogger(AdminDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đăng xuất: " + ex.getMessage());
        }
    }
    
    
}