package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.App;
import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.controller.popup.DeleteDialogController;
import com.mycompany.reservationsystem.controller.popup.addReservationController;
import com.mycompany.reservationsystem.controller.popup.editReservationController;
import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.dto.ReservationTablelogsDTO;
import com.mycompany.reservationsystem.dto.WebUpdateDTO;
import com.mycompany.reservationsystem.hardware.DeviceDetectionManager;
import com.mycompany.reservationsystem.service.ActivityLogService;
import com.mycompany.reservationsystem.model.*;
import com.mycompany.reservationsystem.service.ReservationService;
import com.mycompany.reservationsystem.service.TablesService;
import com.mycompany.reservationsystem.service.MessageService;
import com.mycompany.reservationsystem.service.MessageDispatchService;
import com.mycompany.reservationsystem.websocket.WebSocketClient;
import com.mycompany.reservationsystem.service.PermissionService;
import com.mycompany.reservationsystem.service.ReportsService;
import com.mycompany.reservationsystem.transition.LabelTransition;
import com.mycompany.reservationsystem.util.ComboBoxUtil;
import com.mycompany.reservationsystem.util.NotificationManager;
import com.mycompany.reservationsystem.util.NotificationUtil;
import io.github.palexdev.materialfx.controls.*;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.*;

public class ReservationController {
    private static final String MESSAGE_NOTIFY_CUSTOMER_KEY = "message.notify_customer";


    private AdministratorUIController adminUIController;
    public User currentuser;
    private static ActivityLogService activityLogService = new ActivityLogService();
    private static MessageService messageService = new MessageService();
    private static MessageDispatchService messageDispatchService = new MessageDispatchService();
    private static PermissionService permissionService = new PermissionService();

    /* ===================== TABLE VIEWS ===================== */

    @FXML
    private TableView<ManageTables> AvailableTable;

    @FXML
    private TableView<Reservation> CustomerReservationTable, SCNReservations;

    @FXML
    private TableView<ReservationTableLogs> ReservationLogs;

    /* ===================== TABLE COLUMNS ===================== */

    // ManageTables columns
    @FXML
    private TableColumn<ManageTables, String> TableNoAT, StatusAT, LocationAT;

    @FXML
    private TableColumn<ManageTables, Integer> CapacityAT;

    // Reservation columns (String)
    @FXML
    private TableColumn<Reservation, String>
            NameCRT, PreferCRT, ReferenceCRT,phoneSCNR,
            EmailCRT, PhoneCRT, StatusCRT,refSCNR,
            customerSCNR, statusSCNR;

    // Reservation columns (Number / Time / Date)
    @FXML
    private TableColumn<Reservation, Integer> PaxCRT, paxSCNR;

    @FXML
    private TableColumn<Reservation, String> TableNoCRT;

    @FXML
    private TableColumn<Reservation, LocalTime>
            TimeCRT, regSCNR, seatedSCNR,
            cancelSCNR, noshowSCNR, seatedRL;

    @FXML
    private TableColumn<Reservation, LocalDate> dateSCNR;

    // ReservationTableLogs columns
    @FXML
    private TableColumn<ReservationTableLogs, String> customerRL, statusRL;

    @FXML
    private TableColumn<ReservationTableLogs, LocalTime>
            pendingRL, confirmRL, completeRL;

    @FXML
    private TableColumn<ReservationTableLogs, LocalDate> dateRL;

    // Generic / unresolved columns
    @FXML
    private TableColumn<?, ?>
            refRL, paxRL,
            phoneRL,
            preferRL, tablenoRL;

    /* ===================== CONTROLS ===================== */

    @FXML
    private MFXButton Mergebtn, newcustomer, reservationrefresh,sendsms,editreservation,cancelreservation;

    @FXML
    private MFXComboBox reservationfilter;


    @FXML
    private MFXTextField SearchCL;

    /* ===================== LAYOUT ===================== */

    @FXML
    private ScrollPane ReservationPane;

