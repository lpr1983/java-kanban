package practicum.taskmanager;

import practicum.task.Epic;
import practicum.task.Subtask;
import practicum.task.Task;
import practicum.task.TaskType;
import practicum.task.serializer.CsvSerializer;
import practicum.task.serializer.ResultOfSerialization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public FileBackedTaskManager(String fileName) {
        this.fileName = fileName;
    }

    public FileBackedTaskManager() {
        this.fileName = "tasks.csv";
    }

    public static TaskManager loadFromFile(String fileName) throws IOException {

        FileBackedTaskManager taskManager = new FileBackedTaskManager(fileName);

        List<Task> tasksBufer = new ArrayList<>();
        List<Epic> epicsBufer = new ArrayList<>();
        List<Subtask> subtasksBufer = new ArrayList<>();

        try (FileReader fileReader = new FileReader(fileName, StandardCharsets.UTF_8)) {

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String currentString = bufferedReader.readLine();

            while (bufferedReader.ready()) {
                currentString = bufferedReader.readLine();

                ResultOfSerialization rs = CsvSerializer.fromCsvString(currentString);
                if (rs.getTaskType() == TaskType.TASK) {
                    tasksBufer.add(rs.getTask());
                } else if (rs.getTaskType() == TaskType.EPIC) {
                    epicsBufer.add((Epic) rs.getTask());
                } else if (rs.getTaskType() == TaskType.SUBTASK) {
                    subtasksBufer.add((Subtask) rs.getTask());
                }
            }
        }

        for (Task task : tasksBufer) {
            taskManager.loadTask(task);
        }

        for (Epic epic : epicsBufer) {
            taskManager.loadEpic(epic);
        }

        for (Subtask subtask : subtasksBufer) {
            taskManager.loadSubtask(subtask);
        }

        return taskManager;
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public int createTask(Task newTask) {
        int result = super.createTask(newTask);
        save();
        return result;
    }

    @Override
    public UpdateResult updateTask(Task task) {
        UpdateResult result = super.updateTask(task);
        save();
        return result;
    }

    @Override
    public ResultOfDeletion deleteTaskById(int id) {
        ResultOfDeletion result = super.deleteTaskById(id);
        save();
        return result;
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public int createEpic(Epic newEpic) {
        int result = super.createEpic(newEpic);
        save();
        return result;
    }

    @Override
    public UpdateResult updateEpic(Epic epic) {
        UpdateResult result = super.updateEpic(epic);
        save();
        return result;
    }

    @Override
    public ResultOfDeletion deleteEpicById(int id) {
        ResultOfDeletion result = super.deleteEpicById(id);
        save();
        return result;
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public int createSubtask(Subtask newSubtask) {
        int result = super.createSubtask(newSubtask);
        save();
        return result;
    }

    @Override
    public UpdateResult updateSubtask(Subtask subtask) {
        UpdateResult result = super.updateSubtask(subtask);
        save();
        return result;
    }

    @Override
    public ResultOfDeletion deleteSubtaskById(int id) {
        ResultOfDeletion result = super.deleteSubtaskById(id);
        save();
        return result;
    }

    private void save() {

        try (Writer writer = new FileWriter(fileName, StandardCharsets.UTF_8)) {

            String csvHeader = "id,type,name,status,description,epic";
            writer.write(csvHeader);

            for (Task task : getTasksList()) {
                writer.write('\n' + CsvSerializer.toCsvString(task, TaskType.TASK));
            }

            for (Epic epic : getEpicsList()) {
                writer.write('\n' + CsvSerializer.toCsvString(epic, TaskType.EPIC));
            }

            for (Subtask subtask : getSubtasksList()) {
                writer.write('\n' + CsvSerializer.toCsvString(subtask, TaskType.SUBTASK));
            }

        } catch (IOException e) {
            throw new ManagerSaveException(e);
        }
    }

    private void loadTask(Task task) {
        super.createTask(task);
    }

    private void loadEpic(Epic epic) {
        super.createEpic(epic);
    }

    private void loadSubtask(Subtask subtask) {
        super.createSubtask(subtask);
    }
}