package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.App;
import com.mycompany.reservationsystem.api.ServerConnectionStatus;
import com.mycompany.reservationsystem.config.AppSettings;
import com.mycompany.reservationsystem.dto.*;
import com.mycompany.reservationsystem.hardware.DeviceDetectionManager;
import com.mycompany.reservationsystem.model.*;
import com.mycompany.reservationsystem.service.MessageService;
import com.mycompany.reservationsystem.service.PermissionService;
import com.mycompany.reservationsystem.service.UserService;
import com.mycompany.reservationsystem.util.BackgroundViewLoader;
import com.mycompany.reservationsystem.util.NotificationManager;
import com.mycompany.reservationsystem.util.PaneAnimationUtil;
import com.mycompany.reservationsystem.util.dialog.DeleteDialog;
import com.mycompany.reservationsystem.websocket.WebSocketListener;
import com.mycompany.reservationsystem.websocket.WebSocketClient;
import com.mycompany.reservationsystem.websocket.WebUpdateHandlerImpl;
import com.mycompany.reservationsystem.controller.popup.ConnectionFailedController;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static com.mycompany.reservationsystem.transition.ButtonTransition.setupButtonAnimation;

/**
 * FXML Controller class for Administrator UI
 * Manages navigation and view loading with background processing
 * FIXED: Proper button state management and navigation freeze prevention
 *
 * @author formentera
 */
public class AdministratorUIController implements Initializable {
    private static final String SERVER_CONNECTING_MESSAGE = "Connecting to server...";
    private static final String SERVER_CONNECTED_MESSAGE = "Server connection established.";
    private static final String SERVER_RETRYING_MESSAGE = "Server connection unavailable. Retrying...";
    private static final String Domain = AppSettings.loadWebsocketUrl().isEmpty() 
        ? System.getenv().getOrDefault("WEBSOCKET_URL", "ws://localhost:13473/raw-ws") 
        : AppSettings.loadWebsocketUrl();
    private static final String datasourceUrl = AppSettings.loadServerUrl();

    public User currentuser;
    private BackgroundViewLoader viewLoader;
    private static DeviceDetectionManager deviceDetectionManager = new DeviceDetectionManager();
    private Button currentlyActiveButton = null;
    private boolean navigationLoading = false;
    private String currentBottomMessage = "";
    private boolean internetReconnectBlocking = false;
    private boolean connectionFailedShown = false;

    private ScheduledExecutorService connectivityMonitor;
    
    @FXML
    private final ObservableList<NotificationItem> notificationList = FXCollections.observableArrayList();
    private ContextMenu notificationContextMenu;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    public enum NotificationType {
        SUCCESS, ERROR, CHANGE, WARNING, CONFIRM, NO_SHOW
    }
    
    public static class NotificationItem {
        private final String title;
        private final String message;
        private final NotificationType type;
        private final LocalDateTime timestamp;
        private final String reference;
        
        public NotificationItem(String title, String message, NotificationType type, String reference) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = LocalDateTime.now();
            this.reference = reference;
        }
        
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public NotificationType getType() { return type; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getReference() { return reference; }
        public String getTimeString() { return timestamp.format(timeFormatter); }
        
        public String getTypeStyleClass() {
            return switch (type) {
                case SUCCESS -> "notification-item-success";
                case ERROR -> "notification-item-error";
                case CHANGE -> "notification-item-change";
                case WARNING -> "notification-item-warning";
                case CONFIRM -> "notification-item-confirm";
                case NO_SHOW -> "notification-item-noshow";
            };
        }
        
        public String getIcon() {
            return switch (type) {
                case SUCCESS -> "+";
                case ERROR -> "X";
                case CHANGE -> "~";
                case WARNING -> "!";
                case CONFIRM -> "ok";
                case NO_SHOW -> "--";
            };
        }
    }

    public void setDeviceDetectionManager(DeviceDetectionManager deviceDetectionManager) {
        AdministratorUIController.deviceDetectionManager = deviceDetectionManager;
    }

    public static DeviceDetectionManager getDeviceDetectionManager() {
        return deviceDetectionManager;
    }

    public void setUser(User user) {
        this.currentuser = user;
        applyPermissions();
        accountname.setText(currentuser.getFirstname() + " " + currentuser.getLastname());
        viewLoader.preloadViews("/fxml/main/Reservation.fxml",
                "/fxml/main/Table.fxml","/fxml/main/Messaging.fxml",
                "/fxml/main/Reports.fxml","/fxml/main/Account.fxml",
                "/fxml/main/ActivityLogs.fxml");
    }

    public User getCurrentUser() {
        return currentuser;
    }

