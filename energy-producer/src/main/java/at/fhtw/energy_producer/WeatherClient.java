package at.fhtw.energy_producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Open meteo API
@Component
public class WeatherClient {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${producer.latitude}")
    private double latitude;

    @Value("${producer.longitude}")
    private double longitude;

    public double getSolarRadiation() {
        try {
            String url = "https://api.open-meteo.com/v1/forecast"
                    + "?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&current=shortwave_radiation";

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());
            return root.path("current").path("shortwave_radiation").asDouble(0.0);
        } catch (Exception e) {
            System.out.println("Weather fetch failed, assuming no sun: " + e.getMessage());
            return 0.0;
        }
    }
}
