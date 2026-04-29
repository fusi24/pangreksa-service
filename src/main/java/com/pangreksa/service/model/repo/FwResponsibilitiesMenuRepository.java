package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.FwMenus;
import com.pangreksa.service.model.entity.FwResponsibilities;
import com.pangreksa.service.model.entity.FwResponsibilitiesMenu;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FwResponsibilitiesMenuRepository extends JpaRepository<FwResponsibilitiesMenu, Long> {

    @EntityGraph(attributePaths = {"menu","menu.page", "menu.group"})
    // Cari semua menu yang terhubung ke responsibility tertentu
    List<FwResponsibilitiesMenu> findByResponsibility(FwResponsibilities responsibility);

    // Cari semua responsibility yang terhubung ke menu tertentu
    List<FwResponsibilitiesMenu> findByMenu(FwMenus menu);

    // Cek apakah hubungan responsibility-menu sudah ada
    boolean existsByResponsibilityAndMenu(FwResponsibilities responsibility, FwMenus menu);

    // Cari satu hubungan spesifik
    Optional<FwResponsibilitiesMenu> findByResponsibilityAndMenu(FwResponsibilities responsibility, FwMenus menu);

    // Cari semua yang aktif untuk sebuah responsibility
    List<FwResponsibilitiesMenu> findByResponsibilityAndIsActiveTrue(FwResponsibilities responsibility);

    // BE-merged: menus visible to a given user via their responsibilities
    @Query("select distinct mn " +
            "from FwResponsibilitiesMenu rm " +
            "join rm.menu mn " +
            "join fetch mn.page pg " +
            "where rm.responsibility.id IN (select aur.responsibility.id from FwAppuserResp aur where aur.appuser.id = :userId)")
    List<FwMenus> findMenusByUserId(@Param("userId") Long userId);
}
