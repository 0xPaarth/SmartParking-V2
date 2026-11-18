package com.smartparking.ui;

import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.Role;
import com.smartparking.domain.SlotStatus;
import com.smartparking.domain.VehicleType;
import com.smartparking.domain.ParkingSession;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalTime;

/**
 * Interactive parking-bay map with animated detail drawer.
 * Bay selection is delegated to this UI controller; booking remains in ReservationService.
 */
public final class SlotMapView {
    private final BorderPane root = new BorderPane();
    private final UiServices services;
    private final Pane toastParent;
    private final VBox detailPanel = new VBox(16);
    private final Role role;
    private GridPane grid;
    private StackPane center;
    private boolean panelOpen;

    public SlotMapView(UiServices services, Pane toastParent, Role role) {
        this.services = services;
        this.toastParent = toastParent;
        this.role = role;
        build();
    }

    public Pane getRoot() {
        return root;
    }

    private void build() {
        root.getStyleClass().add("slot-map-view");

        VBox header = new VBox(4);
        header.setPadding(new Insets(24, 28, 10, 28));
        Label title = new Label("Live slot map");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Select a bay to inspect live details");
        subtitle.getStyleClass().add("muted");
        header.getChildren().addAll(title, subtitle);
        root.setTop(header);

        grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(24);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(28));

        for (ParkingSlot slot : services.slots().findAll()) {
            String name = slot.getId();
            String[] parts = name.split("-");
            int row = parts[0].charAt(0) - 'A';
            int column = Integer.parseInt(parts[1]) - 1;
            grid.add(createBay(slot), column, row);
        }

        Label zoneA = new Label("ENTRY / EXIT");
        zoneA.getStyleClass().add("map-label");
        HBox mapRow = new HBox(zoneA);
        mapRow.setAlignment(Pos.CENTER);

        VBox map = new VBox(12, mapRow, grid);
        map.setAlignment(Pos.CENTER);
        map.getStyleClass().add("map-panel");
        map.setPadding(new Insets(20));

