package practicum.task.serializer;

import practicum.task.Epic;
import practicum.task.Subtask;
import practicum.task.Task;
import practicum.task.TaskType;
import practicum.taskmanager.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvSerializer {
    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private CsvSerializer() {

    }

    public static String toCsvString(Task task, TaskType taskType) {
        String csvString = "";

        //id,type,name,status,description,epic,startTime,duration,endTime
        csvString += String.format("%d,", task.getId());
        csvString += String.format("%s,", taskType);
        csvString += String.format("%s,", task.getName());
        csvString += String.format("%s,", task.getStatus());
        csvString += String.format("%s,", task.getDescription());

        if (taskType == TaskType.SUBTASK) {
            csvString += String.format("%d,", ((Subtask) task).getEpicId());
        } else {
            csvString += ",";
        }

        if (task.getStartTime() != null) {
            csvString += String.format("%s,", task.getStartTime().format(dateTimeFormatter));
        } else {
            csvString += ",";
        }

        if (task.getDuration() != null) {
            csvString += String.format("%s,", task.getDuration().toMinutes());
        } else {
            csvString += ",";
        }

        if (task.getEndTime() != null) {
            csvString += String.format("%s", task.getEndTime().format(dateTimeFormatter));
        }

        return csvString;
    }

    public static ResultOfSerialization fromCsvString(String csvString) {

        //id,type,name,status,description,epic,startTime,duration,endTime
        final int idField = 0;
        final int taskTypeField = 1;
        final int nameField = 2;
        final int statusField = 3;
        final int descriptionField = 4;
        final int epicIdField = 5;
        final int startTimeField = 6;
        final int durationField = 7;
        final int endTimeField = 8;

        String[] fields = csvString.split(",", 9);

        int id = Integer.parseInt(fields[idField]);
        TaskType taskType = TaskType.valueOf(fields[taskTypeField]);
        String name = fields[nameField];
        TaskStatus status = TaskStatus.valueOf(fields[statusField]);
        String description = fields[descriptionField];

        Integer epicId = null;
        String epicIdStr = fields[epicIdField];
        if (!epicIdStr.isEmpty()) {
            epicId = Integer.valueOf(fields[epicIdField]);
        }

        LocalDateTime startTime = null;
        String startTimeStr = fields[startTimeField];
        if (!startTimeStr.isEmpty()) {
            startTime = LocalDateTime.parse(startTimeStr, dateTimeFormatter);
        }

        Duration duration = null;
        String durationStr = fields[durationField];
        if (!durationStr.isEmpty()) {
            int minutes = Integer.parseInt(durationStr);
            duration = Duration.ofMinutes(minutes);
        }

        LocalDateTime endTime = null;
        String endTimeStr = fields[endTimeField];
        if (!endTimeStr.isEmpty()) {
            endTime = LocalDateTime.parse(endTimeStr, dateTimeFormatter);
        }

        Task task = switch (taskType) {
            case TASK -> new Task(id, name, status, description, startTime, duration);
            case EPIC -> new Epic(id, name, status, description, startTime, duration, endTime);
            case SUBTASK -> new Subtask(id, name, status, epicId, description, startTime, duration);
            default -> throw new IllegalArgumentException("Not supported type");
        };

        return new ResultOfSerialization(taskType, task);
    }
}
