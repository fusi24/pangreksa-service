package com.pangreksa.service.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hr_tax_brackets", schema = "public")
@Getter
@Setter
public class HrTaxBracket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;

    private BigDecimal minIncome;

    private BigDecimal maxIncome;

    private BigDecimal taxRate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
