package cz.cvut.fel.ko;

import cz.cvut.fel.ko.node.Node;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Edge of the network graph
 * 
 * @author Petr Smrcek <smrkous@gmail.com>
 */
public class Edge {
    public final Node a;
    public final Node b;
    private double pendingError = 0; // a sent less to b in last iteration (of opposite, it will be negative)
    private double overallLoad = 0;
    
    private final Collection<Task> tasks = new ArrayList<>();
    private boolean toA;

    /**
     * Creates edge between two given nodes. The nodes are automatically told about this new edge.
     * 
     * @param a
     * @param b 
     */
    public Edge(Node a, Node b) {
        this.a = a;
        this.b = b;
        a.addEdge(this);
        b.addEdge(this);
    }
    
    /**
     * Get current error against optimal flow on this edge. If positive, 
     * a sent less to b than it optimally should have and vice versa.
     * 
     * @return 
     */
    public double getPendingError() {
        return pendingError;
    }
    
    /**
     * If less than optimal amount of load was sent in current iteration,
     * set here the difference, so it can be compensated in later iterations.
     * 
     * If a sent less to b, give positive number, otherwise negative.
     * 
     * @param error 
     */
    public void setPendingError(double error) {
        this.pendingError = error;
    }
    
    /**
     * Denotes direction of the edge for this iteration.
     * If true, load is travelling from node b to node a and vice versa.
     * 
     * @return 
     */
    public boolean isToA() {
        return toA;
    }

    /**
     * Set the direction of this edge for this itteration.
     * 
     * @param toA 
     */
    public void setToA(boolean toA) {
        this.toA = toA;
    }
    
    /**
     * Get total load sent via this edge so far.
     * 
     * @return 
     */
    public double getOverallLoad() {
        return overallLoad;
    }
    
    /**
     * Send tasks to this edge. To deliver them to target node, use flushTasks().
     * 
     * @param tasks 
     */
    public void addTasksToSend(Collection<Task> tasks) {
        this.tasks.addAll(tasks);
    }
    
    /**
     * Delivers all tasks sent via this edge to target node.
     */
    public void flushTasks() {
        if (toA) {
            a.addTasks(tasks);
        } else {
            b.addTasks(tasks);
        }
        for(Task t: tasks) {
            overallLoad += t.getSize();
        }
        tasks.clear();
    }
}
