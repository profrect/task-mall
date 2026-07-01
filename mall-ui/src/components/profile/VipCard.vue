<template>
  <div class="vip-card" :class="{ current: isCurrent, locked: isLocked }">
    <div class="card-header">
      <span class="vip-name">{{ vip.levelName }}</span>
      <van-tag v-if="isCurrent" type="warning" effect="dark">Current</van-tag>
      <van-tag v-else-if="isAchieved" type="success" plain>Achieved</van-tag>
    </div>

    <div class="benefits">
      <van-tag
        v-for="benefit in benefits"
        :key="benefit"
        plain
        type="primary"
        size="small"
        class="benefit-tag"
      >
        {{ benefit }}
      </van-tag>
    </div>

    <div class="card-footer">
      <span class="price">{{ vip.price === 0 ? 'Free' : `$${vip.price}` }}</span>
      <van-button
        v-if="!isCurrent && !isAchieved"
        type="warning"
        size="small"
        round
        :disabled="isLocked"
        @click="$emit('upgrade', vip)"
      >
        {{ isLocked ? 'Locked' : 'Upgrade' }}
      </van-button>
      <van-button v-else-if="isCurrent" disabled size="small" round>Active</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { VipLevelConfig } from '@/api/user'

const props = defineProps<{
  vip: VipLevelConfig
  currentLevel: number
}>()

defineEmits(['upgrade'])

const benefits = computed(() => {
  const text = props.vip.benefits || ''
  const list = text.split(/[\n,，]/).map((v) => v.trim()).filter(Boolean)
  return list.length ? list : ['标准会员权益']
})
const isCurrent = computed(() => props.vip.level === props.currentLevel)
const isAchieved = computed(() => props.vip.level < props.currentLevel)
// 只能逐级升级，不能跳级购买
const isLocked = computed(() => props.vip.level > props.currentLevel + 1)
</script>

<style scoped>
.vip-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid transparent;
  transition: all 0.2s;
}
.vip-card.current {
  border-color: #ffab00;
  background: #fffde7;
}
.vip-card.locked {
  opacity: 0.6;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.vip-name {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.benefits {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}
.benefit-tag {
  font-size: 10px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price {
  font-size: 18px;
  font-weight: 700;
  color: #ffab00;
}
</style>
