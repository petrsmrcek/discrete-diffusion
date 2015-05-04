package cz.cvut.fel.ko.node;

import cz.cvut.fel.ko.Edge;
import cz.cvut.fel.ko.Task;
import java.util.Collection;

/**
 * Node of the network graph.
 * 
 * @author Petr Smrcek <smrkous@gmail.com>
 */
public interface Node {
    /**
     * Unique identifier of the node
     * 
     * @return 
     */
    public int getId();
    
    /**
     * Add task to this node
     * 
     * @param t 
     */
    public void addTask(Task t);
    
    /**
     * Add multiple tasks to this node
     * 
     * @param tasks 
     */
    public void addTasks(Collection<Task> tasks);
    
    /**
     * Get current load based on tasks
     * 
     * @return 
     */
    public double getLoad();
    
    /**
     * Save load value for current iteration.
     * Tasks can be manipulated, but this value won't change.
     * 
     * @param load 
     */
    public void setIterationLoad(double load);
    
    /**
     * Get value previously saved by setIterationLoad method.
     * 
     * @return 
     */
    public double getIterationLoad();
    
    /**
     * Removes some tasks from this node such that their total load does not
     * exceed maxTotalLoad. This method might encapsulate various knapsack 
     * implementations.
     * 
     * @param maxTotalLoad
     * @return 
     */
    public Collection<Task> removeTasks(double maxTotalLoad);
    
    /**
     * Returns the difference between the actual load on the node and the optimal
     * load the node would have if arbitrary amount of load could be sent.
     * 
     * The optimal node load can be computed as getLoad() + getOptimalLoadDelta().
     * 
     * @return 
     */
    public double getOptimalLoadDelta();
    
    /**
     * Makes this node know that given edge is connected to it.
     * 
     * @param e 
     */
    public void addEdge(Edge e);
}
