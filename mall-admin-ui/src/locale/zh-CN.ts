import localeLogin from '@/views/login/locale/zh-CN';

import localeAllGame from '@/views/dashboard/locale/zh-CN';

import localeUserControl from '@/views/config/user-control/locale/zh-CN';
import localeRoleControl from '@/views/config/role-control/locale/zh-CN';
import localeSystemRouterConfig from '@/views/config/system-router-config/locale/zh-CN';
import localeServerApiConfig from '@/views/config/server-api-config/locale/zh-CN';
import localeUserLog from '@/views/log/user-log/locale/zh-CN';
import localeUserMember from '@/views/user-member/members/locale/zh-CN';

import localePromotion from '@/views/promotion/lucky-draw/locale/zh-CN';
import localeMission from '@/views/mission/mission-list/locale/zh-CN';
import localeReport from '@/views/report/summary/locale/zh-CN';
import localeSystemSetting from '@/views/sys-setting/sys-param/locale/zh-CN';


import localeMissionPlan from '@/views/mission/plan/locale/zh-CN';
import localeMissionApproval from '@/views/mission/approval/locale/zh-CN';

import localeDepositOrder from '@/views/order/deposit/locale/zh-CN';
import localeWithdrawOrder from '@/views/order/withdraw/locale/zh-CN';
import localePaymentOrder from '@/views/order/payment/locale/zh-CN';
import localeTransferOrder from '@/views/order/transfer/locale/zh-CN';
import localeConsolidationOrder from '@/views/order/consolidation/locale/zh-CN';
import localeOrderApproval from '@/views/order/approval/locale/zh-CN';
import localeOrderLog from '@/views/order/log/locale/zh-CN';

import localePrizeConfig from '@/views/promotion/prize-config/locale/zh-CN';
import localeLeaderboard from '@/views/leaderboard/locale/zh-CN';
import localeLangConfig from '@/views/i18n-lang/lang-config/locale/zh-CN';



import localeCompanyProfile from '@/views/content-setting/company-profile/locale/zh-CN';
import localePlatformProfile from '@/views/content-setting/platform-profile/locale/zh-CN';
import localeRegulator from '@/views/content-setting/regulator/locale/zh-CN';
import localeNotices from '@/views/content-setting/notices/locale/zh-CN';
import localeUserAgreement from '@/views/content-setting/user-agreement/locale/zh-CN';
import localeUserPrivacy from '@/views/content-setting/user-privacy/locale/zh-CN';
import localeSysOperationLog from '@/views/sys-log/operation/locale/zh-CN';

import { appName, appVersion } from '@/utils/env';
import localeSettings from './zh-CN/settings';

export default {
  /** 系统 */
  'title.app-name': `${appName} - 管理平台`,
  'title.footer-name': `${appName} - 管理平台 v${appVersion}`,
  'menu.dashboard': '首页',
  'game-page': '首页',
  'menu.config': '系统管理',
  'menu.log': '操作日志',
  ...localeAllGame,

  ...localeUserControl,
  ...localeRoleControl,
  ...localeSystemRouterConfig,
  ...localeServerApiConfig,
  ...localeUserLog,

  /** 其他  */
  'form.search': '查询',
  'form.reset': '重置',
  'form.add': '添加',
  'columns.operations': '操作',
  'menu.result': '结果页',
  'menu.exception': '异常页',
  'menu.user': '个人中心',
  'navbar.action.locale': '切换为中文',
  'delete.confirm.content': '确定要删除该条数据吗？',
  'user.back-to-home': '返回首页',
  'user.official-page': '九鱼乐官网',
  'user.userCenter': '用户信息',
  'user.change-password': '修改密码',
  'user.logout': '登出登录',
  'user-info.name': '用户名',
  'user-info.realName': '姓名',
  'user-info.role': '用户角色',
  'user-info.email': '绑定邮箱',
  ...localeSettings,
  ...localeLogin,
  ...localeUserMember,
  ...localePromotion,
  ...localeMission,
  ...localeReport,
  ...localeSystemSetting,


  ...localeMissionPlan,
  ...localeMissionApproval,

  ...localeDepositOrder,
  ...localeWithdrawOrder,
  ...localePaymentOrder,
  ...localeTransferOrder,
  ...localeConsolidationOrder,
  ...localeOrderApproval,
  ...localeOrderLog,
  ...localePrizeConfig,
  ...localeLeaderboard,
  ...localeLangConfig,

  ...localeCompanyProfile,
  ...localePlatformProfile,
  ...localeRegulator,
  ...localeNotices,
  ...localeUserAgreement,
  ...localeUserPrivacy,
  ...localeSysOperationLog,
};
