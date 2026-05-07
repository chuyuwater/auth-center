-- 消息管理模块 v1.1.0
-- 包含：消息渠道、消息模板、模板内容、发送方案、方案渠道、消息日志、选人规则、规则条件

create table if not exists msg_channel
(
    id           char(26)                               not null comment 'ulid'
        primary key,
    channel_no   varchar(30)                            not null comment '渠道编号，如A-MessageCh-000001',
    channel_name varchar(20)                            not null comment '渠道名称',
    channel_type varchar(20)                            not null comment '渠道类型：wechat/sms/dingtalk/site_msg',
    provider     varchar(30)                            not null comment '服务商编码',
    config_json  text                                   null comment '渠道配置参数JSON',
    description  varchar(200) default ''                not null comment '渠道说明',
    forbidden    tinyint      default 0                 not null comment '0-启用，1-禁用',
    tenant_id    varchar(20)                            not null comment '租户id',
    create_user  char(26)     default '0'               not null,
    update_user  char(26)     default '0'               not null,
    create_time  datetime     default CURRENT_TIMESTAMP not null,
    update_time  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    delete_time  bigint       default 0                 not null,
    constraint ux_msg_channel_no
        unique (channel_no, delete_time),
    constraint ux_msg_channel_name
        unique (channel_name, tenant_id, delete_time)
) comment '消息渠道';

create index ix_msg_channel_tenant
    on msg_channel (tenant_id, channel_type, forbidden);

create table if not exists msg_template
(
    id           char(26)                               not null comment 'ulid'
        primary key,
    template_no  varchar(30)                            not null comment '模板编号，如A-MessageTem-000001',
    template_name varchar(20)                           not null comment '模板名称',
    msg_type     varchar(20)                            not null comment '消息类型：task/alarm/warning/message/notice/activity',
    biz_entity   varchar(50)                            not null comment '业务实体，字典MSG_BIZ_ENTITY',
    group_id     varchar(26)                            not null comment '所属分组，字典分组id',
    description  varchar(200) default ''                not null comment '模板说明',
    forbidden    tinyint      default 0                 not null comment '0-启用，1-禁用',
    tenant_id    varchar(20)                            not null comment '租户id',
    create_user  char(26)     default '0'               not null,
    update_user  char(26)     default '0'               not null,
    create_time  datetime     default CURRENT_TIMESTAMP not null,
    update_time  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    delete_time  bigint       default 0                 not null,
    constraint ux_msg_template_no
        unique (template_no, delete_time),
    constraint ux_msg_template_name
        unique (template_name, tenant_id, delete_time)
) comment '消息模板';

create index ix_msg_template_tenant
    on msg_template (tenant_id, msg_type, forbidden);

create table if not exists msg_template_content
(
    id                 char(26)                               not null comment 'ulid'
        primary key,
    template_id        char(26)                               not null comment '消息模板id',
    msg_title          varchar(50)                            not null comment '消息标题',
    msg_content        varchar(255)                           not null comment '消息内容，支持{变量}占位符；短信渠道仅作平台侧预览',
    channel_id         char(26)                               not null comment '消息渠道id',
    third_template_id  varchar(100) default ''                not null comment '第三方平台模板ID，短信渠道必填（阿里云TemplateCode/腾讯云TemplateId）',
    show_order         int          default 0                 not null comment '排序',
    tenant_id          varchar(20)                            not null comment '租户id',
    create_user        char(26)     default '0'               not null,
    update_user        char(26)     default '0'               not null,
    create_time        datetime     default CURRENT_TIMESTAMP not null,
    update_time        datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    delete_time        bigint       default 0                 not null,
    constraint ux_msg_template_content_template_channel
        unique (template_id, channel_id, delete_time)
) comment '消息模板内容';

create index ix_msg_template_content_template
    on msg_template_content (template_id, show_order);

create table if not exists msg_scheme
(
    id           char(26)                               not null comment 'ulid'
        primary key,
    scheme_no    varchar(30)                            not null comment '方案编号，如A-MessageSch-000001',
    scheme_name  varchar(20)                            not null comment '方案名称',
    template_id  char(26)                               not null comment '关联消息模板id',
    biz_type     varchar(20)                            not null comment '业务类型，自动从模板回填',
    group_id     varchar(26)                            not null comment '所属分组，字典分组id',
    receiver_type tinyint     default 0                 not null comment '接收人策略：0-动态传参，1-规则计算',
    rule_id      char(26)     default ''                not null comment '选人规则id（receiver_type=1时必填）',
    retry_enabled tinyint     default 1                 not null comment '是否启用重试',
    retry_interval int        default 20                not null comment '重试间隔(秒)',
    retry_max_count int       default 5                 not null comment '最大重试次数',
    description  varchar(200) default ''                not null comment '方案说明',
    forbidden    tinyint      default 0                 not null comment '0-启用，1-禁用',
    tenant_id    varchar(20)                            not null comment '租户id',
    create_user  char(26)     default '0'               not null,
    update_user  char(26)     default '0'               not null,
    create_time  datetime     default CURRENT_TIMESTAMP not null,
    update_time  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    delete_time  bigint       default 0                 not null,
    constraint ux_msg_scheme_no
        unique (scheme_no, delete_time),
    constraint ux_msg_scheme_name
        unique (scheme_name, tenant_id, delete_time)
) comment '发送方案';

