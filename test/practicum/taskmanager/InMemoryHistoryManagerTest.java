package practicum.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.task.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    static HistoryManager historyManager;

    @BeforeEach
    void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void getHistory() {

        assertTrue(historyManager.getHistory().isEmpty(), "При создании менеджера" +
                " должна возвращаться пустая история задач");

        Task task = new Task(1, "Task1", TaskStatus.NEW, "");
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size(), "В истории должна быть одна задача");
        assertTrue(historyManager.getHistory().getLast().equals(task),
                "В истории должна быть добавленная задача.");
    }

    @Test
    void add() {
        int historySize = 1000;
        for (int i = 1; i <= historySize; i++) {
            Task newTask = new Task(i, "Task" + i, TaskStatus.NEW, "");
            historyManager.add(newTask);
        }

        assertEquals(historySize, historyManager.getHistory().size(), "Неправильное количество элементов.");

        Task firstTask = new Task(1, "Task1", TaskStatus.NEW, "");
        assertTrue(historyManager.getHistory().getFirst().equals(firstTask),
                "Первая задача должна быть первой.");

        Task lastTask = new Task(historySize, "Task" + historySize, TaskStatus.NEW, "");
        assertTrue(historyManager.getHistory().getLast().equals(lastTask),
                "Последняя задача должна быть последней.");

        // Порверка дублирования
        Task task = new Task(1, "Task1", TaskStatus.NEW, "");

        historyManager.add(task);
        assertEquals(historySize, historyManager.getHistory().size(),
                "После добавления имеющейся задачи количество элементов не должно измениться.");

        int amountOfTask1 = 0;
        for (Task iterTask : historyManager.getHistory()) {
            if (iterTask.equals(task)) {
                amountOfTask1++;
            }
        }

        assertEquals(1, amountOfTask1, "Задача должна присутствовать в истории только 1 раз.");

        assertTrue(historyManager.getHistory().getLast().equals(task),
                "Добавленная повторно задача должна быть последней.");

        assertFalse(historyManager.getHistory().getFirst().equals(task),
                "При добавлении задача должна удалиться с прежнего места и попасть в конец истории.");

        Task task2 = new Task(2, "Task2", TaskStatus.NEW, "");
        assertTrue(historyManager.getHistory().getFirst().equals(task2),
                "Вторая задача должна стать первой.");
    }

    @Test
    void remove() {
        int historySize = 10;
        for (int i = 1; i <= historySize; i++) {
            Task newTask = new Task(i, "Task" + i, TaskStatus.NEW, "");
            historyManager.add(newTask);
        }

        // Удаление начало. 1_2_3_4_5_6_7_8_9_10 -> 2_3_4_5_6_7_8_9_10
        Task task1 = new Task(1, "Task1", TaskStatus.NEW, "");
        historyManager.remove(1);

        assertEquals(historySize - 1, historyManager.getHistory().size(),
                "Размер истории не уменьшился на 1.");

        assertFalse(historyManager.getHistory().contains(task1),
                "Удаленная задача осталась в истории.");

        // Удаление конец. 2_3_4_5_6_7_8_9_10 -> 2_3_4_5_6_7_8_9
        historyManager.remove(10);
        assertEquals(historySize - 2, historyManager.getHistory().size(),
                "Размер истории не уменьшился на 1.");

        Task task9 = new Task(9, "Task9", TaskStatus.NEW, "");
        assertEquals(task9, historyManager.getHistory().getLast(), "Предпоследняя задача не стала последней.");

        // Удаление середина. 2_3_4_5_6_7_8_9 -> 2_3_4_6_7_8_9
        historyManager.remove(5);
        assertEquals(historySize - 3, historyManager.getHistory().size(),
                "Размер истории не уменьшился на 1.");

        Task task6 = new Task(6, "Task6", TaskStatus.NEW, "");
        assertEquals(task6, historyManager.getHistory().get(3), "6я задача не стала на место 5й.");
    }

}