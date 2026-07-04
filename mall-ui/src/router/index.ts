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
    path: '/tasklist',
    name: 'TaskListAlias',
    component: () => import('@/views/Tasks.vue'),
    meta: { title: '任务列表', requiresAuth: true },
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
    path: '/investProductDetail',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '投资产品详情',
      requiresAuth: true,
      icon: 'chart-trending-o',
      description: '参考站投资产品详情入口已接入；本地当前只有投资项目列表，详情和购买/持仓链路待后端补齐。',
      dataScope: '不伪造项目详情、购买订单、持仓或派息数据。',
      apiState: '待新增投资项目详情 open API；如涉及资金锁定和收益派发，需要独立投资状态机。',
      relatedLinks: [{ label: '返回投资专区', path: '/investment', value: '项目列表' }],
    },
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
    path: '/profit',
    name: 'Profit',
    component: () => import('@/views/account/Profit.vue'),
    meta: { title: '收益汇总', requiresAuth: true },
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
    name: 'Coupon',
    component: () => import('@/views/Coupon.vue'),
    meta: { title: '优惠券', requiresAuth: true },
  },
  {
    path: '/coupon/logs',
    redirect: '/coupon?tab=records',
  },
  {
    path: '/sign',
    name: 'CheckIn',
    component: () => import('@/views/CheckIn.vue'),
    meta: { title: '签到', requiresAuth: true },
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
    meta: {
      title: '帮助中心',
      requiresAuth: false,
      contentType: 'PLATFORM_PROFILE',
      icon: 'question-o',
      dataScope: '当前复用后台平台介绍内容；独立帮助分类待内容类型扩展。',
      apiState: '/api/open/content/list?type=PLATFORM_PROFILE',
    },
  },
  {
    path: '/guides',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: {
      title: '新手指南',
      requiresAuth: false,
      contentType: 'PLATFORM_PROFILE',
      icon: 'guide-o',
      dataScope: '当前复用后台平台介绍内容；独立指南分类待内容类型扩展。',
      apiState: '/api/open/content/list?type=PLATFORM_PROFILE',
    },
  },
  {
    path: '/article',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: {
      title: '文章',
      requiresAuth: false,
      icon: 'notes-o',
      description: '文章入口已接入；文章列表/详情内容类型和 open API 待后端补齐。',
      dataScope: '不写死文章正文，不伪造发布时间或阅读量。',
      apiState: '待扩展内容类型 ARTICLE 或等价内容域接口。',
    },
  },
  {
    path: '/notice',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: { title: '公告', requiresAuth: false, contentType: 'NOTICE', icon: 'volume-o' },
  },
  {
    path: '/news',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: { title: '新闻', requiresAuth: false, icon: 'newspaper-o' },
  },
  {
    path: '/company',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: {
      title: '公司介绍',
      requiresAuth: false,
      contentType: 'COMPANY_PROFILE',
      icon: 'shop-o',
      apiState: '/api/open/content/list?type=COMPANY_PROFILE',
    },
  },
  {
    path: '/privacyPolicy',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: {
      title: '隐私政策',
      requiresAuth: false,
      contentType: 'USER_PRIVACY',
      icon: 'shield-o',
      apiState: '/api/open/content/list?type=USER_PRIVACY',
    },
  },
  {
    path: '/termOfUse',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: {
      title: '用户协议',
      requiresAuth: false,
      contentType: 'USER_AGREEMENT',
      icon: 'orders-o',
      dataScope: '读取后台 USER_AGREEMENT 内容配置，不写死协议正文。',
      apiState: '/api/open/content/list?type=USER_AGREEMENT',
    },
  },
  {
    path: '/pfrules',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: {
      title: '平台规则',
      requiresAuth: false,
      contentType: 'REGULATOR',
      icon: 'description-o',
      apiState: '/api/open/content/list?type=REGULATOR',
    },
  },
  {
    path: '/whitepapers',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: {
      title: '白皮书',
      requiresAuth: false,
      icon: 'description-o',
      description: '白皮书入口已接入；文件资料表或内容类型待后端补齐。',
      dataScope: '不伪造白皮书文件、版本或下载地址。',
      apiState: '待新增文件资料 open API 或扩展内容类型 WHITEPAPER。',
    },
  },
  {
    path: '/cpfiles',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: {
      title: '文件资料',
      requiresAuth: false,
      icon: 'folder-o',
      description: '文件资料入口已接入；资料列表、下载地址和权限规则待后端补齐。',
      dataScope: '不伪造资料文件或下载链接。',
      apiState: '待新增文件资料表和只读 open API。',
    },
  },
  {
    path: '/service',
    component: () => import('@/views/account/ContentPage.vue'),
    meta: {
      title: '客服',
      requiresAuth: false,
      icon: 'service-o',
      contentMode: 'service',
      description: '客服入口已接入，只读取后台公开客服配置。',
      dataScope: '不写死外部联系方式，不暴露机器人 token。',
      apiState: '/api/open/content/service',
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
    path: '/shareTask',
    component: () => import('@/views/Tasks.vue'),
    meta: {
      title: '分享任务',
      requiresAuth: true,
      taskType: 'SHARE',
      defaultStatus: 'available',
    },
  },
  {
    path: '/shareTaskList',
    component: () => import('@/views/Tasks.vue'),
    meta: {
      title: '分享任务记录',
      requiresAuth: true,
      taskType: 'SHARE',
      defaultStatus: 'completed',
    },
  },
  {
    path: '/friendsCircle',
    component: () => import('@/views/Tasks.vue'),
    meta: {
      title: '分享任务',
      requiresAuth: true,
      taskType: 'SHARE',
      defaultStatus: 'available',
    },
  },
  {
    path: '/vatask',
    component: () => import('@/views/Tasks.vue'),
    meta: {
      title: 'VA 任务',
      requiresAuth: true,
      taskType: 'VA',
      defaultStatus: 'available',
    },
  },
  {
    path: '/videoTask',
    component: () => import('@/views/Tasks.vue'),
    meta: {
      title: '视频任务',
      requiresAuth: true,
      taskType: 'VIDEO',
      defaultStatus: 'available',
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
    path: '/union',
    component: () => import('@/views/account/AccountStatus.vue'),
    meta: {
      title: '联盟代理',
      requiresAuth: true,
      icon: 'cluster-o',
      description: '联盟代理入口已接入；代理职位、薪资、领取状态和晋级规则待后端补齐。',
      dataScope: '不伪造代理等级、薪资或领取记录。',
      apiState: '待新增 user/agent 域 open API。',
      relatedLinks: [{ label: '我的团队', path: '/team', value: '已接入' }],
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
    redirect: '/termOfUse',
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
    meta: { title: '排行榜' },
  },
  {
    path: '/rank',
    name: 'Rank',
    component: () => import('@/views/Leaderboard.vue'),
    meta: { title: '排行榜' },
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/ErrorPage.vue'),
    meta: {
      title: '无权限访问',
      statusCode: 403,
      requiresAuth: false,
      description: '当前账号没有访问该页面的权限，或参考站入口暂未对本地用户开放。',
    },
  },
  {
    path: '/:error(.*)',
    name: 'NotFound',
    component: () => import('@/views/ErrorPage.vue'),
    meta: {
      title: '页面不存在',
      statusCode: 404,
      requiresAuth: false,
      description: '当前访问的页面不存在，或该参考站入口还在后续功能矩阵中。',
    },
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
