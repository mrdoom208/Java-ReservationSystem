package com.mycompany.reservationsystem.controller.main;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.ActivityLog;
import com.mycompany.reservationsystem.model.User;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.concurrent.Task;
import javafx.scene.input.ScrollEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ActivityLogsController {
    private AdministratorUIController adminUIController;

    public ActivityLogsController() {
    }

    public ActivityLogsController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }

    public void setAdminUIController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }

    private User currentuser;

    @FXML
    private ScrollPane ActivityLogPane;

    @FXML
    private TableView<ActivityLog> ActivityLogsTable;

    @FXML
    private TableColumn<ActivityLog, Void> actionAL;

    @FXML
    private MFXButton applyAL;

    @FXML
    private TableColumn<ActivityLog, String> descriptionAL;

    @FXML
    private MFXDatePicker fromAL;

    @FXML
    private TableColumn<ActivityLog, String> moduleAL;

    @FXML
    private TableColumn<ActivityLog, String> positionAL;

    @FXML
    private MFXTextField searchAL;

    @FXML
    private TableColumn<ActivityLog, LocalDateTime> timestampsAL;

    @FXML
    private MFXDatePicker toAL;

    @FXML
    private TableColumn<?, ?> userAL;

    @FXML
    private ProgressIndicator progressAL;

    private final ObservableList<ActivityLog> activitylogsdata = FXCollections.observableArrayList();
    private FilteredList<ActivityLog> filteredActivityLogs = new FilteredList<>(activitylogsdata, p -> true);
    private SortedList<ActivityLog> filteredActivityLogsSorted;

    private int currentPage = 0;
    private final int pageSize = 100;
    private boolean hasMore = true;
    private boolean isLoading = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private void setupActivityLogs(){
        applyAL.setOnAction(e -> loadActivityLogsPage(true));
        
        searchAL.textProperty().addListener((obs, oldValue, newValue) -> {
            String search = (newValue == null) ? "" : newValue.toLowerCase();

            filteredActivityLogs.setPredicate(log -> {
                if (search.isEmpty()) return true;

                return (log.getUser() != null && log.getUser().toLowerCase().contains(search))
                        || (log.getPosition() != null && log.getPosition().toLowerCase().contains(search))
                        || (log.getModule() != null && log.getModule().toLowerCase().contains(search))
                        || (log.getAction() != null && log.getAction().toLowerCase().contains(search))
                        || (log.getDescription() != null && log.getDescription().toLowerCase().contains(search));
            });
        });
        timestampsAL.setCellFactory(column -> new TableCell<ActivityLog, LocalDateTime>() {

            private final DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.format(formatter));
                }
            }
        });
        descriptionAL.setCellFactory(column -> new TableCell<ActivityLog, String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-alignment: CENTER-LEFT;");
                if (empty || item == null) {
                    setText("");
                    setGraphic(null);
                    setStyle(null);
                } else {
                    setText(item);
                }
            }
        });

        filteredActivityLogsSorted = new SortedList<>(filteredActivityLogs);
        filteredActivityLogsSorted.comparatorProperty().bind(ActivityLogsTable.comparatorProperty());
        ActivityLogsTable.setItems(filteredActivityLogsSorted);
        TableColumn<?, ?>[] column = {userAL,positionAL,moduleAL,actionAL,descriptionAL,timestampsAL};
        double[] widthFactors = {0.15, 0.15, 0.1, 0.1, 0.35,0.15};
        String[] namecol = {"user", "position", "module", "action", "description","timestamp"};

        for (int i = 0; i < column.length; i++) {
            TableColumn<?, ?> col = column[i];
            col.setResizable(false);
            col.setReorderable(false);
            col.prefWidthProperty().bind(ActivityLogsTable.widthProperty().multiply(widthFactors[i]));
            if (!namecol[i].isEmpty()) {
                col.setCellValueFactory(new PropertyValueFactory<>(namecol[i]));
            }
        }

        ActivityLogsTable.setPlaceholder(new Label("No Activity Data "));
    }
    
    private ActivityLog mapToActivityLog(Map<?, ?> map) {
        ActivityLog log = new ActivityLog();
        Object idObj = map.get("id");
        if (idObj instanceof Number) {
            log.setId(((Number) idObj).longValue());
        }
        Object userObj = map.get("user");
        if (userObj != null) log.setUser(String.valueOf(userObj));
        Object positionObj = map.get("position");
        if (positionObj != null) log.setPosition(String.valueOf(positionObj));
        Object moduleObj = map.get("module");
        if (moduleObj != null) log.setModule(String.valueOf(moduleObj));
        Object actionObj = map.get("action");
        if (actionObj != null) log.setAction(String.valueOf(actionObj));
        Object descObj = map.get("description");
        if (descObj != null) log.setDescription(String.valueOf(descObj));
        Object timestampObj = map.get("timestamp");
        if (timestampObj instanceof String) {
            log.setTimestamp(LocalDateTime.parse((String) timestampObj));
        }
        return log;
    }
    
    private void loadActivityLogsPage(boolean reset) {
        if (isLoading) return;
        
        if (reset) {
            currentPage = 0;
            hasMore = true;
            activitylogsdata.clear();
        }

        if (!hasMore) return;

        isLoading = true;
        progressAL.setVisible(true);

        final int pageToLoad = currentPage;

        Task<List<ActivityLog>> task = new Task<>() {
            @Override
            protected List<ActivityLog> call() {
                try {
                    List<Map<String, Object>> body = ApiClient.getActivityLogs(pageToLoad, pageSize);
                    List<ActivityLog> logs = new ArrayList<>();
                    if (body != null) {
                        for (Map<String, Object> item : body) {
                            logs.add(mapToActivityLog(item));
                        }
                    }
                    return logs;
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }
        };

        task.setOnSucceeded(event -> {
            List<ActivityLog> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                activitylogsdata.addAll(data);
                currentPage++;
                if (data.size() < pageSize) {
                    hasMore = false;
                }
            } else {
                hasMore = false;
            }
            progressAL.setVisible(false);
            isLoading = false;
        });

        task.setOnFailed(event -> {
            progressAL.setVisible(false);
            isLoading = false;
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace();
        });

        executor.execute(task);
    }

    @FXML
    private void initialize(){
        currentuser = adminUIController.getCurrentUser();
        loadActivityLogsPage(true);
        setupActivityLogs();
    }
}
