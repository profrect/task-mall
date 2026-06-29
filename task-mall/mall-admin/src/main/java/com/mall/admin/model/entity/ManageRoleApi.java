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
 * 角色-接口关联关系表(ManageRoleApi)实体类
 *
 * @author gmxu
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "manage_role_api", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class ManageRoleApi extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = -77673015830605485L;

    private String roleCode;
    private String apiCode;
}

