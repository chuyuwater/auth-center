CREATE DATABASE IF NOT EXISTS portal_auth_center CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
use portal_auth_center;
create table if not exists app
(
    id             varchar(50)                            not null comment '应用英文标识'
        primary key,
    name_cn        varchar(20)                            not null comment '中文名',
    memo           varchar(200) default ''                not null comment '简介',
    show_order     int          default 0                 not null comment '显示顺序',
    icon           varchar(255) default ''                not null comment '图标路径',
    forbidden      tinyint      default 0                 not null comment '是否禁用',
    binding_tenant varchar(20)  default ''                not null comment '空：多租户应用，"-": 单租户应用尚未绑定租户，其他：单租户应用绑定的租户id',
    delete_time    bigint       default 0                 not null comment '逻辑删除',
    create_time    datetime     default CURRENT_TIMESTAMP not null,
    update_time    datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    create_user    char(26)     default ''                not null,
    update_user    char(26)     default ''                not null,
    constraint ux_app_name_cn
        unique (name_cn, delete_time)
);

create table if not exists audit_log
(
    id          char(26)                              not null
        primary key,
    trace_id    varchar(64) default ''                not null comment '追踪id',
    user_id     char(26)                              not null comment '用户id',
    org_id      varchar(20)                           not null comment '组织id',
    src_app     varchar(20)                           not null comment '请求源app',
    target_app  varchar(20)                           not null comment '目标应用',
    req_method  varchar(10)                           not null comment '请求方法',
    req_host    varchar(200)                          not null comment '请求域名或ip',
    req_path    varchar(300)                          not null comment 'uri的路径',
    req_param   text                                  null comment '请求参数',
    req_body    text                                  null comment '返回值',
    resp_code   int                                   not null comment 'http状态码',
    resp_body   text                                  null,
    req_time    datetime                              not null comment '请求时间点',
    duration    int                                   not null comment '响应时间（毫秒）',
    client_ip   varchar(100)                          null,
    create_time datetime    default CURRENT_TIMESTAMP not null comment '插入数据库时间'
)
    comment '审计日志';

create index ix_audit_log_req_org
    on audit_log (org_id);

create index ix_audit_log_req_path
    on audit_log (target_app, req_method, req_path);

create index ix_audit_log_req_time
    on audit_log (req_time);

create index ix_audit_log_req_user
    on audit_log (user_id);

create table if not exists org_tree
(
    id            varchar(20)                            not null comment '组织/部门id，算法生成'
        primary key,
    node_name     varchar(100)                           not null comment '节点名',
    short_name    varchar(12)  default ''                not null comment '简称',
    memo          varchar(200) default ''                not null comment '说明',
    node_type     tinyint      default 1                 not null comment '节点类型，0-组织，1-部门',
    exist_type    tinyint      default 0                 not null comment '存在形式：0-实体，1-虚拟',
    node_category tinyint      default 1                 not null comment '类别，组织：0-项目部，1-公司，2-分公司，3-子公司',
    parent_id     varchar(50)  default ''                not null comment '父节点id',
    forbidden     tinyint      default 0                 not null comment '0-启用，1-禁用',
    id_path       varchar(768) default ''                not null comment '全路径，方便查询',
    show_order    int          default 0                 not null,
    tenant_id     varchar(20)                            not null comment '租户id',
    relate_id     varchar(50)                            null comment '关联代码，如项目id、第三方平台id',
    delete_time   bigint       default 0                 not null comment '逻辑删除',
    create_time   datetime     default CURRENT_TIMESTAMP not null,
    update_time   datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    create_user   char(26)     default '0'               not null,
    update_user   char(26)     default '0'               not null,
    constraint ux_org_tree_tenant_node
        unique (tenant_id, node_name, parent_id, delete_time),
    constraint ux_org_tree_tenant_relate_id
        unique (relate_id, tenant_id, delete_time),
    constraint ux_org_tree_tenant_short_name
        unique (short_name, tenant_id, parent_id, delete_time)
);

create index ix_org_tree_id_path
    on org_tree (id_path);

