package com.mall.promotion.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class PromotionCheckinStateVO {

    private Boolean checkedToday;

    private Integer todayDate;

    private Integer consecutiveDays;

    private PromotionCheckinRuleVO todayRule;

    private PromotionCheckinRecordVO todayRecord;

    private List<PromotionCheckinRuleVO> rules;

    private List<PromotionCheckinRecordVO> recentRecords;
}