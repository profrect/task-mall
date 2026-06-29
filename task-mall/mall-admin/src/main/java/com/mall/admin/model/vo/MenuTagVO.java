package com.mall.admin.model.vo;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class MenuTagVO {

    private List<Long> systemMenuIds;

    private List<MenuTreeVO> systemMenuTreeList;


    public MenuTagVO(){
        systemMenuIds = Collections.emptyList();
        systemMenuTreeList = Collections.emptyList();
    }
}
