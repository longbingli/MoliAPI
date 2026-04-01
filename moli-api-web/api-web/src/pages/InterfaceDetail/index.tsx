import {
  ArrowLeftOutlined,
  BugOutlined,
  CodeOutlined,
  ExclamationCircleOutlined,
  FileTextOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history, useLocation, useParams } from '@umijs/max';
import {
  Alert,
  Button,
  Card,
  Col,
  Descriptions,
  Input,
  Row,
  Select,
  Space,
  Spin,
  Table,
  Tabs,
  Tag,
  Typography,
} from 'antd';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { getInterfaceInfoById } from '@/services/interfaceInfo';
import { forwardInvoke } from '@/services/invoke';

const SUCCESS_CODE = 0;

type BodyMode = 'json' | 'text' | 'xml' | 'none';
type RequestParamRow = {
  key: string;
  name: string;
  example: string;
  required: string;
  type: string;
  desc: string;
};

const ERROR_CODE_ROWS = [
  { code: 0, name: 'SUCCESS', desc: '请求成功' },
  { code: 40000, name: 'PARAMS_ERROR', desc: '请求参数错误' },
  { code: 40100, name: 'NO_AUTH_ERROR', desc: '未登录或鉴权失败' },
  { code: 40300, name: 'NO_PERMISSION_ERROR', desc: '无访问权限' },
  { code: 50000, name: 'SYSTEM_ERROR', desc: '系统内部错误' },
];

const parseJsonObject = (value: string, fieldName: string): Record<string, any> => {
  if (!value.trim()) {
    return {};
  }
  let parsed: any;
  try {
    parsed = JSON.parse(value);
  } catch (_error) {
    throw new Error(`${fieldName} 不是合法的 JSON`);
  }
  if (parsed === null || Array.isArray(parsed) || typeof parsed !== 'object') {
    throw new Error(`${fieldName} 必须是 JSON 对象`);
  }
  return parsed;
};

const tryParseJson = (value?: string) => {
  if (!value || !value.trim()) {
    return null;
  }
  try {
    return JSON.parse(value);
  } catch (_error) {
    return null;
  }
};

const inferExampleByType = (typeText?: string) => {
  const type = (typeText || '').toLowerCase();
  if (type.includes('int') || type.includes('long') || type.includes('number')) {
    return 0;
  }
  if (type.includes('bool')) {
    return false;
  }
  if (type.includes('array') || type.includes('list')) {
    return [];
  }
  if (type.includes('object') || type.includes('map')) {
    return {};
  }
  return '';
};

const buildRequestParamRows = (requestParams?: string): { rows: RequestParamRow[]; defaults: Record<string, any> } => {
  const parsed = tryParseJson(requestParams);
  const rows: RequestParamRow[] = [];
  const defaults: Record<string, any> = {};

  if (Array.isArray(parsed)) {
    parsed.forEach((item, index) => {
      if (!item || typeof item !== 'object') {
        return;
      }
      const name = String(item.name ?? item.field ?? item.param ?? item.key ?? `param${index + 1}`);
      const type = String(item.type ?? item.dataType ?? 'string');
      const requiredRaw = item.required ?? item.isRequired ?? false;
      const desc = String(item.desc ?? item.description ?? '');
      const sample = item.example ?? item.defaultValue ?? inferExampleByType(type);
      rows.push({
        key: `${name}-${index}`,
        name,
        example: typeof sample === 'string' ? sample : JSON.stringify(sample),
        required: requiredRaw ? '是' : '否',
        type,
        desc: desc || '-',
      });
      defaults[name] = sample;
    });
    return { rows, defaults };
  }

  if (parsed && typeof parsed === 'object') {
    Object.entries(parsed).forEach(([key, value]) => {
      rows.push({
        key,
        name: key,
        example: typeof value === 'string' ? value : JSON.stringify(value),
        required: '否',
        type: Array.isArray(value) ? 'array' : typeof value,
        desc: '-',
      });
      defaults[key] = value;
    });
    return { rows, defaults };
  }

  if (requestParams?.trim()) {
    rows.push({
      key: 'text-request-params',
      name: '请求参数',
      example: '-',
      required: '按接口定义',
      type: 'text',
      desc: requestParams.trim(),
    });
    return { rows, defaults };
  }

  return {
    rows: [
      {
        key: 'none',
        name: '无',
        example: '暂无',
        required: '否',
        type: 'string',
        desc: '无',
      },
    ],
    defaults,
  };
};

