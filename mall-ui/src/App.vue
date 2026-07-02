<template>
  <van-notice-bar
    v-if="showImpersonationBanner"
    class="impersonation-banner"
    color="#ad6800"
    background="#fff7e6"
    left-icon="warning-o"
    text="当前为后台模拟登录，仅可查看前台页面，资金、任务、抽奖和资料修改等操作已禁用。"
  />
  <router-view />
  <van-tabbar
    v-model="active"
    active-color="var(--color-brand)"
    inactive-color="var(--text-secondary)"
    safe-area-inset-bottom
  >
    <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
    <van-tabbar-item icon="orders-o" to="/tasks">任务</van-tabbar-item>
    <van-tabbar-item icon="chart-trending-o" to="/investment">投资</van-tabbar-item>
    <van-tabbar-item icon="balance-o" to="/wallet">钱包</van-tabbar-item>
    <van-tabbar-item icon="balance-o" to="/team">团队</van-tabbar-item>
    <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { tokenStore } from '@/api/http'
import { store } from '@/store'

const active = ref(0)
const showImpersonationBanner = computed(() => Boolean(tokenStore.get() && store.state.isImpersonated))
</script>

<style scoped>
.impersonation-banner {
  position: sticky;
  top: 0;
  z-index: 1000;
}
</style>
