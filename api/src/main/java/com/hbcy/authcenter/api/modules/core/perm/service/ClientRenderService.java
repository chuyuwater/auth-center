package com.hbcy.authcenter.api.modules.core.perm.service;

import com.hbcy.authcenter.api.modules.core.app.service.AppService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author 姚泰然
 * @date 2025-12-30 08:38
 */
@Service
public class ClientRenderService {
    @Resource
    private AppService appService;

    @Resource
    private PermUnitUserService permUnitUserService;

    @Resource
    private PermUnitResourceService permUnitResourceService;

}