const normalizeJsonText = (value?: string) => {
  const parsed = tryParseJson(value);
  if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
    return JSON.stringify(parsed, null, 2);
  }
  return '{}';
};

const prettyJson = (value: any) => {
  if (typeof value === 'string') {
    return value;
  }
  try {
    return JSON.stringify(value, null, 2);
  } catch (_error) {
    return String(value);
  }
};

const formatResponseText = (text: string, contentType: string) => {
  const raw = text || '(空响应)';
  const maybeJson =
    contentType.toLowerCase().includes('application/json') ||
    raw.trim().startsWith('{') ||
    raw.trim().startsWith('[');

  if (!maybeJson) {
    return { text: raw, format: 'text' as const };
  }

  try {
    const parsed = JSON.parse(raw);
    return { text: JSON.stringify(parsed, null, 2), format: 'json' as const };
  } catch (_error) {
    return { text: raw, format: 'text' as const };
  }
};

const pickContentType = (headers?: Record<string, string[]>) => {
  if (!headers) {
    return '';
  }
  const headerKey = Object.keys(headers).find((key) => key.toLowerCase() === 'content-type');
  if (!headerKey) {
    return '';
  }
  const values = headers[headerKey];
  return Array.isArray(values) && values.length > 0 ? values[0] : '';
};

const normalizeApiPath = (value: string) => {
  if (!value) {
    return '/';
  }
  const withSlash = value.startsWith('/') ? value : `/${value}`;
  return withSlash.replace(/\/{2,}/g, '/');
};

const normalizeApiParts = (base: string, path: string) => {
  const baseTrim = base.trim();
  const pathTrim = path.trim();
  const pathNorm = normalizeApiPath(pathTrim || '/');

  const isAbsolute = /^https?:\/\//i.test(baseTrim);
  if (isAbsolute) {
    const baseHasApi = /\/api\/?$/i.test(baseTrim);
    const pathHasApi = /^\/api(\/|$)/i.test(pathNorm);
    const nextPath = baseHasApi && pathHasApi ? pathNorm.replace(/^\/api/i, '') || '/' : pathNorm;
    return { baseUrl: baseTrim, path: normalizeApiPath(nextPath) };
  }

  const baseNormRaw = baseTrim || '/api';
  const baseNorm = baseNormRaw.startsWith('/') ? baseNormRaw : `/${baseNormRaw}`;
  const baseHasApi = /\/api\/?$/i.test(baseNorm);
  const pathHasApi = /^\/api(\/|$)/i.test(pathNorm);
  const nextPath = baseHasApi && pathHasApi ? pathNorm.replace(/^\/api/i, '') || '/' : pathNorm;
  return { baseUrl: baseNorm.replace(/\/{2,}/g, '/'), path: normalizeApiPath(nextPath) };
};

const buildRequestUrl = (base: string, path: string) => {
  const normalized = normalizeApiParts(base, path);
  if (/^https?:\/\//i.test(normalized.baseUrl)) {
    return `${normalized.baseUrl}${normalized.path}`;
  }
  return new URL(`${normalized.baseUrl}${normalized.path}`, window.location.origin).toString();
};

const splitUrl = (url?: string) => {
  if (!url) {
    return {
      baseUrl: '/api',
      path: '/',
    };
  }
  try {
    const target = new URL(url);
    const pathname = target.pathname || '/';
    if (/^\/api(\/|$)/i.test(pathname)) {
      return {
        baseUrl: '/api',
        path: pathname.replace(/^\/api/i, '') || '/',
      };
    }
    return {
      baseUrl: `${target.protocol}//${target.host}`,
      path: `${pathname}${target.search}` || '/',
    };
  } catch (_error) {
    return normalizeApiParts('/api', url);
  }
};

