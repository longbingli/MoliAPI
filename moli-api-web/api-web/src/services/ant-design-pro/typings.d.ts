// @ts-ignore
/* eslint-disable */

declare namespace API {
  type BaseResponse<T> = {
    code: number;
    data: T;
    message: string;
  };

  type LoginUserVO = {
    id?: number;
    userName?: string;
    userAvatar?: string;
    userProfile?: string;
    userRole?: string;
    accessKey?: string;
    secretKey?: string;
    points?: number;
    ak?: string;
    sk?: string;
    createTime?: string;
    updateTime?: string;
  };

  type CurrentUser = LoginUserVO & {
    name?: string;
    avatar?: string;
    userid?: string;
    email?: string;
    signature?: string;
    title?: string;
    group?: string;
    tags?: { key?: string; label?: string }[];
    notifyCount?: number;
    unreadCount?: number;
    country?: string;
    access?: string;
    accessKey?: string;
    secretKey?: string;
    ak?: string;
    sk?: string;
    geographic?: {
      province?: { label?: string; key?: string };
      city?: { label?: string; key?: string };
    };
    address?: string;
    phone?: string;
  };

  type UserLoginRequest = {
    userAccount?: string;
    userPassword?: string;
  };

  type UserRegisterRequest = {
    userAccount?: string;
    userPassword?: string;
    checkPassword?: string;
  };

  type UserUpdateMyRequest = {
    userName?: string;
    userAvatar?: string;
    userProfile?: string;
  };

  type AppInfoVO = {
    appId?: number | string;
    appName?: string;
    description?: string;
    gatewayHost?: string;
    host?: string;
    totalNum?: number;
    deductPoints?: number;
    userId?: number | string;
    status?: number | string;
    createTime?: string;
    updateTime?: string;
  };

  type AppInfoQueryRequest = {
    current?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    appId?: number | string;
    appName?: string;
    description?: string;
    gatewayHost?: string;
    host?: string;
    deductPoints?: number;
    userId?: number | string;
    notId?: number | string;
    searchText?: string;
  };

  type AppInfoAddRequest = {
    appName?: string;
    description?: string;
    host?: string;
    deductPoints?: number;
    status?: number;
  };

  type AppInfoEditRequest = {
    appId?: number | string;
    appName?: string;
    description?: string;
    host?: string;
    deductPoints?: number;
    status?: number;
  };

  type PageAppInfoVO = {
    records?: AppInfoVO[];
    total?: number | string;
    size?: number | string;
    current?: number | string;
  };

  type InterfaceInfoVO = {
    id?: number | string;
    appId?: number | string;
    name?: string;
    description?: string;
    url?: string;
    requestHeader?: string;
    responseHeader?: string;
    requestParams?: string;
    requestExample?: string;
    responseParams?: string;
    returnFormat?: string;
    totalNum?: number;
    status?: number | string;
    method?: string;
    userId?: number | string;
    createTime?: string;
    updateTime?: string;
  };

  type InterfaceInfoQueryRequest = {
    current?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    id?: number | string;
    appId?: number | string;
    name?: string;
    description?: string;
    requestHeader?: string;
    responseHeader?: string;
    requestParams?: string;
    requestExample?: string;
    responseParams?: string;
    returnFormat?: string;
    status?: number | string;
    method?: string;
    userId?: number | string;
    createTime?: string;
    updateTime?: string;
    notId?: number | string;
    searchText?: string;
  };

  type InterfaceInfoAddRequest = {
    appId?: number | string;
    name?: string;
    description?: string;
    url?: string;
    requestHeader?: string;
    responseHeader?: string;
    requestParams?: string;
    requestExample?: string;
    responseParams?: string;
    returnFormat?: string;
    status?: number;
    method?: string;
  };

  type InterfaceInfoEditRequest = {
    id?: number | string;
    appId?: number | string;
    name?: string;
    description?: string;
    url?: string;
    requestHeader?: string;
    responseHeader?: string;
    requestParams?: string;
    requestExample?: string;
    responseParams?: string;
    returnFormat?: string;
    status?: number;
    method?: string;
  };

  type DeleteRequest = {
    id?: number | string;
  };

  type PageInterfaceInfoVO = {
    records?: InterfaceInfoVO[];
    total?: number | string;
    size?: number | string;
    current?: number | string;
  };
}
