package practicum.taskmanager;

import practicum.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Node first;
    private Node last;
    private Map<Integer, Node> idToNode;

    public InMemoryHistoryManager() {
        this.first = null;
        this.last = null;
        this.idToNode = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        int taskId = task.getId();
        Node nodeWithTask = idToNode.get(taskId);
        if (nodeWithTask != null) {
            removeNode(nodeWithTask);
        }
        Node newNode = new Node(task);
        linkLast(newNode);
        idToNode.put(taskId, newNode);
    }

    @Override
    public void remove(int id) {
        Node node = idToNode.get(id);
        if (node == null) {
            return;
        }
        removeNode(node);
        idToNode.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Node node) {
        node.prev = last;
        if (first == null) {
            first = node;
        } else {
            last.next = node;
        }
        last = node;
    }

    private List<Task> getTasks() {
        ArrayList<Task> result = new ArrayList<>();
        Node currentNode = first;
        while (currentNode != null) {
            result.add(currentNode.data);
            currentNode = currentNode.next;
        }
        return result;
    }

    private void removeNode(Node node) {
        Node prevNode = node.prev;
        Node nextNode = node.next;

        if (prevNode == null) {
            first = nextNode;
        } else {
            prevNode.next = nextNode;
        }

        if (nextNode == null) {
            last = prevNode;
        } else {
            nextNode.prev = prevNode;
        }

        node.next = null;
        node.prev = null;
    }

    private class Node {
        Task data;
        Node next;
        Node prev;

        public Node(Task data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }
    }
}
