package practicum.taskmanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.task.Epic;
import practicum.task.Subtask;
import practicum.task.Task;
import practicum.task.TaskType;
import practicum.task.serializer.CsvSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

class FileBackedTaskManagerTest {
    static TaskManager taskManager;
    static String fileName;

    @BeforeEach
    void beforeEach() throws IOException {
        fileName = File.createTempFile("test", "csv").getAbsolutePath();
        taskManager = new FileBackedTaskManager(fileName);
    }

    static void checkFileIsEmpty() throws IOException {

        try (FileReader fileReader = new FileReader(fileName, StandardCharsets.UTF_8)) {

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String header = bufferedReader.readLine();

            Assertions.assertEquals("id,type,name,status,description,epic", header, "Некорретный заголовок");

            Assertions.assertFalse(bufferedReader.ready(), "Файл не пустой");
        }
    }

    static void checkRecord(Task task, TaskType taskType) throws IOException {
        String expectedString = "";
        String storedRecord = "";
        try (FileReader fileReader = new FileReader(fileName, StandardCharsets.UTF_8)) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String header = bufferedReader.readLine();

            Assertions.assertEquals("id,type,name,status,description,epic", header, "Некорретный заголовок");

            while (bufferedReader.ready()) {
                storedRecord = bufferedReader.readLine();
            }

            String formatString = "%d,%s,%s,%s,%s,";
            if (taskType == TaskType.SUBTASK) {
                formatString += "%s";
                expectedString = String.format(formatString,
                        task.getId(),
                        taskType,
                        task.getName(),
                        task.getStatus(),
                        task.getDescription(),
                        ((Subtask) task).getEpicId()
                );
            } else {
                expectedString = String.format(formatString,
                        task.getId(),
                        taskType,
                        task.getName(),
                        task.getStatus(),
                        task.getDescription()
                );
            }
        }

