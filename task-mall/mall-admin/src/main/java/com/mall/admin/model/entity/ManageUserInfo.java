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
 * 用户信息表(ManageUserInfo)实体类
 *
 * @author gmxu
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "manage_user_info", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class ManageUserInfo extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 823273566776838779L;

    private String username;
    private String realName;
    private String passwordHash;
    private String email;
    private Integer level;
}

