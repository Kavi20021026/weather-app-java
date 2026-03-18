import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherService {
    private static final Pattern RESULT_BLOCK = Pattern.compile("\"results\"\\s*:\\s*\\[(\\{.*?})\\s*]", Pattern.DOTALL);
    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("\"country\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern LAT_PATTERN = Pattern.compile("\"latitude\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern LON_PATTERN = Pattern.compile("\"longitude\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern CURRENT_BLOCK = Pattern.compile("\"current\"\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL);
    private static final Pattern CURRENT_TIME = Pattern.compile("\"time\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TEMPERATURE = Pattern.compile("\"temperature_2m\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern FEELS_LIKE = Pattern.compile("\"apparent_temperature\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern HUMIDITY = Pattern.compile("\"relative_humidity_2m\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern WIND_SPEED = Pattern.compile("\"wind_speed_10m\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern PRECIPITATION = Pattern.compile("\"precipitation\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern WEATHER_CODE = Pattern.compile("\"weather_code\"\\s*:\\s*(\\d+)");

    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public String getWeatherByCity(String city) throws IOException, InterruptedException {
        Location location = geocodeCity(city);
        WeatherSnapshot snapshot = fetchCurrentWeather(location);

        return "{"
            + "\"city\":\"" + JsonResponse.escape(location.name) + "\","
            + "\"country\":\"" + JsonResponse.escape(location.country) + "\","
            + "\"latitude\":" + location.latitude + ","
            + "\"longitude\":" + location.longitude + ","
            + "\"updatedAt\":\"" + JsonResponse.escape(snapshot.time) + "\","
            + "\"temperatureC\":" + snapshot.temperatureC + ","
            + "\"feelsLikeC\":" + snapshot.feelsLikeC + ","
            + "\"humidity\":" + snapshot.humidity + ","
            + "\"windSpeedKmh\":" + snapshot.windSpeedKmh + ","
            + "\"precipitationMm\":" + snapshot.precipitationMm + ","
            + "\"weatherCode\":" + snapshot.weatherCode + ","
            + "\"condition\":\"" + JsonResponse.escape(weatherDescription(snapshot.weatherCode)) + "\""
            + "}";
    }

    private Location geocodeCity(String city) throws IOException, InterruptedException {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" + encodedCity + "&count=1&language=en&format=json";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Geocoding request failed.");
        }

        String body = response.body();
        Matcher resultMatcher = RESULT_BLOCK.matcher(body);
        if (!resultMatcher.find()) {
            throw new IllegalArgumentException("City not found. Try another city name.");
        }

        String result = resultMatcher.group(1);
        return new Location(
            extractString(result, NAME_PATTERN, "Unknown"),
            extractString(result, COUNTRY_PATTERN, "Unknown"),
            extractDouble(result, LAT_PATTERN),
            extractDouble(result, LON_PATTERN)
        );
    }

    private WeatherSnapshot fetchCurrentWeather(Location location) throws IOException, InterruptedException {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + location.latitude
            + "&longitude=" + location.longitude
            + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m"
            + "&timezone=auto";

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Weather request failed.");
        }

        String body = response.body();
        Matcher currentMatcher = CURRENT_BLOCK.matcher(body);
        if (!currentMatcher.find()) {
            throw new IOException("Unexpected weather response.");
        }

        String current = currentMatcher.group(1);
        return new WeatherSnapshot(
            extractString(current, CURRENT_TIME, ""),
            extractDouble(current, TEMPERATURE),
            extractDouble(current, FEELS_LIKE),
            extractDouble(current, HUMIDITY),
            extractDouble(current, WIND_SPEED),
            extractDouble(current, PRECIPITATION),
            extractInt(current, WEATHER_CODE)
        );
    }

    private static String extractString(String text, Pattern pattern, String defaultValue) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : defaultValue;
    }

    private static double extractDouble(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return 0;
        }
        return Double.parseDouble(matcher.group(1));
    }

    private static int extractInt(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return 0;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private static String weatherDescription(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2, 3 -> "Partly cloudy";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 71, 73, 75 -> "Snow";
            case 80, 81, 82 -> "Rain showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown";
        };
    }

    private record Location(String name, String country, double latitude, double longitude) {
    }

    private record WeatherSnapshot(
        String time,
        double temperatureC,
        double feelsLikeC,
        double humidity,
        double windSpeedKmh,
        double precipitationMm,
        int weatherCode
    ) {
    }
}
