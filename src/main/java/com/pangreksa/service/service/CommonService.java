package com.pangreksa.service.service;

import com.pangreksa.service.shared.security.AppUserInfo;
import com.pangreksa.service.shared.Authorization;
import com.pangreksa.service.model.entity.FwAppUser;
import com.pangreksa.service.model.entity.FwMenus;
import com.pangreksa.service.model.entity.VwAppUserAuth;
import com.pangreksa.service.model.repo.FwAppUserRepository;
import com.pangreksa.service.model.repo.FwMenusRepository;
import com.pangreksa.service.model.repo.VwAppUserAuthRepository;
import org.springframework.stereotype.Service;

@Service
public class CommonService {
    private final FwMenusRepository menusRepository;
    private final VwAppUserAuthRepository appUserAuthRepository;
    private final FwAppUserRepository fwAppUserRepository;

    public CommonService(FwMenusRepository menusRepository, VwAppUserAuthRepository appUserAuthRepository, FwAppUserRepository fwAppUserRepository) {
        this.menusRepository = menusRepository;
        this.appUserAuthRepository = appUserAuthRepository;
        this.fwAppUserRepository = fwAppUserRepository;
    }

    public Authorization getAuthorization(AppUserInfo user, String responsibility, Long pageId) {
        VwAppUserAuth appUserAuth =  appUserAuthRepository.findByIsActiveTrueAndUsernameAndResponsibilityAndPageId(user.getUserId().toString(), responsibility, pageId);

        if (appUserAuth == null) {
            return new Authorization(false, false, false, false);
        } else {
            FwMenus fwMenus = menusRepository.findById(appUserAuth.getMenuId())
                    .orElseThrow(() -> new IllegalStateException("Page not found with ID: " + appUserAuth.getMenuId()));

            return new Authorization(fwMenus.getCanView(),
                    fwMenus.getCanCreate(),
                    fwMenus.getCanEdit(),
                    fwMenus.getCanDelete());
        }
    }

    public FwAppUser getLoginUser(String username) {
        return fwAppUserRepository.findByUsername(username).orElse(new FwAppUser());
    }
}