create index ix_org_tree_parent_id
    on org_tree (parent_id, show_order);

create table if not exists perm_unit
(
    id           varchar(20)                            not null comment '规则生成名称'
        primary key,
    name_cn      varchar(50)                            not null comment '名称',
    policy_model tinyint      default 0                 not null comment '授权模型，0-RBAC，1-ABAC',
    belong_to    varchar(25)                            not null comment '归属分组',
    memo         varchar(200) default ''                not null comment '说明',
    forbidden    tinyint      default 0                 not null,
    tenant_id    varchar(20)                            not null,
    create_user  char(26)     default '0'               not null,
    update_user  char(26)     default '0'               not null,
    create_time  datetime     default CURRENT_TIMESTAMP not null,
    update_time  datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    delete_time  bigint       default 0                 not null,
    constraint ux_perm_unit_parent_name
        unique (belong_to, name_cn, delete_time)
);

create index ix_perm_unit_name
    on perm_unit (name_cn);

create index ix_perm_unit_tenant_id
    on perm_unit (tenant_id, policy_model, forbidden);

create table if not exists perm_unit_group
(
    id           varchar(25)                            not null
        primary key,
    node_name    varchar(50)                            not null comment '分组（节点）名称',
    memo         varchar(200) default ''                not null comment '说明',
    parent_id    char(26)     default ''                not null comment '父节点id',
    policy_model tinyint      default 0                 not null comment '0-RBAC, 1-ABAC',
    id_path      varchar(768)                           not null,
    show_order   int          default 0                 not null,
    tenant_id    varchar(20)                            not null,
    create_user  char(26)     default '0'               not null,
    update_user  char(26)     default '0'               not null,
    create_time  datetime     default CURRENT_TIMESTAMP not null,
    update_time  datetime     default CURRENT_TIMESTAMP not null,
    delete_time  bigint       default 0                 not null
) comment '授权分组';

create index ix_perm_tree_id_path
    on perm_unit_group (id_path);

create index ix_perm_tree_tenant_name
    on perm_unit_group (tenant_id, show_order, node_name);

create index ux_perm_tree_level_name
    on perm_unit_group (parent_id, node_name);

create table if not exists perm_unit_resource
(
    id          char(26)                           not null
        primary key,
    unit_id     varchar(20)                        not null comment '权限单元id',
    app_id      char(26)                           not null comment '应用id',
    perm_id     char(26)                           not null comment '权限码id',
    tenant_id   varchar(20)                        not null comment '租户id',
    create_user char(26) default '0'               not null,
    update_user char(26) default '0'               not null,
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update current_timestamp
);

create index ix_perm_res_tenant_app
    on perm_unit_resource (app_id, tenant_id);

create index ux_perm_res_unit_code
    on perm_unit_resource (unit_id, perm_id);

create table if not exists perm_unit_user
(
    id          char(26)                           not null
        primary key,
    unit_id     varchar(20)                        not null,
    user_id     char(26)                           not null,
    org_id      varchar(20)                        null comment '主体域1：组织id',
    tenant_id   varchar(20)                        not null,
    forbidden   tinyint  default 0                 not null comment '是否禁用，冗余perm_unit的禁用',
    create_user char(26) default '0'               not null,
    update_user char(26) default '0'               not null,
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update current_timestamp,
    constraint ux_perm_unit_user_unit
        unique (unit_id, org_id, user_id)
);

create index ix_perm_unit_user
    on perm_unit_user (user_id, tenant_id);

create table if not exists resource_perm
(
    id          char(26)                               not null
        primary key,
    app_id      char(26)                               not null comment '应用id，冗余方便搜索',
    res_id      char(26)                               not null comment '资源id',
    perm_name   varchar(100)                           not null comment '权限名称',
    perm_code   varchar(100)                           not null comment '权限码',
    api_method  tinyint      default 0                 not null comment '0-GET, 1-POST, 2-PUT, 3-DELETE',
    api_path    varchar(255) default ''                not null comment '支持ant通配符的api路径',
    create_time datetime     default CURRENT_TIMESTAMP not null,
    update_time datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    create_user char(26)     default '0'               not null,
    update_user char(26)     default '0'               not null,
    constraint ux_res_perm_api
        unique (api_method, api_path)
);

