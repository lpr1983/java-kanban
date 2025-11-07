package practicum.taskmanager;

import practicum.task.Epic;
import practicum.task.Subtask;
import practicum.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {
    protected int numerator = 0;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private final Set<Task> prioritizedTasks = new TreeSet<>(
            (task1, task2) -> {

                int result = task1.getStartTime().compareTo(task2.getStartTime());

                if (result == 0) {
                    result = task1.getId() - task2.getId();
                }

                return result;
            }
    );

    private int getNextId() {
        numerator++;
        return numerator;
    }

    @Override
    public List<Task> getPrioritizedTasks() {

        return prioritizedTasks.stream().map(task -> {
            if (task.getClass() == Task.class) {
                return copyTask(task);
            } else if (task.getClass() == Subtask.class) {
                return copySubtask((Subtask) task);
            } else {
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public List<Task> getTasksList() {
        // Возвращаются копии объектов, чтобы пользователь мог изменить данные только через update.
        return tasks.values().stream().
                map(this::copyTask).toList();
    }

    @Override
    public void clearTasks() {

        for (Map.Entry<Integer, Task> entry : tasks.entrySet()) {
            historyManager.remove(entry.getKey());
            prioritizedTasks.remove(entry.getValue());
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

        if (taskOverlapsWithOtherTask(newTask)) {
            throw new IllegalArgumentException("Task overlaps with other task");
        }

        // У пользователя не должен быть объект, хранимый в менеджере, чтобы он мог менять данные задачи только путем вызова update.
        Task taskToSave = copyTask(newTask);
        int newId = getNextId();
        taskToSave.setId(newId);
        tasks.put(newId, taskToSave);

        if (taskToSave.getStartTime() != null) {
            prioritizedTasks.add(taskToSave);
        }

        return newId;
    }

    @Override
    public UpdateResult updateTask(Task task) {

        int taskId = task.getId();
        if (!tasks.containsKey(taskId)) {
            return UpdateResult.WRONG_TASK_ID;
        }

        if (taskOverlapsWithOtherTask(task)) {
            throw new IllegalArgumentException("Task overlaps with other task");
        }

        Task storedTask = tasks.get(taskId);

        Task taskToSave = copyTask(task);
        tasks.put(taskId, taskToSave);

        prioritizedTasks.remove(storedTask);
        if (taskToSave.getStartTime() != null) {
            prioritizedTasks.add(taskToSave);
        }

        return UpdateResult.SUCCESS;
    }

    @Override
    public ResultOfDeletion deleteTaskById(int id) {
        if (!tasks.containsKey(id)) {
            return ResultOfDeletion.WRONG_TASK_ID;
        }

        prioritizedTasks.remove(tasks.get(id));

        tasks.remove(id);
        historyManager.remove(id);

        return ResultOfDeletion.SUCCESS;
    }

    @Override
    public List<Epic> getEpicsList() {
        return epics.values().stream().map(this::copyEpic).toList();
    }

    @Override
    public void clearEpics() {
        for (Integer i : epics.keySet()) {
            historyManager.remove(i);
        }
        epics.clear();

        for (Map.Entry<Integer, Subtask> entry : subtasks.entrySet()) {
            prioritizedTasks.remove(entry.getValue());
            historyManager.remove(entry.getKey());
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
            prioritizedTasks.remove(subtasks.get(subtaskId));
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }

        epics.remove(id);
        historyManager.remove(id);

        return ResultOfDeletion.SUCCESS;
    }

    @Override
    public List<Subtask> getSubtasksList() {
        return subtasks.values().stream().map(this::copySubtask).toList();
    }

    @Override
    public void clearSubtasks() {

        for (Map.Entry<Integer, Subtask> entry: subtasks.entrySet()) {
            historyManager.remove(entry.getKey());
            prioritizedTasks.remove(entry.getValue());
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.clearSubtasksId();
            calculateAndSetEpicStatus(epic);
            calculateAndSetEpicTimes(epic);
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

        if (taskOverlapsWithOtherTask(newSubtask)) {
            throw new IllegalArgumentException("Task overlaps with other task");
        }

        int newId = getNextId();

        Subtask subtaskToSave = copySubtask(newSubtask);
        subtaskToSave.setId(newId);
        subtasks.put(newId, subtaskToSave);

        epic.addSubtaskId(newId);
        calculateAndSetEpicStatus(epic);
        calculateAndSetEpicTimes(epic);

        if (subtaskToSave.getStartTime() != null) {
            prioritizedTasks.add(subtaskToSave);
        }

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

        if (taskOverlapsWithOtherTask(subtask)) {
            throw new IllegalArgumentException("Task overlaps with other task");
        }

        Subtask subtaskToSave = copySubtask(subtask);

        subtasks.put(subtaskId, subtaskToSave);
        calculateAndSetEpicStatus(epic);
        calculateAndSetEpicTimes(epic);

        prioritizedTasks.remove(storedSubtask);
        if (subtaskToSave.getStartTime() != null) {
            prioritizedTasks.add(subtaskToSave);
        }

        return UpdateResult.SUCCESS;
    }

    @Override
    public ResultOfDeletion deleteSubtaskById(int id) {

        Subtask storedSubtask = subtasks.get(id);
        if (storedSubtask == null) {
            return ResultOfDeletion.WRONG_TASK_ID;
        }

        Epic epic = epics.get(storedSubtask.getEpicId());

        epic.deleteSubtaskId(id);
        subtasks.remove(id);
        historyManager.remove(id);

        calculateAndSetEpicStatus(epic);
        calculateAndSetEpicTimes(epic);

        prioritizedTasks.remove(storedSubtask);

        return ResultOfDeletion.SUCCESS;
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {

        Epic epic = epics.get(epicId);

        if (epic == null) {
            return List.of();
        }

        return epic.getListOfSubtasksId().stream().
                map(subtaskId -> copySubtask(subtasks.get(subtaskId))).toList();
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

    private void calculateAndSetEpicTimes(Epic epic) {

        Duration epicDuration = null;
        LocalDateTime epicStartTime = null;
        LocalDateTime epicEndTime = null;

        for (int subtaskId : epic.getListOfSubtasksId()) {
            Subtask subtask = subtasks.get(subtaskId);

            Duration subtaskDuration = subtask.getDuration();
            LocalDateTime subtaskStartTime = subtask.getStartTime();
            LocalDateTime subtaskEndTime = subtask.getEndTime();

            if (subtaskDuration == null || subtaskStartTime == null || subtaskEndTime == null) {
                continue;
            }

            if (epicDuration == null) {
                epicDuration = Duration.ZERO;
            }
            epicDuration = epicDuration.plus(subtask.getDuration());

            if (epicStartTime == null) {
                epicStartTime = subtaskStartTime;
            }

            if (epicStartTime.isAfter(subtaskStartTime)) {
                epicStartTime = subtaskStartTime;
            }

            if (epicEndTime == null) {
                epicEndTime = subtaskEndTime;
            }

            if (epicEndTime.isBefore(subtaskEndTime)) {
                epicEndTime = subtaskEndTime;
            }
        }

        epic.setEndTime(epicEndTime);
        epic.setStartTime(epicStartTime);
        epic.setDuration(epicDuration);

    }


    private Task copyTask(Task task) {
        return new Task(task.getId(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                task.getStartTime(),
                task.getDuration());
    }

    private Epic copyEpic(Epic epic) {

        Epic copyEpic = new Epic(epic.getId(),
                epic.getName(),
                epic.getStatus(),
                epic.getDescription(),
                epic.getStartTime(),
                epic.getDuration(),
                epic.getEndTime());

        for (Integer id : epic.getListOfSubtasksId()) {
            copyEpic.addSubtaskId(id);
        }

        return copyEpic;
    }

    private Subtask copySubtask(Subtask subtask) {
        return new Subtask(subtask.getId(),
                subtask.getName(),
                subtask.getStatus(),
                subtask.getEpicId(),
                subtask.getDescription(),
                subtask.getStartTime(),
                subtask.getDuration());
    }

    protected void putTaskWithoutAnyActions(Task task) {
        tasks.put(task.getId(), task);
    }

    protected void putEpicWithoutAnyActions(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    protected void putSubtaskWithoutAnyActions(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    protected void removeTaskFromPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

    protected void putTaskToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    protected void setSubtasksIdForEpics(HashMap<Integer, List<Integer>> subtasksIdOfEpics) {

        for (Map.Entry<Integer, List<Integer>> entry : subtasksIdOfEpics.entrySet()) {

            int epicId = entry.getKey();
            Epic epic = epics.get(epicId);

            List<Integer> listOfSubtasksId = entry.getValue();

            for (Integer subtaskId : listOfSubtasksId) {
                epic.addSubtaskId(subtaskId);
            }
        }
    }

    protected boolean taskOverlapsWithOtherTask(Task taskToCheck) {

        return prioritizedTasks.stream().anyMatch(
                task -> !task.equals(taskToCheck) && tasksOverlap(task, taskToCheck)
        );

    }

    public static boolean tasksOverlap(Task task1, Task task2) {

        LocalDateTime startTime1 = task1.getStartTime();
        LocalDateTime endTime1 = task1.getEndTime();

        LocalDateTime startTime2 = task2.getStartTime();
        LocalDateTime endTime2 = task2.getEndTime();

        if (startTime1 == null || endTime1 == null ||
                startTime2 == null || endTime2 == null) {

            return false;
        }

        return startTime2.isBefore(endTime1)
                && startTime1.isBefore(endTime2);
    }

}
