package com.mycompany.reservationsystem.repository;
import com.mycompany.reservationsystem.dto.CustomerReportDTO;
import com.mycompany.reservationsystem.dto.ReservationCustomerDTO;
import com.mycompany.reservationsystem.dto.SalesReportsDTO;
import com.mycompany.reservationsystem.dto.TableUsageReportDTO;
import com.mycompany.reservationsystem.model.Reservation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    long countByStatus(String Status);
    List<Reservation> findTop15ByOrderByDateDescReservationPendingtimeDesc(Pageable pageable);
    List<Reservation> findByStatus(String status);
    List<Reservation> findByStatusIn(List<String> statuses);
    List<Reservation> findAllByStatus(String status);

    default List<Reservation> findActiveReservations() {
        return findByStatusIn(List.of("Pending", "Confirmed", "Seated"));
    }

    boolean existsByTable_Id(Long tableId);
    List<Reservation> findByTable_Id(Long tableId);
    List<Reservation> findByTableId(Long tableId);
    Optional<Reservation> findByReference(String reference);
    List<Reservation> findByDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT r FROM Reservation r JOIN r.customer c WHERE c.phone = :phone AND r.reference = :reference")
    Optional<Reservation> findByCustomerPhoneAndReference(@Param("phone") String phone, @Param("reference") String reference);

    @Query("SELECT r FROM Reservation r JOIN r.customer c WHERE c.phone = :phone ORDER BY r.date DESC")
    List<Reservation> findByCustomerPhoneOrderByDateDesc(@Param("phone") String phone);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = 'Pending' AND r.reservationPendingtime < :time")
    long countAhead(@Param("time") LocalTime time);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reservation r WHERE r.date < :date")
    void deleteByDateBefore(@Param("date") LocalDate date);

    default List<Reservation> findByDate(LocalDate date) {
        return findByDateBetween(date, date);
    }

    @Query("SELECT new com.mycompany.reservationsystem.dto.ReservationCustomerDTO("
            + "r.id, r.pax, r.prefer, r.status, r.reference, r.date, r.sales, "
            + "r.reservationPendingtime, r.reservationConfirmtime, r.reservationCancelledtime, "
            + "r.reservationSeatedtime, r.reservationCompletetime, "
            + "c.id, c.name, c.phone, c.email) "
            + "FROM Reservation r JOIN r.customer c "
            + "WHERE c.phone = :phone")
    List<ReservationCustomerDTO> getReservationCustomerDTOByPhone(@Param("phone") String phone);

    @Query("SELECT new com.mycompany.reservationsystem.dto.ReservationCustomerDTO("
            + "r.id, r.pax, r.prefer, r.status, r.reference, r.date, r.sales, "
            + "r.reservationPendingtime, r.reservationConfirmtime, r.reservationCancelledtime, "
            + "r.reservationSeatedtime, r.reservationCompletetime, "
            + "c.id, c.name, c.phone, c.email) "
            + "FROM Reservation r JOIN r.customer c "
            + "WHERE c.phone = :phone AND r.date >= :from")
    List<ReservationCustomerDTO> getReservationCustomerDTOByPhoneFromDate(
            @Param("phone") String phone, @Param("from") LocalDate from);

    @Query("SELECT new com.mycompany.reservationsystem.dto.ReservationCustomerDTO("
            + "r.id, r.pax, r.prefer, r.status, r.reference, r.date, r.sales, "
            + "r.reservationPendingtime, r.reservationConfirmtime, r.reservationCancelledtime, "
            + "r.reservationSeatedtime, r.reservationCompletetime, "
            + "c.id, c.name, c.phone, c.email) "
            + "FROM Reservation r JOIN r.customer c "
            + "WHERE c.phone = :phone AND r.date <= :to")
    List<ReservationCustomerDTO> getReservationCustomerDTOByPhoneToDate(
            @Param("phone") String phone, @Param("to") LocalDate to);

    @Query("SELECT new com.mycompany.reservationsystem.dto.ReservationCustomerDTO("
            + "r.id, r.pax, r.prefer, r.status, r.reference, r.date, r.sales, "
            + "r.reservationPendingtime, r.reservationConfirmtime, r.reservationCancelledtime, "
            + "r.reservationSeatedtime, r.reservationCompletetime, "
            + "c.id, c.name, c.phone, c.email) "
            + "FROM Reservation r JOIN r.customer c "
            + "WHERE c.phone = :phone AND r.date >= :from AND r.date <= :to")
    List<ReservationCustomerDTO> getReservationCustomerDTOByPhoneAndDateBetween(
            @Param("phone") String phone,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    default List<ReservationCustomerDTO> getReservationCustomerDTOByPhoneAndDate(
            String phone, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return getReservationCustomerDTOByPhone(phone);
        }
        if (from == null) {
            return getReservationCustomerDTOByPhoneToDate(phone, to);
        }
        if (to == null) {
            return getReservationCustomerDTOByPhoneFromDate(phone, from);
        }
        return getReservationCustomerDTOByPhoneAndDateBetween(phone, from, to);
    }

    @Query("SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO("
            + "c.phone, COUNT(r.id), SUM(r.sales), AVG(r.sales)) "
            + "FROM Reservation r JOIN r.customer c GROUP BY c.phone")
    List<CustomerReportDTO> getAllCustomerReport(Pageable pageable);

    @Query("SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO("
            + "c.phone, COUNT(r.id), SUM(r.sales), AVG(r.sales)) "
            + "FROM Reservation r JOIN r.customer c WHERE r.date >= :from GROUP BY c.phone")
    List<CustomerReportDTO> getFilteredCustomerReportFrom(@Param("from") LocalDate from);

    @Query("SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO("
            + "c.phone, COUNT(r.id), SUM(r.sales), AVG(r.sales)) "
            + "FROM Reservation r JOIN r.customer c WHERE r.date <= :to GROUP BY c.phone")
    List<CustomerReportDTO> getFilteredCustomerReportTo(@Param("to") LocalDate to);

    @Query("SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO("
            + "c.phone, COUNT(r.id), SUM(r.sales), AVG(r.sales)) "
            + "FROM Reservation r JOIN r.customer c WHERE r.date >= :from AND r.date <= :to GROUP BY c.phone")
    List<CustomerReportDTO> getFilteredCustomerReportBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    default List<CustomerReportDTO> getFilteredCustomerReport(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return getAllCustomerReport(Pageable.unpaged());
        }
        if (from == null) {
            return getFilteredCustomerReportTo(to);
        }
        if (to == null) {
            return getFilteredCustomerReportFrom(from);
        }
        return getFilteredCustomerReportBetween(from, to);
    }

    @Query("SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO("
            + "c.phone, COUNT(r.id), SUM(r.sales), AVG(r.sales)) "
            + "FROM Reservation r JOIN r.customer c GROUP BY c.phone")
    Page<CustomerReportDTO> getAllCustomerReportPaged(Pageable pageable);

    @Query("SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO("
            + "c.phone, COUNT(r.id), SUM(r.sales), AVG(r.sales)) "
            + "FROM Reservation r JOIN r.customer c WHERE r.date >= :from GROUP BY c.phone")
    Page<CustomerReportDTO> getFilteredCustomerReportFromPaged(@Param("from") LocalDate from, Pageable pageable);

    @Query("SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO("
            + "c.phone, COUNT(r.id), SUM(r.sales), AVG(r.sales)) "
            + "FROM Reservation r JOIN r.customer c WHERE r.date <= :to GROUP BY c.phone")
    Page<CustomerReportDTO> getFilteredCustomerReportToPaged(@Param("to") LocalDate to, Pageable pageable);

    @Query("SELECT new com.mycompany.reservationsystem.dto.CustomerReportDTO("
            + "c.phone, COUNT(r.id), SUM(r.sales), AVG(r.sales)) "
            + "FROM Reservation r JOIN r.customer c WHERE r.date >= :from AND r.date <= :to GROUP BY c.phone")
    Page<CustomerReportDTO> getFilteredCustomerReportBetweenPaged(@Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);

    default Page<CustomerReportDTO> getFilteredCustomerReportPaged(LocalDate from, LocalDate to, Pageable pageable) {
        if (from == null && to == null) {
            return getAllCustomerReportPaged(pageable);
        }
        if (from == null) {
            return getFilteredCustomerReportToPaged(to, pageable);
        }
        if (to == null) {
            return getFilteredCustomerReportFromPaged(from, pageable);
        }
        return getFilteredCustomerReportBetweenPaged(from, to, pageable);
    }

    @Query("SELECT DISTINCT c.phone FROM Customer c")
    List<String> findAllCustomerPhones();

    @Query("SELECT new com.mycompany.reservationsystem.dto.SalesReportsDTO("
            + "r.date, COUNT(r.id), SUM(r.pax), SUM(r.sales)) "
            + "FROM Reservation r GROUP BY r.date ORDER BY r.date ASC")
    List<SalesReportsDTO> getAllSalesReports();

    @Query("SELECT new com.mycompany.reservationsystem.dto.SalesReportsDTO("
            + "r.date, COUNT(r.id), SUM(r.pax), SUM(r.sales)) "
            + "FROM Reservation r WHERE r.date >= :from GROUP BY r.date ORDER BY r.date ASC")
    List<SalesReportsDTO> getSalesReportsFrom(@Param("from") LocalDate from);

    @Query("SELECT new com.mycompany.reservationsystem.dto.SalesReportsDTO("
            + "r.date, COUNT(r.id), SUM(r.pax), SUM(r.sales)) "
            + "FROM Reservation r WHERE r.date <= :to GROUP BY r.date ORDER BY r.date ASC")
    List<SalesReportsDTO> getSalesReportsTo(@Param("to") LocalDate to);

    @Query("SELECT new com.mycompany.reservationsystem.dto.SalesReportsDTO("
            + "r.date, COUNT(r.id), SUM(r.pax), SUM(r.sales)) "
            + "FROM Reservation r WHERE r.date >= :from AND r.date <= :to GROUP BY r.date ORDER BY r.date ASC")
    List<SalesReportsDTO> getSalesReportsBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    default List<SalesReportsDTO> getSalesReports(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return getAllSalesReports();
        }
        if (from == null) {
            return getSalesReportsTo(to);
        }
        if (to == null) {
            return getSalesReportsFrom(from);
        }
        return getSalesReportsBetween(from, to);
    }

    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.sales = :sales WHERE r.reference = :reference")
    int updateSalesByReference(@Param("reference") String reference, @Param("sales") BigDecimal sales);

    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.status = :status WHERE r.reference = :reference")
    int updateStatusByReference(@Param("reference") String reference, @Param("status") String status);

    @Query("SELECT r FROM Reservation r")
    Stream<Reservation> streamAllReservations();

    default List<Reservation> getAllReservationsList() {
        return findAll();
    }

    @Query("SELECT new com.mycompany.reservationsystem.dto.TableUsageReportDTO("
            + "t.tableNo, COUNT(r.id), SUM(r.pax), SUM(r.sales)) "
            + "FROM Reservation r JOIN r.table t WHERE r.date = :date GROUP BY t.tableNo")
    List<TableUsageReportDTO> getTableUsageReport(@Param("date") LocalDate date);

    @Query("SELECT new com.mycompany.reservationsystem.dto.TableUsageReportDTO("
            + "t.tableNo, COUNT(r.id), SUM(r.pax), SUM(r.sales)) "
            + "FROM Reservation r JOIN r.table t GROUP BY t.tableNo")
    List<TableUsageReportDTO> getAllTableUsageReport();

    @Query("SELECT new com.mycompany.reservationsystem.dto.TableUsageReportDTO("
            + "t.tableNo, COUNT(r.id), SUM(r.pax), SUM(r.sales)) "
            + "FROM Reservation r JOIN r.table t WHERE r.date >= :from AND r.date <= :to GROUP BY t.tableNo")
    List<TableUsageReportDTO> getTableUsageReportBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'Confirmed' AND r.reservationNotifiedtime IS NOT NULL "
            + "AND r.reservationConfirmtime IS NULL")
    List<Reservation> findConfirmedAwaitingArrival();

    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.status = 'Cancelled', r.reservationCancelledtime = :cancelTime "
            + "WHERE r.id = :id")
    void cancelReservation(@Param("id") Long id, @Param("cancelTime") LocalTime cancelTime);
}
