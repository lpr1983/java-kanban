package practicum.taskmanager;

import practicum.task.Epic;
import practicum.task.Subtask;
import practicum.task.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager {

    List<Task> getTasksList();

    void clearTasks();

    Optional<Task> getTaskById(int id);

    int createTask(Task newTask);

    UpdateResult updateTask(Task task);

    ResultOfDeletion deleteTaskById(int id);

    List<Epic> getEpicsList();

    void clearEpics();

    Optional<Epic> getEpicById(int id);

    int createEpic(Epic newEpic);

    UpdateResult updateEpic(Epic epic);

    ResultOfDeletion deleteEpicById(int id);

    List<Subtask> getSubtasksList();

    void clearSubtasks();

    Optional<Subtask> getSubtaskById(int id);

    int createSubtask(Subtask newSubtask);

    UpdateResult updateSubtask(Subtask subtask);

    ResultOfDeletion deleteSubtaskById(int id);

    List<Subtask> getSubtasksOfEpic(int epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}