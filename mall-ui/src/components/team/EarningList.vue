<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多收益"
      @load="onLoad"
    >
      <div class="list-wrapper">
        <EarningItem v-for="item in list" :key="item.recordNo" :record="item" />
      </div>
      <van-empty v-if="!loading && !list.length" description="暂无真实团队收益" image="search">
        <template #description>
          <div class="desc">
            下级完成符合规则的付费 VIP 升级后，系统会生成返佣记录并通过钱包入账。
          </div>
        </template>
      </van-empty>
    </van-list>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getTeamEarnings, type InviteCommissionRecord } from '@/api/user'
import EarningItem from './EarningItem.vue'

const PAGE_SIZE = 15

const list = ref<InviteCommissionRecord[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)

async function onLoad() {
  try {
    if (refreshing.value) {
      list.value = []
      refreshing.value = false
    }
    const page = await getTeamEarnings(pageNum.value, PAGE_SIZE)
    list.value.push(...page.records)
    pageNum.value += 1
    finished.value = list.value.length >= page.totalRow || page.records.length < PAGE_SIZE
  } finally {
    loading.value = false
  }
}

function onRefresh() {
  pageNum.value = 1
  finished.value = false
  loading.value = true
  onLoad()
}
</script>

<style scoped>
.list-wrapper {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.desc {
  max-width: 280px;
  margin: 0 auto;
  font-size: 12px;
  line-height: 1.6;
  color: #999;
}
</style>