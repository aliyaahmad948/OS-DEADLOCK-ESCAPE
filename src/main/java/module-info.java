module com.example.deadlock {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens application to javafx.graphics, javafx.fxml;
    opens ui to javafx.graphics, javafx.fxml;
    opens model to javafx.graphics, javafx.fxml;
    opens logic to javafx.graphics, javafx.fxml;
    opens utils to javafx.graphics, javafx.fxml;
    opens test to javafx.graphics, javafx.fxml;
    opens db to javafx.graphics, javafx.fxml;

    exports application;
    exports db;
}