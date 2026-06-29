-- liquibase formatted sql
-- changeset GM:wallet_init_tables_20260629001

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