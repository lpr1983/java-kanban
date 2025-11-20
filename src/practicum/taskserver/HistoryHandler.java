package practicum.taskserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import practicum.taskmanager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    final TaskManager taskManager;
    final Gson gson;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = GsonForTasks.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!HttpTaskServer.HISTORY_PATH.equals(path)) {
            sendNotFound(exchange);
            return;
        }

        String method = exchange.getRequestMethod();
        try {
            if (method.equals("GET")) {
                sendJson(exchange, gson.toJson(taskManager.getHistory()));
            } else {
                sendDoesntAllow(exchange);
            }
        } catch (Exception exception) {
            sendInternalServerError(exchange);
        }
    }
}
