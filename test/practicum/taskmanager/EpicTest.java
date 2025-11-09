package practicum.taskmanager;

import org.junit.jupiter.api.Test;
import practicum.task.Epic;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void equalsById() {
        Epic task1 = new Epic(1, "name", "");
        Epic task2 = new Epic(1, "name", "");
        assertEquals(task1, task2);
    }

    @Test
    void getEndTime() {

        Epic epic = new Epic(1, "epic", "");
        epic.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0));
        epic.setDuration(Duration.ofDays(1));

        epic.setEndTime(LocalDateTime.of(2025, 1, 1, 0, 0));

        assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), epic.getEndTime(), "endTime неправильный");
    }

}