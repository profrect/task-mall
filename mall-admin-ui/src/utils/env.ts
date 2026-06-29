const debug = import.meta.env.MODE !== 'production';

// 当前环境是否为安全环境
export const isSecureEnvironment = (() => {
  return (
    window.location.protocol === 'https:' ||
    window.location.hostname === 'localhost'
  );
})();

// 当前是否为单应用模式 PWA
export const isStandaloneMode = (() =>
  (window.matchMedia('(display-mode: standalone)').matches ||
    (window.navigator as any).standalone ||
    document.referrer.includes('android-app://')) === true)();

// http base url
export const httpBaseUrl = (() => {
  const configBase = import.meta.env.VITE_API_BASE_URL || '';
  if (configBase.startsWith('http') || configBase.startsWith('https')) {
    // 固定
    return configBase;
  }
  // 动态
  const { protocol } = window.location;
  const { host } = window.location;
  return `${protocol}//${host}${configBase}`;
})();

export const appName = (() => {
  return import.meta.env.VITE_APP_NAME || 'Task-Mall-Admin';
})();

export const appVersion = (() => {
  return import.meta.env.VITE_APP_VERSION || '1.0';
})();

export default debug;
