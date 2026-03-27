import type { RequestOptions } from '@@/plugin-request/request';
import type { RequestConfig } from '@umijs/max';
import { message, notification } from 'antd';

enum ErrorShowType {
  SILENT = 0,
  WARN_MESSAGE = 1,
  ERROR_MESSAGE = 2,
  NOTIFICATION = 3,
  REDIRECT = 9,
}

interface ResponseStructure {
  code?: number;
  data?: any;
  message?: string;
  success?: boolean;
  errorCode?: number;
  errorMessage?: string;
  showType?: ErrorShowType;
}

const SUCCESS_CODE = 0;

const getErrorPayload = (res: ResponseStructure) => {
  if (typeof res.code === 'number' && res.code !== SUCCESS_CODE) {
    return {
      errorCode: res.code,
      errorMessage: res.message || '请求失败',
      showType: ErrorShowType.ERROR_MESSAGE,
      data: res.data,
    };
  }

  if (res.success === false) {
    return {
      errorCode: res.errorCode,
      errorMessage: res.errorMessage || res.message || '请求失败',
      showType: res.showType ?? ErrorShowType.ERROR_MESSAGE,
      data: res.data,
    };
  }

  return null;
};

export const errorConfig: RequestConfig = {
  errorConfig: {
    errorThrower: (res) => {
      const errorPayload = getErrorPayload(res as ResponseStructure);
      if (!errorPayload) {
        return;
      }

      const error: any = new Error(errorPayload.errorMessage);
      error.name = 'BizError';
      error.info = errorPayload;
      throw error;
    },
    errorHandler: (error: any, opts: any) => {
      if (opts?.skipErrorHandler) {
        throw error;
      }

      if (error.name === 'BizError') {
        const errorInfo = error.info as ResponseStructure | undefined;
        if (errorInfo) {
          const { errorMessage, errorCode } = errorInfo;
          switch (errorInfo.showType) {
            case ErrorShowType.SILENT:
              break;
            case ErrorShowType.WARN_MESSAGE:
              message.warning(errorMessage);
              break;
            case ErrorShowType.NOTIFICATION:
              notification.open({
                description: errorMessage,
                message: `${errorCode ?? ''}`,
              });
              break;
            case ErrorShowType.REDIRECT:
              break;
            case ErrorShowType.ERROR_MESSAGE:
            default:
              message.error(errorMessage);
          }
        }
        return;
      }

      if (error.response) {
        message.error(`Response status: ${error.response.status}`);
        return;
      }

      if (error.request) {
        message.error('None response! Please retry.');
        return;
      }

      message.error('Request error, please retry.');
    },
  },

  requestInterceptors: [
    (config: RequestOptions) => {
      return { ...config };
    },
  ],

  responseInterceptors: [(response) => response],
};
