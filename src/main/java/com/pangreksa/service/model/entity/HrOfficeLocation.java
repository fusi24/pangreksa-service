package com.pangreksa.service.model.entity;
import com.pangreksa.service.shared.AuditableEntity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "hr_office_location", schema = "public")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrOfficeLocation extends AuditableEntity<HrOfficeLocation> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_hr_seq")
    @SequenceGenerator(name = "global_hr_seq", sequenceName = "global_hr_seq", allocationSize = 1)
    private Long id;

    private String name;

    @Column(columnDefinition = "geometry")
    private Geometry geometry;

    private String description;

    private Boolean isActive;

    private Double buffer;

    // BE-merged field
    @Column(name = "branch_office_id")
    private Long branchOfficeId;
}
