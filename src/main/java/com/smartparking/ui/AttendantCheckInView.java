package com.smartparking.ui;

import com.smartparking.domain.ParkingSession;
import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.VehicleType;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Thin attendant check-in screen.
 * All slot selection and vehicle entry operations are delegated to {@link com.smartparking.service.ParkingService}.
 */
public final class AttendantCheckInView {
    private final VBox root = new VBox(20);

    public AttendantCheckInView(UiServices services, Pane toastParent) {
        build(services, toastParent);
    }

    public Pane getRoot() {
        return root;
    }

    private void build(UiServices services, Pane toastParent) {
        root.getStyleClass().add("content-view");
        root.setPadding(new Insets(24, 28, 28, 28));

        Label heading = new Label("Generate entry ticket");
        heading.getStyleClass().add("section-title");
        Label subtitle = new Label("Scan or enter a walk-in vehicle");
        subtitle.getStyleClass().add("muted");

        GridPane form = new GridPane();
        form.getStyleClass().addAll("card", "form-card");
        form.setHgap(18);
        form.setVgap(16);
        form.setPadding(new Insets(24));

        VBox plateGroup = new VBox(6);
        Label plateLabel = new Label("License Plate");
        plateLabel.getStyleClass().add("floating-label");
        TextField plate = new TextField();
        plate.setPromptText("MH-12-3456");
        plate.getStyleClass().add("floating-input");
        plateGroup.getChildren().addAll(plateLabel, plate);

        VBox typeGroup = new VBox(6);
        Label typeLabel = new Label("Vehicle Type");
        typeLabel.getStyleClass().add("floating-label");
        ComboBox<VehicleType> type = new ComboBox<>();
        type.getItems().setAll(
                VehicleType.BIKE, VehicleType.CAR,
                VehicleType.HEAVY_VEHICLE, VehicleType.EV_CAR);
        type.setValue(VehicleType.CAR);
        type.setMaxWidth(Double.MAX_VALUE);
        type.getStyleClass().add("role-select");
        typeGroup.getChildren().addAll(typeLabel, type);

        VBox slotGroup = new VBox(6);
        Label slotLabel = new Label("Suggested Slot");
        slotLabel.getStyleClass().add("floating-label");
        Label suggested = new Label();
        suggested.getStyleClass().addAll("slot-suggestion");
        ParkingSlot available = services.parkingService().suggestAvailableSlot()
                .orElse(null);
        suggested.setText(available == null ? "No bay available" : available.getId());
        slotGroup.getChildren().addAll(slotLabel, suggested);

        Button generate = new Button("Generate Ticket");
        generate.getStyleClass().addAll("primary-button", "large-button");
        generate.setOnAction(event -> {
            if (plate.getText().isBlank()) {
                plate.getStyleClass().add("input-error");
                ToastNotification.show(toastParent, "License plate is required", false);
                return;
            }
            try {
                ParkingSession session = services.parkingService().vehicleEntry(
                        plate.getText(), type.getValue(), null);
                plate.getStyleClass().remove("input-error");
                plate.clear();
                ParkingSlot next = services.parkingService().suggestAvailableSlot()
                        .orElse(null);
                suggested.setText(next == null ? "No bay available" : next.getId());
                ToastNotification.show(toastParent,
                        "Ticket generated · " + session.getVehicle().getPlateNumber()
                                + " checked into " + session.getSlotId(),
                        true);
            } catch (RuntimeException exception) {
                ToastNotification.show(toastParent, exception.getMessage(), false);
            }
        });

        form.add(plateGroup, 0, 0);
        form.add(typeGroup, 1, 0);
        form.add(slotGroup, 0, 1);
        form.add(generate, 1, 1);
        root.getChildren().addAll(heading, subtitle, form);
    }
}
