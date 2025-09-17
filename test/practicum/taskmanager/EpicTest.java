package practicum.taskmanager;

import org.junit.jupiter.api.Test;
import practicum.task.Epic;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void equalsById() {
        Epic task1 = new Epic(1, "name","");
        Epic task2 = new Epic(1, "name","");
        assertEquals(task1, task2);
    }

}