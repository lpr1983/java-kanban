package practicum.taskmanager;

public final class Managers {
    Managers() {
        throw new IllegalStateException("This class is utilitarian");
    }

    public static TaskManager getDefault() {
        return new FileBackedTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}