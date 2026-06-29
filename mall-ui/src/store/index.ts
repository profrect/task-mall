import { reactive, readonly, watch } from 'vue';
import type { User, Wallet, Task, Investment, TeamMember, VipBenefit, TransactionRecord } from '@/types';
import StorageUtil from '@/utils/storage';

interface State {
  user: User;
  wallet: Wallet;
  tasks: { pending: Task[]; inProgress: Task[]; completed: Task[] };
  investments: { available: Investment[]; participated: Investment[]; history: Investment[] };
  teamMembers: TeamMember[];
  vipBenefits: VipBenefit[];
  walletTabs: {
    recharge: TransactionRecord[]; withdraw: TransactionRecord[];
    transfer: TransactionRecord[]; payment: TransactionRecord[];
    flow: TransactionRecord[]; invite: TransactionRecord[];
  };
  isLoggedIn: boolean;
}

const defaultState: State = {
  user: { name: '张三', vipLevel: 'VIP3', inviteCode: 'ABC123', kycStatus: '已认证' },
  wallet: { balance: 1250.50, frozen: 200.00 },
  tasks: {
    pending: [
      { id: 1, title: '新手任务', description: '完成首次投资', reward: 50, status: 'pending' },
      { id: 2, title: '每日签到', description: '连续签到7天', reward: 20, status: 'pending' },
      { id: 3, title: '邀请好友', description: '邀请1位好友注册', reward: 100, status: 'pending' }
    ],
    inProgress: [
      { id: 4, title: '投资达标', description: '累计投资达到1000元', reward: 0, status: 'in-progress' },
      { id: 5, title: '活跃任务', description: '连续登录7天', reward: 0, status: 'in-progress' }
    ],
    completed: [{ id: 6, title: '注册任务', description: '完成实名认证', reward: 10, status: 'completed' }]
  },
  investments: {
    available: [
      { id: 1, name: '稳健理财A', description: '年化收益率8%', rate: 8.0, amount: 0, expectedReturn: 0, status: 'available' },
      { id: 2, name: '基金定投B', description: '月定投计划', rate: 12.5, amount: 0, expectedReturn: 0, status: 'available' },
      { id: 3, name: '股票组合C', description: '专业团队管理', rate: 15.2, amount: 0, expectedReturn: 0, status: 'available' }
    ],
    participated: [
      { id: 4, name: '稳健理财A', description: '年化收益率8%', rate: 8.0, amount: 5000, expectedReturn: 400, status: 'participated' },
      { id: 5, name: '基金定投B', description: '月定投计划', rate: 12.5, amount: 2000, expectedReturn: 250, status: 'participated' }
    ],
    history: [
      { id: 6, name: '短期理财X', description: '短期高收益', rate: 0, amount: 3000, expectedReturn: 0, status: 'history' },
      { id: 7, name: '债券投资Y', description: '稳健型投资', rate: 0, amount: 1000, expectedReturn: 0, status: 'history' }
    ]
  },
  teamMembers: [
    { id: 1, name: '李四', vipLevel: 'VIP1', contribution: 1200 },
    { id: 2, name: '王五', vipLevel: 'VIP2', contribution: 2500 },
    { id: 3, name: '赵六', vipLevel: 'VIP1', contribution: 800 }
  ],
  vipBenefits: [
    { id: 1, title: '高收益项目', description: '享受更高收益率的投资项目', owned: true },
    { id: 2, title: '专属客服', description: '24小时专属客户服务', owned: true },
    { id: 3, title: '手续费减免', description: '交易手续费减免50%', owned: false },
    { id: 4, title: '优先投资权', description: '新项目优先投资权', owned: false }
  ],
  walletTabs: {
    recharge: [{ id: 1, type: '充值', amount: 500, time: '2023-06-15 10:30', status: '成功' }],
    withdraw: [{ id: 1, type: '提现', amount: 200, time: '2023-06-10 14:15', status: '审核中' }],
    transfer: [{ id: 1, type: '转账', amount: 100, time: '2023-06-12 11:45', status: '成功', to: '李四' }],
    payment: [{ id: 1, type: '支付', amount: 50, time: '2023-06-13 20:10', status: '成功', merchant: '商城购物' }],
    flow: [
      { id: 1, type: '收入', amount: 200, time: '2023-06-15 10:30', status: '' },
      { id: 2, type: '支出', amount: 50, time: '2023-06-14 15:22', status: '' }
    ],
    invite: [{ id: 1, type: '邀请收益', amount: 50, time: '2023-06-10', status: '', user: '李四' }]
  },
  isLoggedIn: false
};

const saved = StorageUtil.getItem<State>('appState');
const state = reactive<State>(saved || defaultState);

// 深度监听自动持久化
watch(state, (val) => StorageUtil.setItem('appState', val), { deep: true });

export const store = {
  state: readonly(state),
  setUser(user: User) { state.user = user; },
  setWallet(wallet: Wallet) { state.wallet = wallet; },
  setLoggedIn(status: boolean) { state.isLoggedIn = status; },
  completeTask(taskId: number) {
    const idx = state.tasks.pending.findIndex(t => t.id === taskId);
    if (idx !== -1) {
      const task = state.tasks.pending.splice(idx, 1)[0];
      task.status = 'completed';
      state.tasks.completed.push(task);
    }
  },
  addToInvestment(project: Investment) {
    state.investments.available = state.investments.available.filter(p => p.id !== project.id);
    state.investments.participated.push({ ...project, status: 'participated' });
  }
};
