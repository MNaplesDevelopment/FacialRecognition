module com.naples.facialrecognition {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires jdk.jsobject;

    opens com.naples.facialrecognition to javafx.fxml;
    exports com.naples.facialrecognition;
}