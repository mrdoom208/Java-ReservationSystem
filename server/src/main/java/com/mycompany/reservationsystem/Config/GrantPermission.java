package com.mycompany.reservationsystem.Config;

import com.mycompany.reservationsystem.model.Permission;
import com.mycompany.reservationsystem.model.PositionPermission;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.PermissionRepository;
import com.mycompany.reservationsystem.repository.PositionPermissionRepository;
import com.mycompany.reservationsystem.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GrantPermission {

    @Autowired
    private PermissionRepository permissionRepo;

    @Autowired
    private PositionPermissionRepository positionPermissionRepo;

    @Autowired
    private UserService userService;

    @PostConstruct
    public void init() {
        seedPermissions();
        seedRolePermissions();
        userService.createAdminIfMissing();
    }

    private void seedPermissions() {
        // ------------------ Navigation ------------------
        createIfMissing("VIEW_DASHBOARD", "NAVIGATION");
        createIfMissing("VIEW_RESERVATION", "NAVIGATION");
        createIfMissing("VIEW_TABLES", "NAVIGATION");
        createIfMissing("VIEW_MESSAGING", "NAVIGATION");
        createIfMissing("VIEW_REPORTS", "NAVIGATION");
        createIfMissing("VIEW_ACCOUNTS", "NAVIGATION");
        createIfMissing("VIEW_ACTIVITY_LOGS", "NAVIGATION");

        // ------------------ Reservation Management ------------------
        createIfMissing("CREATE_RESERVATION", "RESERVATION");
        createIfMissing("CANCEL_RESERVATION", "RESERVATION");
        createIfMissing("EDIT_RESERVATION", "RESERVATION");

        // ------------------ Table Management ------------------
        createIfMissing("ADD_TABLE", "TABLES");
        createIfMissing("REMOVE_TABLE", "TABLES");
        createIfMissing("EDIT_TABLE", "TABLES");

        // ------------------ Message Management ------------------
        createIfMissing("SEND_MESSAGE", "MESSAGING");
        createIfMissing("CREATE_MESSAGE", "MESSAGING");
        createIfMissing("EDIT_MESSAGE", "MESSAGING");
        createIfMissing("DELETE_MESSAGE", "MESSAGING");

        // ------------------ Account Management ------------------
        createIfMissing("CREATE_ACCOUNT", "ACCOUNTS");
        createIfMissing("EDIT_ACCOUNT", "ACCOUNTS");
        createIfMissing("REMOVE_ACCOUNT", "ACCOUNTS");

        // ------------------ Reservation ------------------
        createIfMissing("VIEW_CANCEL_RESERVATION", "RESERVATION");

        // ------------------ Activity Logs ------------------
        createIfMissing("REMOVE_LOGS", "ACTIVITY_LOGS");

        // ------------------ Settings ------------------
        createIfMissing("CHANGE_TITLE","SETTINGS");
        createIfMissing("VIEW_PERMISSION", "SETTINGS");
        createIfMissing("VIEW_DATABASE", "SETTINGS");
    }

    private void createIfMissing(String code, String module) {
        if (permissionRepo.findByCode(code).isEmpty()) {
            permissionRepo.save(new Permission(code, module));
        }
    }

    private void seedRolePermissions() {
        // ------------------ ADMINISTRATOR ------------------
        grantAll(User.Position.ADMINISTRATOR,
                "VIEW_DASHBOARD","VIEW_RESERVATION","VIEW_TABLES","VIEW_MESSAGING","VIEW_REPORTS","VIEW_ACCOUNTS","VIEW_ACTIVITY_LOGS",
                "CREATE_RESERVATION","CANCEL_RESERVATION","VIEW_CANCEL_RESERVATION","EDIT_RESERVATION",
                "ADD_TABLE","REMOVE_TABLE","EDIT_TABLE",
                "SEND_MESSAGE","CREATE_MESSAGE","EDIT_MESSAGE","DELETE_MESSAGE",
                "CREATE_ACCOUNT","EDIT_ACCOUNT","REMOVE_ACCOUNT",
                "REMOVE_LOGS",
                "CHANGE_TITLE","VIEW_PERMISSION","VIEW_DATABASE"
        );

        // ------------------ MANAGER ------------------
        grantAll(User.Position.MANAGER,
                "VIEW_DASHBOARD","VIEW_RESERVATION","VIEW_TABLES","VIEW_MESSAGING","VIEW_REPORTS","VIEW_ACCOUNTS","VIEW_ACTIVITY_LOGS","VIEW_PERMISSION",
                "CREATE_RESERVATION","CANCEL_RESERVATION","VIEW_CANCEL_RESERVATION","EDIT_RESERVATION",
                "ADD_TABLE","REMOVE_TABLE","EDIT_TABLE",
                "SEND_MESSAGE","CREATE_MESSAGE","EDIT_MESSAGE","DELETE_MESSAGE"
        );

        // ------------------ STAFF ------------------
        grantAll(User.Position.STAFF,
                "VIEW_DASHBOARD","VIEW_RESERVATION","VIEW_TABLES",
                "CREATE_RESERVATION"
        );
    }

    private void grantAll(User.Position position, String... codes) {
        for (String code : codes) {
            grant(position, code);
        }
    }

    private void grant(User.Position position, String code) {
        Permission permission = permissionRepo.findByCode(code).orElseThrow();
        if (!positionPermissionRepo.existsByPositionAndPermission(position, permission)) {
            positionPermissionRepo.save(new PositionPermission(position, permission));
        }
    }
}
