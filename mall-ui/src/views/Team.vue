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
      <van-tab title="直属成员" name="members">
        <MemberList />
      </van-tab>
      <van-tab title="收益" name="earnings">
        <EarningList />
      </van-tab>
      <van-tab title="层级" name="levels">
        <van-cell-group inset class="level-group">
          <van-cell title="一级团队" :value="`${userInfo.teamMemberNum ?? 0} 人`" label="已接入直属成员列表接口。" />
          <van-cell title="二级团队" value="待开放" label="当前后端未提供二级团队分页与统计接口。" />
          <van-cell title="三级团队" value="待开放" label="当前后端未提供三级团队分页与统计接口。" />
          <van-cell title="邀请好友" :value="userInfo.inviteCode || '-'" is-link to="/invite" />
          <van-cell title="收益记录" value="查看返佣" is-link to="/income" />
        </van-cell-group>
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
.level-group {
  margin: 12px;
  overflow: hidden;
  border-radius: 12px;
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