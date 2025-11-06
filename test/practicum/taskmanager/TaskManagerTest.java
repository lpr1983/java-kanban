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

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    static TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
    }

    public static <T extends Task> boolean compareTasksByFields(T task1, T task2) {

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
        } else if (startTime1 != null && startTime2 != null) {
            result = result && startTime1.equals(startTime2);
        }

        if (endTime1 == null && endTime2 != null) {
            result = false;
        } else if (endTime2 == null && endTime1 != null) {
            result = false;
        } else if (endTime1 != null && endTime2 != null) {
            result = result && endTime1.equals(endTime2);
        }

        if (duration1 == null && duration2 != null) {
            result = false;
        } else if (duration2 == null && duration1 != null) {
            result = false;
        } else if (duration1 != null && duration2 != null) {
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

    @Test
    void createTasks() {

        Task task = new Task(999, "Task 1", TaskStatus.NEW, "Some description");
        int taskId = taskManager.createTask(task);
        Task createdTask = taskManager.getTaskById(taskId);

        // Задача создалась, нашлась по возвращенному id
        assertNotNull(createdTask, "Не найдена задача по возвращенному Id");
        // Переданные поля совпадают с тем, что записал менеджер
        assertTrue(compareTasksByFields(task, createdTask),
                "Поля переданной и сохраненной задач не совпададают");
        // По переданному в самой задаче Id ничего не должно находиться
        assertNull(taskManager.getTaskById(999), "Id должен переопределиться при создании");

        Epic epic = new Epic(999, "Epic 1", "Epic desсription");
        int epicId = taskManager.createEpic(epic);

        Epic createdEpic = taskManager.getEpicById(epicId);

        assertNotNull(createdEpic, "Не найдена задача по Id");
        assertTrue(compareTasksByFields(epic, createdEpic),
                "Поля переданной и сохраненной задач не совпададают");
        assertNull(taskManager.getEpicById(999), "Id должен переопределиться при создании");

        Subtask subtask = new Subtask(999, "Subtask 1_1", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask createdSubtask = taskManager.getSubtaskById(subtaskId);
        assertNotNull(createdSubtask, "Не найдена задача по Id");
        assertTrue(compareTasksByFields(subtask, createdSubtask),
                "Поля переданной и сохраненной задач не совпададают");
        assertNull(taskManager.getSubtaskById(999), "Id должен переопределиться при создании");

        Epic addedEpic = taskManager.getEpicById(epicId);
        assertTrue(addedEpic.getListOfSubtasksId().contains(subtaskId), "Эпик не содержит id созденной подзадачи.");

        // Состояние epic изменилось при добавлении подзадачи
        assertEquals(TaskStatus.IN_PROGRESS, addedEpic.getStatus(), "Статус должен быть IN_PROGRESS");

        // Задача, оставшаяся у пользователя, не может изменить данные задачи в менеджере
        createdTask.setStatus(TaskStatus.DONE);
        TaskStatus storedStatus = taskManager.getTaskById(taskId).getStatus();
        assertNotEquals(createdTask.getStatus(), storedStatus,
                "Пользователь не должен иметь возможность изменить данные без update");

        createdEpic.setDescription("Новое описание");
        String storedDescription = taskManager.getEpicById(epicId).getDescription();
        assertNotEquals(createdEpic.getDescription(), storedDescription,
                "Пользователь не должен иметь возможность изменить данные без update");

        createdSubtask.setDescription("Новое описание");
        storedDescription = taskManager.getSubtaskById(subtaskId).getDescription();
        assertNotEquals(createdSubtask.getDescription(), storedDescription,
                "Пользователь не должен иметь возможность изменить данные без update");

        assertEquals(1, taskManager.getTasksList().size(), "Неверное количество задач");
        assertEquals(1, taskManager.getEpicsList().size(), "Неверное количество эпиков");
        assertEquals(1, taskManager.getSubtasksList().size(), "Неверное количество подзадач");
        assertEquals(1, taskManager.getSubtasksOfEpic(epicId).size(), "Неверное количество подзадач у эпика");
    }

    @Test
    void updateTasks() {

        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        int taskId = taskManager.createTask(task);

        Epic epic = new Epic("Epic 1", "Epic desсription");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(9999, "Subtask 1_1", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId1 = taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask(9999, "Subtask 1_2", TaskStatus.DONE, epicId, "Subtask description");
        taskManager.createSubtask(subtask2);

        Task taskToUpdate = taskManager.getTaskById(taskId);
        taskToUpdate.setStatus(TaskStatus.IN_PROGRESS);
        taskToUpdate.setDescription("new description");
        taskManager.updateTask(taskToUpdate);

        Task savedTask = taskManager.getTaskById(taskId);
        assertTrue(compareTasksByFields(taskToUpdate, savedTask),
                "Не совпадают поля переданной и сохраненной задачи");

        Epic epicToUpdate = taskManager.getEpicById(epicId);

        // Попытка изменить статус эпика в обход менеджера задач
        epicToUpdate.setStatus(TaskStatus.DONE);
        epicToUpdate = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.IN_PROGRESS, epicToUpdate.getStatus(),
                "Не все подзадачи завершены, должен быть статус IN_PROGRESS");

        epicToUpdate.setDescription("Новое описание");
        taskManager.updateEpic(epicToUpdate);

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertTrue(compareTasksByFields(epicToUpdate, savedEpic),
                "Не совпадают поля переданной и сохраненной задачи");

        Subtask subtask1ToUpdate = taskManager.getSubtaskById(subtaskId1);
        subtask1ToUpdate.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1ToUpdate);

        Subtask savedSubtask1 = taskManager.getSubtaskById(subtaskId1);
        assertTrue(compareTasksByFields(subtask1ToUpdate, savedSubtask1),
                "Не совпадают переданные и сохраненные поля");

        savedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.DONE, savedEpic.getStatus(),
                "Все подзадачи завершены, должен быть статус DONE");
    }

    @Test
    void historyStoring() {

        Task task = new Task(999, "Task 1", TaskStatus.NEW, "Some description");
        int taskId = taskManager.createTask(task);
        Task createdTask = taskManager.getTaskById(taskId);

        Task task2 = new Task(999, "Task 2", TaskStatus.NEW, "Some description");
        int taskId2 = taskManager.createTask(task2);
        taskManager.getTaskById(taskId2);

        Epic epic = new Epic(999, "Epic 1", "Epic desсription");
        int epicId = taskManager.createEpic(epic);
        taskManager.getEpicById(epicId);

        Subtask subtask = new Subtask(999, "Subtask 1_1", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId = taskManager.createSubtask(subtask);
        taskManager.getSubtaskById(subtaskId);

        Subtask subtask2 = new Subtask(999, "Subtask 1_2", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId2 = taskManager.createSubtask(subtask2);
        taskManager.getSubtaskById(subtaskId2);

        assertEquals(5, taskManager.getHistory().size(), "Неправильное количество задач в истории");

        createdTask.setStatus(TaskStatus.DONE);
        taskManager.updateTask(createdTask);

        TaskStatus statusInHistory = taskManager.getHistory().getFirst().getStatus();

        assertNotEquals(createdTask.getStatus(), statusInHistory,
                "История должна хранить версию задачи на момент вызова метода getById");

        Task taskById = taskManager.getTaskById(taskId);
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
        taskManager.getSubtaskById(subtaskId3);

        taskManager.deleteEpicById(epicId);
        assertEquals(0, taskManager.getHistory().size(), "Неверный размер истории после удаления");
    }

    @Test
    void deleteAndClearTasks() {

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

        epic = taskManager.getEpicById(epicId);
        assertTrue(epic.getListOfSubtasksId().contains(subtaskId1), "В эпике нет id добавленной подзадачи.");
        assertFalse(epic.getListOfSubtasksId().contains(subtaskId2), "Эпик содержит id удаленной подзадачи.");

        taskManager.clearSubtasks();
        assertEquals(0, taskManager.getSubtasksList().size(), "Неверное количество подзадач в списке.");

        epic = taskManager.getEpicById(epicId);
        epic2 = taskManager.getEpicById(epicId2);
        assertTrue(epic.getListOfSubtasksId().isEmpty() && epic2.getListOfSubtasksId().isEmpty(),
                "Список подзадач эпиков не пустой после очистки подзадач.");

        taskManager.deleteEpicById(epicId);
        assertEquals(1, taskManager.getEpicsList().size(), "Неверное количество в списке");

        taskManager.clearEpics();
        assertEquals(0, taskManager.getEpicsList().size(), "Неверное количество в списке");
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

        epic = taskManager.getEpicById(epicId);

        assertEquals(minStartTime, epic.getStartTime(), "Неправильное время начала у epic");
        assertEquals(subtask2.getEndTime(), epic.getEndTime(), "Неправильное время окончания у epic");
        assertEquals(duration1.plus(duration2), epic.getDuration(), "Неправильная продолжительность у epic");
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
        assertTrue(compareTasksByFields(task, sortedTasks.getFirst()), "Добавленная задача не совпадает с задачей в списке getPrioritizedTasks");
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
        task = taskManager.getTaskById(taskId);
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

        subtask1 = taskManager.getSubtaskById(subtaskId);
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

        subtask1 = taskManager.getSubtaskById(subtaskId);
        task = taskManager.getTaskById(taskId);
        task2 = taskManager.getTaskById(task2Id);

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
        task2 = taskManager.getTaskById(taskId2);
        taskManager.updateTask(task2);

        // Обновление с пересечением
        Task taskToCheck = taskManager.getTaskById(taskId2);
        taskToCheck.setStartTime(testTime);
        Assertions.assertThrows(IllegalArgumentException.class, () -> taskManager.updateTask(taskToCheck));

        Epic epic = new Epic("Epic", "");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("subtask", TaskStatus.NEW, epicId, "");
        subtask.setStartTime(testTime.plusDays(2));
        subtask.setDuration(Duration.ofDays(1));
        int subtaskId = taskManager.createSubtask(subtask);

        // Обновление без пересечения
        Subtask subtaskToCheck = taskManager.getSubtaskById(subtaskId);
        taskManager.updateSubtask(subtaskToCheck);

        // Обновление с пересечением
        subtaskToCheck.setStartTime(testTime);
        Assertions.assertThrows(IllegalArgumentException.class, () -> taskManager.updateSubtask(subtaskToCheck));
    }

}