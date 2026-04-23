package com.mycompany.reservationsystem.Controller;

import com.mycompany.reservationsystem.model.Permission;
import com.mycompany.reservationsystem.model.User;
import com.mycompany.reservationsystem.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/all")
    public List<Permission> getAllPermissions() {
        return permissionService.findAllPermissions();
    }
    
    @PostMapping("/snapshot")
    public Map<String, Object> getPermissionSnapshot() {
        PermissionService.PermissionSnapshot snapshot = permissionService.getPermissionSnapshot();
        return Map.of(
            "permissions", snapshot.permissions(),
            "states", snapshot.statesByPermissionId()
        );
    }

    @PostMapping("/get")
    public ResponseEntity<Permission> getPermissionById(@RequestBody IdRequest request) {
        return permissionService.findAllPermissions().stream()
                .filter(p -> p.getId().equals(request.getId()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePermission(@PathVariable Long id, @RequestBody Map<String, Object> permissionData) {
        try {
            String positionStr = (String) permissionData.get("position");
            Boolean enabled = (Boolean) permissionData.get("enabled");
            
            if (positionStr == null || enabled == null) {
                return ResponseEntity.badRequest().<Void>build();
            }
            
            User.Position position = User.Position.valueOf(positionStr);
            permissionService.updatePermission(position, id, enabled);
            return ResponseEntity.ok().<Void>build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().<Void>build();
        }
    }

    public static class IdRequest {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }
}
