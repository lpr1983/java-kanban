package practicum.task;

import practicum.taskmanager.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> listOfSubtasksId = new ArrayList<>();

    public Epic(int id, String name, String description) {
        super(id, name, TaskStatus.NEW, description);
    }

    public Epic(String name, String description) {
        super(name, TaskStatus.NEW, description);
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