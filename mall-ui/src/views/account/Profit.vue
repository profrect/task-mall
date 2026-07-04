<template>
  <div class="profit-page">
    <van-nav-bar title="收益汇总" left-arrow fixed placeholder @click-left="router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="loadData">
      <div class="summary-card">
        <div class="summary-label">累计真实收益</div>
        <div class="summary-value">{{ formatMoney(totalEarning) }} USDT</div>
        <div class="summary-desc">
          仅统计钱包已入账的任务奖励、邀请返佣、抽奖奖励、签到奖励和分享任务奖励。
        </div>
      </div>

      <div class="metric-grid">
        <div class="metric-card">
          <span class="metric-value">{{ formatMoney(missionStats.totalEarnings) }}</span>
          <span class="metric-label">任务收益</span>
        </div>
        <div class="metric-card">
          <span class="metric-value">{{ formatMoney(inviteCommissionTotal) }}</span>
          <span class="metric-label">邀请返佣</span>
        </div>
        <div class="metric-card">
          <span class="metric-value">{{ earningFlows.length }}</span>
          <span class="metric-label">入账笔数</span>
        </div>
      </div>

      <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink>
        <van-tab title="收益流水" name="flows">
          <div class="list-wrap">
            <van-skeleton v-if="loading" title :row="5" />
            <van-empty v-else-if="!earningFlows.length" image="search" description="暂无真实收益流水" />
            <div v-for="item in earningFlows" v-else :key="item.flowNo" class="flow-card">
              <div class="flow-main">
                <span class="flow-title">{{ bizTypeText(item.bizType) }}</span>
                <span class="flow-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="flow-amount">+{{ formatMoney(item.changeAmt) }} USDT</div>
              <div v-if="item.remark" class="flow-remark">{{ item.remark }}</div>
            </div>
          </div>
        </van-tab>

        <van-tab title="返佣记录" name="commission">
          <div class="list-wrap">
            <van-empty v-if="!commissions.length" image="search" description="暂无真实返佣记录" />
            <div v-for="item in commissions" v-else :key="item.recordNo" class="flow-card">
              <div class="flow-main">
                <span class="flow-title">{{ item.businessType }}</span>
                <span class="flow-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="flow-amount">+{{ formatMoney(item.commissionAmount) }} {{ item.currency }}</div>
              <div class="flow-remark">
                来源订单 {{ item.sourceOrderNo }} · 状态 {{ item.status }}
              </div>
            </div>
          </div>
        </van-tab>

        <van-tab title="口径说明" name="rules">
          <van-cell-group inset class="rule-group">
            <van-cell title="收益来源" label="钱包流水中的真实入账类型，不在前端本地制造收益。" />
            <van-cell title="任务收益" label="来自任务域统计和任务奖励流水，可与任务中心核对。" />
            <van-cell title="邀请返佣" label="来自团队返佣接口，结算成功后同步写入钱包流水。" />
            <van-cell title="待补能力" label="参考站的签到、分享任务、视频任务、VA 任务奖励需后端状态机补齐后才会进入统计。" />
          </van-cell-group>
        </van-tab>
      </van-tabs>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMissionStats, type MissionTaskStats } from '@/api/mission'
import { getTeamEarnings, type InviteCommissionRecord } from '@/api/user'
import { getWalletFlows, type WalletFlowRecord } from '@/api/wallet'

const router = useRouter()

const EARNING_BIZ_TYPES = new Set([
  'TASK_REWARD',
  'TASK_CENTER_REWARD',
  'SHARE_TASK_REWARD',
  'VIDEO_TASK_REWARD',
  'VA_TASK_REWARD',
  'INVITE_COMMISSION',
  'CHECKIN_REWARD',
  'LOTTERY_REWARD',
])

const activeTab = ref('flows')
const loading = ref(false)
const refreshing = ref(false)
const flows = ref<WalletFlowRecord[]>([])
const commissions = ref<InviteCommissionRecord[]>([])
const missionStats = ref<MissionTaskStats>({
  completedCount: 0,
  inProgressCount: 0,
  totalEarnings: 0,
})

const earningFlows = computed(() =>
  flows.value.filter((item) => EARNING_BIZ_TYPES.has(item.bizType) && item.direction === 'IN')
)

const totalEarning = computed(() =>
  earningFlows.value.reduce((sum, item) => sum + Number(item.changeAmt || 0), 0)
)

const inviteCommissionTotal = computed(() =>
  commissions.value.reduce((sum, item) => sum + Number(item.commissionAmount || 0), 0)
)

async function loadData() {
  loading.value = true
  try {
    const [flowRows, stats, commissionPage] = await Promise.all([
      getWalletFlows(200),
      getMissionStats(),
      getTeamEarnings(1, 100),
    ])
    flows.value = flowRows
    missionStats.value = stats
    commissions.value = commissionPage.records || []
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function formatMoney(value?: number) {
  return Number(value || 0).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 6,
  })
}

function formatTime(value?: number) {
  if (!value) return '-'
  return new Date(Number(value)).toLocaleString()
}

function bizTypeText(value: string) {
  const map: Record<string, string> = {
    TASK_REWARD: '任务奖励',
    TASK_CENTER_REWARD: '任务中心奖励',
    SHARE_TASK_REWARD: '分享任务奖励',
    VIDEO_TASK_REWARD: '视频任务奖励',
    VA_TASK_REWARD: 'VA任务奖励',
    INVITE_COMMISSION: '邀请返佣',
    CHECKIN_REWARD: '签到奖励',
    LOTTERY_REWARD: '抽奖奖励',
  }
  return map[value] || value
}

onMounted(loadData)
</script>

<style scoped>
.profit-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 24px;
}
.summary-card {
  margin: 12px;
  padding: 22px 18px;
  border-radius: 16px;
  color: #fff;
  background: linear-gradient(135deg, #00695c 0%, #26a69a 100%);
}
.summary-label {
  font-size: 13px;
  opacity: 0.88;
}
.summary-value {
  margin-top: 8px;
  font-size: 30px;
  font-weight: 800;
}
.summary-desc {
  margin-top: 10px;
  font-size: 12px;
  line-height: 1.6;
  opacity: 0.86;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin: 12px;
}
.metric-card {
  padding: 14px 8px;
  border-radius: 12px;
  background: #fff;
  text-align: center;
}
.metric-value {
  display: block;
  color: #1989fa;
  font-size: 16px;
  font-weight: 700;
}
.metric-label {
  display: block;
  margin-top: 6px;
  color: #969799;
  font-size: 11px;
}
.list-wrap {
  padding: 12px;
}
.flow-card {
  margin-bottom: 10px;
  padding: 14px 12px;
  border-radius: 12px;
  background: #fff;
}
.flow-main {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.flow-title {
  color: #323233;
  font-size: 14px;
  font-weight: 700;
}
.flow-time {
  color: #969799;
  font-size: 11px;
  white-space: nowrap;
}
.flow-amount {
  margin-top: 8px;
  color: #07c160;
  font-size: 18px;
  font-weight: 800;
}
.flow-remark {
  margin-top: 6px;
  color: #969799;
  font-size: 12px;
  line-height: 1.5;
}
.rule-group {
  margin: 12px;
  overflow: hidden;
  border-radius: 12px;
}
:deep(.van-tabs__nav) {
  background: transparent;
  margin: 0 12px;
  width: calc(100% - 24px);
}
</style>