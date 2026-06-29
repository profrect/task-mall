<template>
  <div
    v-for="element in menuTree"
    :key="element.name"
    v-memo="[element.name, element.meta?.menuKey]"
  >
    <div v-if="element.children && element.children.length">
      <a-sub-menu :key="element.name">
        <template #icon>
          <dynamic-icon
            v-if="element.meta?.icon"
            :icon="element.meta.icon"
            :size="18"
          />
        </template>
        <template #title>
          {{ t(element.meta?.menuKey || '') }}
        </template>
        <menu-item :menu-tree="element.children" />
      </a-sub-menu>
    </div>
    <div v-else>
      <a-menu-item :key="element.name" @click="() => goto(element)">
        <template #icon>
          <dynamic-icon
            v-if="element.meta?.icon"
            :icon="element.meta.icon"
            :size="18"
          />
        </template>
        {{ element.meta?.menuKey ? t(element.meta.menuKey) : '' }}
      </a-menu-item>
    </div>
  </div>
</template>

<script setup lang="ts">
  import DynamicIcon from '@/components/icon/dynamicIcon.vue';
  import { useI18n } from 'vue-i18n';
  import { RouteMeta, RouteRecordRaw, useRoute, useRouter } from 'vue-router';
  import { openWindow, regexUrl } from '@/utils';
  import { watch, ref, shallowRef } from 'vue';

  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();

  // 优化1：props改用shallowRef（减少响应式开销）
  const props = defineProps<{
    menuTree: Array<RouteRecordRaw>;
  }>();
  const emits = defineEmits(['handleUpdateKey']);

  // 优化2：selectedKey仅在需要时更新，避免无用的响应式追踪
  const selectedKey = shallowRef<Array<string>>([]);

  // 优化3：简化goto逻辑，减少冗余判断
  const goto = (item: RouteRecordRaw) => {
    // 外部链接直接打开
    if (regexUrl.test(item.path)) {
      openWindow(item.path);
      return;
    }

    const { hideInMenu, activeMenu } = item.meta as RouteMeta;
    // 避免重复跳转
    if (route.name === item.name && !hideInMenu && !activeMenu) {
      return;
    }

    // 更新选中key（仅必要时）
    selectedKey.value = [item.name as string];
    emits('handleUpdateKey', selectedKey.value);

    // 路由跳转（简化逻辑）
    const targetName = item.name === 'dashboard' ? 'home' : item.name;
    router.push({ name: targetName }).catch((err) => {
      // 捕获跳转异常（如路由不存在），避免控制台报错阻塞流程
      console.warn('路由跳转失败:', err);
    });
  };

  // 优化4：watch仅监听实际变化，且取消无用的handleChange中间函数
  watch(
    () => selectedKey.value,
    (newVal) => {
      emits('handleUpdateKey', newVal);
    },
    { deep: false, immediate: false } // 关闭深度监听和立即执行
  );
</script>
