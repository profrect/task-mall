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
 * 角色信息表(ManageRoleInfo)实体类
 *
 * @author gmxu
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "manage_role_info", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class ManageRoleInfo extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = -21563363368228985L;

    private String roleCode;
    private String roleName;
    private Integer level;
    private String desc;
}