create index ix_msg_scheme_tenant
    on msg_scheme (tenant_id, forbidden);

create table if not exists msg_scheme_channel
(
    id           char(26)                               not null
        primary key,
    scheme_id    char(26)                               not null comment '发送方案id',
    channel_id   char(26)                               not null comment '消息渠道id',
    tenant_id    varchar(20)                            not null,
    create_time  datetime     default CURRENT_TIMESTAMP not null,
    constraint ux_msg_scheme_channel
        unique (scheme_id, channel_id)
) comment '发送方案渠道关联';

create table if not exists msg_log
(
    id              char(26)                               not null comment 'ulid'
        primary key,
    msg_title       varchar(50)                            not null comment '消息标题',
    msg_content     varchar(255)                           not null comment '消息内容',
    biz_entity      varchar(50)                            not null comment '业务实体，字典MSG_BIZ_ENTITY',
    msg_type        varchar(20)                            not null comment '消息类型：task/alarm/warning/message/notice/activity',
    target_user     char(26)                               not null comment '接收人用户id',
    target_user_name varchar(20) default ''                not null comment '接收人姓名(冗余)',
    receive_time    datetime                               not null comment '接收时间',
    view_status     tinyint      default 0                 not null comment '0-未读，1-已读',
    send_status     tinyint      default 0                 not null comment '发送状态：0-发送中，1-成功，2-失败',
    fail_reason     varchar(500) default ''                not null comment '失败原因',
    jump_url        varchar(256) default ''                not null comment '跳转地址',
    scheme_id       char(26)     default ''                not null comment '发送方案id(方案触发时)',
    channel_id      char(26)     default ''                not null comment '消息渠道id',
    retry_count     int          default 0                 not null comment '已重试次数',
    origin_json     text                                   null comment '原始报文',
    src_app         varchar(20)  default ''                not null comment '源应用id(应用直推时)',
    src_id          varchar(100) default ''                not null comment '源消息id(应用直推时)',
    tenant_id       varchar(20)                            not null,
    create_time     datetime     default CURRENT_TIMESTAMP not null,
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint ux_msg_log_target_src_channel
        unique (target_user, src_app, src_id, channel_id)
) comment '消息日志';

create index ix_msg_log_target_user
    on msg_log (target_user, view_status, msg_type, send_status, receive_time);

create index ix_msg_log_scheme
    on msg_log (scheme_id, send_status);

create index ix_msg_log_tenant_biz
    on msg_log (tenant_id, biz_entity, send_status);

create table if not exists select_rule
(
    id           char(26)                               not null comment 'ulid'
        primary key,
    rule_no      varchar(30)                            not null comment '规则编号，如A-Rule-000001',
    rule_name    varchar(20)                            not null comment '规则名称',
    biz_entity   varchar(50)                            not null comment '业务实体，字典RULE_BIZ_ENTITY',
    group_id     varchar(26)                            not null comment '所属分组，规则分组id',
    ref_count    int          default 0                 not null comment '被引用数',
    description  varchar(200) default ''                not null comment '规则说明',
    forbidden    tinyint      default 0                 not null comment '0-启用，1-禁用',
    tenant_id    varchar(20)                            not null,
    create_user  char(26)     default '0'               not null,
    update_user  char(26)     default '0'               not null,
    create_time  datetime     default CURRENT_TIMESTAMP not null,
    update_time  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    delete_time  bigint       default 0                 not null,
    constraint ux_select_rule_no
        unique (rule_no, delete_time),
    constraint ux_select_rule_name
        unique (rule_name, tenant_id, delete_time)
) comment '选人规则';

create index ix_select_rule_tenant
    on select_rule (tenant_id, biz_entity, forbidden);

create table if not exists select_rule_condition
(
    id           char(26)                               not null
        primary key,
    rule_id      char(26)                               not null comment '规则id',
    cond_type    varchar(30)                            not null comment '条件类型：org/user/role/perm等',
    cond_op      varchar(10)  default 'in'              not null comment '操作符：in/not_in',
    cond_values  text                                   not null comment '条件值JSON数组',
    show_order   int          default 0                 not null,
    create_time  datetime     default CURRENT_TIMESTAMP not null,
    constraint ux_select_rule_cond_rule_order
        unique (rule_id, show_order)
) comment '选人规则条件';

create index ix_select_rule_cond_rule
    on select_rule_condition (rule_id);
