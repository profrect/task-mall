<template>
  <div class="invite-page">
    <van-nav-bar title="邀请好友" left-arrow fixed placeholder @click-left="router.back()" />

    <div class="invite-card">
      <div class="card-title">我的邀请码</div>
      <div class="invite-code">{{ userInfo.inviteCode || '-' }}</div>
      <div class="card-desc">邀请好友注册并完成符合规则的 VIP 升级后，返佣会生成真实收益记录并入账钱包。</div>
      <div class="button-row">
        <van-button round block type="primary" :disabled="!userInfo.inviteCode" @click="copyText(userInfo.inviteCode || '')">
          复制邀请码
        </van-button>
        <van-button round block type="success" :disabled="!userInfo.inviteCode" @click="copyText(inviteLink)">
          复制邀请链接
        </van-button>
      </div>
    </div>

    <van-cell-group inset class="cell-group">
      <van-cell title="直属上级" :value="userInfo.inviteUser || 'Platform Official'" />
      <van-cell title="直属成员" :value="String(userInfo.teamMemberNum ?? 0)" is-link to="/team?tab=members" />
      <van-cell title="收益记录" value="查看返佣" is-link to="/income" />
    </van-cell-group>

    <van-cell-group inset class="cell-group" title="邀请规则">
      <van-cell title="数据来源" label="邀请码、团队成员和返佣记录均来自当前登录用户真实接口。" />
      <van-cell title="结算说明" label="返佣由后端规则结算到钱包流水，前端不本地计算、不伪造收益。" />
    </van-cell-group>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { getCurrentUser, type UserDetail } from '@/api/user'

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

const inviteLink = computed(() => {
  if (!userInfo.value.inviteCode) return ''
  const base = `${window.location.origin}${window.location.pathname}`
  return `${base}#/register?inviteCode=${encodeURIComponent(userInfo.value.inviteCode)}`
})

async function loadUser() {
  userInfo.value = await getCurrentUser()
}

async function copyText(text: string) {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    showSuccessToast('已复制')
  } catch {
    showFailToast('复制失败，请手动长按选择')
  }
}

onMounted(loadUser)
</script>

<style scoped>
.invite-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 24px;
}
.invite-card {
  margin: 12px;
  padding: 24px 18px;
  border-radius: 16px;
  color: #fff;
  background: linear-gradient(135deg, #1a237e 0%, #7e57c2 100%);
}
.card-title {
  font-size: 13px;
  opacity: 0.86;
}
.invite-code {
  margin-top: 10px;
  font-size: 36px;
  font-weight: 800;
  letter-spacing: 2px;
}
.card-desc {
  margin-top: 10px;
  font-size: 12px;
  line-height: 1.6;
  opacity: 0.86;
}
.button-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 18px;
}
.cell-group {
  margin: 12px;
  overflow: hidden;
  border-radius: 12px;
}
</style>