<template>
  <div class="home-page">
    <!-- 1. 语言切换下拉菜单 -->
    <van-dropdown-menu active-color="#1989fa" class="lang-switch">
      <van-dropdown-item v-model="currentLang" :options="langOptions" @change="onLangChange" />
    </van-dropdown-menu>

    <!-- 2. 公告弹框（进入首页自动触发） -->
    <van-dialog
      v-model:show="showAnnouncement"
      :title="activeNotice?.title || $t('home.announcement')"
      :show-cancel-button="false"
      :confirm-button-text="$t('common.confirm')"
    >
      <div class="announcement-content">{{ activeNotice?.content }}</div>
    </van-dialog>

    <!-- 3. 总览卡片：简介 + 进入任务按钮 -->
    <div class="mall-intro">
      <div class="intro-decoration"></div>
      <div class="intro-content">
        <div class="intro-title">{{ $t('home.taskMallIntroTitle') }}</div>
        <div class="intro-desc">{{ $t('home.taskMallIntro') }}</div>
        <div class="intro-action action-row">
          <van-button type="primary" size="small" round block @click="$router.push('/tasks')">
            {{ $t('home.goToTaskMall') }}
          </van-button>
          <van-button type="success" size="small" round block @click="$router.push('/lottery')">
            抽奖活动
          </van-button>
        </div>
      </div>
    </div>
    <van-notice-bar
      v-if="noticeBarText"
      left-icon="volume-o"
      :text="noticeBarText"
    />

    <!-- 4. 快捷入口：已有真实页面优先，缺接口页面落到明确状态页。 -->
    <div class="quick-grid">
      <div v-for="item in quickLinks" :key="item.path" class="quick-item" @click="$router.push(item.path)">
        <div class="quick-icon" :class="item.tone">
          <van-icon :name="item.icon" />
        </div>
        <span>{{ item.label }}</span>
      </div>
    </div>

    <div class="section-title">任务预览</div>
    <div class="preview-section">
      <van-skeleton v-if="previewLoading" title :row="3" />
      <van-empty v-else-if="!hasToken" class="empty-section" description="登录后查看可领取任务" />
      <van-empty v-else-if="!taskPreview.length" class="empty-section" description="暂无可领取任务" />
      <div v-else class="preview-list">
        <div v-for="task in taskPreview" :key="task.taskId" class="preview-card" @click="$router.push('/tasks')">
          <div class="preview-title">{{ task.title }}</div>
          <div class="preview-desc">{{ task.description || task.taskCode }}</div>
          <div class="preview-reward">+{{ moneyText(task.rewardAmount) }} {{ task.currency }}</div>
        </div>
      </div>
    </div>

    <div class="section-title">VIP 等级</div>
    <div class="preview-section">
      <van-empty v-if="!hasToken" class="empty-section" description="登录后查看 VIP 权益" />
      <van-empty v-else-if="!vipLevels.length" class="empty-section" description="暂无可用 VIP 配置" />
      <div v-else class="vip-preview">
        <div v-for="vip in vipPreview" :key="vip.level" class="vip-card" @click="$router.push('/profile')">
          <div>
            <div class="preview-title">{{ vip.levelName }}</div>
            <div class="preview-desc">每日任务 {{ vip.dailyTasks }} · 返佣 {{ rateText(vip.rebateRate) }}</div>
          </div>
          <van-tag :type="vip.level === currentVipLevel ? 'warning' : 'primary'">
            {{ vip.level === currentVipLevel ? '当前' : `${vip.price} USDT` }}
          </van-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ContentNotice } from '@/api/content'
import { getNotices } from '@/api/content'
import { tokenStore } from '@/api/http'
import { getMissionTasks, type MissionTaskItem } from '@/api/mission'
import { getVipLevelOverview, type VipLevelConfig } from '@/api/user'

const { locale } = useI18n()

// 1. 语言切换
const currentLang = ref(locale.value)
const langOptions = [
  { text: '中文', value: 'zh-CN' },
  { text: 'English', value: 'en-US' },
]
const onLangChange = (val: string) => {
  locale.value = val
  localStorage.setItem('app-lang', val)
  loadNotices(val)
}

// 2. 公告弹框与通知栏：后台内容配置是唯一来源，只展示已启用公告。
const showAnnouncement = ref(false)
const notices = ref<ContentNotice[]>([])
const taskPreview = ref<MissionTaskItem[]>([])
const vipLevels = ref<VipLevelConfig[]>([])
const currentVipLevel = ref(0)
const previewLoading = ref(false)
const hasToken = computed(() => Boolean(tokenStore.get()))
const activeNotice = computed(() => notices.value[0])
const noticeBarText = computed(() => activeNotice.value?.summary || activeNotice.value?.title || '')
const vipPreview = computed(() => vipLevels.value.slice(0, 3))

