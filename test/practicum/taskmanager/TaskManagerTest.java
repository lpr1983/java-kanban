package practicum.taskmanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.task.Epic;
import practicum.task.Subtask;
import practicum.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    protected abstract T createManager();

    @BeforeEach
    void beforeEach() {
        taskManager = createManager();
    }

    @Test
    void getTasksList() {

        Task task1 = new Task("tast1", TaskStatus.NEW, "");
        int taskId1 = taskManager.createTask(task1);

        Task task2 = new Task("tast2", TaskStatus.NEW, "");
        int taskId2 = taskManager.createTask(task2);

        List<Task> requireList = List.of(getTaskById(taskId1), getTaskById(taskId2));
        List<Task> tasksList = taskManager.getTasksList();

        boolean arraysAreEqual = Arrays.equals(requireList.toArray(), tasksList.toArray());
        Assertions.assertTrue(arraysAreEqual, "Неправильный состав списка.");
    }

    @Test
    void clearTasks() {

        Task task1 = new Task("tast1", TaskStatus.NEW, "");
        taskManager.createTask(task1);

        Task task2 = new Task("tast2", TaskStatus.NEW, "");
        taskManager.createTask(task2);

        taskManager.clearTasks();
        Assertions.assertTrue(taskManager.getTasksList().isEmpty());
    }

    @Test
    void getTaskById() {

        Task task1 = new Task(999, "task1", TaskStatus.NEW, "");
        int taskId1 = taskManager.createTask(task1);

        Task task2 = new Task(999, "task1", TaskStatus.NEW, "");
        taskManager.createTask(task2);

        Task storedTask = getTaskById(taskId1);

        assertTrue(compareTasksByFields(task1, storedTask),
                "По id не вернулась нужная задача.");

        task1.setStatus(TaskStatus.IN_PROGRESS);
        assertNotEquals(task1.getStatus(), getTaskById(taskId1).getStatus(),
                "Не должен возвращаться хранимый объект");

        boolean historyIsAdded = Arrays.equals(taskManager.getHistory().toArray(), List.of(storedTask).toArray());
        assertTrue(historyIsAdded, "Не добавилась история просмотра");
    }

    @Test
    void createTask() {

        Task task1 = new Task(999, "task1", TaskStatus.NEW, "");
        int taskId1 = taskManager.createTask(task1);

        Task storedTask = getTaskById(taskId1);
        assertTrue(compareTasksByFields(task1, storedTask), "Состав полей сохраненной задачи неправильный");
        assertNotEquals(task1.getId(), storedTask.getId(), "Менеджер должен присвоить новый номер");
    }

    @Test
    void updateTask() {

        Task task1 = new Task("task1", TaskStatus.NEW, "");
        int taskId1 = taskManager.createTask(task1);

        Task taskToUpdate = new Task(taskId1, "task1 updated", TaskStatus.IN_PROGRESS, "upd");
        taskManager.updateTask(taskToUpdate);

        Task updatedTask = getTaskById(taskId1);
        assertTrue(compareTasksByFields(taskToUpdate, updatedTask), "Состав полей задачи не обновился");

        updatedTask.setStatus(TaskStatus.DONE);
        assertNotEquals(updatedTask.getStatus(), getTaskById(taskId1).getStatus(),
                "update не должен сохранять объект пользователя");
    }

    @Test
    void deleteTaskById() {

        Task task1 = new Task(999, "task1", TaskStatus.NEW, "");
        int taskId1 = taskManager.createTask(task1);

        Task task2 = new Task(999, "task2", TaskStatus.NEW, "");
        int taskId2 = taskManager.createTask(task2);

        taskManager.deleteTaskById(taskId1);
        boolean arraysAreEqual = Arrays.equals(taskManager.getTasksList().toArray(),
                List.of(getTaskById(taskId2)).toArray());

        assertTrue(arraysAreEqual, "Неправильный состав списка задач после удаления");
    }

    @Test
    void getEpicsList() {

        Epic epic1 = new Epic("epic1", "");
        int epicId1 = taskManager.createEpic(epic1);

        Epic epic2 = new Epic("epic2", "");
        int epicId2 = taskManager.createEpic(epic2);

        List<Task> requireList = List.of(getEpicById(epicId1), getEpicById(epicId2));
        List<Epic> tasksList = taskManager.getEpicsList();

        Assertions.assertArrayEquals(requireList.toArray(), tasksList.toArray(), "Неправильный состав списка.");
    }

    @Test
    void clearEpics() {

        Epic epic1 = new Epic("epic1", "");
        taskManager.createEpic(epic1);

        Epic epic2 = new Epic("epic2", "");
        taskManager.createEpic(epic2);

        taskManager.clearEpics();

        List<Task> requireList = List.of();
        List<Epic> tasksList = taskManager.getEpicsList();

        Assertions.assertArrayEquals(requireList.toArray(), tasksList.toArray(), "Список должен быть пустой.");
    }

    @Test
    void getEpicById() {

        Epic epic1 = new Epic(999, "epic", "");
        int epicId1 = taskManager.createEpic(epic1);

        Epic epic2 = new Epic(999, "epic2", "");
        taskManager.createEpic(epic2);

        Epic storedTask = getEpicById(epicId1);

        assertTrue(compareTasksByFields(epic1, storedTask),
                "По id не вернулась нужная задача.");

        epic1.setStatus(TaskStatus.IN_PROGRESS);
        assertNotEquals(epic1.getStatus(), getEpicById(epicId1).getStatus(),
                "Не должен возвращаться хранимый объект");

        boolean historyIsAdded = Arrays.equals(taskManager.getHistory().toArray(), List.of(storedTask).toArray());
        assertTrue(historyIsAdded, "Не добавилась история просмотра");
    }

    @Test
    void createEpic() {

        Epic epic = new Epic(999, "epic", "descr");
        int epicId = taskManager.createEpic(epic);

        Epic storedEpic = getEpicById(epicId);
        assertTrue(compareTasksByFields(epic, storedEpic), "Состав полей сохраненной задачи неправильный");
        assertNotEquals(epic.getId(), storedEpic.getId(), "Менеджер должен присвоить новый номер");

        Epic epic2 = new Epic(999, "epic2", "descr");
        epic2.setStatus(TaskStatus.IN_PROGRESS);
        int epicId2 = taskManager.createEpic(epic);
        assertEquals(TaskStatus.NEW, getEpicById(epicId2).getStatus(), "Статус нового epic должен быть NEW");
    }

    @Test
    void updateEpic() {

        Epic epic = new Epic("epic", "descr");
        int epicId = taskManager.createEpic(epic);

        Epic epicToUpdate = new Epic(epicId, "epic updated", "upd");
        taskManager.updateEpic(epicToUpdate);

        Epic updatedEpic = getEpicById(epicId);
        assertTrue(compareTasksByFields(epicToUpdate, updatedEpic), "Состав полей задачи не обновился");

        updatedEpic.setStatus(TaskStatus.DONE);
        assertNotEquals(updatedEpic.getStatus(), getEpicById(epicId).getStatus(),
                "update не должен сохранять объект пользователя");
    }

    @Test
    void deleteEpicById() {

        Epic epic = new Epic("epic", "descr");
        int epicId1 = taskManager.createEpic(epic);

        Epic epic2 = new Epic("epic2", "descr2");
        int epicId2 = taskManager.createEpic(epic2);

        taskManager.deleteEpicById(epicId1);

        boolean arraysAreEqual = Arrays.equals(taskManager.getEpicsList().toArray(),
                List.of(getEpicById(epicId2)).toArray());

        assertTrue(arraysAreEqual, "Неправильный состав списка задач после удаления");
    }

    @Test
    void getSubtasksList() {

        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask1", TaskStatus.NEW, epicId, "descr1");
        int subtaskId1 = taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("subtask2", TaskStatus.IN_PROGRESS, epicId, "descr2");
        int subtaskId2 = taskManager.createSubtask(subtask2);

        List<Task> requireList = List.of(getSubtaskById(subtaskId1),
                getSubtaskById(subtaskId2));

        List<Subtask> tasksList = taskManager.getSubtasksList();

        boolean arraysAreEqual = Arrays.equals(requireList.toArray(), tasksList.toArray());
        Assertions.assertTrue(arraysAreEqual, "Неправильный состав списка.");
    }

    @Test
    void clearSubtasks() {
        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask1", TaskStatus.NEW, epicId, "descr1");
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("subtask2", TaskStatus.IN_PROGRESS, epicId, "descr2");
        taskManager.createSubtask(subtask2);

        taskManager.clearSubtasks();
        Assertions.assertTrue(taskManager.getSubtasksList().isEmpty());
    }

    @Test
    void getSubtaskById() {

        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(999, "subtask1", TaskStatus.NEW, epicId, "descr1");
        int subtaskId1 = taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask(999, "subtask2", TaskStatus.IN_PROGRESS, epicId, "descr2");
        taskManager.createSubtask(subtask2);

        Subtask storedTask = getSubtaskById(subtaskId1);
        assertTrue(compareTasksByFields(subtask1, storedTask),
                "По id не вернулась нужная задача.");

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        assertNotEquals(subtask1.getStatus(), getSubtaskById(subtaskId1).getStatus(),
                "Не должен возвращаться хранимый объект");

        boolean historyIsAdded = Arrays.equals(taskManager.getHistory().toArray(), List.of(storedTask).toArray());
        assertTrue(historyIsAdded, "Не добавилась история просмотра");
    }

    @Test
    void createSubtask() {

        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask1", TaskStatus.NEW, epicId, "descr1");
        int subtaskId = taskManager.createSubtask(subtask1);

        Task storedTask = getSubtaskById(subtaskId);
        assertTrue(compareTasksByFields(subtask1, storedTask), "Состав полей сохраненной задачи неправильный");
        assertNotEquals(subtask1.getId(), storedTask.getId(), "Менеджер должен присвоить новый номер");

        Subtask subtaskWithWrongEpic = new Subtask("subtask1", TaskStatus.NEW, 999, "descr1");
        int subtaskWithWrongEpicId = taskManager.createSubtask(subtaskWithWrongEpic);
        assertEquals(-1, subtaskWithWrongEpicId, "Метод создания подздадачи должен вернуть -1 при отсутствии epic");
    }

    @Test
    void updateSubtask() {

        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask1", TaskStatus.NEW, epicId, "descr1");
        int subtaskId = taskManager.createSubtask(subtask1);

        Subtask taskToUpdate = new Subtask(subtaskId, "subtask1 updated", TaskStatus.IN_PROGRESS, epicId, "upd");
        taskManager.updateSubtask(taskToUpdate);

        Task updatedTask = getSubtaskById(subtaskId);
        assertTrue(compareTasksByFields(taskToUpdate, updatedTask), "Состав полей задачи не обновился");

        updatedTask.setStatus(TaskStatus.DONE);
        assertNotEquals(updatedTask.getStatus(), getSubtaskById(subtaskId).getStatus(),
                "update не должен сохранять объект пользователя");
    }

    @Test
    void deleteSubtaskById() {

        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask1", TaskStatus.NEW, epicId, "descr1");
        int subtaskId = taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("subtask2", TaskStatus.NEW, epicId, "descr2");
        int subtaskId2 = taskManager.createSubtask(subtask2);

        taskManager.deleteSubtaskById(subtaskId);
        boolean arraysAreEqual = Arrays.equals(taskManager.getSubtasksList().toArray(),
                List.of(getSubtaskById(subtaskId2)).toArray());

        assertTrue(arraysAreEqual, "Неправильный состав списка задач после удаления");
    }

    @Test
    void getSubtasksOfEpic() {

        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask1", TaskStatus.NEW, epicId, "descr1");
        int subtaskId = taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("subtask2", TaskStatus.NEW, epicId, "descr2");
        int subtaskId2 = taskManager.createSubtask(subtask2);

        List<Subtask> requiredList = List.of(getSubtaskById(subtaskId),
                getSubtaskById(subtaskId2));

        List<Subtask> taskList = taskManager.getSubtasksOfEpic(epicId);

        assertArrayEquals(requiredList.toArray(), taskList.toArray(),
                "Неправильный состав подзадач эпика");
    }

    @Test
    void getHistory() {

        Task task = new Task(999, "Task 1", TaskStatus.NEW, "Some description");
        int taskId = taskManager.createTask(task);
        Task createdTask = getTaskById(taskId);

        Task task2 = new Task(999, "Task 2", TaskStatus.NEW, "Some description");
        int taskId2 = taskManager.createTask(task2);
        getTaskById(taskId2);

        Epic epic = new Epic(999, "Epic 1", "Epic desсription");
        int epicId = taskManager.createEpic(epic);
        getEpicById(epicId);

        Subtask subtask = new Subtask(999, "Subtask 1_1", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId = taskManager.createSubtask(subtask);
        getSubtaskById(subtaskId);

        Subtask subtask2 = new Subtask(999, "Subtask 1_2", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId2 = taskManager.createSubtask(subtask2);
        getSubtaskById(subtaskId2);

        assertEquals(5, taskManager.getHistory().size(), "Неправильное количество задач в истории");

        createdTask.setStatus(TaskStatus.DONE);
        taskManager.updateTask(createdTask);

        TaskStatus statusInHistory = taskManager.getHistory().getFirst().getStatus();

        assertNotEquals(createdTask.getStatus(), statusInHistory,
                "История должна хранить версию задачи на момент вызова метода getById");

        Task taskById = getTaskById(taskId);
        assertEquals(5, taskManager.getHistory().size(), "Неправильное количество задач в истории");
        assertEquals(taskById, taskManager.getHistory().getLast(), "Последний просмотренный должен быть последним");

        taskManager.deleteTaskById(taskId);
        assertEquals(4, taskManager.getHistory().size(), "Неверный размер истории после удаления");

        taskManager.clearTasks();
        assertEquals(3, taskManager.getHistory().size(), "Неверный размер истории после удаления");

        taskManager.deleteSubtaskById(subtaskId);
        assertEquals(2, taskManager.getHistory().size(), "Неверный размер истории после удаления");

        taskManager.clearSubtasks();
        assertEquals(1, taskManager.getHistory().size(), "Неверный размер истории после удаления");

        Subtask subtask3 = new Subtask("Subtask 1_3", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId3 = taskManager.createSubtask(subtask3);
        getSubtaskById(subtaskId3);

        taskManager.deleteEpicById(epicId);
        assertEquals(0, taskManager.getHistory().size(), "Неверный размер истории после удаления");
    }

    @Test
    void getPrioritizedTasksCreateTask() {

        LocalDateTime testTime = LocalDateTime.now();
        // Создание задачи
        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        task.setStartTime(testTime);
        taskManager.createTask(task);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");
        assertTrue(compareTasksByFields(task, sortedTasks.getFirst()),
                "Добавленная задача не совпадает с задачей в списке getPrioritizedTasks");
    }

    @Test
    void getPrioritizedTasksUpdateTask() {

        LocalDateTime testTime = LocalDateTime.now();
        // Создание задачи
        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        task.setStartTime(testTime);
        int taskId = taskManager.createTask(task);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");

        // Изменение задачи
        task = getTaskById(taskId);
        task.setStartTime(testTime.plusDays(1));
        taskManager.updateTask(task);

        sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");
        assertTrue(compareTasksByFields(task, sortedTasks.getFirst()), "Добавленная задача не совпадает с задачей в списке getPrioritizedTasks");
    }

    @Test
    void getPrioritizedTasksDeleteTask() {
        LocalDateTime testTime = LocalDateTime.now();

        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        task.setStartTime(testTime);
        int taskId = taskManager.createTask(task);

        taskManager.deleteTaskById(taskId);
        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, sortedTasks.size(), "getPrioritizedTasks должен быть пустой");
    }

    @Test
    void getPrioritizedTasksClearTasks() {
        LocalDateTime testTime = LocalDateTime.now();

        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        task.setStartTime(testTime);
        taskManager.createTask(task);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");

        // Очистка задач
        taskManager.clearTasks();
        sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, sortedTasks.size(), "getPrioritizedTasks должен быть пустой");
    }

    @Test
    void getPrioritizedTasksCreateSubtask() {
        LocalDateTime testTime = LocalDateTime.now();

        // Создание подзадачи
        Epic epic = new Epic("Epic1", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask 1", TaskStatus.NEW, epicId, "");
        subtask1.setStartTime(testTime);
        taskManager.createSubtask(subtask1);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");
        assertTrue(compareTasksByFields(subtask1, sortedTasks.getFirst()), "Добавленная подзадача не совпадает с подзадачей" +
                " в списке getPrioritizedTasks");
    }

    @Test
    void getPrioritizedTasksUpdateSubtask() {
        LocalDateTime testTime = LocalDateTime.now();

        Epic epic = new Epic("Epic1", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask 1", TaskStatus.NEW, epicId, "");
        subtask1.setStartTime(testTime);
        int subtaskId = taskManager.createSubtask(subtask1);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");

        subtask1 = getSubtaskById(subtaskId);
        subtask1.setStartTime(testTime.plusDays(1));
        subtask1.setDuration(Duration.ofDays(7));

        // Обновление подзадачи
        taskManager.updateSubtask(subtask1);
        sortedTasks = taskManager.getPrioritizedTasks();

        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");
        assertTrue(compareTasksByFields(subtask1, sortedTasks.getFirst()), "Добавленная подзадача не совпадает с подзадачей" +
                " в списке getPrioritizedTasks");
    }

    @Test
    void getPrioritizedTasksDeleteSubtask() {
        LocalDateTime testTime = LocalDateTime.now();

        Epic epic = new Epic("Epic1", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask 1", TaskStatus.NEW, epicId, "");
        subtask1.setStartTime(testTime);
        int subtaskId = taskManager.createSubtask(subtask1);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");

        taskManager.deleteSubtaskById(subtaskId);
        sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, sortedTasks.size(), "getPrioritizedTasks должен быть пустой");
    }

    @Test
    void getPrioritizedTasksClearSubtask() {
        LocalDateTime testTime = LocalDateTime.now();

        Epic epic = new Epic("Epic1", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask 1", TaskStatus.NEW, epicId, "");
        subtask1.setStartTime(testTime);
        taskManager.createSubtask(subtask1);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");

        taskManager.clearSubtasks();
        sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, sortedTasks.size(), "getPrioritizedTasks должен быть пустой");
    }

    @Test
    void getPrioritizedClearEpics() {
        LocalDateTime testTime = LocalDateTime.now();

        Epic epic = new Epic("Epic1", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask 1", TaskStatus.NEW, epicId, "");
        subtask1.setStartTime(testTime);
        taskManager.createSubtask(subtask1);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");

        taskManager.clearEpics();
        sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, sortedTasks.size(), "getPrioritizedTasks должен быть пустой");
    }

    @Test
    void getPrioritizedDeleteEpic() {
        LocalDateTime testTime = LocalDateTime.now();

        Epic epic = new Epic("Epic1", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask 1", TaskStatus.NEW, epicId, "");
        subtask1.setStartTime(testTime);
        taskManager.createSubtask(subtask1);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");

        taskManager.deleteEpicById(epicId);
        sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, sortedTasks.size(), "getPrioritizedTasks должен быть пустой");
    }

    @Test
    void getPrioritizedTasksSortsOrder() {
        LocalDateTime testTime = LocalDateTime.now();

        Epic epic = new Epic("Epic1", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("subtask 1", TaskStatus.NEW, epicId, "");
        subtask1.setStartTime(testTime);
        int subtaskId = taskManager.createSubtask(subtask1);

        Task task = new Task("task1", TaskStatus.NEW, "");
        task.setStartTime(testTime.plusDays(1));
        int taskId = taskManager.createTask(task);

        Task task2 = new Task("task2", TaskStatus.NEW, "");
        task2.setStartTime(testTime.minusDays(1));
        int task2Id = taskManager.createTask(task2);

        List<Task> sortedTasks = taskManager.getPrioritizedTasks();
        assertEquals(3, sortedTasks.size(), "Неправильный размер списка getPrioritizedTasks");

        subtask1 = getSubtaskById(subtaskId);
        task = getTaskById(taskId);
        task2 = getTaskById(task2Id);

        assertTrue(compareTasksByFields(task2, sortedTasks.get(0)), "Неправильный порядок задач");
        assertTrue(compareTasksByFields(subtask1, sortedTasks.get(1)), "Неправильный порядок задач");
        assertTrue(compareTasksByFields(task, sortedTasks.get(2)), "Неправильный порядок задач");

        subtask1.setStartTime(subtask1.getStartTime().minusDays(3));
        taskManager.updateSubtask(subtask1);

        sortedTasks = taskManager.getPrioritizedTasks();
        assertTrue(compareTasksByFields(subtask1, sortedTasks.get(0)), "Неправильный порядок задач");
        assertTrue(compareTasksByFields(task2, sortedTasks.get(1)), "Неправильный порядок задач");
        assertTrue(compareTasksByFields(task, sortedTasks.get(2)), "Неправильный порядок задач");

        task2.setStartTime(task2.getStartTime().plusDays(10));
        taskManager.updateTask(task2);

        sortedTasks = taskManager.getPrioritizedTasks();
        assertTrue(compareTasksByFields(subtask1, sortedTasks.get(0)), "Неправильный порядок задач");
        assertTrue(compareTasksByFields(task, sortedTasks.get(1)), "Неправильный порядок задач");
        assertTrue(compareTasksByFields(task2, sortedTasks.get(2)), "Неправильный порядок задач");
    }

    @Test
    void createTaskAndSubtaskWithOverlaping() {

        LocalDateTime testTime = LocalDateTime.now();

        // Первая задача
        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        task.setStartTime(testTime);
        task.setDuration(Duration.ofDays(1));
        taskManager.createTask(task);

        // Непересекающаяся задача
        Task task2 = new Task("Task 2", TaskStatus.NEW, "Some description");
        task2.setStartTime(testTime.plusDays(1));
        task2.setDuration(Duration.ofDays(1));
        taskManager.createTask(task2);

        // Пересекающаяся задача
        Task task3 = new Task("Task 3", TaskStatus.NEW, "Some description");
        task3.setStartTime(testTime.plusDays(1));
        task3.setDuration(Duration.ofDays(1));

        Assertions.assertThrows(IllegalArgumentException.class, () -> taskManager.createTask(task3));

        Epic epic = new Epic("Epic", "");
        int epicId = taskManager.createEpic(epic);

        // Непересекующаяся подзадача
        Subtask subtask = new Subtask("subtask", TaskStatus.NEW, epicId, "");
        subtask.setStartTime(testTime.plusDays(2));
        subtask.setDuration(Duration.ofDays(1));
        taskManager.createSubtask(subtask);

        // Пересекающаяся подзадача
        Subtask subtask2 = new Subtask("subtask2", TaskStatus.NEW, epicId, "");
        subtask2.setStartTime(testTime.plusHours(12));
        subtask2.setDuration(Duration.ofDays(1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> taskManager.createSubtask(subtask2));
    }

    @Test
    void updateTaskWithOverlaping() {

        LocalDateTime testTime = LocalDateTime.now();

        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        task.setStartTime(testTime);
        task.setDuration(Duration.ofDays(1));
        taskManager.createTask(task);

        Task task2 = new Task("Task 2", TaskStatus.NEW, "Some description");
        task2.setStartTime(testTime.plusDays(1));
        task2.setDuration(Duration.ofDays(1));
        int taskId2 = taskManager.createTask(task2);

        // Обновление без пересечения
        task2 = getTaskById(taskId2);
        taskManager.updateTask(task2);

        // Обновление с пересечением
        Task taskToCheck = getTaskById(taskId2);
        taskToCheck.setStartTime(testTime);
        Assertions.assertThrows(IllegalArgumentException.class, () -> taskManager.updateTask(taskToCheck));

        Epic epic = new Epic("Epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("subtask", TaskStatus.NEW, epicId, "");
        subtask.setStartTime(testTime.plusDays(2));
        subtask.setDuration(Duration.ofDays(1));
        int subtaskId = taskManager.createSubtask(subtask);

        // Обновление без пересечения
        Subtask subtaskToCheck = getSubtaskById(subtaskId);
        taskManager.updateSubtask(subtaskToCheck);

        // Обновление с пересечением
        subtaskToCheck.setStartTime(testTime);
        Assertions.assertThrows(IllegalArgumentException.class, () -> taskManager.updateSubtask(subtaskToCheck));
    }

    @Test
    void checkEpicStatus() {

        Epic epic = new Epic("epic", "");
        int epicId = taskManager.createEpic(epic);

        assertEquals(TaskStatus.NEW, getEpicById(epicId).getStatus());

        Subtask subtask1 = new Subtask("subtask1", TaskStatus.NEW, epicId, "");
        int subtaskId1 = taskManager.createSubtask(subtask1);

        assertEquals(TaskStatus.NEW, getEpicById(epicId).getStatus());

        Subtask subtask2 = new Subtask("subtask2", TaskStatus.NEW, epicId, "");
        int subtaskId2 = taskManager.createSubtask(subtask2);

        assertEquals(TaskStatus.NEW, getEpicById(epicId).getStatus());

        subtask1 = getSubtaskById(subtaskId1);
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatus.IN_PROGRESS, getEpicById(epicId).getStatus());

        subtask2 = getSubtaskById(subtaskId2);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask2);

        assertEquals(TaskStatus.IN_PROGRESS, getEpicById(epicId).getStatus());

        subtask1 = getSubtaskById(subtaskId1);
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);

        assertEquals(TaskStatus.IN_PROGRESS, getEpicById(epicId).getStatus());

        subtask2 = getSubtaskById(subtaskId2);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);

        assertEquals(TaskStatus.DONE, getEpicById(epicId).getStatus());
    }

    @Test
    void testEpicTimes() {

        Epic epic = new Epic("test epic", "");
        int epicId = taskManager.createEpic(epic);

        LocalDateTime minStartTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        Duration duration1 = Duration.ofDays(31);
        Duration duration2 = Duration.ofDays(1);

        Subtask subtask1 = new Subtask("test subtask1", TaskStatus.NEW, epicId, "");
        subtask1.setStartTime(minStartTime);
        subtask1.setDuration(duration1);
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("test subtask2", TaskStatus.IN_PROGRESS, epicId, "");
        subtask2.setStartTime(minStartTime.plusMonths(1));
        subtask2.setDuration(duration2);
        taskManager.createSubtask(subtask2);

        epic = getEpicById(epicId);

        assertEquals(minStartTime, epic.getStartTime(), "Неправильное время начала у epic");
        assertEquals(subtask2.getEndTime(), epic.getEndTime(), "Неправильное время окончания у epic");
        assertEquals(duration1.plus(duration2), epic.getDuration(), "Неправильная продолжительность у epic");
    }

    @Test
    void tasksOverlap() {

        LocalDateTime testTime = LocalDateTime.now();

        Task task1 = new Task("task1", TaskStatus.NEW, "");
        task1.setStartTime(testTime);
        task1.setDuration(Duration.ofDays(1));

        Task taskToCheck = new Task("task to check", TaskStatus.NEW, "");

        // задача полностью слева
        taskToCheck.setStartTime(testTime.minusDays(5));
        taskToCheck.setDuration(Duration.ofDays(1));

        boolean result = InMemoryTaskManager.tasksOverlap(task1, taskToCheck);
        assertFalse(result, "Задача не должна пересекаться");

        // задача совпадает в точке начала второй и конца первой
        taskToCheck.setStartTime(testTime.minusDays(1));
        result = InMemoryTaskManager.tasksOverlap(task1, taskToCheck);
        assertFalse(result, "Задача не должна пересекаться");

        // задача залезает слева
        taskToCheck.setStartTime(testTime.minusDays(1).plusHours(1));
        result = InMemoryTaskManager.tasksOverlap(task1, taskToCheck);
        assertTrue(result, "Задача должна пересекаться");

        // задача полностью внутри
        taskToCheck.setStartTime(testTime.plusHours(1));
        taskToCheck.setDuration(Duration.ofHours(1));
        result = InMemoryTaskManager.tasksOverlap(task1, taskToCheck);
        assertTrue(result, "Задача должна пересекаться");

        // Задача выходит изнутри направо
        taskToCheck.setStartTime(testTime.plusHours(1));
        taskToCheck.setDuration(Duration.ofDays(1));
        result = InMemoryTaskManager.tasksOverlap(task1, taskToCheck);
        assertTrue(result, "Задача должна пересекаться");

        // Задача полностью совпадает по времени
        taskToCheck.setStartTime(testTime);
        taskToCheck.setDuration(Duration.ofDays(1));
        result = InMemoryTaskManager.tasksOverlap(task1, taskToCheck);
        assertTrue(result, "Задача должна пересекаться");

        // Задача справа
        taskToCheck.setStartTime(testTime.plusDays(2));
        taskToCheck.setDuration(Duration.ofDays(1));
        result = InMemoryTaskManager.tasksOverlap(task1, taskToCheck);
        assertFalse(result, "Задача не должна пересекаться");

        // Задача пересекает исходную задачу полностью слева направо
        taskToCheck.setStartTime(testTime.minusDays(2));
        taskToCheck.setDuration(Duration.ofDays(5));
        result = InMemoryTaskManager.tasksOverlap(task1, taskToCheck);
        assertTrue(result, "Задача должна пересекаться");
    }

    @Test
    void tasksCreationCases() {

        Task task = new Task(999, "Task 1", TaskStatus.NEW, "Some description");
        int taskId = taskManager.createTask(task);
        Task createdTask = getTaskById(taskId);

        // Задача создалась, нашлась по возвращенному id
        assertNotNull(createdTask, "Не найдена задача по возвращенному Id");
        // Переданные поля совпадают с тем, что записал менеджер
        assertTrue(compareTasksByFields(task, createdTask),
                "Поля переданной и сохраненной задач не совпададают");
        // По переданному в самой задаче Id ничего не должно находиться
        assertTrue(taskManager.getTaskById(999).isEmpty(), "Id должен переопределиться при создании");

        Epic epic = new Epic(999, "Epic 1", "Epic desсription");
        int epicId = taskManager.createEpic(epic);

        Epic createdEpic = getEpicById(epicId);

        assertNotNull(createdEpic, "Не найдена задача по Id");
        assertTrue(compareTasksByFields(epic, createdEpic),
                "Поля переданной и сохраненной задач не совпададают");
        assertTrue(taskManager.getEpicById(999).isEmpty(), "Id должен переопределиться при создании");

        Subtask subtask = new Subtask(999, "Subtask 1_1", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask createdSubtask = getSubtaskById(subtaskId);
        assertNotNull(createdSubtask, "Не найдена задача по Id");
        assertTrue(compareTasksByFields(subtask, createdSubtask),
                "Поля переданной и сохраненной задач не совпададают");
        assertTrue(taskManager.getSubtaskById(999).isEmpty(), "Id должен переопределиться при создании");

        Epic addedEpic = getEpicById(epicId);
        assertTrue(addedEpic.getListOfSubtasksId().contains(subtaskId), "Эпик не содержит id созденной подзадачи.");

        // Состояние epic изменилось при добавлении подзадачи
        assertEquals(TaskStatus.IN_PROGRESS, addedEpic.getStatus(), "Статус должен быть IN_PROGRESS");

        // Задача, оставшаяся у пользователя, не может изменить данные задачи в менеджере
        createdTask.setStatus(TaskStatus.DONE);
        TaskStatus storedStatus = getTaskById(taskId).getStatus();
        assertNotEquals(createdTask.getStatus(), storedStatus,
                "Пользователь не должен иметь возможность изменить данные без update");

        createdEpic.setDescription("Новое описание");
        String storedDescription = getEpicById(epicId).getDescription();
        assertNotEquals(createdEpic.getDescription(), storedDescription,
                "Пользователь не должен иметь возможность изменить данные без update");

        createdSubtask.setDescription("Новое описание");
        storedDescription = getSubtaskById(subtaskId).getDescription();
        assertNotEquals(createdSubtask.getDescription(), storedDescription,
                "Пользователь не должен иметь возможность изменить данные без update");

        assertEquals(1, taskManager.getTasksList().size(), "Неверное количество задач");
        assertEquals(1, taskManager.getEpicsList().size(), "Неверное количество эпиков");
        assertEquals(1, taskManager.getSubtasksList().size(), "Неверное количество подзадач");
        assertEquals(1, taskManager.getSubtasksOfEpic(epicId).size(), "Неверное количество подзадач у эпика");
    }

    @Test
    void updatingTasksCases() {

        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        int taskId = taskManager.createTask(task);

        Epic epic = new Epic("Epic 1", "Epic desсription");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(9999, "Subtask 1_1", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId1 = taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask(9999, "Subtask 1_2", TaskStatus.DONE, epicId, "Subtask description");
        taskManager.createSubtask(subtask2);

        Task taskToUpdate = getTaskById(taskId);
        taskToUpdate.setStatus(TaskStatus.IN_PROGRESS);
        taskToUpdate.setDescription("new description");
        taskManager.updateTask(taskToUpdate);

        Task savedTask = getTaskById(taskId);
        assertTrue(compareTasksByFields(taskToUpdate, savedTask),
                "Не совпадают поля переданной и сохраненной задачи");

        Epic epicToUpdate = getEpicById(epicId);

        // Попытка изменить статус эпика в обход менеджера задач
        epicToUpdate.setStatus(TaskStatus.DONE);
        epicToUpdate = getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, epicToUpdate.getStatus(),
                "Не все подзадачи завершены, должен быть статус IN_PROGRESS");

        epicToUpdate.setDescription("Новое описание");
        taskManager.updateEpic(epicToUpdate);

        Epic savedEpic = getEpicById(epicId);
        assertTrue(compareTasksByFields(epicToUpdate, savedEpic),
                "Не совпадают поля переданной и сохраненной задачи");

        Subtask subtask1ToUpdate = getSubtaskById(subtaskId1);
        subtask1ToUpdate.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1ToUpdate);

        Subtask savedSubtask1 = getSubtaskById(subtaskId1);
        assertTrue(compareTasksByFields(subtask1ToUpdate, savedSubtask1),
                "Не совпадают переданные и сохраненные поля");

        savedEpic = getEpicById(epicId);
        assertEquals(TaskStatus.DONE, savedEpic.getStatus(),
                "Все подзадачи завершены, должен быть статус DONE");
    }

    @Test
    void deletionAndClearingTasksCases() {

        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        taskManager.createTask(task);

        Task task2 = new Task("Task 2", TaskStatus.NEW, "Some description");
        int taskId2 = taskManager.createTask(task2);

        taskManager.deleteTaskById(taskId2);
        assertEquals(1, taskManager.getTasksList().size(), "Неверное количество в списке");

        taskManager.clearTasks();
        assertEquals(0, taskManager.getTasksList().size(), "Неверное количество в списке");

        Epic epic = new Epic("Epic 2", "Epic desсription");
        int epicId = taskManager.createEpic(epic);

        Epic epic2 = new Epic("Epic 2", "Epic desсription");
        int epicId2 = taskManager.createEpic(epic2);

        Subtask subtask = new Subtask(999, "Subtask 1_1", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId1 = taskManager.createSubtask(subtask);

        Subtask subtask2 = new Subtask(999, "Subtask 1_1", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId2 = taskManager.createSubtask(subtask2);

        taskManager.deleteSubtaskById(subtaskId2);
        assertEquals(1, taskManager.getSubtasksList().size(), "Неверное количество в списке");

        epic = getEpicById(epicId);
        assertTrue(epic.getListOfSubtasksId().contains(subtaskId1), "В эпике нет id добавленной подзадачи.");
        assertFalse(epic.getListOfSubtasksId().contains(subtaskId2), "Эпик содержит id удаленной подзадачи.");

        taskManager.clearSubtasks();
        assertEquals(0, taskManager.getSubtasksList().size(), "Неверное количество подзадач в списке.");

        epic = getEpicById(epicId);
        epic2 = getEpicById(epicId2);
        assertTrue(epic.getListOfSubtasksId().isEmpty() && epic2.getListOfSubtasksId().isEmpty(),
                "Список подзадач эпиков не пустой после очистки подзадач.");

        taskManager.deleteEpicById(epicId);
        assertEquals(1, taskManager.getEpicsList().size(), "Неверное количество в списке");

        taskManager.clearEpics();
        assertEquals(0, taskManager.getEpicsList().size(), "Неверное количество в списке");
    }

    static <T extends Task> boolean compareTasksByFields(T task1, T task2) {

        if (task1 == null || task2 == null) {
            return false;
        }

        String name1 = task1.getName();
        String description1 = task1.getDescription();
        TaskStatus status1 = task1.getStatus();
        LocalDateTime startTime1 = task1.getStartTime();
        Duration duration1 = task1.getDuration();
        LocalDateTime endTime1 = task1.getEndTime();

        String name2 = task2.getName();
        String description2 = task2.getDescription();
        TaskStatus status2 = task2.getStatus();
        LocalDateTime startTime2 = task2.getStartTime();
        Duration duration2 = task2.getDuration();
        LocalDateTime endTime2 = task2.getEndTime();

        boolean result = name1.equals(name2) && description1.equals(description2);

        if (task1.getClass() == Task.class || task1.getClass() == Subtask.class) {
            result = result && (status1 == status2);
        }

        if (startTime1 == null && startTime2 != null) {
            result = false;
        } else if (startTime2 == null && startTime1 != null) {
            result = false;
        } else if (startTime1 != null) {
            result = result && startTime1.equals(startTime2);
        }

        if (endTime1 == null && endTime2 != null) {
            result = false;
        } else if (endTime2 == null && endTime1 != null) {
            result = false;
        } else if (endTime1 != null) {
            result = result && endTime1.equals(endTime2);
        }

        if (duration1 == null && duration2 != null) {
            result = false;
        } else if (duration2 == null && duration1 != null) {
            result = false;
        } else if (duration1 != null) {
            result = result && duration1.equals(duration2);
        }

        if (task1 instanceof Subtask) {
            int epicId1 = ((Subtask) task1).getEpicId();
            int epicId2 = ((Subtask) task2).getEpicId();
            result = result && (epicId1 == epicId2);
        }

        if (task1 instanceof Epic) {
            List<Integer> subtaskIds1 = ((Epic) task1).getListOfSubtasksId();
            List<Integer> subtaskIds2 = ((Epic) task2).getListOfSubtasksId();
            result = result && Arrays.equals(subtaskIds1.toArray(), subtaskIds2.toArray());
        }

        return result;
    }

    Task getTaskById(int id) {
        Optional<Task> optional = taskManager.getTaskById(id);
        assertFalse(optional.isEmpty(), "Запрошена задача с недопустимым id");
        return optional.get();
    }

    Epic getEpicById(int id) {
        Optional<Epic> optional = taskManager.getEpicById(id);
        assertFalse(optional.isEmpty(), "Запрошена задача с недопустимым id");
        return optional.get();
    }

    Subtask getSubtaskById(int id) {
        Optional<Subtask> optional = taskManager.getSubtaskById(id);
        assertFalse(optional.isEmpty(), "Запрошена задача с недопустимым id");
        return optional.get();
    }
}
