import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class QueryParams {
    public static String get(String query, String key) {
        if (query == null || query.isBlank()) {
            return null;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String currentKey = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            if (!key.equals(currentKey)) {
                continue;
            }
            String value = parts.length > 1 ? parts[1] : "";
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }

        return null;
    }
}
