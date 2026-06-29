package com.mall.admin.model.vo;

import com.mall.admin.model.entity.ManageApiInfo;
import lombok.Data;

import java.util.List;

/**
 * @author xiaoc
 */
@Data
public class ServerApiListVO {

    private String typeName;

    private List<ManageApiInfo> apis;

    public ServerApiListVO() {}

    public ServerApiListVO(String typeName, List<ManageApiInfo> apis) {
        this.typeName = typeName;
        this.apis = apis;
    }
}
