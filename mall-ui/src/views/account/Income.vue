<template>
  <div class="income-page">
    <van-nav-bar title="收益" left-arrow fixed placeholder @click-left="router.back()" />

    <div class="income-card">
      <div class="income-title">邀请返佣收益</div>
      <div class="income-desc">
        当前收益页只展示后端已生成的真实返佣记录；任务奖励、抽奖奖励和 VIP 扣款等其它资金变化请在钱包账变查看。
      </div>
      <van-button round size="small" type="primary" @click="router.push('/account/balance')">
        查看钱包账变
      </van-button>
    </div>

    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink>
      <van-tab title="返佣记录" name="commission">
        <EarningList />
      </van-tab>
      <van-tab title="收益说明" name="rules">
        <van-cell-group inset class="rule-group">
          <van-cell title="数据来源" label="收益记录来自邀请返佣接口，不在前端本地计算。" />
          <van-cell title="入账口径" label="结算成功后会写入钱包流水，可在账变页核对。" />
          <van-cell title="待补能力" label="邀请奖励领取、代理职位薪资等参考站能力尚无本地接口，保留为后续后端闭环。" />
        </van-cell-group>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import EarningList from '@/components/team/EarningList.vue'

const router = useRouter()
const activeTab = ref('commission')
</script>

<style scoped>
.income-page {
  min-height: 100vh;
  background: #f5f6fa;
}
.income-card {
  margin: 12px;
  padding: 20px 16px;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, #2e7d32 0%, #43a047 100%);
}
.income-title {
  font-size: 22px;
  font-weight: 700;
}
.income-desc {
  margin: 10px 0 14px;
  font-size: 12px;
  line-height: 1.6;
  opacity: 0.88;
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