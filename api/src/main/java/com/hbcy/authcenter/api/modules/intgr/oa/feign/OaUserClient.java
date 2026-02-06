package com.hbcy.authcenter.api.modules.intgr.oa.feign;

import com.hbcy.authcenter.api.modules.intgr.oa.dto.OaTodoDTO;
import com.hbcy.authcenter.api.modules.intgr.oa.vo.OaQueryTodoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 不同用户使用不同header的接口
 *
 * @author 姚泰然
 * @date 2026-02-06 14:23
 */
@FeignClient(name = "oa-client", url = "${oa.url:}")
public interface OaUserClient {
    /**
     * 查询待办
     */
    @PostMapping(value = "/api/workflow/paService/getToDoWorkflowRequestList",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    List<OaTodoDTO> queryTodo(@RequestHeader String appid, @RequestHeader String userid,
                              @RequestHeader String token, @RequestBody OaQueryTodoVO vo);
}
