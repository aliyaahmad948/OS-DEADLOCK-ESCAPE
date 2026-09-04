package ui.graph;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import logic.GameManager;
import model.Process;
import model.Resource;

import java.util.*;

/**
 * GraphCanvas.java
 *
 * The central visualization panel. Reads the CURRENT state of GameManager
 * (read-only — never modifies it) and draws/updates:
 * - A NodeView circle for every Process (left column) and Resource (right column)
 * - A ConnectionLine for every "holds" relationship (Resource -> Process)
 * - A ConnectionLine for every "waiting" relationship (Process -> Resource)
 * - Animated data packets when a new allocation/request happens
 * - Red pulsing deadlock highlighting, or green safe-state flashing
 *
 * This class is purely a VIEW. GameScreen calls refresh() after every
 * game action, passing the GameManager so this canvas can re-read state.
 */
public class GraphCanvas extends Pane {

    // node id (process/resource name) -> its visual node
    private Map<String, NodeView> nodeViews = new HashMap<>();

    // connection key ("FROM->TO") -> its visual line
    private Map<String, ConnectionLine> connections = new HashMap<>();

    // node ids currently selected by the player (click-to-select)
    private final Set<String> selectedNodes = new HashSet<>();

    // notifies GameScreen when the player clicks a node
    private java.util.function.BiConsumer<String, NodeView.NodeType> nodeClickHandler;

    private static final double NODE_COLUMN_GAP = 260;
    private static final double NODE_ROW_GAP = 90;
    private static final double TOP_MARGIN = 60;
    private static final double LEFT_MARGIN = 90;

    public GraphCanvas() {
        this.getStyleClass().add("graph-canvas");
        this.setPrefSize(760, 420);
        buildLegend();
    }

    /**
     * Compact, always-visible legend explaining the node color meanings.
     * Placed in the top strip of the canvas so it never overlaps nodes.
     */
    private void buildLegend() {
        HBox legend = new HBox(10);
        legend.getStyleClass().add("graph-legend");
        legend.setLayoutX(90);
        legend.setLayoutY(6);
        legend.getChildren().addAll(
                legendItem("#EF4444", "Waiting"),
                legendItem("#38BDF8", "Held / Active"),
                legendItem("#22C55E", "Finished / Free"),
                legendItem("#EF4444", "Deadlock Cycle (pulse)")
        );
        this.getChildren().add(legend);
    }

