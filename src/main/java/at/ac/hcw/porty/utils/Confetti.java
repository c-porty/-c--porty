package at.ac.hcw.porty.utils;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Confetti extends Circle {
    private static final double RADIUS = 10.0;
    private static final double DURATION = 1500;
    private static final double FADE_DURATION = 1500;
    private static final double MAX_X_DISTANCE = 2000;
    private static final double MAX_Y_DISTANCE = 2000;

    public Confetti(Color color, double paneWidth, double paneHeight) {
        super(RADIUS, color);
        // Initial position randomly within the pane's width and height
        setTranslateX(Math.random() * paneWidth - paneWidth / 2);
        setTranslateY(Math.random() * paneHeight - paneHeight / 2);
    }

    public void animate() {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(DURATION), this);
        // Move confetti across a larger area
        translateTransition.setByX(Math.random() * MAX_X_DISTANCE - MAX_X_DISTANCE / 2);
        translateTransition.setByY(Math.random() * MAX_Y_DISTANCE - MAX_Y_DISTANCE / 2);
        translateTransition.setCycleCount(1); // Play animation only once
        translateTransition.setInterpolator(javafx.animation.Interpolator.LINEAR); // Linear movement for consistent speed

        // Fade-out transition
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(FADE_DURATION), this);
        fadeTransition.setFromValue(1.0); // Fully opaque
        fadeTransition.setToValue(0.0); // Fully transparent
        fadeTransition.setCycleCount(1); // Play fade-out only once

        // Start both animations simultaneously
        translateTransition.play();
        fadeTransition.play();
    }
}