        Assertions.assertEquals(expectedString, storedRecord, "Задача сохранена некорректно");
    }

    @Test
    void createTask() throws IOException {
        Task task = new Task("task1", TaskStatus.NEW, "task1 description");
        int taskId = taskManager.createTask(task);
        task = taskManager.getTaskById(taskId);

        checkRecord(task, TaskType.TASK);
    }

    @Test
    void updateTask() throws IOException {
        Task task = new Task("task1", TaskStatus.NEW, "task1 description");
        int taskId = taskManager.createTask(task);

        task = taskManager.getTaskById(taskId);
        task.setDescription("updated description");

        taskManager.updateTask(task);

        checkRecord(task, TaskType.TASK);
    }

    @Test
    void deleteTaskById() throws IOException {
        Task task = new Task("task1", TaskStatus.NEW, "task1 description");
        int taskId = taskManager.createTask(task);
        taskManager.deleteTaskById(taskId);
        checkFileIsEmpty();
    }

    @Test
    void clearTasks() throws IOException {
        Task task = new Task("task1", TaskStatus.NEW, "task1 description");
        taskManager.createTask(task);
        taskManager.clearTasks();
        checkFileIsEmpty();
    }

    @Test
    void clearEpics() throws IOException {
        Epic task = new Epic("task1", "task1 description");
        taskManager.createEpic(task);
        taskManager.clearEpics();
        checkFileIsEmpty();
    }

    @Test
    void createEpic() throws IOException {
        Epic task = new Epic("task1", "task1 description");
        int taskId = taskManager.createEpic(task);
        task = taskManager.getEpicById(taskId);

        checkRecord(task, TaskType.EPIC);
    }

    @Test
    void updateEpic() throws IOException {
        Epic task = new Epic("task1", "task1 description");
        int taskId = taskManager.createEpic(task);

        task = taskManager.getEpicById(taskId);
        task.setDescription("updated description");

        taskManager.updateEpic(task);

        checkRecord(task, TaskType.EPIC);
    }

    @Test
    void deleteEpicById() throws IOException {
        Epic task = new Epic("task1", "task1 description");
        int id = taskManager.createEpic(task);
        taskManager.deleteEpicById(id);
        checkFileIsEmpty();
    }

    @Test
    void clearSubtasks() throws IOException {
        Epic epic = new Epic("epic", "epic description");
        int epicId = taskManager.createEpic(epic);
        epic = taskManager.getEpicById(epicId);

        Subtask task = new Subtask("subtask", TaskStatus.IN_PROGRESS, epicId, "st description");
        taskManager.createSubtask(task);

        taskManager.clearSubtasks();

        checkRecord(epic, TaskType.EPIC);
    }

    @Test
    void createSubtask() throws IOException {
        Epic epic = new Epic("epic", "epic description");
        int epicId = taskManager.createEpic(epic);
        epic = taskManager.getEpicById(epicId);

        Subtask task = new Subtask("subtask", TaskStatus.IN_PROGRESS, epicId, "st description");
        int taskId = taskManager.createSubtask(task);
        task = taskManager.getSubtaskById(taskId);

        checkRecord(task, TaskType.SUBTASK);
    }

    @Test
    void updateSubtask() throws IOException {
        Epic epic = new Epic("epic", "epic description");
        int epicId = taskManager.createEpic(epic);
        epic = taskManager.getEpicById(epicId);

        Subtask task = new Subtask("subtask", TaskStatus.IN_PROGRESS, epicId, "st description");
        int taskId = taskManager.createSubtask(task);
        task = taskManager.getSubtaskById(taskId);
        task.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(task);

        checkRecord(task, TaskType.SUBTASK);
    }

    @Test
    void deleteSubtaskById() throws IOException {
        Epic epic = new Epic("epic", "epic description");
        int epicId = taskManager.createEpic(epic);
        epic = taskManager.getEpicById(epicId);

        Subtask task = new Subtask("subtask", TaskStatus.IN_PROGRESS, epicId, "st description");
        int taskId = taskManager.createSubtask(task);
        taskManager.deleteSubtaskById(taskId);

        checkRecord(epic, TaskType.EPIC);
    }

    @Test
    void loadFromFile() throws IOException {

        Writer writer = new FileWriter(fileName, StandardCharsets.UTF_8);

        Task task = new Task(10, "test task", TaskStatus.NEW, "task descriprion");
        Epic epic = new Epic(20, "test epic", TaskStatus.IN_PROGRESS, "epic descriprion");
        Subtask subtask = new Subtask(22, "test subtask", TaskStatus.DONE, epic.getId(), "epic descriprion");

        writer.write("id,type,name,status,description,epic" + '\n');
        writer.write(CsvSerializer.toCsvString(task, TaskType.TASK) + '\n');
        writer.write(CsvSerializer.toCsvString(epic, TaskType.EPIC) + '\n');
        writer.write(CsvSerializer.toCsvString(subtask, TaskType.SUBTASK));
        writer.close();

        taskManager = FileBackedTaskManager.loadFromFile(fileName);
        Task loadedTask = taskManager.getTaskById(task.getId());
        Epic loadedEpic = taskManager.getEpicById(epic.getId());
        Subtask loadedSubtask = taskManager.getSubtaskById(subtask.getId());

        boolean compareTaskResult = TaskManagerTest.compareTasksByFields(task, loadedTask);
        boolean compareEpicResult = TaskManagerTest.compareTasksByFields(epic, loadedEpic);
        boolean compareSubtaskResult = TaskManagerTest.compareTasksByFields(subtask, loadedSubtask);

        Assertions.assertTrue(compareTaskResult, "Не совпадают сохраненная и восстановленная Task");
        Assertions.assertTrue(compareEpicResult, "Не совпадают сохраненная и восстановленная Epic");
        Assertions.assertTrue(compareSubtaskResult, "Не совпадают сохраненная и восстановленная Subtask");
    }
}