    private HBox legendItem(String color, String text) {
        Circle dot = new Circle(5, Color.web(color));
        Label label = new Label(text);
        label.getStyleClass().add("graph-legend-text");
        HBox item = new HBox(5, dot, label);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    /**
     * Full refresh: rebuilds node positions and connections to match the
     * current GameManager state. Call this after every allocate/finish/tick.
     */
    public void refresh(GameManager gameManager) {
        List<Process> processes = gameManager.getProcesses();
        List<Resource> resources = gameManager.getResources();

        ensureNodesExist(processes, resources);
        positionNodes(processes, resources);
        updateNodeStates(processes, resources);
        rebuildConnections(processes, resources);
    }

    /**
     * Creates a NodeView for any process/resource that doesn't have one yet.
     * Nodes are never removed once created (so connections don't visually vanish
     * mid-level); they simply change state (idle/waiting/finished).
     */
    private void ensureNodesExist(List<Process> processes, List<Resource> resources) {
        for (Process p : processes) {
            if (!nodeViews.containsKey(p.getProcessName())) {
                NodeView node = new NodeView(p.getProcessName(), NodeView.NodeType.PROCESS);
                wireNodeClick(node);
                nodeViews.put(p.getProcessName(), node);
                this.getChildren().add(node);

                // Fade the node in smoothly when it first appears
                node.setOpacity(0);
                FadeTransition fade = new FadeTransition(Duration.millis(400), node);
                fade.setToValue(1);
                fade.play();
            }
        }
        for (Resource r : resources) {
            if (!nodeViews.containsKey(r.getResourceName())) {
                NodeView node = new NodeView(r.getResourceName(), NodeView.NodeType.RESOURCE);
                wireNodeClick(node);
                nodeViews.put(r.getResourceName(), node);
                this.getChildren().add(node);

                node.setOpacity(0);
                FadeTransition fade = new FadeTransition(Duration.millis(400), node);
                fade.setToValue(1);
                fade.play();
            }
        }
    }

    /**
     * Makes a node clickable for the click-to-select gameplay: a click anywhere
     * on the circle forwards the node id + type to the registered handler.
     */
    private void wireNodeClick(NodeView node) {
        node.setOnMousePressed(e -> {
            if (nodeClickHandler != null) {
                nodeClickHandler.accept(node.getNodeId(), node.getType());
            }
        });
    }

    /** Registers the callback used when the player clicks a graph node. */
    public void setNodeClickHandler(java.util.function.BiConsumer<String, NodeView.NodeType> handler) {
        this.nodeClickHandler = handler;
    }

    /** Highlights/de-highlights a node's selection ring. */
    public void setNodeSelected(String id, boolean selected) {
        NodeView node = nodeViews.get(id);
        if (node == null) return;
        if (selected) {
            selectedNodes.add(id);
        } else {
            selectedNodes.remove(id);
        }
        node.setSelectionMarker(selected);
    }

    /** Clears all current selections. */
    public void clearSelection() {
        for (String id : new HashSet<>(selectedNodes)) {
            setNodeSelected(id, false);
        }
        selectedNodes.clear();
    }

    /**
     * Lays out process nodes in a left column and resource nodes in a
     * right column, evenly spaced vertically. Node size and spacing stay
     * fixed and comfortable regardless of the level's process count; taller
     * graphs simply grow the canvas height, and the surrounding ScrollPane
     * provides vertical scrolling to see all nodes.
     */
    private void positionNodes(List<Process> processes, List<Resource> resources) {
        for (int i = 0; i < processes.size(); i++) {
            NodeView node = nodeViews.get(processes.get(i).getProcessName());
            double x = LEFT_MARGIN;
            double y = TOP_MARGIN + i * NODE_ROW_GAP;
            node.setLayoutX(x);
            node.setLayoutY(y);
        }
        for (int i = 0; i < resources.size(); i++) {
            NodeView node = nodeViews.get(resources.get(i).getResourceName());
            double x = LEFT_MARGIN + NODE_COLUMN_GAP;
            double y = TOP_MARGIN + i * NODE_ROW_GAP;
            node.setLayoutX(x);
            node.setLayoutY(y);
        }

        // Grow the canvas height when there are many processes/resources so
        // the ScrollPane kicks in instead of squeezing the nodes together.
        int maxCount = Math.max(processes.size(), resources.size());
        double requiredHeight = TOP_MARGIN + maxCount * NODE_ROW_GAP + 60;
        this.setPrefHeight(Math.max(420, requiredHeight));
    }

    /**
     * Updates each node's visual state based on the process/resource's
     * actual current condition (idle, waiting, finished, active/available).
     */
    private void updateNodeStates(List<Process> processes, List<Resource> resources) {
        for (Process p : processes) {
            NodeView node = nodeViews.get(p.getProcessName());
            if (p.isFinished()) {
                node.applyState(NodeView.NodeState.FINISHED);
            } else if (p.isWaiting()) {
                node.applyState(NodeView.NodeState.WAITING);
            } else {
                node.applyState(NodeView.NodeState.IDLE);
            }
        }
        for (Resource r : resources) {
            NodeView node = nodeViews.get(r.getResourceName());
            if (r.isAvailable()) {
                node.applyState(NodeView.NodeState.FINISHED); // green = free/available
            } else {
                node.applyState(NodeView.NodeState.ACTIVE); // blue = currently held
            }
        }
    }

    /**
     * Rebuilds connection lines: one per "holds" relationship and one per
     * "waiting" relationship. Existing lines matching the same connection
     * are reused (not recreated) so their animation state isn't reset every tick.
     */
    private void rebuildConnections(List<Process> processes, List<Resource> resources) {
        Set<String> stillNeeded = new HashSet<>();

        // Allocation edges: Resource -> Process (resource currently held)
        for (Resource r : resources) {
            if (!r.isAvailable() && r.getAllocatedTo() != null) {
                String key = r.getResourceName() + "->" + r.getAllocatedTo();
                stillNeeded.add(key);
                ensureConnection(key, r.getResourceName(), r.getAllocatedTo(), ConnectionLine.LineState.NORMAL, false);
            }
        }

        // Request edges: Process -> Resource (process is waiting)
        for (Process p : processes) {
            if (p.isWaiting()) {
                String key = p.getProcessName() + "->" + p.getWaitingFor();
                boolean isNew = !connections.containsKey(key);
                stillNeeded.add(key);
                ensureConnection(key, p.getProcessName(), p.getWaitingFor(), ConnectionLine.LineState.WAITING, isNew);
            }
        }

        // Remove connections that are no longer valid (resource released, wait resolved)
        Iterator<Map.Entry<String, ConnectionLine>> it = connections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ConnectionLine> entry = it.next();
            if (!stillNeeded.contains(entry.getKey())) {
                this.getChildren().remove(entry.getValue().getLine());
                it.remove();
            }
        }
    }

