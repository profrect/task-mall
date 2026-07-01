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
 * 运营内容配置。用 type 区分公告、协议、简介等内容，避免每个静态页面扩散一套表结构。 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "admin_content_item", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class AdminContentItem extends BaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = -4451331898139807602L;

    private String type;
    private String languageCode;
    private String title;
    private String summary;
    private String content;
    private Integer sortOrder;
    private Integer status;
}