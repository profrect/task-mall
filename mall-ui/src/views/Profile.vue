<template>
  <div class="profile-page">
    <van-nav-bar title="我的" left-arrow fixed placeholder @click-left="$router.back()" />
    <!-- 用户身份总览 -->
    <div class="user-card">
      <div class="avatar-box">
        <van-icon name="user-o" size="28" />
      </div>
      <div class="user-info">
        <div class="name-row">
          <span class="nickname">{{ userInfo.nickname }}</span>
          <van-tag type="warning" effect="dark" size="medium">VIP{{ userInfo.vipLevel }}</van-tag>
        </div>
        <div class="account-row">
          <span class="account">ID: {{ userInfo.accountId }}</span>
          <div class="invite-code" @click="copyInviteCode">
            <span>{{ userInfo.inviteCode }}</span>
            <van-icon name="records" size="10" />
          </div>
        </div>
      </div>
    </div>

    <!-- 关系透视条 -->
    <div class="relation-bar">
      <div class="rel-item" @click="router.push('/team')">
        <span class="rel-label">Direct Superior</span>
        <span class="rel-val">{{ userInfo.superior || 'Platform Official' }}</span>
      </div>
      <div class="rel-divider"></div>
      <div class="rel-item" @click="router.push('/team?tab=members')">
        <span class="rel-label">Team Members</span>
        <span class="rel-val">{{ userInfo.teamCount }}</span>
      </div>
    </div>

    <!-- VIP等级升级列表 (非分页) -->
    <div class="section-title">VIP Upgrade</div>
    <div class="vip-list">
      <VipCard
        v-for="vip in vipList"
        :key="vip.level"
        :vip="vip"
        :current-level="userInfo.vipLevel"
        @upgrade="handleUpgrade"
      />
    </div>

    <!-- 安全设置 -->
    <div class="section-title">Security Settings</div>
    <van-cell-group inset class="security-group">
      <van-cell title="Login Password" icon="lock" is-link to="/profile/security/login-pwd" />
      <van-cell
        title="Security Questions"
        icon="shield-o"
        is-link
        to="/profile/security/questions"
      />
      <van-cell title="Fund Password" icon="balance-pay" is-link to="/profile/security/fund-pwd" />
    </van-cell-group>

    <div class="bottom-placeholder"></div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { showToast } from 'vant'
import VipCard from '@/components/profile/VipCard.vue'
import router from '@/router'

// TODO: 替换为 /api/user/profile
const userInfo = ref({
  nickname: 'Alex_GM',
  accountId: 'U20260625001',
  vipLevel: 2,
  inviteCode: 'AX8K2M',
  superior: 'Sarah_Li',
  teamCount: 42,
})

// TODO: 替换为 /api/vip/list (全量返回，无需分页)
const vipList = ref([
  { level: 1, name: 'VIP1 Bronze', price: 0, benefits: ['Basic Rewards', 'Standard Support'] },
  {
    level: 2,
    name: 'VIP2 Silver',
    price: 99,
    benefits: ['1.2x Rewards', 'Priority Support', 'Team Bonus'],
  },
  {
    level: 3,
    name: 'VIP3 Gold',
    price: 299,
    benefits: ['1.5x Rewards', 'Dedicated Manager', 'Exclusive Events'],
  },
  {
    level: 4,
    name: 'VIP4 Platinum',
    price: 599,
    benefits: ['2.0x Rewards', 'Custom Strategy', 'VIP Lounge Access'],
  },
])

const copyInviteCode = () => {
  navigator.clipboard.writeText(userInfo.value.inviteCode)
  showToast('Invite code copied')
}

const handleUpgrade = (vip: any) => {
  console.log('Upgrade to VIP:', vip.level)
  // TODO: 跳转至支付确认页或弹出支付弹窗
}
</script>

<style scoped>
.profile-page {
  background: #f5f6fa;
  min-height: 100vh;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 用户身份卡片 */
.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 12px;
  padding: 24px 20px;
  background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
  border-radius: 12px;
  color: #fff;
}
.avatar-box {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.user-info {
  flex: 1;
  min-width: 0;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.nickname {
  font-size: 20px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.account-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  opacity: 0.85;
}
.invite-code {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 4px;
  cursor: pointer;
}

/* 关系透视条 */
.relation-bar {
  display: flex;
  align-items: stretch;
  margin: 0 12px 12px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}
.rel-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 8px;
  gap: 4px;
  cursor: pointer;
}
.rel-label {
  font-size: 11px;
  color: #999;
}
.rel-val {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  text-align: center;
}
.rel-divider {
  width: 1px;
  background: #f0f0f0;
  margin: 12px 0;
}

.section-title {
  padding: 16px 16px 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* VIP列表 */
.vip-list {
  padding: 0 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 安全设置 */
.security-group {
  margin: 0 12px;
  border-radius: 12px;
  overflow: hidden;
}
.bottom-placeholder {
  height: 70px;
}
</style>
