package com.mall.admin.model.vo;


import lombok.Data;

import java.io.Serializable;

@Data
public class ManageApiInfoVO implements Serializable {
    private static final long serialVersionUID = 374423063507672200L;
    private Long id;
    private String apiCode;
    private String apiName;
    private String apiUrl;
    private String method;
    private Integer status;
    private Integer type;
    private String remark;
    private String creator;
    private String updater;
    private Long createTime;
    private Long updateTime;

}

