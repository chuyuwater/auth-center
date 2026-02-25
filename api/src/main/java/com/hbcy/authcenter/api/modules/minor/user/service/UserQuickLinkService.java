package com.hbcy.authcenter.api.modules.minor.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbcy.authcenter.api.modules.core.org.dao.OrgTreeMapper;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.minor.user.dao.UserQuickLinkMapper;
import com.hbcy.authcenter.api.modules.minor.user.model.UserQuickLink;
import com.hbcy.authcenter.api.modules.minor.user.vo.UserQuickLinkUpsertVO;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.PermissionError;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 快捷入口
 * @author 姚泰然
 * @date 2026-02-25 15:49
 */
@Service
public class UserQuickLinkService extends ServiceImpl<UserQuickLinkMapper, UserQuickLink> {
    @Resource
    private OrgTreeMapper orgTreeMapper;
    @Resource
    private ClientRenderService clientRenderService;

    @Transactional(rollbackFor = Exception.class)
    public void overwrite(UserQuickLinkUpsertVO vo) {
        String userId = UserContextUtils.getUserId();
        String orgId = UserContextUtils.getUserOrg();
        //清空已有的
        baseMapper.delete(new QueryWrapper<UserQuickLink>()
                .eq(UserQuickLink.COL_USER_ID, userId)
                .eq(UserQuickLink.COL_CLIENT_TYPE, vo.getClientType())
                .eq(UserQuickLink.COL_ORG_ID, orgId));
        OrgTree org = orgTreeMapper.selectById(orgId);
        if (org == null) {
            throw new PermissionError();
        }
        Set<String> grantIds = clientRenderService.grantResIds(userId, org, 0);
        vo.getResIds().removeIf(id -> !grantIds.contains(id));
        if (vo.getResIds().isEmpty()) {
            return;
        }
        List<UserQuickLink> userQuickLinks = vo.getResIds().stream()
                .map(resId -> {
                    UserQuickLink entity = new UserQuickLink();
                    entity.setUserId(userId);
                    entity.setOrgId(orgId);
                    entity.setResId(resId);
                    entity.setClientType(vo.getClientType());
                    return entity;
                }).toList();
        baseMapper.insert(userQuickLinks);
    }

    public List<UserQuickLink> list(int clientType) {
        String userId = UserContextUtils.getUserId();
        String orgId = UserContextUtils.getUserOrg();
        return baseMapper.selectList(new QueryWrapper<UserQuickLink>()
                .eq(UserQuickLink.COL_USER_ID, userId)
                .eq(UserQuickLink.COL_CLIENT_TYPE, clientType)
                .eq(UserQuickLink.COL_ORG_ID, orgId)
        );
    }
}
