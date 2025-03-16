module com.milkyway.healthmanagement {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.milkyway.healthmanagement to javafx.fxml;
    exports com.milkyway.healthmanagement;
}
