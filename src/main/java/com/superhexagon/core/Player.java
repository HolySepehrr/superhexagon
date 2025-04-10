package com.superhexagon.core;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class Player {
    private Shape shape;
    private double centerX;
    private double centerY;
    private double radius;
    private double angle;
    private int sides;
    private double anglePerSide;

    public Player(double centerX, double centerY, double radius, int sides) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.sides = sides;
        this.anglePerSide = 360.0 / sides;
        this.angle = 0;
        createTriangle();
        updatePosition(0);
    }

    private void createTriangle() {
        Polygon triangle = new Polygon();
        double size = 10;
        triangle.getPoints().addAll(
                0.0, -size,
                -size * Math.sqrt(3) / 2, size / 2,
                size * Math.sqrt(3) / 2, size / 2
        );
        triangle.setFill(Color.WHITE);
        this.shape = triangle;
    }

    public void updatePosition(double rotationAngle) {
        double totalAngle = angle + rotationAngle; // زاویه بازیکن + زاویه چرخش ۶ ضلعی
        shape.setTranslateX(centerX + radius * Math.cos(Math.toRadians(totalAngle)));
        shape.setTranslateY(centerY + radius * Math.sin(Math.toRadians(totalAngle)));
        shape.setRotate(totalAngle);
    }

    public void rotateLeft() {
        angle -= anglePerSide;
        if (angle < 0) {
            angle += 360;
        }
    }

    public void rotateRight() {
        angle += anglePerSide;
        if (angle >= 360) {
            angle -= 360;
        }
    }

    public void update(double deltaTime, double rotationAngle) {
        updatePosition(rotationAngle);
    }

    public Shape getShape() {
        return shape;
    }

    public double getAngle() {
        return angle;
    }
}