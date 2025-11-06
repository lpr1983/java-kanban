package practicum.taskmanager;

import org.junit.jupiter.api.Test;
import practicum.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

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
}