create index ix_res_perm_app_id
    on resource_perm (app_id);

create index ix_res_perm_code
    on resource_perm (perm_code);

create index ix_res_perm_res_id
    on resource_perm (res_id);

create table if not exists resource_tree
(
    id          char(26)                               not null comment 'ulid'
        primary key,
    name_cn     varchar(50)                            null comment '中文名',
    app_id      varchar(50)                            not null comment '应用id',
    custom_id   varchar(50)                            not null comment '应用开发者自定义菜单id',
    parent_id   char(26)     default ''                not null comment '父节点',
    id_path     varchar(768)                           not null comment '节点全路径',
    client_type tinyint      default 0                 not null comment '支持的客户端类型，0-全端，1-PC端，2-移动端',
    res_type    tinyint      default 0                 not null comment '0-页面，1-按钮',
    icon        varchar(200) default ''                not null comment '图标地址',
    route_link  varchar(255) default ''                not null comment '路由地址',
    show_order  int          default 0                 not null comment '同级显示顺序',
    hidden      tinyint      default 0                 not null comment '是否隐藏，0-显示，1-隐藏',
    show_level  tinyint      default 0                 not null comment '显示级别，0-全局，1-组织级，2-项目级',
    create_time datetime     default CURRENT_TIMESTAMP not null,
    update_time datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    create_user char(26)     default '0'               not null,
    update_user char(26)     default '0'               not null,
    constraint ux_res_tree_app_custom_id
        unique (app_id, custom_id)
);

create index ix_res_tree_id_path
    on resource_tree (id_path);

create index ix_res_tree_node_order
    on resource_tree (parent_id, show_order);

create table if not exists sys_dict
(
    id          char(26)                               not null comment 'ulid'
        primary key,
    app_id      varchar(20)  default ''                not null comment '应用id，为空标识通用',
    feat_code   varchar(100)                           not null comment '字典键，为空表示分组',
    value_str   varchar(100)                           not null comment '字典值',
    value_cn    varchar(100) default ''                not null comment '中文值',
    dict_type   tinyint      default 0                 not null comment '0-列表，1-树状',
    parent_id   varchar(26)  default ''                not null comment '父节点',
    id_path     varchar(768) default ''                not null,
    show_order  int          default 0                 not null comment '显示顺序',
    forbidden   tinyint      default 0                 not null,
    create_user char(26)     default '0'               not null,
    update_user char(26)     default '0'               not null,
    create_time datetime     default CURRENT_TIMESTAMP not null,
    update_time datetime     default CURRENT_TIMESTAMP not null,
    delete_time bigint       default 0                 not null,
    constraint ux_sys_dict_kv
        unique (feat_code, value_str, delete_time),
    constraint ux_sys_dict_kv_cn
        unique (value_cn, feat_code, delete_time)
);

create index ix_sys_dict_id_path
    on sys_dict (id_path);

create index ix_sys_dict_parent_id
    on sys_dict (parent_id, show_order, app_id);

create table if not exists sys_user
(
    id            char(26)                               not null comment '用户id'
        primary key,
    phone         varchar(20)                            not null comment '手机号',
    account       varchar(50)                            null comment '登录账号',
    real_name     varchar(20)  default ''                not null comment '姓名',
    passwd        varchar(100) default ''                not null comment 'bcrypt加密的密码',
    email         varchar(200)                           null comment '邮箱',
    avatar        varchar(255) default ''                not null comment '头像',
    forbidden     tinyint      default 0                 not null,
    wecom_id      varchar(200)                           null comment '企业微信id',
    src_type      tinyint      default 0                 not null comment '账号来源，0-自建，1-外部同步',
    employee_type int                                    null comment '用工类型，字典EMPLOYEE_TYPE',
    passwd_expire datetime                               null comment '密码过期时间，null标识永不过期',
    tenant_id     varchar(20)                            not null comment '账号所属租户',
    last_login    datetime                               null comment '最近一次登陆时间',
    delete_time   bigint       default 0                 not null,
    create_time   datetime     default CURRENT_TIMESTAMP not null,
    update_time   datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    create_user   char(26)     default '0'               not null,
    update_user   char(26)     default '0'               not null,
    constraint ux_sys_user_tenant_account
        unique (account, tenant_id, delete_time),
    constraint ux_sys_user_tenant_email
        unique (email, tenant_id, delete_time),
    constraint ux_sys_user_tenant_phone
        unique (phone, tenant_id, delete_time),
    constraint ux_sys_user_tenant_wecom
        unique (tenant_id, wecom_id, delete_time)
);

