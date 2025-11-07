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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    static String fileName;

    @Override
    protected FileBackedTaskManager createManager() {
        return new FileBackedTaskManager();
    }

    @BeforeEach
    void beforeEachFileBackedTaskManager() throws IOException {
        fileName = File.createTempFile("test", "csv").getAbsolutePath();
        taskManager = new FileBackedTaskManager(fileName);
    }

    static void checkFileIsEmpty() throws IOException {

        try (FileReader fileReader = new FileReader(fileName, StandardCharsets.UTF_8)) {

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String header = bufferedReader.readLine();

            Assertions.assertEquals("id,type,name,status,description,epic,startTime,duration,endTime",
                    header, "Некорретный заголовок");

            Assertions.assertFalse(bufferedReader.ready(), "Файл не пустой");
        }
    }

    static void checkRecord(Task task, TaskType taskType) throws IOException {
        String expectedString = "";
        String storedRecord = "";
        try (FileReader fileReader = new FileReader(fileName, StandardCharsets.UTF_8)) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String header = bufferedReader.readLine();

            Assertions.assertEquals("id,type,name,status,description,epic,startTime,duration,endTime",
                    header, "Некорретный заголовок");

            while (bufferedReader.ready()) {
                storedRecord = bufferedReader.readLine();
            }

            String formatString = "%d,%s,%s,%s,%s,%s,";
            if (taskType == TaskType.SUBTASK) {
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
                        task.getDescription(),
                        ""
                );
            }

            if (task.getStartTime() != null) {
                expectedString += String.format("%s,", task.getStartTime());
            } else {
                expectedString += ",";
            }

            if (task.getDuration() != null) {
                expectedString += String.format("%d,", task.getDuration().toMinutes());
            } else {
                expectedString += ",";
            }

            if (task.getEndTime() != null) {
                expectedString += String.format("%s", task.getEndTime());
            }
        }

        Assertions.assertEquals(expectedString, storedRecord, "Задача сохранена некорректно");
    }

    @Test
    void createTaskAndCheckStoring() {
        Task task = new Task("task1", TaskStatus.NEW, "task1 description");
        int taskId = taskManager.createTask(task);

        Task storedTask = getTaskById(taskId);

        assertDoesNotThrow(() ->
                        checkRecord(storedTask, TaskType.TASK)
                , "Ошибка при работе с файлом.");
    }

    @Test
    void updateTaskAndCheckStoring() {
        Task task = new Task("task1", TaskStatus.NEW, "task1 description");
        int taskId = taskManager.createTask(task);

        Task storedTask = getTaskById(taskId);
        storedTask.setDescription("updated description");

        taskManager.updateTask(storedTask);

        assertDoesNotThrow(() ->
                        checkRecord(storedTask, TaskType.TASK)
                , "Ошибка при работе с файлом.");
    }

    @Test
    void deleteTaskAndCheckStoring() {
        Task task = new Task("task1", TaskStatus.NEW, "task1 description");
        int taskId = taskManager.createTask(task);
        taskManager.deleteTaskById(taskId);

        assertDoesNotThrow(FileBackedTaskManagerTest::checkFileIsEmpty, "Исключение при выполнении проверки");
    }

    @Test
    void clearTasksAndCheckStoring() {
        Task task = new Task("task1", TaskStatus.NEW, "task1 description");
        taskManager.createTask(task);
        taskManager.clearTasks();

        assertDoesNotThrow(FileBackedTaskManagerTest::checkFileIsEmpty, "Исключение при выполнении проверки");
    }

    @Test
    void clearEpicsAndCheckStoring() {
        Epic task = new Epic("task1", "task1 description");
        taskManager.createEpic(task);
        taskManager.clearEpics();

        assertDoesNotThrow(FileBackedTaskManagerTest::checkFileIsEmpty, "Исключение при выполнении проверки");
    }

    @Test
    void createEpicAndCheckStoring() {
        Epic task = new Epic("task1", "task1 description");
        int taskId = taskManager.createEpic(task);
        Epic storedTask = getEpicById(taskId);

        assertDoesNotThrow(() -> checkRecord(storedTask, TaskType.EPIC), "Исключение при выполнении проверки");
    }

    @Test
    void updateEpicAndCheckStoring() {
        Epic task = new Epic("task1", "task1 description");
        int taskId = taskManager.createEpic(task);

        Epic storedTask = getEpicById(taskId);
        storedTask.setDescription("updated description");

        taskManager.updateEpic(storedTask);

        assertDoesNotThrow(() -> checkRecord(storedTask, TaskType.EPIC), "Исключение при выполнении проверки");
    }

    @Test
    void deleteEpicByIdAndCheckStoring() {
        Epic task = new Epic("task1", "task1 description");
        int id = taskManager.createEpic(task);
        taskManager.deleteEpicById(id);

        assertDoesNotThrow(FileBackedTaskManagerTest::checkFileIsEmpty, "Исключение при выполнении проверки");
    }

    @Test
    void clearSubtasksAndCheckStoring() {
        Epic epic = new Epic("epic", "epic description");
        int epicId = taskManager.createEpic(epic);
        Epic storedEpic = getEpicById(epicId);

        Subtask task = new Subtask("subtask", TaskStatus.IN_PROGRESS, epicId, "st description");
        taskManager.createSubtask(task);

        taskManager.clearSubtasks();

        assertDoesNotThrow(() -> checkRecord(storedEpic, TaskType.EPIC),
                "Исключение при выполнении проверки");
    }

    @Test
    void createSubtaskAndCheckStoring() {
        Epic epic = new Epic("epic", "epic description");
        int epicId = taskManager.createEpic(epic);
        taskManager.getEpicById(epicId);

        Subtask task = new Subtask("subtask", TaskStatus.IN_PROGRESS, epicId, "st description");
        int taskId = taskManager.createSubtask(task);
        Subtask storedSubtask = getSubtaskById(taskId);

        assertDoesNotThrow(() -> checkRecord(storedSubtask, TaskType.SUBTASK),
                "Исключение при выполнении проверки");
    }

    @Test
    void updateSubtaskAndCheckStoring() {
        Epic epic = new Epic("epic", "epic description");
        int epicId = taskManager.createEpic(epic);
        taskManager.getEpicById(epicId);

        Subtask task = new Subtask("subtask", TaskStatus.IN_PROGRESS, epicId, "st description");
        int taskId = taskManager.createSubtask(task);
        Subtask storedSubtask = getSubtaskById(taskId);
        storedSubtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(storedSubtask);

        assertDoesNotThrow(() -> checkRecord(storedSubtask, TaskType.SUBTASK),
                "Исключение при выполнении проверки");
    }

    @Test
    void deleteSubtaskByIdAndCheckStoring() {
        Epic epic = new Epic("epic", "epic description");
        int epicId = taskManager.createEpic(epic);
        Epic storedEpic = getEpicById(epicId);

        Subtask task = new Subtask("subtask", TaskStatus.IN_PROGRESS, epicId, "st description");
        int taskId = taskManager.createSubtask(task);
        taskManager.deleteSubtaskById(taskId);

        assertDoesNotThrow(() -> checkRecord(storedEpic, TaskType.EPIC),
                "Исключение при выполнении проверки");
    }

    @Test
    void loadFromFile() {

        Writer writer;

        try {
            writer = new FileWriter(fileName, StandardCharsets.UTF_8);
        } catch (IOException e) {
            assertDoesNotThrow(() -> {
                throw new RuntimeException(e);
            }, e.getMessage());
            return;
        }

        Task task = new Task(10, "test task", TaskStatus.NEW, "task descriprion");
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofDays(1));

        Epic epic = new Epic(20, "test epic", TaskStatus.IN_PROGRESS, "epic descriprion");
        epic.setStartTime(LocalDateTime.now());
        epic.setDuration(Duration.ofDays(1));
        epic.setEndTime(epic.getStartTime().plusDays(1));

        Subtask subtask = new Subtask(22, "test subtask", TaskStatus.DONE, epic.getId(), "epic descriprion");
        subtask.setStartTime(LocalDateTime.now().minusDays(1));
        subtask.setDuration(Duration.ofDays(1));
        epic.addSubtaskId(22);

        try {
            writer.write("id,type,name,status,description,epic,startTime,duration,endTime" + '\n');
            writer.write(CsvSerializer.toCsvString(task, TaskType.TASK) + '\n');
            writer.write(CsvSerializer.toCsvString(epic, TaskType.EPIC) + '\n');
            writer.write(CsvSerializer.toCsvString(subtask, TaskType.SUBTASK));
            writer.close();
        } catch (IOException e) {
            assertDoesNotThrow(() -> {
                throw new RuntimeException(e);
            }, e.getMessage());
        }

        assertThrows(IOException.class,
                () -> FileBackedTaskManager.loadFromFile(fileName + "wrong"),
                "При загрузке из несуществующего файла должно быть исключение");
        try {
            taskManager = (FileBackedTaskManager) FileBackedTaskManager.loadFromFile(fileName);
        } catch (IOException e) {
            assertDoesNotThrow(() -> {
                throw new RuntimeException(e);
            }, e.getMessage());
        }

        Task loadedTask = getTaskById(task.getId());
        Epic loadedEpic = getEpicById(epic.getId());
        Subtask loadedSubtask = getSubtaskById(subtask.getId());

        boolean compareTaskResult = TaskManagerTest.compareTasksByFields(task, loadedTask);
        boolean compareEpicResult = TaskManagerTest.compareTasksByFields(epic, loadedEpic);
        boolean compareSubtaskResult = TaskManagerTest.compareTasksByFields(subtask, loadedSubtask);

        Assertions.assertTrue(compareTaskResult, "Не совпадают сохраненная и восстановленная Task");
        Assertions.assertTrue(compareEpicResult, "Не совпадают сохраненная и восстановленная Epic");
        Assertions.assertTrue(compareSubtaskResult, "Не совпадают сохраненная и восстановленная Subtask");

        List<Task> sortedList = taskManager.getPrioritizedTasks();
        Assertions.assertEquals(2, sortedList.size(), "Неправильный размер getPrioritizedTasks");
        assertTrue(TaskManagerTest.compareTasksByFields(loadedSubtask, sortedList.get(0)), "Неправильный порядок задач");
        assertTrue(TaskManagerTest.compareTasksByFields(loadedTask, sortedList.get(1)), "Неправильный порядок задач");
    }

}