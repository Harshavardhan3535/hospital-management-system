package com.hospital.hms.controller;

import com.hospital.hms.dto.BillDTO;
import com.hospital.hms.service.BillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;

    // Admin or Receptionist can generate bills
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<BillDTO> create(@Valid @RequestBody BillDTO dto) {
        return new ResponseEntity<>(billService.createBill(dto), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BillDTO>> getAll() {
        return ResponseEntity.ok(billService.getAllBills());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BillDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BillDTO> getByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(billService.getBillByAppointment(appointmentId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BillDTO>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(billService.getBillsByStatus(status));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<BillDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody BillDTO dto) {
        return ResponseEntity.ok(billService.updateBill(id, dto));
    }

    // Only ADMIN can mark as paid
    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillDTO> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(billService.markAsPaid(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.noContent().build();
    }
}