    @FXML
    private Button Dashboardbtn, ReservationManagementbtn, TableManagementbtn,
            Messagingbtn, ManageStaffAndAccountsbtn, Reportsbtn, ActivityLogbtn, generateQrBtn;
    @FXML
    private MenuItem logoutBtn;
    @FXML
    private MenuButton profile;
    @FXML
    private ScrollPane MessagingPane;
    @FXML
    private Label header, accountname;
    @FXML
    private Label reservationNotificationBadge;
    @FXML
    private Label notificationBadge;
    @FXML
    private Button notificationBellBtn;
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private Label connectionStatusDot;
    @FXML
    private StackPane content;
    @FXML
    private VBox connectionWarningPane;
    @FXML
    private Label connectionWarningLabel;
    @FXML
    private ProgressIndicator connectionWarningIndicator;

    /*--------Bottom Pane-------------*/
    @FXML
    private HBox HboxProgress;
    @FXML
    private Label LabelProgress;
    @FXML
    private ProgressBar BarProgress;
    @FXML
    private Button reconnectCloseBtn;

    public HBox getHboxProgress() { return HboxProgress; }
    public Label getLabelProgress() { return LabelProgress; }
    public ProgressBar getBarProgress() { return BarProgress; }
    public boolean isInternetReconnectBlocking() { return internetReconnectBlocking; }

    public void showQueryLoading(String message) {
        if (internetReconnectBlocking) {
            return;
        }
        Platform.runLater(() -> showBottomMessage(message, true, Color.web("#B0B0B0")));
    }

    public void hideQueryLoading() {
        if (internetReconnectBlocking) {
            return;
        }
        Platform.runLater(this::hideBottomMessage);
    }

    private void showReconnectProgress(String message, Color color) {
        showBottomMessage(message, true, color);
        BarProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }

    /*--------------------------------------*/

    private final PermissionService permissionService = null;

    private final MessageService messageService = null;

    private final UserService userService = null;

    private Reservation selectedReservation;
    private final AtomicInteger unreadReservationNotifications = new AtomicInteger();

    private WebUpdateHandlerImpl webUpdateHandler = new WebUpdateHandlerImpl();

    WebSocketClient wsClient;

