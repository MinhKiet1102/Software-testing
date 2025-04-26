module com.milkyway.healthmanagement {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.base;
    requires java.persistence;
    requires java.sql;
    requires spring.security.crypto;
    
    requires de.jensd.fx.glyphs.fontawesome;
    
    // Thêm requires cho JavaMail API
    requires java.mail;
    requires activation;
    
    opens com.milkyway.healthmanagement to javafx.fxml;
    exports com.milkyway.healthmanagement;
    exports com.milkyway.services;
    exports com.milkyway.pojo;
}
