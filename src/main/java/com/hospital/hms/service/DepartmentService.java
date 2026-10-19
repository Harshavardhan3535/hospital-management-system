package com.hospital.hms.service;

import com.hospital.hms.dto.DepartmentDTO;
import com.hospital.hms.entity.Department;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    // ===== CREATE =====
    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Department already exists: " + dto.getName());
        }

        Department dept = Department.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();

        Department saved = departmentRepository.save(dept);
        return mapToDTO(saved);
    }

    // ===== READ ALL =====
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== READ ONE =====
    public DepartmentDTO getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToDTO(dept);
    }

    // ===== UPDATE =====
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());

        Department updated = departmentRepository.save(dept);
        return mapToDTO(updated);
    }

    // ===== DELETE =====
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }

    // ===== HELPER =====
    private DepartmentDTO mapToDTO(Department dept) {
        return DepartmentDTO.builder()
                .id(dept.getId())
                .name(dept.getName())
                .description(dept.getDescription())
                .build();
    }
}