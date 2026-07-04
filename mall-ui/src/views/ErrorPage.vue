<template>
  <div class="error-page">
    <van-nav-bar :title="title" fixed placeholder />
    <div class="error-card">
      <div class="error-code">{{ code }}</div>
      <div class="error-title">{{ title }}</div>
      <div class="error-desc">{{ description }}</div>
      <div class="actions">
        <van-button round block type="primary" @click="router.replace('/home')">
          返回首页
        </van-button>
        <van-button round block plain type="primary" @click="router.back()">
          返回上一页
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const code = computed(() => String(route.meta.statusCode || '404'))
const title = computed(() => String(route.meta.title || '页面不存在'))
const description = computed(() =>
  String(route.meta.description || '当前访问的页面不存在，或参考站入口尚未在本地开放。')
)
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  background: #f5f6fa;
}
.error-card {
  margin: 12px;
  padding: 38px 22px;
  border-radius: 16px;
  background: #fff;
  text-align: center;
}
.error-code {
  color: #1989fa;
  font-size: 56px;
  font-weight: 900;
  line-height: 1;
}
.error-title {
  margin-top: 14px;
  color: #323233;
  font-size: 22px;
  font-weight: 800;
}
.error-desc {
  margin-top: 10px;
  color: #646566;
  font-size: 13px;
  line-height: 1.7;
}
.actions {
  display: grid;
  gap: 10px;
  margin-top: 28px;
}
</style>