package com.pangreksa.service.service;

import com.pangreksa.service.model.entity.*;
import com.pangreksa.service.model.repo.HrAttendanceRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HrAttendanceRepository attendanceRepository;

    public AttendanceExportService(HrAttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public byte[] exportAttendanceRecapCsv(LocalDate startDate,
                                           LocalDate endDate,
                                           HrCompany company,
                                           HrOrgStructure orgStructure,
                                           HrPerson employee) {
        Specification<HrAttendance> spec = buildSpec(startDate, endDate, company, orgStructure, employee);
        List<HrAttendance> rows = attendanceRepository.findAll(spec, Pageable.unpaged()).getContent();

        StringBuilder csv = new StringBuilder();
        csv.append("attendance_date,employee_name,status,check_in,check_out,total_work_minutes,branch_name\n");
        for (HrAttendance row : rows) {
            String fullName = buildName(row.getPerson());
            csv.append(formatDate(row.getAttendanceDate())).append(",")
                    .append(escapeCsv(fullName)).append(",")
                    .append(escapeCsv(row.getStatus())).append(",")
                    .append(formatDateTime(row.getCheckIn())).append(",")
                    .append(formatDateTime(row.getCheckOut())).append(",")
                    .append(row.getTotalWorkMinutes() == null ? "" : row.getTotalWorkMinutes()).append(",")
                    .append(escapeCsv(row.getBranchName()))
                    .append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Specification<HrAttendance> buildSpec(LocalDate startDate,
                                                  LocalDate endDate,
                                                  HrCompany company,
                                                  HrOrgStructure orgStructure,
                                                  HrPerson employee) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("attendanceDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("attendanceDate"), endDate));
            }
            if (employee != null) {
                predicates.add(cb.equal(root.get("person"), employee));
            }
            if (company != null || orgStructure != null) {
                Join<HrAttendance, HrPerson> personJoin = root.join("person");
                Join<HrPerson, HrPersonPosition> personPositionJoin = personJoin.join("personPosition");
                if (company != null) {
                    predicates.add(cb.equal(personPositionJoin.get("company"), company));
                }
                if (orgStructure != null) {
                    Join<HrPersonPosition, HrPosition> positionJoin = personPositionJoin.join("position");
                    predicates.add(cb.equal(positionJoin.get("orgStructure"), orgStructure));
                }
            }

            query.orderBy(cb.asc(root.get("attendanceDate")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String buildName(HrPerson person) {
        if (person == null) return "";
        String first = person.getFirstName() == null ? "" : person.getFirstName().trim();
        String last = person.getLastName() == null ? "" : person.getLastName().trim();
        return (first + " " + last).trim();
    }

    private String formatDate(LocalDate value) {
        return value == null ? "" : value.format(DATE_FORMAT);
    }

    private String formatDateTime(java.time.LocalDateTime value) {
        return value == null ? "" : value.format(DATETIME_FORMAT);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
