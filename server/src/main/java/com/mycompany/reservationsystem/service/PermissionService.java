package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.model.Permission;
import com.mycompany.reservationsystem.model.PositionPermission;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.repository.PermissionRepository;
import com.mycompany.reservationsystem.repository.PositionPermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class PermissionService {
    
    public record PermissionChange(User.Position position, Long permissionId, boolean enabled) {}

    @Autowired
    private PermissionRepository permissionRepo;

    @Autowired
    private PositionPermissionRepository positionPermissionRepo;

    private PermissionSnapshot cachedPermissionSnapshot;
    private PermissionSnapshot originalSnapshot;

    /**
     * Check if a given user has the given permission code
     */
    public boolean hasPermission(User user, String permissionCode) {
        if (user == null || user.getPosition() == null) return false;

        Permission permission = permissionRepo.findByCode(permissionCode).orElse(null);
        if (permission == null) return false;

        return positionPermissionRepo.existsByPositionAndPermission(user.getPosition(), permission);
    }

    public boolean hasPermission(User.Position position, Permission permission) {
        return positionPermissionRepo
                .existsByPositionAndPermission(position, permission);
    }

    public List<Permission> findAllPermissions() {
        return permissionRepo.findAll();
    }

    public synchronized PermissionSnapshot getPermissionSnapshot() {
        if (cachedPermissionSnapshot == null) {
            cachedPermissionSnapshot = loadPermissionSnapshot();
        }
        return cachedPermissionSnapshot.copy();
    }

    @Transactional
    public void updatePermission(User.Position position, Long permissionId, boolean enabled) {
        Permission permission = permissionRepo.findById(permissionId).orElse(null);
        if (permission == null) return;

        if (enabled) {
            grant(position, permission);   // use your existing grant() method
        } else {
            revoke(position, permission);  // use your existing revoke() method
        }

        updateCachedPermissionState(permissionId, position, enabled);
    }

    @Transactional
    public void savePermissionChanges(List<PermissionChange> changes) {
        for (PermissionChange change : changes) {
            Permission permission = permissionRepo.findById(change.permissionId()).orElse(null);
            if (permission == null) continue;
            
            if (change.enabled()) {
                grant(change.position(), permission);
            } else {
                revoke(change.position(), permission);
            }
            
            updateCachedPermissionState(change.permissionId(), change.position(), change.enabled());
        }
    }

    public List<PermissionChange> getChanges(Map<Long, Map<User.Position, Boolean>> currentStates) {
        List<PermissionChange> changes = new ArrayList<>();
        
        if (originalSnapshot == null) {
            return changes;
        }
        
        Map<Long, Map<User.Position, Boolean>> originalStates = originalSnapshot.statesByPermissionId();
        
        for (Map.Entry<Long, Map<User.Position, Boolean>> entry : currentStates.entrySet()) {
            Long permissionId = entry.getKey();
            Map<User.Position, Boolean> currentPositionStates = entry.getValue();
            Map<User.Position, Boolean> originalPositionStates = originalStates.get(permissionId);
            
            if (originalPositionStates == null) continue;
            
            for (Map.Entry<User.Position, Boolean> posEntry : currentPositionStates.entrySet()) {
                User.Position position = posEntry.getKey();
                Boolean currentValue = posEntry.getValue();
                Boolean originalValue = originalPositionStates.get(position);
                
                if (!currentValue.equals(originalValue)) {
                    changes.add(new PermissionChange(position, permissionId, currentValue));
                }
            }
        }
        
        return changes;
    }
    
    public void setOriginalSnapshot(PermissionSnapshot snapshot) {
        this.originalSnapshot = snapshot;
    }
    
    public PermissionSnapshot getOriginalSnapshot() {
        return originalSnapshot;
    }

    // ---------------- WRITE ----------------

    public void grant(User.Position position, Permission permission) {
        if (!hasPermission(position, permission)) {
            positionPermissionRepo.save(
                    new PositionPermission(position, permission)
            );
        }
    }

    public void revoke(User.Position position, Permission permission) {
        positionPermissionRepo
                .findByPositionAndPermission(position, permission)
                .ifPresent(positionPermissionRepo::delete);
    }

    private PermissionSnapshot loadPermissionSnapshot() {
        List<Permission> permissions = permissionRepo.findAll();
        Map<Long, Map<User.Position, Boolean>> permissionStates = new HashMap<>();

        for (Permission permission : permissions) {
            Map<User.Position, Boolean> statesByPosition = new EnumMap<>(User.Position.class);
            for (User.Position position : User.Position.values()) {
                statesByPosition.put(position, hasPermission(position, permission));
            }
            permissionStates.put(permission.getId(), statesByPosition);
        }

        return new PermissionSnapshot(List.copyOf(permissions), permissionStates);
    }

    private synchronized void updateCachedPermissionState(Long permissionId, User.Position position, boolean enabled) {
        if (cachedPermissionSnapshot == null) {
            return;
        }

        Map<User.Position, Boolean> statesByPosition = cachedPermissionSnapshot.statesByPermissionId().get(permissionId);
        if (statesByPosition != null) {
            statesByPosition.put(position, enabled);
        }
    }

    public record PermissionSnapshot(
            List<Permission> permissions,
            Map<Long, Map<User.Position, Boolean>> statesByPermissionId
    ) {
        public PermissionSnapshot copy() {
            Map<Long, Map<User.Position, Boolean>> copiedStates = new HashMap<>();
            for (Map.Entry<Long, Map<User.Position, Boolean>> entry : statesByPermissionId.entrySet()) {
                copiedStates.put(entry.getKey(), new EnumMap<>(entry.getValue()));
            }
            return new PermissionSnapshot(List.copyOf(permissions), copiedStates);
        }
    }
}
