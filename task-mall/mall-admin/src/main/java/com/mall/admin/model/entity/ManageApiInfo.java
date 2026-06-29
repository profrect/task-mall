package com.mall.admin.model.entity;


import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统接口资源信息表(ManageApiInfo)实体类
 *
 * @author gmxu
 */
@Data
@Table(value = "manage_api_info", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class ManageApiInfo extends BaseEntity<Long> implements Serializable {
    private static final long serialVersionUID = -74143687001078412L;

    private String apiCode;
    private String apiName;
    private String apiUrl;
    private String apiMethod;
    private Integer status;
    private Integer type;
    private String remark;
}

