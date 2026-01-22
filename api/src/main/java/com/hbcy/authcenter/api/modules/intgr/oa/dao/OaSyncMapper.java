package com.hbcy.authcenter.api.modules.intgr.oa.dao;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.hbcy.authcenter.api.modules.core.org.model.OrgTree;
import com.hbcy.authcenter.api.modules.core.user.model.User;
import com.hbcy.authcenter.api.modules.core.user.model.UserOrg;
import com.hbcy.common.web.api.NamedId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 姚泰然
 * @date 2026-01-21 14:13
 */
@Mapper
public interface OaSyncMapper {
    @InterceptorIgnore(blockAttack = "true")
    void batchUpdateOrg(@Param("toUpdate") List<OrgTree> toUpdate);

    void batchInsertOrg(@Param("toInsert") List<OrgTree> toInsert);

    @InterceptorIgnore(blockAttack = "true")
    void fillSourceId(@Param("missIdUsers") List<User> missIdUsers);

    List<NamedId> listUserPhone(@Param("tenantId") String tenantId);

    void upsertUsers(@Param("users") List<User> users);

    void upsertUserOrgs(@Param("userOrgs") List<UserOrg> userOrgs);
}