    @FXML
    private BorderPane reservpane;

    @FXML
    private HBox hboxCRT;

    @FXML
    private VBox hiddenTable;

    /* ===================== LABELS ===================== */

    @FXML
    private Label response,
            CusToTable,
            pending, confirm, seated,
            cancelled, noshow, complete;

    /* ===================== STATE ===================== */

    private Reservation selectedReservation;
    private Message selectedMessage;


    WebSocketClient  webSocketClient;

    public ReservationController() {
    }

    public ReservationController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }

    public void setAdminUIController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }


    private final ObservableList<Reservation> reservationsData = FXCollections.observableArrayList();
    private final ObservableList<ManageTables> availableTables = FXCollections.observableArrayList();
    private final ObservableList<ReservationTableLogs> reservationlogsdata = FXCollections.observableArrayList();
    private final ObservableList<Message> messageData = FXCollections.observableArrayList();

    FilteredList<ManageTables> filteredtable;
    SortedList<ManageTables> filteredtableSorted;
    FilteredList<Reservation> filteredReservationList = new FilteredList<>(reservationsData);
    FilteredList<Reservation> secondfilteredReservationList = new FilteredList<>(filteredReservationList);
    FilteredList<Reservation> filteredSCNRList = new FilteredList<>(reservationsData);
    SortedList<Reservation> filteredSCNRListSorted;
    private LocalDate applyDate = LocalDate.now();


    int currentpax;
    private ManageTables selectedTable;
    private Long prevRow;
    private Long currentRow;

    ///////////////////////LOAD MASTER LIST////////////////////////////////////////
    public void loadReservationsData() {
        Task<List<Reservation>> task = new Task<>() {
            @Override
            protected List<Reservation> call() {
                return ReservationService.loadPage(0, 1000);
            }
        };

        task.setOnSucceeded(e -> {
            reservationsData.setAll(task.getValue());
            long pendingsize = reservationsData.stream().filter(r -> "Pending".equals(r.getStatus())).count();
            pending.setText(String.valueOf(pendingsize));

            long confirmsize = reservationsData.stream().filter(r -> "Confirm".equals(r.getStatus())).count();
            confirm.setText(String.valueOf(confirmsize));


            long seatedsize = reservationsData.stream().filter(r -> "Seated".equals(r.getStatus())).count();
            seated.setText(String.valueOf(seatedsize));

            long cancelledsize = reservationsData.stream().filter(r -> "Cancelled".equals(r.getStatus())).count();
            cancelled.setText(String.valueOf(cancelledsize));

            long noshowsize = reservationsData.stream().filter(r -> "No Show".equals(r.getStatus())).count();
            noshow.setText(String.valueOf(noshowsize));

        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
        });

        new Thread(task, "load-reservations").start();


    }

    //////////////////////CUSTOMER RESERVATION TABLE////////////////////
    public void setupCustomerReservationTable() {

        CustomerReservationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedReservation = newSelection;
            editreservation.setDisable(newSelection == null);
            boolean canCancel = permissionService.hasPermission(currentuser, "VIEW_CANCEL_RESERVATION");
            cancelreservation.setDisable(!canCancel || newSelection == null);
            if (selectedReservation != null) {
                int remaining = NotificationUtil.getRemainingCooldown(selectedReservation, 120);
                if (remaining > 0) {
                    sendsms.setDisable(true);
                    sendsms.setText("Wait " + remaining + "s");
                } else {
                    sendsms.setDisable(false);
                    sendsms.setText("Notify Customer");
                }
            } else {
                sendsms.setDisable(true);
                sendsms.setText("Notify Customer");
            }
        });
        SortedList<Reservation> sorted = new SortedList<>(secondfilteredReservationList);
        sorted.comparatorProperty().bind(CustomerReservationTable.comparatorProperty());

        CustomerReservationTable.setItems(sorted);
        String[] statuses = {"Confirm", "Pending", "Show All"};
        addItemsToCombo(reservationfilter, filteredReservationList, Reservation::getStatus,statuses);

        SearchCL.textProperty().addListener((observable, oldValue, newValue) -> {
            secondfilteredReservationList.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // ----- CUSTOMER FIELDS -----
                if (item.getCustomer() != null) {

                    if (item.getCustomer().getName() != null &&
                            item.getCustomer().getName().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }

                    if (item.getCustomer().getPhone() != null &&
                            item.getCustomer().getPhone().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }

                    if (item.getCustomer().getEmail() != null &&
                            item.getCustomer().getEmail().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                }

                if (item.getStatus() != null &&
                        item.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (item.getReference() != null &&
                        item.getReference().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (item.getPrefer() != null &&
                        item.getPrefer().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (String.valueOf(item.getPax()).contains(lowerCaseFilter)) {
                    return true;
                }

                if (item.getDate() != null &&
                        item.getDate().toString().contains(lowerCaseFilter)) {
                    return true;
                }

                // No match
                return false;
            });
        });

        TableNoCRT.setCellFactory(col -> new TableCell<Reservation, String>() {
            private final Label selectLabel = new Label("Select");
            private static final String SELECT_DEFAULT_STYLE = "-fx-text-fill: #39D353; -fx-font-weight: bold;";
            private static final String SELECT_HOVER_STYLE = "-fx-text-fill: #5EEA78; -fx-font-weight: bold;";

            {
                selectLabel.getStyleClass().add("table-number-btn");
                selectLabel.setStyle(SELECT_DEFAULT_STYLE);
                selectLabel.setCursor(Cursor.HAND);
                selectLabel.setOnMouseEntered(e -> selectLabel.setStyle(SELECT_HOVER_STYLE));
                selectLabel.setOnMouseExited(e -> selectLabel.setStyle(SELECT_DEFAULT_STYLE));

                selectLabel.setOnMouseClicked(e -> {
                    int index = getIndex(); // get the row index
                    Reservation row = getTableView().getItems().get(index);
                    if (row != null) {
                        selectedReservation = row;

                        // Select the row in the table (highlights it)
                        getTableView().getSelectionModel().clearAndSelect(index);
                        getTableView().getFocusModel().focus(index);

                        currentpax = row.getPax();
                        hideTableList(row.getId());
                        loadAvailableTable();

                        System.out.println("Selected Reservation: " + row.getReference());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Reservation row = getTableView().getItems().get(getIndex());
                    if (row.getTable() != null) { // If table assigned, hide the button
                        setText(String.valueOf(row.getTable().getTableNo()));
                        setGraphic(null);
                    } else {
                        selectLabel.setStyle(SELECT_DEFAULT_STYLE);
                        setText(null);
                        setGraphic(selectLabel);
                    }
                }
            }
        });

        applyTimeFormat(TimeCRT);
        applyStatusStyle(StatusCRT);

        ReferenceCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.1));
        NameCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.15));
        PaxCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.06));
        StatusCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.1));
        PreferCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.1));
        PhoneCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.12));
        EmailCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.15));
        TimeCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.13));
        TableNoCRT.prefWidthProperty().bind(CustomerReservationTable.widthProperty().multiply(0.09));

        ReferenceCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getReference()));               // String
        NameCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCustomer() != null ? cd.getValue().getCustomer().getName() : ""));          // String
        PaxCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPax()));                          // Integer
        StatusCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getStatus()));                    // String
        PreferCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPrefer()));                    // String
        PhoneCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCustomer() != null ? cd.getValue().getCustomer().getPhone() : ""));        // String
        EmailCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCustomer() != null ? cd.getValue().getCustomer().getEmail() : ""));        // String
        TimeCRT.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getReservationPendingtime()));     // LocalTime
        TableNoCRT.setCellValueFactory(res ->
                new ReadOnlyObjectWrapper<>(res.getValue().getTable() != null ? res.getValue().getTable().getTableNo() : null)
        );

        for (TableColumn<?, ?> col : CustomerReservationTable.getColumns()) {
            col.setResizable(false);
            col.setReorderable(false);
        }


    }

    public void loadCustomerReservationTable() {
        List<String> statuses = List.of("Pending", "Confirmed");
        filteredReservationList.setPredicate(reservation -> {
            if (reservation == null || reservation.getStatus() == null) {
                return false;
            }
            return statuses.contains(reservation.getStatus());
        });

    }

    /// ////////////////AVAILABLE TABLES//////////////////////////////////////
    public void hideTableList(Long CustomerId) {

        currentRow = CustomerId;

        if (CustomerId == prevRow) {
            if (!hiddenTable.isVisible() && !hiddenTable.isManaged()) {
                hiddenTable.setVisible(true);
                hiddenTable.setManaged(true);

            } else {
                hiddenTable.setVisible(false);
                hiddenTable.setManaged(false);
            }

        } else if (CustomerId == null) {
            hiddenTable.setVisible(false);
            hiddenTable.setManaged(false);
        } else {
            hiddenTable.setVisible(true);
            hiddenTable.setManaged(true);
        }
        prevRow = CustomerId;
        if (selectedReservation == null) {
            CusToTable.setText("No Reservation Selected");

        } else if (selectedReservation == null && selectedTable == null) {
            CusToTable.setText("No Reservation and Table Selected Yet");

        } else {
            CusToTable.setText(selectedReservation.getReference());
        }
    }
    public void setupAvailableTable() {

        filteredtable = new FilteredList<>(availableTables);
        filteredtableSorted = new SortedList<>(filteredtable);
        filteredtableSorted.comparatorProperty().bind(AvailableTable.comparatorProperty());
        AvailableTable.setItems(filteredtableSorted);
        applyStatusStyle(StatusAT);

        filteredtable.setPredicate(table -> table.getCapacity() >= currentpax);
        AvailableTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedTable = newSelection;
                    System.out.println("Selected Table: " + newSelection);
                    Mergebtn.setDisable(newSelection == null); // update button state
                }
        );

        // Initially disable merge button
        Mergebtn.setDisable(true);

        TableColumn<?, ?>[] column = {TableNoAT, CapacityAT, StatusAT, LocationAT};
        double[] widthFactors = {0.25, 0.25, 0.25, 0.25};
        String[] namecol = {"tableNo", "capacity", "status", "location"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];

            if (col == null) {
                System.out.println("❌ NULL COLUMN at index " + i
                        + " (expected: " + namecol[i] + ")");
                continue; // skip this iteration so it won't throw NPE
            } else {
                System.out.println("✔ Column OK: index " + i
                        + " = " + col.getText());
            }

            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(AvailableTable.widthProperty().multiply(widthFactors[i]));
            col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
        }
        AvailableTable.setPlaceholder(new Label("No Available Table yet"));

    }

    public void loadAvailableTable() {
        Task<List<ManageTables>> task = new Task<List<ManageTables>>() {
            @Override
            protected List<ManageTables> call() throws Exception {
                System.out.println("[loadAvailableTable] Loading available tables...");
                List<ManageTables> tables = TablesService.findByStatus("Available");
                System.out.println("[loadAvailableTable] Found " + tables.size() + " available tables");
                for (ManageTables t : tables) {
                    System.out.println("[loadAvailableTable] Table: " + t.getTableNo() + " - " + t.getStatus());
                }
                return tables;
            }
        };
        task.setOnSucceeded(e ->{
            System.out.println("[loadAvailableTable] Setting " + task.getValue().size() + " tables to observable list");
            availableTables.setAll(task.getValue());
        });
        task.setOnFailed(e ->{
            task.getException().printStackTrace();
        });
        new Thread(task, "load-available-tables").start();
    }
    /// /////////////////////////SCNR RESERVATIONS///////////////////////////////////////////
    private void setupSCNReservation(){
        filteredSCNRListSorted = new SortedList<>(filteredSCNRList);
        filteredSCNRListSorted.comparatorProperty().bind(SCNReservations.comparatorProperty());
        SCNReservations.setItems(filteredSCNRListSorted);

        SCNReservations.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar hBar = (ScrollBar) SCNReservations.lookup(".scroll-bar:horizontal");
                if (hBar != null) {
                    hBar.setVisible(false);
                    hBar.setManaged(false); // removes layout space
                }
            }
        });
        applyStatusStyle(statusSCNR);
        applyTimeFormat(seatedSCNR);
        applyTimeFormat(cancelSCNR);
        applyTimeFormat(noshowSCNR);
        applyTimeFormat(regSCNR);

        TableColumn<?, ?>[] column = {refSCNR, customerSCNR, paxSCNR, phoneSCNR, statusSCNR, regSCNR, seatedSCNR, cancelSCNR, noshowSCNR,dateSCNR};
        double[] widthFactors = {0.1, 0.15, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,0.1};


        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];

            if (col == null) {
                System.out.println("❌ NULL COLUMN at index " + i
                        + " (expected: " +")");
                continue; // skip this iteration so it won't throw NPE
            } else {
                System.out.println("✔ Column OK: index " + i
                        + " = " + col.getText());
            }

            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(SCNReservations.widthProperty().multiply(widthFactors[i]));

        }
        refSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReference()));

        customerSCNR.setCellValueFactory(res -> {
            Customer c = res.getValue().getCustomer();
            return new ReadOnlyObjectWrapper<>(c != null ? c.getName() : "");
        });

        paxSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getPax()));

        phoneSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getCustomer().getPhone()));


        statusSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getStatus()));

        regSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationPendingtime()));

        seatedSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationSeatedtime()));

        cancelSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationCancelledtime()));

        noshowSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getReservationNoshowtime()));

        dateSCNR.setCellValueFactory(res -> new ReadOnlyObjectWrapper<>(res.getValue().getDate()));

    }

    private void loadSCNReservation(){
        List<String> statuses = List.of("Seated", "Cancelled","No Show");
        LocalDate today = LocalDate.now();
        filteredSCNRList.setPredicate(reservation -> {
            if (reservation == null || reservation.getStatus() == null || reservation.getDate() == null) {
                return false;
            }
            return statuses.contains(reservation.getStatus()) && today.equals(reservation.getDate());
        });
    }


    /// ////////////////////////RESERVATION LOGS////////////////////////////
    private void setupReservationLogs(){
        complete.setText(String.valueOf(reservationlogsdata.size()));
        ReservationLogs.setItems(reservationlogsdata);
        applyStatusStyle(statusRL);
        applyTimeFormat(pendingRL);
        applyTimeFormat(confirmRL);
        applyTimeFormat(seatedRL);
        applyTimeFormat(completeRL);

        TableColumn<?, ?>[] column = {customerRL,paxRL,phoneRL,preferRL,statusRL,pendingRL,confirmRL,seatedRL,completeRL,tablenoRL,refRL,dateRL};
        double[] widthFactors = {0.11, 0.05, 0.1, 0.1, 0.1, 0.08, 0.08, 0.08,0.08,0.05,0.08,0.09};
        String[] namecol = {"customer", "pax", "phone", "prefer", "status", "reservationPendingtime", "reservationConfirmtime","reservationSeatedtime","reservationCompletetime","tableNo","reference","date"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];

            if (col == null) {
                System.out.println("❌ NULL COLUMN at index " + i + " (expected: " + namecol[i] + ")");
                continue;
            } else {
                System.out.println("✔ Column OK: index " + i + " = " + col.getText());
            }

            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(ReservationLogs.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }
        ReservationLogs.setPlaceholder(new Label("No Complete Reservation yet "));
    }


    public void loadReservationLogs() {
       Task<List<ReservationTableLogs>> task = new Task<List<ReservationTableLogs>>() {
            @Override
            protected List<ReservationTableLogs> call() throws Exception {
                return ReportsService.findByDateBetween(applyDate, LocalDate.now());
            }
        };
        task.setOnSucceeded(e->{
             AvailableTable.getSelectionModel().select(0);
             AvailableTable.getFocusModel().focus(0);
             reservationlogsdata.setAll(task.getValue());
             complete.setText(String.valueOf(task.getValue().size()));
       });
       task.setOnFailed(e->{
           task.getException().printStackTrace();
       });
       new Thread(task).start();
    }