const quickLinks = [
  { label: '账户', path: '/account', icon: 'user-o', tone: 'blue' },
  { label: '充值', path: '/account/recharge', icon: 'plus', tone: 'green' },
  { label: '提现', path: '/account/withDraw', icon: 'minus', tone: 'orange' },
  { label: '邀请', path: '/invite', icon: 'friends-o', tone: 'purple' },
  { label: '收益', path: '/income', icon: 'gold-coin-o', tone: 'green' },
  { label: '公告', path: '/notice', icon: 'volume-o', tone: 'blue' },
  { label: '优惠券', path: '/coupon', icon: 'coupon-o', tone: 'orange' },
  { label: '客服', path: '/service', icon: 'service-o', tone: 'purple' },
]

const moneyText = (value?: number) => Number(value || 0).toFixed(6)
const rateText = (value?: number) => `${Number(value || 0).toFixed(2)}%`

const loadNotices = async (languageCode = currentLang.value) => {
  try {
    notices.value = await getNotices(languageCode)
    showAnnouncement.value = Boolean(activeNotice.value)
  } catch {
    notices.value = []
    showAnnouncement.value = false
  }
}

const loadPreview = async () => {
  if (!hasToken.value) return
  previewLoading.value = true
  try {
    const [tasks, vip] = await Promise.all([
      getMissionTasks('available', 3),
      getVipLevelOverview(),
    ])
    taskPreview.value = tasks || []
    vipLevels.value = vip.levels || []
    currentVipLevel.value = Number(vip.currentLevel || 0)
  } finally {
    previewLoading.value = false
  }
}

onMounted(() => {
  loadNotices()
  loadPreview()
})

</script>

<style scoped>
.home-page {
  padding: 12px;
  background: #f7f8fa;
  min-height: 100vh;
}

/* 1. 语言切换下拉菜单 */
.lang-switch {
  margin-bottom: 12px;
  :deep(.van-dropdown-menu__bar) {
    box-shadow: none;
    background: transparent;
  }
}

/* 2. 公告弹框内容 */
.announcement-content {
  padding: 16px 24px;
  font-size: 14px;
  line-height: 1.6;
  color: #323233;
  max-height: 60vh;
  overflow-y: auto;
}

/* 3. 总览卡片 */
.overview-card {
  margin-bottom: 16px;
  padding: 16px;
  .overview-desc {
    font-size: 14px;
    line-height: 1.6;
    color: #646566;
    margin-bottom: 12px;
  }
}

.mall-intro {
  display: flex;
  align-items: stretch;
  margin-top: 12px;
  padding: 14px 16px;
  background: linear-gradient(90deg, rgba(25, 118, 210, 0.08) 0%, rgba(25, 118, 210, 0.02) 100%);
  border-radius: 8px;
  position: relative;
  overflow: hidden;

  .intro-decoration {
    width: 3px;
    height: 100%;
    background: var(--primary-color, #1976d2);
    border-radius: 2px;
    margin-right: 12px;
    flex-shrink: 0;
  }

  .intro-content {
    flex: 1;
    min-width: 0;

    .intro-title {
      font-size: 15px;
      font-weight: 700;
      color: var(--text-primary, #323233);
      line-height: 1.4;
      margin-bottom: 4px;
    }

    .intro-desc {
      font-size: 12px;
      color: var(--text-secondary, #646566);
      line-height: 1.5;
      margin-bottom: 6px;
    }

    .intro-action {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 8px;
      margin-top: 10px;

      :deep(.van-button) {
        font-size: 13px;
        font-weight: 600;
        height: 32px;
        line-height: 32px;
      }
    }
  }
}

/* 快捷入口与真实数据预览 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-top: 12px;
  padding: 14px 8px;
  background: #fff;
  border-radius: 12px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  color: #323233;
}
.quick-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: #fff;
  font-size: 19px;
}
.quick-icon.blue {
  background: #1989fa;
}
.quick-icon.green {
  background: #07c160;
}
.quick-icon.orange {
  background: #ff976a;
}
.quick-icon.purple {
  background: #7e57c2;
}
.preview-section {
  min-height: 92px;
}
.preview-list,
.vip-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.preview-card,
.vip-card {
  padding: 12px 14px;
  border-radius: 12px;
  background: #fff;
}
.vip-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.preview-title {
  font-size: 14px;
  font-weight: 700;
  color: #323233;
}
.preview-desc {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: #969799;
}
.preview-reward {
  margin-top: 6px;
  font-size: 12px;
  font-weight: 700;
  color: #ff976a;
}
.empty-section {
  background: #fff;
  border-radius: 12px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin: 16px 0 8px;
  padding-left: 4px;
}
</style>
