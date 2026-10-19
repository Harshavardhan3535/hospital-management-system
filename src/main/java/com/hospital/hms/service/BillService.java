package com.hospital.hms.service;

import com.hospital.hms.dto.BillDTO;
import com.hospital.hms.entity.Appointment;
import com.hospital.hms.entity.Bill;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.AppointmentRepository;
import com.hospital.hms.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // ===== CREATE =====
    public BillDTO createBill(BillDTO dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + dto.getAppointmentId()));

        // Business rule: one bill per appointment
        if (billRepository.findByAppointmentId(appointment.getId()).isPresent()) {
            throw new RuntimeException("Bill already exists for appointment id: " + appointment.getId());
        }

        Bill.BillStatus status = Bill.BillStatus.PENDING;
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                status = Bill.BillStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + dto.getStatus()
                        + ". Allowed: PENDING, PAID, CANCELLED");
            }
        }

        Bill bill = Bill.builder()
                .appointment(appointment)
                .amount(dto.getAmount())
                .status(status)
                .generatedDate(dto.getGeneratedDate() != null ? dto.getGeneratedDate() : LocalDate.now())
                .paidDate(dto.getPaidDate())
                .build();

        Bill saved = billRepository.save(bill);
        return mapToDTO(saved);
    }

    // ===== READ ALL =====
    public List<BillDTO> getAllBills() {
        return billRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== READ ONE =====
    public BillDTO getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
        return mapToDTO(bill);
    }

    // ===== READ BY APPOINTMENT =====
    public BillDTO getBillByAppointment(Long appointmentId) {
        Bill bill = billRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill not found for appointment id: " + appointmentId));
        return mapToDTO(bill);
    }

    // ===== READ BY STATUS =====
    public List<BillDTO> getBillsByStatus(String status) {
        Bill.BillStatus billStatus;
        try {
            billStatus = Bill.BillStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status
                    + ". Allowed: PENDING, PAID, CANCELLED");
        }
        return billRepository.findByStatus(billStatus)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== UPDATE =====
    public BillDTO updateBill(Long id, BillDTO dto) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));

        if (dto.getAmount() != null) bill.setAmount(dto.getAmount());

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                Bill.BillStatus status = Bill.BillStatus.valueOf(dto.getStatus().toUpperCase());
                bill.setStatus(status);
                if (status == Bill.BillStatus.PAID && bill.getPaidDate() == null) {
                    bill.setPaidDate(LocalDate.now());
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + dto.getStatus());
            }
        }

        if (dto.getPaidDate() != null) bill.setPaidDate(dto.getPaidDate());

        return mapToDTO(billRepository.save(bill));
    }

    // ===== MARK AS PAID =====
    public BillDTO markAsPaid(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));

        if (bill.getStatus() == Bill.BillStatus.PAID) {
            throw new RuntimeException("Bill is already paid");
        }

        bill.setStatus(Bill.BillStatus.PAID);
        bill.setPaidDate(LocalDate.now());
        return mapToDTO(billRepository.save(bill));
    }

    // ===== DELETE =====
    public void deleteBill(Long id) {
        if (!billRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bill not found with id: " + id);
        }
        billRepository.deleteById(id);
    }

    // ===== HELPER =====
    private BillDTO mapToDTO(Bill bill) {
        return BillDTO.builder()
                .id(bill.getId())
                .appointmentId(bill.getAppointment().getId())
                .amount(bill.getAmount())
                .status(bill.getStatus().name())
                .generatedDate(bill.getGeneratedDate())
                .paidDate(bill.getPaidDate())
                .patientName(bill.getAppointment().getPatient().getName())
                .doctorName(bill.getAppointment().getDoctor().getName())
                .build();
    }
}