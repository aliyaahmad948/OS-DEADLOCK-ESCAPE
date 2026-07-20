package logic;

import model.Process;
import model.Resource;

import java.util.*;

/**
 * DeadlockDetector.java
 *
 * Core logic class that detects deadlocks using a Resource Allocation Graph (RAG).
 * (Logic unchanged from original — only addition: stores + exposes the exact
 * list of node names involved in the last detected cycle, so the graph
 * visualization can highlight precisely those nodes/lines.)
 */
public class DeadlockDetector {

    private Map<String, List<String>> graph;
    private String explanation;

    // NEW: stores the node names (process + resource) involved in the last detected cycle
    private List<String> lastCycleNodes;

    public DeadlockDetector() {
        this.graph = new HashMap<>();
        this.explanation = "";
        this.lastCycleNodes = new ArrayList<>();
    }

    public void buildGraph(List<Process> processes, List<Resource> resources) {
        graph = new HashMap<>();

        for (Process p : processes) {
            graph.putIfAbsent(p.getProcessName(), new ArrayList<>());
        }
        for (Resource r : resources) {
            graph.putIfAbsent(r.getResourceName(), new ArrayList<>());
        }

        for (Resource r : resources) {
            if (!r.isAvailable() && r.getAllocatedTo() != null) {
                graph.get(r.getResourceName()).add(r.getAllocatedTo());
            }
        }

        for (Process p : processes) {
            if (p.isWaiting()) {
                graph.get(p.getProcessName()).add(p.getWaitingFor());
            }
        }
    }

    public boolean checkDeadlock() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        List<String> path = new ArrayList<>();

        for (String node : graph.keySet()) {
            if (!visited.contains(node)) {
                List<String> cycleNodes = new ArrayList<>();
                if (dfs(node, visited, recursionStack, path, cycleNodes)) {
                    explanation = buildExplanation(cycleNodes);
                    lastCycleNodes = new ArrayList<>(cycleNodes); // NEW: store cycle nodes
                    return true;
                }
            }
        }

        explanation = "No deadlock detected. All processes can proceed safely.";
        lastCycleNodes = new ArrayList<>(); // NEW: clear cycle nodes when no deadlock
        return false;
    }

    private boolean dfs(String node, Set<String> visited, Set<String> recursionStack,
                        List<String> path, List<String> cycleNodesOutput) {

        visited.add(node);
        recursionStack.add(node);
        path.add(node);

        List<String> neighbors = graph.getOrDefault(node, new ArrayList<>());
        for (String neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                if (dfs(neighbor, visited, recursionStack, path, cycleNodesOutput)) {
                    return true;
                }
            } else if (recursionStack.contains(neighbor)) {
                int startIndex = path.indexOf(neighbor);
                for (int i = startIndex; i < path.size(); i++) {
                    cycleNodesOutput.add(path.get(i));
                }
                cycleNodesOutput.add(neighbor);
                return true;
            }
        }

        recursionStack.remove(node);
        path.remove(path.size() - 1);
        return false;
    }

    private String buildExplanation(List<String> cycleNodes) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < cycleNodes.size() - 1; i++) {
            String from = cycleNodes.get(i);
            String to = cycleNodes.get(i + 1);

            if (from.startsWith("P")) {
                sb.append(from).append(" waits for ").append(to).append("\n");
            } else {
                sb.append(to).append(" holds ").append(from).append("\n");
            }
        }
        sb.append("Deadlock Detected.");

        return sb.toString();
    }

    public String getExplanation() {
        return explanation;
    }

    public Map<String, List<String>> getGraph() {
        return graph;
    }

    /**
     * NEW METHOD: returns the exact list of process/resource names that
     * were part of the last detected deadlock cycle. Empty list if the
     * last check found no deadlock. Used by GraphCanvas to highlight
     * only the involved nodes/connections in red.
     */
    public List<String> getLastCycleNodes() {
        return lastCycleNodes;
    }
}