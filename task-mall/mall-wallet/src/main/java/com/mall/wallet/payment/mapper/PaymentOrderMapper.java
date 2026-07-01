package com.mall.wallet.payment.mapper;

import com.mall.wallet.payment.model.entity.PaymentOrder;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
}