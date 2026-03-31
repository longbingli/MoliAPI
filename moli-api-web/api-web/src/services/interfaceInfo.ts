import { request } from '@umijs/max';

/** 分页获取接口列表 POST /interfaceInfo/list/page/vo */
export async function listInterfaceInfoByPage(
  body: API.InterfaceInfoQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<API.PageInterfaceInfoVO>>(
    '/interfaceInfo/list/page/vo',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      data: body,
      ...(options || {}),
    },
  );
}

/** 根据 id 获取接口详情 GET /interfaceInfo/get/vo */
export async function getInterfaceInfoById(
  params: { id: number | string },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<API.InterfaceInfoVO>>('/interfaceInfo/get/vo', {
    method: 'GET',
    params,
    ...(options || {}),
  });
}

/** 创建接口 POST /interfaceInfo/add */
export async function addInterfaceInfo(
  body: API.InterfaceInfoAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<number>>('/interfaceInfo/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 编辑接口 POST /interfaceInfo/edit */
export async function editInterfaceInfo(
  body: API.InterfaceInfoEditRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<boolean>>('/interfaceInfo/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除接口 POST /interfaceInfo/delete */
export async function deleteInterfaceInfo(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponse<boolean>>('/interfaceInfo/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
