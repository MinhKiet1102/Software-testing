module com.milkyway.healthmanagement {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.base;
    requires java.persistence;
    requires java.sql;
    
    requires de.jensd.fx.glyphs.fontawesome;
    opens com.milkyway.healthmanagement to javafx.fxml;
    exports com.milkyway.healthmanagement;
    exports com.milkyway.service;
    exports com.milkyway.pojo;
}
