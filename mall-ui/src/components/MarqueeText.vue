<template>
  <div class="marquee-container">
    <div class="marquee-content">
      <div v-for="(item, i) in items" :key="i" class="marquee-item">{{ item }}</div>
    </div>
    <span class="notification-icon">📢</span>
  </div>
</template>

<script setup lang="ts">
defineProps<{ items: string[] }>()
</script>

<style scoped>
.marquee-container {
  position: relative;
  height: 20px;
  overflow: hidden;
  background: #fff7cc;
  border-radius: 5px;
  margin-top: 10px;
}

.marquee-content {
  position: absolute;
  width: 100%;
  line-height: 40px;
  animation: marquee 10s linear infinite;
}

@keyframes marquee {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-100%);
  }
}

.marquee-item {
  padding: 0 10px;
  white-space: nowrap;
  font-size: 11px;
}

.notification-icon {
  position: absolute;
  bottom: 5px;
  right: 5px;
  font-size: 11px;
}
</style>
