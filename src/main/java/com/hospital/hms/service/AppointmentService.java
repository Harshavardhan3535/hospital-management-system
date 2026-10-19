package com.hospital.hms.service;

import com.hospital.hms.dto.AppointmentDTO;
import com.hospital.hms.entity.Appointment;
import com.hospital.hms.entity.Doctor;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.AppointmentRepository;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    // ===== CREATE =====
    public AppointmentDTO createAppointment(AppointmentDTO dto) {
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + dto.getDoctorId()));

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + dto.getPatientId()));

        // Business rule: no double-booking for the same doctor/date/timeSlot
        List<Appointment> existing = appointmentRepository
                .findByDoctorIdAndAppointmentDate(doctor.getId(), dto.getAppointmentDate());
        boolean slotTaken = existing.stream()
                .anyMatch(a -> a.getTimeSlot().equalsIgnoreCase(dto.getTimeSlot())
                        && a.getStatus() != Appointment.AppointmentStatus.CANCELLED);
        if (slotTaken) {
            throw new RuntimeException("Time slot already booked for this doctor on "
                    + dto.getAppointmentDate() + " at " + dto.getTimeSlot());
        }

        Appointment.AppointmentStatus status = Appointment.AppointmentStatus.SCHEDULED;
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                status = Appointment.AppointmentStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + dto.getStatus()
                        + ". Allowed: SCHEDULED, COMPLETED, CANCELLED, NO_SHOW");
            }
        }

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(dto.getAppointmentDate())
                .timeSlot(dto.getTimeSlot())
                .status(status)
                .notes(dto.getNotes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return mapToDTO(saved);
    }

    // ===== READ ALL =====
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== READ ONE =====
    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return mapToDTO(appt);
    }

    // ===== READ BY PATIENT =====
    public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== READ BY DOCTOR =====
    public List<AppointmentDTO> getAppointmentsByDoctor(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
        }
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== READ BY DATE =====
    public List<AppointmentDTO> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDate(date)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== UPDATE =====
    public AppointmentDTO updateAppointment(Long id, AppointmentDTO dto) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + dto.getDoctorId()));

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + dto.getPatientId()));

        Appointment.AppointmentStatus status = appt.getStatus();
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                status = Appointment.AppointmentStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + dto.getStatus());
            }
        }

        appt.setDoctor(doctor);
        appt.setPatient(patient);
        appt.setAppointmentDate(dto.getAppointmentDate());
        appt.setTimeSlot(dto.getTimeSlot());
        appt.setStatus(status);
        appt.setNotes(dto.getNotes());

        Appointment updated = appointmentRepository.save(appt);
        return mapToDTO(updated);
    }

    // ===== UPDATE STATUS ONLY =====
    public AppointmentDTO updateStatus(Long id, String newStatus) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        try {
            appt.setStatus(Appointment.AppointmentStatus.valueOf(newStatus.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + newStatus
                    + ". Allowed: SCHEDULED, COMPLETED, CANCELLED, NO_SHOW");
        }

        return mapToDTO(appointmentRepository.save(appt));
    }

    // ===== DELETE =====
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Appointment not found with id: " + id);
        }
        appointmentRepository.deleteById(id);
    }

    // ===== HELPER =====
    private AppointmentDTO mapToDTO(Appointment appt) {
        return AppointmentDTO.builder()
                .id(appt.getId())
                .doctorId(appt.getDoctor().getId())
                .patientId(appt.getPatient().getId())
                .doctorName(appt.getDoctor().getName())
                .patientName(appt.getPatient().getName())
                .appointmentDate(appt.getAppointmentDate())
                .timeSlot(appt.getTimeSlot())
                .status(appt.getStatus().name())
                .notes(appt.getNotes())
                .createdAt(appt.getCreatedAt())
                .build();
    }
}