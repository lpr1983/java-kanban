package practicum.taskserver;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    void sendJson(HttpExchange exchange, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        sendResponse(exchange, 200, body);
    }

    void sendText(HttpExchange exchange, String body, int code) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        sendResponse(exchange, code, body);
    }

    void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "Not found", 404);
    }

    void sendBadRequest(HttpExchange exchange) throws IOException {
        sendText(exchange, "Bad request", 400);
    }

    void sendHasOverlaps(HttpExchange exchange) throws IOException {
        sendText(exchange, "Has overlaps", 406);
    }

    void sendInternalServerError(HttpExchange exchange) throws IOException {
        sendText(exchange, "Internal server error", 500);
    }

    void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        exchange.close();
    }
}
