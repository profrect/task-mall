import localeLogin from '@/views/login/locale/en-US';

import localeAllGame from '@/views/dashboard/locale/en-US';

import localeUserControl from '@/views/config/user-control/locale/en-US';
import localeRoleControl from '@/views/config/role-control/locale/en-US';
import localeSystemRouterConfig from '@/views/config/system-router-config/locale/en-US';
import localeServerApiConfig from '@/views/config/server-api-config/locale/en-US';
import localeUserLog from '@/views/log/user-log/locale/en-US';
import localeUserMember from '@/views/user-member/members/locale/zh-CN';

import { appName, appVersion } from '@/utils/env';

import localeSettings from './en-US/settings';

export default {
  'title.app-name': `${appName} - 管理平台`,
  'title.footer-name': `${appName} - 管理平台 v${appVersion}`,
  'menu.dashboard': 'Dashboard',
  'game-page': 'Dashboard',
  'menu.config': 'System Config',
  'menu.log': 'Operation Log',
  ...localeAllGame,

  ...localeUserControl,
  ...localeRoleControl,
  ...localeSystemRouterConfig,
  ...localeServerApiConfig,
  ...localeUserLog,

  /** 其他  */
  'form.search': 'Search',
  'form.reset': 'Reset',
  'form.add': 'Add',
  'columns.operations': 'operations',
  'menu.result': 'Result',
  'menu.user': 'User Center',
  'navbar.action.locale': 'Switch to English',
  'delete.confirm.content': 'Are you sure you want to delete?',
  'user.back-to-home': 'Back to home',
  'user.official-page': 'Official website',
  'user.userCenter': 'User info',
  'user.change-password': 'Change password',
  'user.logout': 'Logout',
  'user-info.name': 'Name',
  'user-info.realName': 'Real Name',
  'user-info.role': 'Roles',
  'user-info.email': 'Email',
  ...localeSettings,
  ...localeLogin,
  ...localeUserMember
};
