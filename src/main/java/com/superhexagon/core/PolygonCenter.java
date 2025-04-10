package com.superhexagon.core;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class PolygonCenter {
    private Shape shape;
    private double centerX;
    private double centerY;
    private double radius;
    private int sides;

    public PolygonCenter(double centerX, double centerY, double radius, int sides) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.sides = sides;
        createPolygon();
    }

    private void createPolygon() {
        Polygon polygon = new Polygon();
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            polygon.getPoints().addAll(x, y);
        }
        polygon.setFill(Color.BLACK);
        polygon.setStroke(Color.WHITE);
        this.shape = polygon; // مطمئن شو که shape مقداردهی شده
    }

    public void rotate(double angle) {
        shape.setRotate(shape.getRotate() + angle);
    }

    public Shape getShape() {
        return shape;
    }

    public int getSides() {
        return sides;
    }
}