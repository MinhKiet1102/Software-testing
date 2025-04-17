package com.milkyway.healthmanagement;

import com.milkyway.pojo.Exerciselog;
import com.milkyway.services.ExerciseLogService;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.milkyway.pojo.User;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExerciseLogController extends SwitchSceneController implements Initializable {
    @FXML
    private Button btnClose;

    @FXML
    private Button btnMinimize;

    @FXML
    private TableView<Exerciselog> tbExercise;

    @FXML
    private DatePicker dtpDateOfExercise;

    @FXML
    private TextField txtFind;

    @FXML
    private Label txtTotal;

    @FXML
    private Button prevDateButton;

    @FXML
    private Button nextDateButton;

    private ObservableList<Exerciselog> exerciseData = FXCollections.observableArrayList();

    private void loadTableView(){
        TableColumn<Exerciselog, String> colName = new TableColumn<>("Mục Tiêu");
        colName.setCellValueFactory(cellData -> {
            if (cellData.getValue().getExerciseId() != null && cellData.getValue().getExerciseId().getExerciseName() != null) {
                return new SimpleStringProperty(cellData.getValue().getExerciseId().getExerciseName());
            } else {
                return new SimpleStringProperty("Không có dữ liệu");
            }

        });
        colName.setPrefWidth(300);
        TableColumn<Exerciselog, Integer> colTime = new TableColumn<>("Thời Gian");
        colTime.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colTime.setPrefWidth(200);

        TableColumn<Exerciselog, String> colCalories = new TableColumn<>("Tổng calo tiêu thụ");
        colCalories.setCellValueFactory(new PropertyValueFactory<>("energyBurn"));
        colCalories.setPrefWidth(200);

        TableColumn<Exerciselog, Void> colAction = new TableColumn<>("Hành Động");
        colAction.setPrefWidth(200);
        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox buttonsBox = new HBox(5);
            
            {
                // Xử lý nút sửa
                btnEdit.setOnAction(evt -> {
                    Exerciselog log = getTableView().getItems().get(getIndex());
                    if (log != null) {
                        try {
                            openExerciseDetailForEdit(log);
                        } catch (IOException ex) {
                            Logger.getLogger(ExerciseLogController.class.getName()).log(Level.SEVERE, null, ex);
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ chỉnh sửa: " + ex.getMessage());
                        }
                    }
                });
                
                // Xử lý nút xóa
                btnDelete.setOnAction(evt -> {
                    //cho phép người dùng xóa khi nhấn nút xác nhận
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa không?");
                    alert.setTitle("Xác nhận xóa");
                    alert.setHeaderText("Xóa lịch sử bài tập");
                    alert.setContentText("Bạn có chắc chắn muốn xóa không?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        Exerciselog log = getTableView().getItems().get(getIndex());
                        if (log != null) {
                            ExerciseLogService els = new ExerciseLogService();
                            try {
                                els.deleteExerciseLog(log.getIdExLog());
                            } catch (SQLException ex) {
                                Logger.getLogger(ExerciseLogController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Xóa thành công!");
                            Date sqlDateFilter = Date.valueOf(dtpDateOfExercise.getValue());
                            loadData(User.getCurrentUser().getId(), sqlDateFilter);
                            txtTotal.setText(String.valueOf(exerciseData.stream().mapToDouble(Exerciselog::getEnergyBurn).sum()));
                        }
                    }
                });
                
                buttonsBox.getChildren().addAll(btnEdit, btnDelete);
                // Căn giữa các nút trong cột "Hành động"
                buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
        tbExercise.getColumns().clear();
        tbExercise.getColumns().addAll(colName, colTime, colCalories, colAction);
        tbExercise.setItems(exerciseData);
    }

    /**
     * Mở cửa sổ ExerciseDetail để chỉnh sửa bản ghi log
     * @param log Bản ghi log cần chỉnh sửa
     * @throws IOException Nếu có lỗi khi tải FXML
     */
    private void openExerciseDetailForEdit(Exerciselog log) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milkyway/healthmanagement/ExerciseDetail.fxml"));
        Parent root = loader.load();

        // Tạo cửa sổ mới (Stage)
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Chỉnh sửa bài tập");

        // Lấy controller và thiết lập dữ liệu
        ExerciseDetailController controller = loader.getController();
        // Thiết lập bài tập
        controller.setExercise(log.getExerciseId());
        
        // Thiết lập dữ liệu chi tiết từ log
        controller.setExerciseLogData(log);

        // Thiết lập modal (người dùng phải đóng cửa sổ này trước khi quay lại cửa sổ chính)
        stage.initModality(Modality.APPLICATION_MODAL);

        // Hiển thị cửa sổ và đợi người dùng đóng cửa sổ
        stage.showAndWait();
        
        // Sau khi người dùng đóng cửa sổ, tải lại dữ liệu để hiển thị các thay đổi
        if (User.getCurrentUser() != null && dtpDateOfExercise.getValue() != null) {
            Date sqlDateFilter = Date.valueOf(dtpDateOfExercise.getValue());
            loadData(User.getCurrentUser().getId(), sqlDateFilter);
        }
    }

    private final ExerciseLogService exerciseLogService = new ExerciseLogService();

    public void loadData(int userId, Date selectedDate) {

        try {
            List<Exerciselog> logs = exerciseLogService.getExerciseLogsByUserAndDate(userId, selectedDate);

            exerciseData.clear();
            exerciseData.addAll(logs);

            if (logs.isEmpty()) {
                tbExercise.setPlaceholder(new Label("Không có dữ liệu nào cho ngày " + selectedDate));
                txtTotal.setText("0");
            } else {
                System.out.println("Tải dữ liệu thành công cho ngày " + selectedDate);
                txtTotal.setText(String.valueOf(exerciseData.stream().mapToDouble(Exerciselog::getEnergyBurn).sum()));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tải dữ liệu: " + e.getMessage());
        }
    }

    /**
     * Xử lý khi người dùng nhấn nút mũi tên trái để chuyển đến ngày trước đó
     */
    @FXML
    private void handlePrevDate() {
        LocalDate currentDate = dtpDateOfExercise.getValue();
        if (currentDate != null) {
            LocalDate previousDay = currentDate.minusDays(1);
            dtpDateOfExercise.setValue(previousDay);
        }
    }

    /**
     * Xử lý khi người dùng nhấn nút mũi tên phải để chuyển đến ngày tiếp theo
     */
    @FXML
    private void handleNextDate() {
        LocalDate currentDate = dtpDateOfExercise.getValue();
        if (currentDate != null) {
            // Không cho phép chọn ngày trong tương lai
            LocalDate nextDay = currentDate.plusDays(1);
            if (!nextDay.isAfter(LocalDate.now())) {
                dtpDateOfExercise.setValue(nextDay);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không thể chọn ngày trong tương lai.");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnClose.setOnAction(event -> closeWindow(btnClose));
        btnMinimize.setOnAction(event -> minimizeWindow(btnMinimize));
        displayUsername();
        loadTableView();

        if (User.getCurrentUser() != null) {
            LocalDate todayLocalDate = LocalDate.now();
            Date sqlToday = Date.valueOf(todayLocalDate);
            loadData(User.getCurrentUser().getId(), sqlToday);

            dtpDateOfExercise.valueProperty().addListener((observable, oldValue, newValue) -> {
                Date sqlDateFilter = null;
                if (newValue != null) {
                    sqlDateFilter = Date.valueOf(newValue);
                }
                loadData(User.getCurrentUser().getId(), sqlDateFilter);
            });
            dtpDateOfExercise.setValue(LocalDate.now());
            txtFind.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    tbExercise.setItems(exerciseData);
                } else {
                    ObservableList<Exerciselog> filteredData = FXCollections.observableArrayList();
                    for (Exerciselog log : exerciseData) {
                        if (log.getExerciseId().getExerciseName().toLowerCase().contains(newValue.toLowerCase())) {
                            filteredData.add(log);
                        }
                    }
                    tbExercise.setItems(filteredData);
                }
            });
        } else {

            showAlert(Alert.AlertType.WARNING, "Yêu cầu đăng nhập", "Vui lòng đăng nhập để xem lịch sử.");
            tbExercise.setPlaceholder(new Label("Bạn cần đăng nhập để xem dữ liệu."));
            txtTotal.setText("0");
        }
    }
}
