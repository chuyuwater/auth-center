package com.hbcy.authcenter.api.modules.intgr.oa.feign;

import com.hbcy.authcenter.api.modules.intgr.oa.vo.OaOrgQueryVO;
import com.hbcy.authcenter.api.modules.intgr.oa.vo.OaPageSizeModifyVO;
import com.hbcy.authcenter.api.modules.intgr.oa.vo.OaPersonQueryVO;
import com.hbcy.authcenter.api.modules.intgr.oa.vo.OaQueryTableVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 使用固定账号访问的OA业务接口
 * @author 姚泰然
 * @date 2026-01-20 14:49
 */
@FeignClient(name = "oa-biz", url = "${oa.url:}", configuration = OaReqConfig.class)
public interface OaBizClient {
    /*****************************表单相关通用接口***************************************************************/

    /**
     * 获取表单真正的结果
     * 返回值一般是json，但是动态的结构，需要自行解析
     * @param vo 查询时返回的sessionKey
     * @return 真正的查询结果
     */
    @PostMapping(value = "/api/ec/dev/table/datas", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String queryTableData(@RequestBody OaQueryTableVO vo);

    /**
     * 获取表单查询数量，返回结果格式是固定的，但是是text格式
     * @param vo 获取数量时返回的sessionKey
     * @return 查询数量
     */
    @PostMapping(value = "/api/ec/dev/table/counts", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String queryTableCount(@RequestBody OaQueryTableVO vo);

    /**
     * 修改表单分页大小
     */
    @PostMapping(value = "/api/ec/dev/table/pageSize", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String modifyTablePageSize(@RequestBody OaPageSizeModifyVO vo);

    /************************************************************************************************************/

    /**
     * 查询组织
     */
    @GetMapping(value = "/api/hrm/base/getHrmSearchTree", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String queryOrg(@SpringQueryMap OaOrgQueryVO vo);

    /**
     * 查询人员session
     * @param vo 查询条件
     * @return 表单session
     */
    @PostMapping(value = "/api/hrm/search/getHrmSearchResult", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String queryPersonSession(@RequestBody OaPersonQueryVO vo);
}
