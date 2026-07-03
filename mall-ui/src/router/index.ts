import { createRouter, createWebHashHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { store } from '@/store';
import { tokenStore } from '@/api/http';
import { showDialog } from 'vant';

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/home' },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页', requiresAuth: false },
  },
  {
    path: '/tasks',
    name: 'Tasks',
    component: () => import('@/views/Tasks.vue'),
    meta: { title: '任务中心', requiresAuth: true },
  },
  {
    path: '/investment',
    name: 'Investment',
    component: () => import('@/views/Investment.vue'),
    meta: { title: '投资项目', requiresAuth: false },
  },
  {
    path: '/lottery',
    name: 'Lottery',
    component: () => import('@/views/Lottery.vue'),
    meta: { title: '抽奖活动', requiresAuth: true },
  },
  {
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/Wallet.vue'),
    meta: { title: '我的钱包', requiresAuth: true },
  },
  {
    path: '/wallet/deposit',
    name: 'WalletDeposit',
    component: () => import('@/views/wallet/Deposit.vue'),
    meta: { title: '充值', requiresAuth: true },
  },
  {
    path: '/wallet/withdraw',
    name: 'WalletWithdraw',
    component: () => import('@/views/wallet/Withdraw.vue'),
    meta: { title: '提现', requiresAuth: true },
  },
  {
    path: '/wallet/transfer',
    name: 'WalletTransfer',
    component: () => import('@/views/wallet/Transfer.vue'),
    meta: { title: '站内转账', requiresAuth: true },
  },
  {
    path: '/account',
    name: 'Account',
    component: () => import('@/views/account/AccountHub.vue'),
    meta: { title: '账户中心', requiresAuth: true },
  },
  {
    path: '/account/balance',
    redirect: '/wallet?tab=flow',
  },
  {
    path: '/account/recharge',
    redirect: '/wallet/deposit',
  },
  {
    path: '/account/withDraw',
    redirect: '/wallet/withdraw',
  },
  {
    path: '/account/transfer',
    redirect: '/wallet/transfer',
  },
  {
    path: '/account/person',
    redirect: '/profile',
  },
  {
    path: '/mine',
    redirect: '/profile',
  },
  {
    path: '/mission',
    redirect: '/tasks',
  },
  {
    path: '/invest',
    redirect: '/investment',
  },
  {
    path: '/investZone',
    redirect: '/investment',
  },
  {
    path: '/invest/note',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '投资记录',
      requiresAuth: true,
      icon: 'records-o',
      description: '投资记录入口已接入；本地暂不建立独立投资订单状态机。',
      dataScope: '如涉及资金变化，应落在任务记录或钱包流水。',
      apiState: '待明确参考站投资记录与任务记录的映射后再接接口。',
      relatedLinks: [{ label: '查看任务记录', path: '/tasks', value: '任务中心' }],
    },
  },
  {
    path: '/invest/:id',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '项目详情',
      requiresAuth: true,
      icon: 'chart-trending-o',
      description: '投资详情入口已接入；本地项目仍按任务中心商品/计划展示，不新增独立投资资金链路。',
      dataScope: '项目列表来自任务域 invest/projects；详情接口待后端补齐。',
      apiState: '待新增项目详情开放接口。',
      relatedLinks: [{ label: '返回投资项目', path: '/investment', value: '任务商品口径' }],
    },
  },
  {
    path: '/account/:feature(changePwd|financial|googleAuth|kyc|select|vipUplog)',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '账户功能状态',
      requiresAuth: true,
      icon: 'info-o',
      description: '该账户子页面入口已接入，独立业务接口待后端补齐。',
      dataScope: '只展示明确状态，不提交资料修改、不伪造认证、资金资料或 VIP 订单数据。',
      apiState: '待新增对应 open API。',
      relatedLinks: [
        { label: '账户中心', path: '/account', value: '返回' },
        { label: '钱包账变', path: '/wallet?tab=flow', value: '已接入' },
      ],
    },
  },
  {
    path: '/invite',
    name: 'Invite',
    component: () => import('@/views/account/Invite.vue'),
    meta: { title: '邀请好友', requiresAuth: true },
  },
  {
    path: '/income',
    name: 'Income',
    component: () => import('@/views/account/Income.vue'),
    meta: { title: '收益', requiresAuth: true },
  },
  {
    path: '/vip',
    redirect: '/profile',
  },
  {
    path: '/vipDetail',
    redirect: '/profile',
  },
  {
    path: '/coupon',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '优惠券',
      requiresAuth: true,
      icon: 'coupon-o',
      description: '优惠券入口已接入；本地尚无优惠券列表、领取或核销接口。',
      dataScope: '不伪造优惠券余额、可用券或使用记录。',
      apiState: '待新增优惠券域接口。',
    },
  },
  {
    path: '/coupon/logs',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '优惠券记录',
      requiresAuth: true,
      icon: 'records-o',
      description: '优惠券记录入口已接入；记录接口待开放。',
      dataScope: '不展示模拟领取/使用记录。',
      apiState: '待新增优惠券记录接口。',
      relatedLinks: [{ label: '优惠券', path: '/coupon', value: '返回' }],
    },
  },
  {
    path: '/findpwd',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '找回密码',
      requiresAuth: false,
      icon: 'lock',
      description: '找回密码入口已接入；邮件验证码或安全验证接口尚未实现。',
      dataScope: '不在前端本地重置密码。',
      apiState: '待新增找回密码接口。',
      relatedLinks: [{ label: '返回登录', path: '/login', value: '登录' }],
    },
  },
  {
    path: '/auth/forgot-pwd',
    redirect: '/findpwd',
  },
  {
    path: '/help',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: { title: '帮助中心', requiresAuth: false, icon: 'question-o' },
  },
  {
    path: '/guides',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: { title: '新手指南', requiresAuth: false, icon: 'guide-o' },
  },
  {
    path: '/notice',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: { title: '公告', requiresAuth: false, contentType: 'notice', icon: 'volume-o' },
  },
  {
    path: '/news',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: { title: '新闻', requiresAuth: false, icon: 'newspaper-o' },
  },
  {
    path: '/company',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: { title: '公司介绍', requiresAuth: false, icon: 'shop-o' },
  },
  {
    path: '/privacyPolicy',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: { title: '隐私政策', requiresAuth: false, icon: 'shield-o' },
  },
  {
    path: '/pfrules',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: { title: '平台规则', requiresAuth: false, icon: 'description-o' },
  },
  {
    path: '/service',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: {
      title: '客服',
      requiresAuth: false,
      icon: 'service-o',
      description: '客服入口已接入；客服联系方式和 Telegram/在线客服配置接口待开放。',
      dataScope: '不写死外部联系方式，等待后台配置。',
    },
  },
  {
    path: '/activity',
    redirect: '/lottery',
  },
  {
    path: '/activity/:id',
    redirect: '/lottery',
  },
  {
    path: '/friendsCircle',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '分享任务',
      requiresAuth: true,
      icon: 'share-o',
      description: '分享任务入口已接入；提交/审核链路待 mission 分享任务接口闭环。',
      dataScope: '不伪造分享素材、提交状态或审核结果。',
      apiState: '待新增分享任务列表、详情、提交与审核接口。',
      relatedLinks: [{ label: '任务中心', path: '/tasks', value: '返回' }],
    },
  },
  {
    path: '/marketing',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '营销推广',
      requiresAuth: true,
      icon: 'bullhorn-o',
      description: '营销推广入口已接入；参考站具体数据结构待短会话校准。',
      dataScope: '当前不伪造推广活动或奖励。',
      apiState: '待校准后落入 promotion 或 content 域。',
    },
  },
  {
    path: '/agent/position',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '代理职位',
      requiresAuth: true,
      icon: 'medal-o',
      description: '代理职位入口已接入；职位列表、我的职位和领取薪资接口待开放。',
      dataScope: '不伪造职位、薪资或领取状态。',
      apiState: '待新增 agent position 接口。',
    },
  },
  {
    path: '/team/:id',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '下级详情',
      requiresAuth: true,
      icon: 'friends-o',
      description: '团队下级详情入口已接入；当前后端只提供直属成员列表。',
      dataScope: '不越权查询下级详情，不伪造团队层级。',
      apiState: '待新增下级详情与层级统计接口。',
      relatedLinks: [{ label: '我的团队', path: '/team', value: '返回' }],
    },
  },
  {
    path: '/team',
    name: 'team',
    component: () => import('@/views/Team.vue'),
    meta: { title: '我的团队', requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '注册', requiresAuth: false, defaultTab: 'register' },
  },
  {
    path: '/terms',
    redirect: '/pfrules',
  },
  {
    path: '/privacy',
    redirect: '/privacyPolicy',
  },
  {
    path: '/admin-login',
    name: 'AdminLogin',
    component: () => import('@/views/auth/AdminLogin.vue'),
    meta: { title: '后台模拟登录', requiresAuth: false },
  },
  {
    path: '/leaderboard',
    name: 'Leaderboard',
    component: () => import('@/views/Leaderboard.vue'),
    meta: { title: 'Leaderboard' },
  },
]

const router = createRouter({
  // 使用 Hash 模式，部署无需服务器配置
  history: createWebHashHistory(),
  routes
});

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '移动端H5';
  if (to.meta.requiresAuth && !store.state.isLoggedIn && !tokenStore.get()) {
    showDialog({
      title: '提示',
      message: '请先登录后再访问该页面'
    }).then(() => {
      next({ name: 'Login', query: { redirect: to.fullPath } });
    });
    return;
  }
  next();
});

export default router;
