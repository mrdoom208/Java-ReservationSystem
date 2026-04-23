package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.dto.DashboardCountsDTO;
import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.service.ReservationService;
import com.mycompany.reservationsystem.service.TablesService;
import com.mycompany.reservationsystem.service.UserService;
import com.mycompany.reservationsystem.util.NotificationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.applyStatusStyle;
import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.applyTimeFormat;

@Component
public class DashboardController {
    private AdministratorUIController adminUIController;

    public void setAdminUIController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }

    @FXML
    private TableColumn<Reservation, String> CustomerColm;

    @FXML
    private ScrollPane DashboardPane;

    @FXML
    private TableView<ManageTablesDTO> ManageTableView;

    @FXML
    private TableColumn<Reservation, Integer> PaxColm;

    @FXML
    private TableView<Reservation> RecentReservationTable;

    @FXML
    private TableColumn<ManageTablesDTO, Integer> TableCapacityColum;

    @FXML
    private TableColumn<ManageTablesDTO,String> TableCustomerColum;

    @FXML
    private TableColumn<ManageTablesDTO, String> TableNoColum;

    @FXML
    private TableColumn<ManageTablesDTO,Integer> TablePaxColum;

    @FXML
    private TableColumn<ManageTablesDTO, String> TableStatusColum;

    @FXML
    private TableColumn<ManageTablesDTO, LocalTime> TableTimeColum;

    @FXML
    private TableColumn<Reservation, LocalTime> TimeColm;

    @FXML
    private Label Total_Cancelled;

    @FXML
    private Label Total_Complete;

    @FXML
    private Label Total_CustomerDbd;

    @FXML
    private Label Total_Pending;

    @FXML
    private Label activetable;

    @FXML
    private BorderPane dashpane;

    @FXML
    private LineChart<String, Number> myBarChart;

    @FXML
    private Label dashboardTrendLabel;

    @FXML
    private VBox notificationArea;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private final ObservableList<Reservation> recentReservations = FXCollections.observableArrayList();
    private final ObservableList<ManageTablesDTO> manageTablesData = FXCollections.observableArrayList();

    public void updateLabels() {
        if (adminUIController != null) {
            adminUIController.showQueryLoading("Updating dashboard stats...");
        }

        Task<DashboardCountsDTO> task = new Task<DashboardCountsDTO>() {
            @Override
            protected DashboardCountsDTO call() throws Exception {
                long occupied = TablesService.countByStatus("Occupied");
                long reserved = TablesService.countByStatus("Reserved");
                long totalTables = TablesService.countTables();

                long todayCustomers = ReservationService.countTodayCustomers();
                long pending = ReservationService.countByStatus("Pending");
                long cancelled = ReservationService.countByStatus("Cancelled");
                long completed = ReservationService.countByStatus("Complete");
                return new DashboardCountsDTO(
                        occupied,
                        reserved,
                        totalTables,
                        todayCustomers,
                        pending,
                        cancelled,
                        completed
                );
            }
        };
        task.setOnSucceeded(e->{
            DashboardCountsDTO c = task.getValue();
            long available = c.totalTables() - c.occupied() - c.reserved();
            long busy = c.occupied() + c.reserved();
            activetable.setText(
                    available + "/" + busy + "/" + c.totalTables()
            );
            Total_CustomerDbd.setText(String.valueOf(c.todayCustomers()));
            Total_Pending.setText(String.valueOf(c.pending()));
            Total_Cancelled.setText(String.valueOf(c.cancelled()));
            Total_Complete.setText(String.valueOf(c.completed()));

            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });
        new Thread(task, "dashboard-counts").start();
    }


    public void barchart() {
        if (adminUIController != null) {
            adminUIController.showQueryLoading("Loading chart data...");
        }
        myBarChart.getData().clear();

        CategoryAxis xAxis = (CategoryAxis) myBarChart.getXAxis();
        xAxis.getCategories().clear();

        Task<XYChart.Series<String, Number>> task = new Task<>() {
            @Override
            protected XYChart.Series<String, Number> call() {
                XYChart.Series<String, Number> series = new XYChart.Series<>();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
                LocalDate today = LocalDate.now();
                LocalDate startDate = today.minusDays(15);

                List<Map<String, Object>> reservations = ApiClient.getAllReservationsList();

                for (int i = 15; i >= 0; i--) {
                    LocalDate day = today.minusDays(i);
                    String label = day.format(formatter);

                    long count = reservations.stream()
                            .filter(r -> r.get("date") != null)
                            .filter(r -> {
                                Object dateObj = r.get("date");
                                if (dateObj instanceof String) {
                                    LocalDate date = LocalDate.parse((String) dateObj);
                                    return !date.isBefore(startDate) && !date.isAfter(today) && day.equals(date);
                                }
                                return false;
                            })
                            .count();

                    series.getData().add(new XYChart.Data<>(label, count));
                }

                return series;
            }
        };

        task.setOnSucceeded(e -> {
            XYChart.Series<String, Number> series = task.getValue();

            // Explicitly set categories for X-axis
            ObservableList<String> categories = FXCollections.observableArrayList();
            for (XYChart.Data<String, Number> data : series.getData()) {
                categories.add(data.getXValue());
            }
            xAxis.setCategories(categories);

            myBarChart.getData().add(series);

            if (dashboardTrendLabel != null) {
                dashboardTrendLabel.setText(calculateAvgAndTrend(series));
            }

            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });

        new Thread(task, "bar-chart-loader").start();
    }


    public void setupRecentReservation() {

        RecentReservationTable.setItems(recentReservations);
        applyTimeFormat(TimeColm);


        TableColumn<?, ?>[] column = {CustomerColm, PaxColm, TimeColm};
        double[] widthFactors = {0.4, 0.2, 0.4};
        String[] namecol = {"name","pax","reservationPendingtime"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(true);
            col.setReorderable(false);
            col.prefWidthProperty().bind(
                    RecentReservationTable.widthProperty().multiply(widthFactors[i])
            );
            if (namecol[i].equals("name")) {
                ((TableColumn<Reservation, String>) col).setCellValueFactory(cellData -> {
                    Reservation r = cellData.getValue();
                    String name = (r.getCustomer() != null) ? r.getCustomer().getName() : "";
                    return new SimpleStringProperty(name);
                });
            } else {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }
        RecentReservationTable.setPlaceholder(new Label("No Customer Reservation yet"));

    }

    public void setupTableView(){
        ManageTableView.setItems(manageTablesData);

        applyStatusStyle(TableStatusColum);
        applyTimeFormat(TableTimeColum);


        TableColumn<?, ?>[] column = {TableCustomerColum,TableNoColum,TableStatusColum,TablePaxColum,TableCapacityColum,TableTimeColum};
        double[] widthFactors = {0.2, 0.15, 0.15, 0.1, 0.2, 0.2};
        String[] namecol = {"customer","tableNo","status","pax","capacity","tablestarttime"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(ManageTableView.widthProperty().multiply(widthFactors[i]));
            col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
        }

        ManageTableView.setPlaceholder(new Label("No Table set yet"));

    }


    public void loadRecentReservations() {
        Task<List<Reservation>> task = new Task<>() {
            @Override
            protected List<Reservation> call() {
                List<Map<String, Object>> data = ApiClient.getRecentReservationsList();
                List<Reservation> results = new ArrayList<>();
                for (Map<String, Object> map : data) {
                    Reservation r = new Reservation();
                    r.setId(((Number) map.getOrDefault("id", 0)).longValue());
                    r.setStatus((String) map.get("status"));
                    Object paxObj = map.get("pax");
                    if (paxObj instanceof Number) r.setPax(((Number) paxObj).intValue());
                    Object timeObj = map.get("reservationPendingtime");
                    if (timeObj instanceof String) {
                        r.setReservationPendingtime(LocalTime.parse((String) timeObj));
                    }
                    Object customerObj = map.get("customer");
                    if (customerObj instanceof Map) {
                        Map<String, Object> customerMap = (Map<String, Object>) customerObj;
                        Customer c = new Customer();
                        c.setId(customerMap.get("id") != null ? ((Number) customerMap.get("id")).longValue() : null);
                        c.setName((String) customerMap.get("name"));
                        c.setPhone((String) customerMap.get("phone"));
                        c.setEmail((String) customerMap.get("email"));
                        r.setCustomer(c);
                    }
                    results.add(r);
                }
                return results;
            }
        };
        task.setOnSucceeded(e -> {
            List<Reservation> data = task.getValue();
            recentReservations.setAll(data);
        });
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }
    public void loadTableView() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return ApiClient.getManageTablesDTOList();
            }
        };
        task.setOnSucceeded(e->{
            List<Map<String, Object>> data = task.getValue();
            manageTablesData.clear();
            for (Map<String, Object> row : data) {
                ManageTablesDTO dto = new ManageTablesDTO();
                dto.setId(((Number) row.getOrDefault("id", 0)).longValue());
                dto.setTableNumber((String) row.getOrDefault("tableNo", ""));
                dto.setCapacity(((Number) row.getOrDefault("capacity", 0)).intValue());
                dto.setStatus((String) row.getOrDefault("status", ""));
                dto.setLocation((String) row.getOrDefault("location", ""));
                dto.setCustomer((String) row.getOrDefault("customer", ""));
                
                Object startTimeObj = row.get("tablestarttime");
                if (startTimeObj instanceof String) {
                    try {
                        String timeStr = startTimeObj.toString();
                        if (timeStr.contains(".")) {
                            timeStr = timeStr.split("\\.")[0];
                        }
                        dto.setTablestarttime(LocalTime.parse(timeStr));
                    } catch (Exception ex) {}
                }
                
                manageTablesData.add(dto);
            }
        });
        task.setOnFailed(e-> {
            task.getException().printStackTrace();
        });
        new Thread(task).start();
    }


    @FXML
    public void initialize(){
        dashpane.minHeightProperty().bind(dashpane.widthProperty().multiply(0.965));
        updateLabels();
        loadRecentReservations();
        loadTableView();

        setupRecentReservation();
        setupTableView();
        NotificationManager.setContainer(notificationArea);
        barchart();

    }

    private String calculateAvgAndTrend(XYChart.Series<String, Number> series) {
        if (series.getData().isEmpty()) return "";

        List<Double> values = series.getData().stream()
            .map(d -> d.getYValue() != null ? d.getYValue().doubleValue() : 0.0)
            .filter(v -> v > 0)
            .toList();

        if (values.isEmpty()) return "";

        double avg = values.stream().mapToDouble(v -> v).average().orElse(0);

        String trend = "";
        if (values.size() >= 2) {
            double first = values.get(0);
            double last = values.get(values.size() - 1);
            if (last > first) {
                trend = " ↑";
            } else if (last < first) {
                trend = " ↓";
            } else {
                trend = " →";
            }
        }

        String avgStr = avg >= 1000 ? String.format("%.1fK", avg / 1000) : String.format("%.1f", avg);
        return "Avg: " + avgStr + trend;
    }

}
