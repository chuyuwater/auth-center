package com.hbcy.authcenter.api.modules.core.user.controller;

import com.google.common.base.Joiner;
import com.hbcy.authcenter.api.modules.core.user.dto.OrgUserDTO;
import com.hbcy.authcenter.api.modules.core.user.dto.UserExportDTO;
import com.hbcy.authcenter.api.modules.core.user.dto.UserQueryResultDTO;
import com.hbcy.authcenter.api.modules.core.user.service.UserService;
import com.hbcy.authcenter.api.modules.core.user.vo.*;
import com.hbcy.common.base.error.ParamError;
import com.hbcy.common.base.pojo.BatchDeleteVO;
import com.hbcy.common.base.pojo.PageResp;
import com.hbcy.common.web.annotation.IgnoreResponseWrapper;
import com.hbcy.common.web.bean.NameFill;
import com.pig4cloud.plugin.excel.annotation.RequestExcel;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.pig4cloud.plugin.excel.vo.ErrorMessage;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理（租户侧）
 *
 * @author 姚泰然
 * @module user
 * @date 2025-12-26
 */
@RestController
@RequestMapping("/api/portal/v1/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public String createUser(@Valid @RequestBody UserCreateVO vo) {
        return userService.createUser(vo);
    }

    /**
     * 修改用户
     */
    @PutMapping("/{userId}")
    public void updateUser(@PathVariable String userId, @Valid @RequestBody UserUpdateVO vo) {
        userService.updateUser(userId, vo);
    }

    /**
     * 删除用户
     *
     * @param userId 用户id
     */
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }

    /**
     * 用户详情
     *
     * @param userId 用户id
     * @return 用户及其关联的组织信息
     */
    @GetMapping("/{userId}")
    @NameFill
    public UserQueryResultDTO getUser(@PathVariable String userId) {
        return userService.getUser(userId);
    }

    /**
     * 搜索用户
     * 人不会重复，汇总任职信息
     *
     * @param vo 搜索条件
     * @return 用户及其关联的组织信息
     */
    @GetMapping("")
    @NameFill
    public PageResp<UserQueryResultDTO> listUser(UserQueryVO vo) {
        return userService.queryUser(vo);
    }

    /**
     * 搜索用户（选人窗口）
     * 人员会重复，不同的任职信息各一条数据
     *
     * @param vo 搜索条件
     * @return 用户及其关联的组织信息
     */
    @GetMapping("/for-select")
    public PageResp<OrgUserDTO> listOrgUser(UserQueryVO vo) {
        return userService.filterUser4Select(vo);
    }

    /**
     * 导出用户
     *
     * @param vo 导出条件
     * @return 用户及其关联的组织信息
     */
    @PostMapping("/export")
    @IgnoreResponseWrapper
    @ResponseExcel
    public List<UserExportDTO> exportUser(UserQueryVO vo) {
        return userService.export(vo);
    }

    /**
     * 导入用户
     *
     * @param vo 导入数据
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/import")
    public void importUser(@RequestExcel List<UserImportVO> vo, BindingResult bindingResult) {
        List<ErrorMessage> errorMessageList = (List<ErrorMessage>) bindingResult.getTarget();
        if (!CollectionUtils.isEmpty(errorMessageList)) {
            StringBuilder sb = new StringBuilder();
            for (ErrorMessage m : errorMessageList) {
                sb.append("第").append(m.getLineNum()).append("行");
                sb.append(":");
                sb.append(Joiner.on(",").join(m.getErrors()));
                sb.append(";");
            }
            throw new ParamError(sb.toString());
        }
        userService.batchInsert(vo);
    }

    /**
     * 批量删除
     *
     * @param vo 用户id列表
     */
    @PostMapping("/batch-delete")
    public void deleteUsers(@Valid @RequestBody BatchDeleteVO vo) {
        userService.deleteUsers(vo);
    }

    /**
     * 禁用/解禁用户
     */
    @PatchMapping("/forbidden")
    public void forbidUser(@Valid @RequestBody UserForbidVO vo) {
        userService.forbidUser(vo);
    }

    /**
     * 管理员重置密码
     */
    @PostMapping("/admin-reset-passwd")
    public void adminResetPasswd(@Valid @RequestBody AdminResetPasswdVO vo) {
        userService.adminResetPasswd(vo);
    }
}
