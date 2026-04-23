package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.service.ReservationService;
import com.mycompany.reservationsystem.dto.*;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.service.ReportsService;
import com.mycompany.reservationsystem.transition.ChartsTransition;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.mycompany.reservationsystem.transition.NodeTransition.showSmooth;
import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.*;

public class ReportsController {
    private AdministratorUIController adminUIController;

    public ReportsController() {
    }

    public ReportsController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }

    public void setAdminUIController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }

    // ====================== Constants ======================
    private static final int PAGE_SIZE = 100;
    private static final int MAX_CHART_TICKS = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private static final Map<String, String> STATUS_COLORS = Map.of(
            "Pending", "#455A64",
            "Confirm", "#2196F3",
            "Cancelled", "#D32F2F",
            "Seated", "#2E7D32",
            "Complete", "#4CAF50",
            "No Show", "#9C27B0"
    );

    // ====================== FXML Controls ======================
    @FXML private MFXButton ApplyCusrep, ApplyResrep, ApplyRevrep, ApplyTUrep;
    @FXML private MFXButton Customerrpts, Reservationrpts, Salesrpts, TableUsagerpts;
    @FXML private MFXComboBox<String> StatusfilterResrep;

    @FXML private MFXDatePicker dateFromCusrep, dateToCusrep;
    @FXML private MFXDatePicker dateFromResrep, dateToResrep;
    @FXML private MFXDatePicker dateFromRevrep, dateToRevrep;
    @FXML private MFXDatePicker dateFromTUrep, dateToTUrep;

    @FXML private ScrollPane ReportsPane;
    @FXML private HBox CustomerReport, ReservationReport, SalesReport;
    @FXML private GridPane TableUsageReport;
    @FXML private VBox rootVBox, rootVBoxTableUsage, tableInfopane, listContainer, profileContent;
    @FXML private StackPane profileContainer;
    @FXML private ProgressIndicator profileProgress;

    @FXML private PieChart reservationPieChart;
    @FXML private BarChart<String, Number> totalCustomerChart, totalReservationChart, totalSalesChart;
    @FXML private BarChart<String, Number> totalCustomerChartTableUsage, totalReservationChartTableUsage, totalSalesChartTableUsage;
    @FXML private Label reservationAvgLabel, customerAvgLabel, salesAvgLabel;
    @FXML private Label tuReservationAvgLabel, tuCustomerAvgLabel, tuSalesAvgLabel;
    @FXML private Label reservationHighLowLabel, customerHighLowLabel, salesHighLowLabel;
    @FXML private Label tuReservationHighLowLabel, tuCustomerHighLowLabel, tuSalesHighLowLabel;
    @FXML private Label profilePhone, profileTotalVisits, profileTotalSpent, profileInitials;

    //======================= StackPane =======================
    @FXML private StackPane ResRepContainer,CusRepContainer, RevRepContainer, TableUseContainer;
    // ====================== TableViews ======================
    @FXML private TableView<CustomerReportDTO> CusRepTable;
    @FXML private TableView<Reservation> ResRepTable;
    @FXML private TableView<ReservationCustomerDTO> ResInCusRep;
    @FXML private TableView<SalesReportsDTO> RevRepTable;
    @FXML private TableView<TableUsageReportDTO> TableUseRep;
    @FXML private TableView<TableUsageInformationDTO> TableinfoTUrep;

    // ====================== TableColumns ======================
    @FXML private TableColumn<CustomerReportDTO, Double> averageCusrep;
    @FXML private TableColumn<CustomerReportDTO, BigDecimal> totalsalesCusrep;
    @FXML private TableColumn<?, ?> salesResInCusRep;
    @FXML private TableColumn<CustomerReportDTO, ?> phoneCusrep, totalreservationCusrep;

    @FXML private TableColumn<?, ?> dateResrep, paxResrep, referenceResrep, totalreservationRevrep;
    @FXML private TableColumn<?, String> statusResrep;
    @FXML private TableColumn<?, LocalTime> timeResrep;

    @FXML private TableColumn<?, ?> nameResInCusRep, phoneResInCusRep, referenceResInCusRep, dateResInCusRep;
    @FXML private TableColumn<?, String> statusResInCusRep, timeResInCusRep;

    @FXML private TableColumn<SalesReportsDTO, BigDecimal> totalsalesRevrep;
    @FXML private TableColumn<?, ?> dateRevrep, totalcustomerRevrep;

    @FXML private TableColumn<TableUsageReportDTO, BigDecimal> totalsalesTableUseRep;
    @FXML private TableColumn<?, ?> tablenoTableUseRep, totalcusotmerTableUseRep, totalreservationTableUseRep;

    @FXML private TableColumn<?, ?> tablenoTableinfo, paxTableinfo, dateTableinfo, referenceTableinfo, salesTableinfo, timeTableinfo;

    // ====================== Data Models ======================
    private final ObservableList<Reservation> reservationReports = FXCollections.observableArrayList();
    private final ObservableList<CustomerReportDTO> customerReports = FXCollections.observableArrayList();
    private final ObservableList<ReservationCustomerDTO> reservationCustomerDTOS = FXCollections.observableArrayList();
    private final ObservableList<SalesReportsDTO> salesReportDTOS = FXCollections.observableArrayList();
    private final ObservableList<TableUsageReportDTO> tableUsageReportDTOS = FXCollections.observableArrayList();
    private final ObservableList<TableUsageInformationDTO> tableUsageInformationDTOS = FXCollections.observableArrayList();

    // ====================== Filters ======================
    private FilteredList<Reservation> filterReservationReports =
            new FilteredList<>(reservationReports, p -> true);
    private SortedList<Reservation> filterReservationReportsSorted;
    private final FilteredList<CustomerReportDTO> filterCustomerReports =
            new FilteredList<>(customerReports, p -> true);
    private SortedList<CustomerReportDTO> filterCustomerReportsSorted;

    // ====================== Utilities ======================
    private final ChartsTransition chartsTransition = new ChartsTransition();

    // ====================== State Management ======================
    private boolean reservationLoaded, customerLoaded, salesLoaded, tableUsageLoaded;
    private int reservationReportPage = 0;
    private int customerReportPage = 0;
    private int salesReportPage = 0;
    private int tableUsageReportPage = 0;
    private long totalReservationCount = 0;
    private long totalCustomerCount = 0;
    private boolean allReservationDataLoaded = false;
    private boolean allCustomerDataLoaded = false;
    private boolean allSalesDataLoaded = false;
    private boolean allTableUsageDataLoaded = false;
    private boolean isLoading = false;
    private Task<List<ReservationCustomerDTO>> currentProfileTask;

    // ====================== Initialization ======================
    @FXML
    private void initialize() {
        tableInfopane.setManaged(false);
        tableInfopane.setVisible(false);
        
        // Hide and manage profileContainer on initialization
        profileContainer.setManaged(false);
        profileContainer.setVisible(false);
        profileContainer.setOpacity(0);
        
        // Initialize labels - hide initially to prevent showing in corner
        reservationHighLowLabel.setVisible(false);
        customerHighLowLabel.setVisible(false);
        salesHighLowLabel.setVisible(false);
        tuReservationHighLowLabel.setVisible(false);
        tuCustomerHighLowLabel.setVisible(false);
        tuSalesHighLowLabel.setVisible(false);
        
        reservationHighLowLabel.setText("");
        customerHighLowLabel.setText("");
        salesHighLowLabel.setText("");
        tuReservationHighLowLabel.setText("");
        tuCustomerHighLowLabel.setText("");
        tuSalesHighLowLabel.setText("");
        
        // Add CSS styles for animations to containers
        setupContainerAnimations();
        
        // Setup UI first
        setupAllReports();
        setupChartTransitions();
        
        // Defer data loading
        Platform.runLater(() -> {
            Reservationrpts.fire();
        });
    }
    
    private void setupContainerAnimations() {
        if (listContainer != null) {
            listContainer.setStyle(listContainer.getStyle() + "; -fx-transition: all 0.3s ease-in-out;");
        }
        if (profileContainer != null) {
            profileContainer.setStyle(profileContainer.getStyle() + "; -fx-transition: all 0.3s ease-in-out;");
        }
    }
    
    private void slideInFromRight(Node node, double duration) {
        node.setTranslateX(300);
        node.setOpacity(0);
        node.setVisible(true);
        node.setManaged(true);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(duration), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(duration), node);
        slideIn.setFromX(300);
        slideIn.setToX(0);
        
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideIn);
        parallelTransition.play();
    }
    
    private void slideOutToLeft(Node node, double duration, Runnable onFinished) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(duration), node);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(duration), node);
        slideOut.setFromX(0);
        slideOut.setToX(-300);
        
        ParallelTransition parallelTransition = new ParallelTransition(fadeOut, slideOut);
        parallelTransition.setOnFinished(e -> {
            node.setVisible(false);
            node.setManaged(false);
            node.setTranslateX(0);
            if (onFinished != null) {
                onFinished.run();
            }
        });
        parallelTransition.play();
    }

    private void setupAllReports() {
        setupReservationReports();
        setupCustomerReports();
        setupReservationInformation();
        setupSalesReports();
        setupTableUsageReport();
        setupTableUsageInfo();
        Platform.runLater(this::setupLazyLoadingForTables);
    }
    
    private void setupLazyLoadingForTables() {
        setupTableScrollListener(ResRepTable, this::loadMoreReservationReports, reservationReports);
        setupTableScrollListener(CusRepTable, this::loadMoreCustomerReports, customerReports);
        setupTableScrollListener(RevRepTable, this::loadMoreSalesReports, salesReportDTOS);
        setupTableScrollListener(TableUseRep, this::loadMoreTableUsageReports, tableUsageReportDTOS);
    }
    
    private <T> void setupTableScrollListener(TableView<T> tableView, Runnable loadMoreAction, ObservableList<T> dataList) {
        tableView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                try {
                    ScrollPane scrollPane = (ScrollPane) tableView.lookup(".scroll-pane");
                    if (scrollPane != null) {
                        scrollPane.vvalueProperty().addListener((v, oldVal, newVal) -> {
                            if (newVal.doubleValue() >= 0.95 && !isLoading) {
                                loadMoreAction.run();
                            }
                        });
                    }
                } catch (Exception e) {
                    System.out.println("Could not setup scroll listener for table");
                }
            }
        });
    }

    private void setupChartTransitions() {
        chartsTransition.setupHoverExpand(totalReservationChart, rootVBox);
        chartsTransition.setupHoverExpand(totalCustomerChart, rootVBox);
        chartsTransition.setupHoverExpand(totalSalesChart, rootVBox);
        chartsTransition.setupHoverExpand(totalReservationChartTableUsage, rootVBoxTableUsage);
        chartsTransition.setupHoverExpand(totalCustomerChartTableUsage, rootVBoxTableUsage);
        chartsTransition.setupHoverExpand(totalSalesChartTableUsage, rootVBoxTableUsage);
    }

    // ====================== Navigation ======================
    @FXML
    private void navigateReports(ActionEvent event) {
        Button clicked = (Button) event.getSource();

        hideAllReports();
        updateNavigationButtons(clicked);
        showSelectedReport(clicked.getId());
    }

    private void hideAllReports() {
        ReservationReport.setVisible(false);
        TableUsageReport.setVisible(false);
        SalesReport.setVisible(false);
        CustomerReport.setVisible(false);
    }

    private void updateNavigationButtons(Button activeButton) {
        Button[] buttons = {Reservationrpts, Customerrpts, Salesrpts, TableUsagerpts};

        for (Button btn : buttons) {
            if (btn == null) continue;

            btn.getStyleClass().removeAll("navigation-report-btns-active");
            if (!btn.getStyleClass().contains("navigation-report-btns")) {
                btn.getStyleClass().add("navigation-report-btns");
            }
        }

        activeButton.getStyleClass().removeAll("navigation-report-btns");
        if (!activeButton.getStyleClass().contains("navigation-report-btns-active")) {
            activeButton.getStyleClass().add("navigation-report-btns-active");
        }
    }

    private void showSelectedReport(String reportId) {
        switch (reportId) {
            case "Reservationrpts":
                showSmooth(ReservationReport);
                loadReportIfNeeded(() -> loadReservationReports(), () -> reservationLoaded, flag -> reservationLoaded = flag);
                break;

            case "Customerrpts":
                showSmooth(CustomerReport);
                loadReportIfNeeded(() -> loadCustomerReport(), () -> customerLoaded, flag -> customerLoaded = flag);
                break;

            case "Salesrpts":
                showSmooth(SalesReport);
                loadReportIfNeeded(() -> loadSalesReport(), () -> salesLoaded, flag -> salesLoaded = flag);
                break;

            case "TableUsagerpts":
                showSmooth(TableUsageReport);
                loadReportIfNeeded(() -> loadTableUsageReport(), () -> tableUsageLoaded, flag -> tableUsageLoaded = flag);
                break;
        }
    }

    private void loadReportIfNeeded(Runnable loader, java.util.function.Supplier<Boolean> isLoaded,
                                    java.util.function.Consumer<Boolean> setLoaded) {
        if (!isLoaded.get()) {
            loader.run();
            setLoaded.accept(true);
        }
    }

    private void runReportQuery(String loadingMessage, Runnable action) {
        action.run();
    }

    private void finishReportQuery() {
    }

    private void handleReportFailure(Throwable throwable) {
        adminUIController.handleConnectivityIssue(throwable);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    // ====================== Reservation Reports ======================
    private void setupReservationReports() {
        StatusfilterResrep.getItems().addAll("Complete", "Cancelled", "Pending", "No Show", "Seated", "Show All");
        StatusfilterResrep.setText("Show All");

        applyStatusStyle(statusResrep);
        applyTimeFormat(timeResrep);

        setupTableColumns(ResRepTable,
                new TableColumn[]{referenceResrep, paxResrep, statusResrep, timeResrep, dateResrep},
                new double[]{0.2, 0.2, 0.2, 0.2, 0.21},
                new String[]{"reference", "pax", "status", "reservationPendingtime", "date"});

        filterReservationReportsSorted = new SortedList<>(filterReservationReports);
        filterReservationReportsSorted.comparatorProperty().bind(ResRepTable.comparatorProperty());
        ResRepTable.setItems(filterReservationReportsSorted);

        ApplyResrep.setOnAction(e -> runReportQuery("Loading reservation reports...", this::loadReservationReports));
    }

    private void applyReservationFilters() {

        LocalDate from = dateFromResrep.getValue();
        LocalDate to = dateToResrep.getValue();
        String selectedStatus = StatusfilterResrep.getText();

        filterReservationReports.setPredicate(item ->
                isDateInRange(item.getDate(), from, to) &&
                        matchesStatus(item.getStatus(), selectedStatus)
        );

        updateReservationPieChart();
    }

    private boolean isDateInRange(LocalDate date, LocalDate from, LocalDate to) {
        if (from != null && date.isBefore(from)) return false;
        if (to != null && date.isAfter(to)) return false;
        return true;
    }

    private boolean matchesStatus(String itemStatus, String selectedStatus) {
        if ("Show All".equals(selectedStatus)) return true;
        return itemStatus != null && itemStatus.equalsIgnoreCase(selectedStatus);
    }

    public void loadReservationReports() {
        if (adminUIController != null) {
            adminUIController.showQueryLoading("Loading reservation reports...");
        }
        reservationReportPage = 0;
        allReservationDataLoaded = false;
        reservationReports.clear();
        
        Task<List<Reservation>> task = new Task<>() {
            @Override
            protected List<Reservation> call() {
                totalReservationCount = ReservationService.getTotalReservationCount();
                return ReservationService.loadPage(reservationReportPage, PAGE_SIZE);
            }
        };

        task.setOnSucceeded(e -> {
            List<Reservation> results = task.getValue();
            if (results.isEmpty()) {
                allReservationDataLoaded = true;
            } else {
                reservationReports.addAll(results);
                reservationReportPage++;
                if (reservationReports.size() >= totalReservationCount) {
                    allReservationDataLoaded = true;
                }
            }
            applyReservationFilters();
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });

        task.setOnFailed(e -> {
            handleReportFailure(task.getException());
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });
        new Thread(task, "reports-reservation-loader").start();
    }
    
    private void loadMoreReservationReports() {
        if (allReservationDataLoaded || isLoading) return;
        if (reservationReports.size() >= totalReservationCount && totalReservationCount > 0) {
            allReservationDataLoaded = true;
            return;
        }
        isLoading = true;
        
        Task<List<Reservation>> task = new Task<>() {
            @Override
            protected List<Reservation> call() {
                return ReservationService.loadPage(reservationReportPage, PAGE_SIZE);
            }
        };

        task.setOnSucceeded(e -> {
            List<Reservation> results = task.getValue();
            if (results.isEmpty() || reservationReports.size() + results.size() >= totalReservationCount) {
                allReservationDataLoaded = true;
            }
            reservationReports.addAll(results);
            reservationReportPage++;
            isLoading = false;
        });

        task.setOnFailed(e -> {
            handleReportFailure(task.getException());
            isLoading = false;
        });
        new Thread(task, "reports-reservation-more-loader").start();
    }

    private void updateReservationPieChart() {
        Map<String, Long> statusCounts = filterReservationReports.stream()
                .collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting()));

        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        statusCounts.forEach((status, count) -> pieChartData.add(new PieChart.Data(status, count)));

        reservationPieChart.setData(pieChartData);
        applyPieChartColors(statusCounts, total);
    }

    private void applyPieChartColors(Map<String, Long> statusCounts, long total) {
        for (PieChart.Data data : reservationPieChart.getData()) {
            String color = STATUS_COLORS.getOrDefault(data.getName(), "#bdc3c7");
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        }

        Platform.runLater(() -> updatePieChartLegend(statusCounts, total));
    }

    private void updatePieChartLegend(Map<String, Long> statusCounts, long total) {
        for (Node node : reservationPieChart.lookupAll(".chart-legend-item")) {
            if (node instanceof Label label) {
                String status = label.getText();
                long count = statusCounts.getOrDefault(status, 0L);
                double percent = total == 0 ? 0 : (count * 100.0 / total);

                label.setText(String.format("%s (%.1f%% | %d)", status, percent, count));

                String color = STATUS_COLORS.getOrDefault(status, "#bdc3c7");
                applyColorToLegendSymbol(label.getGraphic(), color);
            }
        }
    }

    private void applyColorToLegendSymbol(Node graphic, String color) {
        if (graphic == null) return;

        if (graphic instanceof Region region) {
            region.setStyle("-fx-background-color: " + color + ";");
        } else if (graphic instanceof Shape shape) {
            shape.setFill(Paint.valueOf(color));
            shape.setStroke(Paint.valueOf(color));
        } else {
            graphic.setStyle("-fx-background-color: " + color + "; -fx-fill: " + color + ";");
        }
    }

    // ====================== Customer Reports ======================
    private void setupCustomerReports() {
        applyDecimalFormat(totalsalesCusrep, 2);
        applyDecimalFormat(averageCusrep, 2);

        setupTableColumns(CusRepTable,
                new TableColumn[]{phoneCusrep, totalreservationCusrep, totalsalesCusrep, averageCusrep},
                new double[]{0.25, 0.25, 0.25, 0.25},
                new String[]{"phone", "totalReservation", "totalSales", "averageSales"});

        filterCustomerReportsSorted = new SortedList<>(filterCustomerReports);
        filterCustomerReportsSorted.comparatorProperty().bind(CusRepTable.comparatorProperty());
        CusRepTable.setItems(filterCustomerReportsSorted);
        ApplyCusrep.setOnAction(e -> runReportQuery("Loading customer reports...", this::loadCustomerReport));

        CusRepTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                String customerPhone = newSelection.getPhone();
                String customerName = newSelection.getCustomerName();
                loadReservationInformation(customerPhone, customerName);
            } else {
                if (profileContainer != null) {
                    slideOutToLeft(profileContainer, 250, null);
                }
                reservationCustomerDTOS.clear();
            }
        });
    }

    public void loadCustomerReport() {
        if (adminUIController != null) {
            adminUIController.showQueryLoading("Loading customer reports...");
        }
        LocalDate from = dateFromCusrep.getValue();
        LocalDate to = dateToCusrep.getValue();

        if (from == null && to == null) {
            resetPagination();
            loadCustomerReportPage();
        } else {
            loadCustomerReportFiltered(from, to);
        }
    }

    private void resetPagination() {
        customerReportPage = 0;
        allCustomerDataLoaded = false;
        customerReports.clear();
        CusRepTable.setItems(filterCustomerReportsSorted);
    }

    private void loadCustomerReportPage() {
        if (allCustomerDataLoaded) return;

        if (adminUIController != null) {
            adminUIController.showQueryLoading("Loading customer reports...");
        }

        Task<List<CustomerReportDTO>> task = new Task<>() {
            @Override
            protected List<CustomerReportDTO> call() {
                return ReservationService.loadPageAsCustomerReport(customerReportPage, PAGE_SIZE);
            }
        };

        task.setOnSucceeded(e -> {
            List<CustomerReportDTO> results = task.getValue();
            if (results.isEmpty()) {
                allCustomerDataLoaded = true;
            } else {
                customerReports.addAll(results);
                customerReportPage++;
                if (results.size() < PAGE_SIZE) {
                    allCustomerDataLoaded = true;
                }
            }
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });

        task.setOnFailed(e -> {
            handleReportFailure(task.getException());
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });
        new Thread(task, "reports-customer-page-loader").start();
    }
    
    private void loadMoreCustomerReports() {
        LocalDate from = dateFromCusrep.getValue();
        LocalDate to = dateToCusrep.getValue();
        if (from != null || to != null || allCustomerDataLoaded || isLoading) return;
        if (customerReports.size() >= totalCustomerCount && totalCustomerCount > 0) {
            allCustomerDataLoaded = true;
            return;
        }
        isLoading = true;
        
        Task<List<CustomerReportDTO>> task = new Task<>() {
            @Override
            protected List<CustomerReportDTO> call() {
                return ReservationService.loadPageAsCustomerReport(customerReportPage, PAGE_SIZE);
            }
        };

        task.setOnSucceeded(e -> {
            List<CustomerReportDTO> results = task.getValue();
            if (results.isEmpty() || results.size() < PAGE_SIZE) {
                allCustomerDataLoaded = true;
            }
            customerReports.addAll(results);
            customerReportPage++;
            isLoading = false;
        });

        task.setOnFailed(e -> {
            handleReportFailure(task.getException());
            isLoading = false;
        });
        new Thread(task, "reports-customer-more-loader").start();
    }

    private void loadCustomerReportFiltered(LocalDate from, LocalDate to) {
        if (adminUIController != null) {
            adminUIController.showQueryLoading("Loading customer reports...");
        }

        Task<List<CustomerReportDTO>> task = new Task<>() {
            @Override
            protected List<CustomerReportDTO> call() {
                return ReservationService.loadByDateAsCustomerReport(from, to);
            }
        };

        task.setOnSucceeded(e -> {
            CusRepTable.setItems(FXCollections.observableArrayList(task.getValue()));
            allCustomerDataLoaded = true;
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });

        task.setOnFailed(e -> {
            handleReportFailure(task.getException());
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });
        new Thread(task, "reports-customer-filtered-loader").start();
    }

    // ====================== Reservation Information ======================
    private void setupReservationInformation() {
        applyStatusStyle(statusResInCusRep);

        setupTableColumns(ResInCusRep,
                new TableColumn[]{nameResInCusRep, referenceResInCusRep, statusResInCusRep,
                        salesResInCusRep, timeResInCusRep, dateResInCusRep},
                new double[]{0.2, 0.15, 0.2, 0.15, 0.15, 0.15},
                new String[]{"customerName", "reference", "status", "sales",
                        "time", "date"});

        ResInCusRep.setItems(reservationCustomerDTOS);
    }

    private void loadReservationInformation(String phone, String customerName) {
        if (phone == null || phone.isEmpty()) {
            return;
        }

        if (currentProfileTask != null && currentProfileTask.isRunning()) {
            currentProfileTask.cancel();
        }
        
        final boolean wasVisible = profileContainer.isVisible();
        if (wasVisible) {
            profileProgress.setVisible(true);
            profileContent.setOpacity(0.5);
        }

        final String selectedPhone = phone;
        final String selectedName = customerName;
        
        currentProfileTask = new Task<List<ReservationCustomerDTO>>() {
            @Override
            protected List<ReservationCustomerDTO> call() throws Exception {
                LocalDate from = dateFromCusrep.getValue();
                LocalDate to = dateToCusrep.getValue();
                return ReservationService.getReservationCustomerDTOByPhoneAndDate(selectedPhone, from, to);
            }
        };

        currentProfileTask.setOnSucceeded(e->{
            List<ReservationCustomerDTO> result = currentProfileTask.getValue();
            reservationCustomerDTOS.setAll(result);
            
            profileProgress.setVisible(false);
            profileContent.setOpacity(1.0);

            if (profileContainer != null) {
                if (result.isEmpty()) {
                    slideOutToLeft(profileContainer, 250, null);
                } else {
                    if (profilePhone != null) profilePhone.setText(selectedPhone);
                    if (profileTotalVisits != null) profileTotalVisits.setText(String.valueOf(result.size()));
                    if (profileTotalSpent != null) {
                        double totalSpent = result.stream().mapToDouble(ReservationCustomerDTO::getTotalSpent).sum();
                        profileTotalSpent.setText(String.format("%.2f", totalSpent));
                    }
                    if (profileInitials != null && selectedName != null && !selectedName.isEmpty()) {
                        String[] parts = selectedName.trim().split("\\s+");
                        String initials;
                        if (parts.length >= 2) {
                            initials = Character.toString(parts[0].charAt(0)) + Character.toString(parts[1].charAt(0));
                        } else {
                            initials = Character.toString(selectedName.charAt(0));
                        }
                        profileInitials.setText(initials.toUpperCase());
                    } else if (profileInitials != null) {
                        profileInitials.setText("?");
                    }
                    
                    if (!wasVisible) {
                        slideInFromRight(profileContainer, 300);
                    }
                }
            }
        });

        currentProfileTask.setOnFailed(e->{
            if (currentProfileTask.isCancelled()) return;
            profileProgress.setVisible(false);
            profileContent.setOpacity(1.0);
            handleReportFailure(currentProfileTask.getException());
        });

        new Thread(currentProfileTask, "reports-reservation-info-loader").start();
    }

    // ====================== Sales Reports ======================
    private void setupSalesReports() {
        applyDecimalFormat(totalsalesRevrep, 2);

        setupTableColumns(RevRepTable,
                new TableColumn[]{dateRevrep, totalreservationRevrep, totalcustomerRevrep, totalsalesRevrep},
                new double[]{0.25, 0.25, 0.25, 0.25},
                new String[]{"date", "totalReservation", "totalCustomer", "totalSales"});

        RevRepTable.setItems(salesReportDTOS);
        ApplyRevrep.setOnAction(e -> runReportQuery("Loading sales reports...", this::loadSalesReport));
    }

    public void loadSalesReport() {
        if (adminUIController != null) {
            adminUIController.showQueryLoading("Loading sales reports...");
        }
        LocalDate from = dateFromRevrep.getValue();
        LocalDate to = dateToRevrep.getValue();
        
        salesReportPage = 0;
        allSalesDataLoaded = false;
        salesReportDTOS.clear();

        Task<List<SalesReportsDTO>> task = new Task<List<SalesReportsDTO>>() {
            @Override
            protected List<SalesReportsDTO> call() throws Exception {
                return ReservationService.getSalesReports(from, to);
            }
        };
        task.setOnSucceeded(e->{
            List<SalesReportsDTO> results = task.getValue();
            if (results.isEmpty()) {
                allSalesDataLoaded = true;
            } else {
                salesReportDTOS.addAll(results);
                salesReportPage++;
            }
            loadSalesBarCharts(results, from, to);
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });
        task.setOnFailed(e->{
            handleReportFailure(task.getException());
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });
        new Thread(task, "reports-sales-loader").start();
    }
    
    private void loadMoreSalesReports() {
        LocalDate from = dateFromRevrep.getValue();
        LocalDate to = dateToRevrep.getValue();
        if (allSalesDataLoaded || isLoading) return;
        isLoading = true;
        
        Task<List<SalesReportsDTO>> task = new Task<List<SalesReportsDTO>>() {
            @Override
            protected List<SalesReportsDTO> call() throws Exception {
                return ReservationService.getSalesReportsPage(from, to, salesReportPage, PAGE_SIZE);
            }
        };
        task.setOnSucceeded(e->{
            List<SalesReportsDTO> results = task.getValue();
            if (results.isEmpty() || results.size() < PAGE_SIZE) {
                allSalesDataLoaded = true;
            }
            salesReportDTOS.addAll(results);
            salesReportPage++;
            isLoading = false;
        });
        task.setOnFailed(e->{
            handleReportFailure(task.getException());
            isLoading = false;
        });
        new Thread(task, "reports-sales-more-loader").start();
    }

    private void loadSalesBarCharts(List<SalesReportsDTO> data, LocalDate dateFrom, LocalDate dateTo) {
        clearCharts(totalReservationChart, totalCustomerChart, totalSalesChart);

        if (data == null || data.isEmpty()) return;

        DateRange dateRange = calculateDateRange(data, dateFrom, dateTo);
        List<LocalDate> allDates = generateDateRange(dateRange.from, dateRange.to);
        Map<LocalDate, SalesReportsDTO> dataMap = createDataMap(data);

        int step = Math.max(1, allDates.size() / MAX_CHART_TICKS);

        XYChart.Series<String, Number> reservationSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> customerSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();

        for (int i = 0; i < allDates.size(); i++) {
            LocalDate date = allDates.get(i);
            SalesReportsDTO dto = dataMap.getOrDefault(date, createEmptySalesDTO(date));
            String dateStr = (i % step == 0) ? date.format(DATE_FORMATTER) : "";

            reservationSeries.getData().add(new XYChart.Data<>(dateStr, dto.getTotalReservation()));
            customerSeries.getData().add(new XYChart.Data<>(dateStr, dto.getTotalCustomer()));
            salesSeries.getData().add(new XYChart.Data<>(dateStr, dto.getTotalSales()));
        }

        populateCharts(totalReservationChart, totalCustomerChart, totalSalesChart,
                reservationSeries, customerSeries, salesSeries,
                reservationAvgLabel, customerAvgLabel, salesAvgLabel,
                reservationHighLowLabel, customerHighLowLabel, salesHighLowLabel);
    }

    // ====================== Table Usage Reports ======================
    private void setupTableUsageReport() {
        setupTableColumns(TableUseRep,
                new TableColumn[]{tablenoTableUseRep, totalreservationTableUseRep,
                        totalcusotmerTableUseRep, totalsalesTableUseRep},
                new double[]{0.25, 0.25, 0.25, 0.25},
                new String[]{"tableNo", "totalReservation", "totalCustomer", "totalSales"});

        TableUseRep.setItems(tableUsageReportDTOS);
        ApplyTUrep.setOnAction(e -> runReportQuery("Loading table usage reports...", this::loadTableUsageReport));

        TableUseRep.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadTableUsageInfo(newVal.getTableNo());
                //showTableInfoPane();
            }
        });
    }

    private void showTableInfoPane() {
        if (!tableInfopane.isManaged() && !tableInfopane.isVisible()) {
            tableInfopane.setManaged(true);
            tableInfopane.setVisible(true);
        }
    }

    private void loadTableUsageReport() {
        if (adminUIController != null) {
            adminUIController.showQueryLoading("Loading table usage reports...");
        }
        LocalDate from = dateFromTUrep.getValue();
        LocalDate to = dateToTUrep.getValue();
        
        tableUsageReportPage = 0;
        allTableUsageDataLoaded = false;
        tableUsageReportDTOS.clear();

        Task<List<TableUsageReportDTO>> task = new Task<List<TableUsageReportDTO>>() {
            @Override
            protected List<TableUsageReportDTO> call() throws Exception {
                return ReportsService.getTableUsageReport(from, to);
            }
        };
        task.setOnSucceeded(e->{
            List<TableUsageReportDTO> results = task.getValue();
            if (results.isEmpty()) {
                allTableUsageDataLoaded = true;
            } else {
                tableUsageReportDTOS.addAll(results);
                tableUsageReportPage++;
            }
            loadTableUsageBarCharts(results);
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });
        task.setOnFailed(e->{
            handleReportFailure(task.getException());
            if (adminUIController != null) {
                adminUIController.hideQueryLoading();
            }
        });
        new Thread(task, "reports-table-usage-loader").start();
    }
    
    private void loadMoreTableUsageReports() {
        LocalDate from = dateFromTUrep.getValue();
        LocalDate to = dateToTUrep.getValue();
        if (allTableUsageDataLoaded || isLoading) return;
        isLoading = true;
        
        Task<List<TableUsageReportDTO>> task = new Task<List<TableUsageReportDTO>>() {
            @Override
            protected List<TableUsageReportDTO> call() throws Exception {
                return ReportsService.getTableUsageReportPage(from, to, tableUsageReportPage, PAGE_SIZE);
            }
        };
        task.setOnSucceeded(e->{
            List<TableUsageReportDTO> results = task.getValue();
            if (results.isEmpty() || results.size() < PAGE_SIZE) {
                allTableUsageDataLoaded = true;
            }
            tableUsageReportDTOS.addAll(results);
            tableUsageReportPage++;
            isLoading = false;
        });
        task.setOnFailed(e->{
            handleReportFailure(task.getException());
            isLoading = false;
        });
        new Thread(task, "reports-table-usage-more-loader").start();
    }

    private void setupTableUsageInfo() {
        setupTableColumns(TableinfoTUrep,
                new TableColumn[]{tablenoTableinfo, referenceTableinfo, paxTableinfo,
                        salesTableinfo, timeTableinfo, dateTableinfo},
                new double[]{0.10, 0.25, 0.10, 0.15, 0.20, 0.20},
                new String[]{"tableNo", "reference", "pax", "sales", "time", "date"});

        TableinfoTUrep.setItems(tableUsageInformationDTOS);
    }

    private void loadTableUsageInfo(String tableNo) {
        LocalDate from = dateFromTUrep.getValue();
        LocalDate to = dateToTUrep.getValue();
        new Thread(new Task<>() {
            @Override
            protected Void call() {
                List<TableUsageInformationDTO> results = ReportsService.getTableUsageInfo(from, to, tableNo);

                Platform.runLater(() -> {
                    tableUsageInformationDTOS.setAll(results);

                });
                return null;
            }
        }).start();
    }

    private void loadTableUsageBarCharts(List<TableUsageReportDTO> data) {
        clearCharts(totalReservationChartTableUsage, totalCustomerChartTableUsage, totalSalesChartTableUsage);

        if (data == null || data.isEmpty()) return;

        XYChart.Series<String, Number> reservationSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> customerSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();

        for (TableUsageReportDTO dto : data) {
            String tableNo = dto.getTableNo();
            reservationSeries.getData().add(new XYChart.Data<>(tableNo, dto.getTotalReservation()));
            customerSeries.getData().add(new XYChart.Data<>(tableNo, dto.getTotalCustomer()));
            salesSeries.getData().add(new XYChart.Data<>(tableNo, dto.getTotalSales()));
        }

        populateCharts(totalReservationChartTableUsage, totalCustomerChartTableUsage,
                totalSalesChartTableUsage, reservationSeries, customerSeries, salesSeries,
                tuReservationAvgLabel, tuCustomerAvgLabel, tuSalesAvgLabel,
                tuReservationHighLowLabel, tuCustomerHighLowLabel, tuSalesHighLowLabel);
    }

    // ====================== Utility Methods ======================
    private void setupTableColumns(TableView<?> table, TableColumn<?, ?>[] columns,
                                   double[] widthFactors, String[] propertyNames) {
        for (int i = 0; i < columns.length; i++) {
            TableColumn<?, ?> col = columns[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.setSortable(true);
            col.prefWidthProperty().bind(table.widthProperty().multiply(widthFactors[i]));

            if (i < propertyNames.length && !propertyNames[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(propertyNames[i]));
            }
        }
    }

    private void clearCharts(BarChart<String, Number>... charts) {
        for (BarChart<String, Number> chart : charts) {
            chart.getData().clear();
        }
    }

    private void populateCharts(BarChart<String, Number> chart1, BarChart<String, Number> chart2,
                                BarChart<String, Number> chart3, XYChart.Series<String, Number> series1,
                                XYChart.Series<String, Number> series2, XYChart.Series<String, Number> series3) {
        populateCharts(chart1, chart2, chart3, series1, series2, series3, null, null, null);
    }

    private void populateCharts(BarChart<String, Number> chart1, BarChart<String, Number> chart2,
                                BarChart<String, Number> chart3, XYChart.Series<String, Number> series1,
                                XYChart.Series<String, Number> series2, XYChart.Series<String, Number> series3,
                                Label avgLabel1, Label avgLabel2, Label avgLabel3) {
        chart1.setLegendVisible(false);
        chart2.setLegendVisible(false);
        chart3.setLegendVisible(false);

        chart1.setAnimated(false);
        chart2.setAnimated(false);
        chart3.setAnimated(false);

        chart1.getData().add(series1);
        chart2.getData().add(series2);
        chart3.getData().add(series3);

        ((javafx.scene.chart.CategoryAxis) chart1.getXAxis()).setTickLabelRotation(45);
        ((javafx.scene.chart.CategoryAxis) chart2.getXAxis()).setTickLabelRotation(45);
        ((javafx.scene.chart.CategoryAxis) chart3.getXAxis()).setTickLabelRotation(45);

        if (avgLabel1 != null) {
            avgLabel1.setText(calculateAvgAndTrend(series1));
        }
        if (avgLabel2 != null) {
            avgLabel2.setText(calculateAvgAndTrend(series2));
        }
        if (avgLabel3 != null) {
            avgLabel3.setText(calculateAvgAndTrend(series3));
        }
    }

    private void populateCharts(BarChart<String, Number> chart1, BarChart<String, Number> chart2,
                                BarChart<String, Number> chart3, XYChart.Series<String, Number> series1,
                                XYChart.Series<String, Number> series2, XYChart.Series<String, Number> series3,
                                Label avgLabel1, Label avgLabel2, Label avgLabel3,
                                Label highLowLabel1, Label highLowLabel2, Label highLowLabel3) {
        chart1.setLegendVisible(false);
        chart2.setLegendVisible(false);
        chart3.setLegendVisible(false);

        // Disable animation
        chart1.setAnimated(false);
        chart2.setAnimated(false);
        chart3.setAnimated(false);

        chart1.getData().add(series1);
        chart2.getData().add(series2);
        chart3.getData().add(series3);

        ((javafx.scene.chart.CategoryAxis) chart1.getXAxis()).setTickLabelRotation(45);
        ((javafx.scene.chart.CategoryAxis) chart2.getXAxis()).setTickLabelRotation(45);
        ((javafx.scene.chart.CategoryAxis) chart3.getXAxis()).setTickLabelRotation(45);

        if (avgLabel1 != null) {
            avgLabel1.setText(calculateAvgAndTrend(series1));
        }
        if (avgLabel2 != null) {
            avgLabel2.setText(calculateAvgAndTrend(series2));
        }
        if (avgLabel3 != null) {
            avgLabel3.setText(calculateAvgAndTrend(series3));
        }

        if (highLowLabel1 != null) {
            highLowLabel1.setText(calculateHighLow(series1));
            highLowLabel1.setVisible(true);
        }
        if (highLowLabel2 != null) {
            highLowLabel2.setText(calculateHighLow(series2));
            highLowLabel2.setVisible(true);
        }
        if (highLowLabel3 != null) {
            highLowLabel3.setText(calculateHighLow(series3));
            highLowLabel3.setVisible(true);
        }
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

    private String calculateHighLow(XYChart.Series<String, Number> series) {
        if (series.getData().isEmpty()) return "";

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        String maxLabel = "";
        String minLabel = "";

        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getYValue() != null) {
                double value = data.getYValue().doubleValue();
                if (value > 0) {
                    if (value > max) {
                        max = value;
                        maxLabel = data.getXValue();
                    }
                    if (value < min) {
                        min = value;
                        minLabel = data.getXValue();
                    }
                }
            }
        }

        if (max == Double.MIN_VALUE || min == Double.MAX_VALUE) return "";

        String formatMax = max >= 1000 ? String.format("%.1fK", max / 1000) : String.format("%.0f", max);
        String formatMin = min >= 1000 ? String.format("%.1fK", min / 1000) : String.format("%.0f", min);
        return "↑ " + maxLabel + ": " + formatMax + "   ↓ " + minLabel + ": " + formatMin;
    }

    private DateRange calculateDateRange(List<SalesReportsDTO> data, LocalDate from, LocalDate to) {
        LocalDate minDate = data.stream().map(SalesReportsDTO::getDate)
                .min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxDate = data.stream().map(SalesReportsDTO::getDate)
                .max(LocalDate::compareTo).orElse(LocalDate.now());

        return new DateRange(from != null ? from : minDate, to != null ? to : maxDate);
    }

    private List<LocalDate> generateDateRange(LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }

    private Map<LocalDate, SalesReportsDTO> createDataMap(List<SalesReportsDTO> data) {
        Map<LocalDate, SalesReportsDTO> map = new HashMap<>();
        for (SalesReportsDTO dto : data) {
            map.put(dto.getDate(), dto);
        }
        return map;
    }

    private SalesReportsDTO createEmptySalesDTO(LocalDate date) {
        return new SalesReportsDTO(date, 0L, 0L, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    // ====================== Helper Classes ======================
    private static class DateRange {
        final LocalDate from;
        final LocalDate to;

        DateRange(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }
    }
}
