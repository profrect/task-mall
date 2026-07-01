<template>
  <div class="team-page">
    <van-nav-bar title="团队" left-arrow fixed placeholder @click-left="$router.back()" />

    <div class="team-card">
      <div class="team-main">
        <span class="team-label">Direct Members</span>
        <span class="team-value">{{ userInfo.teamMemberNum ?? 0 }}</span>
      </div>
      <div class="team-sub">
        <div class="sub-item">
          <span class="sub-val">{{ userInfo.inviteUser || 'Platform Official' }}</span>
          <span class="sub-label">Direct Superior</span>
        </div>
        <div class="sub-item">
          <span class="sub-val">{{ userInfo.inviteCode || '-' }}</span>
          <span class="sub-label">Invite Code</span>
        </div>
      </div>
    </div>

    <van-tabs v-model:active="activeTab" sticky offset-top="46" swipeable>
      <van-tab title="Members" name="members">
        <MemberList />
      </van-tab>
      <van-tab title="Earnings" name="earnings">
        <EarningList />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import MemberList from '@/components/team/MemberList.vue'
import EarningList from '@/components/team/EarningList.vue'
import { getCurrentUser, type UserDetail } from '@/api/user'

const route = useRoute()
const activeTab = ref(String(route.query.tab || 'members'))
const userInfo = ref<UserDetail>({
  userId: 0,
  account: '',
  nickName: '',
  vipLevel: '0',
  inviteCode: '',
  inviteUser: '',
  teamMemberNum: 0,
})

async function loadUser() {
  userInfo.value = await getCurrentUser()
}

onMounted(loadUser)
</script>

<style scoped>
.team-page {
  background: #f5f6fa;
  min-height: 100vh;
}
.team-card {
  margin: 12px;
  padding: 24px 20px;
  background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
  border-radius: 12px;
  color: #fff;
}
.team-main .team-label {
  font-size: 12px;
  opacity: 0.8;
}
.team-main .team-value {
  display: block;
  font-size: 36px;
  font-weight: 700;
  margin-top: 6px;
}
.team-sub {
  display: flex;
  gap: 24px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}
.sub-item {
  flex: 1;
  min-width: 0;
}
.sub-val {
  display: block;
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sub-label {
  display: block;
  font-size: 11px;
  opacity: 0.7;
  margin-top: 2px;
}
:deep(.van-tabs__nav) {
  background: transparent;
  margin: 0 12px;
  width: calc(100% - 24px);
}
:deep(.van-tabs__content) {
  min-height: 60vh;
}
</style>