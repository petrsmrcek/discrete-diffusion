package cz.cvut.fel.ko.node;

import cz.cvut.fel.ko.Task;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents the non-discrete algorithm, where tasks can be split and arbitrary 
 * amount of load sent via edge.
 * 
 * This behavior is achieved by simply returning new tasks of requested size, 
 * rather than picking from tasks assigned to this node.
 * 
 * @author Petr Smrcek <smrkous@gmail.com>
 */
public class ContinuousNode extends AbstractNode {
    
    protected double load;
    
    @Override
    public void addTasks(Collection<Task> tasks) {
        tasks.stream().forEach((t) -> {
            this.load += t.getSize();
        });
    }
    
    @Override
    public void addTask(Task t) {
        this.load += t.getSize();
    }
    
    @Override
    public double getLoad() {
        return load;
    }

    @Override
    public Collection<Task> removeTasks(double maxTotalLoad) {
        this.load -= maxTotalLoad;
        return Arrays.asList(Task.createDummyTask(maxTotalLoad));
    }

    @Override
    public String toString() {
        return getId() + ": " + getLoad();
    }
}
