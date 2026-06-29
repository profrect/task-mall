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
 * 角色-菜单关联关系表(ManageRoleMenu)实体类
 *
 * @author gmxu
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "manage_role_menu", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class ManageRoleMenu extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = -76526814587312666L;

    private String roleCode;
    private String menuKey;
}

