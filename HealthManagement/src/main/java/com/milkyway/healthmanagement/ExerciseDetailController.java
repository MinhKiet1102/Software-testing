/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.milkyway.healthmanagement;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author ASUS
 */
public class ExerciseDetailController implements Initializable {

    @FXML
    private DatePicker dtpDate;

    @FXML
    private TextField txtDuration;

    @FXML
    private ComboBox<String> txtEffort;

    @FXML
    private TextField txtExercise;
    
    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (txtEffort != null) {
            txtEffort.getItems().addAll("Nhẹ", "Vừa", "Nặng");}    
        else {
            System.err.println("Lỗi: ComboBox txtEffort chưa được khởi tạo từ FXML!");
        }
    }
}
