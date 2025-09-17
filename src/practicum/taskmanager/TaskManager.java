package practicum.taskmanager;

import java.util.List;

public interface TaskManager {

    List<Task> getTasksList();

    void clearTasks();

    Task getTaskById(int id);

    int createTask(Task newTask);

    UpdateResult updateTask(Task task);

    ResultOfDeletion deleteTaskById(int id);

    List<Epic> getEpicsList();

    void clearEpics();

    Epic getEpicById(int id);

    int createEpic(Epic newEpic);

    UpdateResult updateEpic(Epic epic);

    ResultOfDeletion deleteEpicById(int id);

    List<Subtask> getSubtasksList();

    void clearSubtasks();

    Subtask getSubtaskById(int id);

    int createSubtask(Subtask newSubtask);

    UpdateResult updateSubtask(Subtask subtask);

    ResultOfDeletion deleteSubtaskById(int id);

    List<Subtask> getSubtasksOfEpic(int epicId);

    List<Task> getHistory();
}