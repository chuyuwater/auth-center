CREATE DATABASE IF NOT EXISTS portal_auth_center CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
use portal_auth_center;
create table app
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

create table org_tree
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
    id_path       varchar(768) default ''                not null comment '全路径，方便查询',
    show_order    int          default 0                 not null,
    tenant_id     varchar(20)                            not null comment '租户id',
    relate_id     varchar(50)  default ''                not null comment '关联代码，如项目id、第三方平台id',
    delete_time   bigint       default 0                 not null comment '逻辑删除',
    create_time   datetime     default CURRENT_TIMESTAMP not null,
    update_time   datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    create_user   char(26)     default '0'               not null,
    update_user   char(26)     default '0'               not null,
    constraint ux_org_tree_tenant_node
        unique (tenant_id, node_name, parent_id, delete_time),
    constraint ux_org_tree_tenant_short_name
        unique (short_name, tenant_id, parent_id, delete_time)
);

create unique index ux_org_tree_id_path
    on org_tree (id_path);

create index ix_org_tree_parent_id
    on org_tree (parent_id, show_order);

create table perm_tree
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
    update_time  datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    delete_time  bigint       default 0                 not null
);

create unique index ux_perm_tree_id_path
    on perm_tree (id_path);

create index ix_perm_tree_tenant_name
    on perm_tree (tenant_id, show_order, node_name);

create index ux_perm_tree_level_name
    on perm_tree (parent_id, node_name);

create table perm_unit
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

create table perm_unit_policy
(
    id          char(26)                               not null
        primary key,
    unit_id     varchar(20)                            null comment '权限单元id',
    policy_fe   varchar(768) default ''                not null comment '前端规则表达式',
    policy_be   varchar(768) default ''                not null comment '后端表达式',
    policy_sql  varchar(768) default ''                not null comment '对应的sql表达式（宽表）',
    tenant_id   varchar(20)                            not null,
    create_user char(26)     default '0'               not null,
    update_user char(26)     default '0'               not null,
    create_time datetime     default CURRENT_TIMESTAMP not null,
    update_time datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    constraint ux_perm_rule_unit
        unique (unit_id)
);

create table perm_unit_resource
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

create table perm_unit_user
(
    id          char(26)                           not null
        primary key,
    unit_id     varchar(20)                        not null,
    user_id     char(26)                           not null,
    org_id      varchar(20)                        null comment '主体域1：组织id',
    tenant_id   varchar(20)                        not null,
    create_user char(26) default '0'               not null,
    update_user char(26) default '0'               not null,
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update current_timestamp,
    constraint ux_perm_unit_user_unit
        unique (unit_id, org_id, user_id)
);

create index ix_perm_unit_user
    on perm_unit_user (user_id, tenant_id);

create table resource_perm
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

create table resource_tree
(
    id          char(26)                               not null comment 'ulid'
        primary key,
    name_cn     varchar(50)                            null comment '中文名',
    app_id      varchar(50)                            not null comment '应用id',
    custom_id   varchar(50)                            not null comment '应用开发者自定义菜单id',
    parent_id   char(26)     default ''                not null comment '父节点',
    id_path     varchar(768)                           not null comment '节点全路径',
    client_type tinyint      default 0                 not null comment '支持的客户端类型，0-全端，1-PC端，2-移动端',
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

create unique index ux_res_tree_id_path
    on resource_tree (id_path);

create index ix_res_tree_node_order
    on resource_tree (parent_id, show_order);

create table sys_dict
(
    id          char(26)                               not null comment 'ulid'
        primary key,
    feat_code   varchar(100)                           not null comment '字典键',
    value_str   varchar(100)                           not null comment '字典值',
    value_cn    varchar(100) default ''                not null comment '中文值',
    parent_id   varchar(26)  default ''                not null comment '父节点',
    show_order  int          default 0                 not null comment '显示顺序',
    create_time datetime     default CURRENT_TIMESTAMP not null,
    update_time datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    constraint ux_dict_kv
        unique (feat_code, value_str)
);

create index ix_sys_dict_parent_id
    on sys_dict (parent_id, show_order);

create table sys_user
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
    src_type      tinyint      default 0                 not null comment '账号来源，0-自建，1-OA同步',
    src_id        varchar(200)                           null comment '源系统id',
    passwd_expire datetime                               null comment '密码过期时间，null标识永不过期',
    tenant_id     varchar(20)                            not null comment '账号所属租户',
    last_login    datetime                               null comment '最近一次登陆时间',
    delete_time   bigint       default 0                 not null,
    create_time   datetime     default CURRENT_TIMESTAMP not null,
    update_time   datetime     default CURRENT_TIMESTAMP not null on update current_timestamp,
    create_user   char(26)     default '0'               not null,
    update_user   char(26)     default '0'               not null,
    constraint ux_sys_user_src_id
        unique (src_type, src_id, tenant_id),
    constraint ux_sys_user_tenant_account
        unique (account, tenant_id),
    constraint ux_sys_user_tenant_email
        unique (email, tenant_id),
    constraint ux_sys_user_tenant_phone
        unique (phone, tenant_id),
    constraint ux_sys_user_tenant_wecom
        unique (tenant_id, wecom_id)
);

create table tenant
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

create table tenant_app
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
        unique (app_id, tenant_id)
);

