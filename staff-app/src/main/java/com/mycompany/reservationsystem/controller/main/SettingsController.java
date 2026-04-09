package com.mycompany.reservationsystem.controller.main;

import com.fazecast.jSerialComm.SerialPort;
import com.mycompany.reservationsystem.App;
import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.hardware.DeviceDetectionManager;
import com.mycompany.reservationsystem.model.Message;
import com.mycompany.reservationsystem.model.Permission;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.service.MessageService;
import com.mycompany.reservationsystem.service.PermissionService;
import com.mycompany.reservationsystem.service.WebsiteSyncService;
import com.mycompany.reservationsystem.transition.BorderPaneTransition;
import com.mycompany.reservationsystem.transition.NodeTransition;
import com.mycompany.reservationsystem.util.ComboBoxUtil;
import com.mycompany.reservationsystem.util.ToggleButtonUtil;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mycompany.reservationsystem.transition.ButtonTransition.setupButtonAnimation;

public class SettingsController {
    private static final String API_TOGGLE_KEY = "api.toggle";
    private static final String MESSAGE_NEW_KEY = "message.new";
    private static final String MESSAGE_CANCELLED_KEY = "message.cancelled";
    private static final String MESSAGE_CONFIRM_KEY = "message.confirm";
    private static final String MESSAGE_COMPLETE_KEY = "message.complete";
    private static final Set<String> HIDDEN_PERMISSION_CODES = Set.of(
            "CHANGE_TITLE",
            "VIEW_PERMISSION",
            "VIEW_DATABASE",
            "REMOVE_ACCOUNT","EDIT_ACCOUNT","CREATE_ACCOUNT"

    );

    /* ---------------- CORE ---------------- */
    private AdministratorUIController adminUIController;
    private User currentUser;

    /* ---------------- NAV ---------------- */
    @FXML private Button GeneralBtn;
    @FXML private Button MessageBtn;
    @FXML private Button PermissionBtn;
    @FXML private Button DatabaseBtn;

    /* ---------------- PANES ---------------- */
    @FXML private BorderPane GeneralPane;
    @FXML private BorderPane MessagePane;
    @FXML private BorderPane PermissionPane;
    @FXML private BorderPane DatabasePane;
    private BorderPane currentPane;

    @FXML private Label Section;
    /*--------------------- GENERAL ------------------------------------*/
    @FXML
    private TextField ApplicationTitle;

    @FXML
    private MFXComboBox<String> AutoCancelTime;

    /* ---------------- MESSAGING ---------------- */
    @FXML private MFXComboBox<SerialPort> messageDevicePortCombo;
    @FXML private MFXToggleButton apiToggle;
    @FXML private GridPane apiPane;
    @FXML private GridPane manualPane;
    @FXML private Label ControllerName;
    @FXML private Label ModuleName;
    @FXML private Label PhoneNo;
    @FXML private MFXTextField apiID;
    @FXML private MFXPasswordField apiToken;
    @FXML private Button TestButton;
    @FXML private ProgressIndicator ControllerProgress;
    @FXML private ProgressIndicator ModuleProgress;
    @FXML private ProgressIndicator PhoneNoProgress;

    private final Set<String> knownPorts = new HashSet<>();
    private ScheduledExecutorService deviceMonitor;
    @FXML private MFXComboBox<Message> newReservation,cancelledReservation,confirmReservation,completeReservation;
    @FXML private MFXToggleButton newReservationtoggle,cancelledReservationtoggle,confirmReservationtoggle,completeReservationtoggle;

    /* ---------------- PERMISSIONS ---------------- */
    @FXML private TableView<Permission> permissionTable;
    private final Map<Long, Map<User.Position, Boolean>> permissionStates = new HashMap<>();
    private final Map<Long, Map<User.Position, Boolean>> origPermissionStates = new HashMap<>();
    private PermissionService.PermissionSnapshot originalSnapshot;

    /* ---------------- ORIGINAL VALUES FOR CHANGE TRACKING ---------------- */
    private String origAppTitle, origCancelTime;
    private String origController, origModule, origPhone;
    private boolean origApiToggle, origNewToggle, origCancelToggle, origConfirmToggle, origCompleteToggle;
    private boolean restartRequired = false;
    private boolean closeAfterApply = false;
    private String origNewMsg, origCancelMsg, origConfirmMsg, origCompleteMsg;
    private String origServerUrl, origWebsocketUrl;

    @FXML private Button cancelBtn;
    /*--------------------- Server ------------------------------*/
    @FXML private MFXTextField serverUrl, websiteUrl;

    public SettingsController() {
    }