        center = new StackPane(map);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(8, 28, 28, 28));
        root.setCenter(center);

        detailPanel.getStyleClass().add("detail-panel");
        detailPanel.setPrefWidth(320);
        detailPanel.setMaxWidth(320);
        detailPanel.setPadding(new Insets(24));
        detailPanel.setTranslateX(340);
        detailPanel.setVisible(false);

        StackPane.setAlignment(detailPanel, Pos.CENTER_RIGHT);
        center.getChildren().add(detailPanel);
    }

    private Button createBay(ParkingSlot slot) {
        Button bay = new Button(slot.getId());
        bay.getStyleClass().addAll("parking-bay",
                slot.getStatus() == SlotStatus.AVAILABLE ? "bay-available"
                        : slot.getStatus() == SlotStatus.OCCUPIED ? "bay-occupied"
                        : slot.getStatus() == SlotStatus.RESERVED ? "bay-reserved"
                        : "bay-maintenance");
        bay.setPrefSize(128, 92);
        bay.setOnAction(event -> openDetails(slot));
        return bay;
    }

    private void openDetails(ParkingSlot slot) {
        detailPanel.getChildren().clear();

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        Label title = new Label("BAY " + slot.getId());
        title.getStyleClass().add("detail-title");
        javafx.scene.layout.Region spacer =
                new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = new Button("×");
        close.getStyleClass().add("close-button");
        close.setOnAction(event -> closeDetails());
        header.getChildren().addAll(title, spacer, close);

        Label stateLabel = new Label(slot.getStatus().name());
        stateLabel.getStyleClass().addAll("status-pill", statusStyle(slot.getStatus()));

        VBox plateCard = detailRow("Vehicle plate",
                slot.getVehicle() == null ? "—" : slot.getVehicle().getPlateNumber());
        VBox timeCard = detailRow("Local time",
                LocalTime.now().withNano(0).toString());

        detailPanel.getChildren().addAll(header, stateLabel, plateCard, timeCard);
        if (role == Role.ATTENDANT && slot.getStatus() == SlotStatus.AVAILABLE) {
            Button assign = new Button("Assign Walk-in Vehicle");
            assign.getStyleClass().addAll("primary-button", "large-button");
            assign.setMaxWidth(Double.MAX_VALUE);
            assign.setOnAction(event -> openWalkInDialog(slot));
            detailPanel.getChildren().add(assign);
        }

        if (!panelOpen) {
            detailPanel.setVisible(true);
            panelOpen = true;
        }

        TranslateTransition slide = new TranslateTransition(Duration.millis(320), detailPanel);
        slide.setToX(0);
        slide.play();

        if (slot.getStatus() == SlotStatus.AVAILABLE) {
            ToastNotification.show(toastParent, "Bay " + slot.getId() + " is ready to reserve", true);
        }
    }

    private void openWalkInDialog(ParkingSlot slot) {
        Dialog<WalkInInput> dialog = new Dialog<>();
        dialog.setTitle("Assign Walk-in Vehicle");
        dialog.setHeaderText("Check in to bay " + slot.getId());

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().addAll(root.getScene() == null
                ? java.util.List.of()
                : root.getScene().getStylesheets());
        pane.getStyleClass().add("walk-in-dialog");
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        VBox form = new VBox(14);
        form.getStyleClass().add("dialog-form");
        Label plateLabel = new Label("License Plate");
        plateLabel.getStyleClass().add("floating-label");
        TextField plate = new TextField();
        plate.setPromptText("MH-12-3456");
        plate.getStyleClass().add("floating-input");

        Label typeLabel = new Label("Vehicle Type");
        typeLabel.getStyleClass().add("floating-label");
        ComboBox<VehicleType> type = new ComboBox<>();
        type.getItems().setAll(
                VehicleType.BIKE, VehicleType.CAR,
                VehicleType.HEAVY_VEHICLE, VehicleType.EV_CAR);
        type.setValue(VehicleType.CAR);
        type.setMaxWidth(Double.MAX_VALUE);

        form.getChildren().addAll(plateLabel, plate, typeLabel, type);
        pane.setContent(form);

        dialog.setResultConverter(button -> button == ButtonType.OK
                ? new WalkInInput(plate.getText(), type.getValue())
                : null);

        dialog.showAndWait().ifPresent(input -> {
            if (input.plateNumber().isBlank()) {
                plate.getStyleClass().add("input-error");
                ToastNotification.show(toastParent, "License plate is required", false);
                return;
            }
            try {
                ParkingSession session = services.parkingService().vehicleEntry(
                        input.plateNumber(), input.vehicleType(), slot.getId());
                refreshMap();
                closeDetails();
                ToastNotification.show(toastParent,
                        "Vehicle " + session.getVehicle().getPlateNumber()
                                + " checked into " + slot.getId(),
                        true);
            } catch (RuntimeException exception) {
                ToastNotification.show(toastParent, exception.getMessage(), false);
            }
        });
    }

    private void refreshMap() {
        grid.getChildren().clear();
        for (ParkingSlot slot : services.slots().findAll()) {
            String[] parts = slot.getId().split("-");
            int row = parts[0].charAt(0) - 'A';
            int column = Integer.parseInt(parts[1]) - 1;
            grid.add(createBay(slot), column, row);
        }
    }

    private VBox detailRow(String labelText, String value) {
        VBox row = new VBox(4);
        row.getStyleClass().add("detail-row");
        Label label = new Label(labelText);
        label.getStyleClass().add("card-label");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("detail-value");
        row.getChildren().addAll(label, valueLabel);
        return row;
    }

    private void closeDetails() {
        TranslateTransition slide = new TranslateTransition(Duration.millis(260), detailPanel);
        slide.setToX(340);
        slide.setOnFinished(event -> {
            detailPanel.setVisible(false);
            panelOpen = false;
        });
        slide.play();
    }

    private String statusStyle(SlotStatus status) {
        return switch (status) {
            case AVAILABLE -> "status-available";
            case OCCUPIED -> "status-occupied";
            case RESERVED -> "status-reserved";
            case MAINTENANCE -> "status-maintenance";
        };
    }

    private record WalkInInput(String plateNumber, VehicleType vehicleType) {
    }
}
