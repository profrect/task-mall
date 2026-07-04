<template>
  <div class="checkin-page">
    <van-nav-bar title="签到" left-arrow fixed placeholder @click-left="$router.back()" />

    <div class="hero-card">
      <div class="hero-title">每日签到</div>
      <div class="hero-desc">连续天数与奖励规则来自 promotion 域，奖励通过钱包结算链路入账。</div>
      <div class="hero-stats">
        <div>
          <span class="value">{{ state?.consecutiveDays || 0 }}</span>
          <span class="label">连续天数</span>
        </div>
        <div>
          <span class="value">{{ moneyText(state?.todayRule?.rewardAmount) }}</span>
          <span class="label">今日奖励 {{ state?.todayRule?.currency || 'USDT' }}</span>
        </div>
      </div>
      <van-button
        block
        round
        type="primary"
        class="checkin-button"
        :loading="submitting"
        :disabled="readonlyMode || state?.checkedToday"
        @click="handleCheckin"
      >
        {{ buttonText }}
      </van-button>
    </div>

    <van-cell-group inset title="奖励规则" class="cell-group">
      <van-cell
        v-for="rule in state?.rules || []"
        :key="rule.id"
        :title="rule.title"
        :label="`连续 ${rule.requiredConsecutiveDays} 天`"
        :value="`${moneyText(rule.rewardAmount)} ${rule.currency}`"
      />
    </van-cell-group>

    <van-cell-group inset title="签到记录" class="cell-group">
      <van-empty v-if="!loading && records.length === 0" description="暂无签到记录" />
      <van-skeleton v-if="loading" title :row="4" />
      <van-cell v-for="record in records" :key="record.id" :title="dateText(record.checkinDate)">
        <template #label>
          连续 {{ record.consecutiveDays }} 天 · {{ record.recordNo }}
        </template>
        <template #value>
          <div class="record-value">
            <span>{{ moneyText(record.rewardAmount) }} {{ record.currency }}</span>
            <van-tag :type="record.status === 'SETTLED' ? 'success' : 'danger'">
              {{ record.status === 'SETTLED' ? '已入账' : '入账失败' }}
            </van-tag>
          </div>
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { showSuccessToast } from 'vant';
import { checkin, getCheckinState } from '@/api/promotion';
import type { PromotionCheckinRecord, PromotionCheckinState } from '@/api/promotion';
import { isImpersonatedSession, rejectIfImpersonated } from '@/utils/impersonation';

const loading = ref(false);
const submitting = ref(false);
const state = ref<PromotionCheckinState>();
const records = ref<PromotionCheckinRecord[]>([]);
const readonlyMode = computed(() => isImpersonatedSession());

const buttonText = computed(() => {
  if (readonlyMode.value) return '模拟登录只读';
  if (state.value?.checkedToday) return '今日已签到';
  return '立即签到';
});

const moneyText = (value?: number) => Number(value || 0).toFixed(6);
const dateText = (value?: number) => {
  if (!value) return '-';
  const text = String(value);
  return `${text.slice(0, 4)}-${text.slice(4, 6)}-${text.slice(6, 8)}`;
};

const loadState = async () => {
  loading.value = true;
  try {
    state.value = await getCheckinState(30);
    records.value = state.value.recentRecords || [];
  } finally {
    loading.value = false;
  }
};

const handleCheckin = async () => {
  if (rejectIfImpersonated()) {
    return;
  }
  submitting.value = true;
  try {
    await checkin();
    showSuccessToast('签到成功');
    await loadState();
  } finally {
    submitting.value = false;
  }
};

onMounted(loadState);
</script>

<style scoped>
.checkin-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 24px;
}

.hero-card {
  margin: 12px;
  padding: 20px 16px;
  border-radius: 16px;
  color: #fff;
  background: linear-gradient(135deg, #07c160 0%, #1989fa 100%);
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
}

.hero-desc {
  font-size: 13px;
  line-height: 1.5;
  opacity: 0.92;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 18px 0;
}

.hero-stats > div {
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.16);
}

.value,
.label {
  display: block;
}

.value {
  font-size: 20px;
  font-weight: 700;
}

.label {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.88;
}

.checkin-button {
  border: none;
  color: #1989fa;
  background: #fff;
}

.cell-group {
  margin: 12px;
  overflow: hidden;
  border-radius: 12px;
}

.record-value {
  display: grid;
  gap: 6px;
  justify-items: end;
}
</style>