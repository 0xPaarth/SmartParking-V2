package com.smartparking.ui;

import com.smartparking.domain.ParkingSlot;
import com.smartparking.domain.SlotStatus;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.EnumMap;
import java.util.Map;

/**
 * Produces data-rich Driver and Attendant dashboard layouts.
 * Chart data is presentation projection of repository state, not domain or pricing logic.
 */
public final class DashboardViews {
    private DashboardViews() {
    }

    public static Pane driverDashboard(UiServices services, Pane toastParent) {
        VBox view = new VBox(20);
        view.getStyleClass().add("content-view");
        view.setPadding(new Insets(24, 28, 28, 28));

        Label heading = new Label("Good morning, Aarav");
        heading.getStyleClass().add("section-title");
        Label subheading = new Label("Your parking activity at a glance");
        subheading.getStyleClass().add("muted");
        view.getChildren().addAll(heading, subheading);

        GridPane cards = new GridPane();
        cards.setHgap(16);
        cards.setVgap(16);
        cards.getColumnConstraints().addAll(
                column(), column(), column(), column());

        cards.add(statCard("Active reservation", "A-3", "Today · 10:30 AM", "accent-cyan"), 0, 0);
        cards.add(statCard("Parking time", "1h 24m", "Since 09:06 AM", "accent-violet"), 1, 0);
        cards.add(statCard("This month", "₹1,840", "12% below last month", "accent-green"), 2, 0);
        cards.add(statCard("Saved hours", "4h 40m", "Fastest available bays", "accent-orange"), 3, 0);
        view.getChildren().add(cards);

        HBox charts = new HBox(16);
        charts.setAlignment(Pos.CENTER);
        VBox occupancy = chartCard("Weekly occupancy",
                animatedBarChart());
        VBox spending = chartCard("Vehicle mix",
                animatedPieChart());
        HBox.setHgrow(occupancy, Priority.ALWAYS);
        HBox.setHgrow(spending, Priority.ALWAYS);
        charts.getChildren().addAll(occupancy, spending);
        view.getChildren().add(charts);
        return view;
    }

    public static Pane attendantDashboard(UiServices services, Pane toastParent) {
        VBox view = new VBox(20);
        view.getStyleClass().add("content-view");
        view.setPadding(new Insets(24, 28, 28, 28));

        Label heading = new Label("Live operations");
        heading.getStyleClass().add("section-title");
        Label subheading = new Label("Today's zone activity and bay status");
        subheading.getStyleClass().add("muted");
        view.getChildren().addAll(heading, subheading);

        Map<SlotStatus, Integer> statusCounts = new EnumMap<>(SlotStatus.class);
        for (SlotStatus status : SlotStatus.values()) {
            statusCounts.put(status, 0);
        }
        for (ParkingSlot slot : services.slots().findAll()) {
            statusCounts.merge(slot.getStatus(), 1, Integer::sum);
        }

        GridPane cards = new GridPane();
        cards.setHgap(16);
        cards.setVgap(16);
        cards.getColumnConstraints().addAll(
                column(), column(), column(), column());
        cards.add(statCard("Available now",
                String.valueOf(statusCounts.get(SlotStatus.AVAILABLE)),
                "Ready for entry", "accent-green"), 0, 0);
        cards.add(statCard("Occupied",
                String.valueOf(statusCounts.get(SlotStatus.OCCUPIED)),
                "Vehicles currently parked", "accent-cyan"), 1, 0);
        cards.add(statCard("Reserved",
                String.valueOf(statusCounts.get(SlotStatus.RESERVED)),
                "Upcoming arrivals", "accent-violet"), 2, 0);
        cards.add(statCard("Maintenance",
                String.valueOf(statusCounts.get(SlotStatus.MAINTENANCE)),
                "Bays offline", "accent-orange"), 3, 0);
        view.getChildren().add(cards);

        HBox charts = new HBox(16);
        VBox throughput = chartCard("Hourly throughput", animatedBarChart());
        VBox distribution = chartCard("Slot distribution", liveStatusPie(statusCounts));
        HBox.setHgrow(throughput, Priority.ALWAYS);
        HBox.setHgrow(distribution, Priority.ALWAYS);
        charts.getChildren().addAll(throughput, distribution);
        view.getChildren().add(charts);
        return view;
    }

