package at.fhtw.energy_gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HelloController {

    @FXML private Label communityPoolLabel;
    @FXML private Label gridPortionLabel;
    @FXML private ComboBox<String> startBox;
    @FXML private ComboBox<String> endBox;
    @FXML private Label producedLabel;
    @FXML private Label usedLabel;
    @FXML private Label gridUsedLabel;

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter API_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        List<String> options = generateOptions();
        startBox.getItems().addAll(options);
        endBox.getItems().addAll(options);
        startBox.setValue("10.01.2025 00:00");
        endBox.setValue("14.01.2025 23:00");
    }

    private List<String> generateOptions() {
        List<String> options = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2025, 1, 10, 0, 0);
        for (int day = 0; day < 5; day++) {
            for (int hour = 0; hour < 24; hour++) {
                options.add(base.plusDays(day).plusHours(hour).format(DISPLAY_FMT));
            }
        }
        return options;
    }

    @FXML
    public void handleRefresh() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/energy/current"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = mapper.readTree(response.body());
            double depleted = node.get("community_depleted").asDouble();
            double grid = node.get("grid_portion").asDouble();
            communityPoolLabel.setText(depleted + "% used");
            gridPortionLabel.setText(grid + "%");
        } catch (Exception e) {
            communityPoolLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleShowData() {
        try {
            String start = toApiFormat(startBox.getValue());
            String end = toApiFormat(endBox.getValue());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/energy/historical?start=" + start + "&end=" + end))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode rows = mapper.readTree(response.body());
            double produced = 0, used = 0, gridUsed = 0;
            for (JsonNode row : rows) {
                produced += row.get("community_produced").asDouble();
                used += row.get("community_used").asDouble();
                gridUsed += row.get("grid_used").asDouble();
            }
            producedLabel.setText(round(produced) + " kWh");
            usedLabel.setText(round(used) + " kWh");
            gridUsedLabel.setText(round(gridUsed) + " kWh");
        } catch (Exception e) {
            producedLabel.setText("Error: " + e.getMessage());
        }
    }

    private String toApiFormat(String display) {
        try {
            return LocalDateTime.parse(display, DISPLAY_FMT).format(API_FMT);
        } catch (Exception e) {
            return display;
        }
    }

    // auf 3 Nachkommastellen runden
    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
