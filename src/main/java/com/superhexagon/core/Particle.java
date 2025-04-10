package com.superhexagon.core;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.Random;

public class Particle {
    private Circle shape;
    private double velocityX;
    private double velocityY;
    private double life;
    private double maxLife;

    public Particle(double centerX, double centerY) {
        Random random = new Random();
        this.shape = new Circle(centerX, centerY, 3, Color.WHITE); // دایره کوچک سفید
        this.velocityX = (random.nextDouble() - 0.5) * 200; // سرعت تصادفی در جهت X
        this.velocityY = (random.nextDouble() - 0.5) * 200; // سرعت تصادفی در جهت Y
        this.maxLife = 1.0; // طول عمر ذره (۱ ثانیه)
        this.life = maxLife;
    }

    public Circle getShape() {
        return shape;
    }

    public boolean isAlive() {
        return life > 0;
    }


    public void update(double deltaTime) {
        // حرکت ذره
        shape.setCenterX(shape.getCenterX() + velocityX * deltaTime);
        shape.setCenterY(shape.getCenterY() + velocityY * deltaTime);

        // کاهش طول عمر
        life -= deltaTime;

        // محو شدن تدریجی
        double opacity = life / maxLife;
        shape.setOpacity(opacity);
    }
}