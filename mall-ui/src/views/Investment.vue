<template>
  <div class="invest-page">
    <van-nav-bar title="投资" left-arrow fixed placeholder @click-left="$router.back()" />
    <div class="asset-card">
      <div class="balance-section">
        <span class="balance-label">Task Center Projects</span>
        <span class="balance-value">{{ projects.length }}</span>
      </div>
      <div class="boundary-text">
        当前投资项目仅作为任务中心商品/计划展示口径，不产生独立投资扣款、派息、分红或到期结算。
      </div>
    </div>

    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink>
      <van-tab title="Available" name="available">
        <InvestList :projects="projects" :loading="loading" @refresh="loadProjects" />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import InvestList from '@/components/invest/InvestList.vue';
import { MissionInvestProject, getInvestProjects } from '@/api/mission';

const activeTab = ref('available');
const projects = ref<MissionInvestProject[]>([]);
const loading = ref(false);

const loadProjects = async () => {
  loading.value = true;
  try {
    projects.value = await getInvestProjects(50);
  } finally {
    loading.value = false;
  }
};

onMounted(loadProjects);
</script>

<style scoped>
.invest-page {
  background: #f5f6fa;
  min-height: 100vh;
}

.asset-card {
  margin: 12px;
  padding: 20px 16px;
  background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
  border-radius: 12px;
  color: #fff;
}

.balance-section {
  margin-bottom: 12px;
}
.balance-label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
}
.balance-value {
  display: block;
  font-size: 32px;
  font-weight: 700;
  margin-top: 6px;
  letter-spacing: -0.5px;
}

.boundary-text {
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  font-size: 12px;
  line-height: 1.5;
  opacity: 0.86;
}

:deep(.van-tabs__nav) {
  background: transparent;
  margin: 0 12px;
  width: calc(100% - 24px);
}
:deep(.van-tabs__content) {
  min-height: 60vh;
}
</style>