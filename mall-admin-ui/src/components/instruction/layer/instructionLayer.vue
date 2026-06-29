<template>
  <div class="instruction_layer" title="点击查看说明" @click="open">
    <icon-info-circle-fill
      :style="{ verticalAlign: iconStyle ? iconStyle : 'middle' }"
    />
  </div>

  <a-modal v-model:visible="visible" title="功能说明" width="30%">
    <div style="white-space: pre-wrap">
      {{ getInstruction(props.name) }}
    </div>
    <template #footer>
      <a-button type="primary" @click="visible = false"> 确定 </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import getInstruction from '@/components/instruction/layer/instructionLayer';
  import { ref } from 'vue';

  const props = defineProps<{
    name: string;
    iconStyle?: string;
  }>();

  const visible = ref(false);

  const open = () => {
    visible.value = true;
  };
</script>

<style lang="scss">
  .instruction_layer {
    align-items: center;
    cursor: pointer;
    overflow: hidden;
  }
  .instruction_layer:hover {
    transform: scale(1.1, 1.1);
  }
</style>
