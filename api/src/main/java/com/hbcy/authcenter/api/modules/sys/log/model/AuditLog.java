package com.hbcy.authcenter.api.modules.sys.log.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志
 * @author 姚泰然
 * @date 2026-01-13 15:36
 */

@Data
@NoArgsConstructor
@TableName(value = "audit_log")
public class AuditLog {
    public static final String COL_ID = "id";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_ORG_ID = "org_id";
    public static final String COL_TRACE_ID = "trace_id";
    public static final String COL_SRC_APP = "src_app";
    public static final String COL_TARGET_APP = "target_app";
    public static final String COL_REQ_METHOD = "req_method";
    public static final String COL_REQ_HOST = "req_host";
    public static final String COL_REQ_PATH = "req_path";
    public static final String COL_REQ_PARAM = "req_param";
    public static final String COL_REQ_BODY = "req_body";
    public static final String COL_RESP_CODE = "resp_code";
    public static final String COL_RESP_BODY = "resp_body";
    public static final String COL_REQ_TIME = "req_time";
    public static final String COL_DURATION = "duration";
    public static final String COL_CLIENT_IP = "client_ip";
    public static final String COL_CREATE_TIME = "create_time";
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    /**
     * 追踪id
     */
    @TableField(value = "trace_id")
    private String traceId;
    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private String userId;
    /**
     * 组织id
     */
    @TableField(value = "org_id")
    private String orgId;
    /**
     * 请求源app
     */
    @TableField(value = "src_app")
    private String srcApp;
    /**
     * 目标应用
     */
    @TableField(value = "target_app")
    private String targetApp;
    /**
     * 请求方法
     */
    @TableField(value = "req_method")
    private String reqMethod;
    /**
     * 请求域名或ip
     */
    @TableField(value = "req_host")
    private String reqHost;
    /**
     * uri的路径
     */
    @TableField(value = "req_path")
    private String reqPath;
    /**
     * 请求参数
     */
    @TableField(value = "req_param")
    private String reqParam;
    /**
     * 返回值
     */
    @TableField(value = "req_body")
    private String reqBody;
    /**
     * http状态码
     */
    @TableField(value = "resp_code")
    private Integer respCode;
    @TableField(value = "resp_body")
    private String respBody;
    /**
     * 请求时间点
     */
    @TableField(value = "req_time")
    private LocalDateTime reqTime;
    /**
     * 响应时间（毫秒）
     */
    @TableField(value = "duration")
    private Integer duration;
    @TableField(value = "client_ip")
    private String clientIp;
    /**
     * 插入数据库时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}