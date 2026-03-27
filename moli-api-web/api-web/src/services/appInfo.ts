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
