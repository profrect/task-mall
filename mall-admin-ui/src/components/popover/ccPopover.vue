<template>
  <a-popover :content-style="{ maxWidth: '400px' }" :trigger="ifTrigger">
    <a-tag bordered color="blue" :style="tagStyle">
      <div class="text-ellipsis">
        {{ content }}
      </div>
    </a-tag>
    <template #content>
      <div v-if="element">
        <slot name="element" />
      </div>
      <div v-else>
        {{ content }}
      </div>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import { computed } from 'vue';

  const props = defineProps<{
    content: string;
    tagWidth?: number;
    element?: boolean;
    trigger?: string;
  }>();

  const tagStyle = computed(() => {
    if (props.tagWidth) {
      return { width: `${props.tagWidth}vw` };
    }
    return { width: '13vw' };
  });

  const ifTrigger = computed(() => {
    return props.trigger ? props.trigger : 'hover';
  });
</script>
