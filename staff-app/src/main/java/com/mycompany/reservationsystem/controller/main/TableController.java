package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.App;
import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.controller.popup.DeleteDialogController;
import com.mycompany.reservationsystem.controller.popup.addTableDialogController;
import com.mycompany.reservationsystem.controller.popup.editTableDialogController;
import com.mycompany.reservationsystem.controller.popup.setAmountPaidController;
import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.dto.TableStatsDTO;
import com.mycompany.reservationsystem.model.ManageTables;
import com.mycompany.reservationsystem.model.Reservation;
import com.mycompany.reservationsystem.model.ReservationTableLogs;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.service.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mycompany.reservationsystem.util.TableCellFactoryUtil.*;


public class TableController {

    // ====================== Constructor-injected dependencies ======================
    private AdministratorUIController adminUIController;

    // ====================== Other Dependencies ======================
    public User currentuser;
    private static ActivityLogService activityLogService = new ActivityLogService();
    private static PermissionService permissionService = new PermissionService();
    private static TablesService tablesService = new TablesService();
    private static ReservationService reservationService = new ReservationService();

// ====================== FXML Components ======================

    // --- TableViews ---
    @FXML
    private TableView<ManageTablesDTO> TableManager;
    @FXML
    private TableView<ReservationTableLogs> TableHistory;

    // --- TableColumns: ManageTablesDTO ---
    @FXML
    private TableColumn<ManageTablesDTO, Void> ActionTM;
    @FXML
    private TableColumn<ManageTablesDTO, Integer> CapacityTM, PaxTM;
    @FXML
    private TableColumn<ManageTablesDTO, String> CustomerTM, LocationTM, StatusTM, TablenoTM;
    @FXML
    private TableColumn<ManageTablesDTO, LocalTime> TimeUsedTM;

    // --- TableColumns: ReservationTableLogs ---
    @FXML
    private TableColumn<ReservationTableLogs, Integer> capacityTH, paxTH;
    @FXML
    private TableColumn<ReservationTableLogs, LocalTime> completeTH, occupiedTH, reservedTH;
    @FXML
    private TableColumn<ReservationTableLogs, String> customerTH, referenceTH, statusTH;
    @FXML
    private TableColumn<ReservationTableLogs, LocalDate> dateTH;
    @FXML
    private TableColumn<?, ?> tablenoTH;

    // --- Controls ---
    @FXML
    private MFXButton AddTablebtn;

    @FXML
    private MFXTextField SearchTM;
    @FXML
    private MFXComboBox tablefilter;

    // --- Layouts ---
    @FXML
    private BorderPane tablepane;
    @FXML
    private ScrollPane TableManagementPane;

    // --- Labels ---
    @FXML
    private Label totalbusy, totalfree, totaltables;

    // ====================== Local variables / ObservableLists ======================
    private final ObservableList<ManageTablesDTO> tableManagerData = FXCollections.observableArrayList();
    private final ObservableList<ReservationTableLogs> reservationlogsdata = FXCollections.observableArrayList();
    FilteredList<ManageTablesDTO> tablesfilteredData;
    SortedList<ManageTablesDTO> tablesSortedData;

    private LocalDate applyDate;

    public TableController() {
    }

