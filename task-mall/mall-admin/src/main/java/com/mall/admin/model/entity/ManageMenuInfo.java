package com.mall.admin.model.entity;


import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统菜单资源信息表(ManageMenuInfo)实体类
 *
 * @author gmxu
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "manage_menu_info", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class ManageMenuInfo extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 576732105229207933L;

    private String menuKey;
    private String name;
    private String path;
    private Integer type;
    private String component;
    private Integer hideInMenu;
    private String icon;
    private Long pno;
    private Integer menuLevel;
    private Integer order;
    private Integer status;
}

