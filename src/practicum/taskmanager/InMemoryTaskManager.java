package practicum.taskmanager;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager {
    private int numerator = 0;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private int getNextId() {
        numerator++;
        return numerator;
    }

    public ArrayList<Task> getTasksList() {
        return new ArrayList<>(tasks.values());
    }

    public void clearTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public int createTask(Task newTask) {

        int newId = getNextId();
        newTask.setId(newId);
        tasks.put(newId, newTask);

        return newId;
    }

    public UpdateResult updateTask(Task task) {

        int taskId = task.getId();
        if (!tasks.containsKey(taskId)) {
            return UpdateResult.WRONG_TASK_ID;
        }
        tasks.put(taskId, task);

        return UpdateResult.SUCCESS;
    }

    public ResultOfDeletion deleteTaskById(int id) {
        if (!tasks.containsKey(id)) {
            return ResultOfDeletion.WRONG_TASK_ID;
        }

        tasks.remove(id);

        return ResultOfDeletion.SUCCESS;
    }

    public ArrayList<Task> getEpicsList() {
        return new ArrayList<>(epics.values());
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public int createEpic(Epic newEpic) {

        int newId = getNextId();
        newEpic.setId(newId);
        epics.put(newId, newEpic);

        return newId;
    }

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

    public ArrayList<Task> getSubtasksList() {
        return new ArrayList<>(subtasks.values());
    }

    public void clearSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasksId();
            calculateAndSetEpicStatus(epic);
        }
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

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
}