    public TableController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }

    public void setAdminUIController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
        this.currentuser = adminUIController.getCurrentUser();
    }


    //////////////////////TABLE MANAGER////////////////////////////
    public void setupTableManager() {
        currentuser = adminUIController.getCurrentUser();
        AddTablebtn.setDisable(!permissionService.hasPermission(currentuser,"ADD_TABLE"));

        new Thread(new Task<>() {
            @Override
            protected Void call() {
                boolean add = permissionService.hasPermission(currentuser, "ADD_TABLE");
                Platform.runLater(() -> {
                    AddTablebtn.setDisable(!add);
                });
                return null;
            }
        }).start();

        System.out.println(currentuser);
        AddTablebtn.setOnAction(e -> showAddTableDialog());
        tablesfilteredData = new FilteredList<>(tableManagerData, p -> true);
        tablesSortedData = new SortedList<>(tablesfilteredData);
        tablesSortedData.comparatorProperty().bind(TableManager.comparatorProperty());
        TableManager.setItems(tablesSortedData);
        SearchTM.textProperty().addListener((observable, oldValue, newValue) -> {
            tablesfilteredData.setPredicate(item -> {
                // If search field is empty, display all
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                // Compare name with search text
                String lowerCaseFilter = newValue.toLowerCase();

                // Check all columns
                if (item.getCustomer() != null && item.getCustomer().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getPax() != null && String.valueOf(item.getPax()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getStatus() != null && item.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getTableNo() != null && item.getTableNo().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getTablestarttime() != null && String.valueOf(item.getTablestarttime()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getCapacity() > 0 && String.valueOf(item.getCapacity()).contains(lowerCaseFilter)) {
                    return true;
                }
                if (item.getTableId() != null && String.valueOf(item.getTableId()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }



                return false; // no match
            });
        });
        String[] statuses = {"Available","Reserved","Occupied","Show All"};
        addItemsToCombo(tablefilter,tablesfilteredData,ManageTablesDTO::getStatus,statuses);
        applyStatusStyle(StatusTM);
        applyTimeFormat(TimeUsedTM);

        // --- Action Column with Buttons ---
        ActionTM.setCellFactory(col -> new TableCell<ManageTablesDTO, Void>() {
            FontIcon utensilIcon = new FontIcon(FontAwesomeSolid.UTENSILS);
            FontIcon editIcon = new FontIcon(FontAwesomeSolid.PEN_SQUARE);
            FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TRASH);
            FontIcon completeIcon = new FontIcon(FontAwesomeSolid.RECEIPT);
            FontIcon removeIcon = new FontIcon(FontAwesomeSolid.USER_SLASH);

            private final Button btnStart = new Button("Start Service");
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final Button btnComplete = new Button("Complete");
            private final Button btnRemoveCustomer = new Button("Remove Customer");
            private final HBox hbox = new HBox(5);

            {
                // Initialize icons and buttons (same as before)
                initIconsAndButtons();
                new Thread(new Task<>() {
                    @Override
                    protected Void call() {
                        boolean edit = permissionService.hasPermission(currentuser, "EDIT_ACCOUNT");
                        boolean delete = permissionService.hasPermission(currentuser, "REMOVE_ACCOUNT");
                        Platform.runLater(() -> {
                            btnEdit.setDisable(!edit);
                            btnDelete.setDisable(!delete);
                        });
                        return null;
                    }
                }).start();

                btnStart.setOnAction(e -> handleStart());
                btnComplete.setOnAction(e -> handleComplete());
                btnEdit.setOnAction(e -> {
                    ManageTablesDTO data = getTableView().getItems().get(getIndex());
                    handleEdit(data);
                });
                btnRemoveCustomer.setOnAction(e -> handleRemoveCustomer());
                btnDelete.setOnAction(e -> handleDelete());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                ManageTablesDTO data = getCurrentItem();
                if (data != null) {
                    updateButtonsForStatus(data);
                    setGraphic(hbox);
                }
            }
            // ----------------update Buttons For Status---------------------------------
            private void resetButtonStates() {
                btnComplete.setGraphic(completeIcon);
                btnComplete.setText("Complete");
                btnComplete.setMouseTransparent(true);
                btnComplete.setFocusTraversable(false);
                btnEdit.setDisable(false);
                btnDelete.setDisable(false);
            }
            
            // ----------------update Buttons For Status---------------------------------
            private void updateButtonsForStatus(ManageTablesDTO data) {
                // Reset all button states first
                btnStart.setDisable(false);
                btnStart.setMouseTransparent(false);
                btnComplete.setDisable(false);
                btnComplete.setMouseTransparent(false);
                btnEdit.setDisable(false);
                btnDelete.setDisable(false);
                
                boolean hasCustomer = data.getCustomer() != null && !data.getCustomer().isEmpty();
                
                if ("Reserved".equals(data.getStatus())) {
                    hbox.getChildren().setAll(btnStart, btnRemoveCustomer);
                }
                else if("Occupied".equals(data.getStatus())){
                    if (hasCustomer) {
                        hbox.getChildren().setAll(btnComplete, btnRemoveCustomer);
                    } else {
                        hbox.getChildren().setAll(btnComplete, btnEdit, btnDelete);
                    }
                }else {
                    hbox.getChildren().setAll(btnEdit, btnDelete);
                }
            }
            // ---------------- init Icons and Buttons ---------------
            private void initIconsAndButtons() {
                // Icon setup
                utensilIcon.setIconSize(12);
                utensilIcon.setIconColor(Color.web("#4A2A33"));
                editIcon.setIconSize(12);
                editIcon.setIconColor(Color.web("#000000"));
                deleteIcon.setIconSize(12);
                deleteIcon.setIconColor(Color.web("#ffffff"));
                completeIcon.setIconSize(12);
                completeIcon.setIconColor(Color.web("#ffffff"));
                removeIcon.setIconSize(12);
                removeIcon.setIconColor(Color.web("#ffffff"));

                // Button setup
                btnStart.setGraphic(utensilIcon);
                btnStart.setContentDisplay(ContentDisplay.LEFT);
                btnStart.getStyleClass().add("start-service");

                btnEdit.setGraphic(editIcon);
                btnEdit.setContentDisplay(ContentDisplay.LEFT);
                btnEdit.getStyleClass().add("edit");

                btnDelete.setGraphic(deleteIcon);
                btnDelete.setContentDisplay(ContentDisplay.LEFT);
                btnDelete.getStyleClass().add("delete");

                btnComplete.setGraphic(completeIcon);
                btnComplete.setContentDisplay(ContentDisplay.LEFT);
                btnComplete.getStyleClass().add("complete");

                btnRemoveCustomer.setGraphic(removeIcon);
                btnRemoveCustomer.setContentDisplay(ContentDisplay.LEFT);
                btnRemoveCustomer.getStyleClass().add("delete");

                // HBox setup
                hbox.setAlignment(Pos.CENTER);
                btnStart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnEdit.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnDelete.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnComplete.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnRemoveCustomer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                HBox.setHgrow(btnStart, Priority.ALWAYS);
                HBox.setHgrow(btnEdit, Priority.ALWAYS);
                HBox.setHgrow(btnDelete, Priority.ALWAYS);
                HBox.setHgrow(btnComplete, Priority.ALWAYS);
                HBox.setHgrow(btnRemoveCustomer, Priority.ALWAYS);

            }


            // ---------------- Private helper methods ----------------

            private void handleStart() {

                ProgressIndicator progress = new ProgressIndicator();
                progress.setPrefSize(15,15);
                btnStart.setMouseTransparent(true);
                btnStart.setFocusTraversable(false);
                btnStart.setText("");
                btnStart.setGraphic(progress);

                btnEdit.setDisable(true);
                btnDelete.setDisable(true);


                Task<ManageTablesDTO> task = new Task<>() {
                    @Override
                    protected ManageTablesDTO call() {

                        ManageTablesDTO data = getCurrentItem();
                        if (data == null) {
                            throw new IllegalArgumentException("The selected message is currently in use.");
                        }

                        String currentStatus = data.getStatus();

                        // BACKGROUND THREAD (DB + logic only)
                        LocalTime now = LocalTime.now();
                        tablesService.updateStatusWithStartTime(data.getTableId(), "Occupied", now);
                        reservationService.updateSeatedtime(data.getReference(), now);
                        reservationService.updateStatus(data.getReference(), "Seated");

                        activityLogService.logAction(
                                currentuser.getUsername(),
                                currentuser.getPosition().toString(),
                                "Table",
                                "Update Status",
                                String.format(
                                        "Changed table %d status from %s to Occupied for reservation %s",
                                        data.getTableId(),
                                        currentStatus,
                                        data.getReference()
                                )
                        );

                        // Update DTO (safe – not UI)
                        data.setStatus("Occupied");
                        data.setReservationSeatedtime(now);

                        return data;
                    }
                };

                task.setOnSucceeded(e -> {
                    ManageTablesDTO data = task.getValue();
                    updateButtonsForStatus(data);

                    int loadCount = 5;
                    CountDownLatch latch = new CountDownLatch(loadCount);

                    Task<Void> loadTask1 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getDashboardController().loadTableView(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask2 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getDashboardController().updateLabels(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask3 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getReservationController().loadReservationsData(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask4 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getReservationController().loadAvailableTable(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask5 = new Task<Void>() {
                        @Override protected Void call() { loadTableManager(); latch.countDown(); return null; }
                    };

                    new Thread(loadTask1).start();
                    new Thread(loadTask2).start();
                    new Thread(loadTask3).start();
                    new Thread(loadTask4).start();
                    new Thread(loadTask5).start();

                    Task<Void> waitTask = new Task<Void>() {
                        @Override protected Void call() throws Exception {
                            latch.await();
                            return null;
                        }
                    };
                    waitTask.setOnSucceeded(ev -> {
                        btnStart.setGraphic(utensilIcon);
                        btnStart.setText("Start Service");
                        btnStart.setMouseTransparent(false);
                        btnStart.setFocusTraversable(true);
                        resetButtonStates();
                    });
                    new Thread(waitTask).start();
                });

                task.setOnFailed(e -> {
                    btnStart.setGraphic(utensilIcon);
                    btnStart.setText("Start Service");
                    btnStart.setDisable(false);
                    btnStart.setMouseTransparent(false);
                    resetButtonStates();

                    Throwable ex = task.getException();
                    ex.printStackTrace(); // log properly
                });

                new Thread(task).start();
            }


            private void handleComplete() {

                ManageTablesDTO data = getCurrentItem();
                if (data == null) return;

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/setAmountPaid.fxml"));

                Parent root;
                try {
                    root = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.initOwner(App.primaryStage);
                dialogStage.initStyle(StageStyle.TRANSPARENT);
                dialogStage.setResizable(false);

                Scene scn = new Scene(root);
                scn.setFill(Color.TRANSPARENT);
                dialogStage.setScene(scn);

                setAmountPaidController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setReference(data.getReference());

                dialogStage.showAndWait();

                if (controller.isCancelled()) return;

                BigDecimal amount = controller.getAmount();

                runCompleteTask(data, amount);
            }
            private void runCompleteTask(ManageTablesDTO data, BigDecimal amount) {

                ProgressIndicator progress = new ProgressIndicator();
                progress.setPrefSize(15, 15);
                btnComplete.setMouseTransparent(true);
                btnComplete.setFocusTraversable(false);
                btnComplete.setText(null);
                btnComplete.setGraphic(progress);
                btnEdit.setDisable(true);
                btnDelete.setDisable(true);

                Task<ManageTablesDTO> task = new Task<>() {
                    @Override
                    protected ManageTablesDTO call() {

                        data.setStatus("Complete");
                        data.setDate(LocalDate.now());
                        data.setReservationCompletetime(LocalTime.now());
                        data.setSales(amount);

                        String ref = data.getReference();
                        System.out.println("[Complete] Reference: " + ref);
                        System.out.println("[Complete] Current data phone before fetch: " + data.getPhone());
                        
                        Map<String, Object> resData = ApiClient.getReservationByReference(ref);
                        System.out.println("[Complete] Reservation data: " + resData);
                        
                        if (resData != null && !resData.isEmpty() && !resData.containsKey("error")) {
                            Object custObj = resData.get("customer");
                            System.out.println("[Complete] Customer object: " + custObj);
                            if (custObj instanceof Map) {
                                Map<String, Object> customer = (Map<String, Object>) custObj;
                                String phone = (String) customer.get("phone");
                                String email = (String) customer.get("email");
                                System.out.println("[Complete] Extracted phone: " + phone + ", email: " + email);
                                data.setPhone(phone != null ? phone : data.getPhone());
                                data.setEmail(email);
                            }
                            Object pendObj = resData.get("reservationPendingtime");
                            if (pendObj instanceof String) {
                                data.setReservationPendingtime(LocalTime.parse((String) pendObj));
                            }
                            Object seatedObj = resData.get("reservationSeatedtime");
                            if (seatedObj instanceof String) {
                                data.setReservationSeatedtime(LocalTime.parse((String) seatedObj));
                            }
                        }
                        
                        System.out.println("[Complete] Data phone after fetch: " + data.getPhone());

                        ReservationTableLogs log = new ReservationTableLogs(data);
                        System.out.println("[Complete] Log phone: " + log.getPhone());
                        ReportsService.saveLog(log);

                        reservationService.updateCompletetime(ref, LocalTime.now());
                        reservationService.updateStatus(ref, "Complete");

                        activityLogService.logAction(
                                currentuser.getUsername(),
                                currentuser.getPosition().toString(),
                                "Table",
                                "Update Status",
                                String.format(
                                        "Changed table %s status to Complete → Available",
                                        data.getTableNo()
                                )
                        );

                        tablesService.clearStartTime(data.getTableId());

                        return data;
                    }
                };

                task.setOnSucceeded(e -> {
                    updateButtonsForStatus(data);

                    int loadCount = 7;
                    CountDownLatch latch = new CountDownLatch(loadCount);

                    Task<Void> loadTask1 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getDashboardController().loadTableView(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask2 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getDashboardController().updateLabels(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask3 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getReservationController().loadReservationsData(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask4 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getReservationController().loadReservationLogs(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask5 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getReservationController().loadAvailableTable(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask6 = new Task<Void>() {
                        @Override protected Void call() { loadTableManager(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask7 = new Task<Void>() {
                        @Override protected Void call() { loadTableHistory(); latch.countDown(); return null; }
                    };

                    new Thread(loadTask1).start();
                    new Thread(loadTask2).start();
                    new Thread(loadTask3).start();
                    new Thread(loadTask4).start();
                    new Thread(loadTask5).start();
                    new Thread(loadTask6).start();
                    new Thread(loadTask7).start();

                    Task<Void> waitTask = new Task<Void>() {
                        @Override protected Void call() throws Exception {
                            latch.await();
                            return null;
                        }
                    };
                    waitTask.setOnSucceeded(ev -> {
                        resetButtonStates();
                    });
                    new Thread(waitTask).start();
                });

                task.setOnFailed(e -> {
                    task.getException().printStackTrace();

                    resetButtonStates();
                });

                new Thread(task).start();
            }


            private void handleEdit(ManageTablesDTO dto) {
                System.out.println("[handleEdit] Called with dto: " + dto);
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/editTableDialog.fxml"));
                    System.out.println("[handleEdit] FXMLLoader created");
                    Parent root = loader.load();
                    System.out.println("[handleEdit] FXML loaded, dto tableNo: " + dto.getTableNo() + ", id: " + dto.getId());

                    Stage dialogStage = new Stage();
                    dialogStage.initModality(Modality.APPLICATION_MODAL);
                    dialogStage.initOwner(App.primaryStage);
                    dialogStage.initStyle(StageStyle.TRANSPARENT);
                    dialogStage.setResizable(false);
                    Scene scn = new Scene(root);
                    scn.setFill(Color.TRANSPARENT);
                    dialogStage.setScene(scn);

                    editTableDialogController controller = loader.getController();
                    controller.setDialogStage(dialogStage);
                    
                    ManageTables table = new ManageTables();
                    table.setId(dto.getTableId());
                    table.setTableNo(dto.getTableNo());
                    table.setCapacity(dto.getCapacity());
                    table.setStatus(dto.getStatus());
                    table.setLocation(dto.getLocation());
                    
                    controller.setTargetTable(table);
                    dialogStage.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adminUIController.getDashboardController().loadTableView();
                adminUIController.getDashboardController().updateLabels();
                loadTableManager();
                adminUIController.getReservationController().loadAvailableTable();
            }


            private void handleDelete() {
                ManageTablesDTO data = getCurrentItem();
                if (data == null) {
                    System.out.println("[handleDelete] data is null");
                    return;
                }
                
                System.out.println("[handleDelete] TableId: " + data.getTableId() + ", TableNo: " + data.getTableNo());

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/deleteDialog.fxml"));
                    Parent root = loader.load();
                    DeleteDialogController controller = loader.getController();

                    controller.setOnDelete(() -> {
                        System.out.println("[handleDelete] onDelete called");
                        String status = data.getStatus();
                        System.out.println("[handleDelete] Table status: " + status);
                        System.out.println("[handleDelete] Calling TablesService.deleteById with tableId: " + data.getTableId());
                        TablesService.deleteById(data.getTableId());
                        System.out.println("[handleDelete] Delete completed, reloading...");
                        activityLogService.logAction(
                                currentuser.getUsername(),
                                currentuser.getPosition().toString(),
                                "Table",
                                "Delete Table",
                                String.format("Delete table %s", data.getTableNo())
                        );
                        loadTableManager();
                        adminUIController.getDashboardController().loadTableView();
                        adminUIController.getDashboardController().updateLabels();
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
                }

                //loadReports();
            }

            private void handleRemoveCustomer() {
                ManageTablesDTO data = getCurrentItem();
                if (data == null) {
                    System.out.println("[handleRemoveCustomer] data is null");
                    return;
                }
                
                System.out.println("[handleRemoveCustomer] TableId: " + data.getTableId() + ", TableNo: " + data.getTableNo());

                ProgressIndicator progress = new ProgressIndicator();
                progress.setPrefSize(15, 15);
                btnRemoveCustomer.setMouseTransparent(true);
                btnRemoveCustomer.setFocusTraversable(false);
                btnRemoveCustomer.setText(null);
                btnRemoveCustomer.setGraphic(progress);
                btnEdit.setDisable(true);
                btnDelete.setDisable(true);

                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        TablesService.removeCustomerFromTable(data.getTableId());
                        return null;
                    }
                };

                task.setOnSucceeded(e -> {
                    activityLogService.logAction(
                            currentuser.getUsername(),
                            currentuser.getPosition().toString(),
                            "Table",
                            "Remove Customer",
                            String.format("Removed customer from table %s", data.getTableNo())
                    );

                    int loadCount = 6;
                    CountDownLatch latch = new CountDownLatch(loadCount);

                    Task<Void> loadTask1 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getDashboardController().loadTableView(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask2 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getDashboardController().updateLabels(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask3 = new Task<Void>() {
                        @Override protected Void call() { loadTableManager(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask4 = new Task<Void>() {
                        @Override protected Void call() { loadTableHistory(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask5 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getReservationController().loadAvailableTable(); latch.countDown(); return null; }
                    };
                    Task<Void> loadTask6 = new Task<Void>() {
                        @Override protected Void call() { adminUIController.getReservationController().loadReservationsData(); latch.countDown(); return null; }
                    };

                    new Thread(loadTask1).start();
                    new Thread(loadTask2).start();
                    new Thread(loadTask3).start();
                    new Thread(loadTask4).start();
                    new Thread(loadTask5).start();
                    new Thread(loadTask6).start();

                    Task<Void> waitTask = new Task<Void>() {
                        @Override protected Void call() throws Exception {
                            latch.await();
                            return null;
                        }
                    };
                    waitTask.setOnSucceeded(ev -> {
                        Platform.runLater(() -> {
                            updateButtonsForStatus(data);
                            btnRemoveCustomer.setGraphic(removeIcon);
                            btnRemoveCustomer.setText("Remove Customer");
                            btnRemoveCustomer.setMouseTransparent(false);
                            btnRemoveCustomer.setFocusTraversable(true);
                            resetButtonStates();
                        });
                    });
                    new Thread(waitTask).start();
                });

                task.setOnFailed(e -> {
                    task.getException().printStackTrace();
                    
                    Platform.runLater(() -> {
                        btnRemoveCustomer.setGraphic(removeIcon);
                        btnRemoveCustomer.setText("Remove Customer");
                        btnRemoveCustomer.setMouseTransparent(false);
                        resetButtonStates();
                    });
                });

                new Thread(task).start();
            }

            // ---------------- Utility methods ----------------

            private ManageTablesDTO getCurrentItem() {
                int i = getIndex();
                if (i >= 0 && i < getTableView().getItems().size()) {
                    return getTableView().getItems().get(i);
                }
                return null;
            }
        });

        // --- Setup Other Columns ---
        TableColumn<?, ?>[] column = {TablenoTM, CustomerTM, PaxTM, StatusTM, CapacityTM, LocationTM, TimeUsedTM, ActionTM};
        double[] widthFactors = {0.08, 0.18, 0.06, 0.11, 0.08, 0.12, 0.10, 0.27};
        String[] namecol = {"tableNo", "customer", "pax", "status", "capacity", "location", "tablestarttime", ""};

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
            col.prefWidthProperty().bind(TableManager.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }
        TableManager.setPlaceholder(new Label("No Table set yet"));

    }


    public void loadTableManager(){
        Task<TableStatsDTO> task = new Task<TableStatsDTO>() {
            @Override
            protected TableStatsDTO call() throws Exception {
                TableStatsDTO stats = new TableStatsDTO();

                stats.setTables(TablesService.getManageTablesDTO().size());
                stats.setTotal((int) TablesService.count());
                stats.setFree((int) TablesService.countByStatus("Available"));
                stats.setBusy((int) (TablesService.countByStatus("Reserved") + TablesService.countByStatus("Occupied")));
                stats.setTableId(1L);
                return stats;

            }
        };
        task.setOnSucceeded(e->{
            TableStatsDTO stats = task.getValue();
            tableManagerData.setAll(TablesService.getManageTablesDTO());
            totaltables.setText(String.valueOf(stats.getTotal()));
            totalfree.setText(String.valueOf(stats.getFree()));
            totalbusy.setText(String.valueOf(stats.getBusy()));
        });
        task.setOnFailed(e->{
            task.getException().printStackTrace();
        });
        new Thread(task).start();
    }

    ///////////////////////////////////////TABLE HISTORY/////////////////////////////////////
    private void setupTableHistory(){
        TableHistory.setItems(reservationlogsdata);
        applyTimeFormat(reservedTH);
        applyTimeFormat(occupiedTH);
        applyTimeFormat(completeTH);
        
        TableColumn<?, ?>[] column = {referenceTH,customerTH,paxTH,statusTH,tablenoTH,capacityTH,reservedTH,occupiedTH,completeTH,dateTH};
        double[] widthFactors = {0.11, 0.2, 0.05, 0.1, 0.05,0.05,0.11,0.11,0.11,0.11};
        String[] namecol = {"reference","customer","pax","status","tableNo","tablecapacity","tablestarttime","occupiedTime","reservationCompletetime","date"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(TableHistory.widthProperty().multiply(widthFactors[i]));
            col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
        }
        applyStatusStyle(statusTH);
        TableHistory.setPlaceholder(new Label("No Table set yet"));

    }
    public void loadTableHistory(){
        Task<List<ReservationTableLogs>> task = new Task<List<ReservationTableLogs>>() {
            @Override
            protected List<ReservationTableLogs> call() throws Exception {
                return ReportsService.findByDateBetween(applyDate, LocalDate.now());
            }
        };
        task.setOnSucceeded(e->{
            reservationlogsdata.setAll(task.getValue());
        });
        task.setOnFailed(e->{
            task.getException().printStackTrace();
        });
        new Thread(task).start();
    }

    ////////////////////////////////////////BUTTONS////////////////////////////
     private void showAddTableDialog() {
         try {
             FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/popup/addTableDialog.fxml"));
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
             addTableDialogController controller = loader.getController();
             controller.setDialogStage(dialogStage);

             dialogStage.showAndWait(); // wait until closed
         } catch (Exception e) {
             e.printStackTrace();
         }
         adminUIController.getDashboardController().loadTableView();
         adminUIController.getDashboardController().updateLabels();
         loadTableManager();
         adminUIController.getReservationController().loadAvailableTable();

     }
     //////////////////////////ALLERT///////////////////////////////////////////
     public void showAlert(String message) {
         Alert alert = new Alert(Alert.AlertType.WARNING);
         alert.setTitle("Warning");
         alert.setHeaderText(null); // no header
         alert.setContentText(message);
         alert.showAndWait();
     }

     private void handleRemoveCustomerFromSelected() {
         ManageTablesDTO data = TableManager.getSelectionModel().getSelectedItem();
         if (data == null) {
             System.out.println("[handleRemoveCustomerFromSelected] No table selected");
             return;
         }

         if (data.getCustomer() == null || data.getCustomer().isEmpty()) {
             System.out.println("[handleRemoveCustomerFromSelected] No customer assigned to this table");
             return;
         }

         System.out.println("[handleRemoveCustomerFromSelected] TableId: " + data.getTableId() + ", TableNo: " + data.getTableNo());

         User user = currentuser;
          Task<Void> task = new Task<>() {
              @Override
              protected Void call() {
                  TablesService.removeCustomerFromTable(data.getTableId());
                  return null;
              }
          };

          task.setOnSucceeded(e -> {
              activityLogService.logAction(
                      user.getUsername(),
                      user.getPosition().toString(),
                      "Table",
                      "Remove Customer",
                      String.format("Removed customer from table %s", data.getTableNo())
              );
              adminUIController.getDashboardController().loadTableView();
              adminUIController.getDashboardController().updateLabels();
              loadTableManager();
              loadTableHistory();
              adminUIController.getReservationController().loadAvailableTable();
              adminUIController.getReservationController().loadReservationsData();
          });

         task.setOnFailed(e -> {
             task.getException().printStackTrace();
         });

         new Thread(task).start();
     }





    public void initialize(){

         applyDate = LocalDate.now();
         tablepane.minHeightProperty().bind(tablepane.widthProperty().multiply(0.95));
         
         // Setup UI first
         if (adminUIController != null) {
             setupTableManager();
         }
         setupTableHistory();
         
         // Defer data loading to prevent UI blocking
         Platform.runLater(() -> {
             loadTableManager();
             loadTableHistory();
         });
     }

}
