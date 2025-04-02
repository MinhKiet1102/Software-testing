package com.milkyway.healthmanagement;


import com.milkyway.pojo.Exercise;
import com.milkyway.service.ExerciseService;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class ExercisePageController extends SwitchSceneController implements Initializable{

    @FXML
    private GridPane gridPane;

    @FXML
    private ScrollPane scrollPanel;
    
    @FXML
    private TextField txtFind;
    
    @FXML
    private void loadExercise(String kw) throws IOException{
        ExerciseService es = new ExerciseService();

        try {
            gridPane.getChildren().clear();
            int col = 0, row = 0;
            List<Exercise> e = es.getExercise(kw);
            for(var i : e ){
                FXMLLoader fXMLLoader = new FXMLLoader();
                fXMLLoader.setLocation(getClass().getResource("/com/milkyway/healthmanagement/Exercise.fxml"));
                AnchorPane anchorPane = fXMLLoader.load();
                ExerciseController controller = fXMLLoader.getController();
                controller.setData(i);
                
                anchorPane.setOnMouseClicked(event -> {
                try {
                    switchToExerciseDetail(i);
                } catch (IOException ex) {
                    Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
                
                if(col == 4)
                {
                    col = 0;
                    row++;
                }
                gridPane.add(anchorPane, ++col, row);
                GridPane.setMargin(anchorPane, new Insets(10));  
            }
        } catch (SQLException ex) {
            Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
    }
    
    private void switchToExerciseDetail(Exercise exercise) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milkyway/healthmanagement/ExerciseDetail.fxml"));
        Parent root = loader.load();

        // Tạo cửa sổ mới (Stage)
        Stage stage = new Stage();
        stage.setScene(new Scene(root, 700, 225));
        
        // Ngăn không cho người dùng tương tác với cửa sổ chính khi chưa đóng cửa sổ con
        stage.initModality(Modality.APPLICATION_MODAL); 

        // Hiển thị cửa sổ
        stage.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            displayUsername();
            
            loadExercise(null);
        } catch (IOException ex) {
            Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.txtFind.textProperty().addListener((ev) -> {
            try {
                this.loadExercise(this.txtFind.getText());
            } catch (IOException ex) {
                Logger.getLogger(ExercisePageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
}
