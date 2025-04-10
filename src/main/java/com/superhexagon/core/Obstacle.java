package com.superhexagon.core;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class Obstacle {
    private Shape shape;
    private double centerX;
    private double centerY;
    private double minDistance;
    private double maxDistance;
    private double startAngle;
    private double angleWidth;
    private double speed;
    private double currentDistance;
    private boolean active;

    public Obstacle(double centerX, double centerY, double minDistance, double maxDistance, double startAngle, double angleWidth, double speed) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.startAngle = startAngle;
        this.angleWidth = angleWidth;
        this.speed = speed;
        this.currentDistance = maxDistance;
        this.active = true;
        createShape();
        updatePosition(0);
    }

    private void createShape() {
        Polygon wall = new Polygon();
        double innerRadius = currentDistance;
        double outerRadius = innerRadius + 5; // ضخامت کمتر (برای مشکل ۴)
        double startRad = Math.toRadians(startAngle);
        double endRad = Math.toRadians(startAngle + angleWidth);

        wall.getPoints().addAll(
                innerRadius * Math.cos(startRad), innerRadius * Math.sin(startRad),
                innerRadius * Math.cos(endRad), innerRadius * Math.sin(endRad),
                outerRadius * Math.cos(endRad), outerRadius * Math.sin(endRad),
                outerRadius * Math.cos(startRad), outerRadius * Math.sin(startRad)
        );
        wall.setFill(null);
        wall.setStroke(Color.RED);
        wall.setStrokeWidth(3);
        this.shape = wall;
    }

    private void updatePosition(double rotationAngle) {
        double innerRadius = currentDistance;
        double outerRadius = innerRadius + 5; // ضخامت کمتر (برای مشکل ۴)
        double adjustedStartAngle = startAngle + rotationAngle; // اعمال زاویه چرخش ۶ ضلعی
        double startRad = Math.toRadians(adjustedStartAngle);
        double endRad = Math.toRadians(adjustedStartAngle + angleWidth);

        Polygon wall = new Polygon();
        wall.getPoints().addAll(
                innerRadius * Math.cos(startRad), innerRadius * Math.sin(startRad),
                innerRadius * Math.cos(endRad), innerRadius * Math.sin(endRad),
                outerRadius * Math.cos(endRad), outerRadius * Math.sin(endRad),
                outerRadius * Math.cos(startRad), outerRadius * Math.sin(startRad)
        );
        wall.setFill(null);
        wall.setStroke(Color.RED);
        wall.setStrokeWidth(3);
        this.shape = wall;
    }

    public void update(double deltaTime, double rotationAngle) {
        if (!active) return;
        currentDistance -= speed * deltaTime;
        updatePosition(rotationAngle);
        if (currentDistance <= minDistance) {
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }

    public Shape getShape() {
        return shape;
    }
}