    public WebSocketClient getWsClient() {
        return wsClient;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize background loader as final instance field
        this.viewLoader = new BackgroundViewLoader();
        this.viewLoader.setAdminUIController(this);
        this.viewLoader.setAdminUIController(this);
        webUpdateHandler.setAdminController(this);

        // Preload commonly used views on startup

        // Setup button animations (FIXED: removed duplicate)
        setupButtonAnimations();

        // Load dashboard initially
        loadInitialDashboard();

        // Start connection status monitoring
        startConnectionStatusMonitor();

        missingDetails();

        // Setup WebSocket
        try {
            this.wsClient = new WebSocketClient(Domain);
            this.wsClient.addListener(webUpdateHandler);
            this.wsClient.addListener(new WebSocketListener() {
                @Override
                public void onMessage(WebUpdateDTO dto) {
                }

            @Override
            public void onConnecting() {
                Platform.runLater(() -> {
                    hideInternetWarning();
                    setInternetReconnectBlocking(false);
                    showReconnectProgress(SERVER_CONNECTING_MESSAGE, Color.web("#7CC7FF"));
                });
            }

            @Override
            public void onConnected() {
                connectionFailedShown = false;
                Platform.runLater(() -> {
                    hideInternetWarning();
                    setInternetReconnectBlocking(false);
                    showBottomMessage(SERVER_CONNECTED_MESSAGE, false, Color.web("#59D97A"));
                    PauseTransition hideDelay = new PauseTransition(javafx.util.Duration.seconds(1));
                    hideDelay.setOnFinished(event -> {
                        if (SERVER_CONNECTED_MESSAGE.equals(currentBottomMessage) && !navigationLoading) {
                            hideBottomMessage();
                        }
                    });
                    hideDelay.play();
                });
            }

            @Override
            public void onConnectionError(String message) {
                Platform.runLater(() -> {
                    hideInternetWarning();
                    setInternetReconnectBlocking(false);
                    
                    if (message != null && message.contains("Max reconnect")) {
                        showConnectionFailedPopup();
                    } else {
                        showReconnectProgress(SERVER_RETRYING_MESSAGE, Color.web("#FFB347"));
                    }
                });
            }
        });
        this.wsClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Hide progress initially
        HboxProgress.setManaged(false);
        HboxProgress.setVisible(false);
        updateReservationNotificationBadge();
        startConnectivityMonitor();
    }

    private void setupButtonAnimations() {
        setupButtonAnimation(Dashboardbtn);
        setupButtonAnimation(ReservationManagementbtn);
        setupButtonAnimation(TableManagementbtn);
        setupButtonAnimation(Messagingbtn);
        setupButtonAnimation(Reportsbtn);
        setupButtonAnimation(ManageStaffAndAccountsbtn);
        setupButtonAnimation(ActivityLogbtn);
    }

    private void loadInitialDashboard() {
        header.setText("Dashboard");
        setActiveButton(Dashboardbtn);
        viewLoader.loadViewAsync("/fxml/main/Dashboard.fxml", content, null);
    }

    private void startConnectionStatusMonitor() {
        updateConnectionStatus();
        
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            updateConnectionStatus();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void updateConnectionStatus() {
        ServerConnectionStatus.checkConnection();
        
        Timeline statusCheck = new Timeline(new KeyFrame(Duration.millis(500), event -> {
            ServerConnectionStatus.Status status = ServerConnectionStatus.getStatus();
            Platform.runLater(() -> {
                connectionStatusDot.getStyleClass().clear();
                switch (status) {
                    case CONNECTED:
                        connectionStatusDot.getStyleClass().add("connected");
                        break;
                    case DISCONNECTED:
                        connectionStatusDot.getStyleClass().add("disconnected");
                        break;
                    case CHECKING:
                        connectionStatusDot.getStyleClass().add("checking");
                        break;
                }
            });
        }));
        statusCheck.play();
    }

    @FXML
    private void openQrCode() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/popup/QrCodeDialog.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(App.primaryStage);
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            
            com.mycompany.reservationsystem.transition.NodeTransition.showSmooth(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void navigate(ActionEvent event) {
        Button clicked = (Button) event.getSource();

        // Prevent clicking same button or if already loading
        if (clicked == currentlyActiveButton || isLoading()) return;

        setActiveButton(clicked);
        if (clicked == ReservationManagementbtn) {
            clearUnreadReservationNotifications();
        }

        // Determine FXML file to load
        String fxmlFile = getFxmlFile(clicked);
        if (fxmlFile == null) {
            return;
        }

        // Load view asynchronously using BackgroundViewLoader
        viewLoader.loadViewAsync(fxmlFile, content, () -> {
            // On completion (UI thread)
            Platform.runLater(() -> {
                // Preload the next likely views in the background
                preloadAdjacentViews(clicked.getId());
            });
        });
    }

    private String getFxmlFile(Button button) {
        switch (button.getId()) {
            case "Dashboardbtn": return "/fxml/main/Dashboard.fxml";
            case "ReservationManagementbtn": return "/fxml/main/Reservation.fxml";
            case "TableManagementbtn": return "/fxml/main/Table.fxml";
            case "Messagingbtn": return "/fxml/main/Messaging.fxml";
            case "ManageStaffAndAccountsbtn": return "/fxml/main/Account.fxml";
            case "Reportsbtn": return "/fxml/main/Reports.fxml";
            case "ActivityLogbtn": return "/fxml/main/ActivityLogs.fxml";
            default: return null;
        }
    }

    private void setActiveButton(Button activeButton) {
        // Clear all active states
        Button[] buttons = {Dashboardbtn, ReservationManagementbtn, TableManagementbtn,
                Messagingbtn, ManageStaffAndAccountsbtn, Reportsbtn, ActivityLogbtn};

        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("navigation-btns-active");
                if (!btn.getStyleClass().contains("navigation-btns")) {
                    btn.getStyleClass().add("navigation-btns");
                }
            }
        }

        // Set new active button
        if (activeButton != null) {
            activeButton.getStyleClass().remove("navigation-btns");
            activeButton.getStyleClass().add("navigation-btns-active");
            currentlyActiveButton = activeButton;
            headerSet(activeButton.getText());
        }
    }
    private void headerSet(String text){
        switch (text) {
            case "Dashboard" -> {
                header.setText("Dashboard");
            }
            case "Reservations" -> {
                header.setText("Reservaiton Management");
            }
            case "Tables" -> {
                header.setText("Table Management");
            }
            case "Messaging" -> {
                header.setText("Messaging Panel");
            }
            case "Reports" -> {
                header.setText("Reservation Reports");
            }
            case "Accounts" -> {
                header.setText("Account Management");
            }
            case "Activity Logs" -> {
                header.setText("Activity Logs");
            }
            default -> {
                header.setText("ReservationSystem");
            }
        }



    }

    private void disableAllNavButtons(boolean disable) {
        Button[] buttons = {Dashboardbtn, ReservationManagementbtn, TableManagementbtn,
                Messagingbtn, ManageStaffAndAccountsbtn, Reportsbtn, ActivityLogbtn};
        for (Button btn : buttons) {
            if (btn != null) {
                btn.setDisable(disable);
            }
        }
    }

    public void disableNavButtons(boolean disable) {
        disableAllNavButtons(disable);
    }

    private boolean isLoading() {
        return navigationLoading;
    }

    public void setNavigationLoading(boolean loading) {
        this.navigationLoading = loading;
    }

    private void showLoading(String message, boolean show) {
        if (show) {
            if (currentBottomMessage != null && (currentBottomMessage.contains("Reconnecting") || currentBottomMessage.contains("Retrying"))) {
                return; // Don't show loading if reconnect is active
            }
            navigationLoading = true;
            showBottomMessage(message, true, Color.web("#B0B0B0"));
            BarProgress.setProgress(0.3);
        } else {
            navigationLoading = false;
            hideBottomMessage();
        }
    }

    public void showBottomMessage(String message, boolean showProgress, Color color) {
        if (HboxProgress.isVisible()
                && message.equals(currentBottomMessage)
                && BarProgress.isVisible() == showProgress) {
            LabelProgress.setTextFill(color);
            reconnectCloseBtn.setVisible(internetReconnectBlocking);
            reconnectCloseBtn.setManaged(internetReconnectBlocking);
            return;
        }

        HboxProgress.setVisible(true);
        HboxProgress.setManaged(true);
        currentBottomMessage = message;
        LabelProgress.setText(message);
        LabelProgress.setTextFill(color);
        BarProgress.setVisible(showProgress);
        BarProgress.setManaged(showProgress);
        reconnectCloseBtn.setVisible(internetReconnectBlocking);
        reconnectCloseBtn.setManaged(internetReconnectBlocking);
    }

    private void hideBottomMessage() {
        HboxProgress.setVisible(false);
        HboxProgress.setManaged(false);
        currentBottomMessage = "";
        reconnectCloseBtn.setVisible(false);
        reconnectCloseBtn.setManaged(false);
    }

    private void showInternetWarning(String message) {
        internetReconnectBlocking = true;
        disableAllNavButtons(true);
        content.setDisable(true);
        if (profile != null) {
            profile.setDisable(true);
        }
        connectionWarningPane.toFront();
        connectionWarningPane.setVisible(true);
        connectionWarningPane.setManaged(true);
        connectionWarningIndicator.setVisible(true);
        connectionWarningIndicator.setManaged(true);
        connectionWarningLabel.setText(message);
        connectionWarningLabel.setVisible(true);
        connectionWarningLabel.setManaged(true);
    }

    private void hideInternetWarning() {
        connectionWarningPane.setVisible(false);
        connectionWarningPane.setManaged(false);
        connectionWarningIndicator.setVisible(false);
        connectionWarningIndicator.setManaged(false);
        connectionWarningLabel.setVisible(false);
        connectionWarningLabel.setManaged(false);
        setInternetReconnectBlocking(false);
    }

    private boolean isInternetConnectionIssue(String message) {
        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase();
        return normalized.contains("network location cannot be reached")
                || normalized.contains("the network location cannot be reached")
                || normalized.contains("unknown host")
                || normalized.contains("unresolvedaddressexception")
                || normalized.contains("unresolved address")
                || normalized.contains("network is unreachable")
                || normalized.contains("no route to host")
                || normalized.contains("i/o error")
                || normalized.contains("timed out")
                || normalized.contains("timeout")
                || normalized.contains("socketexception")
                || normalized.contains("connectexception")
                || normalized.contains("host is down")
                || normalized.contains("temporary failure in name resolution")
                || normalized.contains("unable to resolve host");
    }

    public void handleConnectivityIssue(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            String text = current.getMessage();
            if (text != null && !text.isBlank()) {
                if (!builder.isEmpty()) {
                    builder.append(" | ");
                }
                builder.append(text);
            } else {
                if (!builder.isEmpty()) {
                    builder.append(" | ");
                }
                builder.append(current.getClass().getSimpleName());
            }
            current = current.getCause();
        }

        String details = builder.toString();
        if (!isInternetConnectionIssue(details)) {
            return;
        }

        Platform.runLater(() -> {
            hideInternetWarning();
            setInternetReconnectBlocking(true);
            showReconnectProgress("Connection issue. Reconnecting...", Color.web("#FF7B72"));
        });
    }

