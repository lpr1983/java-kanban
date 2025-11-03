package practicum.task;

import practicum.taskmanager.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> listOfSubtasksId = new ArrayList<>();
    protected LocalDateTime endTime;

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Epic(int id, String name, String description) {
        super(id, name, TaskStatus.NEW, description);
    }

    public Epic(String name, String description) {
        super(name, TaskStatus.NEW, description);
    }

    public Epic(int id, String name, TaskStatus status, String description) {
        super(id, name, status, description);
    }

    public Epic(int id, String name, TaskStatus status, String description, LocalDateTime startTime, Duration duration, LocalDateTime endTime) {
        super(id, name, status, description, startTime, duration);
        this.endTime = endTime;
    }

    public List<Integer> getListOfSubtasksId() {
        return new ArrayList<>(listOfSubtasksId);
    }

    public void deleteSubtaskId(int subtaskId) {
        listOfSubtasksId.remove(Integer.valueOf(subtaskId));
    }

    public void  clearSubtasksId() {
        listOfSubtasksId.clear();
    }

    public void addSubtaskId(int subtaskId) {

        if (listOfSubtasksId.contains(subtaskId)) {
            return;
        }
        listOfSubtasksId.add(subtaskId);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description.length='" + description.length() + '\'' +
                ", listOfSubtasksId.size='" + listOfSubtasksId.size() + '\'' +
                ", status=" + status +
                '}';
    }

}