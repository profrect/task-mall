<template>
  <div class="lottery-page">
    <van-nav-bar title="抽奖活动" left-arrow fixed placeholder @click-left="$router.back()" />

    <div class="hero-card">
      <div class="hero-title">每日抽奖</div>
      <div class="hero-desc">中奖现金奖后将写入抽奖记录，并通过钱包结算 Provider 入账。</div>
    </div>

    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink>
      <van-tab title="活动" name="activities">
        <van-pull-refresh v-model="refreshing" @refresh="loadData">
          <div class="activity-list">
            <van-empty v-if="!loading && activities.length === 0" description="暂无可参与抽奖活动" />
            <van-skeleton v-if="loading" title :row="4" />
            <div v-for="item in activities" :key="item.activity.id" class="activity-card">
              <div class="activity-header">
                <div>
                  <div class="activity-title">{{ item.activity.title }}</div>
                  <div class="activity-code">{{ item.activity.activityCode }}</div>
                </div>
                <van-tag type="success">{{ remainText(item.todayRemainCount) }}</van-tag>
              </div>
              <div v-if="item.activity.description" class="activity-desc">
                {{ item.activity.description }}
              </div>
              <div class="prize-list">
                <div v-for="prize in item.prizes" :key="prize.id" class="prize-item">
                  <div class="prize-name">{{ prize.prizeName }}</div>
                  <div class="prize-meta">
                    {{ prize.prizeType === 'CASH' ? `${moneyText(prize.amount)} ${prize.currency}` : prize.prizeType }}
                  </div>
                </div>
              </div>
              <van-button
                type="primary"
                block
                round
                :loading="drawingId === item.activity.id"
                :disabled="item.todayRemainCount === 0"
                @click="handleDraw(item.activity.id)"
              >
                立即抽奖
              </van-button>
            </div>
          </div>
        </van-pull-refresh>
      </van-tab>

      <van-tab title="记录" name="records">
        <van-pull-refresh v-model="recordRefreshing" @refresh="loadRecords">
          <div class="record-list">
            <van-empty v-if="!recordLoading && records.length === 0" description="暂无抽奖记录" />
            <van-skeleton v-if="recordLoading" title :row="4" />
            <div v-for="record in records" :key="record.id" class="record-card">
              <div class="record-main">
                <div>
                  <div class="record-title">{{ record.prizeName }}</div>
                  <div class="record-sub">{{ record.activityTitle }} · {{ record.recordNo }}</div>
                </div>
                <van-tag :type="record.status === 'SETTLED' ? 'success' : record.status === 'SETTLE_FAILED' ? 'danger' : 'primary'">
                  {{ statusText(record.status) }}
                </van-tag>
              </div>
              <div class="record-row">
                <span>金额</span>
                <span>{{ moneyText(record.amount) }} {{ record.currency }}</span>
              </div>
              <div class="record-row">
                <span>钱包流水</span>
                <span>{{ record.walletFlowNo || '-' }}</span>
              </div>
              <div v-if="record.failReason" class="record-row danger">
                <span>失败原因</span>
                <span>{{ record.failReason }}</span>
              </div>
            </div>
          </div>
        </van-pull-refresh>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { showDialog, showSuccessToast } from 'vant';
import {
  PromotionLotteryDetail,
  PromotionLotteryRecord,
  drawLottery,
  getLotteryActivities,
  getLotteryRecords,
} from '@/api/promotion';
import { rejectIfImpersonated } from '@/utils/impersonation';

const activeTab = ref('activities');
const loading = ref(false);
const refreshing = ref(false);
const recordLoading = ref(false);
const recordRefreshing = ref(false);
const drawingId = ref<number>();
const activities = ref<PromotionLotteryDetail[]>([]);
const records = ref<PromotionLotteryRecord[]>([]);

const moneyText = (value?: number) => Number(value || 0).toFixed(6);

const remainText = (remain: number) => {
  if (remain < 0) return '不限次数';
  return `剩余 ${remain} 次`;
};

const statusText = (status: string) => {
  const map: Record<string, string> = {
    DRAWN: '已抽中',
    SETTLED: '已结算',
    SETTLE_FAILED: '结算失败',
  };
  return map[status] || status;
};

const loadActivities = async () => {
  loading.value = true;
  try {
    activities.value = await getLotteryActivities();
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
};

const loadRecords = async () => {
  recordLoading.value = true;
  try {
    records.value = await getLotteryRecords(30);
  } finally {
    recordLoading.value = false;
    recordRefreshing.value = false;
  }
};

const loadData = async () => {
  await Promise.all([loadActivities(), loadRecords()]);
};

const handleDraw = async (activityId: number) => {
  if (rejectIfImpersonated()) {
    return;
  }
  drawingId.value = activityId;
  try {
    const record = await drawLottery(activityId);
    showSuccessToast('抽奖完成');
    await showDialog({
      title: '抽奖结果',
      message: `${record.prizeName}\n${moneyText(record.amount)} ${record.currency}\n状态：${statusText(record.status)}`,
    });
    await loadData();
  } finally {
    drawingId.value = undefined;
  }
};

onMounted(loadData);
</script>

<style scoped>
.lottery-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 24px;
}

.hero-card {
  margin: 12px;
  padding: 20px 16px;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, #ff7a45 0%, #ff4d4f 100%);
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
}

.hero-desc {
  font-size: 13px;
  line-height: 1.5;
  opacity: 0.9;
}

.activity-list,
.record-list {
  padding: 12px;
}

.activity-card,
.record-card {
  margin-bottom: 12px;
  padding: 14px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.activity-header,
.record-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.activity-title,
.record-title {
  font-size: 16px;
  font-weight: 700;
  color: #323233;
}

.activity-code,
.record-sub {
  margin-top: 4px;
  font-size: 11px;
  color: #969799;
  word-break: break-all;
}

.activity-desc {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.5;
  color: #646566;
}

.prize-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin: 12px 0;
}

.prize-item {
  padding: 10px;
  border-radius: 10px;
  background: #f7f8fa;
}

.prize-name {
  font-size: 13px;
  font-weight: 600;
  color: #323233;
}

.prize-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #ee0a24;
}

.record-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
  font-size: 12px;
  color: #646566;
}

.record-row span:last-child {
  color: #323233;
  text-align: right;
  word-break: break-all;
}

.record-row.danger span:last-child {
  color: #ee0a24;
}

:deep(.van-tabs__nav) {
  background: transparent;
  margin: 0 12px;
  width: calc(100% - 24px);
}
</style>