package practicum.taskmanager;

import org.junit.jupiter.api.Test;
import practicum.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void equalsById() {
        Task task1 = new Task(1, "name", TaskStatus.NEW, "");
        Task task2 = new Task(1, "name", TaskStatus.NEW, "");

        assertEquals(task1, task2);
    }

    @Test
    void getEndTime() {
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        Duration duration = Duration.ofDays(1);

        Task taskWithDuration = new Task(1, "name", TaskStatus.NEW, "");
        taskWithDuration.setStartTime(startTime);
        taskWithDuration.setDuration(duration);

        LocalDateTime endTime = taskWithDuration.getEndTime();
        LocalDateTime expectedTime = LocalDateTime.of(2025, 1, 2, 0, 0);
        assertEquals(expectedTime, endTime, "Время окончания задачи рассчитана неправильно");
    }

}