export default [
  {
    path: '/user',
    layout: false,
    routes: [
      {
        name: 'login',
        path: '/user/login',
        component: './user/login',
      },
    ],
  },
  {
    path: '/home',
    name: 'home',
    icon: 'home',
    component: './Home',
  },
  {
    path: '/account/center',
    name: '个人中心',
    icon: 'user',
    component: './AccountCenter',
  },
  {
    path: '/account/token',
    name: '令牌管理',
    icon: 'key',
    component: './TokenManage',
  },
  {
    path: '/developer/docs',
    component: './DeveloperDocs',
  },
  {
    path: '/manage/apis',
    name: '接口管理',
    icon: 'appstore',
    component: './ApiManage',
  },
  {
    path: '/manage/apis/:appId',
    hideInMenu: true,
    component: './ApiManage/InterfaceManage',
  },
  {
    path: '/apps/:appId/interfaces',
    component: './InterfaceList',
  },
  {
    path: '/apps/:appId/interfaces/:interfaceId',
    component: './InterfaceDetail',
  },
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '*',
    layout: false,
    component: './404',
  },
];
