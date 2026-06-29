// 需要放到 localStorage 的数据操作
export const getItem = (name: string): string => {
  return window.localStorage.getItem(name) as string;
};

export const setItemJson = (name: string, value: object): void => {
  window.localStorage.setItem(name, JSON.stringify(value));
};

export const getItemJson = (name: string): any => {
  const data = window.localStorage.getItem(name);
  if (data != null) {
    return JSON.parse(data);
  }
  return data;
};

export const setItem = (name: string, value: string): void => {
  window.localStorage.setItem(name, value);
};

export const removeItem = (name: string): void => {
  window.localStorage.removeItem(name);
};

export const isItem = (name: string) => {
  return localStorage.getItem(name);
};
