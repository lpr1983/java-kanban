package practicum.task;

import practicum.taskmanager.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, TaskStatus status, int epicId, String description) {
        super(name, status, description);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, TaskStatus status, int epicId, String description) {
        super(id, name, status, description);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, TaskStatus status, int epicId, String description, LocalDateTime startTime, Duration duration) {
        super(id, name, status, description, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", epic_id='" + epicId +
                ", name='" + name + '\'' +
                ", description.length='" + description.length() + '\'' +
                ", status=" + this.getStatus() +
                '}';
    }
}
