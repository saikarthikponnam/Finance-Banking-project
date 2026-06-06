package com.hackathon.financebanking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String merchant;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String status; // APPROVED, FLAGGED, BLOCKED

    private Integer riskScore;

    @Column(length = 500)
    private String failureReason;
}
