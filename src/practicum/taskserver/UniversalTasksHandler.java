package practicum.taskserver;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import practicum.task.Epic;
import practicum.task.Subtask;
import practicum.task.Task;
import practicum.task.TaskType;
import practicum.taskmanager.OverlapTasksException;
import practicum.taskmanager.ResultOfDeletion;
import practicum.taskmanager.TaskManager;
import practicum.taskmanager.UpdateResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class UniversalTasksHandler extends BaseHttpHandler implements HttpHandler {
    final String basePath;
    final TaskManager taskManager;
    final TaskType taskType;
    final Gson gson;

    public UniversalTasksHandler(TaskManager taskManager, TaskType taskType) {
        this.taskManager = taskManager;
        this.gson = GsonForTasks.getGson();
        this.taskType = taskType;
        this.basePath = HttpTaskServer.getPathForTaskType(taskType);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path == null
                || path.isEmpty()
                || path.contains("//")
                || !path.startsWith(basePath)
                || path.endsWith("/")
        ) {
            sendNotFound(exchange);
            return;
        }

        try {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    processGET(exchange);
                    break;
                case "POST":
                    processPOST(exchange);
                    break;
                case "DELETE":
                    processDELETE(exchange);
                    break;
                default:
                    sendDoesntAllow(exchange);
            }
        } catch (Exception exception) {
            sendInternalServerError(exchange);
        }
    }

    void processGET(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals(basePath)) {
            handleGetTasksList(exchange);
        } else if (path.startsWith(basePath + "/")) {
            if (taskType == TaskType.EPIC && path.endsWith("/subtasks")) {
                handleGetSubtasksOfEpic(exchange);
                return;
            }
            handleGetTaskById(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    void processPOST(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String json = new String(exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);

        if (!path.equals(basePath)) {
            sendNotFound(exchange);
            return;
        }

        Task task = switch (taskType) {
            case TASK -> gson.fromJson(json, Task.class);
            case SUBTASK -> gson.fromJson(json, Subtask.class);
            case EPIC -> gson.fromJson(json, Epic.class);
        };

        if (task.getId() == 0) {
            handleCreateTask(exchange, task);
        } else {
            if (taskType == TaskType.EPIC) {
                sendBadRequest(exchange);
                return;
            }
            handleUpdateTask(exchange, task);
        }
    }

    void processDELETE(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (!path.startsWith(basePath + "/")) {
            sendNotFound(exchange);
            return;
        }
        handleDeleteTaskById(exchange);
    }

    void handleGetTasksList(HttpExchange exchange) throws IOException {
        List<? extends Task> tasksList = switch (taskType) {
            case TASK -> taskManager.getTasksList();
            case SUBTASK -> taskManager.getSubtasksList();
            case EPIC -> taskManager.getEpicsList();
        };
        sendJson(exchange, gson.toJson(tasksList));
    }

    void handleGetTaskById(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String idStr = path.substring((basePath + "/").length());

        int taskId;
        try {
            taskId = Integer.parseInt(idStr);
        } catch (NumberFormatException exception) {
            sendBadRequest(exchange);
            return;
        }

        Optional<? extends Task> optional = switch (taskType) {
            case TASK -> taskManager.getTaskById(taskId);
            case SUBTASK -> taskManager.getSubtaskById(taskId);
            case EPIC -> taskManager.getEpicById(taskId);
        };

        if (optional.isPresent()) {
            String responseBody = gson.toJson(optional.get());
            sendJson(exchange, responseBody);
        } else {
            sendNotFound(exchange);
        }
    }

    void handleGetSubtasksOfEpic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        String subPath = path.substring(0, path.indexOf("/subtasks"));
        String epicIdStr = subPath.substring((HttpTaskServer.EPICS_PATH + "/").length());

        int epicId;
        try {
            epicId = Integer.parseInt(epicIdStr);
        } catch (NumberFormatException exception) {
            sendBadRequest(exchange);
            return;
        }

        Optional<Epic> optional = taskManager.getEpicById(epicId);
        if (optional.isEmpty()) {
            sendNotFound(exchange);
            return;
        }
        sendJson(exchange, gson.toJson(taskManager.getSubtasksOfEpic(epicId)));
    }

    private void handleUpdateTask(HttpExchange exchange, Task task) throws IOException {
        UpdateResult updateResult;
        try {
            updateResult = switch (taskType) {
                case TASK -> taskManager.updateTask(task);
                case SUBTASK -> taskManager.updateSubtask((Subtask) task);
                case EPIC -> throw new IllegalArgumentException("Doesn't allow for epic");
            };
        } catch (OverlapTasksException exception) {
            sendHasOverlaps(exchange);
            return;
        }

        if (updateResult == UpdateResult.SUCCESS) {
            sendText(exchange, "ok", 201);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleCreateTask(HttpExchange exchange, Task task) throws IOException {
        int taskId;
        try {
            taskId = switch (taskType) {
                case TASK -> taskManager.createTask(task);
                case SUBTASK -> taskManager.createSubtask((Subtask) task);
                case EPIC -> taskManager.createEpic((Epic) task);
            };
        } catch (OverlapTasksException exception) {
            sendHasOverlaps(exchange);
            return;
        }

        if (taskId == -1 && taskType == TaskType.SUBTASK) {
            sendNotFound(exchange);
        } else {
            sendText(exchange, Integer.toString(taskId), 201);
        }
    }

    void handleDeleteTaskById(HttpExchange exchange) throws IOException {
        String taskIdString = exchange.getRequestURI()
                .getPath()
                .substring((basePath + "/").length());

        int taskId;
        try {
            taskId = Integer.parseInt(taskIdString);
        } catch (NumberFormatException exception) {
            sendBadRequest(exchange);
            return;
        }

        ResultOfDeletion resultOfDeletion = switch (taskType) {
            case TASK -> taskManager.deleteTaskById(taskId);
            case SUBTASK -> taskManager.deleteSubtaskById(taskId);
            case EPIC -> taskManager.deleteEpicById(taskId);
        };

        if (resultOfDeletion == ResultOfDeletion.SUCCESS) {
            sendText(exchange, "ok", 200);
        } else {
            sendNotFound(exchange);
        }
    }
}
