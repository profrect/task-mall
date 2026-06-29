package com.mall.common.db.listener;

import com.mall.common.core.context.UserContext;
import com.mall.common.db.entity.BaseEntity;
import com.mybatisflex.annotation.InsertListener;

import java.util.Objects;

public class CustomInsertListener implements InsertListener {

    @Override
    public void onInsert(Object o) {
        String userId= UserContext.currentUserId();
        long nowMillis = System.currentTimeMillis();
        if(o instanceof BaseEntity<?> be) {
            if(Objects.isNull(be.getCreator())) {
                be.setCreator(userId);
            }
            if(Objects.isNull(be.getCreateTime())) {
                be.setCreateTime(nowMillis);
            }
        }
    }
}
