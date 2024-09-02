package de.lv1871;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

public class Main extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final int BOID_GROUPS = 5;
    private static final Map<Integer, Color> GROUP_COLORS = new HashMap<>();
    static {
        GROUP_COLORS.put(0, Color.RED);
        GROUP_COLORS.put(1, Color.GREEN);
        GROUP_COLORS.put(2, Color.BLUE);
    }

    private static final int NUMBER_OF_BOIDS = 500;
    private static final double BOID_POLYGON_SIZE = 5;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        final List<Boid> boids = new ArrayList<>();
        final Map<Boid, Polygon> polygons = new HashMap<>();

        Pane flock = new Pane();
        flock.setPrefHeight(HEIGHT);
        flock.setPrefWidth(WIDTH);

        Slider separationSlider = createSlider(0, 3);
        Slider alignmentSlider = createSlider(0, 3);
        Slider cohesionSlider = createSlider(0, 3);
        Slider speedSlider = createSlider(1, 30);
        Slider perceptionRadiusSlider = createSlider(1, 50);
        Slider changeProbabilitySlider = createSlider(0, 1);

        GridPane root = createRootNode(
                separationSlider, alignmentSlider, cohesionSlider,
                speedSlider, perceptionRadiusSlider, changeProbabilitySlider,
                flock);

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("Flocking Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> System.exit(0));

        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            flock.setPrefHeight(newVal.doubleValue());
        });
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            flock.setPrefWidth(newVal.doubleValue());
        });

        Random rand = new Random();

        scene.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                boids.add(new Boid(Vector2D.of(
                        e.getSceneX(),
                        e.getSceneY()),
                        rand.nextInt(BOID_GROUPS)));
            }
        });
        for (int i = 0; i < NUMBER_OF_BOIDS; i++) {
            boids.add(new Boid(Vector2D.of(
                    rand.nextDouble(flock.getWidth()),
                    rand.nextDouble(flock.getHeight())),
                    rand.nextInt(BOID_GROUPS)));
        }

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boids.forEach(boid -> {
                    boid.update(boids, flock.getWidth(), flock.getHeight(),
                            separationSlider.getValue(), alignmentSlider.getValue(), cohesionSlider.getValue(),
                            speedSlider.getValue(), perceptionRadiusSlider.getValue(), changeProbabilitySlider.getValue());

                    Polygon boidPolygon = polygons.get(boid);
                    if (null == boidPolygon) {
                        boidPolygon = createPolygon(boid);

                        polygons.put(boid, boidPolygon);
                        flock.getChildren().add(boidPolygon);
                    }

                    // Set new boid position
                    boidPolygon.setTranslateX(boid.getPosition().getX());
                    boidPolygon.setTranslateY(boid.getPosition().getY());

                    // Rotate boid into movement direction
                    double r = -90 + Math.toDegrees(Math.atan(boid.getVelocity().getY() / boid.getVelocity().getX()));
                    r = boid.getVelocity().getX() < 0 ? r - 180 : r;
                    boidPolygon.setRotate(r);
                });
            }
        };
        timer.start();
    }

    private static Polygon createPolygon(Boid boid) {
        Polygon boidPolygon = new Polygon();
        boidPolygon.getPoints().addAll(0.0, BOID_POLYGON_SIZE, BOID_POLYGON_SIZE, -BOID_POLYGON_SIZE, -BOID_POLYGON_SIZE, -BOID_POLYGON_SIZE);

        int group = boid.getBoidGroup();

        Color color = GROUP_COLORS.get(group);
        if (null == color) {
            color = Color.rgb(new Random().nextInt(1,255), new Random().nextInt(1,255), new Random().nextInt(1,255));
            GROUP_COLORS.put(group, color);
        }

        boidPolygon.setFill(color);
        boidPolygon.setCache(true);
        boidPolygon.setCacheHint(CacheHint.SPEED);
        return boidPolygon;
    }

    private static GridPane createRootNode(Slider separationSlider, Slider alignmentSlider, Slider cohesionSlider,
            Slider speedSlider, Slider perceptionRadiusSlider, Slider changeProbabilitySlider,
            Pane flock) {
        GridPane root = new GridPane();
        root.setBackground(new Background(new BackgroundFill(
                Color.BLACK, null, null)));

        HBox flockSettings = new HBox();
        flockSettings.setSpacing(20);
        flockSettings.setPadding(new Insets(10));

        HBox separationHbox = createLabelWithSlider("Separation", separationSlider);
        HBox alignmentHbox = createLabelWithSlider("Alignment", alignmentSlider);
        HBox cohesionHbox = createLabelWithSlider("Cohesion", cohesionSlider);

        flockSettings.getChildren().addAll(separationHbox, alignmentHbox, cohesionHbox);

        HBox boidSettings = new HBox();
        boidSettings.setSpacing(20);
        boidSettings.setPadding(new Insets(10));

        HBox speedHbox = createLabelWithSlider("Speed", speedSlider);
        HBox perceptionRadiusHbox = createLabelWithSlider("Perception Radius", perceptionRadiusSlider);
        HBox changeProbabilityHbox = createLabelWithSlider("Change Probability", changeProbabilitySlider);

        boidSettings.getChildren().addAll(speedHbox, perceptionRadiusHbox, changeProbabilityHbox);

        root.add(flock, 0, 0);
        root.add(flockSettings, 0, 1);
        root.add(boidSettings, 0, 2);

        return root;
    }

    private static HBox createLabelWithSlider(String Alignment, Slider alignmentSlider) {
        HBox alignmentHbox = new HBox();
        alignmentHbox.setSpacing(5);
        Label alignmentLabel = new Label(Alignment);
        alignmentLabel.setPrefWidth(110);
        alignmentHbox.getChildren().addAll(alignmentLabel, alignmentSlider);
        return alignmentHbox;
    }

    private static Slider createSlider(int min, int max) {
        Slider slider = new Slider();
        slider.setMin(min);
        slider.setMax(max);
        slider.setBlockIncrement(0.1);
        // slider.setShowTickLabels(true);
        // slider.setShowTickMarks(true);
        return slider;
    }
}
