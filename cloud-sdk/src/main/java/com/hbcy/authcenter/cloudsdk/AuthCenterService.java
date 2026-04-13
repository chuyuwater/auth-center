package com.hbcy.authcenter.cloudsdk;

import com.hbcy.authcenter.sdk.feign.AuthCenterClient;
import com.hbcy.authcenter.sdk.utils.UserContextUtils;
import com.hbcy.common.base.error.ClientError;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.error.ServerError;
import com.hbcy.common.base.pojo.ApiResponse;
import com.hbcy.common.base.util.SpringUtils;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author 姚泰然
 * @date 2026-04-13 14:06
 */
@Component
public class AuthCenterService {
    @Resource
    private AuthCenterClient authCenterClient;

    @Cacheable(value = "@1m")
    public List<String> listGrantOrgs(String appId, String userId, String orgId, String permCode) {
        ApiResponse<List<String>> resp = authCenterClient.listGrantOrgs(userId, appId, permCode, orgId);
        if (resp.getStatus().equals(0)) {
            return resp.getData();
        }
        return List.of();
    }

    @Cacheable(value = "@1m")
    public boolean checkPerm(String appId, String userId, String orgId, String permCode) {
        ApiResponse<Boolean> resp = authCenterClient.checkAnyPerm(userId, orgId, appId, permCode);
        if (resp.getStatus().equals(0)) {
            return resp.getData();
        }
        return false;
    }

    public Map<String, Boolean> checkPerms(String appId, String userId, String orgId, List<String> permCodes) {
        ApiResponse<Map<String, Boolean>> resp = authCenterClient.checkAnyPerm(userId, orgId, appId, permCodes);
        if (resp.getStatus().equals(0)) {
            return resp.getData();
        }
        return Map.of();
    }

    public void checkPerm(String appId, String orgId, String permCode) {
        String userId = UserContextUtils.getUserId();
        //检查用户是否有对应组织的设备操作权限
        try {
            AuthCenterService bean = SpringUtils.getBean(AuthCenterService.class);
            if (bean == null) {
                throw new ServerError("权限服务异常");
            }
            boolean ok = bean.checkPerm(appId, userId, orgId, permCode);
            if (!ok) {
                throw new ParamError("无指定组织的设备权限");
            }
        } catch (ClientError e) {
            throw e;
        } catch (Exception e) {
            throw new ServerError("权限服务异常");
        }
    }
}
