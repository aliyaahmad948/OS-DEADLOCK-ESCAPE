package ui.graph;

import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * NodeView.java
 *
 * A single visual node representing either a Process or a Resource
 * in the graph visualization. Purely a VIEW component — it holds no
 * game logic itself, it just reflects whatever state GraphCanvas tells it to.
 *
 * States map directly to visual styles:
 * IDLE      -> neutral blue glow (default)
 * WAITING   -> red glow (process blocked on a resource)
 * ACTIVE    -> blue glow, slightly larger (resource currently allocated)
 * FINISHED  -> green glow (process finished / resource available)
 * DEADLOCK  -> red glow + continuous pulsing (part of a detected cycle)
 * SAFE      -> green glow + brief pulse (used for the safe-state animation)
 */
public class NodeView extends StackPane {

    public enum NodeType { PROCESS, RESOURCE }
    public enum NodeState { IDLE, WAITING, ACTIVE, FINISHED, DEADLOCK, SAFE }

    private final String nodeId;
    private final NodeType type;
    private final Circle circle;
    private final Label label;

    private Timeline pulseTimeline;

    public NodeView(String nodeId, NodeType type) {
        this.nodeId = nodeId;
        this.type = type;

        // Circle size differs slightly so Processes and Resources are visually distinct
        double radius = (type == NodeType.PROCESS) ? 30 : 26;

        circle = new Circle(radius);
        circle.getStyleClass().add(type == NodeType.PROCESS ? "node-circle-process" : "node-circle-resource");

        label = new Label(nodeId);
        label.getStyleClass().add("node-label");

        this.getChildren().addAll(circle, label);
        this.setAlignment(Pos.CENTER);
        this.setPickOnBounds(false);

        // Hover animation: slight zoom on mouse enter/exit
        this.setOnMouseEntered(e -> animateScale(1.15));
        this.setOnMouseExited(e -> animateScale(1.0));

        applyState(NodeState.IDLE);
    }

    /**
     * Smoothly scales the node up/down (used for hover effect).
     */
    private void animateScale(double target) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), this);
        st.setToX(target);
        st.setToY(target);
        st.play();
    }

    /**
     * Applies a visual state to this node. Stops any previous pulsing
     * animation first, then either sets a static style or starts a new pulse.
     */
    public void applyState(NodeState state) {
        stopPulse();

        circle.getStyleClass().removeAll(
                "node-state-idle", "node-state-waiting", "node-state-active",
                "node-state-finished", "node-state-deadlock", "node-state-safe"
        );

        switch (state) {
            case IDLE:
                circle.getStyleClass().add("node-state-idle");
                break;
            case WAITING:
                circle.getStyleClass().add("node-state-waiting");
                break;
            case ACTIVE:
                circle.getStyleClass().add("node-state-active");
                break;
            case FINISHED:
                circle.getStyleClass().add("node-state-finished");
                break;
            case SAFE:
                circle.getStyleClass().add("node-state-safe");
                startBriefPulse(Color.web("#22C55E"));
                break;
            case DEADLOCK:
                circle.getStyleClass().add("node-state-deadlock");
                startContinuousPulse(Color.web("#EF4444"));
                break;
        }
    }

    /**
     * Starts a continuous pulsing glow (used for DEADLOCK state) —
     * the node's glow radius grows and shrinks in a loop.
     */
    private void startContinuousPulse(Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(15);
        circle.setEffect(glow);

        pulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 15)),
                new KeyFrame(Duration.seconds(0.6), new KeyValue(glow.radiusProperty(), 35)),
                new KeyFrame(Duration.seconds(1.2), new KeyValue(glow.radiusProperty(), 15))
        );
        pulseTimeline.setCycleCount(Timeline.INDEFINITE);
        pulseTimeline.play();
    }

    /**
     * Starts a short one-time pulse (used for SAFE state) then settles down.
     */
    private void startBriefPulse(Color color) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(15);
        circle.setEffect(glow);

        pulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 15)),
                new KeyFrame(Duration.seconds(0.4), new KeyValue(glow.radiusProperty(), 40)),
                new KeyFrame(Duration.seconds(0.8), new KeyValue(glow.radiusProperty(), 15))
        );
        pulseTimeline.setCycleCount(2);
        pulseTimeline.play();
    }

    /**
     * Stops any active pulsing animation on this node.
     */
    private void stopPulse() {
        if (pulseTimeline != null) {
            pulseTimeline.stop();
            pulseTimeline = null;
        }
    }

    /**
     * Toggles the player's selection ring: gold glow + slight zoom.
     * Independent of the state-driven glow effects on the circle
     * (they can coexist).
     */
    public void setSelectionMarker(boolean selected) {
        if (selected) {
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#FBBF24"));
            glow.setRadius(14);
            this.setEffect(glow);
            setScaleX(1.12);
            setScaleY(1.12);
        } else {
            this.setEffect(null);
            setScaleX(1.0);
            setScaleY(1.0);
        }
    }

    /**
     * One-shot green glow pulse — used when the process finishes or when the
     * safe sequence is celebrated. Unlike applyState(SAFE), this does NOT
     * change the persistent style classes; the next refresh() restores the
     * node's real state.
     */
    public void flash() {
        stopPulse();
        circle.setEffect(null);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#22C55E"));
        glow.setRadius(15);
        circle.setEffect(glow);

        pulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 15)),
                new KeyFrame(Duration.seconds(0.35), new KeyValue(glow.radiusProperty(), 42)),
                new KeyFrame(Duration.seconds(0.7), new KeyValue(glow.radiusProperty(), 15))
        );
        pulseTimeline.setCycleCount(2);
        pulseTimeline.setOnFinished(e -> circle.setEffect(null));
        pulseTimeline.play();
    }

    public String getNodeId() {
        return nodeId;
    }

    public NodeType getType() {
        return type;
    }
}