package com.smartparking.ui;

import com.smartparking.domain.Role;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Role-aware login screen.
 * It selects a demo role and hands navigation over to the application shell; it contains no RBAC
 * or authentication business logic.
 */
public final class LoginView {
    private final BorderPane root = new BorderPane();
    private final UiServices services;
    private final Stage stage;
    private boolean dark = true;

    public LoginView(Stage stage, UiServices services) {
        this.stage = stage;
        this.services = services;
        build();
    }

    public Pane getRoot() {
        return root;
    }

    public void applyTheme(Scene scene) {
        ThemeManager.apply(scene, dark);
        scene.getRoot().getStyleClass().removeAll("light-theme", "dark-theme");
        scene.getRoot().getStyleClass().add(dark ? "dark-theme" : "light-theme");
    }

    private void build() {
        root.getStyleClass().add("login-root");

        VBox branding = new VBox(12);
        branding.getStyleClass().add("login-brand");
        branding.setAlignment(Pos.CENTER_LEFT);
        branding.setPadding(new Insets(58));

        Label eyebrow = new Label("SMART MOBILITY");
        eyebrow.getStyleClass().add("eyebrow");
        Label title = new Label("SmartParking");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Find your space. Save your time.");
        subtitle.getStyleClass().add("login-subtitle");
        branding.getChildren().addAll(eyebrow, title, subtitle);

        VBox card = new VBox(18);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(420);
        card.setPadding(new Insets(34));

        Label welcome = new Label("Welcome back");
        welcome.getStyleClass().add("section-title");
        Label hint = new Label("Sign in to your parking workspace");
        hint.getStyleClass().add("muted");

        VBox emailGroup = floatingField("Email", "email");
        TextField email = (TextField) emailGroup.getChildren().get(1);
        email.setText("demo@smartparking.in");

        VBox passwordGroup = floatingPassword("Password");
        PasswordField password = (PasswordField) passwordGroup.getChildren().get(1);
        password.setText("password");

        ComboBox<Role> role = new ComboBox<>();
        role.getItems().setAll(Role.DRIVER, Role.ATTENDANT, Role.ADMIN);
        role.setValue(Role.DRIVER);
        role.setMaxWidth(Double.MAX_VALUE);
        role.getStyleClass().add("role-select");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        Button login = new Button("Sign in");
        login.getStyleClass().addAll("primary-button", "large-button");
        login.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(login, javafx.scene.layout.Priority.ALWAYS);

        Button theme = new Button("☀");
        theme.getStyleClass().add("icon-button");
        theme.setOnAction(event -> {
            dark = !dark;
            theme.setText(dark ? "☀" : "☾");
            applyTheme(stage.getScene());
        });
        actions.getChildren().addAll(login, theme);

        login.setOnAction(event -> {
            if (email.getText().trim().isEmpty() || password.getText().trim().isEmpty()) {
                email.getStyleClass().add("input-error");
                password.getStyleClass().add("input-error");
                ToastNotification.show(root, "Enter your email and password", false);
                return;
            }
            ShellView shell = new ShellView(stage, services, role.getValue());
            Scene scene = stage.getScene();
            shell.show(scene);
        });

        card.getChildren().addAll(welcome, hint, emailGroup, passwordGroup, role, actions);

        StackPane right = new StackPane(card);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(48));
        root.setLeft(branding);
        root.setCenter(right);
        BorderPane.setAlignment(branding, Pos.CENTER);
    }

    private VBox floatingField(String labelText, String prompt) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("floating-input");
        VBox group = new VBox(6, label, field);
        return group;
    }

    private VBox floatingPassword(String labelText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");
        PasswordField field = new PasswordField();
        field.setPromptText("••••••••");
        field.getStyleClass().add("floating-input");
        return new VBox(6, label, field);
    }
}