create table tenant_app_resource
(
    id          char(26)                           not null
        primary key,
    tenant_id   varchar(20)                        not null comment '租户id',
    app_id      varchar(50)                        not null comment '应用id',
    perm_id     char(26)                           null comment '权限点id',
    create_user char(26) default '0'               not null,
    update_user char(26) default '0'               not null,
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update current_timestamp,
    constraint ux_tenant_app_res
        unique (perm_id, app_id, tenant_id)
);

create table user_org
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
    update_time datetime default CURRENT_TIMESTAMP not null on update current_timestamp,
    constraint ux_user_org_user_dept
        unique (user_id, node_id)
);

create index ix_user_org_main_job
    on user_org (org_id, tenant_id, main_job);


INSERT INTO `app`
VALUES ('portal', '统一门户', '', 1, '', 0, '0', 0, '2026-01-06 01:10:24', '2026-01-06 01:10:24', '0', '0');
INSERT INTO `tenant`
VALUES ('0', '系统', '系统', '', '', 0, '姚泰然', '18502710984', '01KE8DW1DK2KPAZJAENJW6SSFE', 0, '0', '0',
        '2026-01-06 09:13:48', '2026-01-06 09:13:48');
INSERT INTO `tenant_app`
VALUES ('01KE8DY46ZGMHAZH7P7RDRZDQW', 'portal', '0', '0-ORG-000001', 1, 0, 0, '2026-01-06 09:14:57',
        '2026-01-06 09:14:57', '0', '0');
INSERT INTO `sys_user`
VALUES ('01KE8DW1DK2KPAZJAENJW6SSFE', '18502710984', '18502710984', '姚泰然',
        '$2a$10$Kg/w2Tcsqbu1RsmcuZgW4uRseXBFuAADY3Eavl9yplcPRHRZovFHC', NULL, '', 0, NULL, 0, NULL, NULL, '0',
        '2026-01-08 17:48:17', 0, '2026-01-06 09:13:48', '2026-01-06 09:13:48', '0', '0');

INSERT INTO `sys_dict`
VALUES ('01KE8EFFY2XC6ZSX1NKARS72Q0', 'ROOT', 'CLIENT_TYPE', '客户端类型', '', 0, '2026-01-06 09:24:26',
        '2026-01-06 09:24:26'),
       ('01KE8EHWVSM9FQP5C0K0G2SXVV', 'CLIENT_TYPE', '0', '全端', '01KE8EFFY2XC6ZSX1NKARS72Q0', 0,
        '2026-01-06 09:25:44', '2026-01-06 09:25:44'),
       ('01KE8EJ9WN1WCMC66ET9HGXTFD', 'CLIENT_TYPE', '1', 'PC端', '01KE8EFFY2XC6ZSX1NKARS72Q0', 0,
        '2026-01-06 09:25:58', '2026-01-06 09:25:58'),
       ('01KE8EJKRHFZAQ9CPZS6PQ2XJ3', 'CLIENT_TYPE', '2', '移动端', '01KE8EFFY2XC6ZSX1NKARS72Q0', 0,
        '2026-01-06 09:26:08', '2026-01-06 09:26:08'),
       ('01KE8EKGW3W428BJEEB9ATGAFA', 'ROOT', 'MENU_LEVEL', '显示级别', '', 0, '2026-01-06 09:26:38',
        '2026-01-06 09:26:38'),
       ('01KE8EM552768TQ7AWKFPPS2ZW', 'MENU_LEVEL', '0', '全级', '01KE8EKGW3W428BJEEB9ATGAFA', 0, '2026-01-06 09:26:58',
        '2026-01-06 09:26:58'),
       ('01KE8EMJN3A57J8VFCG55YJEQF', 'MENU_LEVEL', '1', '组织级', '01KE8EKGW3W428BJEEB9ATGAFA', 0,
        '2026-01-06 09:27:12', '2026-01-06 09:27:12'),
       ('01KE8EMSV5T1FW5YXQABJPF6JR', 'MENU_LEVEL', '2', '项目级', '01KE8EKGW3W428BJEEB9ATGAFA', 0,
        '2026-01-06 09:27:20', '2026-01-06 09:27:20'),
       ('01KE8EP5825600MR5D03YEMNZ9', 'ROOT', 'ORG_CATEGORY', '组织类型', '', 0, '2026-01-06 09:28:04',
        '2026-01-06 09:28:04'),
       ('01KE8EQ1H000TNJED9N14AAZD2', 'ORG_CATEGORY', '0', '项目部', '01KE8EP5825600MR5D03YEMNZ9', 0,
        '2026-01-06 09:28:33', '2026-01-06 09:28:33'),
       ('01KE8EQFH86ESG8WQQQC6AGQ85', 'ORG_CATEGORY', '1', '集团', '01KE8EP5825600MR5D03YEMNZ9', 0,
        '2026-01-06 09:28:47', '2026-01-06 09:28:47'),
       ('01KE8EQS0SKVNYK6GSNCRDTCV4', 'ORG_CATEGORY', '2', '公司', '01KE8EP5825600MR5D03YEMNZ9', 0,
        '2026-01-06 09:28:57', '2026-01-06 09:28:57'),
       ('01KE8ER1YCF9VJ8ZKPV3KE3KM0', 'ORG_CATEGORY', '3', '分公司', '01KE8EP5825600MR5D03YEMNZ9', 0,
        '2026-01-06 09:29:06', '2026-01-06 09:29:06'),
       ('01KE8ERCRSW4G1D9HXC9HY9JRK', 'ORG_CATEGORY', '4', '子公司', '01KE8EP5825600MR5D03YEMNZ9', 0,
        '2026-01-06 09:29:17', '2026-01-06 09:29:17');