<template>
  <van-cell class="task-cell" :border="false">
    <template #icon>
      <div class="task-icon" :style="{ background: task.iconBg }">
        <van-icon :name="task.icon" color="#fff" size="18" />
      </div>
    </template>

    <template #title>
      <div class="task-title">{{ task.title }}</div>
      <div class="task-reward">+{{ task.reward }}</div>
      <!-- 进行中任务显示进度条 -->
      <van-progress
        v-if="task.status === 'in_progress'"
        :percentage="task.progress || 0"
        stroke-width="4"
        color="#2196f3"
        class="task-progress"
      />
    </template>

    <template #right-icon>
      <van-button
        size="small"
        :type="btnType"
        :disabled="task.status === 'completed'"
        @click.stop="emit('action')"
      >
        {{ btnText }}
      </van-button>
    </template>
  </van-cell>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  task: {
    id: number
    title: string
    reward: number
    status: 'available' | 'in_progress' | 'completed'
    progress?: number
    icon: string
    iconBg: string
  }
}>()

defineEmits(['action'])

const btnText = computed(() => {
  if (props.task.status === 'completed') return 'Done'
  if (props.task.status === 'in_progress') return 'Continue'
  return 'Start'
})

const btnType = computed(() => {
  if (props.task.status === 'available') return 'primary'
  if (props.task.status === 'in_progress') return 'warning'
  return 'default'
})
</script>

<style scoped>
.task-cell {
  align-items: center;
  padding: 14px 16px;
}

.task-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
}

.task-reward {
  font-size: 12px;
  color: #ff976a;
  margin-top: 2px;
}

.task-progress {
  margin-top: 6px;
  width: 120px;
}
</style>
