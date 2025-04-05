/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.milkyway.healthmanagement;

import com.milkyway.pojo.Target;
import com.milkyway.pojo.User;
import com.milkyway.service.TargetService;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class TargetController extends SwitchSceneController implements Initializable {

    @FXML
    private AnchorPane menu_target_nav;

    @FXML
    private Button menu_target_nav_main;

    @FXML
    private Button menu_target_nav_sub;

    @FXML
    private AnchorPane form_target_main;

    @FXML
    private AnchorPane form_target_sub;

    @FXML
    private Label username;

    @FXML
    private TextArea myPlans_plan;

    @FXML
    private DatePicker myPlans_startDate;

    @FXML
    private DatePicker myPlans_endDate;

    @FXML
    private TextField myPlans_target;

    @FXML
    private ComboBox<String> myPlans_unit;

    @FXML
    private TableView<Target> myPlans_tableView;

    @FXML
    private TextField myPlans_progress;

    @FXML
    private TableColumn<Target, String> myPlans_col_plan;

    @FXML
    private TableColumn<Target, String> myPlans_col_dateCreated;

    @FXML
    private TableColumn<Target, String> myPlans_col_startDate;

    @FXML
    private TableColumn<Target, String> myPlans_col_endDate;

    @FXML
    private TableColumn<Target, String> myPlans_col_progress;

    @FXML
    private TableColumn<Target, String> myPlans_col_status;

    @FXML
    private TableColumn<Target, String> myPlans_col_target;

    @FXML
    private TableColumn<Target, String> myPlans_col_unit;

    @FXML
    private Label finishedPlans_countPlan;

    @FXML
    private Label finishedPlans_achievedPlan;

    @FXML
    private TextField finishedPlans_planID;

    @FXML
    private ComboBox<String> finishedPlans_status;

    @FXML
    private TableColumn<Target, String> finishedPlans_col_endDate;

    @FXML
    private TableColumn<Target, String> finishedPlans_col_plan;

    @FXML
    private TableColumn<Target, String> finishedPlans_col_planID;

    @FXML
    private TableColumn<Target, String> finishedPlans_col_progress;

    @FXML
    private TableColumn<Target, String> finishedPlans_col_startDate;

    @FXML
    private TableColumn<Target, String> finishedPlans_col_status;

    @FXML
    private TableColumn<Target, String> finishedPlans_col_target;

    @FXML
    private TableColumn<Target, String> finishedPlans_col_unit;

    @FXML
    private TableView<Target> finishedPlans_tableView;

    @FXML
    private Button menu_personal_btn;

    private final double ORIGINAL_Y = 394;
    private final double EXPANDED_Y = 441;
    private final double ORIGINAL_X = 9;

    private void animateMoveY(Node node, double toY, double durationMillis) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(durationMillis), node);
        transition.setToY(toY - node.getLayoutY()); 
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.setOnFinished(e -> {
            node.setTranslateY(0); 
            node.setLayoutY(toY);  // Cập nhật vị trí thật
        });
        transition.play();
    }

    @FXML
    private void showMenu() {
        menu_target_nav.setVisible(true);
        menu_personal_btn.setLayoutX(ORIGINAL_X); 
        animateMoveY(menu_personal_btn, EXPANDED_Y, 200); 
    }

    @FXML
    private void hideMenu() {
        menu_target_nav.setVisible(false);
        animateMoveY(menu_personal_btn, ORIGINAL_Y, 200); 
    }

    private ObservableList<Target> myPlansListData = FXCollections.observableArrayList();

    private ObservableList<Target> finishedPlansListData = FXCollections.observableArrayList();

    private TargetService planService = new TargetService();

    private Alert alert;

    private int idTarget;

    private void showForm(Pane showPane, Pane hidePane) {
        showPane.setVisible(true);
        hidePane.setVisible(false);

        if (showPane == form_target_sub) {
            try {
                finishedPlansShowData();

                finishedPlansDisplayCP();

                finishedPlansDisplayAP();
            } catch (SQLException ex) {
                Logger.getLogger(TargetController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (showPane == form_target_main) {
            try {
                myPlansShowData();
            } catch (SQLException ex) {
                Logger.getLogger(TargetController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupMenuEvents() {
        menu_target_nav_main.setOnAction(event -> showForm(form_target_main, form_target_sub));
        menu_target_nav_sub.setOnAction(event -> showForm(form_target_sub, form_target_main));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String[] listUnits = {"kg (Kilogam)", "g (Gram)", "km (Kilomet)", "m (Mét)", "cm (Centimet)", "h (Giờ)", "min (Phút)", "calories (Năng lượng tiêu thụ)", "reps (Số lần lặp lại bài tập)"};

    public void myPlansListUnits() {
        List<String> listU = new ArrayList<>();

        for (String data : listUnits) {
            listU.add(data);
        }

        ObservableList listData = FXCollections.observableArrayList(listU);
        myPlans_unit.setItems(listData);

        myPlans_unit.setPromptText("Chọn...");
    }

    public void myPlansAddBtn() {
        try {
            if (myPlans_plan.getText().isEmpty() || myPlans_startDate.getValue() == null
                    || myPlans_endDate.getValue() == null || myPlans_target.getText().isEmpty()
                    || myPlans_unit.getSelectionModel().getSelectedItem() == null) {

                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin!");
                return;
            }

            String planName = myPlans_plan.getText().trim();
            LocalDate startDate = myPlans_startDate.getValue();
            LocalDate endDate = myPlans_endDate.getValue();
            LocalDate today = LocalDate.now();

            if (endDate.isBefore(startDate)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Ngày kết thúc phải sau ngày bắt đầu!");
                return;
            }
            if (startDate.isBefore(today)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Ngày bắt đầu không được ở quá khứ!");
                return;
            }

            float targetValue;
            try {
                targetValue = Float.parseFloat(myPlans_target.getText().trim());
                if (targetValue <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Mục tiêu phải lớn hơn 0!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mục tiêu phải là số hợp lệ!");
                return;
            }

            if (planService.isPlanExist(planName)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Kế hoạch \"" + planName + "\" đã tồn tại!");
                return;
            }

            planService.addPlan(planName, startDate, endDate, targetValue, String.valueOf(myPlans_unit.getSelectionModel().getSelectedItem()), User.getCurrentUser().getId());

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm kế hoạch thành công!");
            myPlansShowData();
            myPlansClearBtn();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi SQL", "Có lỗi xảy ra khi thêm kế hoạch! Vui lòng thử lại.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra! Vui lòng thử lại.");
        }
    }

    public void myPlansUpdateBtn() {
        try {
            if (idTarget == 0) {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Vui lòng chọn mục");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có chắc chắn muốn CẬP NHẬT Kế: " + myPlans_plan.getText());
            Optional<ButtonType> option = alert.showAndWait();

            if (option.isPresent() && option.get().equals(ButtonType.OK)) {
                LocalDate oldEndDate = planService.getOldEndDate(idTarget);
                String currentD = planService.getDateCreated(idTarget);
                LocalDate newEndDate = myPlans_endDate.getValue();
                float targetValue = Float.parseFloat(myPlans_target.getText().trim());

                if (newEndDate.isBefore(oldEndDate)) {
                    showAlert(Alert.AlertType.ERROR, "Error Message", "Bạn không được rút ngắn thời gian mục tiêu!");
                } else {
                    planService.updatePlan(idTarget, myPlans_plan.getText(), myPlans_startDate.getValue(), newEndDate, currentD, targetValue, String.valueOf(myPlans_unit.getSelectionModel().getSelectedItem()));

                    showAlert(Alert.AlertType.INFORMATION, "Information Message", "Đã cập nhật thành công!");
                    myPlansShowData();
                    myPlansClearBtn();
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Information Message", "Cancelled!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void myPlansClearBtn() {
        myPlans_plan.setText("");
        myPlans_startDate.setValue(null);
        myPlans_endDate.setValue(null);
        myPlans_target.setText("");
        myPlans_progress.setText("");
        progressPercent.setText("0%");
        idTarget = 0;
        myPlans_tableView.getSelectionModel().clearSelection();
        myPlans_unit.setValue(null);

        // Sử dụng Platform.runLater để đảm bảo lựa chọn được cập nhật lại
        Platform.runLater(() -> {
            if (!myPlans_tableView.getItems().isEmpty()) {
                myPlans_tableView.getSelectionModel().selectFirst();
                myPlans_tableView.getSelectionModel().clearSelection();
            }
        });
    }

    public void myPlansDeleteBtn() {
        if (idTarget == 0) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Vui lòng chọn mục đầu tiên");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Message");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa kế hoạch không?: " + myPlans_plan.getText());
        Optional<ButtonType> option = alert.showAndWait();

        if (option.isPresent() && option.get().equals(ButtonType.OK)) {
            try {
                if (planService.isPlanExist(idTarget)) {
                    planService.deletePlan(idTarget);
                    showAlert(Alert.AlertType.INFORMATION, "Information Message", "Đã xóa thành công!");
                    myPlansShowData();
                    myPlansClearBtn();
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi SQL", "Không thể xóa kế hoạch!");
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Information Message", "Cancelled!");
        }
    }

    public void myPlansUpdateProgressBtn() {
        Target selectedPlan = myPlans_tableView.getSelectionModel().getSelectedItem();

        if (selectedPlan == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn kế hoạch!");
            return;
        }

        float newProgress;
        try {
            newProgress = Float.parseFloat(myPlans_progress.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng nhập số hợp lệ!");
            return;
        }

        if (newProgress > selectedPlan.getTargetNumber()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Tiến độ không thể lớn hơn mục tiêu!");
            return;
        }

        try {
            planService.updatePlanProgress(selectedPlan.getIdTarget(), newProgress);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tiến độ đã được cập nhật!");
            myPlansShowData();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi SQL", "Không thể cập nhật tiến độ!");
            e.printStackTrace();
        }
    }

    @FXML
    private Label progressPercent;

    public void updateProgressPercent() {
        try {
            Target pData = myPlans_tableView.getSelectionModel().getSelectedItem();
            if (pData == null) {
                progressPercent.setText("0%");
                return;
            }
            idTarget = pData.getIdTarget();
            Target target = planService.getPlanById(idTarget);

            if (target != null) {
                float progress = target.getProgress();
                float targetNumber = target.getTargetNumber();

                if (targetNumber <= 0) {
                    progressPercent.setText("0%");
                    return;
                }

                float percentage = (progress / targetNumber) * 100;

                // Đảm bảo phần trăm nằm trong khoảng từ 0 đến 100
                percentage = Math.max(0, Math.min(percentage, 100));

                // Định dạng phần trăm với tối đa hai chữ số thập phân
                String formattedPercentage;
                if (percentage == 0 || percentage == 100) {
                    formattedPercentage = String.format("%.0f%%", percentage);
                } else {
                    formattedPercentage = String.format("%.2f%%", percentage);
                }

                progressPercent.setText(formattedPercentage);
            } else {
                progressPercent.setText("0%");
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy mục tiêu!");
            }

        } catch (SQLException e) {
            progressPercent.setText("0%");
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Có lỗi xảy ra khi truy vấn cơ sở dữ liệu!");
            e.printStackTrace();
        }
    }

    public ObservableList<Target> myPlansDataList() {
        try {
            return planService.getPlansForCurrentUser(User.getCurrentUser().getId());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi tải dữ liệu kế hoạch!");
            return FXCollections.observableArrayList();
        }
    }

    public void myPlansShowData() throws SQLException {
        myPlansListData.setAll(myPlansDataList());

        myPlans_col_plan.setCellValueFactory(new PropertyValueFactory<>("targetName"));
        myPlans_col_dateCreated.setCellValueFactory(new PropertyValueFactory<>("dateCreated"));
        myPlans_col_startDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        myPlans_col_endDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        myPlans_col_target.setCellValueFactory(new PropertyValueFactory<>("targetNumber"));
        myPlans_col_unit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        myPlans_col_progress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        myPlans_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));

        myPlans_col_status.setCellValueFactory(cellData -> {
            Target target = cellData.getValue();
            String status = planService.calculateStatus(target);
            try {
                planService.updatePlanStatus(target.getIdTarget(), status);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty(status);
        });

        myPlans_col_status.setCellFactory(new Callback<TableColumn<Target, String>, TableCell<Target, String>>() {
            @Override
            public TableCell<Target, String> call(TableColumn<Target, String> param) {
                return new TableCell<Target, String>() {
                    @Override
                    protected void updateItem(String status, boolean empty) {
                        super.updateItem(status, empty);

                        if (empty || status == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("");
                        } else {
                            Text text = new Text(status);
                            setGraphic(text);

                            switch (status) {
                                case "Not Started":
                                    text.setFill(Color.GRAY);
                                    break;
                                case "In Progress":
                                    text.setFill(Color.BLUE);
                                    break;
                                case "Achieved":
                                    text.setFill(Color.GREEN);
                                    break;
                                case "Failed":
                                    text.setFill(Color.RED);
                                    break;
                                case "Cancelled":
                                    text.setFill(Color.ORANGE);
                                    break;
                                default:
                                    text.setFill(Color.BLACK); // Màu mặc định
                                    break;
                            }
                        }
                    }
                };
            }
        });

        myPlans_tableView.setItems(myPlansListData);

        // Cập nhật trạng thái cho mỗi mục trong danh sách
        myPlansListData.forEach(target -> {
            String status = planService.calculateStatus(target);
            try {
                planService.updatePlanStatus(target.getIdTarget(), status);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void myPlansSelectData() {
        Target pData = myPlans_tableView.getSelectionModel().getSelectedItem();
        int num = myPlans_tableView.getSelectionModel().getSelectedIndex();

        if ((num - 1) < -1) {
            return;
        }

        idTarget = pData.getIdTarget();

        myPlans_plan.setText(pData.getTargetName());
        myPlans_startDate.setValue(LocalDate.parse(String.valueOf(pData.getStartDate())));
        myPlans_endDate.setValue(LocalDate.parse(String.valueOf(pData.getEndDate())));
        myPlans_target.setText(String.valueOf(pData.getTargetNumber()));
        myPlans_unit.setValue(pData.getUnit());

        myPlans_progress.setText(String.valueOf(pData.getProgress()));

        planService.checkMidCycleProgress(pData);
    }

    public void finishedPlansDisplayCP() {
        try {
            int count = planService.countQuantityPlans(User.getCurrentUser().getId());
            finishedPlans_countPlan.setText(String.valueOf(count));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi tải dữ liệu kế hoạch!");
            e.printStackTrace();
        }
    }

    public void finishedPlansDisplayAP() {
        try {
            int count = planService.countAchievedPlans(User.getCurrentUser().getId());
            finishedPlans_achievedPlan.setText(String.valueOf(count));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi tải dữ liệu kế hoạch!");
            e.printStackTrace();
        }
    }

    public void finishedPlansUpdateBtn() {
        try {
            if (finishedPlans_planID.getText().isEmpty() || finishedPlans_status.getSelectionModel().getSelectedItem() == null) {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Vui lòng chọn 1 mục");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có chắc chắn muốn cập nhật trạng thái không?: " + finishedPlans_planID.getText() + "?");
            Optional<ButtonType> option = alert.showAndWait();

            if (option.isPresent() && option.get().equals(ButtonType.OK)) {
                int idTarget = Integer.parseInt(finishedPlans_planID.getText());
                Target plan = planService.getPlanById(idTarget);

                if (plan != null) {
                    String status = finishedPlans_status.getSelectionModel().getSelectedItem();
                    LocalDate today = LocalDate.now();
                    LocalDate startDate = LocalDate.parse(String.valueOf(plan.getStartDate()));
                    LocalDate endDate = LocalDate.parse(String.valueOf(plan.getEndDate()));

                    if ("Not Started".equals(status)) {
                        if (!today.isBefore(startDate)) {
                            showAlert(Alert.AlertType.WARNING, "Warning Message", "Chỉ có thể cập nhật trạng thái là Not Started trước ngày bắt đầu.");
                            return;
                        }
                    }

                    if ("Achieved".equals(status)) {
                        if (plan.getProgress() < plan.getTargetNumber()) {
                            showAlert(Alert.AlertType.WARNING, "Warning Message", "Tiến trình chưa đạt mục tiêu, không thể cập nhật trạng thái là Achieved.");
                            return;
                        }
                    }

                    if ("In Progress".equals(status)) {
                        if (today.isBefore(startDate)) {
                            showAlert(Alert.AlertType.WARNING, "Warning Message", "Chưa tới ngày bắt đầu, không thể cập nhật trạng thái là In Progress.");
                            return;
                        }
                    }

                    if ("Failed".equals(status)) {
                        if (!today.isAfter(endDate)) {
                            showAlert(Alert.AlertType.WARNING, "Warning Message", "Chưa quá ngày kết thúc, không thể cập nhật trạng thái là Failed.");
                            return;
                        }
                    }

                    planService.updatePlanStatus(idTarget, status);

                    showAlert(Alert.AlertType.INFORMATION, "Information Message", "Đã cập nhật thành công!");
                    finishedPlansShowData();
                    finishedPlansDisplayCP();
                    finishedPlansDisplayAP();
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Information Message", "Cancelled");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void finishedPlansListStatus() {
        try {
            List<String> statusList = planService.getStatusList();
            ObservableList listData = FXCollections.observableArrayList(statusList);
            finishedPlans_status.setItems(listData);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi tải danh sách trạng thái!");
            e.printStackTrace();
        }
    }

    public ObservableList<Target> finishedPlansDataList() {
        try {
            return planService.getFinishedPlansDataList(User.getCurrentUser().getId());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi tải dữ liệu kế hoạch!");
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public void finishedPlansShowData() throws SQLException {
        finishedPlansListData.setAll(finishedPlansDataList());

        finishedPlans_col_planID.setCellValueFactory(new PropertyValueFactory<>("idTarget"));
        finishedPlans_col_plan.setCellValueFactory(new PropertyValueFactory<>("targetName"));
        finishedPlans_col_startDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        finishedPlans_col_endDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        finishedPlans_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        finishedPlans_col_target.setCellValueFactory(new PropertyValueFactory<>("targetNumber"));
        finishedPlans_col_unit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        finishedPlans_col_progress.setCellValueFactory(new PropertyValueFactory<>("progress"));

        finishedPlans_col_status.setCellValueFactory(cellData -> {
            Target target = cellData.getValue();
            String status = planService.calculateStatus(target);
            try {
                planService.updatePlanStatus(target.getIdTarget(), status);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty(status);
        });

        finishedPlans_col_status.setCellFactory(new Callback<TableColumn<Target, String>, TableCell<Target, String>>() {
            @Override
            public TableCell<Target, String> call(TableColumn<Target, String> param) {
                return new TableCell<Target, String>() {
                    @Override
                    protected void updateItem(String status, boolean empty) {
                        super.updateItem(status, empty);

                        if (empty || status == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("");
                        } else {
                            Text text = new Text(status);
                            setGraphic(text);

                            switch (status) {
                                case "Not Started":
                                    text.setFill(Color.GRAY);
                                    break;
                                case "In Progress":
                                    text.setFill(Color.BLUE);
                                    break;
                                case "Achieved":
                                    text.setFill(Color.GREEN);
                                    break;
                                case "Failed":
                                    text.setFill(Color.RED);
                                    break;
                                case "Cancelled":
                                    text.setFill(Color.ORANGE);
                                    break;
                                default:
                                    text.setFill(Color.BLACK);
                                    break;
                            }
                        }
                    }
                };
            }
        });

        finishedPlans_tableView.setItems(finishedPlansListData);

        finishedPlansListData.forEach(target -> {
            String status = planService.calculateStatus(target);
            try {
                planService.updatePlanStatus(target.getIdTarget(), status);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void finishedPlansSelectData() {
        Target pData = finishedPlans_tableView.getSelectionModel().getSelectedItem();
        int num = finishedPlans_tableView.getSelectionModel().getSelectedIndex();

        if ((num - 1) < -1) {
            return;
        }

        finishedPlans_planID.setText(String.valueOf(pData.getIdTarget()));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {

            setupMenuEvents();

            displayUsername();

            myPlansListUnits();

            myPlansShowData();

            myPlans_tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateProgressPercent();
                } else {
                    progressPercent.setText("0%");
                }
            });

            finishedPlansListStatus();

            finishedPlansShowData();

            finishedPlansDisplayCP();

            finishedPlansDisplayAP();

        } catch (SQLException ex) {
            Logger.getLogger(TargetController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
