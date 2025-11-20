package practicum.taskserver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.task.Epic;
import practicum.task.Subtask;
import practicum.task.Task;
import practicum.task.TaskType;
import practicum.taskmanager.InMemoryTaskManager;
import practicum.taskmanager.TaskManager;
import practicum.taskmanager.TaskStatus;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    HttpTaskServer taskServer;
    TaskManager taskManager;
    Gson gson = GsonForTasks.getGson();

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManager();
        taskServer = assertDoesNotThrow(() -> new HttpTaskServer(taskManager));
        taskServer.start();
    }

    @AfterEach
    void afterEach() {
        taskServer.stop();
    }

    @Test
    void invalidPathsShouldReturnErrors() {

        HttpResponse<String> response = sendRequest("http://localhost:8080/wrong", "GET", "");
        assertEquals(404, response.statusCode());

        for (TaskType taskType : TaskType.values()) {
            String basePath = HttpTaskServer.getPathForTaskType(taskType);

            response = sendRequest("http://localhost:8080" + basePath + "//123", "GET", "");
            assertEquals(404, response.statusCode());

            response = sendRequest("http://localhost:8080" + basePath + "/", "GET", "");
            assertEquals(404, response.statusCode());

            response = sendRequest("http://localhost:8080" + basePath + "/test", "GET", "");
            assertEquals(400, response.statusCode());

            response = sendRequest("http://localhost:8080" + basePath + "/123", "POST", "");
            assertEquals(404, response.statusCode());

            response = sendRequest("http://localhost:8080" + basePath, "PATCH", "");
            assertEquals(405, response.statusCode());
        }
    }

    @Test
    void getTasksList() {
        Task taskWithoutTime = new Task("task", TaskStatus.NEW, "");
        taskManager.createTask(taskWithoutTime);

        Task taskWithTime = createNewTaskWithTime("task with time");
        taskManager.createTask(taskWithTime);

        List<Task> expectedList = taskManager.getTasksList();

        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/tasks", "GET", "");
        assertEquals(200, httpResponse.statusCode());

        String responseBody = httpResponse.body();
        List<Task> tasksList = gson.fromJson(responseBody, new TaskListTypeToken().getType());

        assertEquals(expectedList, tasksList, "Не совпадает с ожидаемым списков");
    }

    @Test
    void getSubtasksList() {
        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("subtask", TaskStatus.NEW, epicId, "");
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofDays(1));
        taskManager.createSubtask(subtask);

        List<Subtask> expectedList = taskManager.getSubtasksList();

        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/subtasks", "GET", "");
        assertEquals(200, httpResponse.statusCode());

        String responseBody = httpResponse.body();
        List<Subtask> tasksList = gson.fromJson(responseBody, new SubtaskListTypeToken().getType());

        assertEquals(expectedList, tasksList, "Не совпадает с ожидаемым списков");
    }

    @Test
    void getEpicsList() {
        Epic epic = new Epic("epic", "");
        taskManager.createEpic(epic);

        List<Epic> expectedList = taskManager.getEpicsList();

        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/epics", "GET", "");
        assertEquals(200, httpResponse.statusCode());

        String responseBody = httpResponse.body();
        List<Subtask> tasksList = gson.fromJson(responseBody, new EpicListTypeToken().getType());

        assertEquals(expectedList, tasksList, "Не совпадает с ожидаемым списков");
    }

    @Test
    void getTaskById() {
        Task task = createNewTaskWithTime("task");
        int taskId = taskManager.createTask(task);
        task = taskManager.getTaskById(taskId).get();

        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/tasks/" + taskId, "GET", "");
        assertEquals(200, httpResponse.statusCode());

        String responseBody = httpResponse.body();
        Task returnedTask = gson.fromJson(responseBody, Task.class);

        assertEquals(task, returnedTask, "Не совпадает с ожидаемой задачей");

        httpResponse = sendRequest("http://localhost:8080/tasks/999", "GET", "");
        assertEquals(404, httpResponse.statusCode());

        httpResponse = sendRequest("http://localhost:8080/tasks/wrongId", "GET", "");
        assertEquals(400, httpResponse.statusCode());
    }

    @Test
    void getSubtaskById() {
        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("subtask", TaskStatus.NEW, epicId, "");
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofDays(1));
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask expectedSubtask = assertDoesNotThrow(() -> taskManager.getSubtaskById(subtaskId).get());

        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/subtasks/" + subtaskId, "GET", "");
        assertEquals(200, httpResponse.statusCode());

        String responseBody = httpResponse.body();
        Task returnedTask = gson.fromJson(responseBody, Subtask.class);

        assertEquals(expectedSubtask, returnedTask, "Не совпадает с ожидаемой задачей");

        httpResponse = sendRequest("http://localhost:8080/subtasks/999", "GET", "");
        assertEquals(404, httpResponse.statusCode());

        httpResponse = sendRequest("http://localhost:8080/subtasks/wrongId", "GET", "");
        assertEquals(400, httpResponse.statusCode());
    }

    @Test
    void getEpicById() {
        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Epic expectedEpic = assertDoesNotThrow(() -> taskManager.getEpicById(epicId).get());

        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/epics/" + epicId, "GET", "");
        assertEquals(200, httpResponse.statusCode());

        String responseBody = httpResponse.body();
        Epic returnedEpic = gson.fromJson(responseBody, Epic.class);

        assertEquals(expectedEpic, returnedEpic, "Не совпадает с ожидаемой задачей");

        httpResponse = sendRequest("http://localhost:8080/epics/999", "GET", "");
        assertEquals(404, httpResponse.statusCode());

        httpResponse = sendRequest("http://localhost:8080/epics/wrongId", "GET", "");
        assertEquals(400, httpResponse.statusCode());
    }

    @Test
    void createTask() {
        Task task = createNewTaskWithTime("task1");

        String requestBody = gson.toJson(task);
        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/tasks", "POST", requestBody);

        assertEquals(201, httpResponse.statusCode());

        int taskId = assertDoesNotThrow(() -> Integer.parseInt(httpResponse.body()));
        assertDoesNotThrow(() -> taskManager.getTaskById(taskId).get());

        Task taskWithOverlap = createNewTaskWithTime("task2");

        requestBody = gson.toJson(taskWithOverlap);
        HttpResponse httpResponse2 = sendRequest("http://localhost:8080/tasks", "POST", requestBody);
        assertEquals(406, httpResponse2.statusCode());
    }

    @Test
    void createSubtask() {
        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("subtask", TaskStatus.NEW, epicId, "");
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofDays(1));

        String requestBody = gson.toJson(subtask);
        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/subtasks", "POST", requestBody);

        assertEquals(201, httpResponse.statusCode());

        int subtaskId = assertDoesNotThrow(() -> Integer.parseInt(httpResponse.body()));
        assertDoesNotThrow(() -> taskManager.getSubtaskById(subtaskId).get());

        Subtask subtaskWithOverlap = new Subtask("subtask with overlap", TaskStatus.NEW, epicId, "");
        subtaskWithOverlap.setStartTime(LocalDateTime.now());
        subtaskWithOverlap.setDuration(Duration.ofDays(1));

        requestBody = gson.toJson(subtaskWithOverlap);
        HttpResponse httpResponse2 = sendRequest("http://localhost:8080/subtasks", "POST", requestBody);
        assertEquals(406, httpResponse2.statusCode());
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("epic", "");

        String requestBody = gson.toJson(epic);
        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/epics", "POST", requestBody);

        assertEquals(201, httpResponse.statusCode());

        int epicId = assertDoesNotThrow(() -> Integer.parseInt(httpResponse.body()));
        assertDoesNotThrow(() -> taskManager.getEpicById(epicId).get());
    }

    @Test
    void updateTask() {
        Task task = createNewTaskWithTime("task");
        int taskId = taskManager.createTask(task);

        Task createdTask = assertDoesNotThrow(() -> taskManager.getTaskById(taskId).get());
        createdTask.setDescription("Обновление");

        String requestBody = gson.toJson(createdTask);
        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/tasks", "POST", requestBody);

        assertEquals(201, httpResponse.statusCode());

        Task updatedTask = assertDoesNotThrow(() -> taskManager.getTaskById(taskId).get());
        assertEquals(createdTask.getDescription(), updatedTask.getDescription());

        Task taskWithOverlap = createNewTaskWithTime("task2");
        taskWithOverlap.setStartTime(LocalDateTime.now().plusDays(2));
        int taskWithOverlapId = taskManager.createTask(taskWithOverlap);
        taskWithOverlap = assertDoesNotThrow(() -> taskManager.getTaskById(taskWithOverlapId).get());

        taskWithOverlap.setStartTime(LocalDateTime.now());

        HttpResponse<String> httpResponse2 = sendRequest("http://localhost:8080/tasks", "POST",
                gson.toJson(taskWithOverlap));

        assertEquals(406, httpResponse2.statusCode());

        createdTask.setId(999);
        httpResponse = sendRequest("http://localhost:8080/tasks", "POST", gson.toJson(createdTask));
        assertEquals(404, httpResponse.statusCode());
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("subtask", TaskStatus.NEW, epicId, "");
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofDays(1));
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask createdSubtask = assertDoesNotThrow(() -> taskManager.getSubtaskById(subtaskId).get());
        createdSubtask.setDescription("Обновление");

        String requestBody = gson.toJson(createdSubtask);
        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/subtasks", "POST", requestBody);

        assertEquals(201, httpResponse.statusCode());

        Subtask updatedSubtask = assertDoesNotThrow(() -> taskManager.getSubtaskById(subtaskId).get());
        assertEquals(createdSubtask.getDescription(), updatedSubtask.getDescription());

        Subtask subtaskWithOverlap = new Subtask("subtask with overlap", TaskStatus.NEW, epicId, "");
        subtaskWithOverlap.setStartTime(LocalDateTime.now().plusDays(2));
        subtaskWithOverlap.setDuration(Duration.ofDays(1));
        int subtaskWithOverlapId = taskManager.createSubtask(subtaskWithOverlap);

        subtaskWithOverlap = assertDoesNotThrow(() -> taskManager.getSubtaskById(subtaskWithOverlapId).get());
        subtaskWithOverlap.setStartTime(LocalDateTime.now());

        HttpResponse<String> httpResponse2 = sendRequest("http://localhost:8080/subtasks", "POST",
                gson.toJson(subtaskWithOverlap));

        assertEquals(406, httpResponse2.statusCode());

        createdSubtask.setId(999);
        httpResponse = sendRequest("http://localhost:8080/subtasks", "POST", gson.toJson(createdSubtask));
        assertEquals(404, httpResponse.statusCode());
    }

    @Test
    void updateEpic() {
        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Epic epicToUpdate = assertDoesNotThrow(() -> taskManager.getEpicById(epicId).get());
        epicToUpdate.setDescription("Обновление");

        String requestBody = gson.toJson(epicToUpdate);
        HttpResponse<String> httpResponse = sendRequest("http://localhost:8080/epics", "POST", requestBody);

        assertEquals(400, httpResponse.statusCode());
    }

    @Test
    void deleteTask() {
        Task task = createNewTaskWithTime("task to delete");
        int taskId = taskManager.createTask(task);

        HttpResponse<String> response = sendRequest("http://localhost:8080/tasks/" + taskId, "DELETE", "");
        assertEquals(200, response.statusCode());
        assertFalse(taskManager.getTaskById(taskId).isPresent(), "Задача не должна существовать после удаления");

        response = sendRequest("http://localhost:8080/tasks/" + taskId, "DELETE", "");
        assertEquals(404, response.statusCode());

        response = sendRequest("http://localhost:8080/tasks/abc", "DELETE", "");
        assertEquals(400, response.statusCode());

        response = sendRequest("http://localhost:8080/wrong/123", "DELETE", "");
        assertEquals(404, response.statusCode());
    }

    @Test
    void deleteSubtask() {

        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("subtask", TaskStatus.NEW, epicId, "");
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofDays(1));
        int subtaskId = taskManager.createSubtask(subtask);

        HttpResponse<String> response = sendRequest("http://localhost:8080/subtasks/" + subtaskId, "DELETE", "");
        assertEquals(200, response.statusCode());
        assertFalse(taskManager.getSubtaskById(subtaskId).isPresent(), "Задача не должна существовать после удаления");

        response = sendRequest("http://localhost:8080/subtasks/" + subtaskId, "DELETE", "");
        assertEquals(404, response.statusCode());

        response = sendRequest("http://localhost:8080/subtasks/abc", "DELETE", "");
        assertEquals(400, response.statusCode());

        response = sendRequest("http://localhost:8080/wrong/123", "DELETE", "");
        assertEquals(404, response.statusCode());
    }

    @Test
    void deleteEpic() {

        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        HttpResponse<String> response = sendRequest("http://localhost:8080/epics/" + epicId, "DELETE", "");
        assertEquals(200, response.statusCode());
        assertFalse(taskManager.getEpicById(epicId).isPresent(), "Задача не должна существовать после удаления");

        response = sendRequest("http://localhost:8080/epics/" + epicId, "DELETE", "");
        assertEquals(404, response.statusCode());

        response = sendRequest("http://localhost:8080/epics/abc", "DELETE", "");
        assertEquals(400, response.statusCode());

        response = sendRequest("http://localhost:8080/wrong/123", "DELETE", "");
        assertEquals(404, response.statusCode());
    }

    @Test
    void getHistory() {

        Task task = createNewTaskWithTime("task");
        int taskId = taskManager.createTask(task);

        Task task2 = new Epic("task2", "");
        int taskId2 = taskManager.createTask(task2);

        assertDoesNotThrow( ()->taskManager.getTaskById(taskId).get());
        assertDoesNotThrow( ()->taskManager.getTaskById(taskId2).get());

        List<Task> expectedHistory = taskManager.getHistory();

        HttpResponse<String> response = sendRequest("http://localhost:8080/history", "GET", "");
        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(expectedHistory, history);

        HttpResponse<String> response2 = sendRequest("http://localhost:8080/history/wrong", "GET", "");
        assertEquals(404, response2.statusCode());

        HttpResponse<String> response3 = sendRequest("http://localhost:8080/history", "POST", "");
        assertEquals(405, response3.statusCode());
    }

    @Test
    void getPrioritizedTasks() {

        Task task1 = createNewTaskWithTime("task1");
        taskManager.createTask(task1);

        Task task2 = createNewTaskWithTime("task2");
        task2.setStartTime(task2.getStartTime().plusDays(2));
        taskManager.createTask(task2);

        List<Task> expectedList = taskManager.getPrioritizedTasks();

        HttpResponse<String> response = sendRequest("http://localhost:8080/prioritized", "GET", "");
        assertEquals(200, response.statusCode());

        List<Task> actualList = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        assertEquals(expectedList, actualList);

        HttpResponse<String> response2 = sendRequest("http://localhost:8080/prioritized/wrong", "GET", "");
        assertEquals(404, response2.statusCode());

        HttpResponse<String> response3 = sendRequest("http://localhost:8080/prioritized", "POST", "");
        assertEquals(405, response3.statusCode());
    }


    Task createNewTaskWithTime(String name) {
        Task task = new Task(name, TaskStatus.NEW, "");
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofDays(1));

        return task;
    }

    HttpResponse sendRequest(String path, String method, String body) {

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        URI url = URI.create(path);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .uri(url)
                .header("Accept", "application/json")
                .build();

        return assertDoesNotThrow(() -> client.send(httpRequest, HttpResponse.BodyHandlers.ofString()));
    }

    class TaskListTypeToken extends TypeToken<List<Task>> {
    }

    class SubtaskListTypeToken extends TypeToken<List<Subtask>> {
    }

    class EpicListTypeToken extends TypeToken<List<Epic>> {
    }
}