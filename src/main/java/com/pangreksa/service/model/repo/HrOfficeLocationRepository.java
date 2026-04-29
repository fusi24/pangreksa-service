package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrOfficeLocation;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface HrOfficeLocationRepository extends CrudRepository<HrOfficeLocation, Long>, JpaSpecificationExecutor<HrOfficeLocation> {

    // BE-merged: PostGIS-backed geofence lookup used by mobile attendance
    @Query(value = """
        SELECT hol.*
        FROM hr_office_location hol
        WHERE hol.is_active = true
          AND hol.branch_office_id = ANY(:branchIds)
          AND ST_DWithin(
                hol.geometry::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                COALESCE(hol.buffer, 0)::double precision
          )
        ORDER BY COALESCE(hol.buffer, 0) ASC
        LIMIT 1
        """, nativeQuery = true)
    HrOfficeLocation findMatchedOffice(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("branchIds") Long[] branchIds
    );
}
