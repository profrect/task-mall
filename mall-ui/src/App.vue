<template>
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
import { ref } from 'vue'
const active = ref(0)
</script>