    private void startConnectivityMonitor() {
        if (connectivityMonitor != null && !connectivityMonitor.isShutdown()) {
            return;
        }

        connectivityMonitor = Executors.newSingleThreadScheduledExecutor();
        connectivityMonitor.scheduleAtFixedRate(() -> {
            boolean serverReachable = isHostReachable(extractHost(Domain));
            boolean dbReachable = isHostReachable(extractDatabaseHost(datasourceUrl));

            Platform.runLater(() -> {
                String message = null;
                if (!serverReachable && !dbReachable) {
                    message = "No internet connection. Reconnecting...";
                } else if (!serverReachable) {
                    message = "Server connection unavailable. Retrying...";
                } else if (!dbReachable) {
                    message = "Database connection unavailable. Retrying...";
                }

                if (message != null) {
                    setInternetReconnectBlocking(true);
                    showReconnectProgress(message, Color.web("#FF7B72"));
                } else if (internetReconnectBlocking) {
                    setInternetReconnectBlocking(false);
                    showBottomMessage(SERVER_CONNECTED_MESSAGE, false, Color.web("#59D97A"));
                    PauseTransition hideDelay = new PauseTransition(javafx.util.Duration.seconds(1));
                    hideDelay.setOnFinished(event -> {
                        if (SERVER_CONNECTED_MESSAGE.equals(currentBottomMessage) && !navigationLoading) {
                            hideBottomMessage();
                        }
                    });
                    hideDelay.play();
                }
            });
        }, 0, 2, TimeUnit.SECONDS);
    }

