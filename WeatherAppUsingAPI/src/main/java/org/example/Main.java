package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final List<String> CITY_OPTIONS = List.of(
            "New York",
            "London",
            "Tokyo",
            "Paris",
            "Sydney"
    );

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String apiKey = getApiKey(scanner);

        while (true) {
            printMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1", "2", "3", "4", "5" -> {
                    String city = CITY_OPTIONS.get(Integer.parseInt(choice) - 1);
                    showWeatherForCity(city, apiKey);
                }
                case "6" -> showWeatherForMultipleCities(scanner, apiKey);
                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid selection. Please choose a valid menu option.");
            }

            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("====================================");
        System.out.println("     OpenWeatherMap Weather Menu     ");
        System.out.println("====================================");
        for (int i = 0; i < CITY_OPTIONS.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, CITY_OPTIONS.get(i));
        }
        System.out.println("6. Check 2-3 cities");
        System.out.println("0. Exit");
    }

    private static String getApiKey(Scanner scanner) {
        String apiKey = System.getProperty("weather.api.key", System.getenv("OPENWEATHER_API_KEY"));
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey.trim();
        }

        System.out.println("OpenWeatherMap API key is missing.");
        System.out.print("Enter your OpenWeatherMap API key: ");
        apiKey = scanner.nextLine().trim();

        if (apiKey.isBlank()) {
            throw new IllegalStateException("An API key is required to fetch weather data.");
        }

        System.setProperty("weather.api.key", apiKey);
        return apiKey;
    }

    private static void showWeatherForCity(String city, String apiKey) {
        try {
            WeatherSummary summary = fetchWeather(city, apiKey);
            System.out.println("====================================");
            System.out.printf("Current weather for %s%n", summary.city());
            System.out.printf("Temperature: %.1f°C%n", summary.temperatureCelsius());
            System.out.printf("Description: %s%n", capitalize(summary.description()));
            System.out.printf("Humidity: %d%%%n", summary.humidity());
            System.out.println("====================================");
        } catch (IOException | InterruptedException e) {
            System.out.println("Could not fetch weather for " + city + ".");
            System.out.println("Error: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private static void showWeatherForMultipleCities(Scanner scanner, String apiKey) {
        List<String> chosenCities = new ArrayList<>();
        System.out.println("Choose 2 to 3 cities to compare.");
        System.out.print("Enter city numbers separated by commas (example: 1,2,3): ");
        String line = scanner.nextLine().trim();

        String[] parts = line.split(",");
        for (String part : parts) {
            String value = part.trim();
            if (value.isEmpty()) {
                continue;
            }
            try {
                int index = Integer.parseInt(value);
                if (index < 1 || index > CITY_OPTIONS.size()) {
                    System.out.println("Ignoring invalid city number: " + value);
                    continue;
                }
                String city = CITY_OPTIONS.get(index - 1);
                if (!chosenCities.contains(city)) {
                    chosenCities.add(city);
                }
            } catch (NumberFormatException e) {
                System.out.println("Ignoring invalid city entry: " + value);
            }
        }

        if (chosenCities.size() < 2 || chosenCities.size() > 3) {
            System.out.println("Please select between 2 and 3 valid cities.");
            return;
        }

        System.out.println("Weather summary for selected cities:");
        for (String city : chosenCities) {
            showWeatherForCity(city, apiKey);
        }
    }

    private static WeatherSummary fetchWeather(String city, String apiKey) throws IOException, InterruptedException {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String requestUrl = API_URL + "?q=" + encodedCity + "&units=metric&appid=" + apiKey;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            String message = errorJson.optString("message", "Unknown API error.");
            throw new IOException("OpenWeatherMap request failed: " + message);
        }

        JSONObject root = new JSONObject(response.body());
        if (root.has("cod") && root.get("cod") instanceof Integer && root.getInt("cod") != 200) {
            throw new IOException(root.optString("message", "Invalid response from the API."));
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
    }

    private static String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return "N/A";
        }

        String[] words = text.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                builder.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return builder.toString().trim();
    }

    private record WeatherSummary(String city, double temperatureCelsius, String description, int humidity) {
    }
}