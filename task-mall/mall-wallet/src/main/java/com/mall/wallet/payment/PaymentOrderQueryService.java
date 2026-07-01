package com.mall.wallet.payment;

import com.mall.common.model.dto.req.PaymentOrderQueryReq;
import com.mall.common.model.dto.resp.PaymentOrderResp;
import com.mall.wallet.payment.mapper.PaymentOrderMapper;
import com.mall.wallet.payment.model.entity.PaymentOrder;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

/** 支付审计只读查询。 */
@Service
public class PaymentOrderQueryService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @Resource
    private PaymentOrderMapper paymentOrderMapper;

    public Page<PaymentOrderResp> page(PaymentOrderQueryReq req) {
        PaymentOrderQueryReq q = req == null ? new PaymentOrderQueryReq() : req;
        long pageNumber = q.getPageNumber() <= 0 ? 1 : q.getPageNumber();
        long pageSize = q.getPageSize() <= 0 ? DEFAULT_PAGE_SIZE : Math.min(q.getPageSize(), MAX_PAGE_SIZE);
        QueryWrapper wrapper = QueryWrapper.create()
                .from(PaymentOrder.class)
                .eq(PaymentOrder::getUserId, q.getUserId(), q.getUserId() != null)
                .eq(PaymentOrder::getBusinessType, upper(q.getBusinessType()), StringUtils.hasText(q.getBusinessType()))
                .eq(PaymentOrder::getChannelCode, upper(q.getChannelCode()), StringUtils.hasText(q.getChannelCode()))
                .eq(PaymentOrder::getStatus, upper(q.getStatus()), StringUtils.hasText(q.getStatus()))
                .like(PaymentOrder::getOrderNo, q.getOrderNo(), StringUtils.hasText(q.getOrderNo()))
                .ge(PaymentOrder::getCreateTime, q.getStartTime(), q.getStartTime() != null)
                .le(PaymentOrder::getCreateTime, q.getEndTime(), q.getEndTime() != null)
                .orderBy(PaymentOrder::getId, false);
        return paymentOrderMapper.paginate(pageNumber, pageSize, wrapper).map(this::toResp);
    }

    private PaymentOrderResp toResp(PaymentOrder o) {
        PaymentOrderResp r = new PaymentOrderResp();
        r.setId(o.getId());
        r.setOrderNo(o.getOrderNo());
        r.setUserId(o.getUserId());
        r.setBusinessType(o.getBusinessType());
        r.setBusinessOrderNo(o.getBusinessOrderNo());
        r.setChannelCode(o.getChannelCode());
        r.setChannelOrderNo(o.getChannelOrderNo());
        r.setCurrency(o.getCurrency());
        r.setAmount(o.getAmount());
        r.setStatus(o.getStatus());
        r.setPayAddress(o.getPayAddress());
        r.setPayerAddress(o.getPayerAddress());
        r.setTxHash(o.getTxHash());
        r.setAuditRemark(o.getAuditRemark());
        r.setPaidAt(o.getPaidAt());
        r.setExpiredAt(o.getExpiredAt());
        r.setCreateTime(o.getCreateTime());
        r.setUpdateTime(o.getUpdateTime());
        return r;
    }

    private String upper(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : value;
    }
}