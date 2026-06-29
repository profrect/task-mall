<template>
  <a-watermark
    :grayscale="true"
    :alpha="0.6"
    :z-index="9999"
    style="width: 100%; height: 100%"
    :content="[appName + '-管理平台', useUserStore().userInfo.username]"
  >
    <router-view v-slot="{ Component, route }">
      <transition name="fade" mode="out-in" appear>
        <keep-alive :include="cacheList">
          <component :is="Component" :key="route.name" />
        </keep-alive>
      </transition>
    </router-view>
  </a-watermark>
</template>

<script lang="ts" setup>
  import { appName } from '@/utils/env';
  import { computed } from 'vue';
  import { useTabBarStore } from '@/store';
  import useUserStore from '../store/user';

  const tabBarStore = useTabBarStore();
  const cacheList = computed(() => tabBarStore.getCacheList);
</script>

<style scoped lang="less"></style>
