<template>
  <a-col class="banner">
    <a-col :span="8">
      <a-typography-title :heading="5" style="margin-top: 0">
        {{ '您好！' }}<i style="color: #e36161">{{ userInfo.name }}</i>
      </a-typography-title>
      <a-typography-title :heading="5" style="margin-top: 0">
        {{ '欢迎进入' }}
        <i style="color: #e36161">{{ appName }}</i>
        {{ '管理平台' }}
      </a-typography-title>
    </a-col>
    <a-divider class="panel-border" />
  </a-col>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useUserStore } from '@/store';
  import { appName } from '@/utils/env';

  const userStore = useUserStore();
  const userInfo = computed(() => {
    return {
      name: userStore.userInfo.username,
    };
  });
</script>

<style scoped lang="less">
  .banner {
    width: 100%;
    padding: 20px 20px 0 20px;
    background-color: var(--color-bg-2);
    border-radius: 4px 4px 0 0;
  }

  :deep(.arco-icon-home) {
    margin-right: 6px;
  }
</style>
