package com.superhexagon.core;

import javafx.scene.shape.Shape; // اضافه کردن ایمپورت Shape

public abstract class GameObject {
    protected double x, y; // موقعیت
    protected Shape shape; // شکل گرافیکی

    public GameObject(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update(double deltaTime); // به‌روزرسانی موقعیت و وضعیت
    public abstract Shape getShape(); // برگرداندن شکل برای رندر
}