import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public class App {
    private static final int PORT = 8080;
    private static final Path WEB_ROOT = Path.of("web");

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        WeatherService weatherService = new WeatherService();

        server.createContext("/api/weather", exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                JsonResponse.send(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String city = QueryParams.get(exchange.getRequestURI().getRawQuery(), "city");
            if (city == null || city.isBlank()) {
                JsonResponse.send(exchange, 400, "{\"error\":\"Please provide a city name.\"}");
                return;
            }

            try {
                String weatherJson = weatherService.getWeatherByCity(city.trim());
                JsonResponse.send(exchange, 200, weatherJson);
            } catch (IllegalArgumentException ex) {
                JsonResponse.send(exchange, 404, JsonResponse.error(ex.getMessage()));
            } catch (Exception ex) {
                JsonResponse.send(exchange, 500, JsonResponse.error("Unable to fetch weather data right now."));
            }
        });

        server.createContext("/", exchange -> {
            addCorsHeaders(exchange);
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) {
                path = "/index.html";
            }

            Path filePath = WEB_ROOT.resolve(path.substring(1)).normalize();
            if (!filePath.startsWith(WEB_ROOT) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
                byte[] response = "404 Not Found".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(404, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
                return;
            }

            byte[] content = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().add("Content-Type", ContentTypes.forPath(filePath));
            exchange.sendResponseHeaders(200, content.length);
            exchange.getResponseBody().write(content);
            exchange.close();
        });

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Weather app running at http://localhost:" + PORT);
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
}
