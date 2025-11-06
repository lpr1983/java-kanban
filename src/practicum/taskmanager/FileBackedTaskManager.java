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
import java.util.HashMap;
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

        try (FileReader fileReader = new FileReader(fileName, StandardCharsets.UTF_8)) {

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String currentString = bufferedReader.readLine();

            int maxId = 0;

            // Для восстановления внутреннего хранилища epic.listOfSubtasksId
            HashMap<Integer, List<Integer>> subtasksIdOfEpics = new HashMap<>();

            while (bufferedReader.ready()) {
                currentString = bufferedReader.readLine();

                ResultOfSerialization rs = CsvSerializer.fromCsvString(currentString);

                Task task = rs.getTask();
                TaskType taskType = rs.getTaskType();

                List<TaskType> supportedTypes = List.of(TaskType.TASK, TaskType.EPIC, TaskType.SUBTASK);
                if (!supportedTypes.contains(taskType)) {
                    continue;
                }

                maxId = Integer.max(task.getId(), maxId);

                if (rs.getTaskType() == TaskType.TASK) {
                    taskManager.putTaskWithoutAnyActions(task);
                    taskManager.putTaskToPrioritizedTasks(task);
                } else if (rs.getTaskType() == TaskType.EPIC) {
                    taskManager.putEpicWithoutAnyActions((Epic) task);
                } else if (rs.getTaskType() == TaskType.SUBTASK) {
                    Subtask subtask = (Subtask) task;

                    taskManager.putSubtaskWithoutAnyActions(subtask);
                    taskManager.putTaskToPrioritizedTasks(subtask);

                    int epicId = subtask.getEpicId();
                    List<Integer> listOfSubtasksId = subtasksIdOfEpics.getOrDefault(epicId, new ArrayList<>());
                    listOfSubtasksId.add(subtask.getId());
                    subtasksIdOfEpics.put(epicId, listOfSubtasksId);
                }
            }

            taskManager.numerator = maxId + 1;
            taskManager.setSubtasksIdForEpics(subtasksIdOfEpics);
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

            String csvHeader = "id,type,name,status,description,epic,startTime,duration,endTime";
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
}