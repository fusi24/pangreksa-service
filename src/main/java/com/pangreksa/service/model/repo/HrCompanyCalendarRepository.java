package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrCompanyCalendar;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HrCompanyCalendarRepository extends JpaRepository<HrCompanyCalendar, Long> {
    // find all calendars for a specific year and active true
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    List<HrCompanyCalendar> findAllByYearAndIsActiveTrueOrderByStartDateAsc(Integer year);
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    List<HrCompanyCalendar> findAllByYearOrderByStartDateAsc(Integer year);

    // NEW METHOD:
    Optional<HrCompanyCalendar> findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate startDate,
            LocalDate endDate
    );
}
