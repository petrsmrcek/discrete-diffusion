package cz.cvut.fel.ko.node;

import cz.cvut.fel.ko.Edge;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract node implementing common basic behavior.
 * 
 * @author Petr Smrcek <smrkous@gmail.com>
 */
public abstract class AbstractNode implements Node {
    private static int lastId = 0;
    private final int id;
    private double iterationLoad = 0;
    private final List<Edge> edges = new ArrayList<>();
    
    protected AbstractNode () {
        this.id = ++lastId;
    }

    @Override
    public int getId() {
        return id;
    }    
    
    @Override
    public void setIterationLoad(double load) {
        this.iterationLoad = load;
    }

    @Override
    public double getIterationLoad() {
        return iterationLoad;
    }

    @Override
    public double getOptimalLoadDelta() {
        double delta = 0;
        for(Edge e: edges) {
            if (e.a.getId() == id) {
                // this node sent less
                delta -= e.getPendingError();
            } else {
                // this node received less
                delta += e.getPendingError();
            }
        }
        return delta;
    }
    
    @Override
    public void addEdge(Edge e) {
        this.edges.add(e);
    }
}
