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
 * 用户-用户角色关联关系表(ManageUserRole)实体类
 *
 * @author gmxu
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "manage_user_role", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class ManageUserRole extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = -66289697415333610L;

    private String username;
    private String roleCode;
}

