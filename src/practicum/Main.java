package practicum;

import practicum.taskmanager.Epic;
import practicum.taskmanager.Managers;
import practicum.taskmanager.Subtask;
import practicum.taskmanager.Task;
import practicum.taskmanager.TaskManager;
import practicum.taskmanager.TaskStatus;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();

        operationsExample(taskManager);

        printAllTasks(taskManager);
    }

    private static void operationsExample(TaskManager taskManager) {
        Task task = new Task("Task 1", TaskStatus.NEW, "Some description");
        int taskId = taskManager.createTask(task);

        task = taskManager.getTaskById(taskId);
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setDescription("Changed description");

        Epic epic = new Epic("Epic 1", "Epic desription");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask(9999, "Subtask 1_1", TaskStatus.IN_PROGRESS , epicId, "Subtask description");
        int subtaskId = taskManager.createSubtask(subtask);

        subtask = taskManager.getSubtaskById(subtaskId);
        subtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask);
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

        System.out.println("История (хранит версии на момент просмотра):");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }

}
