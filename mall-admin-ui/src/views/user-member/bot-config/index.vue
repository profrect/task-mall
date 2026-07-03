<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.user-member', 'menu.user-member.bot-config']" />
    <a-row :gutter="16">
      <a-col :span="13">
        <a-card title="机器人列表" :bordered="false">
          <a-space :size="12" wrap style="margin-bottom: 16px">
            <a-input
              v-model="keyword"
              allow-clear
              placeholder="机器人名称"
              style="width: 200px"
              @press-enter="loadBots"
            />
            <a-select
              v-model="filterStatus"
              style="width: 130px"
              @change="loadBots"
            >
              <a-option :value="undefined">全部</a-option>
              <a-option :value="1">启用</a-option>
              <a-option :value="0">停用</a-option>
            </a-select>
            <a-button type="primary" @click="loadBots">查询</a-button>
            <a-button @click="resetQuery">重置</a-button>
            <a-button type="primary" @click="openBotCreate">新增机器人</a-button>
          </a-space>

          <a-table
            row-key="id"
            :columns="botColumns"
            :data="botData"
            :loading="botLoading"
            :pagination="false"
            :row-selection="{ type: 'radio', selectedRowKeys: selectedBotKeys }"
            :scroll="{ x: 980 }"
            @row-click="handleBotRowClick"
          >
            <template #maskedToken="{ record }">
              <a-tag color="arcoblue">{{ record.maskedToken || '-' }}</a-tag>
            </template>
            <template #status="{ record }">
              <a-tag :color="record.status === 1 ? 'green' : 'gray'">
                {{ record.status === 1 ? '启用' : '停用' }}
              </a-tag>
            </template>
            <template #updateTime="{ record }">
              {{ record.updateTime ? parseTime(record.updateTime) : '-' }}
            </template>
            <template #operations="{ record }">
              <a-space :size="4">
                <a-button type="text" size="mini" @click.stop="openBotEdit(record)">
                  编辑
                </a-button>
                <a-button
                  type="text"
                  size="mini"
                  :status="record.status === 1 ? 'warning' : 'success'"
                  @click.stop="toggleBotStatus(record)"
                >
                  {{ record.status === 1 ? '停用' : '启用' }}
                </a-button>
                <a-popconfirm
                  content="确认删除该机器人？关联自动回复会一并清理。"
                  @ok="doDeleteBot(record.id)"
                >
                  <a-button type="text" size="mini" status="danger" @click.stop>
                    删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :span="11">
        <a-card :title="replyCardTitle" :bordered="false">
          <a-space :size="12" wrap style="margin-bottom: 16px">
            <a-input
              v-model="replyKeyword"
              allow-clear
              placeholder="关键词"
              style="width: 180px"
              :disabled="!selectedBot"
              @press-enter="loadReplies"
            />
            <a-button :disabled="!selectedBot" @click="loadReplies">查询</a-button>
            <a-button type="primary" :disabled="!selectedBot" @click="openReplyCreate">
              新增回复
            </a-button>
          </a-space>
          <a-alert v-if="!selectedBot" type="info" show-icon style="margin-bottom: 12px">
            请先在左侧选择一个机器人。
          </a-alert>
          <a-table
            :columns="replyColumns"
            :data="replyData"
            :loading="replyLoading"
            :pagination="false"
            :scroll="{ x: 760 }"
          >
            <template #matchType="{ record }">
              {{ record.matchType === 2 ? '完全匹配' : '包含' }}
            </template>
            <template #status="{ record }">
              <a-tag :color="record.status === 1 ? 'green' : 'gray'">
                {{ record.status === 1 ? '启用' : '停用' }}
              </a-tag>
            </template>
            <template #operations="{ record }">
              <a-space :size="4">
                <a-button type="text" size="mini" @click="openReplyEdit(record)">
                  编辑
                </a-button>
                <a-button
                  type="text"
                  size="mini"
                  :status="record.status === 1 ? 'warning' : 'success'"
                  @click="toggleReplyStatus(record)"
                >
                  {{ record.status === 1 ? '停用' : '启用' }}
                </a-button>
                <a-popconfirm content="确认删除该自动回复？" @ok="doDeleteReply(record.id)">
                  <a-button type="text" size="mini" status="danger">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:visible="botModalVisible"
      :title="botForm.id ? '编辑机器人' : '新增机器人'"
      :confirm-loading="savingBot"
      width="720px"
      @ok="doSaveBot"
    >
      <a-form :model="botForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="botName" label="机器人名称" required>
              <a-input v-model="botForm.botName" allow-clear :max-length="64" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="botForm.sortOrder" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="status" label="状态">
              <a-select v-model="botForm.status">
                <a-option :value="1">启用</a-option>
                <a-option :value="0">停用</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="botToken" :label="botForm.id ? 'Token（留空则保留原 Token）' : 'Token'" required>
          <a-input-password
            v-model="botForm.botToken"
            allow-clear
            :max-length="255"
            placeholder="不会调用 Telegram API，仅保存配置"
          />
        </a-form-item>
        <a-form-item field="webhookUrl" label="Webhook">
          <a-input v-model="botForm.webhookUrl" allow-clear :max-length="255" />
        </a-form-item>
        <a-form-item field="description" label="说明">
          <a-input v-model="botForm.description" allow-clear :max-length="255" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="replyModalVisible"
      :title="replyForm.id ? '编辑自动回复' : '新增自动回复'"
      :confirm-loading="savingReply"
      width="680px"
      @ok="doSaveReply"
    >
      <a-form :model="replyForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="keyword" label="关键词" required>
              <a-input v-model="replyForm.keyword" allow-clear :max-length="128" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="matchType" label="匹配方式">
              <a-select v-model="replyForm.matchType">
                <a-option :value="1">包含</a-option>
                <a-option :value="2">完全匹配</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="status" label="状态">
              <a-select v-model="replyForm.status">
                <a-option :value="1">启用</a-option>
                <a-option :value="0">停用</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="replyContent" label="回复内容" required>
          <a-textarea
            v-model="replyForm.replyContent"
            allow-clear
            :auto-size="{ minRows: 4, maxRows: 8 }"
            :max-length="2000"
          />
        </a-form-item>
        <a-form-item field="sortOrder" label="排序">
          <a-input-number v-model="replyForm.sortOrder" :min="0" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { TableColumnData, TableData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import {
    BotAutoReply,
    BotConfig,
    botConfigList,
    botReplyList,
    deleteBotConfig,
    deleteBotReply,
    saveBotConfig,
    saveBotReply,
  } from '@/api/bot';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'bot-config',
  });

  const botColumns: TableColumnData[] = [
    { title: '名称', dataIndex: 'botName', align: 'center', width: 160 },
    { title: 'Token', slotName: 'maskedToken', align: 'center', width: 160 },
    { title: 'Webhook', dataIndex: 'webhookUrl', align: 'center', ellipsis: true, tooltip: true, width: 220 },
    { title: '状态', slotName: 'status', align: 'center', width: 90 },
    { title: '更新时间', slotName: 'updateTime', align: 'center', width: 170 },
    { title: '操作', slotName: 'operations', align: 'center', fixed: 'right', width: 190 },
  ];

  const replyColumns: TableColumnData[] = [
    { title: '关键词', dataIndex: 'keyword', align: 'center', width: 140 },
    { title: '回复内容', dataIndex: 'replyContent', align: 'left', ellipsis: true, tooltip: true, width: 220 },
    { title: '匹配', slotName: 'matchType', align: 'center', width: 100 },
    { title: '状态', slotName: 'status', align: 'center', width: 90 },
    { title: '操作', slotName: 'operations', align: 'center', fixed: 'right', width: 180 },
  ];

  const emptyBotForm = (): BotConfig => ({
    botName: '',
    botToken: '',
    webhookUrl: '',
    description: '',
    sortOrder: 0,
    status: 1,
  });

  const emptyReplyForm = (): BotAutoReply => ({
    botId: 0,
    keyword: '',
    replyContent: '',
    matchType: 1,
    sortOrder: 0,
    status: 1,
  });

  const keyword = ref('');
  const filterStatus = ref<number | undefined>();
  const botLoading = ref(false);
  const savingBot = ref(false);
  const botModalVisible = ref(false);
  const botData = ref<BotConfig[]>([]);
  const selectedBot = ref<BotConfig | null>(null);
  const botForm = reactive<BotConfig>(emptyBotForm());

  const replyKeyword = ref('');
  const replyLoading = ref(false);
  const savingReply = ref(false);
  const replyModalVisible = ref(false);
  const replyData = ref<BotAutoReply[]>([]);
  const replyForm = reactive<BotAutoReply>(emptyReplyForm());

  const selectedBotKeys = computed(() =>
    selectedBot.value?.id ? [selectedBot.value.id] : []
  );
  const replyCardTitle = computed(() =>
    selectedBot.value ? `自动回复 - ${selectedBot.value.botName}` : '自动回复'
  );

  const assignBotForm = (item?: BotConfig) => {
    Object.assign(botForm, emptyBotForm(), item || {}, { botToken: '' });
  };

  const assignReplyForm = (item?: BotAutoReply) => {
    Object.assign(replyForm, emptyReplyForm(), item || {}, {
      botId: selectedBot.value?.id || item?.botId || 0,
    });
  };

  const loadBots = () => {
    botLoading.value = true;
    botConfigList({ keyword: keyword.value, status: filterStatus.value })
      .then((rep) => {
        if (rep.success) {
          botData.value = rep.data || [];
          if (!selectedBot.value && botData.value.length) {
            selectBot(botData.value[0]);
          } else if (selectedBot.value) {
            selectedBot.value = botData.value.find((item) => item.id === selectedBot.value?.id) || null;
            loadReplies();
          }
        }
      })
      .finally(() => {
        botLoading.value = false;
      });
  };

  const resetQuery = () => {
    keyword.value = '';
    filterStatus.value = undefined;
    loadBots();
  };

  const selectBot = (item: BotConfig) => {
    selectedBot.value = item;
    replyKeyword.value = '';
    loadReplies();
  };

  const handleBotRowClick = (record: TableData) => {
    selectBot(record as BotConfig);
  };

  const loadReplies = () => {
    if (!selectedBot.value?.id) {
      replyData.value = [];
      return;
    }
    replyLoading.value = true;
    botReplyList({ botId: selectedBot.value.id, keyword: replyKeyword.value })
      .then((rep) => {
        if (rep.success) {
          replyData.value = rep.data || [];
        }
      })
      .finally(() => {
        replyLoading.value = false;
      });
  };

  const openBotCreate = () => {
    assignBotForm();
    botModalVisible.value = true;
  };

  const openBotEdit = (item: BotConfig) => {
    assignBotForm(item);
    botModalVisible.value = true;
  };

  const doSaveBot = () => {
    if (!botForm.botName.trim()) {
      Message.warning('请填写机器人名称');
      return false;
    }
    if (!botForm.id && !botForm.botToken?.trim()) {
      Message.warning('新增机器人必须填写 Token');
      return false;
    }
    savingBot.value = true;
    return saveBotConfig(botPayload(botForm))
      .then((rep) => {
        if (rep.success) {
          Message.success('保存成功');
          botModalVisible.value = false;
          selectedBot.value = rep.data;
          loadBots();
        }
      })
      .finally(() => {
        savingBot.value = false;
      });
  };

  const toggleBotStatus = (item: BotConfig) => {
    saveBotConfig(botPayload({ ...item, status: item.status === 1 ? 0 : 1 }))
      .then((rep) => {
        if (rep.success) {
          Message.success('状态已更新');
          loadBots();
        }
      });
  };

  const doDeleteBot = (id?: number) => {
    if (!id) return;
    deleteBotConfig(id).then((rep) => {
      if (rep.success) {
        Message.success('删除成功');
        if (selectedBot.value?.id === id) {
          selectedBot.value = null;
          replyData.value = [];
        }
        loadBots();
      }
    });
  };

  const openReplyCreate = () => {
    if (!selectedBot.value?.id) return;
    assignReplyForm();
    replyModalVisible.value = true;
  };

  const openReplyEdit = (item: BotAutoReply) => {
    assignReplyForm(item);
    replyModalVisible.value = true;
  };

  const doSaveReply = () => {
    if (!selectedBot.value?.id) return false;
    if (!replyForm.keyword.trim() || !replyForm.replyContent.trim()) {
      Message.warning('请填写关键词和回复内容');
      return false;
    }
    savingReply.value = true;
    return saveBotReply(replyPayload(replyForm))
      .then((rep) => {
        if (rep.success) {
          Message.success('保存成功');
          replyModalVisible.value = false;
          loadReplies();
        }
      })
      .finally(() => {
        savingReply.value = false;
      });
  };

  const toggleReplyStatus = (item: BotAutoReply) => {
    saveBotReply(replyPayload({ ...item, status: item.status === 1 ? 0 : 1 }))
      .then((rep) => {
        if (rep.success) {
          Message.success('状态已更新');
          loadReplies();
        }
      });
  };

  const doDeleteReply = (id?: number) => {
    if (!id) return;
    deleteBotReply(id).then((rep) => {
      if (rep.success) {
        Message.success('删除成功');
        loadReplies();
      }
    });
  };

  const botPayload = (item: BotConfig): BotConfig => ({
    id: item.id,
    botName: item.botName,
    botToken: item.botToken || '',
    webhookUrl: item.webhookUrl || '',
    description: item.description || '',
    sortOrder: item.sortOrder || 0,
    status: item.status,
  });

  const replyPayload = (item: BotAutoReply): BotAutoReply => ({
    id: item.id,
    botId: selectedBot.value?.id || item.botId,
    keyword: item.keyword,
    replyContent: item.replyContent,
    matchType: item.matchType || 1,
    sortOrder: item.sortOrder || 0,
    status: item.status,
  });

  loadBots();
</script>