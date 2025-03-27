module com.milkyway.healthmanagement {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.base;
    requires java.persistence;
    


    opens com.milkyway.healthmanagement to javafx.fxml;
    exports com.milkyway.healthmanagement;
}
