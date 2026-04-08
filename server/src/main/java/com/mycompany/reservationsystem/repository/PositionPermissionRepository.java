package com.mycompany.reservationsystem.repository;

import com.mycompany.reservationsystem.model.Permission;
import com.mycompany.reservationsystem.model.PositionPermission;
import com.mycompany.reservationsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionPermissionRepository extends JpaRepository<PositionPermission, Long> {

    boolean existsByPositionAndPermission(User.Position position, Permission permission);

    Optional<PositionPermission> findByPositionAndPermission(User.Position position, Permission permission);


}
