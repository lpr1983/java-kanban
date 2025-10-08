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
    void add() {
        int historySize = 1000;
        for (int i = 1; i <= historySize; i++) {
            Task newTask = new Task(i,"Task" + i, TaskStatus.NEW, "" );
            historyManager.add(newTask);
        }

        assertEquals(historySize, historyManager.getHistory().size(), "Неправильное количество элементов.");

        Task firstTask = new Task(1, "Task1", TaskStatus.NEW, "");
        assertTrue(historyManager.getHistory().getFirst().equals(firstTask),
                "Первая задача должна быть первой.");

        Task lastTask = new Task(historySize, "Task" + historySize, TaskStatus.NEW, "");
        assertTrue(historyManager.getHistory().getLast().equals(lastTask),
                "Последняя задача должна быть последней.");

        Task task = new Task(1, "Task1", TaskStatus.NEW, "");
        assertTrue(historyManager.getHistory().contains(task), "Добавленная задача отсутствует.");

        Task unaddedTask = new Task(historySize + 1, "Task1", TaskStatus.NEW, "");
        assertFalse(historyManager.getHistory().contains(unaddedTask), "Недобаленная задача присутствует.");

        historyManager.add(task);
        assertEquals(historySize, historyManager.getHistory().size(),
                "После добавления имеющейся задачи количество элементов не должно измениться.");

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
            Task newTask = new Task(i,"Task" + i, TaskStatus.NEW, "" );
            historyManager.add(newTask);
        }

        Task task1 = new Task(1, "Task1", TaskStatus.NEW, "");
        historyManager.remove(1);

        assertEquals(historySize - 1, historyManager.getHistory().size(),
                "Размер истории не уменьшился на 1.");

        assertFalse(historyManager.getHistory().contains(task1),
                "Удаленная задача осталась в истории.");

        Task task2 = new Task(2, "Task2", TaskStatus.NEW, "");
        assertTrue(historyManager.getHistory().getFirst().equals(task2), "Вторая задача не стала первой.");
    }

}