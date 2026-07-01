package com.mall.admin.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/** 后台系统参数。 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "admin_system_param", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class AdminSystemParam extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 3940868762697966580L;

    @Column("param_key")
    private String paramKey;

    @Column("param_value")
    private String paramValue;

    private String description;

    @Column("sort_order")
    private Integer sortOrder;

    private Integer status;
}