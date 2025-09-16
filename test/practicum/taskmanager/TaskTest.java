package practicum.taskmanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void equalsById() {
        Task task1 = new Task(1, "name", TaskStatus.NEW, "");
        Task task2 = new Task(1, "name", TaskStatus.NEW, "");

        assertEquals(task1, task2);
    }

}