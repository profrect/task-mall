<template>
  <div class="admin-login-page">
    <van-nav-bar title="后台模拟登录" fixed placeholder />
    <div class="card">
      <van-loading v-if="loading" type="spinner" color="#1a237e" />
      <van-icon v-else name="warning-o" size="42" color="#ff976a" />
      <div class="title">{{ loading ? '正在进入前台查看模式' : '无法进入查看模式' }}</div>
      <div class="desc">{{ message }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { exchangeImpersonationTicket } from '@/api/auth'
import { tokenStore } from '@/api/http'
import { store } from '@/store'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const message = ref('票据仅可使用一次，成功后会进入只读查看会话。')

function firstQueryValue(value: unknown): string {
  if (Array.isArray(value)) return String(value[0] || '')
  return String(value || '')
}

onMounted(async () => {
  const ticket = firstQueryValue(route.query.ticket).trim()
  const redirect = firstQueryValue(route.query.redirect).trim() || '/profile'
  if (!ticket) {
    loading.value = false
    message.value = '缺少模拟登录票据，请从后台会员列表重新进入。'
    return
  }

  try {
    const { accessToken } = await exchangeImpersonationTicket({ ticket })
    tokenStore.set(accessToken)
    store.setLoggedIn(true)
    store.setImpersonated(true)
    showSuccessToast('已进入后台只读查看模式')
    router.replace(redirect)
  } catch {
    loading.value = false
    message.value = '票据无效、已过期或已被使用，请从后台重新生成。'
  }
})
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  background: #f5f6fa;
}
.card {
  margin: 24px 16px;
  padding: 36px 20px;
  border-radius: 16px;
  background: #fff;
  text-align: center;
  box-shadow: 0 6px 18px rgba(26, 35, 126, 0.08);
}
.title {
  margin-top: 18px;
  font-size: 18px;
  font-weight: 700;
  color: #323233;
}
.desc {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.6;
  color: #646566;
}
</style>