/**
 * @name 代理配置（统一后端地址入口）
 * @doc https://umijs.org/docs/guides/proxy
 */
const DEFAULT_API_TARGET = 'https://api-backend.moka123.cn';
const rawTarget = (process.env.API_TARGET || '').trim();
const normalizedTarget = !rawTarget
  ? DEFAULT_API_TARGET
  : /^https?:\/\//i.test(rawTarget)
    ? rawTarget
    : `http://${rawTarget}`;

const commonProxy = {
  '/api/': {
    target: normalizedTarget,
    changeOrigin: true,
    pathRewrite: { '^': '' },
  },
};

export default {
  dev: commonProxy,
  test: commonProxy,
  pre: commonProxy,
};