    /**
     * Creates a connection line between two nodes if it doesn't already
     * exist, or updates its state if it does. If brand new AND it's a
     * request edge, also animates a data packet traveling along it.
     */
    private void ensureConnection(String key, String fromId, String toId,
                                  ConnectionLine.LineState state, boolean animatePacket) {
        NodeView fromNode = nodeViews.get(fromId);
        NodeView toNode = nodeViews.get(toId);
        if (fromNode == null || toNode == null) return;

        double fromX = fromNode.getLayoutX();
        double fromY = fromNode.getLayoutY();
        double toX = toNode.getLayoutX();
        double toY = toNode.getLayoutY();

        ConnectionLine connection = connections.get(key);
        if (connection == null) {
            connection = new ConnectionLine(fromX, fromY, toX, toY);
            connections.put(key, connection);
            // Add the line BEHIND the nodes so nodes render on top
            this.getChildren().add(0, connection.getLine());

            if (animatePacket) {
                connection.animateDataPacket(this);
            }
        }
        connection.applyState(state);
    }

    /**
     * Called when a deadlock is detected. Highlights ONLY the nodes and
     * connections that are part of the given cycle in red, with pulsing
     * animation and a blocked-loop packet on each waiting edge.
     * All other nodes are dimmed to draw attention to the deadlock.
     *
     * @param cycleNodeNames names of processes/resources involved in the cycle
     */
    public void highlightDeadlock(List<String> cycleNodeNames) {
        Set<String> involved = new HashSet<>(cycleNodeNames);

        // Dim everything first
        for (NodeView node : nodeViews.values()) {
            node.setOpacity(involved.contains(node.getNodeId()) ? 1.0 : 0.3);
        }

        // Highlight involved nodes in red
        for (String id : involved) {
            NodeView node = nodeViews.get(id);
            if (node != null) {
                node.applyState(NodeView.NodeState.DEADLOCK);
            }
        }

        // Highlight involved connections in red with blocked-loop packets
        for (Map.Entry<String, ConnectionLine> entry : connections.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("->");
            boolean bothInvolved = parts.length == 2 && involved.contains(parts[0]) && involved.contains(parts[1]);

            if (bothInvolved) {
                entry.getValue().applyState(ConnectionLine.LineState.DEADLOCK);
                entry.getValue().animateBlockedLoop(this);
            }
        }
    }

    /**
     * Called when the level is won safely (no deadlock). Briefly flashes
     * every active connection and node green to celebrate the safe sequence.
     */
    public void showSafeState() {
        for (NodeView node : nodeViews.values()) {
            node.setOpacity(1.0);
            node.applyState(NodeView.NodeState.SAFE);
        }
        for (ConnectionLine connection : connections.values()) {
            connection.applyState(ConnectionLine.LineState.SAFE);
        }
    }

    /**
     * Resets opacity/dimming (used when restarting a level after a deadlock
     * highlight was shown, so the canvas isn't stuck half-dimmed).
     */
    public void resetDimming() {
        for (NodeView node : nodeViews.values()) {
            node.setOpacity(1.0);
        }
    }

    /**
     * Plays a one-shot green glow pulse on the given node (e.g. when a
     * process finishes). Does not change persistent styling.
     */
    public void pulseNode(String nodeId) {
        NodeView node = nodeViews.get(nodeId);
        if (node != null) {
            node.flash();
        }
    }

    /**
     * Sequential green flashes along the safe completion order — a little
     * "victory sweep" showing how the safe sequence resolves the graph.
     */
    public void animateSafeSequence(List<String> order) {
        List<String> seq = new ArrayList<>(order);
        if (seq.isEmpty()) return;

        for (int i = 0; i < seq.size(); i++) {
            final int idx = i;
            PauseTransition step = new PauseTransition(Duration.millis(idx * 240L));
            step.setOnFinished(e -> {
                NodeView node = nodeViews.get(seq.get(idx));
                if (node != null) {
                    node.flash();
                }
            });
            step.play();
        }
    }

    /**
     * Clears everything — used when restarting a level from scratch.
     */
    public void clearAll() {
        this.getChildren().clear();
        nodeViews.clear();
        connections.clear();
        selectedNodes.clear();
        buildLegend();
    }
}