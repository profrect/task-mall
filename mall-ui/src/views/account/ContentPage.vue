<template>
  <div class="content-page">
    <van-nav-bar :title="title" left-arrow fixed placeholder @click-left="router.back()" />

    <template v-if="showNoticeList">
      <van-pull-refresh v-model="refreshing" @refresh="loadNotices">
        <div class="notice-list">
          <van-skeleton v-if="loading" title :row="4" />
          <van-empty v-else-if="!notices.length" description="暂无公告" image="search" />
          <div v-for="item in notices" :key="item.id" class="notice-card">
            <div class="notice-title">{{ item.title }}</div>
            <div v-if="item.summary" class="notice-summary">{{ item.summary }}</div>
            <div class="notice-content">{{ item.content }}</div>
          </div>
        </div>
      </van-pull-refresh>
    </template>

    <template v-else>
      <div class="status-card">
        <van-icon :name="icon" size="42" color="#1989fa" />
        <div class="status-title">{{ title }}</div>
        <div class="status-desc">{{ description }}</div>
      </div>
      <van-cell-group inset class="cell-group">
        <van-cell title="当前状态" value="入口已接入" />
        <van-cell title="数据来源" :label="dataScope" />
        <van-cell title="后端接口" label="当前仅公告列表有开放接口，其它内容页等待内容配置接口。" />
      </van-cell-group>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { getNotices, type ContentNotice } from '@/api/content'

const route = useRoute()
const router = useRouter()
const { locale } = useI18n()

const notices = ref<ContentNotice[]>([])
const loading = ref(false)
const refreshing = ref(false)

const title = computed(() => String(route.meta.title || '内容页'))
const description = computed(() => String(route.meta.description || '该内容页入口已接入，独立内容接口待开放。'))
const dataScope = computed(() => String(route.meta.dataScope || '不写死正文，不伪造公告、规则或帮助内容。'))
const icon = computed(() => String(route.meta.icon || 'description-o'))
const showNoticeList = computed(() => route.meta.contentType === 'notice')

async function loadNotices() {
  if (!showNoticeList.value) {
    refreshing.value = false
    return
  }
  loading.value = true
  try {
    notices.value = await getNotices(locale.value)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

watch(() => route.fullPath, loadNotices)
onMounted(loadNotices)
</script>

<style scoped>
.content-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 24px;
}
.notice-list {
  padding: 12px;
}
.notice-card {
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #fff;
}
.notice-title {
  font-size: 16px;
  font-weight: 700;
  color: #323233;
}
.notice-summary {
  margin-top: 6px;
  font-size: 12px;
  color: #969799;
}
.notice-content {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: #646566;
  white-space: pre-wrap;
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