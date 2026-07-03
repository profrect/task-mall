package com.mall.user.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table(value = "user_member_group", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class UserMemberGroup extends BaseEntity<Long> {

    private String groupName;

    private String remark;

    private Integer status;

    private Integer sortOrder;
}