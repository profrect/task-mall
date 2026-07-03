<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.user-member', titleKey]" />
    <a-card :bordered="false">
      <a-result status="info" :title="title">
        <template #subtitle>
          <div class="member-feature-subtitle">
            {{ feature.description }}
          </div>
        </template>
        <template #extra>
          <a-space direction="vertical" fill>
            <a-alert type="info" show-icon>
              当前入口已接入后台会员管理菜单；列表、筛选、按钮和接口闭环会在后续会员动作模块继续补齐。
            </a-alert>
            <a-descriptions :data="descriptionRows" bordered :column="1" />
          </a-space>
        </template>
      </a-result>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import { useRoute } from 'vue-router';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const route = useRoute();

  const featureMap: Record<string, { description: string; scope: string }> = {
    'member-groups': {
      description: '用于维护会员分组，并支撑会员列表筛选和批量设置分组。',
      scope: '分组 CRUD、会员绑定、批量分组',
    },
    'member-log': {
      description: '用于按会员维度聚合登录、状态变更和关键操作记录。',
      scope: '登录日志、后台操作日志、会员状态变更记录',
    },
    'member-flow': {
      description: '用于查看会员账变流水，和钱包流水查询保持同源。',
      scope: '账变类型、金额、前后余额、关联订单',
    },
    'site-message': {
      description: '用于后台给会员发送站内信，并查看读取状态。',
      scope: '消息发送、收件范围、读取状态',
    },
    'telegram-info': {
      description: '用于承载电报信息与机器人配置入口。',
      scope: 'Telegram 绑定信息、机器人配置、自动回复',
    },
    'balance-record': {
      description: '用于查看会员余额变动记录。',
      scope: '可用余额、冻结金额、变更来源',
    },
    'vip-upgrade-record': {
      description: '用于查看会员 VIP 升级记录。',
      scope: '升级前后等级、支付金额、升级时间',
    },
    'share-audit': {
      description: '用于审核会员提交的分享任务。',
      scope: '分享材料、审核通过、审核驳回',
    },
    'test-account': {
      description: '用于管理测试账号，并与会员列表筛选联动。',
      scope: '测试账号标记、登录限制、数据隔离',
    },
  };

  const titleKey = computed(() => String(route.meta.menuKey || ''));
  const title = computed(() => t(titleKey.value));
  const feature = computed(
    () =>
      featureMap[String(route.name || '')] || {
        description: '该会员管理入口已接入，后续按按钮闭环补齐接口。',
        scope: '待补齐',
      }
  );
  const descriptionRows = computed(() => [
    {
      label: '页面作用域',
      value: feature.value.scope,
    },
    {
      label: '接口状态',
      value: '待后续会员动作模块补齐真实接口',
    },
  ]);
</script>

<style scoped lang="less">
  .member-feature-subtitle {
    max-width: 720px;
    margin: 0 auto;
    color: var(--color-text-2);
  }
</style>