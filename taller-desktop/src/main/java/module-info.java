module com.taller.tallerdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;
    requires okhttp3;

    opens com.taller.tallerdesktop to javafx.fxml;
    opens com.taller.tallerdesktop.controller to javafx.fxml;
    exports com.taller.tallerdesktop;
}