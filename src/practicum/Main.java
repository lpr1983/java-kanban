package practicum;

import practicum.task.Epic;
import practicum.taskmanager.Managers;
import practicum.task.Subtask;
import practicum.task.Task;
import practicum.taskmanager.TaskManager;
import practicum.taskmanager.TaskStatus;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();

        operationsExampleSprint6(taskManager);
    }

    private static void operationsExampleSprint6(TaskManager taskManager) {
        // Условие:
        // 1. Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач.
        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        int taskId = taskManager.createTask(task);

        Task task2 = new Task("Task 2", TaskStatus.NEW, "Some description");
        int taskId2 = taskManager.createTask(task2);

        Epic epic = new Epic("Epic 1", "Epic desription");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1_1", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask subtask2 = new Subtask("Subtask 1_2", TaskStatus.NEW, epicId, "");
        int subtaskId2 = taskManager.createSubtask(subtask2);

        Subtask subtask3 = new Subtask("Subtask 1_3", TaskStatus.NEW, epicId, "");
        int subtaskId3 = taskManager.createSubtask(subtask3);

        Epic epic2 = new Epic("Epic 2", "Epic 2 desription");
        int epicId2 = taskManager.createEpic(epic2);

        // 2. Запросите созданные задачи несколько раз в разном порядке.
        // 3. После каждого запроса выведите историю и убедитесь, что в ней нет повторов.
        taskManager.getTaskById(taskId);
        System.out.println("Task1:"  + taskManager.getHistory());

        taskManager.getTaskById(taskId2);
        System.out.println("Task1, Task2: " + taskManager.getHistory());

        taskManager.getTaskById(taskId);
        System.out.println("Task2, Task1: " + taskManager.getHistory());

        taskManager.getEpicById(epicId);
        System.out.println("Task2, Task1, Epic: " + taskManager.getHistory());

        taskManager.getSubtaskById(subtaskId3);
        System.out.println("Task2, Task1, Epic, Subtask3: " + taskManager.getHistory());

        taskManager.getSubtaskById(subtaskId);
        System.out.println("Task2, Task1, Epic, Subtask3, Subtask: " + taskManager.getHistory());

        taskManager.getSubtaskById(subtaskId2);
        System.out.println("Task2, Task1, Epic, Subtask3, Subtask, Subtask2: " + taskManager.getHistory());

        // 4. Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться.
        taskManager.deleteSubtaskById(subtaskId);
        System.out.println("Task2, Task1, Epic, Subtask3, Subtask2: " + taskManager.getHistory());

        taskManager.getEpicById(epicId2);
        System.out.println("Task2, Task1, Epic, Subtask3, Subtask2, Epic2: " + taskManager.getHistory());

        // 5. Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи.
        taskManager.deleteEpicById(epicId);
        System.out.println("Task2, Task1, Epic2: " + taskManager.getHistory());
    }

}
