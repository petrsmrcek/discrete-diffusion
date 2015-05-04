package cz.cvut.fel.ko;

import Jama.Matrix;
import cz.cvut.fel.ko.node.ContinuousNode;
import cz.cvut.fel.ko.node.ContinuousModifiedNode;
import cz.cvut.fel.ko.node.DiscreteNode;
import cz.cvut.fel.ko.node.Node;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Diffusion algorithm simulator.
 * Capable of running both discrete and non-discrete version (original one, or slightly modified one).
 * 
 * @author Petr Smrcek <smrkous@gmail.com>
 */
public class DiscDiffuseAlg {

    private final PrintStream out;
    
    /**
     * Specifies the version of algorithm to run.
     */
    public enum Version {
        /**
         * Atomic tasks (Discreet diffusion algorithm).
         */
        DISCRETE, 
        /**
         * Node can send arbitrary load anytime (optimal OPT scheme).
         */
        CONTINUOUS, 
        /**
         * Cuttable tasks, but node can never send less than it has.
         */
        CONTINUOUS_MODFIED
    };
    
    /**
     * Print program usage (arguments)
     * 
     * @param out 
     */
    public static void printUsage(PrintStream out) {
        out.println("Argumants: <inputFilePath> [\"cont\"|\"cont-mod\">]");
    }
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            printUsage(System.err);
            System.exit(1);
        }
        Version v = Version.DISCRETE;
        if (args.length == 2) {
            switch (args[1].trim().toLowerCase()) {
                case "cont":
                    v = Version.CONTINUOUS;
                    break;
                case "cont-mod":
                    v = Version.CONTINUOUS_MODFIED;
                    break;
                default:
                    printUsage(System.err);
                    System.exit(1);
            }
        }
        DiscDiffuseAlg alg = new DiscDiffuseAlg(System.out);
        
        // load input file
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
        
        // parse nodes and their tasks
        int nodeCnt = Integer.parseInt(br.readLine());
        Node[] nodes = new Node[nodeCnt];
        double biggestTask = 0;
        for (int i = 0; i < nodeCnt; i++) {
            switch(v) {
                case DISCRETE:
                    nodes[i] = new DiscreteNode();
                    break;
                case CONTINUOUS:
                    nodes[i] = new ContinuousNode();
                    break;
                case CONTINUOUS_MODFIED:
                    nodes[i] = new ContinuousModifiedNode();
                    break;
            }
            for(String s: br.readLine().split(" ")) {
                if (s.length() > 0) {
                    double load = Double.parseDouble(s);
                    biggestTask = Math.max(biggestTask, load);
                    nodes[i].addTask(Task.createTask(load));
                }
            }
        }
        // parse adjacency matrix
        Matrix adjMat = Matrix.read(br);
        
        // run the simulation
        alg.simulateDiffuseAlgorithm(nodes, adjMat, biggestTask);
    }

    /**
     * Construct new instance if he diffuse algorithm solver.
     * 
     * @param out 
     */
    public DiscDiffuseAlg(PrintStream out) {
        this.out = out;
    }    
    
    /**
     * Runs the algorithm for given input.
     * 
     * @param nodes
     * @param adjMat
     * @param biggestTask
     * @throws IOException 
     */
    public void simulateDiffuseAlgorithm(Node[] nodes, Matrix adjMat, double biggestTask) throws IOException {
        // initialize variables
        double totalLoad = 0;
        for(Node n: nodes) {
            totalLoad += n.getLoad();
        }
        
        // create edges
        List<Edge> edges = new ArrayList<>();
        for(int i = 0; i < adjMat.getRowDimension()-1; i++) {
            for(int j = i+1; j < adjMat.getColumnDimension(); j++) {
                double edge = adjMat.get(i, j);
                if (edge != 0) {
                    edges.add(new Edge(nodes[i], nodes[j]));
                }
            }
        }
        
        // rebuild adjacency matrix to laplacian
        for(int i = 0; i < adjMat.getRowDimension(); i++) {
            int degree = 0;
            for(int j = 0; j < adjMat.getColumnDimension(); j++) {
                degree += adjMat.get(i, j);
                adjMat.set(i, j, -adjMat.get(i, j));
            }
            adjMat.set(i, i, degree);
        }
        
        // compute eigenvalues from the laplacian matrix
        double[] eigvals = orderEigenvalues(adjMat.eig().getRealEigenvalues());
        
        // do the iterations
        for(int i = 0; i < eigvals.length; i++) {
            
            // precompute load of nodes in this iteration, so it does not change when we move tasks
            for(Node n: nodes) {
                // save the load the optimal algorithm would have
                n.setIterationLoad(n.getLoad() + n.getOptimalLoadDelta());
            }
            
            // for each edge pick tasks to send
            for(Edge e: edges) {
                // compute the loadToSend based on load difference and current eigenvalue
                double loadToSend = (e.a.getIterationLoad() - e.b.getIterationLoad()) / eigvals[i];
                // assign apropriate tasks to this edge from the node with smaller load
                // the tasks are not yet delivered to the target node, because the target node cannot use them in this iteration
                processEdge(e, loadToSend);
            }
            
            // deliver the tasks (all nodes have already selected which tasks they send)
            for(Edge e: edges) {
                e.flushTasks();
            }
        }
        
        // final error correcting iterations
        // exchange load based on the error against the optimal algorithm, until treshold is reached
        int errCorrItCnt = 0;
        while (true) {
            
            // check the error on each node and decide whether the error correction is needed
            boolean stop = true;
            for (int i = 0; i < nodes.length; i++) {
                Node n = nodes[i];
                // the treshold is the size of the biggest task multiplied by this node degree
                if (adjMat.get(i,i)*biggestTask < Math.abs(n.getOptimalLoadDelta())) {
                    stop = false;
                    break;
                }
            }
            if (stop) {
                break;
            }
            
            // do the error correction iteration
            ++errCorrItCnt;
            for(Edge e: edges) {
                processEdge(e, 0);
            }
            for(Edge e: edges) {
                e.flushTasks();
            }
        }

        // display results
        // iteration count
        out.println("Iterations: " + (eigvals.length + errCorrItCnt) 
                + " (" + eigvals.length + " standard, " + errCorrItCnt + " error correcting)");
        // load sent via edges
        double l2norm = 0;
        double overallLoad = 0;
        for(Edge e: edges) {
            l2norm += e.getOverallLoad()*e.getOverallLoad();
            overallLoad += e.getOverallLoad();
        }
        l2norm = Math.sqrt(l2norm);
        out.println("Flow cost as l2-norm: " + l2norm + " (total load sum: " + overallLoad + ")");
        // final load on nodes
        double averageLoad = (totalLoad / nodes.length);
        double variation = 0;
        for (int i = 0; i < nodes.length; i++) {
            variation += Math.abs(nodes[i].getLoad() - averageLoad);
        }
        out.println("Node loads (average " + averageLoad + ", average variation " + variation / nodes.length + ") :");
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            out.println(n.getLoad() + " (remaining error " + n.getOptimalLoadDelta() + ")");
        }
    }
    
    /**
     * Picks tasks from the node with lower load (based on loadToSend) and sends 
     * them to the edge. Also tries to compensate error against the optimal
     * algorithm from the previous iterations.
     * 
     * @param e Edge to send the load via.
     * @param loadToSend Load that has to be sent based on the optimal algorithm. Positive means a has to send to b.
     */
    public void processEdge(Edge e, double loadToSend) {
        // If there is some deficit against the optimal alg., try to send it
        loadToSend += e.getPendingError(); 

        // set the direction of the edge in this iteration
        Node sender;
        if (loadToSend >= 0) {
            sender = e.a;
            e.setToA(false);
        } else {
            sender = e.b;
            e.setToA(true);
            loadToSend = -loadToSend; // make the load positive
        }
        // select available tasks so it fits the target load best
        Collection<Task> tasks = sender.removeTasks(loadToSend);
        double loadSent = 0;
        for (Task t: tasks) {
            loadSent += t.getSize();
        }
        // load them into the edge
        e.addTasksToSend(tasks);
        // update the error
        e.setPendingError(e.isToA() ? loadSent - loadToSend : loadToSend - loadSent);
    }

    /**
     * Center-stated ordering of eigenvalues to achieve better results.
     * Eigenvalues are also filtered to be unique and exclude 0.
     * 
     * @param realEigenvalues
     * @return 
     */
    public static double[] orderEigenvalues(double[] realEigenvalues) {
        // remove zero and duplicate eigenvalues 
        Set<Double> eigSet = new HashSet<>();
        for(int i = 0; i < realEigenvalues.length; i++) {
            // do rounding to avoid 2 identical eigenvalues only because of precission problems
            eigSet.add(round(realEigenvalues[i], 9));
        }
        eigSet.remove(0.0);
        
        // sort by value
        List<Double> sortedEigs = new ArrayList<>(eigSet);
        Collections.sort(sortedEigs);
        
        // reorder the eigenvalues for higher stability
        int eigCnt = sortedEigs.size();
        double[] result = new double[eigCnt];
        int half = (eigCnt - eigCnt%2)/2;
        int divider = eigCnt%2 == 0 ? 2 : -2 ;
        for(int i = 0; i < eigCnt; i++) {
            result[i] = sortedEigs.get(half + (i+1)/divider);
            divider = -divider;
        }
        
        return result;
    }
    
    /**
     * Rounds given double to specified number of digits.
     * Meant only for reasonably small numbers: value * (10 ^ digitCount)
     * has to fit to long, otherwise owerflow occurs.
     * 
     * @param value
     * @param digitCount
     * @return 
     */
    public static double round(double value, int digitCount) {
        return Math.round(value*Math.pow(10, digitCount))/Math.pow(10.0, digitCount);
    }
    
}
