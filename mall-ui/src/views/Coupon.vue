<template>
  <div class="coupon-page">
    <van-nav-bar title="优惠券" left-arrow fixed placeholder @click-left="$router.back()" />

    <div class="hero-card">
      <div class="hero-title">优惠券中心</div>
      <div class="hero-desc">券模板、领取记录和过期状态都来自 promotion 域，不伪造可用券。</div>
    </div>

    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink @change="handleTabChange">
      <van-tab title="可领取" name="templates">
        <van-pull-refresh v-model="refreshing" @refresh="loadTemplates">
          <div class="list-wrap">
            <van-skeleton v-if="loading" title :row="4" />
            <van-empty v-else-if="templates.length === 0" description="暂无可领取优惠券" />
            <div v-for="item in templates" :key="item.id" class="coupon-card">
              <div class="coupon-main">
                <div>
                  <div class="coupon-title">{{ item.title }}</div>
                  <div class="coupon-code">{{ item.couponCode }}</div>
                </div>
                <div class="coupon-amount">{{ moneyText(item.discountAmount) }} {{ item.currency }}</div>
              </div>
              <div v-if="item.description" class="coupon-desc">{{ item.description }}</div>
              <div class="coupon-meta">
                <span>满 {{ moneyText(item.minOrderAmount) }} 可用</span>
                <span>{{ stockText(item.remainStock) }}</span>
                <span>限领 {{ item.perUserLimit }} 张</span>
              </div>
              <van-button
                block
                round
                type="primary"
                :loading="claimingId === item.id"
                :disabled="readonlyMode || !canClaim(item)"
                @click="handleClaim(item.id)"
              >
                {{ claimButtonText(item) }}
              </van-button>
            </div>
          </div>
        </van-pull-refresh>
      </van-tab>

      <van-tab title="我的券" name="records">
        <van-dropdown-menu>
          <van-dropdown-item v-model="recordStatus" :options="statusOptions" @change="loadRecords" />
        </van-dropdown-menu>
        <van-pull-refresh v-model="recordRefreshing" @refresh="loadRecords">
          <div class="list-wrap">
            <van-skeleton v-if="recordLoading" title :row="4" />
            <van-empty v-else-if="records.length === 0" description="暂无优惠券记录" />
            <div v-for="record in records" :key="record.id" class="coupon-card record-card">
              <div class="coupon-main">
                <div>
                  <div class="coupon-title">{{ record.title }}</div>
                  <div class="coupon-code">{{ record.recordNo }}</div>
                </div>
                <van-tag :type="recordTagType(record.status)">{{ recordStatusText(record.status) }}</van-tag>
              </div>
              <div class="coupon-meta vertical">
                <span>抵扣 {{ moneyText(record.discountAmount) }} {{ record.currency }}，满 {{ moneyText(record.minOrderAmount) }} 可用</span>
                <span>有效期至 {{ timeText(record.validTo) }}</span>
              </div>
            </div>
          </div>
        </van-pull-refresh>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { showSuccessToast } from 'vant';
import { claimCoupon, getCouponRecords, getCouponTemplates } from '@/api/promotion';
import type { PromotionCouponRecord, PromotionCouponTemplate } from '@/api/promotion';
import { isImpersonatedSession, rejectIfImpersonated } from '@/utils/impersonation';

const route = useRoute();
const activeTab = ref(route.query.tab === 'records' ? 'records' : 'templates');
const loading = ref(false);
const refreshing = ref(false);
const recordLoading = ref(false);
const recordRefreshing = ref(false);
const claimingId = ref<number>();
const templates = ref<PromotionCouponTemplate[]>([]);
const records = ref<PromotionCouponRecord[]>([]);
const recordStatus = ref('');
const readonlyMode = computed(() => isImpersonatedSession());

const statusOptions = [
  { text: '全部记录', value: '' },
  { text: '可用', value: 'CLAIMED' },
  { text: '已锁定', value: 'LOCKED' },
  { text: '已使用', value: 'USED' },
  { text: '已过期', value: 'EXPIRED' },
];

const moneyText = (value?: number) => Number(value || 0).toFixed(2);
const stockText = (remain: number) => (remain < 0 ? '不限库存' : `剩余 ${remain}`);
const timeText = (value?: number) => (value ? new Date(value).toLocaleString() : '-');

const recordStatusText = (status: string) => {
  const map: Record<string, string> = {
    CLAIMED: '可用',
    LOCKED: '已锁定',
    USED: '已使用',
    EXPIRED: '已过期',
  };
  return map[status] || status;
};

const recordTagType = (status: string) => {
  if (status === 'CLAIMED') return 'success';
  if (status === 'USED') return 'primary';
  if (status === 'EXPIRED') return 'default';
  return 'warning';
};

const canClaim = (item: PromotionCouponTemplate) => {
  const hasStock = item.remainStock < 0 || item.remainStock > 0;
  return hasStock && item.userClaimedCount < item.perUserLimit;
};

const claimButtonText = (item: PromotionCouponTemplate) => {
  if (readonlyMode.value) return '模拟登录只读';
  if (!canClaim(item)) return '不可领取';
  return '立即领取';
};

const loadTemplates = async () => {
  loading.value = true;
  try {
    templates.value = await getCouponTemplates(30);
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
};

const loadRecords = async () => {
  recordLoading.value = true;
  try {
    records.value = await getCouponRecords(recordStatus.value || undefined, 30);
  } finally {
    recordLoading.value = false;
    recordRefreshing.value = false;
  }
};

const handleClaim = async (templateId: number) => {
  if (rejectIfImpersonated()) {
    return;
  }
  claimingId.value = templateId;
  try {
    await claimCoupon(templateId);
    showSuccessToast('领取成功');
    await Promise.all([loadTemplates(), loadRecords()]);
  } finally {
    claimingId.value = undefined;
  }
};

const handleTabChange = async (name: string | number) => {
  if (name === 'records') {
    await loadRecords();
  }
};

onMounted(async () => {
  await Promise.all([loadTemplates(), loadRecords()]);
});
</script>

<style scoped>
.coupon-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 24px;
}

.hero-card {
  margin: 12px;
  padding: 20px 16px;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, #7c4dff 0%, #1989fa 100%);
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

.list-wrap {
  padding: 12px;
}

.coupon-card {
  margin-bottom: 12px;
  padding: 14px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.coupon-main {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.coupon-title {
  font-size: 16px;
  font-weight: 700;
  color: #323233;
}

.coupon-code {
  margin-top: 4px;
  font-size: 11px;
  color: #969799;
  word-break: break-all;
}

.coupon-amount {
  color: #ee0a24;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
}

.coupon-desc {
  margin-top: 10px;
  color: #646566;
  font-size: 13px;
  line-height: 1.5;
}

.coupon-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 12px 0;
  color: #969799;
  font-size: 12px;
}

.coupon-meta.vertical {
  display: grid;
  gap: 6px;
}

.record-card {
  border-left: 4px solid #1989fa;
}
</style>