/// ////////////////////////////BUTTONS////////////////////////////////////
    @FXML
    private void addCustomerReservation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/addReservation.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(App.primaryStage);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.setResizable(false);
            Scene scn = new Scene(root);
            scn.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scn);


            addReservationController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5. Refresh UI
        loadReservationsData();
        adminUIController.getDashboardController().loadRecentReservations();
        adminUIController.getDashboardController().barchart();
    }
    public void merge(ActionEvent event){
        System.out.println("[merge] Method called!");
        releaseMergeFocus();
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(20,20);
        Mergebtn.setGraphic(progress);
        Mergebtn.setText(null);
        Mergebtn.setMouseTransparent(true);
        Mergebtn.setFocusTraversable(false);

        System.out.println("[merge] currentRow: " + currentRow);
        System.out.println("[merge] selectedTable: " + selectedTable);
        
        if (selectedTable == null) {
            System.out.println("[merge] ERROR: selectedTable is null!");
            Mergebtn.setGraphic(null);
            Mergebtn.setText("Confirm");
            Mergebtn.setMouseTransparent(false);
            return;
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Reservation cr = ReservationService.findById(currentRow);
                System.out.println("[merge] Reservation found: " + (cr != null ? cr.getReference() : "null"));
                
                String currentStatus = selectedTable.getStatus();
                System.out.println("[merge] Current table status: " + currentStatus);

                if (cr != null && selectedTable != null) {
                    System.out.println("[merge] Setting table " + selectedTable.getTableNo() + " on reservation");
                    cr.setTable(selectedTable);
                    // Keep the original reservation status (Pending/Confirmed)
                    selectedTable.setStatus("Reserved");
                    selectedTable.setTablestarttime(LocalTime.now());

                    System.out.println("[merge] Saving table...");
                    TablesService.saveTable(selectedTable);

                    System.out.println("[merge] Saving reservation...");
                    ReservationService.saveReservation(cr);
                    
                    User currentuser = adminUIController.getCurrentUser();
                    if (currentuser != null) {
                        activityLogService.logAction(currentuser.getUsername(),String.valueOf(currentuser.getPosition()),"Table","Reserve Table",String.format("Reserved table %s for %s (Status: %s)", selectedTable.getTableNo(),cr.getReference(),cr.getStatus()));
                    }
                }

                return null;
            }
        };
        task.setOnSucceeded(e->{
            adminUIController.getDashboardController().loadTableView();
            loadAvailableTable();
            adminUIController.getTableController().loadTableManager();
            loadReservationsData();

            Mergebtn.setGraphic(null);
            Mergebtn.setText("Confirm");
            Mergebtn.setMouseTransparent(false);
            AvailableTable.getSelectionModel().clearSelection();
            AvailableTable.getFocusModel().focus(-1);

            hiddenTable.setVisible(false);
            hiddenTable.setManaged(false);
            adminUIController.getDashboardController().updateLabels();
            releaseMergeFocus();
        });
        task.setOnFailed(e->{
            Mergebtn.setGraphic(null);
            Mergebtn.setText("Confirm");
            Mergebtn.setMouseTransparent(false);
            releaseMergeFocus();
            task.getException().printStackTrace();
        });
        new Thread(task).start();

    }

    private void releaseMergeFocus() {
        Platform.runLater(() -> {
            ReservationPane.requestFocus();
        });
    }

    @FXML
    private void sendMessage() {
        if (selectedReservation == null) return;

        // If still in cooldown, ignore
        if (NotificationUtil.isInCooldown(selectedReservation, 120)) return;

        sendsms.setDisable(true);
        sendsms.setText("Sending Message...");
        response.setText(null);
        response.setGraphic(new javafx.scene.control.ProgressIndicator());

        WebSocketClient webSocketClient = adminUIController.getWsClient();

        WebUpdateDTO dto = new WebUpdateDTO();
        dto.setCode("TABLE_READY");
        dto.setMessage("Open Confirmation Modal");
        dto.setPhone(selectedReservation.getCustomer().getPhone());
        dto.setReference(selectedReservation.getReference());

        Task<Void> sendSmsTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    webSocketClient.sendDTO(dto);

                    String phoneNo = selectedReservation.getCustomer().getPhone();
                    String details = resolveNotifyCustomerMessage();

                    messageDispatchService.send(phoneNo, details);

                    selectedReservation.setReservationNotifiedtime(java.time.LocalTime.now());
                    ReservationService.saveReservation(selectedReservation);

                } catch (Exception e) {
                    throw new RuntimeException("Message send failed", e);
                }
                return null;
            }
        };

        sendSmsTask.setOnSucceeded(evt -> {
            response.setGraphic(null);
            sendResponse(true, "Message sent successfully!");
            startButtonCountdown(selectedReservation);
            NotificationUtil.markNotified(selectedReservation);


        });

        sendSmsTask.setOnFailed(evt -> {
            response.setGraphic(null);
            sendsms.setDisable(false);
            sendsms.setText("Notify Customer");
            sendResponse(false, "Failed to send message");
            sendSmsTask.getException().printStackTrace();
        });

        new Thread(sendSmsTask).start();
    }

    private String resolveNotifyCustomerMessage() {
        String configuredLabel = AppSettings.loadMessageLabel(MESSAGE_NOTIFY_CUSTOMER_KEY);
        if (configuredLabel != null && !configuredLabel.isBlank()) {
            return messageService.findByLabel(configuredLabel)
                    .map(Message::getMessageDetails)
                    .filter(text -> text != null && !text.isBlank())
                    .orElseGet(this::buildDefaultNotifyCustomerMessage);
        }
        return buildDefaultNotifyCustomerMessage();
    }

    private String buildDefaultNotifyCustomerMessage() {
        return "Hello " + selectedReservation.getCustomer().getName() + ", your table is ready.\n"
                + "Please proceed to the reception for seating. Thank you.\n";
    }

    private void startButtonCountdown(Reservation reservation) {
        Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            if (selectedReservation == null) return;

            int remaining = NotificationUtil.getRemainingCooldown(selectedReservation, 120);
            if (remaining > 0) {
                sendsms.setDisable(true);
                sendsms.setText("Wait " + remaining + "s");
            } else {
                sendsms.setDisable(false);
                sendsms.setText("Notify Customer");
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }





    @FXML
    private void editCustomerReservation(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/editReservation.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(App.primaryStage); // mainStage is your primary stage
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.setResizable(false);
            Scene scn = new Scene(root);
            scn.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scn);

            // Link controller with dialog stage
            editReservationController controller = loader.getController();
            controller.setTargetReservation(selectedReservation);
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait(); // wait until closed

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5. Refresh UI
        loadReservationsData();
        adminUIController.getDashboardController().loadRecentReservations();
        adminUIController.getTableController().loadTableManager();
        //loadReservationReports();
        adminUIController.getDashboardController().barchart();

    }

    @FXML
    private void cancelCustomerReservation() {
        if (selectedReservation == null) {
            sendResponse(false, "Please select a reservation to cancel");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/deleteDialog.fxml"));
            Parent root = loader.load();
            DeleteDialogController controller = loader.getController();
            controller.setMessage("Are you sure you want to cancel this reservation?");

            controller.setOnDelete(() -> {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        ReservationService.updateStatus(selectedReservation.getReference(), "Cancelled");
                        return null;
                    }
                };

                task.setOnSucceeded(e -> {
                    User currentuser = adminUIController.getCurrentUser();
                    if (currentuser != null) {
                        activityLogService.logAction(
                                currentuser.getUsername(),
                                currentuser.getPosition().toString(),
                                "Reservation",
                                "Cancel Reservation",
                                "Cancelled reservation: " + selectedReservation.getReference()
                        );
                    }
                    sendResponse(true, "Reservation cancelled successfully");
                    loadReservationsData();
                    loadAvailableTable();
                    adminUIController.getTableController().loadTableManager();
                    adminUIController.getDashboardController().updateLabels();
                });

                task.setOnFailed(e -> {
                    task.getException().printStackTrace();
                    sendResponse(false, "Failed to cancel reservation");
                });

                new Thread(task).start();
            });

            Stage dialog = new Stage(StageStyle.UNDECORATED);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.setResizable(false);
            Scene scn = new Scene(root);
            scn.setFill(Color.TRANSPARENT);
            dialog.setScene(scn);
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(false, "Error opening cancel dialog");
        }
    }

    public void sendResponse(boolean successfully,String details){
        if(!successfully){
            response.getStyleClass().removeAll("login-success", "login-message");
            response.getStyleClass().add("login-error");
            response.setText(details);
        }
        else{
            response.getStyleClass().removeAll("login-error", "login-message-hidden");
            response.getStyleClass().add("login-success");
            response.setText(details);
        }
        LabelTransition.play(response);

    }

    private void applyPermissions() {
        User currentuser = adminUIController.getCurrentUser();
        if (currentuser == null) return;

        // Map each button to its required permission code
        Map<Button, String> buttonPermissions = Map.of(
                newcustomer,"CREATE_RESERVATION"
                );

        // Disable buttons if user doesn't have permission
        Task<Map<Button, Boolean>> permissionTask = new Task<>() {
            @Override
            protected Map<Button, Boolean> call() {
                Map<Button, Boolean> results = new HashMap<>();
                buttonPermissions.forEach((button, code) ->
                        results.put(button, permissionService.hasPermission(currentuser, code))
                );
                return results;
            }
        };

        permissionTask.setOnSucceeded(e -> {
            // Update buttons safely on FX thread
            permissionTask.getValue().forEach((button, allowed) -> button.setManaged(allowed));
        });

        permissionTask.setOnFailed(e -> {
            permissionTask.getException().printStackTrace();
        });

        new Thread(permissionTask, "permission-task").start();
    }



    @FXML
    private void initialize(){
        reservpane.minHeightProperty().bind(reservpane.widthProperty().multiply(1.456));
        hiddenTable.setVisible(false);
        hiddenTable.setManaged(false);
        sendsms.setDisable(selectedReservation == null);
        editreservation.setDisable(selectedReservation == null);
        cancelreservation.setDisable(selectedReservation == null);
        
        // Check permission for cancel reservation button
        boolean canCancel = permissionService.hasPermission(currentuser, "VIEW_CANCEL_RESERVATION");
        cancelreservation.setDisable(!canCancel || selectedReservation == null);

        // Setup UI first (fast operations)
        setupCustomerReservationTable();
        setupAvailableTable();
        setupSCNReservation();
        setupReservationLogs();
        sendsms.setFocusTraversable(false);
        editreservation.setFocusTraversable(false);
        cancelreservation.setFocusTraversable(false);
        Mergebtn.setFocusTraversable(false);
        
        // Load data in separate threads to prevent UI blocking
        new Thread(() -> loadReservationsData(), "load-reservations").start();
        new Thread(() -> loadAvailableTable(), "load-available-tables").start();
        new Thread(() -> loadCustomerReservationTable(), "load-customer-reservations").start();
        new Thread(() -> loadReservationLogs(), "load-reservation-logs").start();
        new Thread(() -> loadSCNReservation(), "load-scn-reservations").start();
        
        if (adminUIController != null) {
            applyPermissions();
        }
    }
}
