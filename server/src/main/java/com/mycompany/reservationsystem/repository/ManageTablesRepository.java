/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.repository;

import com.mycompany.reservationsystem.dto.ManageTablesDTO;
import com.mycompany.reservationsystem.dto.TableUsageReportDTO;
import com.mycompany.reservationsystem.model.ManageTables;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author formentera
 */
@Repository
public interface ManageTablesRepository extends JpaRepository<ManageTables,Long>{
    @Query("""
SELECT new com.mycompany.reservationsystem.dto.ManageTablesDTO(
    t.tableNo,
    t.status,
    t.capacity,
    c.name,
    r.pax,
    r.sales,
    t.location,
    r.prefer,
    r.status,
    r.reference,
    c.phone,
    c.email,
    r.reservationPendingtime,
    r.reservationConfirmtime,
    r.reservationSeatedtime,
    r.reservationCompletetime,
    t.id,
    t.tablestarttime,
    t.tableendtime,
    r.date
    )
    FROM ManageTables t
    LEFT JOIN t.reservations r
    LEFT JOIN r.customer c
    """)
    List<ManageTablesDTO> getManageTablesDTO();
    List<ManageTables> findByStatus(String status);
    Optional<ManageTables> findByTableNo(String TableNo);
    
    long countByStatus(String status);
    boolean existsByTableNo(String TableNo);

}





