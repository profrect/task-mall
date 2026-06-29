<template>
  <div class="success_fail_font">
    <span :style="'color: var(' + statusItem.color + ')'">
      {{ statusItem.name }}
    </span>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive } from 'vue';
  import StatusItem from '@/model/statusItem';

  const props = defineProps<{
    status: number;
    statusMap?: Map<number, StatusItem>;
  }>();

  const baseMap = reactive(
    new Map<number, StatusItem>([
      [0, new StatusItem(0, '失败', '--color_red')],
      [1, new StatusItem(1, '成功', '--color_green')],
    ])
  );

  const statusItem = computed(() => {
    const map = props.statusMap || baseMap;
    return map.get(props.status) || new StatusItem(-1, '', '');
  });
</script>

<style lang="scss"></style>
