package practicum.taskmanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void equalsById() {
        Epic task1 = new Epic(1, "name","");
        Epic task2 = new Epic(1, "name","");
        assertEquals(task1, task2);
    }

    @Test
    void setStatusDoesntChangeEpicStatus() {
        Epic task1 = new Epic(1, "name","");
        task1.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.NEW, task1.getStatus());
    }

}