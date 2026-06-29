<template>
  <component :is="iconComponent" v-if="iconComponent" :style="style" />
</template>

<script lang="ts" setup>
  import { computed, resolveDynamicComponent } from 'vue';
  import * as ElementIcons from '@element-plus/icons-vue';

  const props = defineProps<{
    icon: string;
    size?: number;
  }>();

  const iconComponent = computed(() => {
    // 1. 先尝试解析为 Arco Design 图标
    if (props.icon.startsWith('icon-')) {
      return resolveDynamicComponent(`${props.icon}`);
    }

    // 2. 尝试解析为 Element Plus 图标
    if (props.icon.startsWith('el-')) {
      const result = props.icon.replace('el-', '');
      return (ElementIcons as { [key: string]: any })[result];
    }
    return null;
  });

  const style = computed(() => {
    if (props.size) {
      return { width: `${props.size}px`, height: `${props.size}px` };
    }
    return null;
  });
</script>
