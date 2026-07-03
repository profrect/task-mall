<template>
  <van-cell class="task-cell" :border="false">
    <template #icon>
      <div class="task-icon">
        <van-icon name="todo-list-o" color="#fff" size="18" />
      </div>
    </template>

    <template #title>
      <div class="task-title">{{ task.title }}</div>
      <div class="task-desc" v-if="task.description">{{ task.description }}</div>
      <div class="task-reward">+{{ moneyText(task.rewardAmount) }} {{ task.currency }}</div>
      <van-tag v-if="task.userStatus" :type="statusTagType" plain class="status-tag">
        {{ statusText }}
      </van-tag>
    </template>

    <template #right-icon>
      <van-button size="small" :type="btnType" :disabled="btnDisabled" @click.stop="emit('action')">
        {{ btnText }}
      </van-button>
    </template>
  </van-cell>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { MissionTaskItem } from '@/api/mission';

const props = defineProps<{
  task: MissionTaskItem;
  readonly?: boolean;
}>();

const emit = defineEmits<{ action: [] }>();

const statusText = computed(() => {
  const map: Record<string, string> = {
    CLAIMED: 'Claimed',
    SUBMITTED: 'Reviewing',
    APPROVED: 'Approved',
    REJECTED: 'Rejected',
    CANCELLED: 'Cancelled',
    EXPIRED: 'Expired',
  };
  return props.task.userStatus ? map[props.task.userStatus] || props.task.userStatus : '';
});

const statusTagType = computed(() => {
  if (props.task.userStatus === 'APPROVED') return 'success';
  if (props.task.userStatus === 'REJECTED') return 'danger';
  if (props.task.userStatus === 'SUBMITTED') return 'warning';
  return 'primary';
});

const btnText = computed(() => {
  if (props.readonly && (!props.task.userStatus || props.task.userStatus === 'CLAIMED' || props.task.userStatus === 'REJECTED')) return 'Read Only';
  if (!props.task.userStatus) return 'Start';
  if (props.task.userStatus === 'CLAIMED' || props.task.userStatus === 'REJECTED') return 'Submit';
  if (props.task.userStatus === 'SUBMITTED') return 'Reviewing';
  return 'Done';
});

const btnType = computed(() => {
  if (!props.task.userStatus) return 'primary';
  if (props.task.userStatus === 'CLAIMED' || props.task.userStatus === 'REJECTED') return 'warning';
  if (props.task.userStatus === 'APPROVED') return 'success';
  return 'default';
});

const btnDisabled = computed(() =>
  Boolean(props.readonly) || ['SUBMITTED', 'APPROVED', 'CANCELLED', 'EXPIRED'].includes(props.task.userStatus || '')
);

const moneyText = (value?: number) => Number(value || 0).toFixed(6);
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
  background: #1989fa;
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
}

.task-desc {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  line-height: 1.4;
}

.task-reward {
  font-size: 12px;
  color: #ff976a;
  margin-top: 2px;
}

.status-tag {
  margin-top: 6px;
}
</style>