const InterfaceDetailPage: React.FC = () => {
  const { appId, interfaceId } = useParams<{ appId: string; interfaceId: string }>();
  const location = useLocation();
  const query = useMemo(() => new URLSearchParams(location.search), [location.search]);
  const appName = query.get('appName') || `应用 #${appId}`;
  const appGatewayHost = query.get('gatewayHost') || query.get('host') || '';
  const appDescription = query.get('appDescription') || '';
  const appDeductPoints = query.get('deductPoints') || '-';
  const appTotalNum = query.get('totalNum') || '-';
  const appStatus = query.get('appStatus') || '';

  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [info, setInfo] = useState<API.InterfaceInfoVO | null>(null);

  const [method, setMethod] = useState('POST');
  const [baseUrl, setBaseUrl] = useState('/api');
  const [path, setPath] = useState('/');

  const [queryJson, setQueryJson] = useState('{}');
  const [headerJson, setHeaderJson] = useState('{}');
  const [bodyMode, setBodyMode] = useState<BodyMode>('json');
  const [bodyText, setBodyText] = useState('{}');

  const [debugLoading, setDebugLoading] = useState(false);
  const [debugError, setDebugError] = useState('');
  const [respStatus, setRespStatus] = useState<number | null>(null);
  const [respCost, setRespCost] = useState<number | null>(null);
  const [respText, setRespText] = useState('');
  const [respFormat, setRespFormat] = useState<'json' | 'text'>('text');
  const [requestParamsData, setRequestParamsData] = useState<RequestParamRow[]>([
    {
      key: 'none',
      name: '无',
      example: '暂无',
      required: '否',
      type: 'string',
      desc: '无',
    },
  ]);

  const fetchDetail = useCallback(async () => {
    if (!interfaceId) {
      return;
    }
    setLoading(true);
    setErrorMessage('');
    try {
      const response = await getInterfaceInfoById({ id: interfaceId });
      if (response.code !== SUCCESS_CODE || !response.data) {
        throw new Error(response.message || '获取接口详情失败');
      }
      const detail = response.data as API.InterfaceInfoVO;
      setInfo(detail);

      const targetMethod = (detail.method || 'POST').toUpperCase();
      setMethod(targetMethod);
      const targetUrl = splitUrl(detail.url);
      setBaseUrl(targetUrl.baseUrl);
      setPath(targetUrl.path);

      setHeaderJson(normalizeJsonText(detail.requestHeader));
      const requestParamInfo = buildRequestParamRows(detail.requestParams);
      setRequestParamsData(requestParamInfo.rows);

      const requestExampleObj = tryParseJson(detail.requestExample);
      const defaultPayload =
        requestExampleObj && typeof requestExampleObj === 'object' && !Array.isArray(requestExampleObj)
          ? (requestExampleObj as Record<string, any>)
          : requestParamInfo.defaults;

      if (['GET', 'HEAD'].includes(targetMethod)) {
        setQueryJson(JSON.stringify(defaultPayload, null, 2));
        setBodyText('{}');
        setBodyMode('none');
      } else {
        setQueryJson('{}');
        setBodyText(JSON.stringify(defaultPayload, null, 2));
        setBodyMode('json');
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : '获取接口详情失败';
      setErrorMessage(message);
      setInfo(null);
      setRequestParamsData([
        {
          key: 'none',
          name: '无',
          example: '暂无',
          required: '否',
          type: 'string',
          desc: '无',
        },
      ]);
    } finally {
      setLoading(false);
    }
  }, [interfaceId]);

  useEffect(() => {
    fetchDetail();
  }, [fetchDetail]);

  const runDebug = async () => {
    if (!baseUrl.trim() || !path.trim()) {
      setDebugError('请先填写完整请求地址');
      return;
    }

    setDebugLoading(true);
    setDebugError('');
    setRespText('');
    setRespFormat('text');
    setRespStatus(null);
    setRespCost(null);

    try {
      const requestUrl = buildRequestUrl(baseUrl, path);
      const url = new URL(requestUrl);
      const queryParams = parseJsonObject(queryJson, '查询参数');
      const headers = parseJsonObject(headerJson, '请求头') as Record<string, string>;
      const mergedQueryParams: Record<string, string> = {};

      url.searchParams.forEach((value, key) => {
        mergedQueryParams[key] = value;
      });
      Object.entries(queryParams).forEach(([key, value]) => {
        if (value !== null && value !== undefined && `${value}`.length > 0) {
          mergedQueryParams[key] = String(value);
        }
      });

      let body = '';
      if (!['GET', 'HEAD'].includes(method) && bodyMode !== 'none') {
        if (bodyMode === 'json') {
          const bodyObject = parseJsonObject(bodyText, '请求体');
          body = JSON.stringify(bodyObject);
          if (!Object.keys(headers).some((key) => key.toLowerCase() === 'content-type')) {
            headers['Content-Type'] = 'application/json';
          }
        } else {
          body = bodyText;
        }
      }

      const invokeRes = await forwardInvoke({
        method,
        path: url.pathname,
        queryParams: mergedQueryParams,
        headers,
        body,
      });

      if (invokeRes.code !== SUCCESS_CODE || !invokeRes.data) {
        throw new Error(invokeRes.message || '调试请求失败');
      }

      const contentType = pickContentType(invokeRes.data.headers);
      const formatted = formatResponseText(invokeRes.data.body || '', contentType);

      setRespStatus(invokeRes.data.statusCode ?? null);
      setRespCost(invokeRes.data.durationMs ?? null);
      setRespText(formatted.text);
      setRespFormat(formatted.format);
    } catch (error) {
      const message = error instanceof Error ? error.message : '调试请求失败';
      setDebugError(message);
    } finally {
      setDebugLoading(false);
    }
  };

  const responseParamsData = [
    { key: 'code', name: 'code', type: 'int', desc: '响应码' },
    { key: 'data', name: 'data.text', type: 'string', desc: '返回业务数据' },
    { key: 'message', name: 'message', type: 'string', desc: '响应描述' },
  ];

  const exampleCode = `{
  "code": 0,
  "data": {
    "text": "示例返回值"
  },
  "message": "ok"
}`;

  const fetchSnippet = `const headers = ${headerJson || '{}'};

fetch("${baseUrl}${path}", {
  method: "${method}",
  headers,
  credentials: "include",
  body: ${bodyMode === 'json' ? bodyText || '{}' : '""'},
})
  .then(res => res.text())
  .then(console.log)
  .catch(console.error);`;

  const gatewayAddress =
    appGatewayHost || (/^https?:\/\//i.test(baseUrl) ? baseUrl : window.location.origin);
  const interfaceAddress = info?.url || `${baseUrl}${path}`;
  const interfaceStatus = `${info?.status ?? ''}` === '1' ? '开启' : '关闭';
  const interfaceDescription = info?.description || '暂无描述';

  return (
    <PageContainer
      title="接口详情"
      subTitle={info?.name || query.get('interfaceName') || `接口 #${interfaceId}`}
      extra={[
        <Button
          key="back"
          icon={<ArrowLeftOutlined />}
          onClick={() => {
            const params = new URLSearchParams();
            params.set('appName', appName);
            if (appGatewayHost) {
              params.set('gatewayHost', appGatewayHost);
            }
            if (appDescription) {
              params.set('appDescription', appDescription);
            }
            if (appDeductPoints !== '-') {
              params.set('deductPoints', appDeductPoints);
            }
            if (appTotalNum !== '-') {
              params.set('totalNum', appTotalNum);
            }
            if (appStatus) {
              params.set('appStatus', appStatus);
            }
            history.push(`/apps/${appId}/interfaces?${params.toString()}`);
          }}
        >
          返回接口列表
        </Button>,
        <Button key="refresh" icon={<ReloadOutlined />} onClick={fetchDetail}>
          刷新详情
        </Button>,
      ]}
    >
      <Card style={{ marginBottom: 16 }}>
        <Space size={12} wrap style={{ marginBottom: 12 }}>
          <Typography.Text type="secondary">应用：{appName}</Typography.Text>
          <Tag color="blue">{method}</Tag>
        </Space>
        <Descriptions column={{ xs: 1, sm: 1, md: 2 }} size="small">
          <Descriptions.Item label="接口地址">{interfaceAddress}</Descriptions.Item>
          <Descriptions.Item label="网关地址">{gatewayAddress}</Descriptions.Item>
          <Descriptions.Item label="返回格式">
            <Tag color="processing">JSON</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="调用积分">{appDeductPoints}</Descriptions.Item>
          <Descriptions.Item label="调用总次数">{appTotalNum}</Descriptions.Item>
          <Descriptions.Item label="接口状态">
            <Tag color={interfaceStatus === '开启' ? 'green' : 'default'}>{interfaceStatus}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="接口描述" span={2}>
            {interfaceDescription}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {errorMessage ? <Alert type="error" showIcon message={errorMessage} style={{ marginBottom: 16 }} /> : null}

      {loading ? (
        <Card>
          <div style={{ textAlign: 'center', padding: 64 }}>
            <Spin size="large" />
          </div>
        </Card>
      ) : (
        <Tabs
          defaultActiveKey="doc"
          items={[
            {
              key: 'doc',
              label: (
                <Space size={6}>
                  <FileTextOutlined /> API文档
                </Space>
              ),
              children: (
                <Space direction="vertical" size={16} style={{ width: '100%' }}>
                  <Typography.Title level={4} style={{ margin: 0 }}>
                    请求参数说明：
                  </Typography.Title>
                  <Table
                    size="middle"
                    pagination={false}
                    dataSource={requestParamsData}
                    columns={[
                      { title: '参数名称', dataIndex: 'name' },
                      { title: '示例值', dataIndex: 'example' },
                      { title: '必选', dataIndex: 'required' },
                      { title: '类型', dataIndex: 'type' },
                      { title: '描述', dataIndex: 'desc' },
                    ]}
                  />

                  <Typography.Title level={4} style={{ margin: 0 }}>
                    响应参数说明：错误码参照
                  </Typography.Title>
                  <Table
                    size="middle"
                    pagination={false}
                    dataSource={responseParamsData}
                    columns={[
                      { title: '参数名称', dataIndex: 'name' },
                      { title: '类型', dataIndex: 'type' },
                      { title: '描述', dataIndex: 'desc' },
                    ]}
                  />

                  <Typography.Title level={4} style={{ margin: 0 }}>
                    返回示例：
                  </Typography.Title>
                  <Input.TextArea
                    value={exampleCode}
                    readOnly
                    autoSize={{ minRows: 8, maxRows: 18 }}
                    style={{ fontFamily: 'Consolas, Menlo, monospace' }}
                  />
                </Space>
              ),
            },
            {
              key: 'debug',
              label: (
                <Space size={6}>
                  <BugOutlined /> 在线调试工具
                </Space>
              ),
              children: (
                <Space direction="vertical" size={16} style={{ width: '100%' }}>
                  <Card>
                    <Row gutter={[12, 12]} align="middle">
                      <Col xs={24} md={4}>
                        <Select
                          value={method}
                          onChange={setMethod}
                          style={{ width: '100%' }}
                          options={['GET', 'POST', 'PUT', 'DELETE', 'PATCH'].map((item) => ({
                            label: item,
                            value: item,
                          }))}
                        />
                      </Col>
                      <Col xs={24} md={8}>
                        <Input value={baseUrl} onChange={(event) => setBaseUrl(event.target.value)} placeholder="Base URL" />
                      </Col>
                      <Col xs={24} md={8}>
                        <Input value={path} onChange={(event) => setPath(event.target.value)} placeholder="Path" />
                      </Col>
                      <Col xs={24} md={4}>
                        <Button
                          type="primary"
                          icon={<PlayCircleOutlined />}
                          loading={debugLoading}
                          onClick={runDebug}
                          style={{ width: '100%' }}
                        >
                          发送
                        </Button>
                      </Col>
                    </Row>
                  </Card>

                  <Row gutter={[16, 16]}>
                    <Col xs={24} lg={8}>
                      <Card title="查询参数 JSON">
                        <Input.TextArea
                          value={queryJson}
                          onChange={(event) => setQueryJson(event.target.value)}
                          autoSize={{ minRows: 8, maxRows: 18 }}
                        />
                      </Card>
                    </Col>
                    <Col xs={24} lg={8}>
                      <Card title="请求头 JSON">
                        <Input.TextArea
                          value={headerJson}
                          onChange={(event) => setHeaderJson(event.target.value)}
                          autoSize={{ minRows: 8, maxRows: 18 }}
                        />
                      </Card>
                    </Col>
                    <Col xs={24} lg={8}>
                      <Card
                        title="请求体"
                        extra={
                          <Select
                            size="small"
                            value={bodyMode}
                            onChange={setBodyMode}
                            options={[
                              { label: 'JSON', value: 'json' },
                              { label: 'Text', value: 'text' },
                              { label: 'XML', value: 'xml' },
                              { label: 'None', value: 'none' },
                            ]}
                            style={{ width: 110 }}
                          />
                        }
                      >
                        <Input.TextArea
                          value={bodyText}
                          onChange={(event) => setBodyText(event.target.value)}
                          autoSize={{ minRows: 8, maxRows: 18 }}
                          disabled={bodyMode === 'none'}
                        />
                      </Card>
                    </Col>
                  </Row>

                  {debugError ? <Alert type="error" showIcon message={debugError} /> : null}

                  <Card title="响应结果">
                    <Space style={{ marginBottom: 12 }}>
                      <Tag color={respStatus && respStatus < 400 ? 'green' : 'red'}>
                        状态码：{respStatus ?? '-'}
                      </Tag>
                      <Tag color="blue">耗时：{respCost !== null ? `${respCost}ms` : '-'}</Tag>
                      <Tag color={respFormat === 'json' ? 'purple' : 'default'}>
                        格式：{respFormat.toUpperCase()}
                      </Tag>
                    </Space>
                    <Input.TextArea
                      value={respText}
                      readOnly
                      autoSize={{ minRows: 10, maxRows: 24 }}
                      style={{ fontFamily: 'Consolas, Menlo, monospace' }}
                      placeholder="发送请求后展示响应"
                    />
                  </Card>
                </Space>
              ),
            },
            {
              key: 'error',
              label: (
                <Space size={6}>
                  <ExclamationCircleOutlined /> 错误码参照
                </Space>
              ),
              children: (
                <Table
                  rowKey="code"
                  size="middle"
                  pagination={false}
                  dataSource={ERROR_CODE_ROWS}
                  columns={[
                    { title: '错误码', dataIndex: 'code' },
                    { title: '名称', dataIndex: 'name' },
                    { title: '说明', dataIndex: 'desc' },
                  ]}
                />
              ),
            },
            {
              key: 'example',
              label: (
                <Space size={6}>
                  <CodeOutlined /> 示例代码
                </Space>
              ),
              children: (
                <Input.TextArea
                  value={fetchSnippet}
                  readOnly
                  autoSize={{ minRows: 14, maxRows: 28 }}
                  style={{ fontFamily: 'Consolas, Menlo, monospace' }}
                />
              ),
            },
          ]}
        />
      )}
    </PageContainer>
  );
};

export default InterfaceDetailPage;