    private boolean isHostReachable(String host) {
        if (host == null || host.isBlank()) {
            return true;
        }

        try {
            InetAddress.getByName(host);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String extractHost(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        try {
            return URI.create(url).getHost();
        } catch (Exception ex) {
            return null;
        }
    }

    private String extractDatabaseHost(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return null;
        }

        int start = jdbcUrl.indexOf("://");
        if (start < 0) {
            return null;
        }

        String remainder = jdbcUrl.substring(start + 3);
        int slashIndex = remainder.indexOf('/');
        String hostPort = slashIndex >= 0 ? remainder.substring(0, slashIndex) : remainder;
        int colonIndex = hostPort.indexOf(':');
        return colonIndex >= 0 ? hostPort.substring(0, colonIndex) : hostPort;
    }

    private void setInternetReconnectBlocking(boolean blocking) {
        internetReconnectBlocking = blocking;
        content.setDisable(blocking);
        if (profile != null) {
            profile.setDisable(blocking);
        }
        if (blocking) {
            disableAllNavButtons(true);
        } else if (!navigationLoading) {
            disableAllNavButtons(false);
        }
        if (reconnectCloseBtn != null) {
            reconnectCloseBtn.setVisible(blocking);
            reconnectCloseBtn.setManaged(blocking);
        }
    }

    @FXML
    private void handleReconnectClose() {
        DeleteDialog.show(
                "Are you sure you want to close the application? The system is still trying to reconnect.",
                () -> {
                    if (wsClient != null) {
                        wsClient.disconnect();
                    }
                    shutdown();
                    Stage stage = App.primaryStage != null ? App.primaryStage : (Stage) HboxProgress.getScene().getWindow();
                    if (stage != null) {
                        stage.close();
                    }
                }
        );
    }

    private void showConnectionFailedPopup() {
        if (connectionFailedShown) {
            return;
        }
        connectionFailedShown = true;
        
        Platform.runLater(() -> {
            ConnectionFailedController.show(
                () -> {
                    connectionFailedShown = false;
                    Platform.runLater(() -> {
                        if (wsClient != null) {
                            wsClient.forceReconnect();
                        }
                    });
                },
                () -> {
                    Platform.runLater(() -> {
                        shutdown();
                        Stage stage = App.primaryStage;
                        if (stage != null) {
                            stage.close();
                        }
                    });
                }
            );
        });
    }

    public void incrementUnreadReservationNotifications() {
        unreadReservationNotifications.incrementAndGet();
        updateReservationNotificationBadge();
    }

    public void clearUnreadReservationNotifications() {
        unreadReservationNotifications.set(0);
        updateReservationNotificationBadge();
    }

    private void updateReservationNotificationBadge() {
        if (reservationNotificationBadge == null) {
            return;
        }

        Platform.runLater(() -> {
            int unreadCount = unreadReservationNotifications.get();
            boolean hasUnread = unreadCount > 0;
            reservationNotificationBadge.setVisible(hasUnread);
            reservationNotificationBadge.setManaged(hasUnread);
            reservationNotificationBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
        });
    }
    
    @FXML
    private void showNotificationMenu() {
        if (notificationBellBtn == null) return;
        
        if (notificationContextMenu == null) {
            createNotificationContextMenu();
        }
        
        populateNotificationMenu();
        notificationContextMenu.show(notificationBellBtn, javafx.geometry.Side.BOTTOM, 0, 0);
    }
    
    private void createNotificationContextMenu() {
        notificationContextMenu = new ContextMenu();
        notificationContextMenu.setId("notificationContextMenu");
        notificationContextMenu.setAutoHide(true);
        
        MenuItem header = new MenuItem("Notifications");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #333333; -fx-text-fill: white;");
        header.setDisable(true);
        
        MenuItem clearAll = new MenuItem("Clear All");
        clearAll.setStyle("-fx-text-fill: #FF6B6B;");
        clearAll.setOnAction(e -> clearAllNotifications());
        
        notificationContextMenu.getItems().add(header);
        notificationContextMenu.getItems().add(new SeparatorMenuItem());
        notificationContextMenu.getItems().add(new SeparatorMenuItem());
        notificationContextMenu.getItems().add(clearAll);
    }
    
    private void populateNotificationMenu() {
        notificationContextMenu.getItems().clear();
        
        MenuItem header = new MenuItem("Notifications");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #333333; -fx-text-fill: white;");
        header.setDisable(true);
        
        notificationContextMenu.getItems().add(header);
        
        if (notificationList.isEmpty()) {
            MenuItem empty = new MenuItem("No notifications");
            empty.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
            empty.setDisable(true);
            notificationContextMenu.getItems().add(empty);
        } else {
            for (NotificationItem item : notificationList) {
                addNotificationItemToMenu(item);
            }
        }
        
        MenuItem clearAll = new MenuItem("Clear All");
        clearAll.setStyle("-fx-text-fill: #FF6B6B;");
        clearAll.setOnAction(e -> clearAllNotifications());
        notificationContextMenu.getItems().add(clearAll);
    }
    
    private void addNotificationItemToMenu(NotificationItem item) {
        MenuItem menuItem = new MenuItem();
        menuItem.setUserData(item);
        menuItem.getStyleClass().add("notification-item");
        menuItem.getStyleClass().add(getTypeBorderClass(item.getType()));
        
        HBox content = new HBox(12);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);
        
        Label iconLabel = new Label(item.getIcon());
        iconLabel.getStyleClass().add("icon-circle");
        iconLabel.getStyleClass().add(getIconClass(item.getType()));
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        VBox textContent = new VBox(3);
        HBox.setHgrow(textContent, javafx.scene.layout.Priority.ALWAYS);
        
        HBox topRow = new HBox(8);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(item.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        Label refLabel = new Label(item.getReference() != null ? item.getReference() : "");
        refLabel.setStyle("-fx-text-fill: " + getTypeColor(item.getType()) + "; -fx-font-size: 11px;");
        
        Label timeLabel = new Label(item.getTimeString());
        timeLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        HBox.setHgrow(timeLabel, javafx.scene.layout.Priority.ALWAYS);
        
        topRow.getChildren().addAll(titleLabel, refLabel, timeLabel);
        
        Label messageLabel = new Label(truncateMessage(item.getMessage()));
        messageLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 12px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(260);
        
        textContent.getChildren().addAll(topRow, messageLabel);
        content.getChildren().addAll(iconLabel, textContent);
        
        menuItem.setGraphic(content);
        menuItem.setOnAction(e -> handleNotificationClick(item));
        
        notificationContextMenu.getItems().add(menuItem);
    }
    
    private String getTypeBorderClass(NotificationType type) {
        return switch (type) {
            case SUCCESS -> "success-border";
            case ERROR -> "error-border";
            case CHANGE -> "change-border";
            case WARNING -> "warning-border";
            case CONFIRM -> "confirm-border";
            case NO_SHOW -> "noshow-border";
        };
    }
    
    private String getIconClass(NotificationType type) {
        return switch (type) {
            case SUCCESS -> "success-icon";
            case ERROR -> "error-icon";
            case CHANGE -> "change-icon";
            case WARNING -> "warning-icon";
            case CONFIRM -> "confirm-icon";
            case NO_SHOW -> "noshow-icon";
        };
    }
    
    private String getTypeColor(NotificationType type) {
        return switch (type) {
            case SUCCESS -> "#00C277";
            case ERROR -> "#E74C3C";
            case CHANGE -> "#3B82F6";
            case WARNING -> "#F59E0B";
            case CONFIRM -> "#22C55E";
            case NO_SHOW -> "#DC2626";
        };
    }
    
    @FXML
    private void clearAllNotifications() {
        notificationList.clear();
        
        if (notificationContextMenu != null) {
            notificationContextMenu.hide();
        }
        
        updateNotificationBadge();
    }
    
    public void addNotification(String title, String message, NotificationType type, String reference) {
        NotificationItem item = new NotificationItem(title, message, type, reference);
        notificationList.add(0, item);
        
        if (notificationList.size() > 50) {
            notificationList.remove(notificationList.size() - 1);
        }
        
        Platform.runLater(() -> {
            addNotificationItemToMenu(item);
            updateNotificationBadge();
        });
    }
    
    private String truncateMessage(String msg) {
        if (msg == null) return "";
        if (msg.length() > 80) {
            return msg.substring(0, 77) + "...";
        }
        return msg;
    }
    
    private void handleNotificationClick(NotificationItem item) {
        navigate(new ActionEvent(ReservationManagementbtn, null));
    }
    
    private void updateNotificationBadge() {
        if (notificationBadge == null) return;
        
        int count = notificationList.size();
        boolean hasUnread = count > 0;
        notificationBadge.setVisible(hasUnread);
        notificationBadge.setManaged(hasUnread);
        notificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
    }

    @FXML
    private void Profile() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main/Profile.fxml"));
                Parent root = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.initOwner(App.primaryStage);
                dialogStage.initStyle(StageStyle.TRANSPARENT);
                dialogStage.setResizable(false);
                Scene scn = new Scene(root);
                scn.setFill(Color.TRANSPARENT);
                dialogStage.setScene(scn);

                ProfileController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setCurrentuser(currentuser);
                dialogStage.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private Stage settingsStageRef;


    @FXML
    private void Settings(ActionEvent event) {
        Scene currentScene = content.getScene();
        currentScene.setCursor(Cursor.WAIT);
        disableAllNavButtons(true);
        showLoading("Loading Settings...", true);

        FXMLLoader settingsLoader = new FXMLLoader(getClass().getResource("/fxml/main/Settings.fxml"));

        // Load FXML on BACKGROUND thread only
        Task<Parent> loadFxmlTask = new Task<>() {
            @Override
            protected Parent call() throws IOException {
                return settingsLoader.load();
            }
        };

        loadFxmlTask.setOnSucceeded(e -> {
            // Create and show Stage on FX thread
            Platform.runLater(() -> {
                try {
                    showLoading("", false);
                    Parent settingsRoot = loadFxmlTask.getValue();
                    
                    SettingsController settingsController = settingsLoader.getController();
                    settingsController.setAdminUIController(AdministratorUIController.this);
                    settingsController.refreshPermissions();
                    
                    settingsStageRef = new Stage(); // Now safe on FX thread
                    settingsStageRef.initStyle(StageStyle.TRANSPARENT);

                    settingsStageRef.setScene(new Scene(settingsRoot));
                    //settingsScene.setFill(Color.TRANSPARENT);
                    settingsStageRef.setTitle("Settings");
                    settingsStageRef.initModality(Modality.APPLICATION_MODAL);
                    settingsStageRef.initOwner(App.primaryStage);
                    settingsStageRef.setResizable(false);
                    settingsStageRef.centerOnScreen();
                    PaneAnimationUtil.animateDialogOpen(settingsRoot, null);
                    settingsStageRef.showAndWait(); // Blocks until closed

                        currentScene.setCursor(Cursor.DEFAULT);
                        disableAllNavButtons(false);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    currentScene.setCursor(Cursor.DEFAULT);
                    disableAllNavButtons(false);
                    showLoading("", false);
                }
            });
        });

        loadFxmlTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                currentScene.setCursor(Cursor.DEFAULT);
                disableAllNavButtons(false);
                showLoading("", false);
                loadFxmlTask.getException().printStackTrace();
            });
        });

        new Thread(loadFxmlTask).start();
    }




    private void preloadAdjacentViews(String currentViewId) {
        switch (currentViewId) {
            case "Dashboardbtn":
                viewLoader.preloadView("/fxml/main/Reservation.fxml");
                break;
            case "ReservationManagementbtn":
                viewLoader.preloadView("/fxml/main/Table.fxml");
                break;
            case "TableManagementbtn":
                viewLoader.preloadView("/fxml/main/Messaging.fxml");
                break;
            case "Messagingbtn":
                viewLoader.preloadView("/fxml/main/Account.fxml");
                break;
            case "ManageStaffAndAccountsbtn":
                viewLoader.preloadView("/fxml/main/Reports.fxml");
                break;
            case "Reportsbtn":
                viewLoader.preloadView("/fxml/main/ActivityLogs.fxml");
                break;
        }
    }

    private void applyPermissions() {
        if (currentuser == null) return;

        Map<Button, String> buttonPermissions = Map.of(
                Dashboardbtn, "VIEW_DASHBOARD",
                ReservationManagementbtn, "VIEW_RESERVATION",
                TableManagementbtn, "VIEW_TABLES",
                Messagingbtn, "VIEW_MESSAGING",
                Reportsbtn, "VIEW_REPORTS",
                ManageStaffAndAccountsbtn, "VIEW_ACCOUNTS",
                ActivityLogbtn, "VIEW_ACTIVITY_LOGS"
        );

        Task<Map<Button, Boolean>> permissionTask = new Task<>() {
            @Override
            protected Map<Button, Boolean> call() {
                Map<Button, Boolean> results = new HashMap<>();
                buttonPermissions.forEach((button, code) -> {
                    if (button != null) {
                        results.put(button, PermissionService.hasPermission(currentuser, code));
                    }
                });
                return results;
            }
        };

        permissionTask.setOnSucceeded(e -> {
            permissionTask.getValue().forEach((button, allowed) -> {
                //button.setManaged(allowed);
                button.setVisible(allowed);
            });
        });

        permissionTask.setOnFailed(e -> {
            permissionTask.getException().printStackTrace();
        });

        new Thread(permissionTask, "permission-task").start();
    }

    // FIXED: Non-static controller getters using instance viewLoader
    public DashboardController getDashboardController() {
        return (DashboardController) viewLoader.getCachedController("/fxml/main/Dashboard.fxml");
    }

    public ReservationController getReservationController() {
        return (ReservationController) viewLoader.getCachedController("/fxml/main/Reservation.fxml");
    }

    public TableController getTableController() {
        return (TableController) viewLoader.getCachedController("/fxml/main/Table.fxml");
    }

    public MessagingController getMessagingController() {
        return (MessagingController) viewLoader.getCachedController("/fxml/main/Messaging.fxml");
    }

    public AccountController getAccountController() {
        return (AccountController) viewLoader.getCachedController("/fxml/main/Account.fxml");
    }

    public ActivityLogsController getActivityLogsController() {
        return (ActivityLogsController) viewLoader.getCachedController("/fxml/main/ActivityLogs.fxml");
    }

    public ReportsController getReportsController() {
        return (ReportsController) viewLoader.getCachedController("/fxml/main/Reports.fxml");
    }

    public SettingsController getSettingsController() {
        return (SettingsController) viewLoader.getCachedController("/fxml/main/Settings.fxml");
    }

    private void missingDetails() {
        List<String> labels = List.of(
            "New Reservation",
            "Confirm Reservation", 
            "Cancelled Reservation",
            "Complete Reservation",
            "Notify Customer"
        );
        MessageService.seedMessagesFromBackend(labels);

        if (AppSettings.loadMessageLabel("message.new").isBlank()) {
            AppSettings.saveMessageLabel("message.new", "New Reservation");
        }
        if (AppSettings.loadMessageLabel("message.cancelled").isBlank()) {
            AppSettings.saveMessageLabel("message.cancelled", "Cancelled Reservation");
        }
        if (AppSettings.loadMessageLabel("message.confirm").isBlank()) {
            AppSettings.saveMessageLabel("message.confirm", "Confirm Reservation");
        }
        if (AppSettings.loadMessageLabel("message.complete").isBlank()) {
            AppSettings.saveMessageLabel("message.complete", "Complete Reservation");
        }

        if (AppSettings.loadMessageLabel("message.cancelled").isBlank()) {
            AppSettings.saveMessageLabel("message.cancelled", "Cancelled Reservation");
        }
        if (AppSettings.loadMessageLabel("message.confirm").isBlank()) {
            AppSettings.saveMessageLabel("message.confirm", "Confirm Reservation");
        }
        if (AppSettings.loadMessageLabel("message.complete").isBlank()) {
            AppSettings.saveMessageLabel("message.complete", "Complete Reservation");
        }
        if (AppSettings.loadMessageLabel("message.notify_customer").isBlank()) {
            AppSettings.saveMessageLabel("message.notify_customer", "Notify Customer");
        }
    }

    @FXML
    private void logout() {
        shutdown();
        if (wsClient != null) {
            wsClient.disconnect();
        }
        this.currentuser.setStatus("Offline");
        userService.save(currentuser);
        this.currentuser = null;

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/Login.fxml"));
                Parent loginRoot = loader.load();

                Stage loginStage = new Stage();
                App.primaryStage = loginStage;
                loginStage.initStyle(StageStyle.TRANSPARENT);
                Scene scn = new Scene(loginRoot);
                scn.setFill(Color.TRANSPARENT);
                loginStage.setScene(scn);
                loginStage.setTitle("Login");
                loginStage.setResizable(false);
                loginStage.show();
                loginStage.centerOnScreen();

                Stage currentStage = (Stage)((MenuItem) logoutBtn).getParentPopup().getOwnerWindow();
                currentStage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        if (connectivityMonitor != null) {
            connectivityMonitor.shutdownNow();
        }
        if (viewLoader != null) {
            viewLoader.shutdown();
        }
    }
}
