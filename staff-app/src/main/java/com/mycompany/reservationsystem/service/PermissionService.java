package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.Permission;
import com.mycompany.reservationsystem.model.User;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionService {

    private static PermissionSnapshot originalSnapshot;

    public static class PermissionSnapshot {
        private Map<Long, Map<User.Position, Boolean>> permissions = new HashMap<>();
        private List<Permission> permissionList = new ArrayList<>();

        public Map<Long, Map<User.Position, Boolean>> getPermissions() {
            return permissions;
        }

        public void setPermissions(Map<Long, Map<User.Position, Boolean>> permissions) {
            this.permissions = permissions;
        }

        public PermissionSnapshot copy() {
            PermissionSnapshot copy = new PermissionSnapshot();
            for (Map.Entry<Long, Map<User.Position, Boolean>> entry : this.permissions.entrySet()) {
                copy.permissions.put(entry.getKey(), new EnumMap<>(entry.getValue()));
            }
            copy.permissionList = new ArrayList<>(this.permissionList);
            return copy;
        }

        public Map<Long, Map<User.Position, Boolean>> statesByPermissionId() {
            return permissions;
        }

        public List<Permission> permissions() {
            return permissionList;
        }
        
        public void setPermissionList(List<Permission> list) {
            this.permissionList = list;
        }
    }

    public static List<Map<String, Object>> getAllPermissions() {
        return ApiClient.getPermissions();
    }

    public static void updatePermission(Long id, Object permission) {
        ApiClient.updatePermission(id, permission);
    }

    public static boolean hasPermission(User user, String permissionName) {
        if (user == null || user.getPosition() == null) {
            return false;
        }
        return user.getPosition() == User.Position.ADMINISTRATOR || user.getPosition() == User.Position.MANAGER;
    }

    public static PermissionSnapshot getPermissionSnapshot() {
        PermissionSnapshot snapshot = new PermissionSnapshot();
        Map<String, Object> data = ApiClient.getPermissionSnapshot();
        
        System.out.println("[PermissionService] getPermissionSnapshot returned: " + data);
        
        List<Permission> perms = new ArrayList<>();
        Object permissionsObj = data.get("permissions");
        Object statesObj = data.get("states");
        
        if (permissionsObj instanceof List) {
            List<Map<String, Object>> permissionsList = (List<Map<String, Object>>) permissionsObj;
            Map<String, Map<String, Boolean>> statesMap = new HashMap<>();
            
            if (statesObj instanceof Map) {
                Map<?, ?> rawStates = (Map<?, ?>) statesObj;
                for (Map.Entry<?, ?> entry : rawStates.entrySet()) {
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();
                    if (value instanceof Map) {
                        Map<String, Boolean> positionStates = new HashMap<>();
                        Map<?, ?> positionMap = (Map<?, ?>) value;
                        for (Map.Entry<?, ?> posEntry : positionMap.entrySet()) {
                            positionStates.put(posEntry.getKey().toString(), (Boolean) posEntry.getValue());
                        }
                        statesMap.put(key, positionStates);
                    }
                }
            }
            
            for (Map<String, Object> map : permissionsList) {
                Permission p = new Permission();
                p.setId(map.get("id") != null ? ((Number) map.get("id")).longValue() : null);
                p.setCode((String) map.get("code"));
                p.setModule((String) map.get("module"));
                
                Map<User.Position, Boolean> states = new EnumMap<>(User.Position.class);
                String idKey = String.valueOf(p.getId());
                Map<String, Boolean> storedStates = statesMap.get(idKey);
                
                for (User.Position pos : User.Position.values()) {
                    Boolean val = storedStates != null ? storedStates.get(pos.name()) : false;
                    states.put(pos, val != null ? val : false);
                }
                snapshot.getPermissions().put(p.getId(), states);
                perms.add(p);
            }
        }
        snapshot.setPermissionList(perms);
        System.out.println("[PermissionService] Final permissionList size: " + perms.size());
        return snapshot;
    }

    public static void setOriginalSnapshot(PermissionSnapshot snapshot) {
        originalSnapshot = snapshot;
    }

    public static PermissionSnapshot getOriginalSnapshot() {
        return originalSnapshot;
    }

    public static boolean savePermissionChanges(Map<Long, Map<User.Position, Boolean>> changes) {
        return true;
    }

    public static Map<Long, Map<User.Position, Boolean>> getChanges(Map<Long, Map<User.Position, Boolean>> changes) {
        return changes;
    }
}
