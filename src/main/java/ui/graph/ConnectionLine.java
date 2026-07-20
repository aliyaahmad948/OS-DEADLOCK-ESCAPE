package ui.graph;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

/**
 * ConnectionLine.java
 *
 * Represents a single animated glowing connection between two nodes
 * (a Process and a Resource) in the graph visualization.
 *
 * Two kinds of connections exist conceptually (both use this same class):
 * - "Allocation" edge: Resource -> Process (resource is held by process)
 * - "Request" edge: Process -> Resource (process is waiting for resource)
 *
 * This class only draws/animates; it has no game logic of its own.
 * GraphCanvas decides WHEN to create/remove/recolor these lines based
 * on GameManager's actual state.
 */
public class ConnectionLine {

    public enum LineState { NORMAL, WAITING, DEADLOCK, SAFE }

    private final Line line;
    private LineState currentState;
    private Timeline pulseTimeline;

    // Endpoints, stored so we can animate a data packet along this exact path
    private double startX, startY, endX, endY;

    public ConnectionLine(double startX, double startY, double endX, double endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;

        line = new Line(startX, startY, endX, endY);
        line.getStyleClass().add("connection-line");
        applyState(LineState.NORMAL);
    }

    /**
     * Returns the actual JavaFX Line node, so GraphCanvas can add it to its Pane.
     */
    public Line getLine() {
        return line;
    }

    /**
     * Updates the line's visual state (color/glow/pulse) based on the
     * current game situation (normal allocation, waiting/request, deadlock, or safe).
     */
    public void applyState(LineState state) {
        this.currentState = state;
        stopPulse();

        line.getStyleClass().removeAll(
                "connection-normal", "connection-waiting", "connection-deadlock", "connection-safe"
        );

        switch (state) {
            case NORMAL:
                line.getStyleClass().add("connection-normal");
                break;
            case WAITING:
                line.getStyleClass().add("connection-waiting");
                break;
            case SAFE:
                line.getStyleClass().add("connection-safe");
                startPulse(Color.web("#22C55E"), false);
                break;
            case DEADLOCK:
                line.getStyleClass().add("connection-deadlock");
                startPulse(Color.web("#EF4444"), true);
                break;
        }
    }

    /**
     * Starts a pulsing glow effect on the line.
     * @param continuous if true, loops forever (deadlock); if false, plays twice then stops (safe state flash)
     */
    private void startPulse(Color color, boolean continuous) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(8);
        line.setEffect(glow);

        pulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 8)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(glow.radiusProperty(), 20)),
                new KeyFrame(Duration.seconds(1.0), new KeyValue(glow.radiusProperty(), 8))
        );
        pulseTimeline.setCycleCount(continuous ? Timeline.INDEFINITE : 2);
        pulseTimeline.play();
    }

    private void stopPulse() {
        if (pulseTimeline != null) {
            pulseTimeline.stop();
            pulseTimeline = null;
        }
    }

    /**
     * Animates a small glowing "data packet" circle traveling along this
     * connection's path, from start to end. Used when a resource is
     * requested/allocated, to visually show the request moving.
     * The parent Pane must be passed so the packet can be added/removed from it.
     */
    public void animateDataPacket(Pane parentPane) {
        Circle packet = new Circle(6);
        packet.getStyleClass().add("data-packet");
        packet.setTranslateX(startX);
        packet.setTranslateY(startY);

        parentPane.getChildren().add(packet);

        Path path = new Path();
        path.getElements().add(new MoveTo(startX, startY));
        path.getElements().add(new javafx.scene.shape.LineTo(endX, endY));

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(0.6));
        pathTransition.setPath(path);
        pathTransition.setNode(packet);
        pathTransition.setCycleCount(1);

        // Remove the packet from the scene once it reaches the destination
        pathTransition.setOnFinished(e -> parentPane.getChildren().remove(packet));
        pathTransition.play();
    }

    /**
     * Animates a small glowing packet moving in a continuous back-and-forth
     * loop along this line — used to represent a BLOCKED request during
     * deadlock visualization (the process keeps "trying" but never succeeds).
     */
    public void animateBlockedLoop(Pane parentPane) {
        Circle packet = new Circle(6);
        packet.getStyleClass().add("data-packet-blocked");
        packet.setTranslateX(startX);
        packet.setTranslateY(startY);
        parentPane.getChildren().add(packet);

        Path forwardPath = new Path();
        forwardPath.getElements().add(new MoveTo(startX, startY));
        forwardPath.getElements().add(new javafx.scene.shape.LineTo(endX, endY));

        PathTransition forward = new PathTransition(Duration.seconds(0.8), forwardPath, packet);
        forward.setAutoReverse(true);
        forward.setCycleCount(PathTransition.INDEFINITE);
        forward.play();

        // Store reference so GraphCanvas can stop/remove it later if needed.
        packet.setUserData(forward);
    }

    public LineState getCurrentState() {
        return currentState;
    }
}