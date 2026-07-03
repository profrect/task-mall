<template>
  <div class="status-page">
    <van-nav-bar :title="title" left-arrow fixed placeholder @click-left="router.back()" />

    <div class="status-card">
      <van-icon :name="icon" size="42" color="#1989fa" />
      <div class="status-title">{{ title }}</div>
      <div class="status-desc">{{ description }}</div>
    </div>

    <van-cell-group inset class="cell-group">
      <van-cell title="当前状态" :value="stateText" />
      <van-cell title="数据口径" :label="dataScope" />
      <van-cell title="接口状态" :label="apiState" />
    </van-cell-group>

    <van-cell-group v-if="relatedLinks.length" inset class="cell-group" title="可用入口">
      <van-cell
        v-for="item in relatedLinks"
        :key="item.path"
        :title="item.label"
        :value="item.value"
        is-link
        @click="router.push(item.path)"
      />
    </van-cell-group>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const featureMap: Record<string, { title: string; icon: string; description: string; apiState: string }> = {
  changePwd: {
    title: '修改密码',
    icon: 'lock',
    description: '修改密码入口已接入；当前缺少用户端修改密码开放接口。',
    apiState: '待新增 /api/open/user/password 或等价接口。',
  },
  financial: {
    title: '资金资料',
    icon: 'balance-pay',
    description: '资金资料入口已接入；当前缺少银行卡/链上地址资料管理接口。',
    apiState: '待新增资金资料查询和维护接口。',
  },
  googleAuth: {
    title: 'Google 验证',
    icon: 'shield-o',
    description: 'Google 验证入口已接入；当前缺少密钥绑定、验证和解绑接口。',
    apiState: '待新增二次验证接口。',
  },
  kyc: {
    title: '实名认证',
    icon: 'contact',
    description: '实名认证入口已接入；当前缺少实名资料提交、审核状态接口。',
    apiState: '待新增 KYC 状态机接口。',
  },
  select: {
    title: '账户选择',
    icon: 'exchange',
    description: '账户选择入口已接入；当前钱包只开放 USDT 主账户。',
    apiState: '待新增多账户/多资产选择接口。',
  },
  vipUplog: {
    title: 'VIP 升级记录',
    icon: 'vip-card-o',
    description: 'VIP 升级记录入口已接入；当前缺少独立升级订单列表接口。',
    apiState: '待新增 VIP 升级记录查询接口，资金核对可先查看钱包账变。',
  },
}

const feature = computed(() => {
  const key = String(route.params.feature || '')
  return featureMap[key]
})
const title = computed(() => feature.value?.title || String(route.meta.title || '功能状态'))
const description = computed(() => feature.value?.description || String(route.meta.description || '该页面入口已接入，独立后端接口待开放。'))
const stateText = computed(() => String(route.meta.stateText || '入口已接入'))
const dataScope = computed(() => String(route.meta.dataScope || '不伪造数据；仅展示当前可确认状态。'))
const apiState = computed(() => feature.value?.apiState || String(route.meta.apiState || '待后端接口或内容配置接入。'))
const icon = computed(() => feature.value?.icon || String(route.meta.icon || 'info-o'))

const relatedLinks = computed(() => {
  const items = route.meta.relatedLinks
  if (!Array.isArray(items)) return []
  return items.filter((item): item is { label: string; path: string; value?: string } => {
    return Boolean(item && typeof item.label === 'string' && typeof item.path === 'string')
  })
})
</script>

<style scoped>
.status-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 24px;
}
.status-card {
  margin: 12px;
  padding: 28px 20px;
  border-radius: 14px;
  background: #fff;
  text-align: center;
}
.status-title {
  margin-top: 12px;
  font-size: 20px;
  font-weight: 700;
  color: #323233;
}
.status-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #646566;
}
.cell-group {
  margin: 12px;
  overflow: hidden;
  border-radius: 12px;
}
</style>