package com.smartparking.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point for SmartParking V2.
 * It creates the shared UI service container and starts with the login view.
 */
public final class SmartParkingApplication extends Application {
    @Override
    public void start(Stage stage) {
        UiServices services = UiServices.createDemo();
        LoginView login = new LoginView(stage, services);

        Scene scene = new Scene(login.getRoot(), 1180, 760);
        login.applyTheme(scene);
        stage.setTitle("SmartParking V2");
        stage.setMinWidth(960);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
