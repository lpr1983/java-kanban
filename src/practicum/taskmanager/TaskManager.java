package practicum.taskmanager;

import java.util.ArrayList;

public interface TaskManager {
    ArrayList<Task> getTasksList();

    void clearTasks();

    Task getTaskById(int id);

    int createTask(Task newTask);

    UpdateResult updateTask(Task task);

    ResultOfDeletion deleteTaskById(int id);

    ArrayList<Epic> getEpicsList();

    void clearEpics();

    Epic getEpicById(int id);

    int createEpic(Epic newEpic);

    UpdateResult updateEpic(Epic epic);

    ResultOfDeletion deleteEpicById(int id);

    ArrayList<Subtask> getSubtasksList();

    void clearSubtasks();

    Subtask getSubtaskById(int id);

    int createSubtask(Subtask newSubtask);

    UpdateResult updateSubtask(Subtask subtask);

    ResultOfDeletion deleteSubtaskById(int id);

    ArrayList<Subtask> getSubtasksOfEpic(int epicId);

    ArrayList<Task> getHistory();
}