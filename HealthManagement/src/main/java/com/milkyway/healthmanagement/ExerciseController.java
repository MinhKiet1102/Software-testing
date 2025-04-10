/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.milkyway.healthmanagement;

import com.milkyway.pojo.Exercise;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * FXML Controller class
 *
 * @author ASUS
 */
public class ExerciseController extends SwitchSceneController implements Initializable {
        @FXML
    private ImageView imageExercise;

    @FXML
    private Label nameExerciseLabel; 
    
    private Exercise exercise;
    public void setData(Exercise exercise){
        //kiểm tra nameExerciseLabel có null hay không
        if(nameExerciseLabel != null){
            this.exercise = exercise;
            nameExerciseLabel.setText(exercise.getExerciseName());
            String imgScr = "/com/milkyway/healthmanagement/image/" + exercise.getImageExercise();
            InputStream input = getClass().getResourceAsStream(imgScr);
            if(input != null)
            {
                Image image = new Image(input);
                imageExercise.setImage(image);
            }
            else {
                    System.out.println("Không tìm thấy ảnh" + imgScr);
                    System.out.println("Sử dụng ảnh mặc định"); 
                }
            }
        else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy tên bài tập!");
        }
        
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }    
}
