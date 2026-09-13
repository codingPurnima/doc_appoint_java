package com.docappoint.service;

import com.docappoint.dto.BreakInterval;
import com.docappoint.dto.SlotGenerateRequest;
import com.docappoint.entity.Role;
import com.docappoint.entity.Slot;
import com.docappoint.entity.Status;
import com.docappoint.entity.User;
import com.docappoint.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;

    public SlotService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Transactional
    public List<Slot> generateSlots(User doctor, SlotGenerateRequest request) {
        if (doctor.getRole() != Role.doctor) {
            throw new SecurityException("Only doctors are permitted to generate slots");
        }

        if (!request.dayStart().isBefore(request.dayEnd())) {
            throw new IllegalArgumentException("day_start must be before day_end");
        }

        if (request.slotDurationMinutes() == null || request.slotDurationMinutes() <= 0) {
            throw new IllegalArgumentException("slot_duration_minutes must be greater than 0");
        }

        LocalDate today = LocalDate.now();
        if (request.date().isBefore(today)) {
            throw new IllegalArgumentException("Cannot generate slots for past dates");
        }

        LocalTime now = LocalTime.now();
        boolean isToday = request.date().isEqual(today);
        LocalTime currentStart = request.dayStart();

        if (isToday && currentStart.isBefore(now)) {
            currentStart = now;
        }

        if (currentStart.isAfter(request.dayEnd()) || currentStart.equals(request.dayEnd())) {
            return List.of();
        }

        List<Slot> existingSlots = slotRepository.findByDoctorAndDateOrderByStartTimeAsc(doctor, request.date());

        List<Slot> createdSlots = new ArrayList<>();
        
        while (true) {
            LocalTime currentEnd = currentStart.plusMinutes(request.slotDurationMinutes());

            // Stop if the slot end exceeds day_end or rolled over past midnight
            if (currentEnd.isAfter(request.dayEnd()) || currentEnd.isBefore(currentStart)) {
                break;
            }

            // Handle break intervals: if candidate slot overlaps a break,
            // advance currentStart directly to the end of the break interval
            boolean breakOverlap = false;
            if (request.breaks() != null) {
                for (BreakInterval b : request.breaks()) {
                    if (currentStart.isBefore(b.end()) && currentEnd.isAfter(b.start())) {
                        currentStart = b.end();
                        breakOverlap = true;
                        break;
                    }
                }
            }
            if (breakOverlap) {
                continue;
            }

            // Preserve overlap detection with existing slots
            boolean overlapsExisting = false;
            for (Slot existing : existingSlots) {
                if (currentStart.isBefore(existing.getEndTime()) && currentEnd.isAfter(existing.getStartTime())) {
                    overlapsExisting = true;
                    break;
                }
            }

            if (!overlapsExisting) {
                Slot slot = new Slot(doctor, request.date(), currentStart, currentEnd, Status.available);
                createdSlots.add(slot);
            }

            currentStart = currentEnd;
        }

        return slotRepository.saveAll(createdSlots);
    }

    public List<Slot> getDoctorSlots(User doctor, LocalDate date) {
        return slotRepository.findByDoctorAndDateOrderByStartTimeAsc(doctor, date);
    }

    public List<Slot> getAvailableSlots(LocalDate date) {
        return slotRepository.findByDateAndStatusOrderByStartTimeAsc(date, Status.available);
    }

    @Transactional
    public Slot toggleFreezeSlot(User doctor, Integer slotId) {
        Slot slot = slotRepository.findByIdAndDoctor(slotId, doctor)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found or does not belong to this doctor"));

        if (slot.getStatus() == Status.booked) {
            throw new IllegalStateException("A booked slot cannot be frozen");
        }
        if (slot.getStatus() == Status.completed) {
            throw new IllegalStateException("A completed slot cannot be toggled");
        }

        if (slot.getStatus() == Status.available) {
            slot.setStatus(Status.frozen);
        } else if (slot.getStatus() == Status.frozen) {
            slot.setStatus(Status.available);
        } else {
            throw new IllegalStateException("Slot in status " + slot.getStatus() + " cannot be toggled");
        }

        return slotRepository.save(slot);
    }
}
