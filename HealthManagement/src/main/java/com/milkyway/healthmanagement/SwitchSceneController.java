/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.healthmanagement;

import com.milkyway.pojo.User;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author ASUS
 */
public class SwitchSceneController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Label username;

    @FXML
    private Button logout_btn;

    private Alert alert;

    public void SwitchToHome(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
        root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Health Management System");
        Image icon = new Image(getClass().getResourceAsStream("/com/milkyway/healthmanagement/image/image.jpg"));
        stage.getIcons().add(icon);
        stage.setResizable(false);
        scene = new Scene(root);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    public void SwitchToExerciseLog(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milkyway/healthmanagement/ExerciseLog.fxml"));
        root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Health Management System");
        Image icon = new Image(getClass().getResourceAsStream("/com/milkyway/healthmanagement/image/image.jpg"));
        stage.getIcons().add(icon);
        stage.setResizable(false);
        scene = new Scene(root);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    public void SwitchToExercisePage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milkyway/healthmanagement/ExercisePage.fxml"));
        root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Health Management System");
        Image icon = new Image(getClass().getResourceAsStream("/com/milkyway/healthmanagement/image/image.jpg"));
        stage.getIcons().add(icon);
        stage.setResizable(false);
        scene = new Scene(root);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    public void SwitchToMealFood(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("MealFood.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Health Management System");
        Image icon = new Image(getClass().getResourceAsStream("/com/milkyway/healthmanagement/image/image.jpg"));
        stage.getIcons().add(icon);
        stage.setResizable(false);
        scene = new Scene(root);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    public void SwitchToTarget(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("Target.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Health Management System");
        Image icon = new Image(getClass().getResourceAsStream("/com/milkyway/healthmanagement/image/image.jpg"));
        stage.getIcons().add(icon);
        stage.setResizable(false);
        scene = new Scene(root);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    public void SwitchToPersonalInfor(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("PersonalInfor.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Health Management System");
        Image icon = new Image(getClass().getResourceAsStream("/com/milkyway/healthmanagement/image/image.jpg"));
        stage.getIcons().add(icon);
        stage.setResizable(false);
        scene = new Scene(root);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    public void displayUsername() {
        if (User.getCurrentUser() != null) {
            String user = User.getCurrentUser().getUsername();
            username.setText(user.substring(0, 1).toUpperCase() + user.substring(1));
        } else {
            username.setText("Guest");
        }
    }

    public void logout() {
        try {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có chắc chắn muốn đăng xuất không?");
            Optional<ButtonType> option = alert.showAndWait();

            if (option.get().equals(ButtonType.OK)) {
                logout_btn.getScene().getWindow().hide();

                Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));

                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setTitle("Health Management System");
                Image icon = new Image(getClass().getResourceAsStream("/com/milkyway/healthmanagement/image/image.jpg"));
                stage.getIcons().add(icon);
                stage.setResizable(false);
                stage.setScene(scene);
                stage.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    
    public void closeWindow(Button btnCancel) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    public void minimizeWindow(Button btnMinimize) {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
    }

}
