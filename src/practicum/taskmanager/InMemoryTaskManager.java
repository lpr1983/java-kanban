package practicum.taskmanager;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int numerator = 0;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int getNextId() {
        numerator++;
        return numerator;
    }

    @Override
    public ArrayList<Task> getTasksList() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {

        Task task = tasks.get(id);
        if (task == null) {
            return null;
        }
        // Согласно требованию о том, что история просмотров хранит версии на момент вызова
        // метода получения задачи.
        Task taskCopy = copyTask(task);
        historyManager.add(taskCopy);

        return task;
    }

    @Override
    public int createTask(Task newTask) {

        int newId = getNextId();
        newTask.setId(newId);
        tasks.put(newId, newTask);

        return newId;
    }

    @Override
    public UpdateResult updateTask(Task task) {

        int taskId = task.getId();
        if (!tasks.containsKey(taskId)) {
            return UpdateResult.WRONG_TASK_ID;
        }
        tasks.put(taskId, task);

        return UpdateResult.SUCCESS;
    }

    @Override
    public ResultOfDeletion deleteTaskById(int id) {
        if (!tasks.containsKey(id)) {
            return ResultOfDeletion.WRONG_TASK_ID;
        }

        tasks.remove(id);

        return ResultOfDeletion.SUCCESS;
    }

    @Override
    public ArrayList<Task> getEpicsList() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {

        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }

        Task epicCopy = copyEpic(epic);
        historyManager.add(epicCopy);

        return epic;
    }

    @Override
    public int createEpic(Epic newEpic) {

        int newId = getNextId();
        newEpic.setId(newId);
        epics.put(newId, newEpic);

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
        }

        epics.remove(id);

        return ResultOfDeletion.SUCCESS;
    }

    @Override
    public ArrayList<Task> getSubtasksList() {
        return new ArrayList<>(subtasks.values());
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

        Subtask copySubtask = copySubtask(subtask);
        historyManager.add(copySubtask);

        return subtask;
    }

    @Override
    public int createSubtask(Subtask newSubtask) {

        Epic epic = epics.get(newSubtask.getEpicId());
        if (epic == null) {
            return -1;
        }
        int newId = getNextId();
        newSubtask.setId(newId);
        subtasks.put(newId, newSubtask);

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

        subtasks.put(subtaskId, subtask);
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
            Subtask subtask = subtasks.get(subtaskId);
            result.add(subtask);
        }
        return result;
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
            epic.setCalculatedStatus(TaskStatus.NEW);
        } else if (hasIncompletedTasks) {
            epic.setCalculatedStatus(TaskStatus.IN_PROGRESS);
        } else {
            epic.setCalculatedStatus(TaskStatus.DONE);
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }

    private Task copyTask(Task task) {
        return new Task(task.getId(), task.getName(), task.getStatus(), task.getDescription());
    }

    private Epic copyEpic(Epic epic) {
        Epic copyEpic = new Epic(epic.getId(), epic.getName(), epic.getDescription());
        copyEpic.setCalculatedStatus(epic.getStatus());
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