create table if not exists sys_user_mapping
(
    id           char(26)                           not null
        primary key,
    user_id      char(26)                           not null comment '我方用户id',
    src_type     int      default 1                 not null comment '第三方平台，字典项',
    src_id       varchar(100)                       null comment '第三方平台id',
    account_type tinyint  default 0                 not null comment '多账号系统同步，0-主账号，1-子账号',
    create_time  datetime default CURRENT_TIMESTAMP not null,
    constraint ux_sys_user_mapping
        unique (src_id, src_type, user_id)
)
    comment '用户映射';

create index ix_sys_user_mapping_user
    on sys_user_mapping (user_id);

create table if not exists tenant
(
    id            varchar(20)                            not null comment '算法生成id'
        primary key,
    name_cn       varchar(20)                            not null comment '中文名称',
    short_name    varchar(12)  default ''                not null comment '简称',
    logo          varchar(255) default ''                not null comment 'logo url',
    memo          varchar(200) default ''                not null comment '备注信息',
    forbidden     tinyint      default 0                 not null comment '0-启用，1-禁用',
    contact_user  varchar(20)                            not null comment '联系人',
    contact_phone varchar(20)                            not null comment '联系电话',
    admin_id      char(26)     default ''                not null comment '管理员id',
    delete_time   bigint       default 0                 not null comment '删除时间戳标记',
    create_user   char(26)     default '0'               not null comment '创建者',
    update_user   char(26)     default '0'               not null,
    create_time   datetime     default CURRENT_TIMESTAMP not null,
    update_time   datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    constraint ux_name
        unique (name_cn, delete_time)
);

create table if not exists tenant_app
(
    id          char(26)                              not null comment 'ulid'
        primary key,
    app_id      varchar(50)                           not null comment '应用id',
    tenant_id   varchar(20)                           not null comment '租户id',
    org_tree    varchar(50) default ''                not null comment '组织树id',
    grant_all   tinyint     default 0                 not null comment '是否授予应用所有权限',
    forbidden   tinyint     default 0                 not null,
    delete_time bigint      default 0                 not null,
    create_time datetime    default CURRENT_TIMESTAMP not null,
    update_time datetime    default CURRENT_TIMESTAMP not null on update current_timestamp,
    create_user char(26)    default ''                not null,
    update_user char(26)    default ''                not null,
    constraint ux_tenant_app
        unique (app_id, tenant_id, delete_time)
);

create table if not exists tenant_app_resource
(
    id          char(26)                           not null
        primary key,
    tenant_id   varchar(20)                        not null comment '租户id',
    app_id      varchar(50)                        not null comment '应用id',
    perm_id     char(26)                           null comment '权限点id',
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update current_timestamp,
    create_user char(26) default '0'               not null,
    update_user char(26) default '0'               not null,
    constraint ux_tenant_app_res
        unique (perm_id, app_id, tenant_id)
);

create table if not exists user_access
(
    id          char(26)                           not null
        primary key,
    key_name    varchar(200)                       null comment '名称',
    access_key  char(26)                           not null,
    secret_key  varchar(64)                        not null comment '加密的密钥',
    forbidden   tinyint  default 0                 not null comment '是否禁用',
    expire_time datetime                           null comment '过期时间',
    user_id     char(26)                           not null comment 'ak归属用户',
    tenant_id   varchar(20)                        not null comment 'ak归属租户',
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint ix_user_access_user_tenant
        unique (user_id, tenant_id)
)
    comment 'ak/sk通信';

