package practicum;

import practicum.taskmanager.Epic;
import practicum.taskmanager.Subtask;
import practicum.taskmanager.Task;
import practicum.taskmanager.TaskManager;
import practicum.taskmanager.TaskStatus;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

//        TaskManager taskManager = new TaskManager();
//
//        Task task1 = new Task("Задача 1.", TaskStatus.NEW);
//        task1.setDescription("Описание задачи 1.");
//        taskManager.createTask(task1);
//        System.out.println("1. Одна задача в списке, статус NEW: " + taskManager.getTasksList());
//
//        Task task2 = new Task("Задача 2.", TaskStatus.IN_PROGRESS);
//        taskManager.createTask(task2);
//        System.out.println("2. Две задачи в списке, статус NEW и IN_PROGRESS: " + taskManager.getTasksList());
//
//        Epic epic1 = new Epic("Эпик1.");
//        taskManager.createEpic(epic1);
//
//        Subtask subtask11 = new Subtask("Подзадача 1.1.", TaskStatus.IN_PROGRESS, epic1.getId());
//        taskManager.createSubtask(subtask11);
//        Subtask subtask12 = new Subtask("Подзадача 1.2.", TaskStatus.NEW, epic1.getId());
//        taskManager.createSubtask(subtask12);
//
//        System.out.println("3. Эпик 1 в статусе IN_PROGRESS: " + epic1);
//        System.out.println("4. Подзадачи эпика 1 в статусах NEW и IN_PROGRESS: " + taskManager.getSubtasksOfEpic(epic1));
//
//        Epic epic2 = new Epic("Эпик2.");
//        taskManager.createEpic(epic2);
//        Subtask subtask21 = new Subtask("Подзадача 2.1.", TaskStatus.NEW, epic2.getId());
//        taskManager.createSubtask(subtask21);
//
//        System.out.println("4. Эпик 2 в статусе NEW: " + epic2);
//        System.out.println("5. Подзадача эпика 2 в статусе NEW: " + taskManager.getSubtasksOfEpic(epic2));
//        System.out.println("6. Список эпиков: " + taskManager.getEpicsList());
//        System.out.println("7. Список всех подзадач с id эпиков: " + taskManager.getSubtasksList());
//
//        subtask11 = new Subtask(4, "Подзадача 1.1. Изменение.", TaskStatus.DONE, epic1.getId());
//        taskManager.updateSubtask(subtask11);
//
//        subtask12 = new Subtask(subtask12.getId(), subtask12.getName(), TaskStatus.DONE, epic1.getId());
//        taskManager.updateSubtask(subtask12);
//
//        System.out.println("8. Эпик 1 в статусе DONE: " + epic1);
//
//        subtask21 = new Subtask(7, "Подзадача 2.1. Изменение.", TaskStatus.IN_PROGRESS, 6);
//        taskManager.updateSubtask(subtask21);
//
//        System.out.println("9. Эпик 2 в статусе IN_PROGRESS: " + epic2);
//
//        Task task1updated = new Task(task1.getId(), task1.getName(), TaskStatus.DONE);
//        taskManager.updateTask(task1updated);
//
//        System.out.println("10. Задача 1 завершена: " + taskManager.getTasksList());
//
//        Epic epic2updated = new Epic(epic2.getId(), epic2.getName() + "!");
//        taskManager.updateEpic(epic2updated);
//
//        System.out.println("11. У эпика 2 изменено наименование c \"!\": " + taskManager.getEpicById(6));
//
//        taskManager.deleteTaskById(task1.getId());
//        System.out.println("12. Задача 1 удалена: " + taskManager.getTasksList());
//
//        taskManager.deleteSubtaskById(subtask21.getId());
//        System.out.println("13. Подзадача эпика 2 удалена, эпик 2 в статусе NEW: " + taskManager.getEpicsList());
//
//        taskManager.deleteEpicById(epic2.getId());
//        System.out.println("14. Эпик 2 удален. Остался один эпик 1: " + taskManager.getEpicsList());
//        System.out.println("15. Остались подзадачи только 1го эпика: " + taskManager.getSubtasksList());
//
//        taskManager.clearTasks();
//        System.out.println("16. Удалены все задачи: " + taskManager.getTasksList());
//
//        taskManager.clearSubtasks();
//        System.out.println("18. Через clear удалены все подзадачи, эпик в статусе NEW, подзадач нет: " + taskManager.getEpicsList() +
//                " " + taskManager.getSubtasksList());
//
//        Subtask subtask13 = new Subtask("Подзадача 1.3", TaskStatus.DONE, epic1.getId());
//        taskManager.createSubtask(subtask13);
//        Subtask subtask14 = new Subtask("Подзадача 1.4", TaskStatus.DONE, epic1.getId());
//        taskManager.createSubtask(subtask14);
//
//        System.out.println("19. Добавлены 2 поздадачи в эпик 1. Эпик 1 в статусе DONE: " + taskManager.getEpicsList() +
//                " " + taskManager.getSubtasksList());
//
//        taskManager.clearEpics();
//        System.out.println("20. Через clear удален оставишийся эпик, подзадач нет: " + taskManager.getEpicsList() + " "
//                + taskManager.getSubtasksList());
    }
}
