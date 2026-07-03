import { mergeConfig } from 'vite';
import baseConfig from './vite.config.base';

export default mergeConfig(
  {
    mode: 'development',
    server: {
      open: true,
      fs: {
        strict: true,
      },
      // port: 3000,
    },
  },
  baseConfig
);
