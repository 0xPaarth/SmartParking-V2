package com.smartparking.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * A small transient notification that slides down and fades out.
 * It is visual-only: callers decide success or failure messages.
 */
public final class ToastNotification {
    private ToastNotification() {
    }

    public static void show(Pane parent, String message, boolean success) {
        StackPane toast = new StackPane();
        toast.setAlignment(Pos.CENTER_RIGHT);
        toast.setPickOnBounds(false);

        Label label = new Label(message);
        label.getStyleClass().addAll("toast", success ? "toast-success" : "toast-error");
        label.setPadding(new Insets(14, 18, 14, 18));
        toast.getChildren().add(label);

        parent.getChildren().add(toast);
        StackPane.setAlignment(label, Pos.TOP_RIGHT);
        toast.setMaxSize(420, 56);

        label.setTranslateY(-90);
        TranslateTransition slide = new TranslateTransition(Duration.millis(360), label);
        slide.setFromY(-90);
        slide.setToY(24);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(260), label);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        new ParallelTransition(slide, fadeIn).play();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(280), label);
        fadeOut.setDelay(Duration.seconds(3));
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> parent.getChildren().remove(toast));
        fadeOut.play();
    }
}
