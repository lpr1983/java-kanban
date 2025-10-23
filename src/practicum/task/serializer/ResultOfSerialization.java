package practicum.task.serializer;

import practicum.task.Task;
import practicum.task.TaskType;

public class ResultOfSerialization {

    private TaskType taskType;
    private Task task;

    ResultOfSerialization(TaskType taskType, Task task) {
        this.task = task;
        this.taskType = taskType;
    }

    public Task getTask() {
        return task;
    }

    public TaskType getTaskType() {
        return taskType;
    }
}
