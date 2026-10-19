package com.hospital.hms.service;

import com.hospital.hms.dto.MedicalRecordDTO;
import com.hospital.hms.entity.Doctor;
import com.hospital.hms.entity.MedicalRecord;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.MedicalRecordRepository;
import com.hospital.hms.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // ===== CREATE =====
    public MedicalRecordDTO createRecord(MedicalRecordDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + dto.getPatientId()));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + dto.getDoctorId()));

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .diagnosis(dto.getDiagnosis())
                .treatment(dto.getTreatment())
                .recordDate(dto.getRecordDate())
                .build();

        MedicalRecord saved = medicalRecordRepository.save(record);
        return mapToDTO(saved);
    }

    // ===== READ ALL =====
    public List<MedicalRecordDTO> getAllRecords() {
        return medicalRecordRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== READ ONE =====
    public MedicalRecordDTO getRecordById(Long id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord not found with id: " + id));
        return mapToDTO(record);
    }

    // ===== READ BY PATIENT =====
    public List<MedicalRecordDTO> getRecordsByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
        return medicalRecordRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== READ BY DOCTOR =====
    public List<MedicalRecordDTO> getRecordsByDoctor(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
        }
        return medicalRecordRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== UPDATE =====
    public MedicalRecordDTO updateRecord(Long id, MedicalRecordDTO dto) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord not found with id: " + id));

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + dto.getPatientId()));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + dto.getDoctorId()));

        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setDiagnosis(dto.getDiagnosis());
        record.setTreatment(dto.getTreatment());
        record.setRecordDate(dto.getRecordDate());

        MedicalRecord updated = medicalRecordRepository.save(record);
        return mapToDTO(updated);
    }

    // ===== DELETE =====
    public void deleteRecord(Long id) {
        if (!medicalRecordRepository.existsById(id)) {
            throw new ResourceNotFoundException("MedicalRecord not found with id: " + id);
        }
        medicalRecordRepository.deleteById(id);
    }

    // ===== HELPER =====
    private MedicalRecordDTO mapToDTO(MedicalRecord record) {
        return MedicalRecordDTO.builder()
                .id(record.getId())
                .patientId(record.getPatient().getId())
                .doctorId(record.getDoctor().getId())
                .patientName(record.getPatient().getName())
                .doctorName(record.getDoctor().getName())
                .diagnosis(record.getDiagnosis())
                .treatment(record.getTreatment())
                .recordDate(record.getRecordDate())
                .build();
    }
}