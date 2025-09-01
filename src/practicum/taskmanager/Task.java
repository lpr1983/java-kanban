package practicum.taskmanager;

public class Task {
    private int id;
    private String name;
    private String description = "";
    private TaskStatus status;

    public Task(String name, TaskStatus status) {
        this.name = name;
        this.status = status;
    }

    public Task(int id, String name, TaskStatus status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description.length='" + description.length() + '\'' +
                ", status=" + status +
                '}';
    }
}