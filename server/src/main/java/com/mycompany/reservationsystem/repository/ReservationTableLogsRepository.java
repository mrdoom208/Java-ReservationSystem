/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.repository;


import com.mycompany.reservationsystem.dto.TableUsageInformationDTO;
import com.mycompany.reservationsystem.dto.TableUsageReportDTO;
import com.mycompany.reservationsystem.model.ReservationTableLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author formentera
 */
public interface ReservationTableLogsRepository extends JpaRepository<ReservationTableLogs,Long>{

    List<ReservationTableLogs> findByDateBetween(LocalDate start, LocalDate end);


    @Query("""
    SELECT new com.mycompany.reservationsystem.dto.TableUsageReportDTO(
        r.tableNo,
        COUNT(r.id),
        COALESCE(SUM(r.pax), 0),
        COALESCE(SUM(r.Sales), 0)
    )
    FROM ReservationTableLogs r
    WHERE (:from IS NULL OR r.date >= :from)
      AND (:to IS NULL OR r.date <= :to)
    GROUP BY r.tableNo
    ORDER BY r.tableNo
    """)
    List<TableUsageReportDTO> getTableUsageReport(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT new com.mycompany.reservationsystem.dto.TableUsageInformationDTO(
            r.tableNo,
            r.reference,
            r.pax,
            r.Sales,
            r.tablestarttime,
            r.date
        )
        FROM ReservationTableLogs r
        WHERE r.date BETWEEN :dateFrom AND :dateTo
          AND (:tableNo IS NULL OR r.tableNo = :tableNo)
        ORDER BY r.date ASC, r.tablestarttime ASC
      """)
    List<TableUsageInformationDTO> getTableUsageInfo(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("tableNo") String tableNo
    );

}
