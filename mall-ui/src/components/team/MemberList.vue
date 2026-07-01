<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多成员"
      @load="onLoad"
    >
      <div class="list-wrapper">
        <MemberItem v-for="(m, idx) in list" :key="`${m.nickname || 'member'}-${m.invitateTime}-${idx}`" :member="m" />
      </div>
      <van-empty v-if="!loading && !list.length" description="暂无直属成员" image="search" />
    </van-list>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getTeamMembers, type TeamMemberRecord } from '@/api/user'
import MemberItem from './MemberItem.vue'

const PAGE_SIZE = 15

const list = ref<TeamMemberRecord[]>([])
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
    const page = await getTeamMembers(pageNum.value, PAGE_SIZE)
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
</style>