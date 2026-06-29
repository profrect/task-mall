// eslint-disable-next-line import/prefer-default-export
export const getRequestParamDeal = (
  url: string,
  param?: Map<string, string>
) => {
  if (param) {
    url += '?';

    param.forEach((v, key) => {
      if (v || `${v}` === '0') {
        url += `${key}=${v}&`;
      }
    });
  }
  return url;
};
