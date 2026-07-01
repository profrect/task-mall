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

    <!-- 4. 任务区块：mall-mission 尚无真实任务源，不展示假任务。 -->
    <div class="section-title">{{ $t('home.featuredTasks') }}</div>
    <van-empty class="empty-section" description="任务模块暂未开放" />

    <!-- 5. VIP等级：后台 VIP 配置 provider 未落地，不展示假权益。 -->
    <div class="section-title">{{ $t('home.vipLevels') }}</div>
    <van-empty class="empty-section" description="VIP 配置暂未开放" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ContentNotice } from '@/api/content'
import { getNotices } from '@/api/content'
// import { useUserStore } from '@/stores/user' // 复用已保存的用户状态

const { locale } = useI18n()
// const userStore = useUserStore()

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
const activeNotice = computed(() => notices.value[0])
const noticeBarText = computed(() => activeNotice.value?.summary || activeNotice.value?.title || '')

const loadNotices = async (languageCode = currentLang.value) => {
  try {
    notices.value = await getNotices(languageCode)
    showAnnouncement.value = Boolean(activeNotice.value)
  } catch {
    notices.value = []
    showAnnouncement.value = false
  }
}

onMounted(() => {
  loadNotices()
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

/* 4. 精选任务区块 */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin: 16px 0 8px;
  padding-left: 4px;
}
</style>
