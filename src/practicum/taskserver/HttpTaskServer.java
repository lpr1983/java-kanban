package practicum.taskserver;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import practicum.task.TaskType;
import practicum.taskmanager.Managers;
import practicum.taskmanager.TaskManager;

public class HttpTaskServer {
    public static final String TASKS_PATH = "/tasks";
    public static final String SUBTASKS_PATH = "/subtasks";
    public static final String EPICS_PATH = "/epics";
    public static final String HISTORY_PATH = "/history";
    public static final String PRIORITIZED_PATH = "/prioritized";
    HttpServer httpServer;
    TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext(HttpTaskServer.TASKS_PATH, new UniversalTasksHandler(taskManager, TaskType.TASK));
        httpServer.createContext(HttpTaskServer.SUBTASKS_PATH, new UniversalTasksHandler(taskManager, TaskType.SUBTASK));
        httpServer.createContext(HttpTaskServer.EPICS_PATH, new UniversalTasksHandler(taskManager, TaskType.EPIC));
        httpServer.createContext(HttpTaskServer.HISTORY_PATH, new HistoryHandler(taskManager));
        httpServer.createContext(HttpTaskServer.PRIORITIZED_PATH, new PrioritizedHandler(taskManager));
    }

    public static String getPathForTaskType(TaskType taskType) {
        return switch (taskType) {
            case TASK -> HttpTaskServer.TASKS_PATH;
            case SUBTASK -> HttpTaskServer.SUBTASKS_PATH;
            case EPIC -> HttpTaskServer.EPICS_PATH;
        };
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer taskServer = new HttpTaskServer(Managers.getDefault());
        taskServer.start();
    }
}