<template>
  <div class="account-page">
    <van-nav-bar title="账户中心" left-arrow fixed placeholder @click-left="router.back()" />

    <div class="user-card">
      <div>
        <div class="user-name">{{ displayName }}</div>
        <div class="user-sub">UID {{ userInfo.userId || '-' }} · VIP{{ userInfo.vipLevel || '0' }}</div>
      </div>
      <van-tag type="warning" effect="dark">{{ userInfo.inviteCode || 'No Invite Code' }}</van-tag>
    </div>

    <div class="asset-card">
      <div>
        <div class="asset-label">可用余额</div>
        <div class="asset-value">{{ moneyText(overview.availBalance) }} {{ overview.currency }}</div>
      </div>
      <van-button size="small" round type="primary" @click="router.push('/account/balance')">
        查看账变
      </van-button>
    </div>

    <div class="action-grid">
      <div v-for="item in quickActions" :key="item.path" class="action-item" @click="router.push(item.path)">
        <div class="icon-box" :class="item.tone">
          <van-icon :name="item.icon" />
        </div>
        <span>{{ item.label }}</span>
      </div>
    </div>

    <van-cell-group inset class="cell-group" title="账户资料">
      <van-cell title="个人资料" value="已接入" is-link to="/account/person" />
      <van-cell title="VIP 中心" value="等级与权益" is-link to="/vip" />
      <van-cell title="VIP 升级记录" value="待独立接口" is-link to="/account/vipUplog" />
      <van-cell title="邀请好友" value="邀请码/链接" is-link to="/invite" />
      <van-cell title="收益记录" value="返佣收益" is-link to="/income" />
    </van-cell-group>

    <van-cell-group inset class="cell-group" title="资金与安全">
      <van-cell title="资金资料" value="待开放" is-link to="/account/financial" />
      <van-cell title="修改密码" value="待开放" is-link to="/account/changePwd" />
      <van-cell title="Google 验证" value="待开放" is-link to="/account/googleAuth" />
      <van-cell title="实名认证" value="待开放" is-link to="/account/kyc" />
    </van-cell-group>

    <van-cell-group inset class="cell-group" title="服务与内容">
      <van-cell title="优惠券" value="待开放" is-link to="/coupon" />
      <van-cell title="公告" value="已接入" is-link to="/notice" />
      <van-cell title="帮助中心" value="待内容接口" is-link to="/help" />
      <van-cell title="联系客服" value="待配置" is-link to="/service" />
    </van-cell-group>

    <div class="bottom-space"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, type UserDetail } from '@/api/user'
import { getOverview, type WalletOverview } from '@/api/wallet'

const router = useRouter()

const userInfo = ref<UserDetail>({
  userId: 0,
  account: '',
  nickName: '',
  vipLevel: '0',
  inviteCode: '',
  inviteUser: '',
  teamMemberNum: 0,
})

const overview = ref<WalletOverview>({
  userId: 0,
  currency: 'USDT',
  totalBalance: 0,
  availBalance: 0,
  frozenBalance: 0,
})

const quickActions = [
  { label: '充值', path: '/account/recharge', icon: 'plus', tone: 'green' },
  { label: '提现', path: '/account/withDraw', icon: 'minus', tone: 'orange' },
  { label: '转账', path: '/account/transfer', icon: 'exchange', tone: 'blue' },
  { label: '账变', path: '/account/balance', icon: 'bill-o', tone: 'purple' },
]

const displayName = computed(() => userInfo.value.nickName || userInfo.value.account || 'User')
const moneyText = (value?: number) => Number(value || 0).toFixed(2)

async function loadData() {
  userInfo.value = await getCurrentUser()
  overview.value = await getOverview()
}

onMounted(loadData)
</script>

<style scoped>
.account-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: env(safe-area-inset-bottom);
}
.user-card,
.asset-card {
  margin: 12px;
  padding: 18px 16px;
  border-radius: 14px;
  color: #fff;
}
.user-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
}
.user-name {
  font-size: 20px;
  font-weight: 700;
}
.user-sub {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.85;
}
.asset-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #1565c0 0%, #1e88e5 100%);
}
.asset-label {
  font-size: 12px;
  opacity: 0.85;
}
.asset-value {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 700;
}
.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin: 12px;
  padding: 14px 8px;
  background: #fff;
  border-radius: 14px;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  color: #333;
}
.icon-box {
  width: 42px;
  height: 42px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}
.icon-box.green {
  background: #4caf50;
}
.icon-box.orange {
  background: #ff9800;
}
.icon-box.blue {
  background: #2196f3;
}
.icon-box.purple {
  background: #7e57c2;
}
.cell-group {
  margin: 12px;
  overflow: hidden;
  border-radius: 12px;
}
.bottom-space {
  height: 72px;
}
</style>