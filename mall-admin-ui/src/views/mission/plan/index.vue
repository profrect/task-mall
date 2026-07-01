<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.mission', 'menu.mission.plan']" />
    <a-card>
      <a-result status="info" title="自动任务计划暂未开放">
        <template #subtitle>
          当前已接入真实任务配置、用户领取提交、后台审核和钱包奖励入账闭环。
          自动任务计划需要独立的计划表、调度器、失败重试和审计日志，本轮不使用静态数据伪造。
        </template>
      </a-result>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  defineOptions({
    name: 'mission-plan',
  });
</script>