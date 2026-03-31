import { request } from '@umijs/max';

/** 分页获取应用列表 POST /appInfo/list/page/vo */
export async function listAppInfoByPage(
  body: API.AppInfoQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<API.PageAppInfoVO>>('/appInfo/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 分页获取当前登录用户创建的应用列表 POST /appInfo/my/list/page/vo */
export async function listMyAppInfoByPage(
  body: API.AppInfoQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<API.PageAppInfoVO>>('/appInfo/my/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 创建应用 POST /appInfo/add */
export async function addAppInfo(
  body: API.AppInfoAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<number>>('/appInfo/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 编辑应用 POST /appInfo/edit */
export async function editAppInfo(
  body: API.AppInfoEditRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<boolean>>('/appInfo/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除应用 POST /appInfo/delete */
export async function deleteAppInfo(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<boolean>>('/appInfo/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
