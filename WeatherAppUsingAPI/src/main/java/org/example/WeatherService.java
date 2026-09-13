package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WeatherService {
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherSummary fetchWeather(String city, String apiKey) {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String requestUrl = API_URL + "?q=" + encodedCity + "&units=metric&appid=" + apiKey;

        try {
            String response = restTemplate.getForObject(requestUrl, String.class);
            
            if (response == null) {
                throw new RuntimeException("Empty response from API");
            }

            JSONObject root = new JSONObject(response);
            if (root.has("cod") && root.get("cod") instanceof Integer && root.getInt("cod") != 200) {
                throw new RuntimeException("API Error: " + root.optString("message", "Unknown error"));
            }

            JSONObject main = root.getJSONObject("main");
            JSONArray weatherArray = root.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);

            return new WeatherSummary(
                    root.optString("name", city),
                    main.getDouble("temp"),
                    weather.optString("description", "No description available"),
                    main.getInt("humidity")
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather for " + city + ": " + e.getMessage(), e);
        }
    }

    public record WeatherSummary(String city, double temperatureCelsius, String description, int humidity) {
    }
}
