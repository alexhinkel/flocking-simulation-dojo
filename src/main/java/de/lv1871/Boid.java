package de.lv1871;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

public class Boid {

    private Vector2D position;
    private Vector2D velocity = Vector2D.of(
            new Random().nextDouble(-1, 1),
            new Random().nextDouble(-1, 1));

    private final int boidGroup;

    public Boid(Vector2D position, int boidGroup) {
        this.position = position;
        this.boidGroup = boidGroup;
    }

    public void update(List<Boid> boids, double boundX, double boundY,
            double separationWeight, double alignmentWeight, double cohesionWeight,
            double speed, double perceptionRadius, double changeProbability) {
        // movement
        if (new Random().nextDouble() < changeProbability) {
            velocity = velocity.add(Vector2D.of(new Random().nextDouble() - 0.5, new Random().nextDouble() - 0.5).withNorm(0.5));
        }

        List<Boid> boidsWithinDistance = getBoidsWithinDistance(boids, perceptionRadius);
        if (!boidsWithinDistance.isEmpty()) {
            // separation
            // TODO

            // alignment
            // TODO

            // cohesion
            // TODO
        }

        velocity = velocity.withNorm(speed);
        position = position.add(velocity);
        position = wrapAround(boundX, boundY);
    }

    private List<Boid> getBoidsWithinDistance(List<Boid> boids, double perceptionRadius) {
        List<Boid> boidsWithinDistance = new ArrayList<>();
        for (Boid boid : boids) {
            if (boid == this) {
                continue;
            }
            Vector2D position = boid.getPosition();
            double distance = position.distance(this.position);
            if (distance < perceptionRadius && this.boidGroup == boid.getBoidGroup()) {
                boidsWithinDistance.add(boid);
            }
        }
        return boidsWithinDistance;
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public int getBoidGroup() {
        return boidGroup;
    }

    /**
     * When boid moves off of one side of the screen it reappears on the other side
     */
    private Vector2D wrapAround(double boundX, double boundY) {
        double wrapX = position.getX() > boundX ? position.getX() - boundX : position.getX();
        wrapX = wrapX < 0 ? boundX - wrapX : wrapX;

        double wrapY = position.getY() > boundY ? position.getY() - boundY : position.getY();
        wrapY = wrapY < 0 ? boundY - wrapY : wrapY;

        return Vector2D.of(wrapX, wrapY);
    }
}
