package practicum.taskmanager;

public final class Managers {
    Managers() {
        throw new IllegalStateException("This class is utilitarian");
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

}
