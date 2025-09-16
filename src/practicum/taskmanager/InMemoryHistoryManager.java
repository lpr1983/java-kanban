package practicum.taskmanager;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    static final int MAX_HISTORY_SIZE = 10;
    private final ArrayList<Task> historyList = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (historyList.size() == MAX_HISTORY_SIZE) {
            historyList.removeFirst();
        }
        historyList.add(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(historyList);
    }
}