create index ix_user_access_ak
    on user_access (access_key, expire_time, forbidden);

create table if not exists user_msg
(
    id          char(26)                               not null
        primary key,
    src_id      varchar(100)                           null comment '源消息id，用来去重',
    src_app     varchar(20)                            not null comment '源应用id',
    msg_title   varchar(200)                           not null comment '消息标题',
    msg_content varchar(700) default ''                not null comment '消息内容',
    target_user char(26)                               not null comment '目标用户',
    send_time   datetime                               not null comment '发送时间',
    view_status tinyint      default 0                 not null comment '0-未读，1-已读',
    msg_type    tinyint      default 0                 not null comment '0-普通消息，1-预警消息',
    relate_link varchar(256) default ''                not null comment '跳转链接',
    origin_json text                                   null comment '原始报文',
    tenant_id   varchar(20)                            not null,
    create_time datetime     default CURRENT_TIMESTAMP not null,
    constraint ux_user_inbox_target_src
        unique (src_app, src_id, target_user)
)
    comment '站内信';

create index ix_user_inbox_msg_title
    on user_msg (msg_title);

create index ix_user_inbox_user_time_status
    on user_msg (target_user, view_status, msg_type, send_time);

create table if not exists user_org
(
    id          char(26)                           not null
        primary key,
    user_id     char(26)                           not null comment '用户id',
    org_id      varchar(20)                        not null,
    node_id     varchar(20)                        not null comment '关联节点id（组织或部门）',
    tenant_id   varchar(20)                        not null comment '租户id',
    main_job    tinyint  default 0                 not null comment '0-兼职，1-主职',
    create_user char(26) default '0'               not null,
    update_user char(26) default '0'               not null,
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null,
    constraint ux_user_org_user_dept
        unique (user_id, node_id)
) comment '用户组织关系';

create index ix_user_org_main_job
    on user_org (org_id, tenant_id, main_job);

create table if not exists user_quick_link
(
    id          char(26)                           not null
        primary key,
    res_id      char(26)                           not null comment '菜单id',
    show_order  int      default 0                 not null comment '显示顺序',
    user_id     char(26)                           not null comment '用户id',
    org_id      varchar(20)                        null comment '组织id',
    client_type tinyint  default 1                 not null comment '1-PC端，2-移动端',
    create_time datetime default CURRENT_TIMESTAMP not null
)
    comment '快捷入口';

create index ix_user_quick_link_res_id
    on user_quick_link (res_id);

create index ux_user_quick_link
    on user_quick_link (user_id, org_id, client_type, res_id);

create table if not exists user_todo
(
    id            char(26)                               not null
        primary key,
    src_app       varchar(20)                            not null comment '源app id',
    src_id        varchar(100)                           null comment '源id，用于去重',
    todo_title    varchar(300)                           not null comment '待办标题',
    todo_content  varchar(700)                           null comment '待办内容',
    target_user   char(26)                               not null comment '关联用户',
    send_time     datetime                               not null,
    process_state tinyint      default 0                 not null comment '处理状态，字典TODO_PROCESS_STATE',
    view_state    tinyint      default 0                 not null comment '0-未读，1-已读',
    todo_type     tinyint      default 0                 not null comment '待办类型，0-流程待办，1-任务待办',
    relate_link   varchar(255) default ''                not null comment '关联链接',
    origin_json   text                                   null comment '用于调试',
    tenant_id     varchar(20)                            not null,
    create_time   datetime     default CURRENT_TIMESTAMP not null,
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint ux_user_todo_target_src
        unique (src_app, src_id, target_user)
);

create index ix_user_todo_title
    on user_todo (todo_title);

create index ix_user_todo_user_time_status
    on user_todo (target_user, view_state, process_state, todo_type);



INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EFFY2XC6ZSX1NKARS72Q0', 'portal', '', 'CLIENT_TYPE', '客户端类型', 0, '', '01KE8EFFY2XC6ZSX1NKARS72Q0', 0,
        0, '0', '0', '2026-01-06 09:24:26', '2026-01-06 09:24:26', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EHWVSM9FQP5C0K0G2SXVV', 'portal', 'CLIENT_TYPE', '0', '全端', 0, '01KE8EFFY2XC6ZSX1NKARS72Q0',
        '01KE8EFFY2XC6ZSX1NKARS72Q0/01KE8EHWVSM9FQP5C0K0G2SXVV', 0, 0, '0', '0', '2026-01-06 09:25:44',
        '2026-01-06 09:25:44', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EJ9WN1WCMC66ET9HGXTFD', 'portal', 'CLIENT_TYPE', '1', 'PC端', 0, '01KE8EFFY2XC6ZSX1NKARS72Q0',
        '01KE8EFFY2XC6ZSX1NKARS72Q0/01KE8EJ9WN1WCMC66ET9HGXTFD', 0, 0, '0', '0', '2026-01-06 09:25:58',
        '2026-01-06 09:25:58', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EJKRHFZAQ9CPZS6PQ2XJ3', 'portal', 'CLIENT_TYPE', '2', '移动端', 0, '01KE8EFFY2XC6ZSX1NKARS72Q0',
        '01KE8EFFY2XC6ZSX1NKARS72Q0/01KE8EJKRHFZAQ9CPZS6PQ2XJ3', 0, 0, '0', '0', '2026-01-06 09:26:08',
        '2026-01-06 09:26:08', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EKGW3W428BJEEB9ATGAFA', 'portal', '', 'MENU_LEVEL', '显示级别', 0, '', '01KE8EKGW3W428BJEEB9ATGAFA', 0,
        0, '0', '0', '2026-01-06 09:26:38', '2026-01-06 09:26:38', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EM552768TQ7AWKFPPS2ZW', 'portal', 'MENU_LEVEL', '0', '全级', 0, '01KE8EKGW3W428BJEEB9ATGAFA',
        '01KE8EKGW3W428BJEEB9ATGAFA/01KE8EM552768TQ7AWKFPPS2ZW', 0, 0, '0', '0', '2026-01-06 09:26:58',
        '2026-01-06 09:26:58', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EMJN3A57J8VFCG55YJEQF', 'portal', 'MENU_LEVEL', '1', '组织级', 0, '01KE8EKGW3W428BJEEB9ATGAFA',
        '01KE8EKGW3W428BJEEB9ATGAFA/01KE8EMJN3A57J8VFCG55YJEQF', 0, 0, '0', '0', '2026-01-06 09:27:12',
        '2026-01-06 09:27:12', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EMSV5T1FW5YXQABJPF6JR', 'portal', 'MENU_LEVEL', '2', '项目级', 0, '01KE8EKGW3W428BJEEB9ATGAFA',
        '01KE8EKGW3W428BJEEB9ATGAFA/01KE8EMSV5T1FW5YXQABJPF6JR', 0, 0, '0', '0', '2026-01-06 09:27:20',
        '2026-01-06 09:27:20', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EP5825600MR5D03YEMNZ9', 'portal', '', 'ORG_CATEGORY', '组织类型', 0, '', '01KE8EP5825600MR5D03YEMNZ9', 0,
        0, '0', '0', '2026-01-06 09:28:04', '2026-01-06 09:28:04', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EQ1H000TNJED9N14AAZD2', 'portal', 'ORG_CATEGORY', '0', '项目部', 0, '01KE8EP5825600MR5D03YEMNZ9',
        '01KE8EP5825600MR5D03YEMNZ9/01KE8EQ1H000TNJED9N14AAZD2', 0, 0, '0', '0', '2026-01-06 09:28:33',
        '2026-01-06 09:28:33', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EQFH86ESG8WQQQC6AGQ85', 'portal', 'ORG_CATEGORY', '1', '集团', 0, '01KE8EP5825600MR5D03YEMNZ9',
        '01KE8EP5825600MR5D03YEMNZ9/01KE8EQFH86ESG8WQQQC6AGQ85', 0, 0, '0', '0', '2026-01-06 09:28:47',
        '2026-01-06 09:28:47', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8EQS0SKVNYK6GSNCRDTCV4', 'portal', 'ORG_CATEGORY', '2', '公司', 0, '01KE8EP5825600MR5D03YEMNZ9',
        '01KE8EP5825600MR5D03YEMNZ9/01KE8EQS0SKVNYK6GSNCRDTCV4', 0, 0, '0', '0', '2026-01-06 09:28:57',
        '2026-01-06 09:28:57', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8ER1YCF9VJ8ZKPV3KE3KM0', 'portal', 'ORG_CATEGORY', '3', '分公司', 0, '01KE8EP5825600MR5D03YEMNZ9',
        '01KE8EP5825600MR5D03YEMNZ9/01KE8ER1YCF9VJ8ZKPV3KE3KM0', 0, 0, '0', '0', '2026-01-06 09:29:06',
        '2026-01-06 09:29:06', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KE8ERCRSW4G1D9HXC9HY9JRK', 'portal', 'ORG_CATEGORY', '4', '子公司', 0, '01KE8EP5825600MR5D03YEMNZ9',
        '01KE8EP5825600MR5D03YEMNZ9/01KE8ERCRSW4G1D9HXC9HY9JRK', 0, 0, '0', '0', '2026-01-06 09:29:17',
        '2026-01-06 09:29:17', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFM9R6GS6BA56YYC2FFEH757', 'portal', '', 'EMPLOYEE_TYPE', '用工形式', 0, '', '01KFM9R6GS6BA56YYC2FFEH757', 0,
        0, '0', '0', '2026-01-23 02:10:49', '2026-01-23 02:10:49', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFMA52ZERARG4Y62MP1C7Z7A', 'portal', 'EMPLOYEE_TYPE', '0', '劳务合同工', 0, '01KFM9R6GS6BA56YYC2FFEH757',
        '01KFM9R6GS6BA56YYC2FFEH757/01KFMA52ZERARG4Y62MP1C7Z7A', 0, 0, '0', '0', '2026-01-23 02:16:29',
        '2026-01-23 02:16:29', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFMA56WRTMEAQQY0D4HKNP9S', 'portal', 'EMPLOYEE_TYPE', '1', '劳务派遣工', 0, '01KFM9R6GS6BA56YYC2FFEH757',
        '01KFM9R6GS6BA56YYC2FFEH757/01KFMA56WRTMEAQQY0D4HKNP9S', 0, 0, '0', '0', '2026-01-23 02:16:29',
        '2026-01-23 02:16:29', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFMA5ANQ026FV2KNY554NDPR', 'portal', 'EMPLOYEE_TYPE', '2', '劳务外包工', 0, '01KFM9R6GS6BA56YYC2FFEH757',
        '01KFM9R6GS6BA56YYC2FFEH757/01KFMA5ANQ026FV2KNY554NDPR', 0, 0, '0', '0', '2026-01-23 02:16:29',
        '2026-01-23 02:16:29', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFMA5HVR541T6JZVZDGJ70ZF', 'portal', 'EMPLOYEE_TYPE', '3', '实习生', 0, '01KFM9R6GS6BA56YYC2FFEH757',
        '01KFM9R6GS6BA56YYC2FFEH757/01KFMA5HVR541T6JZVZDGJ70ZF', 0, 0, '0', '0', '2026-01-23 02:16:29',
        '2026-01-23 02:16:29', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFMAS9WS88ZFZRHB83ZTEZ78', 'portal', 'EMPLOYEE_TYPE', '4', '外部单位人员', 0, '01KFM9R6GS6BA56YYC2FFEH757',
        '01KFM9R6GS6BA56YYC2FFEH757/01KFMAS9WS88ZFZRHB83ZTEZ78', 0, 0, '0', '0', '2026-01-23 10:29:34',
        '2026-01-23 10:29:52', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFYM0AV5ED1X686525888CZS', 'portal', '', 'TODO_PROCESS_STATE', '待办处理状态', 0, '',
        '01KFYM0AV5ED1X686525888CZS', 0, 0, '0', '0', '2026-01-27 10:21:44', '2026-01-27 10:21:44', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFYM40NPEER546ZZZKT96W7N', 'portal', 'TODO_PROCESS_STATE', '0', '待办', 0, '01KFYM0AV5ED1X686525888CZS',
        '01KFYM0AV5ED1X686525888CZS/01KFYM40NPEER546ZZZKT96W7N', 0, 0, '0', '0', '2026-01-27 10:23:02',
        '2026-01-27 10:23:02', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFYM46Y3EHDBQNR8DWN0MZ51', 'portal', 'TODO_PROCESS_STATE', '2', '已办', 0, '01KFYM0AV5ED1X686525888CZS',
        '01KFYM0AV5ED1X686525888CZS/01KFYM46Y3EHDBQNR8DWN0MZ51', 0, 0, '0', '0', '2026-01-27 10:23:02',
        '2026-01-27 10:23:02', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFYM4ANAKN48HNKKFDMRB22J', 'portal', 'TODO_PROCESS_STATE', '4', '办结', 0, '01KFYM0AV5ED1X686525888CZS',
        '01KFYM0AV5ED1X686525888CZS/01KFYM4ANAKN48HNKKFDMRB22J', 0, 0, '0', '0', '2026-01-27 10:23:02',
        '2026-01-27 10:23:02', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time,
                                         update_time, delete_time)
