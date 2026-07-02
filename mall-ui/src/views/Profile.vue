<template>
  <div class="profile-page">
    <van-nav-bar title="我的" left-arrow fixed placeholder @click-left="$router.back()" />

    <div class="user-card">
      <div class="avatar-box">
        <van-icon name="user-o" size="28" />
      </div>
      <div class="user-info">
        <div class="name-row">
          <span class="nickname">{{ displayName }}</span>
          <van-tag type="warning" effect="dark" size="medium">VIP{{ vipLevel }}</van-tag>
        </div>
        <div class="account-row">
          <span class="account">ID: {{ userInfo.userId || '-' }}</span>
          <div class="invite-code" @click="copyInviteCode">
            <span>{{ userInfo.inviteCode || '-' }}</span>
            <van-icon name="records" size="10" />
          </div>
        </div>
      </div>
    </div>

    <div class="relation-bar">
      <div class="rel-item" @click="router.push('/team')">
        <span class="rel-label">Direct Superior</span>
        <span class="rel-val">{{ userInfo.inviteUser || 'Platform Official' }}</span>
      </div>
      <div class="rel-divider"></div>
      <div class="rel-item" @click="router.push('/team?tab=members')">
        <span class="rel-label">Team Members</span>
        <span class="rel-val">{{ userInfo.teamMemberNum ?? 0 }}</span>
      </div>
    </div>

    <div class="section-title">Profile</div>
    <van-cell-group inset class="security-group">
      <van-cell title="Account" :value="userInfo.account || '-'" />
      <van-cell title="Nickname" :value="userInfo.nickName || '-'" is-link @click="showNickDialog = true" />
      <van-cell title="Invite Code" :value="userInfo.inviteCode || '-'" is-link @click="copyInviteCode" />
    </van-cell-group>

    <div class="section-title">VIP</div>
    <div class="vip-section">
      <van-loading v-if="vipLoading" class="vip-loading" />
      <van-empty v-else-if="!vipLevels.length" description="暂无可用 VIP 配置" image="search" />
      <div v-else class="vip-list">
        <VipCard
          v-for="item in vipLevels"
          :key="item.level"
          :vip="item"
          :current-level="vipLevel"
          @upgrade="confirmUpgrade"
        />
      </div>
    </div>

    <div class="section-title">Security Settings</div>
    <van-cell-group inset class="security-group">
      <van-cell title="Login Password" icon="lock" value="未开放" />
      <van-cell title="Security Questions" icon="shield-o" value="未开放" />
      <van-cell title="Fund Password" icon="balance-pay" value="未开放" />
    </van-cell-group>

    <van-dialog
      v-model:show="showNickDialog"
      title="修改昵称"
      show-cancel-button
      :before-close="submitNickname"
    >
      <van-field v-model="nickForm.nickname" label="昵称" placeholder="请输入昵称" maxlength="50" clearable />
    </van-dialog>

    <div class="bottom-placeholder"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant'
import router from '@/router'
import VipCard from '@/components/profile/VipCard.vue'
import {
  getCurrentUser,
  getVipLevelOverview,
  updateCurrentUser,
  upgradeVipLevel,
  type UserDetail,
  type VipLevelConfig,
} from '@/api/user'
import { rejectIfImpersonated } from '@/utils/impersonation'

const userInfo = ref<UserDetail>({
  userId: 0,
  account: '',
  nickName: '',
  vipLevel: '0',
  inviteCode: '',
  inviteUser: '',
  teamMemberNum: 0,
})
const showNickDialog = ref(false)
const nickForm = reactive({ nickname: '' })
const vipLoading = ref(false)
const vipLevels = ref<VipLevelConfig[]>([])

const vipLevel = computed(() => Number(userInfo.value.vipLevel || 0))
const displayName = computed(() => userInfo.value.nickName || userInfo.value.account || 'User')

async function loadUser() {
  userInfo.value = await getCurrentUser()
  nickForm.nickname = userInfo.value.nickName || ''
}

async function copyInviteCode() {
  if (!userInfo.value.inviteCode) return
  try {
    await navigator.clipboard.writeText(userInfo.value.inviteCode)
    showSuccessToast('邀请码已复制')
  } catch {
    showFailToast('复制失败，请手动长按选择')
  }
}

async function submitNickname(action: string): Promise<boolean> {
  if (action !== 'confirm') return true
  if (rejectIfImpersonated()) return false
  const nickname = nickForm.nickname.trim()
  if (!nickname) {
    showFailToast('昵称不能为空')
    return false
  }
  await updateCurrentUser({ nickname })
  showSuccessToast('资料已更新')
  await loadUser()
  return true
}

async function loadVipOverview() {
  vipLoading.value = true
  try {
    const overview = await getVipLevelOverview()
    vipLevels.value = overview.levels || []
    userInfo.value.vipLevel = String(overview.currentLevel ?? vipLevel.value)
  } finally {
    vipLoading.value = false
  }
}

async function confirmUpgrade(item: VipLevelConfig) {
  if (item.level <= vipLevel.value) return
  if (rejectIfImpersonated()) return
  try {
    await showConfirmDialog({
      title: `升级到 ${item.levelName}`,
      message: `确认扣款 ${item.price} USDT 购买 ${item.levelName}？`,
    })
    await upgradeVipLevel(item.level)
    showSuccessToast('VIP 升级成功')
    await Promise.all([loadUser(), loadVipOverview()])
  } catch (error) {
    if (error) {
      // 业务错误已由请求层展示；这里保留取消弹窗的安静路径。
    }
  }
}

onMounted(() => {
  loadUser()
  loadVipOverview()
})
</script>

<style scoped>
.profile-page {
  background: #f5f6fa;
  min-height: 100vh;
  padding-bottom: env(safe-area-inset-bottom);
}
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
.security-group {
  margin: 0 12px;
  border-radius: 12px;
  overflow: hidden;
}
.bottom-placeholder {
  height: 70px;
}
</style>