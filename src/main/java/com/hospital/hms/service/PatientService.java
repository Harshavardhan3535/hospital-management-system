package com.hospital.hms.service;

import com.hospital.hms.dto.PatientDTO;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    // ===== CREATE =====
    public PatientDTO createPatient(PatientDTO dto) {
        if (patientRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Patient with email already exists: " + dto.getEmail());
        }

        Patient patient = Patient.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .dob(dto.getDob())
                .address(dto.getAddress())
                .bloodGroup(dto.getBloodGroup())
                .build();

        Patient saved = patientRepository.save(patient);
        return mapToDTO(saved);
    }

    // ===== READ ALL =====
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== READ ONE =====
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return mapToDTO(patient);
    }

    // ===== UPDATE =====
    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setPhone(dto.getPhone());
        patient.setDob(dto.getDob());
        patient.setAddress(dto.getAddress());
        patient.setBloodGroup(dto.getBloodGroup());

        Patient updated = patientRepository.save(patient);
        return mapToDTO(updated);
    }

    // ===== DELETE =====
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(id);
    }

    // ===== HELPER: Entity → DTO =====
    private PatientDTO mapToDTO(Patient patient) {
        return PatientDTO.builder()
                .id(patient.getId())
                .name(patient.getName())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .dob(patient.getDob())
                .address(patient.getAddress())
                .bloodGroup(patient.getBloodGroup())
                .build();
    }
}