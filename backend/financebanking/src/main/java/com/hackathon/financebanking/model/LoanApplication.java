package com.hackathon.financebanking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    private BigDecimal dtiRatio;

    @Column(nullable = false)
    private String status; // APPROVED, REJECTED

    @Column(length = 1000)
    private String analysisReport;

    private LocalDateTime applicationDate;
}