VALUES ('01KFYM4E91TFYAFSP6CFMP3A9Z', 'portal', 'TODO_PROCESS_STATE', '8', '抄送', 0, '01KFYM0AV5ED1X686525888CZS',
        '01KFYM0AV5ED1X686525888CZS/01KFYM4E91TFYAFSP6CFMP3A9Z', 0, 0, '0', '0', '2026-01-27 10:23:02',
        '2026-01-27 10:23:02', 0);


INSERT INTO `app`
VALUES ('portal', '统一门户', '', 1, '', 0, '', 0, '2026-01-06 01:10:24', '2026-01-06 01:10:24', '0', '0');
INSERT INTO `tenant`
VALUES ('0', '系统', '系统', '', '', 0, '姚泰然', '18502710984', '01KE8DW1DK2KPAZJAENJW6SSFE', 0, '0', '0',
        '2026-01-06 09:13:48', '2026-01-06 09:13:48');
INSERT INTO `tenant_app`
VALUES ('01KE8DY46ZGMHAZH7P7RDRZDQW', 'portal', '0', '0-ORG-000001', 1, 0, 0, '2026-01-06 09:14:57',
        '2026-01-06 09:14:57', '0', '0');
INSERT INTO `sys_user`
VALUES ('01KE8DW1DK2KPAZJAENJW6SSFE', '17912345678', 'admin', '超级管理员',
        '$2a$10$Kg/w2Tcsqbu1RsmcuZgW4uRseXBFuAADY3Eavl9yplcPRHRZovFHC', NULL, '', 0, NULL, 0, 0, NULL, '0',
        null, 0, now(), now(), '0', '0');
INSERT INTO org_tree (id, node_name, short_name, memo, node_type, exist_type, node_category,
                      parent_id, id_path, show_order, tenant_id, relate_id, delete_time, create_time,
                      update_time, create_user, update_user)
VALUES ('0-ORG-000000', '根组织', '根组织', '', 0, 1, 1, '', '0-ORG-000000', 0, '0', null, 0, now(),
        now(), '0', '0'),
       ('0-ORG-000001', '平台管理', '平台管理', '', 0, 1, 1, '0-ORG-000000', '0-ORG-000000/0-ORG-000001', 0, '0', null,
        0, now(),
        now(), '0', '0');

INSERT INTO user_org (id, user_id, org_id, node_id, tenant_id, main_job, create_user, update_user,
                      create_time, update_time)
VALUES ('01KE8DW1HB7X41NK3Q9K6QCCHC', '01KE8DW1DK2KPAZJAENJW6SSFE', '0-ORG-000001', '0-ORG-000001', '0', 1, '0', '0',
        now(), now());