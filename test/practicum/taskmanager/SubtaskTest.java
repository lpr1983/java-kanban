package practicum.taskmanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void equalsById() {
        Subtask task1 = new Subtask(1, "", TaskStatus.NEW, 1,"");
        Subtask task2 = new Subtask(1, "", TaskStatus.NEW, 1,"");
        assertEquals(task1, task2);
    }

}