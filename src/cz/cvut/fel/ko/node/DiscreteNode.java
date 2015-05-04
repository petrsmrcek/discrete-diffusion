package cz.cvut.fel.ko.node;

import cz.cvut.fel.ko.Task;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Represents version of algorithm where tasks cannot be split and node has to 
 * select some tasks to send. Knapsack heurisctics are used.
 * 
 * @author Petr Smrcek <smrkous@gmail.com>
 */
public class DiscreteNode extends AbstractNode {
    
    private final TreeSet<Task> tasks = new TreeSet<>();
        
    @Override
    public void addTasks(Collection<Task> tasks) {
        this.tasks.addAll(tasks);
    }
    
    @Override
    public void addTask(Task t) {
        tasks.add(t);
    }
    
    @Override
    public double getLoad() {
        return tasks.stream().map((t) -> t.getSize()).reduce(0.0, (accumulator, _item) -> accumulator + _item);
    }

    @Override
    public Collection<Task> removeTasks(double maxTotalLoad) {
        List<Task> result = new ArrayList<>();
        Task t = tasks.floor(Task.createDummyTask(maxTotalLoad));
        while (t != null) {
            result.add(t);
            tasks.remove(t);
            maxTotalLoad -= t.getSize();
            t = tasks.floor(Task.createDummyTask(maxTotalLoad));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getId());
        sb.append(':');
        sb.append(' ');
        sb.append(getLoad());
        sb.append(" delta (" + getOptimalLoadDelta() + ")");
        sb.append(' ');
        sb.append('(');
        for(Task t: tasks) {
            sb.append(t.getId());
            sb.append(':');
            sb.append(t.getSize());
            sb.append(' ');
        }
        sb.append(')');
        
        return sb.toString();
    }
}
