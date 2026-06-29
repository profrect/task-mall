package com.mall.common.db.listener;

import com.mall.common.core.context.UserContext;
import com.mall.common.db.entity.BaseEntity;
import com.mybatisflex.annotation.UpdateListener;

import java.util.Objects;

public class CustomUpdateListener implements UpdateListener {

    @Override
    public void onUpdate(Object o) {
        String userId= UserContext.currentUserId();
        long nowMillis = System.currentTimeMillis();
        if(o instanceof BaseEntity<?> be) {
            if(Objects.isNull(be.getUpdater())) {
                be.setUpdater(userId);
            }
            if(Objects.isNull(be.getUpdateTime())) {
                be.setUpdateTime(nowMillis);
            }
        }
    }
}
