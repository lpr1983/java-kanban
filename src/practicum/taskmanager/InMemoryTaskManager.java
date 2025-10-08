package practicum.taskmanager;

import practicum.task.Epic;
import practicum.task.Subtask;
import practicum.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private int numerator = 0;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int getNextId() {
        numerator++;
        return numerator;
    }

    @Override
    public ArrayList<Task> getTasksList() {
        // Возвращаются копии объектов, чтобы пользователь мог изменить данные только через update.
        ArrayList<Task> result = new ArrayList<>();
        for (Task task : tasks.values()) {
            result.add(copyTask(task));
        }
        return result;
    }

    @Override
    public void clearTasks() {
        for (Integer i : tasks.keySet()) {
            historyManager.remove(i);
        }
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {

        Task task = tasks.get(id);
        if (task == null) {
            return null;
        }
        // Согласно требованию, история должна хранить версии.
        Task taskVersion = copyTask(task);
        historyManager.add(taskVersion);

        // Пользователю также возвращается копия объекта, чтобы изменения он мог вносить только через update.
        return copyTask(task);
    }

    @Override
    public int createTask(Task newTask) {

        // У пользователя не должен быть объект, хранимый в менеджере, чтобы он мог менять данные задачи только путем вызова update.
        Task taskToSave = copyTask(newTask);
        int newId = getNextId();
        taskToSave.setId(newId);
        tasks.put(newId, taskToSave);

        return newId;
    }

    @Override
    public UpdateResult updateTask(Task task) {

        int taskId = task.getId();
        if (!tasks.containsKey(taskId)) {
            return UpdateResult.WRONG_TASK_ID;
        }
        tasks.put(taskId, copyTask(task));

        return UpdateResult.SUCCESS;
    }

    @Override
    public ResultOfDeletion deleteTaskById(int id) {
        if (!tasks.containsKey(id)) {
            return ResultOfDeletion.WRONG_TASK_ID;
        }

        tasks.remove(id);
        historyManager.remove(id);

        return ResultOfDeletion.SUCCESS;
    }

    @Override
    public ArrayList<Epic> getEpicsList() {
        ArrayList<Epic> result = new ArrayList<>();
        for (Epic epic : epics.values()) {
            result.add(copyEpic(epic));
        }
        return result;
    }

    @Override
    public void clearEpics() {
        for (Integer i : epics.keySet()) {
            historyManager.remove(i);
        }
        epics.clear();

        for (Integer i : subtasks.keySet()) {
            historyManager.remove(i);
        }
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {

        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }

        Epic epicVersion = copyEpic(epic);
        historyManager.add(epicVersion);

        return copyEpic(epic);
    }

    @Override
    public int createEpic(Epic newEpic) {

        Epic epicToSave = copyEpic(newEpic);
        int newId = getNextId();
        epicToSave.setId(newId);
        epics.put(newId, epicToSave);

        return newId;
    }

    @Override
    public UpdateResult updateEpic(Epic epic) {

        int epicId = epic.getId();
        Epic storedEpic = epics.get(epicId);
        if (storedEpic == null) {
            return UpdateResult.WRONG_TASK_ID;
        }
        storedEpic.setName(epic.getName());
        storedEpic.setDescription(epic.getDescription());

        return UpdateResult.SUCCESS;
    }

    @Override
    public ResultOfDeletion deleteEpicById(int id) {

        Epic epic = epics.get(id);
        if (epic == null) {
            return ResultOfDeletion.WRONG_TASK_ID;
        }

        for (int subtaskId : epic.getListOfSubtasksId()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }

        epics.remove(id);
        historyManager.remove(id);

        return ResultOfDeletion.SUCCESS;
    }

    @Override
    public ArrayList<Subtask> getSubtasksList() {
        ArrayList<Subtask> result = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            result.add(copySubtask(subtask));
        }
        return result;
    }

    @Override
    public void clearSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasksId();
            calculateAndSetEpicStatus(epic);
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {

        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return null;
        }

        Subtask subtaskVersion = copySubtask(subtask);
        historyManager.add(subtaskVersion);

        return copySubtask(subtask);
    }

    @Override
    public int createSubtask(Subtask newSubtask) {

        Epic epic = epics.get(newSubtask.getEpicId());
        if (epic == null) {
            return -1;
        }
        int newId = getNextId();

        Subtask subtaskToSave = copySubtask(newSubtask);
        subtaskToSave.setId(newId);
        subtasks.put(newId, subtaskToSave);

        epic.addSubtaskId(newId);
        calculateAndSetEpicStatus(epic);

        return newId;
    }

    @Override
    public UpdateResult updateSubtask(Subtask subtask) {

        int subtaskId = subtask.getId();
        Subtask storedSubtask = subtasks.get(subtaskId);

        if (storedSubtask == null) {
            return UpdateResult.WRONG_TASK_ID;
        }

        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);

        if (epic == null || storedSubtask.getEpicId() != epicId) {
            return UpdateResult.WRONG_EPIC_ID;
        }

        subtasks.put(subtaskId, copySubtask(subtask));
        calculateAndSetEpicStatus(epic);

        return UpdateResult.SUCCESS;
    }

    @Override
    public ResultOfDeletion deleteSubtaskById(int id) {

        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return ResultOfDeletion.WRONG_TASK_ID;
        }

        Epic epic = epics.get(subtask.getEpicId());

        epic.deleteSubtaskId(id);
        subtasks.remove(id);
        historyManager.remove(id);

        calculateAndSetEpicStatus(epic);

        return ResultOfDeletion.SUCCESS;
    }

    @Override
    public ArrayList<Subtask> getSubtasksOfEpic(int epicId) {
        ArrayList<Subtask> result = new ArrayList<>();

        Epic epic = epics.get(epicId);
        if (epic == null) {
            return result;
        }

        for (int subtaskId : epic.getListOfSubtasksId()) {
            Subtask subtask = copySubtask(subtasks.get(subtaskId));
            result.add(subtask);
        }

        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void calculateAndSetEpicStatus(Epic epic) {

       boolean hasIncompletedTasks = false;
       boolean hasOnlyNewTasks = true;

        for (Integer i : epic.getListOfSubtasksId()) {
            Subtask currentSubtask = subtasks.get(i);
            if (currentSubtask.getStatus() != TaskStatus.DONE) {
                hasIncompletedTasks = true;
            }
            if (currentSubtask.getStatus() != TaskStatus.NEW) {
                hasOnlyNewTasks = false;
            }
        }

        if (hasOnlyNewTasks) {
            epic.setStatus(TaskStatus.NEW);
        } else if (hasIncompletedTasks) {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            epic.setStatus(TaskStatus.DONE);
        }
    }

    private Task copyTask(Task task) {
        return new Task(task.getId(), task.getName(), task.getStatus(), task.getDescription());
    }

    private Epic copyEpic(Epic epic) {
        Epic copyEpic = new Epic(epic.getId(), epic.getName(), epic.getDescription());
        copyEpic.setStatus(epic.getStatus());
        for (Integer id : epic.getListOfSubtasksId()) {
            copyEpic.addSubtaskId(id);
        }
        return copyEpic;
    }

    private Subtask copySubtask(Subtask subtask) {
        return new Subtask(subtask.getId(), subtask.getName(), subtask.getStatus(),
                subtask.getEpicId(), subtask.getDescription());
    }
}
