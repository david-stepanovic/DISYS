module at.fhtw.energy_gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    opens at.fhtw.energy_gui to javafx.fxml;
    exports at.fhtw.energy_gui;
}