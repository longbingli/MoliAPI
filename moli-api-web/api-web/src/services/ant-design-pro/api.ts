// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

const SUCCESS_CODE = 0;

/** 获取当前登录用户 GET /user/get/login */
export async function currentUser(options?: { [key: string]: any }) {
  const response = await request<API.BaseResponse<API.LoginUserVO>>(
    '/user/get/login',
    {
      method: 'GET',
      ...(options || {}),
    },
  );

  if (response.code !== SUCCESS_CODE || !response.data) {
    throw new Error(response.message || '获取当前登录用户失败');
  }

  return {
    data: {
      ...response.data,
      name: response.data.userName,
      avatar: response.data.userAvatar,
      access: response.data.userRole,
    } as API.CurrentUser,
  };
}

/** 退出登录 POST /user/logout */
export async function outLogin(options?: { [key: string]: any }) {
  return request<API.BaseResponse<boolean>>('/user/logout', {
    method: 'POST',
    ...(options || {}),
  });
}

/** 登录接口 POST /user/login */
export async function login(
  body: API.UserLoginRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<API.LoginUserVO>>('/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 注册接口 POST /user/register */
export async function register(
  body: API.UserRegisterRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<number>>('/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 更新当前用户信息 POST /user/update/my */
export async function updateMyUser(
  body: API.UserUpdateMyRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<boolean>>('/user/update/my', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
