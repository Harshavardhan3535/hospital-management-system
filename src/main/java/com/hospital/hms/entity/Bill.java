package com.hospital.hms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ONE bill → ONE appointment
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
//    private Appointment appointment;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillStatus status;

    @Column(name = "generated_date", nullable = false)
    private LocalDate generatedDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    public enum BillStatus {
        PENDING, PAID, CANCELLED
    }
}