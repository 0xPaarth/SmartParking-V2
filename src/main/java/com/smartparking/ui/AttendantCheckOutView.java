package com.smartparking.ui;

import com.smartparking.domain.ParkingSession;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Thin check-out screen that looks up a live session and delegates calculation/payment release
 * to the parking service.
 */
public final class AttendantCheckOutView {
    private final VBox root = new VBox(20);

    public AttendantCheckOutView(UiServices services, Pane toastParent) {
        build(services, toastParent);
    }

    public Pane getRoot() {
        return root;
    }

    private void build(UiServices services, Pane toastParent) {
        root.getStyleClass().add("content-view");
        root.setPadding(new Insets(24, 28, 28, 28));

        Label heading = new Label("Vehicle check-out");
        heading.getStyleClass().add("section-title");
        Label subtitle = new Label("Find an active session, calculate fare, and take payment");
        subtitle.getStyleClass().add("muted");

        GridPane form = new GridPane();
        form.getStyleClass().addAll("card", "form-card");
        form.setHgap(18);
        form.setVgap(16);
        form.setPadding(new Insets(24));

        VBox lookup = new VBox(6);
        Label plateLabel = new Label("License Plate");
        plateLabel.getStyleClass().add("floating-label");
        TextField plate = new TextField();
        plate.setPromptText("MH-12-3456");
        plate.getStyleClass().add("floating-input");
        lookup.getChildren().addAll(plateLabel, plate);

        Button find = new Button("Find Active Session");
        find.getStyleClass().addAll("secondary-button");

        VBox sessionCard = new VBox(8);
        sessionCard.getStyleClass().add("checkout-session");
        Label sessionStatus = new Label("Enter a plate to begin");
        sessionStatus.getStyleClass().add("muted");
        Label sessionDetails = new Label("—");
        sessionDetails.getStyleClass().add("detail-value");
        Label fareLabel = new Label("₹0");
        fareLabel.getStyleClass().addAll("stat-value", "accent-cyan");
        sessionCard.getChildren().addAll(sessionStatus, sessionDetails, fareLabel);

        final ParkingSession[] active = new ParkingSession[1];
        find.setOnAction(event -> {
            var session = services.parkingService().findActiveSession(plate.getText());
            if (session.isEmpty()) {
                active[0] = null;
                sessionStatus.setText("No active session");
                sessionDetails.setText("Plate " + plate.getText().trim().toUpperCase());
                fareLabel.setText("₹0");
                ToastNotification.show(toastParent, "No active parking session found", false);
                return;
            }
            active[0] = session.get();
            ParkingSession found = session.get();
            long minutes = Duration.between(found.getEntryTime(), java.time.Instant.now()).toMinutes();
            sessionStatus.setText("ACTIVE SESSION · " + found.getSlotId());
            sessionDetails.setText(found.getVehicle().getPlateNumber()
                    + " · " + minutes + " min parked");
            try {
                BigDecimal preview = services.pricingService().calculate(
                        new com.smartparking.service.pricing.PricingContext(
                                found.getVehicle(), found.getEntryTime(),
                                java.time.Instant.now(), 0.75,
                                false, false, false));
                fareLabel.setText("₹" + preview.toPlainString());
            } catch (RuntimeException exception) {
                fareLabel.setText("Calculation unavailable");
            }
        });

        HBox paymentRow = new HBox(12);
        Button receivePayment = new Button("Take Payment & Exit");
        receivePayment.getStyleClass().addAll("primary-button", "large-button");
        receivePayment.setOnAction(event -> {
            if (active[0] == null) {
                ToastNotification.show(toastParent, "Find an active session first", false);
                return;
            }
            try {
                BigDecimal paid = services.parkingService().vehicleExit(active[0]);
                String plateNumber = active[0].getVehicle().getPlateNumber();
                String slotId = active[0].getSlotId();
                active[0] = null;
                sessionStatus.setText("CHECKED OUT");
                sessionDetails.setText(plateNumber + " · released " + slotId);
                fareLabel.setText("Paid ₹" + paid.toPlainString());
                ToastNotification.show(toastParent,
                        "Payment received · " + plateNumber + " checked out", true);
            } catch (RuntimeException exception) {
                ToastNotification.show(toastParent, exception.getMessage(), false);
            }
        });
        paymentRow.getChildren().add(receivePayment);

        form.add(lookup, 0, 0);
        form.add(find, 1, 0);
        form.add(sessionCard, 0, 1, 2, 1);
        form.add(paymentRow, 0, 2, 2, 1);
        root.getChildren().addAll(heading, subtitle, form);
    }
}
