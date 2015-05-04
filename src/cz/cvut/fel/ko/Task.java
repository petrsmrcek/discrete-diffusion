package cz.cvut.fel.ko;

/**
 * Task of given load assignable to some node.
 * 
 * @author Petr Smrcek <smrkous@gmail.com>
 */
public class Task implements Comparable<Task> {

    private static int lastId = 0;
    private final int id;
    private final double size;

    private Task(int id, double size) {
        this.id = id;
        this.size = size;
    }

    public int getId() {
        return id;
    }
    
    public static Task createDummyTask(double size) {
        return new Task(0, size);
    }
    
    public static Task createTask(double size) {
        return new Task(++lastId, size);
    }

    public double getSize() {
        return size;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return this.id == ((Task)obj).id;
    }

    @Override
    public int compareTo(Task o) {
        // id = 0 is the biggest of equal sized tasks
        if (o.size == this.size) {
            return o.id - this.id;
        }
        return Double.compare(this.size, o.size);
    }
}
