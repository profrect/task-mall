CREATE TABLE `manage_api_info`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `api_code`    varchar(200) NOT NULL COMMENT '接口 code',
    `api_name`    varchar(50) NULL DEFAULT NULL COMMENT '接口名称',
    `api_url`     varchar(255) NULL DEFAULT NULL COMMENT '接口地址',
    `api_method`  varchar(20) NULL DEFAULT NULL COMMENT 'HTTP方法(GET、POST等)',
    `status`      tinyint NULL DEFAULT NULL COMMENT '接口状态(0：禁用；1：启用)',
    `type`        tinyint NULL DEFAULT NULL COMMENT '接口类型（用户管理、权限管理等）',
    `remark`      varchar(255) NULL DEFAULT NULL COMMENT '备注/描述',
    `creator`     varchar(50) NULL DEFAULT '' COMMENT '添加人',
    `updater`     varchar(50) NULL DEFAULT '' COMMENT '最后修改人',
    `create_time` bigint NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time` bigint NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code`(`api_code`) USING BTREE,
    INDEX         `idx_url_status`(`api_url`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '系统接口资源信息表';

CREATE TABLE `manage_menu_info`
(
    `id`           bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `menu_key`     varchar(100) NOT NULL COMMENT '菜单key',
    `name`         varchar(50) NULL DEFAULT '' COMMENT '路由名称',
    `path`         varchar(50) NULL DEFAULT '' COMMENT '路由路径',
    `type`         tinyint NULL DEFAULT 1 COMMENT '菜单类型（0-页面，1-菜单（默认））',
    `component`    varchar(100) NULL DEFAULT '' COMMENT '组件路径(vue页面完整路径，省略.vue后缀)',
    `hide_in_menu` tinyint NULL DEFAULT NULL COMMENT '显示状态(1-显示，0-隐藏)',
    `icon`         varchar(60) NULL DEFAULT '' COMMENT '菜单图标',
    `pno`          bigint NULL DEFAULT NULL COMMENT '上级菜单编号（如果没有上级，则为0）',
    `menu_level`   tinyint NULL DEFAULT NULL COMMENT '菜单级别（顶级菜单为1，下级菜单逐级递增）',
    `order`        tinyint NULL DEFAULT NULL COMMENT '排序编号（同级、同父级菜单依次递增）',
    `status`       tinyint NULL DEFAULT NULL COMMENT '菜单状态（1-正常）',
    `creator`      varchar(50) NULL DEFAULT '' COMMENT '添加人',
    `updater`      varchar(50) NULL DEFAULT '' COMMENT '最后修改人',
    `create_time`  bigint NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`  bigint NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_key`(`menu_key`) USING BTREE COMMENT '代码唯一索引',
    INDEX          `idx_pno`(`pno`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 COMMENT = '系统菜单资源信息表';

CREATE TABLE `manage_role_api`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `role_code`   varchar(50)  NOT NULL COMMENT '角色code',
    `api_code`    varchar(200) NOT NULL COMMENT '接口 code',
    `creator`     varchar(50) NULL DEFAULT '' COMMENT '添加人',
    `updater`     varchar(50) NULL DEFAULT '' COMMENT '最后修改人',
    `create_time` bigint NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time` bigint NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_role_api`(`role_code`, `api_code`) USING BTREE COMMENT '接口-角色索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '角色-接口关联关系表';

CREATE TABLE `manage_role_info`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_code`   varchar(50) NOT NULL COMMENT '角色代码(唯一)',
    `role_name`   varchar(50) NULL DEFAULT '' COMMENT '角色名称',
    `level`       int NULL DEFAULT NULL COMMENT '等级',
    `desc`        varchar(255) NULL DEFAULT '' COMMENT '角色说明',
    `creator`     varchar(50) NULL DEFAULT '' COMMENT '添加人',
    `updater`     varchar(50) NULL DEFAULT '' COMMENT '最后修改人',
    `create_time` bigint NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time` bigint NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_code`(`role_code`) USING BTREE COMMENT '代码唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '角色信息表';

CREATE TABLE `manage_role_menu`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `role_code`   varchar(50)  NOT NULL COMMENT '角色code',
    `menu_key`    varchar(100) NOT NULL COMMENT '菜单id',
    `creator`     varchar(50) NULL DEFAULT '' COMMENT '添加人',
    `updater`     varchar(50) NULL DEFAULT '' COMMENT '最后修改人',
    `create_time` bigint NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time` bigint NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_role_menu`(`role_code`, `menu_key`) USING BTREE COMMENT '菜单-角色索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '角色-菜单关联关系表';

CREATE TABLE `manage_user_info`
(
    `id`            bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      varchar(50)  NOT NULL COMMENT '用户名(唯一)',
    `real_name`     varchar(50) NULL DEFAULT '' COMMENT '真实姓名',
    `password_hash` varchar(255) NOT NULL COMMENT '账户密码',
    `email`         varchar(40) NULL DEFAULT '' COMMENT '账户绑定邮箱',
    `level`         tinyint NULL DEFAULT NULL COMMENT '用户级别',
    `creator`       varchar(50) NULL DEFAULT '' COMMENT '添加人',
    `updater`       varchar(50) NULL DEFAULT '' COMMENT '最后修改人',
    `create_time`   bigint NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`   bigint NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_name`(`username`) USING BTREE COMMENT '名称唯一索引',
    INDEX           `idx_nm_pswd`(`username`, `password_hash`) USING BTREE COMMENT '帐号密码登陆联合索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '用户信息表';

CREATE TABLE `manage_user_role`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `username`    varchar(50) NOT NULL COMMENT '用户名',
    `role_code`   varchar(50) NOT NULL COMMENT '角色code',
    `creator`     varchar(50) NULL DEFAULT '' COMMENT '添加人',
    `updater`     varchar(50) NULL DEFAULT '' COMMENT '最后修改人',
    `create_time` bigint NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time` bigint NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_role`(`username`, `role_code`) USING BTREE COMMENT '用户-角色索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '用户-用户角色关联关系表';

CREATE TABLE `admin_content_item`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `type`          varchar(32)  NOT NULL COMMENT '内容类型：COMPANY_PROFILE/PLATFORM_PROFILE/REGULATOR/NOTICE/USER_AGREEMENT/USER_PRIVACY',
    `language_code` varchar(16)  NOT NULL DEFAULT 'zh-CN' COMMENT '语言编码',
    `title`         varchar(128) NOT NULL COMMENT '标题',
    `summary`       varchar(255) NULL DEFAULT '' COMMENT '摘要',
    `content`       text         NOT NULL COMMENT '正文',
    `sort_order`    int          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `creator`       varchar(50)  NULL DEFAULT '' COMMENT '添加人',
    `updater`       varchar(50)  NULL DEFAULT '' COMMENT '最后修改人',
    `create_time`   bigint       NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`   bigint       NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_type_lang_status`(`type`, `language_code`, `status`) USING BTREE,
    INDEX `idx_sort`(`sort_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '后台运营内容配置表';

CREATE TABLE `admin_system_param`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `param_key`   varchar(96)  NOT NULL COMMENT '参数键',
    `param_value` text         NULL COMMENT '参数值',
    `description` varchar(255) NULL DEFAULT '' COMMENT '参数说明',
    `sort_order`  int          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    `creator`     varchar(50)  NULL DEFAULT '' COMMENT '添加人',
    `updater`     varchar(50)  NULL DEFAULT '' COMMENT '最后修改人',
    `create_time` bigint       NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time` bigint       NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_param_key`(`param_key`) USING BTREE,
    INDEX `idx_status_sort`(`status`, `sort_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '后台系统参数表';

CREATE TABLE `admin_operation_log`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `admin_account` varchar(64)  NOT NULL DEFAULT '' COMMENT '管理员账号',
    `method`        varchar(16)  NOT NULL DEFAULT '' COMMENT '请求方法',
    `path`          varchar(512) NOT NULL DEFAULT '' COMMENT '请求路径',
    `query_string`  varchar(1024) NULL DEFAULT '' COMMENT '请求查询参数',
    `action`        varchar(128) NOT NULL DEFAULT '' COMMENT '操作动作',
    `status_code`   int          NOT NULL DEFAULT 0 COMMENT '响应状态码',
    `success`       tinyint      NOT NULL DEFAULT 0 COMMENT '是否成功：1-成功，0-失败',
    `duration_ms`   bigint       NOT NULL DEFAULT 0 COMMENT '请求耗时毫秒',
    `ip_address`    varchar(64)  NULL DEFAULT '' COMMENT '操作IP地址',
    `user_agent`    varchar(512) NULL DEFAULT '' COMMENT '浏览器标识',
    `creator`       varchar(50)  NULL DEFAULT '' COMMENT '添加人',
    `updater`       varchar(50)  NULL DEFAULT '' COMMENT '最后修改人',
    `create_time`   bigint       NULL DEFAULT NULL COMMENT '数据创建时间（UTC毫秒）',
    `update_time`   bigint       NULL DEFAULT NULL COMMENT '数据最后修改时间（UTC毫秒）',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_admin_create`(`admin_account`, `create_time`) USING BTREE,
    INDEX `idx_action_create`(`action`, `create_time`) USING BTREE,
    INDEX `idx_success_create`(`success`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '后台操作日志表';
