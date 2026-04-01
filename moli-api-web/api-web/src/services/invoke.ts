import { request } from '@umijs/max';

export type InvokeForwardRequest = {
  method: string;
  path: string;
  queryParams?: Record<string, any>;
  headers?: Record<string, string>;
  body?: string;
};

/** 通用在线调试转发 POST /invoke/forward */
export async function forwardInvoke(body: InvokeForwardRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponse<{
    statusCode?: number;
    durationMs?: number;
    body?: string;
    headers?: Record<string, string[]>;
  }>>('/invoke/forward', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
