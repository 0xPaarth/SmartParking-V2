package com.smartparking.ui;

import javafx.scene.Scene;

/**
 * Centralizes theme stylesheet selection for every JavaFX scene.
 */
public final class ThemeManager {
    private ThemeManager() {
    }

    public static final String DARK = "/css/dark.css";
    public static final String LIGHT = "/css/light.css";

    public static void apply(Scene scene, boolean dark) {
        scene.getStylesheets().clear();
        String stylesheet = dark ? DARK : LIGHT;
        scene.getStylesheets().add(ThemeManager.class.getResource(stylesheet).toExternalForm());
    }
}
