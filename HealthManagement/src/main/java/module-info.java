module com.milkyway.healthmanagement {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.base;
    requires java.persistence;
    requires java.sql;
    


    opens com.milkyway.healthmanagement to javafx.fxml;
    exports com.milkyway.healthmanagement;
    exports com.milkyway.pojo;
}
