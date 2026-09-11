package com.docappoint.repository;

import com.docappoint.entity.Slot;
import com.docappoint.entity.Status;
import com.docappoint.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Integer> {

    // Find slots by doctor and date ordered by start time
    List<Slot> findByDoctorAndDateOrderByStartTimeAsc(User doctor, LocalDate date);

    List<Slot> findByDoctorIdAndDateOrderByStartTimeAsc(Integer doctorId, LocalDate date);

    // Find available slots by date
    List<Slot> findByDateAndStatusOrderByStartTimeAsc(LocalDate date, Status status);

    @Query("SELECT s FROM Slot s WHERE s.date = :date AND s.status = com.docappoint.entity.Status.available ORDER BY s.startTime ASC")
    List<Slot> findAvailableSlotsByDate(@Param("date") LocalDate date);

    // Find a slot by id and doctor
    Optional<Slot> findByIdAndDoctor(Integer id, User doctor);

    Optional<Slot> findByIdAndDoctorId(Integer id, Integer doctorId);

    // Support the appointment booking workflow where a slot needs to be locked for update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Integer id);
}
