package com.docappoint.controller;

import com.docappoint.dto.SlotGenerateRequest;
import com.docappoint.entity.Slot;
import com.docappoint.entity.User;
import com.docappoint.service.AuthService;
import com.docappoint.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/slots")
public class SlotController {

    private final SlotService slotService;
    private final AuthService authService;

    public SlotController(SlotService slotService, AuthService authService) {
        this.slotService = slotService;
        this.authService = authService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Slot>> generateSlots(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SlotGenerateRequest request) {
        User currentUser = getAuthenticatedUser(authHeader);
        return ResponseEntity.ok(slotService.generateSlots(currentUser, request));
    }

    @GetMapping
    public ResponseEntity<List<Slot>> getDoctorSlots(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        User currentUser = getAuthenticatedUser(authHeader);
        return ResponseEntity.ok(slotService.getDoctorSlots(currentUser, date));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Slot>> getAvailableSlots(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.getAvailableSlots(date));
    }

    @PatchMapping("/{slot_id}/freeze")
    public ResponseEntity<Slot> toggleFreezeSlot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("slot_id") Integer slotId) {
        User currentUser = getAuthenticatedUser(authHeader);
        return ResponseEntity.ok(slotService.toggleFreezeSlot(currentUser, slotId));
    }

    private User getAuthenticatedUser(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new SecurityException("Missing Authorization header");
        }
        return authService.getUserFromToken(authHeader);
    }
}
