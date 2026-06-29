<template>
  <div class="common-card" :class="{ 'card-highlight': highlight }">
    <div v-if="title" class="card-title">{{ title }}</div>
    <div v-if="subtitle" class="card-subtitle">{{ subtitle }}</div>
    <div class="card-body"><slot /></div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  subtitle?: string
  highlight?: boolean
}>()
</script>

<style scoped>
.common-card {
  background: var(--bg-card);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  padding: 16px;
  margin: 12px 16px; /* 左右统一16px边距 */
}

/* 顶部高亮模式 */
.card-highlight {
  background: linear-gradient(135deg, var(--bg-highlight-start) 0%, var(--bg-highlight-end) 100%);
  min-height: 160px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-top: 0;
  border-radius: 0 0 var(--radius-card) var(--radius-card);
  padding: 20px 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.card-highlight .card-title {
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
}
.card-highlight .card-subtitle {
  color: rgba(255, 255, 255, 0.85);
}

.card-subtitle {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.card-body {
  flex-shrink: 0;
}

/* 按钮层次适配 */
.card-highlight :deep(.van-button--primary) {
  background: #ffffff;
  color: var(--color-brand);
  border: none;
  border-radius: var(--radius-btn);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  font-weight: 600;
}
.card-highlight :deep(.van-button--primary:active) {
  transform: scale(0.98);
  background: #f0f5ff;
}

/* 🔽 修改点：进度条轨道色改为半透明白，适配深蓝背景 */
.card-highlight :deep(.van-progress__track) {
  background: rgba(255, 255, 255, 0.3);
}
</style>
