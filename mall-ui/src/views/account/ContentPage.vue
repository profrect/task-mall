<template>
  <div class="content-page">
    <van-nav-bar :title="title" left-arrow fixed placeholder @click-left="router.back()" />

    <template v-if="pageMode === 'content'">
      <van-pull-refresh v-model="refreshing" @refresh="loadPage">
        <div class="content-list">
          <van-skeleton v-if="loading" title :row="4" />
          <van-empty v-else-if="!contentItems.length" :description="emptyText" image="search" />
          <div v-for="item in contentItems" :key="item.id" class="content-card">
            <div class="content-title">{{ item.title }}</div>
            <div v-if="item.summary" class="content-summary">{{ item.summary }}</div>
            <div class="content-body">{{ item.content }}</div>
          </div>
        </div>
        <van-cell-group inset class="cell-group">
          <van-cell title="当前状态" value="已读取后台内容配置" />
          <van-cell title="数据来源" :label="dataScope" />
          <van-cell title="后端接口" :label="apiState" />
        </van-cell-group>
      </van-pull-refresh>
    </template>

    <template v-else-if="pageMode === 'service'">
      <van-pull-refresh v-model="refreshing" @refresh="loadPage">
        <div class="service-card">
          <van-icon name="service-o" size="42" color="#1989fa" />
          <div class="status-title">{{ serviceTitle }}</div>
          <div class="status-desc">{{ serviceConfig.message || description }}</div>
        </div>
        <van-cell-group v-if="serviceConfig.bots.length" inset class="cell-group">
          <van-cell
            v-for="bot in serviceConfig.bots"
            :key="bot.botName"
            :title="bot.botName"
            :label="bot.description || '客服机器人配置已启用'"
          />
        </van-cell-group>
        <van-cell-group inset class="cell-group">
          <van-cell title="当前状态" value="只读客服配置" />
          <van-cell title="数据来源" label="后台系统参数与机器人配置；不暴露 botToken。" />
          <van-cell title="后端接口" label="/api/open/content/service" />
        </van-cell-group>
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
        <van-cell title="后端接口" :label="apiState" />
      </van-cell-group>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import {
  getContentItems,
  getServiceConfig,
  type ContentItem,
  type ContentItemType,
  type OpenServiceConfig,
} from '@/api/content'

const route = useRoute()
const router = useRouter()
const { locale } = useI18n()

const SUPPORTED_CONTENT_TYPES: ContentItemType[] = [
  'NOTICE',
  'COMPANY_PROFILE',
  'PLATFORM_PROFILE',
  'REGULATOR',
  'USER_AGREEMENT',
  'USER_PRIVACY',
]

const contentItems = ref<ContentItem[]>([])
const serviceConfig = ref<OpenServiceConfig>({ title: '在线客服', message: '', bots: [] })
const loading = ref(false)
const refreshing = ref(false)

const title = computed(() => String(route.meta.title || '内容页'))
const description = computed(() => String(route.meta.description || '该内容页入口已接入，独立内容接口待开放。'))
const dataScope = computed(() => String(route.meta.dataScope || '读取后台已启用内容配置，不写死正文。'))
const icon = computed(() => String(route.meta.icon || 'description-o'))
const apiState = computed(() => String(route.meta.apiState || '/api/open/content/list'))
const contentType = computed(() => normalizeContentType(route.meta.contentType))
const pageMode = computed<'content' | 'service' | 'status'>(() => {
  if (route.meta.contentMode === 'service') return 'service'
  return contentType.value ? 'content' : 'status'
})
const serviceTitle = computed(() => serviceConfig.value.title || title.value)
const emptyText = computed(() => `${title.value}暂无已启用内容`)

async function loadPage() {
  contentItems.value = []
  if (pageMode.value === 'status') {
    loading.value = false
    refreshing.value = false
    return
  }

  loading.value = true
  try {
    if (pageMode.value === 'service') {
      serviceConfig.value = await getServiceConfig()
      return
    }
    if (contentType.value) {
      contentItems.value = await getContentItems(contentType.value, locale.value)
    }
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function normalizeContentType(value: unknown): ContentItemType | undefined {
  if (typeof value !== 'string') return undefined
  return SUPPORTED_CONTENT_TYPES.includes(value as ContentItemType) ? (value as ContentItemType) : undefined
}

watch([() => route.fullPath, () => locale.value], loadPage)
onMounted(loadPage)
</script>

<style scoped>
.content-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 24px;
}
.content-list {
  padding: 12px;
}
.content-card {
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #fff;
}
.content-title {
  font-size: 16px;
  font-weight: 700;
  color: #323233;
}
.content-summary {
  margin-top: 6px;
  font-size: 12px;
  color: #969799;
}
.content-body {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: #646566;
  white-space: pre-wrap;
}
.status-card,
.service-card {
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