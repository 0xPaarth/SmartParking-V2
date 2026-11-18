package com.smartparking.ui;

import com.smartparking.domain.Role;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Shared application shell for all roles.
 * It renders a completely distinct sidebar for each role and swaps only thin view controllers.
 */
public final class ShellView {
    private final BorderPane root = new BorderPane();
    private final UiServices services;
    private final Role role;
    private final Scene scene;
    private final Label pageTitle = new Label();
    private boolean dark = true;

    public ShellView(StageScope stage, UiServices services, Role role) {
        this.scene = stage.getScene();
        this.services = services;
        this.role = role;
        build(stage);
    }

    public ShellView(javafx.stage.Stage stage, UiServices services, Role role) {
        this(new StageScope(stage), services, role);
    }

    public void show(Scene targetScene) {
        targetScene.setRoot(root);
        ThemeManager.apply(targetScene, dark);
        root.getStyleClass().setAll("app-shell");
    }

    private void build(StageScope stage) {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(26, 18, 18, 18));
        sidebar.setPrefWidth(238);

        Label brand = new Label("SP");
        brand.getStyleClass().add("brand-mark");
        Label brandName = new Label("SmartParking");
        brandName.getStyleClass().add("brand-name");
        HBox brandRow = new HBox(10, brand, brandName);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        sidebar.getChildren().add(brandRow);

        Label navLabel = new Label(role.name() + " WORKSPACE");
        navLabel.getStyleClass().add("nav-label");
        sidebar.getChildren().add(navLabel);

        if (role == Role.DRIVER) {
            navButton(sidebar, "Overview", true, () -> setDriverDashboard());
            navButton(sidebar, "Find Parking", false, () -> setSlotMap());
            navButton(sidebar, "Reservations", false, () -> setSlotMap());
            navButton(sidebar, "Payments", false, () -> setDriverDashboard());
        } else if (role == Role.ATTENDANT) {
            navButton(sidebar, "Operations", true, () -> setAttendantDashboard());
            navButton(sidebar, "Slot Map", false, () -> setSlotMap());
            navButton(sidebar, "Check-In", false, () -> setSlotMap());
            navButton(sidebar, "Check-Out", false, () -> setAttendantDashboard());
        } else {
            navButton(sidebar, "Admin Overview", true, () -> setAdminPlaceholder());
            navButton(sidebar, "Users", false, () -> setAdminPlaceholder());
            navButton(sidebar, "Parking Slots", false, () -> setSlotMap());
            navButton(sidebar, "Reports", false, () -> setAdminPlaceholder());
            navButton(sidebar, "Pricing Rules", false, () -> setAdminPlaceholder());
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button theme = new Button("Toggle theme");
        theme.getStyleClass().add("sidebar-theme");
        theme.setMaxWidth(Double.MAX_VALUE);
        theme.setOnAction(event -> {
            dark = !dark;
            ThemeManager.apply(scene, dark);
        });

        Button logout = new Button("Log out");
        logout.getStyleClass().add("logout-button");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(event -> {
            LoginView login = new LoginView(stage.stage, services);
            scene.setRoot(login.getRoot());
            login.applyTheme(scene);
        });
        sidebar.getChildren().addAll(theme, logout);

        HBox topbar = new HBox();
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setPadding(new Insets(18, 26, 18, 26));
        pageTitle.getStyleClass().add("page-title");
        pageTitle.setText("Overview");
        topbar.getChildren().add(pageTitle);

        root.setLeft(sidebar);
        root.setTop(topbar);
        if (role == Role.DRIVER) {
            setDriverDashboard();
        } else if (role == Role.ATTENDANT) {
            setAttendantDashboard();
        } else {
            setAdminPlaceholder();
        }
    }

    private void navButton(VBox sidebar, String text, boolean selected, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("nav-button", selected ? "nav-button-active" : "");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            sidebar.getChildren().stream()
                    .filter(Button.class::isInstance)
                    .map(Button.class::cast)
                    .forEach(item -> item.getStyleClass().remove("nav-button-active"));
            button.getStyleClass().add("nav-button-active");
            action.run();
        });
        sidebar.getChildren().add(button);
    }

    private void setDriverDashboard() {
        pageTitle.setText("Driver overview");
        root.setCenter(DashboardViews.driverDashboard(services, root));
    }

    private void setAttendantDashboard() {
        pageTitle.setText("Operations");
        root.setCenter(DashboardViews.attendantDashboard(services, root));
    }

    private void setSlotMap() {
        pageTitle.setText("Interactive slot map");
        root.setCenter(new SlotMapView(services, root).getRoot());
    }

    private void setAdminPlaceholder() {
        pageTitle.setText("Admin overview");
        root.setCenter(DashboardViews.adminDashboard(services, root));
    }

    private record StageScope(javafx.stage.Stage stage) {
        Scene getScene() {
            return stage.getScene();
        }
    }
}
