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
import javafx.fxml.Initializable;
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
import com.milkyway.pojo.User;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExerciseLogController extends SwitchSceneController implements Initializable {

    @FXML
    private TableView<Exerciselog> tbExercise;

    @FXML
    private DatePicker dtpDateOfExercise;

    @FXML
    private TextField txtFind;

    @FXML
    private Label txtTotal;

    private ObservableList<Exerciselog> exerciseData = FXCollections.observableArrayList();


   

    private void loadTableView() {

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
            private final Button btn = new Button("Xóa");
            {
                btn.setOnAction(evt -> {
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
                    } else {
                        return;
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
        tbExercise.getColumns().clear();
        tbExercise.getColumns().addAll(colName, colTime, colCalories, colAction);
        tbExercise.setItems(exerciseData);
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


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        displayUsername();


        this.loadTableView();


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
