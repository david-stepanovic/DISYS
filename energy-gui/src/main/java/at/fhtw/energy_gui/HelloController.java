package at.fhtw.energy_gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.URI;
import java.net.http.*;

public class HelloController {

    @FXML private Label communityPoolLabel;
    @FXML private Label gridPortionLabel;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private Label producedLabel;
    @FXML private Label usedLabel;
    @FXML private Label gridUsedLabel;

    private final HttpClient client = HttpClient.newHttpClient();

    @FXML
    public void handleRefresh() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/energy/current"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            double depleted = parseValue(body, "communityDepleted");
            double grid = parseValue(body, "gridPortion");
            communityPoolLabel.setText("Community Pool: " + depleted + "% used");
            gridPortionLabel.setText("Grid Portion: " + grid + "%");
        } catch (Exception e) {
            communityPoolLabel.setText("Fehler: " + e.getMessage());
        }
    }

    @FXML
    public void handleShowData() {
        try {
            String start = startField.getText();
            String end = endField.getText();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/energy/historical?start=" + start + "&end=" + end))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            double produced = parseValue(body, "communityProduced");
            double used = parseValue(body, "communityUsed");
            double gridUsed = parseValue(body, "gridUsed");
            producedLabel.setText("Community produced: " + produced + " kWh");
            usedLabel.setText("Community used: " + used + " kWh");
            gridUsedLabel.setText("Grid used: " + gridUsed + " kWh");
        } catch (Exception e) {
            producedLabel.setText("Fehler: " + e.getMessage());
        }
    }

    private double parseValue(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return 0;
        int start = idx + search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Double.parseDouble(json.substring(start, end).trim());
    }
}