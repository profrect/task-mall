import type { RouteRecordRaw } from 'vue-router';

export interface AdminMenuModule {
  key: string;
  label: string;
  rootNames: string[];
}

export const ADMIN_MENU_MODULES: AdminMenuModule[] = [
  {
    key: 'system',
    label: '系统管理',
    rootNames: [
      'dashboard',
      'sys-permission',
      'sys-setting',
      'i18n-lang',
      'content-setting',
      'sys-log',
    ],
  },
  {
    key: 'member',
    label: '会员管理',
    rootNames: ['user-member'],
  },
  {
    key: 'bill',
    label: '账单管理',
    rootNames: ['order', 'mission', 'promotion', 'leaderboard'],
  },
  {
    key: 'report',
    label: '报表管理',
    rootNames: ['report'],
  },
];

const routeName = (route: RouteRecordRaw) => String(route.name || '');

const routeContainsName = (route: RouteRecordRaw, name: string): boolean => {
  if (routeName(route) === name) return true;
  return Boolean(
    route.children?.some((child) => routeContainsName(child, name))
  );
};

export const resolveMenuModuleKey = (
  menuTree: RouteRecordRaw[],
  currentRouteName?: string
) => {
  if (currentRouteName) {
    const matched = ADMIN_MENU_MODULES.find((module) =>
      module.rootNames.some((rootName) => {
        const root = menuTree.find((item) => routeName(item) === rootName);
        return root ? routeContainsName(root, currentRouteName) : false;
      })
    );
    if (matched) return matched.key;
  }

  const firstAvailable = ADMIN_MENU_MODULES.find((module) =>
    module.rootNames.some((rootName) =>
      menuTree.some((item) => routeName(item) === rootName)
    )
  );

  return firstAvailable?.key || '';
};

export const getMenuModuleRoutes = (
  menuTree: RouteRecordRaw[],
  moduleKey: string
) => {
  const moduleConfig = ADMIN_MENU_MODULES.find(
    (item) => item.key === moduleKey
  );
  if (!moduleConfig) return menuTree;

  const roots = moduleConfig.rootNames
    .map((rootName) => menuTree.find((item) => routeName(item) === rootName))
    .filter(Boolean) as RouteRecordRaw[];

  if (roots.length === 1) {
    const [root] = roots;
    return root.children?.length ? root.children : roots;
  }

  return roots;
};

export const findFirstNavigableRoute = (
  routes: RouteRecordRaw[]
): RouteRecordRaw | undefined =>
  routes.reduce<RouteRecordRaw | undefined>((matched, route) => {
    if (matched) return matched;

    const visible = route.meta?.hideInMenu !== true;
    if (visible && !route.children?.length) return route;

    const child = findFirstNavigableRoute(route.children || []);
    if (child) return child;

    return visible ? route : undefined;
  }, undefined);
