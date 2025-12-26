package com.hbcy.authcenter.api.modules.core.org.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgUserMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgUser;
import org.springframework.stereotype.Service;

/**
 * 用户与组织的关联关系管理
 *
 * @author 姚泰然
 * @date 2025-12-25 17:51
 */
@Service
public class OrgUserService extends ServiceImpl<OrgUserMapper, OrgUser> {
}