    public static Pane adminDashboard(UiServices services, Pane toastParent) {
        VBox view = new VBox(20);
        view.getStyleClass().add("content-view");
        view.setPadding(new Insets(24, 28, 28, 28));
        Label title = new Label("Admin overview");
        title.getStyleClass().add("section-title");
        Label sub = new Label("System health and revenue controls");
        sub.getStyleClass().add("muted");
        GridPane cards = new GridPane();
        cards.setHgap(16);
        cards.getColumnConstraints().addAll(column(), column(), column());
        cards.add(statCard("Total revenue", "₹18.4K", "+8.2% this week", "accent-green"), 0, 0);
        cards.add(statCard("Registered users", "1,284", "+42 this month", "accent-cyan"), 1, 0);
        cards.add(statCard("System uptime", "99.98%", "All systems normal", "accent-violet"), 2, 0);
        view.getChildren().addAll(title, sub, cards);
        return view;
    }

    private static javafx.scene.layout.ColumnConstraints column() {
        javafx.scene.layout.ColumnConstraints constraint =
                new javafx.scene.layout.ColumnConstraints();
        constraint.setPercentWidth(25);
        constraint.setHgrow(Priority.ALWAYS);
        return constraint;
    }

    private static VBox statCard(String label, String value, String detail, String accent) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("stat-card", "card");
        card.setPadding(new Insets(18));
        Label name = new Label(label);
        name.getStyleClass().add("card-label");
        Label amount = new Label(value);
        amount.getStyleClass().addAll("stat-value", accent);
        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().add("card-detail");
        card.getChildren().addAll(name, amount, detailLabel);
        animateCard(card);
        return card;
    }

    private static VBox chartCard(String title, javafx.scene.Node chart) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("chart-card", "card");
        card.setPadding(new Insets(16));
        Label label = new Label(title);
        label.getStyleClass().add("card-label");
        card.getChildren().addAll(label, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        return card;
    }

    private static BarChart<String, Number> animatedBarChart() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel("Vehicles");
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(260);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] hours = {"08", "10", "12", "14", "16", "18"};
        int[] values = {18, 30, 24, 38, 48, 34};
        for (int i = 0; i < hours.length; i++) {
            XYChart.Data<String, Number> data =
                    new XYChart.Data<>(hours[i], 0);
            series.getData().add(data);
            final int target = values[i];
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(data.YValueProperty(), 0)),
                    new KeyFrame(Duration.millis(650 + i * 80),
                            new KeyValue(data.YValueProperty(), target)));
            timeline.play();
        }
        chart.getData().add(series);
        return chart;
    }

    private static PieChart animatedPieChart() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Cars", 52),
                new PieChart.Data("Bikes", 28),
                new PieChart.Data("EV", 20));
        PieChart chart = new PieChart(data);
        chart.setLabelsVisible(true);
        chart.setAnimated(true);
        chart.setPrefHeight(260);
        return chart;
    }

    private static PieChart liveStatusPie(Map<SlotStatus, Integer> counts) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        data.add(new PieChart.Data("Available", counts.get(SlotStatus.AVAILABLE)));
        data.add(new PieChart.Data("Occupied", counts.get(SlotStatus.OCCUPIED)));
        data.add(new PieChart.Data("Reserved", counts.get(SlotStatus.RESERVED)));
        data.add(new PieChart.Data("Maintenance", counts.get(SlotStatus.MAINTENANCE)));
        PieChart chart = new PieChart(data);
        chart.setAnimated(true);
        chart.setPrefHeight(260);
        return chart;
    }

    private static void animateCard(VBox card) {
        card.setOpacity(0);
        card.setTranslateY(12);
        javafx.animation.FadeTransition fade =
                new javafx.animation.FadeTransition(Duration.millis(420), card);
        fade.setToValue(1);
        javafx.animation.TranslateTransition slide =
                new javafx.animation.TranslateTransition(Duration.millis(420), card);
        slide.setToY(0);
        new javafx.animation.ParallelTransition(fade, slide).play();
    }
}
