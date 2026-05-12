package com.pangreksa.service.model.entity;
import com.pangreksa.service.shared.AuditableEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hr_address", schema = "public")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPersonAddress extends AuditableEntity<HrPersonAddress>{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_hr_seq")
    @SequenceGenerator(name = "global_hr_seq", sequenceName = "global_hr_seq", allocationSize = 1)
    private Long id;

    @Column(name = "full_address", length = 500, nullable = false)
    private String fullAddress;

    @Column(name = "is_default")
    private Boolean isDefault;

    @ManyToOne
    @JoinColumn(name = "reference_id", nullable = false)
    private HrPerson person;
}

