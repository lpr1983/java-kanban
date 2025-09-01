package practicum.taskmanager;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
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

    public ResultOfCreation createTask(Task newTask) {

        int newId = getNextId();
        newTask.setId(newId);
        tasks.put(newId, newTask);

        return ResultOfCreation.SUCCESS;
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

    public ResultOfCreation createEpic(Epic newEpic) {

        int newId = getNextId();
        newEpic.setId(newId);
        epics.put(newId, newEpic);

        return ResultOfCreation.SUCCESS;
    }

    public UpdateResult updateEpic(Epic epic) {

        int epicId = epic.getId();
        if (!epics.containsKey(epicId)) {
            return UpdateResult.WRONG_TASK_ID;
        }
        epics.put(epicId, epic);

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

        ArrayList<Integer> listOfKeys = new ArrayList<>(subtasks.keySet());
        for (Integer i : listOfKeys) {
            deleteSubtaskById(i);
        }
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public ResultOfCreation createSubtask(Subtask newSubtask) {

        Epic epic = epics.get(newSubtask.getEpicId());
        if (epic == null) {
            return ResultOfCreation.WRONG_EPIC_ID;
        }
        int newId = getNextId();
        newSubtask.setId(newId);
        subtasks.put(newId, newSubtask);

        ArrayList<Integer> epicsSubtasksId = epic.getListOfSubtasksId();
        epicsSubtasksId.add(newSubtask.getId());
        calculateAndSetEpicStatus(epic);

        return ResultOfCreation.SUCCESS;
    }

    public UpdateResult updateSubtask(Subtask subtask) {

        int subtaskId = subtask.getId();
        if (!subtasks.containsKey(subtaskId)) {
            return UpdateResult.WRONG_TASK_ID;
        }
        int epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            return UpdateResult.WRONG_EPIC_ID;
        }
        Epic epic = epics.get(epicId);

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

        epic.getListOfSubtasksId().remove(Integer.valueOf(id));
        subtasks.remove(id);

        calculateAndSetEpicStatus(epic);

        return ResultOfDeletion.SUCCESS;
    }

    public ArrayList<Subtask> getSubtasksOfEpic(Epic epic) {
        ArrayList<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getListOfSubtasksId()) {
            Subtask subtask = subtasks.get(subtaskId);
            result.add(subtask);
        }
        return result;
    }

    public void calculateAndSetEpicStatus(Epic epic) {

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
