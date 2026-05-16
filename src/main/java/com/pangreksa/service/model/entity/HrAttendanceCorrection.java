package com.pangreksa.service.model.entity;

import com.pangreksa.service.model.enumerate.AttendanceCorrectionStatusEnum;
import com.pangreksa.service.shared.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hr_attendance_correction", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrAttendanceCorrection extends AuditableEntity<HrAttendanceCorrection> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_hr_seq")
    @SequenceGenerator(name = "global_hr_seq", sequenceName = "global_hr_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_id")
    private HrAttendance attendance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private HrPerson employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private HrCompany company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_to")
    private HrPerson submittedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private HrPerson approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AttendanceCorrectionStatusEnum status = AttendanceCorrectionStatusEnum.SUBMITTED;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "original_attendance_date")
    private LocalDate originalAttendanceDate;

    @Column(name = "requested_attendance_date")
    private LocalDate requestedAttendanceDate;

    @Column(name = "original_check_in")
    private LocalDateTime originalCheckIn;

    @Column(name = "requested_check_in")
    private LocalDateTime requestedCheckIn;

    @Column(name = "original_check_out")
    private LocalDateTime originalCheckOut;

    @Column(name = "requested_check_out")
    private LocalDateTime requestedCheckOut;

    @PrePersist
    public void prePersist() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AttendanceCorrectionStatusEnum.SUBMITTED;
        }
    }
}