    public SettingsController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
        if (adminUIController != null) {
            this.currentUser = adminUIController.getCurrentUser();
        }
    }

    public void setAdminUIController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
        if (adminUIController != null) {
            this.currentUser = adminUIController.getCurrentUser();
        }
    }

    /*------------------- UI RESPONSE  ------------------*/
    @FXML private Label UIresponse;
    @FXML private Button Apply;
    @FXML private Button okBtn;


    /* ================= INITIALIZE ================= */
    @FXML
    public void initialize() {
        startPortMonitoring();

        showGeneral();

        setupGeneral();
        setupMessaging();
        setupDatabase();
        setupButtons();
        setupSerialCombo();
        setupMessageActionState();

        TestButton.disableProperty().bind(messageDevicePortCombo.valueProperty().isNull());

        Platform.runLater(() -> {
            restoreSettings();
        });
    }

    /* ================= UI ================= */
    private void setupButtons() {
        setupButtonAnimation(GeneralBtn);
        setupButtonAnimation(MessageBtn);
        setupButtonAnimation(PermissionBtn);
        setupButtonAnimation(DatabaseBtn);
    }

    @FXML private void showGeneral()    { switchPane(GeneralPane, "GENERAL", GeneralBtn); }
    @FXML private void showMessage()    { switchPane(MessagePane, "MESSAGING", MessageBtn); }
    @FXML private void showPermission() { switchPane(PermissionPane, "PERMISSION", PermissionBtn); }
    @FXML private void showDatabase()   { switchPane(DatabasePane, "SERVER", DatabaseBtn); }

    private void switchPane(BorderPane pane, String title, Button btn) {
        Section.setText(title);
        setActive(btn);

        if (currentPane == pane) return;
        BorderPane old = currentPane;
        currentPane = pane;
        if (old != null) {
            BorderPaneTransition.animateOut(old, () -> {
                if (pane != null) BorderPaneTransition.animateIn(pane);

            });
        } else if (pane != null) {
            BorderPaneTransition.animateIn(pane);
        }
    }

    private void setActive(Button active) {
        List.of(GeneralBtn, MessageBtn, PermissionBtn, DatabaseBtn)
                .forEach(b -> b.getStyleClass().remove("settings-nav-active"));
        active.getStyleClass().add("settings-nav-active");
    }
    /*=============================== GENERAL =========================================*/
    private void setupGeneral(){
        List<String> options = List.of("Never", "2 minutes", "5 minutes", "10 minutes", "15 minutes", "20 minutes");
        AutoCancelTime.getItems().addAll(options);
        String savedTime = AppSettings.loadCancelTime();
        if (savedTime != null && options.contains(savedTime)) {
            AutoCancelTime.selectItem(savedTime);
        } else if (savedTime != null && !savedTime.isBlank() && !savedTime.equals("Until Table is Available")) {
            AutoCancelTime.selectItem(savedTime);
        }
    }

    public int getSelectedMinutes() {
        String value = AutoCancelTime.getValue();
        if (value == null || value.equals("Never")) {
            return -1; // Never auto-cancel
        }
        if (value != null && value.matches("\\d+.*")) {
            return Integer.parseInt(value.replaceAll("\\D+", ""));
        }
        return 0;
    }

    /* ================= SERIAL PORTS ================= */
    private void setupSerialCombo() {
        messageDevicePortCombo.setConverter(new StringConverter<>() {
            @Override public String toString(SerialPort p) {
                return p == null ? "" : p.getSystemPortName();
            }
            @Override public SerialPort fromString(String s) { return null; }
        });
    }

    private void startPortMonitoring() {
        if (deviceMonitor == null || deviceMonitor.isShutdown() || deviceMonitor.isTerminated()) {
            deviceMonitor = Executors.newSingleThreadScheduledExecutor();
        }

        // Clear combo and populate all ports immediately
        Platform.runLater(() -> {
            messageDevicePortCombo.getItems().clear();
            SerialPort[] ports = SerialPort.getCommPorts();
            messageDevicePortCombo.getItems().addAll(ports);
        });

        // Start background polling
        deviceMonitor.scheduleAtFixedRate(this::refreshPorts, 1, 1, TimeUnit.SECONDS);

        messageDevicePortCombo.valueProperty().addListener((o, a, b) -> {
            if (b != null) AppSettings.saveSerialPort(b.getSystemPortName());
        });
    }

    private void refreshPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        Set<String> current = new HashSet<>();

        for (SerialPort p : ports) {
            String name = p.getSystemPortName();
            current.add(name);
            if (!knownPorts.contains(name)) {
                Platform.runLater(() -> {
                    if (!messageDevicePortCombo.getItems().contains(p)) {
                        messageDevicePortCombo.getItems().add(p);
                    }
                });
            }
        }


        knownPorts.removeIf(old -> !current.contains(old));

        Platform.runLater(() ->
                messageDevicePortCombo.getItems().removeIf(p -> !current.contains(p.getSystemPortName()))
        );
        Platform.runLater(() -> {
            SerialPort selected = messageDevicePortCombo.getValue();
            if (selected != null && !current.contains(selected.getSystemPortName())) {
                // Port unplugged
                messageDevicePortCombo.setValue(null);
                ControllerName.setText("Disconnected");
                ModuleName.setText("Disconnected");
                PhoneNo.setText("Disconnected");

                AppSettings.saveController("");
                AppSettings.saveModule("");
                AppSettings.savePhone("");
            }
        });

        knownPorts.clear();
        knownPorts.addAll(current);
    }


    /* ================= TEST DEVICE ================= */
    @FXML
    private void TestDevice() {
        SerialPort selected = messageDevicePortCombo.getValue();
        if (selected == null) return;

        ControllerName.setText("Searching...");
        ModuleName.setText("Searching...");
        PhoneNo.setText("Searching...");

        Task<DeviceDetectionManager.DeviceResult> task = new Task<>() {
            @Override
            protected DeviceDetectionManager.DeviceResult call() throws Exception {
                DeviceDetectionManager mgr = new DeviceDetectionManager();
                try {
                    mgr.openPort(selected.getSystemPortName(), 115200); // use port name string
                    return mgr.detectDevice();
                } finally {
                    mgr.closePort();
                }
            }
        };

        bindTask(task);

        task.setOnSucceeded(e -> {
            var r = task.getValue();
            ControllerName.setText(r.controller);
            ModuleName.setText(r.module);
            PhoneNo.setText(r.phone);

            AppSettings.saveController(r.controller);
            AppSettings.saveModule(r.module);
            AppSettings.savePhone(r.phone);
            updateMessageActionButtons();
        });

        task.setOnFailed(e -> {
            ControllerName.setText("Detection failed");
            ModuleName.setText("Detection failed");
            PhoneNo.setText("Detection failed");
            task.getException().printStackTrace();
            updateMessageActionButtons();
        });

        new Thread(task, "device-test").start();
    }
    private void bindTask(Task<?> t) {
        ControllerProgress.visibleProperty().unbind();
        ModuleProgress.visibleProperty().unbind();
        PhoneNoProgress.visibleProperty().unbind();
        TestButton.disableProperty().unbind();
        ControllerProgress.visibleProperty().bind(t.runningProperty());
        ModuleProgress.visibleProperty().bind(t.runningProperty());
        PhoneNoProgress.visibleProperty().bind(t.runningProperty());
        TestButton.disableProperty().bind(t.runningProperty());
    }

    //===================== MESSAGING =======================================
    private void setupMessaging(){
        List<Message> allMessages = MessageService.getAllMessagesList();
        ObservableList<Message> messageItems = FXCollections.observableArrayList(allMessages);
        configureMessageCombo(newReservation, messageItems, MESSAGE_NEW_KEY);
        configureMessageCombo(cancelledReservation, messageItems, MESSAGE_CANCELLED_KEY);
        configureMessageCombo(confirmReservation, messageItems, MESSAGE_CONFIRM_KEY);
        configureMessageCombo(completeReservation, messageItems, MESSAGE_COMPLETE_KEY);

        apiID.setText(defaultString(AppSettings.loadPhilSmsSenderId()));
        apiToken.setText(defaultString(AppSettings.loadPhilSmsApiToken()));

        ToggleButtonUtil.setupMessage(apiToggle, API_TOGGLE_KEY);
        updateMessagePaneVisibility(apiToggle.isSelected());
        apiToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateMessagePaneVisibility(newVal);
            updateMessageActionButtons();
        });
        ToggleButtonUtil.setupToggle(newReservationtoggle, MESSAGE_NEW_KEY);
        ToggleButtonUtil.setupToggle(confirmReservationtoggle, MESSAGE_CONFIRM_KEY);
        ToggleButtonUtil.setupToggle(cancelledReservationtoggle, MESSAGE_CANCELLED_KEY);
        ToggleButtonUtil.setupToggle(completeReservationtoggle, MESSAGE_COMPLETE_KEY);

        boolean isAdmin = currentUser != null && currentUser.getPosition() == User.Position.ADMINISTRATOR;
        apiID.setDisable(!isAdmin);
        apiToken.setDisable(!isAdmin);
    }

    private void updateMessagePaneVisibility(boolean showApiPane) {
        manualPane.setVisible(showApiPane);
        manualPane.setManaged(showApiPane);
        apiPane.setVisible(!showApiPane);
        apiPane.setManaged(!showApiPane);

    }

    private void configureMessageCombo(MFXComboBox<Message> comboBox, ObservableList<Message> items, String key) {
        comboBox.setItems(items);
        ComboBoxUtil.formatMessageComboBox(comboBox);
        ComboBoxUtil.selectMessageByLabel(comboBox, AppSettings.loadMessageLabel(key));
    }

    private String getSelectedMessageLabel(MFXComboBox<Message> comboBox) {
        Message selectedMessage = comboBox.getValue();
        return selectedMessage == null ? "" : defaultString(selectedMessage.getMessageLabel());
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private void setupMessageActionState() {
        messageDevicePortCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateMessageActionButtons());
        ControllerName.textProperty().addListener((obs, oldVal, newVal) -> updateMessageActionButtons());
        ModuleName.textProperty().addListener((obs, oldVal, newVal) -> updateMessageActionButtons());
        PhoneNo.textProperty().addListener((obs, oldVal, newVal) -> updateMessageActionButtons());
        ControllerProgress.visibleProperty().addListener((obs, oldVal, newVal) -> updateMessageActionButtons());
        ModuleProgress.visibleProperty().addListener((obs, oldVal, newVal) -> updateMessageActionButtons());
        PhoneNoProgress.visibleProperty().addListener((obs, oldVal, newVal) -> updateMessageActionButtons());
        Platform.runLater(this::updateMessageActionButtons);
    }

    private void updateMessageActionButtons() {
        boolean disableForModule = !apiToggle.isSelected() && !isModuleReady();
        Apply.setDisable(disableForModule);
        if (okBtn != null) {
            okBtn.setDisable(disableForModule);
        }
    }

    private boolean isModuleReady() {
        if (messageDevicePortCombo.getValue() == null) {
            return false;
        }
        if (ControllerProgress.isVisible() || ModuleProgress.isVisible() || PhoneNoProgress.isVisible()) {
            return false;
        }
        return isDetectedValue(ControllerName.getText(), "controller")
                && isDetectedValue(ModuleName.getText(), "module")
                && isDetectedValue(PhoneNo.getText(), "sim");
    }

    private boolean isDetectedValue(String value, String fallbackWord) {
        if (value == null) {
            return false;
        }

        String normalized = value.trim().toLowerCase();
        return !normalized.isBlank()
                && !normalized.contains("no " + fallbackWord)
                && !normalized.contains("not detected")
                && !normalized.contains("disconnected")
                && !normalized.contains("searching")
                && !normalized.contains("failed");
    }






    /* ================= PERMISSIONS ================= */
    private void setupPermissionTable() {
        permissionTable.getColumns().clear();

        //CodeName
        TableColumn<Permission, String> codeCol = new TableColumn<>("Access");
        codeCol.setStyle("-fx-alignment: CENTER-LEFT;");

        codeCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(toPermissionDisplayName(d.getValue().getCode())));

        codeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        codeCol.prefWidthProperty().bind(permissionTable.widthProperty().multiply(0.4));
        permissionTable.getColumns().add(codeCol);

        //Position columns
        for (User.Position pos : User.Position.values()) {
            final User.Position currentPos = pos;
            TableColumn<Permission, Boolean> col = new TableColumn<>(pos.name());

            col.prefWidthProperty().bind(permissionTable.widthProperty().multiply(0.2));

            col.setCellValueFactory(cellData -> {
                Long permId = cellData.getValue().getId();
                Map<User.Position, Boolean> states = permissionStates.get(permId);
                boolean initialValue = false;
                if (states != null && states.containsKey(currentPos)) {
                    initialValue = Boolean.TRUE.equals(states.get(currentPos));
                }
                BooleanProperty property = new SimpleBooleanProperty(initialValue);
                property.addListener((obs, oldVal, newVal) -> {
                    Map<User.Position, Boolean> stateMap = permissionStates.get(permId);
                    if (stateMap != null) {
                        stateMap.put(currentPos, newVal);
                    }
                });
                return property;
            });

            col.setCellFactory(CheckBoxTableCell.forTableColumn(col));
            
            boolean canEdit = canEditPermissionColumn(pos);
            col.setEditable(canEdit);
            if (!canEdit) {
                col.setStyle("-fx-opacity: 0.7;");
            }
            
            permissionTable.getColumns().add(col);
        }
        permissionTable.setEditable(currentUser != null && currentUser.getPosition() != User.Position.STAFF);
    }

    private void loadPermissions() {
        Task<ObservableList<Permission>> task = new Task<>() {
            @Override
            protected ObservableList<Permission> call() throws Exception {
                permissionStates.clear();
                origPermissionStates.clear();
                PermissionService.PermissionSnapshot snapshot = PermissionService.getPermissionSnapshot();
                originalSnapshot = snapshot.copy();
                PermissionService.setOriginalSnapshot(snapshot);
                snapshot.statesByPermissionId().forEach((permissionId, statesByPosition) -> {
                        permissionStates.put(permissionId, new EnumMap<>(statesByPosition));
                        origPermissionStates.put(permissionId, new EnumMap<>(statesByPosition));
                });
                return FXCollections.observableArrayList(snapshot.permissions());
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<Permission> visiblePermissions = task.getValue().filtered(
                    permission -> !HIDDEN_PERMISSION_CODES.contains(permission.getCode())
            );
            permissionTable.setItems(visiblePermissions);
            permissionTable.refresh();
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private String toPermissionDisplayName(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        String normalized = code.toLowerCase().replace('_', ' ');
        String[] words = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }
        return builder.toString();
    }

    private boolean canEditPermissionColumn(User.Position targetPosition) {
        if (targetPosition == User.Position.ADMINISTRATOR) {
            return false;
        }
        if (currentUser == null || currentUser.getPosition() == null) {
            return false;
        }
        return switch (currentUser.getPosition()) {
            case ADMINISTRATOR -> true;
            case MANAGER -> targetPosition != User.Position.MANAGER;
            case STAFF -> false;
        };
    }

    /*=================== SERVER SETTINGS ===========================*/
    private void setupDatabase(){
        serverUrl.setFloatingText("Server URL");
        websiteUrl.setFloatingText("WebSocket Config");
    }

    /* ================= APPLY / CLOSE ================= */
    @FXML
    private void applyChanges() {
        closeAfterApply = false;
        Stage stage = (Stage) Apply.getScene().getWindow();
        com.mycompany.reservationsystem.util.dialog.ConfirmationDialog.show(
            stage,
            "Are you sure you want to apply the changes?",
            () -> doApplyChanges()
        );
    }
    
    private void doApplyChanges() {
        Apply.setDisable(true);
        if (okBtn != null) {
            okBtn.setDisable(true);
        }
        messageresponse(true, "");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(25, 25);

        UIresponse.setGraphic(progressIndicator);
        progressIndicator.setVisible(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                boolean somethingChanged = false;

                /*------------------ General ------------------------*/
                String newAppTitle = ApplicationTitle.getText();
                String newCancelTime = AutoCancelTime.getValue();
                
                if (!stringsEqual(origAppTitle, newAppTitle)) {
                    AppSettings.saveApplicationTitle(newAppTitle);
                    somethingChanged = true;
                }
                if (!stringsEqual(origCancelTime, newCancelTime)) {
                    AppSettings.saveCancelTime(newCancelTime);
                    WebsiteSyncService.sendAutoCancelTime(getSelectedMinutes());
                    somethingChanged = true;
                }

                /*----------------- Messaging --------------------------*/
                String newController = ControllerName.getText();
                String newModule = ModuleName.getText();
                String newPhone = PhoneNo.getText();
                boolean newApiToggle = apiToggle.isSelected();
                boolean newNewToggle = newReservationtoggle.isSelected();
                boolean newCancelToggle = cancelledReservationtoggle.isSelected();
                boolean newConfirmToggle = confirmReservationtoggle.isSelected();
                boolean newCompleteToggle = completeReservationtoggle.isSelected();
                String newNewMsg = getSelectedMessageLabel(newReservation);
                String newCancelMsg = getSelectedMessageLabel(cancelledReservation);
                String newConfirmMsg = getSelectedMessageLabel(confirmReservation);
                String newCompleteMsg = getSelectedMessageLabel(completeReservation);
                
                if (!stringsEqual(origController, newController)) {
                    AppSettings.saveController(newController);
                    somethingChanged = true;
                }
                if (!stringsEqual(origModule, newModule)) {
                    AppSettings.saveModule(newModule);
                    somethingChanged = true;
                }
                if (!stringsEqual(origPhone, newPhone)) {
                    AppSettings.savePhone(newPhone);
                    somethingChanged = true;
                }
                if (origApiToggle != newApiToggle) {
                    AppSettings.saveMessagePane(API_TOGGLE_KEY, newApiToggle);
                    somethingChanged = true;
                }
                if (origNewToggle != newNewToggle) {
                    AppSettings.saveMessageEnabled(MESSAGE_NEW_KEY, newNewToggle);
                    WebsiteSyncService.syncMessageSettings(MESSAGE_NEW_KEY, newNewToggle, newNewMsg);
                    somethingChanged = true;
                }
                if (origCancelToggle != newCancelToggle) {
                    AppSettings.saveMessageEnabled(MESSAGE_CANCELLED_KEY, newCancelToggle);
                    WebsiteSyncService.syncMessageSettings(MESSAGE_CANCELLED_KEY, newCancelToggle, newCancelMsg);
                    somethingChanged = true;
                }
                if (origConfirmToggle != newConfirmToggle) {
                    AppSettings.saveMessageEnabled(MESSAGE_CONFIRM_KEY, newConfirmToggle);
                    WebsiteSyncService.syncMessageSettings(MESSAGE_CONFIRM_KEY, newConfirmToggle, newConfirmMsg);
                    somethingChanged = true;
                }
                if (origCompleteToggle != newCompleteToggle) {
                    AppSettings.saveMessageEnabled(MESSAGE_COMPLETE_KEY, newCompleteToggle);
                    WebsiteSyncService.syncMessageSettings(MESSAGE_COMPLETE_KEY, newCompleteToggle, newCompleteMsg);
                    somethingChanged = true;
                }
                if (!stringsEqual(origNewMsg, newNewMsg)) {
                    AppSettings.saveMessageLabel(MESSAGE_NEW_KEY, newNewMsg);
                    WebsiteSyncService.syncMessageSettings(MESSAGE_NEW_KEY, newNewToggle, newNewMsg);
                    somethingChanged = true;
                }
                if (!stringsEqual(origCancelMsg, newCancelMsg)) {
                    AppSettings.saveMessageLabel(MESSAGE_CANCELLED_KEY, newCancelMsg);
                    WebsiteSyncService.syncMessageSettings(MESSAGE_CANCELLED_KEY, newCancelToggle, newCancelMsg);
                    somethingChanged = true;
                }
                if (!stringsEqual(origConfirmMsg, newConfirmMsg)) {
                    AppSettings.saveMessageLabel(MESSAGE_CONFIRM_KEY, newConfirmMsg);
                    WebsiteSyncService.syncMessageSettings(MESSAGE_CONFIRM_KEY, newConfirmToggle, newConfirmMsg);
                    somethingChanged = true;
                }
                if (!stringsEqual(origCompleteMsg, newCompleteMsg)) {
                    AppSettings.saveMessageLabel(MESSAGE_COMPLETE_KEY, newCompleteMsg);
                    WebsiteSyncService.syncMessageSettings(MESSAGE_COMPLETE_KEY, newCompleteToggle, newCompleteMsg);
                    somethingChanged = true;
                }

                /*------------- Permission ------------*/
                var permissionChanges = PermissionService.getChanges(permissionStates);
                if (!permissionChanges.isEmpty()) {
                    PermissionService.savePermissionChanges(permissionChanges);
                    somethingChanged = true;
                }
                
                /*---------------- Server Settings ------------------------*/
                String newServerUrl = serverUrl != null ? serverUrl.getText() : "";
                String newWebsocketUrl = websiteUrl != null ? websiteUrl.getText() : "";
                restartRequired = false;
                
                if (!stringsEqual(origServerUrl, newServerUrl)) {
                    AppSettings.saveServerUrl(newServerUrl);
                    somethingChanged = true;
                    restartRequired = true;
                }
                if (!stringsEqual(origWebsocketUrl, newWebsocketUrl)) {
                    AppSettings.saveWebsocketUrl(newWebsocketUrl);
                    somethingChanged = true;
                    restartRequired = true;
                }
                
                if (!somethingChanged) {
                    System.out.println("No settings changed - nothing to save");
                }
                return null;
            }

            @Override
            protected void succeeded() {
                UIresponse.setGraphic(null);
                messageresponse(true, "APPLY SUCCESSFULLY");
                updateMessageActionButtons();
                
                if (restartRequired) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Restart Required");
                        alert.setHeaderText(null);
                        alert.setContentText("Please restart the application for server/website URL changes to take effect.");
                        alert.showAndWait();
                        
                        if (closeAfterApply) {
                            close();
                        }
                    });
                } else if (closeAfterApply) {
                    Platform.runLater(() -> close());
                }
            }

            @Override
            protected void failed() {
                UIresponse.setGraphic(null);
                messageresponse(false, "APPLY FAILED");
                updateMessageActionButtons();
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private boolean stringsEqual(String a, String b) {
        String sa = (a == null) ? "" : a;
        String sb = (b == null) ? "" : b;
        return sa.equals(sb);
    }
    
    private void saveToConfigFile(String key, String value) {
        try {
            java.nio.file.Path configDir = java.nio.file.Paths.get("config");
            if (!java.nio.file.Files.exists(configDir)) {
                java.nio.file.Files.createDirectories(configDir);
            }
            
            java.nio.file.Path configPath = configDir.resolve("application.properties");
            Properties props = new Properties();
            
            if (java.nio.file.Files.exists(configPath)) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(configPath.toFile())) {
                    props.load(fis);
                }
            }
            
            props.setProperty(key, value);
            
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(configPath.toFile())) {
                props.store(fos, "ReservationSystem Configuration");
            }
            
            System.setProperty(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOK() {
        if (hasUnsavedChanges()) {
            Stage stage = (Stage) okBtn.getScene().getWindow();
            com.mycompany.reservationsystem.util.dialog.ConfirmationDialog.show(
                stage,
                "Are you sure you want to apply the changes and close?",
                () -> {
                    closeAfterApply = true;
                    doApplyChanges();
                }
            );
        } else {
            close();
        }
    }

    @FXML
    private void handleCancel() {
        if (hasUnsavedChanges()) {
            Stage stage = (Stage) cancelBtn.getScene().getWindow();
            com.mycompany.reservationsystem.util.dialog.DeleteDialog.show(
                stage,
                "You have unsaved changes. Do you want to discard them?",
                () -> close()
            );
        } else {
            close();
        }
    }
    
    private boolean hasUnsavedChanges() {
        System.out.println("[Settings] Checking for unsaved changes...");
        
        if (!stringsEqual(origAppTitle, ApplicationTitle.getText())) {
            System.out.println("[Settings] AppTitle changed: " + origAppTitle + " vs " + ApplicationTitle.getText());
            return true;
        }
        if (!stringsEqual(origCancelTime, AutoCancelTime.getValue())) {
            System.out.println("[Settings] CancelTime changed");
            return true;
        }
        
        if (!stringsEqual(origController, ControllerName.getText())) {
            System.out.println("[Settings] Controller changed");
            return true;
        }
        if (!stringsEqual(origModule, ModuleName.getText())) {
            System.out.println("[Settings] Module changed");
            return true;
        }
        if (!stringsEqual(origPhone, PhoneNo.getText())) {
            System.out.println("[Settings] Phone changed");
            return true;
        }
        
        if (origApiToggle != apiToggle.isSelected()) {
            System.out.println("[Settings] API toggle changed");
            return true;
        }
        if (origNewToggle != newReservationtoggle.isSelected()) {
            System.out.println("[Settings] New toggle changed");
            return true;
        }
        if (origCancelToggle != cancelledReservationtoggle.isSelected()) {
            System.out.println("[Settings] Cancel toggle changed");
            return true;
        }
        if (origConfirmToggle != confirmReservationtoggle.isSelected()) {
            System.out.println("[Settings] Confirm toggle changed");
            return true;
        }
        if (origCompleteToggle != completeReservationtoggle.isSelected()) {
            System.out.println("[Settings] Complete toggle changed");
            return true;
        }
        
        if (!stringsEqual(origNewMsg, getSelectedMessageLabel(newReservation))) {
            System.out.println("[Settings] New msg changed");
            return true;
        }
        if (!stringsEqual(origCancelMsg, getSelectedMessageLabel(cancelledReservation))) {
            System.out.println("[Settings] Cancel msg changed");
            return true;
        }
        if (!stringsEqual(origConfirmMsg, getSelectedMessageLabel(confirmReservation))) {
            System.out.println("[Settings] Confirm msg changed");
            return true;
        }
        if (!stringsEqual(origCompleteMsg, getSelectedMessageLabel(completeReservation))) {
            System.out.println("[Settings] Complete msg changed");
            return true;
        }
        
        if (serverUrl != null && !stringsEqual(origServerUrl, serverUrl.getText())) {
            System.out.println("[Settings] Server URL changed");
            return true;
        }
        if (websiteUrl != null && !stringsEqual(origWebsocketUrl, websiteUrl.getText())) {
            System.out.println("[Settings] WebSocket changed");
            return true;
        }
        
        // Compare current permission states with original
        for (Long permId : origPermissionStates.keySet()) {
            Map<User.Position, Boolean> origStates = origPermissionStates.get(permId);
            Map<User.Position, Boolean> currStates = permissionStates.get(permId);
            if (currStates == null) {
                System.out.println("[Settings] Permission " + permId + " removed");
                return true;
            }
            for (User.Position pos : User.Position.values()) {
                Boolean orig = origStates.get(pos);
                Boolean curr = currStates.get(pos);
                if ((orig == null && curr != null) || (orig != null && !orig.equals(curr))) {
                    System.out.println("[Settings] Permission " + permId + " changed for " + pos);
                    return true;
                }
            }
        }
        // Check for new permissions
        for (Long permId : permissionStates.keySet()) {
            if (!origPermissionStates.containsKey(permId)) {
                System.out.println("[Settings] New permission added: " + permId);
                return true;
            }
        }
        
        System.out.println("[Settings] No unsaved changes");
        return false;
    }

    private void close() {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
    /*================== RESPONSE ===========================*/
    public void messageresponse(boolean successfully,String details){
        if(!successfully){
            UIresponse.getStyleClass().removeAll("login-success", "login-message");
            UIresponse.getStyleClass().add("login-error");
            UIresponse.setText(details);
        }
        else{
            UIresponse.getStyleClass().removeAll("login-error", "login-message-hidden");
            UIresponse.getStyleClass().add("login-success");
            UIresponse.setText(details);
        }

    }

    /* ================= RESTORE ================= */
    private void restoreSettings() {
        //GENERAL
        origAppTitle = AppSettings.loadApplicationTitle();
        origCancelTime = AppSettings.loadCancelTime();
        
        ApplicationTitle.setText(origAppTitle);
        AutoCancelTime.selectItem(origCancelTime);

        //MESSAGING
        String saved = AppSettings.loadSerialPort();
        if (saved != null) {
            messageDevicePortCombo.getItems().stream()
                    .filter(p -> p.getSystemPortName().equals(saved))
                    .findFirst()
                    .ifPresent(messageDevicePortCombo::setValue);
        }

        String cancelTime = AppSettings.loadCancelTime();
        if (cancelTime != null && !cancelTime.isBlank()) {
            AutoCancelTime.selectItem(cancelTime);
        }

        origController = AppSettings.loadController();
        origModule = AppSettings.loadModule();
        origPhone = AppSettings.loadPhone();
        
        ControllerName.setText(origController);
        ModuleName.setText(origModule);
        PhoneNo.setText(origPhone);
        
        origApiToggle = AppSettings.loadMessagePane(API_TOGGLE_KEY);
        origNewToggle = AppSettings.loadMessageEnabled(MESSAGE_NEW_KEY);
        origCancelToggle = AppSettings.loadMessageEnabled(MESSAGE_CANCELLED_KEY);
        origConfirmToggle = AppSettings.loadMessageEnabled(MESSAGE_CONFIRM_KEY);
        origCompleteToggle = AppSettings.loadMessageEnabled(MESSAGE_COMPLETE_KEY);
        
        origNewMsg = AppSettings.loadMessageLabel(MESSAGE_NEW_KEY);
        origCancelMsg = AppSettings.loadMessageLabel(MESSAGE_CANCELLED_KEY);
        origConfirmMsg = AppSettings.loadMessageLabel(MESSAGE_CONFIRM_KEY);
        origCompleteMsg = AppSettings.loadMessageLabel(MESSAGE_COMPLETE_KEY);
        
        origServerUrl = AppSettings.loadServerUrl();
        origWebsocketUrl = AppSettings.loadWebsocketUrl();
        
        if (serverUrl != null) serverUrl.setText(origServerUrl);
        if (websiteUrl != null) websiteUrl.setText(origWebsocketUrl);
        
        updateMessageActionButtons();
    }

    private void applyPermission() {

        Task<Map<String, Boolean>> task = new Task<>() {
            @Override
            protected Map<String, Boolean> call() {
                Map<String, Boolean> perms = new HashMap<>();

                perms.put("CHANGE_TITLE",
                        PermissionService.hasPermission(currentUser, "CHANGE_TITLE"));
                perms.put("VIEW_PERMISSION",
                        PermissionService.hasPermission(currentUser, "VIEW_PERMISSION"));
                perms.put("VIEW_DATABASE",
                        PermissionService.hasPermission(currentUser, "VIEW_DATABASE"));

                return perms;
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Boolean> perms = task.getValue();
            System.out.println("[Settings] applyPermission - currentUser: " + (currentUser != null ? currentUser.getPosition() : "null"));
            System.out.println("[Settings] Permissions: " + perms);

            ApplicationTitle.setDisable(!perms.get("CHANGE_TITLE"));
            PermissionBtn.setManaged(perms.get("VIEW_PERMISSION"));
            PermissionBtn.setVisible(perms.get("VIEW_PERMISSION"));
            DatabaseBtn.setManaged(perms.get("VIEW_DATABASE"));
            DatabaseBtn.setVisible(perms.get("VIEW_DATABASE"));

        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }
    
    public void refreshPermissions() {
        if (adminUIController != null) {
            currentUser = adminUIController.getCurrentUser();
            System.out.println("[Settings] refreshPermissions - currentUser: " + currentUser + ", position: " + (currentUser != null ? currentUser.getPosition() : "null"));
        }
        applyPermission();
        loadPermissions();
        setupPermissionTable();
    }
}
