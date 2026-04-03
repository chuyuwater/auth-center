create table if not exists user_custom_style
(
    id          char(26)                           not null comment 'ulid'
        primary key,
    item        varchar(100)                       not null comment '自定义项，字典CUSTOM_STYLE',
    setting     varchar(255)                       not null comment '用户设置',
    user_id     char(26)                           not null comment '用户',
    create_time datetime default CURRENT_TIMESTAMP not null,
    uptime_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint ux_user_custom_style
        unique (user_id, item)
) comment '用户自定义页面风格';

INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time, update_time,
                                         delete_time)
VALUES ('01KN906DTS3WM2XG4WZQMDN2D8', 'portal', '', 'USER_CUSTOM_STYLE', '用户自定义元素', 1, '',
        '01KN906DTS3WM2XG4WZQMDN2D8', 0, 0, '0', '0',
        '2026-04-03 14:23:56', '2026-04-03 14:23:56', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time, update_time,
                                         delete_time)
VALUES ('01KN907HJCC5YEH7FM7F7HJXFZ', 'portal', 'USER_CUSTOM_STYLE', 'NAV_TYPE', '导航风格', 1,
        '01KN906DTS3WM2XG4WZQMDN2D8', '01KN906DTS3WM2XG4WZQMDN2D8/01KN907HJCC5YEH7FM7F7HJXFZ', 1, 0,
        '0', '0', '2026-04-03 14:24:32', '2026-04-03 14:24:32', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time, update_time,
                                         delete_time)
VALUES ('01KN90844WJC4AKC5QQFABZQEQ', 'portal', 'NAV_TYPE', '0', '菜单', 1, '01KN907HJCC5YEH7FM7F7HJXFZ',
        '01KN906DTS3WM2XG4WZQMDN2D8/01KN907HJCC5YEH7FM7F7HJXFZ/01KN90844WJC4AKC5QQFABZQEQ', 1, 0,
        '0', '0', '2026-04-03 14:24:51', '2026-04-03 14:24:51', 0);
INSERT INTO portal_auth_center.sys_dict (id, app_id, feat_code, value_str, value_cn, dict_type, parent_id, id_path,
                                         show_order, forbidden, create_user, update_user, create_time, update_time,
                                         delete_time)
VALUES ('01KN908J9T0ZA6CEBQKEQNWZYC', 'portal', 'NAV_TYPE', '1', '应用', 1, '01KN907HJCC5YEH7FM7F7HJXFZ',
        '01KN906DTS3WM2XG4WZQMDN2D8/01KN907HJCC5YEH7FM7F7HJXFZ/01KN908J9T0ZA6CEBQKEQNWZYC', 2, 0,
        '0', '0', '2026-04-03 14:25:05', '2026-04-03 14:25:05', 0);
