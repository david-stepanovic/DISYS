package at.fhtw.energy_gui;

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

    @FXML
    public void initialize() {
        List<String> options = generateOptions();
        startBox.getItems().addAll(options);
        endBox.getItems().addAll(options);
        startBox.setValue("10.01.2025 14:00");
        endBox.setValue("10.02.2025 14:00");
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
            String body = response.body();
            double depleted = parseValue(body, "community_depleted");
            double grid = parseValue(body, "grid_portion");
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
            String body = response.body();
            double produced = parseValue(body, "community_produced");
            double used = parseValue(body, "community_used");
            double gridUsed = parseValue(body, "grid_used");
            producedLabel.setText(produced + " kWh");
            usedLabel.setText(used + " kWh");
            gridUsedLabel.setText(gridUsed + " kWh");
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

    private double parseValue(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return 0;
        int start = idx + search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        String val = json.substring(start, end).trim().replaceAll("[^0-9.]", "");
        try { return Double.parseDouble(val); } catch (Exception e) { return 0; }
    }
}
