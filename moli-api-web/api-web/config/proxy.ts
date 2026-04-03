/**
 * @name 代理配置（统一后端地址入口）
 * @doc https://umijs.org/docs/guides/proxy
 */
const API_TARGET = process.env.API_TARGET || 'http://localhost:8101';

const commonProxy = {
  '/api/': {
    target: API_TARGET,
    changeOrigin: true,
    pathRewrite: { '^': '' },
  },
};

export default {
  dev: commonProxy,
  test: commonProxy,
  pre: commonProxy,
};
