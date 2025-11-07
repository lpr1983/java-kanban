package practicum.taskmanager;

class InMemoryTaskManagerTest extends TaskManagerTest {

    @Override
    protected TaskManager createManager() {
        return new InMemoryTaskManager();
    }
}