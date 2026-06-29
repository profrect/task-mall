<template>
  <div class="home-page">
    <!-- 1. 语言切换下拉菜单 -->
    <van-dropdown-menu active-color="#1989fa" class="lang-switch">
      <van-dropdown-item v-model="currentLang" :options="langOptions" @change="onLangChange" />
    </van-dropdown-menu>

    <!-- 2. 公告弹框（进入首页自动触发） -->
    <van-dialog
      v-model:show="showAnnouncement"
      :title="$t('home.announcement')"
      show-cancel-button
      :confirm-button-text="$t('common.confirm')"
    >
      <div class="announcement-content">{{ announcementText }}</div>
    </van-dialog>

    <!-- 3. 总览卡片：简介 + 进入任务按钮 -->
    <div class="mall-intro">
      <div class="intro-decoration"></div>
      <div class="intro-content">
        <div class="intro-title">{{ $t('home.taskMallIntroTitle') }}</div>
        <div class="intro-desc">{{ $t('home.taskMallIntro') }}</div>
        <!-- ✅ 新增行动按钮 -->
        <div class="intro-action">
          <van-button type="primary" size="small" round block @click="$router.push('/tasks')">
            {{ $t('home.goToTaskMall') }}
          </van-button>
        </div>
      </div>
    </div>
    <van-notice-bar
      left-icon="volume-o"
      text="无论我们能活多久，我们能够享受的只有无法分割的此刻，此外别无其他，zzzzzzzzzzzzzzzzzz。"
    />

    <!-- 4. 精选任务区块（重构为列表） -->
    <div class="section-title">{{ $t('home.featuredTasks') }}</div>
    <van-cell-group inset class="task-list">
      <van-cell
        v-for="task in featuredTasks"
        :key="task.id"
        clickable
        @click="goTaskDetail(task.id)"
        class="task-item"
      >
        <template #icon>
          <van-image
            :src="task.coverUrl"
            width="80"
            height="80"
            fit="cover"
            radius="6"
            class="task-thumb"
          />
        </template>
        <template #title>
          <div class="task-content">
            <div class="task-header">
              <span class="task-title">{{ task.title }}</span>
              <van-tag type="danger" v-if="task.status === 'hot'" size="medium">{{
                $t('home.hotTag')
              }}</van-tag>
            </div>
            <div class="task-desc">{{ task.description || $t('home.noDesc') }}</div>
            <div class="task-footer">
              <span class="task-price" v-if="task.rewardAmount">¥{{ task.rewardAmount }}</span>
              <van-button size="small" type="primary" round>{{ $t('home.doTask') }}</van-button>
            </div>
          </div>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 5. VIP等级配置列表（替换原邀请卡片） -->
    <div class="section-title">{{ $t('home.vipLevels') }}</div>
    <van-cell-group inset class="vip-card">
      <van-steps direction="vertical" :active="currentVipLevel" active-color="#ee0a24">
        <van-step v-for="level in vipLevels" :key="level.id">
          <div class="vip-level-header">
            <span class="vip-name">{{ level.name }}</span>
            <van-tag type="warning" v-if="level.isCurrent">{{ $t('home.currentLevel') }}</van-tag>
          </div>
          <div class="vip-benefits">
            <div v-for="(benefit, idx) in level.benefits" :key="idx" class="benefit-item">
              <van-icon name="passed" color="#07c160" size="14" />
              <span>{{ benefit }}</span>
            </div>
          </div>
          <div class="vip-condition">{{ $t('home.upgradeCondition') }}: {{ level.condition }}</div>
        </van-step>
      </van-steps>
    </van-cell-group>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { showToast } from 'vant'
// import { useUserStore } from '@/stores/user' // 复用已保存的用户状态

const { t, locale } = useI18n()
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
}

// 2. 公告弹框（复用登录响应中的 announcement 字段）
const showAnnouncement = ref(false)
const announcementText = ref('')
onMounted(() => {
  showAnnouncement.value = true
  // if (userStore.announcement) {
  //   announcementText.value = userStore.announcement
  //   showAnnouncement.value = true
  // }
})

// 4. 精选任务数据（扩展任务模型，新增 coverUrl）
interface FeaturedTask {
  id: number
  title: string
  coverUrl: string
}

// ✅ 确认featuredTasks数据结构包含以下字段（若接口未返回需补充mock）
interface Task {
  id: number
  coverUrl: string
  title: string // 任务标题（必填）
  description: string // 简短描述（选填，超长自动截断）
  rewardAmount: number // 奖励金额（选填）
  status: 'normal' | 'hot' // 状态标识
}

// 示例数据（实际应从/api/tasks/featured获取）
const featuredTasks = ref<Task[]>([
  {
    id: 1001,
    coverUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    title: '关注官方公众号',
    description: '完成关注即可获得奖励，每日限1次',
    rewardAmount: 2.5,
    status: 'hot',
  },
  {
    id: 1002,
    coverUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    title: '关注官方公众号',
    description: '完成关注即可获得奖励，每日限1次',
    rewardAmount: 2.5,
    status: 'hot',
  },
  {
    id: 1003,
    coverUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    title: '关注官方公众号',
    description: '完成关注即可获得奖励，每日限1次',
    rewardAmount: 2.5,
    status: 'hot',
  },
  // ...其他任务
])

// ✅ 新增VIP等级数据（实际项目应从接口获取）
interface VipLevel {
  id: number
  name: string
  condition: string
  benefits: string[]
  isCurrent?: boolean
}

const currentVipLevel = ref(1) // 当前用户等级索引
const vipLevels = ref<VipLevel[]>([
  {
    id: 1,
    name: '普通会员',
    condition: '注册即享',
    benefits: ['基础任务权限', '每日3次提现'],
    isCurrent: true,
  },
  {
    id: 2,
    name: '黄金会员',
    condition: '累计完成50个任务',
    benefits: ['专属高佣任务', '每日10次提现', '优先客服通道'],
  },
  {
    id: 3,
    name: '钻石会员',
    condition: '累计收益≥5000元',
    benefits: ['全部任务解锁', '无限提现', '专属运营对接', '月度奖金池'],
  },
])
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

.task-list {
  margin-top: 12px;

  .task-item {
    padding: 12px 16px;

    :deep(.van-cell__left-icon) {
      margin-right: 12px;
      align-self: flex-start;
    }

    .task-thumb {
      flex-shrink: 0;
    }

    .task-content {
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      min-height: 80px; /* 与图片等高，保证垂直居中 */

      .task-header {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-bottom: 4px;

        .task-title {
          font-size: 14px;
          font-weight: 600;
          color: #323233;
          line-height: 1.4;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          flex: 1;
        }
      }

      .task-desc {
        font-size: 12px;
        color: #969799;
        line-height: 1.5;
        margin-bottom: 8px;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .task-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .task-price {
          font-size: 16px;
          font-weight: 700;
          color: #ee0a24;
        }
      }
    }
  }
}

/* ✅ 新增VIP等级样式 */
.vip-card {
  margin-top: 16px;
  padding: 16px;

  :deep(.van-step__title) {
    width: 100%;
  }

  .vip-level-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    .vip-name {
      font-size: 15px;
      font-weight: 600;
      color: #323233;
    }
  }

  .vip-benefits {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;

    .benefit-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #646566;
      background: #f7f8fa;
      padding: 4px 8px;
      border-radius: 4px;
    }
  }

  .vip-condition {
    font-size: 12px;
    color: #969799;
  }
}
</style>
