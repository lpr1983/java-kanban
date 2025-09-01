package practicum.taskmanager;

import java.util.ArrayList;

public class Epic extends Task{
    private final ArrayList<Integer> listOfSubtasksId = new ArrayList<>();

    public Epic(int id, String name) {
        super(id, name, TaskStatus.NEW);
    }

    public Epic(String name) {
        super(name, TaskStatus.NEW);
    }

    protected ArrayList<Integer> getListOfSubtasksId() {
        return listOfSubtasksId;
    }

    @Override
    public void setStatus(TaskStatus status) {
        // Не должно быть возможности установить статус Epic вызовом этого метода.
    }

    protected void setCalculatedStatus(TaskStatus status) {
        super.setStatus(status);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + super.getId() +
                ", name='" + super.getName() + '\'' +
                ", description.length='" + super.getDescription().length() + '\'' +
                ", status=" + super.getStatus() +
                '}';
    }

}
