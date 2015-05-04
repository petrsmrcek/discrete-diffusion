package cz.cvut.fel.ko.node;

import cz.cvut.fel.ko.Task;
import java.util.Collection;

/**
 * Behavior identical to {@link ContinuousNode} with one exception:
 * Node cannot send less than it has in current iteration.
 * 
 * @author Petr Smrcek <smrkous@gmail.com>
 */
public class ContinuousModifiedNode extends ContinuousNode {
    
    @Override
    public Collection<Task> removeTasks(double maxTotalLoad) {
        return super.removeTasks(Math.min(maxTotalLoad, this.load));
    }
    
    @Override
    public String toString() {
        return super.toString() + " delta (" + getOptimalLoadDelta() + ")";
    }
}
