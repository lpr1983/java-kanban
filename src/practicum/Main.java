package practicum;

import practicum.task.Epic;
import practicum.taskmanager.FileBackedTaskManager;
import practicum.taskmanager.Managers;
import practicum.task.Subtask;
import practicum.task.Task;
import practicum.taskmanager.TaskManager;
import practicum.taskmanager.TaskStatus;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();

        operationsExample(taskManager);

        printAllTasks(taskManager);

        try {
            System.out.println("Восстановление");

            taskManager = FileBackedTaskManager.loadFromFile("tasks.csv");

            printAllTasks(taskManager);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void printAllTasks(TaskManager manager) {

        System.out.println("Задачи:");
        for (Task task : manager.getTasksList()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpicsList()) {
            System.out.println(epic);

            for (Task task : manager.getSubtasksOfEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasksList()) {
            System.out.println(subtask);
        }
    }

    private static void operationsExample(TaskManager taskManager) {

        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        int taskId = taskManager.createTask(task);

        Task task2 = new Task("Task 2", TaskStatus.NEW, "Some description");
        taskManager.createTask(task2);

        Epic epic = new Epic("Epic 1", "Epic desription");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1_1", TaskStatus.IN_PROGRESS, epicId, "Subtask description");
        taskManager.createSubtask(subtask);

        Subtask subtask2 = new Subtask("Subtask 1_2", TaskStatus.NEW, epicId, "");
        taskManager.createSubtask(subtask2);

        Subtask subtask3 = new Subtask("Subtask 1_3", TaskStatus.NEW, epicId, "");
        taskManager.createSubtask(subtask3);

        Epic epic2 = new Epic("Epic 2", "Epic 2 desription");
        taskManager.createEpic(epic2);
    }

}
