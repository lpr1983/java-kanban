package practicum.task.serializer;

import practicum.task.Epic;
import practicum.task.Subtask;
import practicum.task.Task;
import practicum.task.TaskType;
import practicum.taskmanager.TaskStatus;

public class CsvSerializer {

    private CsvSerializer() {

    }

    public static String toCsvString(Task task, TaskType taskType) {
        String csvString = "";

        //id,type,name,status,description,epic
        String formatString = "%d,%s,%s,%s,%s,";

        csvString = String.format(formatString,
                task.getId(),
                taskType,
                task.getName(),
                task.getStatus(),
                task.getDescription()
        );

        if (taskType == TaskType.SUBTASK) {
            csvString += ((Subtask) task).getEpicId();
        }

        return csvString;
    }

    public static ResultOfSerialization fromCsvString(String csvString) {

        // id,type,name,status,description,epic
        final int idField = 0;
        final int taskTypeField = 1;
        final int nameField = 2;
        final int statusField = 3;
        final int descriptionField = 4;
        final int epicIdField = 5;

        String[] fields = csvString.split(",");

        int id = Integer.parseInt(fields[idField]);
        TaskType taskType = TaskType.valueOf(fields[taskTypeField]);
        String name = fields[nameField];
        TaskStatus status = TaskStatus.valueOf(fields[statusField]);
        String description = fields[descriptionField];
        Integer epicId = null;

        if (fields.length == 6) {
            epicId = Integer.valueOf(fields[epicIdField]);
        }

        Task task = switch (taskType) {
            case TASK -> new Task(id, name, status, description);
            case EPIC -> new Epic(id, name, description);
            case SUBTASK -> new Subtask(id, name, status, epicId, description);
            default -> throw new IllegalArgumentException("Not supported type");
        };

        return new ResultOfSerialization(taskType, task);
    }
}
