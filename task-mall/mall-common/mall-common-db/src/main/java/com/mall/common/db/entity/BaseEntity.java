package com.mall.common.db.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;

@Data
public class BaseEntity<T> {

    @Id(keyType = KeyType.Auto)
    private T id;

    @Column("create_time")
    private Long createTime;

    @Column("update_time")
    private Long updateTime;

    @Column("creator")
    private String creator;

    @Column("updater")
    private String updater;
}
