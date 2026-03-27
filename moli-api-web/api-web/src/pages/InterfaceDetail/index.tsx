import {
  ArrowLeftOutlined,
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
  Divider,
  Input,
  Row,
  Space,
  Spin,
  Tag,
  Typography,
} from 'antd';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { getInterfaceInfoById } from '@/services/interfaceInfo';

const SUCCESS_CODE = 0;

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

const prettyJson = (value: any): string => {
  if (typeof value === 'string') {
    return value;
  }
  try {
    return JSON.stringify(value, null, 2);
  } catch (_error) {
    return String(value);
  }
};

const InterfaceDetailPage: React.FC = () => {
  const { appId, interfaceId } = useParams<{ appId: string; interfaceId: string }>();
  const location = useLocation();
  const query = useMemo(() => new URLSearchParams(location.search), [location.search]);

  const appName = query.get('appName') || `应用 #${appId}`;

  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [interfaceInfo, setInterfaceInfo] = useState<API.InterfaceInfoVO | null>(null);

  const [queryText, setQueryText] = useState('{}');
  const [headerText, setHeaderText] = useState('{}');
  const [bodyText, setBodyText] = useState('{}');

  const [debugLoading, setDebugLoading] = useState(false);
  const [debugError, setDebugError] = useState('');
  const [responseStatus, setResponseStatus] = useState<number | null>(null);
  const [responseDuration, setResponseDuration] = useState<number | null>(null);
  const [responseText, setResponseText] = useState('');

  const fetchInterfaceInfo = useCallback(async () => {
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
      setInterfaceInfo(response.data as API.InterfaceInfoVO);

      const defaultHeaders = response.data.requestHeader
        ? prettyJson(response.data.requestHeader)
        : '{}';
      setHeaderText(defaultHeaders);
    } catch (error) {
      const message =
        error instanceof Error && error.message ? error.message : '获取接口详情失败';
      setErrorMessage(message);
      setInterfaceInfo(null);
    } finally {
      setLoading(false);
    }
  }, [interfaceId]);

  useEffect(() => {
    fetchInterfaceInfo();
  }, [fetchInterfaceInfo]);

  const onDebug = async () => {
    if (!interfaceInfo?.url) {
      setDebugError('当前接口未配置 URL，无法调试');
      return;
    }

    setDebugLoading(true);
    setDebugError('');
    setResponseText('');
    setResponseStatus(null);
    setResponseDuration(null);

    try {
      const queryParams = parseJsonObject(queryText, '查询参数');
      const headers = parseJsonObject(headerText, '请求头');
      const body = parseJsonObject(bodyText, '请求体');

      const method = (interfaceInfo.method || 'GET').toUpperCase();
      const url = new URL(interfaceInfo.url);

      Object.entries(queryParams).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          url.searchParams.set(key, String(value));
        }
      });

      const requestInit: RequestInit = {
        method,
        headers,
      };

      if (!['GET', 'HEAD'].includes(method)) {
        requestInit.body = JSON.stringify(body);
        if (!headers['Content-Type']) {
          (requestInit.headers as Record<string, string>)['Content-Type'] =
            'application/json';
        }
      }

      const start = performance.now();
      const response = await fetch(url.toString(), requestInit);
      const end = performance.now();

      const text = await response.text();

      setResponseStatus(response.status);
      setResponseDuration(Math.round(end - start));
      setResponseText(text || '(空响应)');
    } catch (error) {
      const message =
        error instanceof Error && error.message
          ? error.message
          : '调试请求失败，请检查参数与网络';
      setDebugError(message);
    } finally {
      setDebugLoading(false);
    }
  };

  return (
    <PageContainer
      title="接口详情"
      subTitle={interfaceInfo?.name || query.get('interfaceName') || `接口 #${interfaceId}`}
      extra={[
        <Button
          key="back"
          icon={<ArrowLeftOutlined />}
          onClick={() => history.push(`/apps/${appId}/interfaces?appName=${encodeURIComponent(appName)}`)}
        >
          返回接口列表
        </Button>,
        <Button key="refresh" icon={<ReloadOutlined />} onClick={fetchInterfaceInfo}>
          刷新详情
        </Button>,
      ]}
    >
      <Card style={{ marginBottom: 16 }}>
        <Space direction="vertical" style={{ width: '100%' }} size={8}>
          <Typography.Text type="secondary">应用：{appName}</Typography.Text>
          <Typography.Text>
            <strong>路径：</strong>
            {interfaceInfo?.url || '-'}
          </Typography.Text>
          <Typography.Text>
            <strong>方法：</strong>
            <Tag color="blue">{(interfaceInfo?.method || 'GET').toUpperCase()}</Tag>
          </Typography.Text>
          <Typography.Paragraph style={{ marginBottom: 0 }}>
            <strong>描述：</strong>
            {interfaceInfo?.description || '暂无描述'}
          </Typography.Paragraph>
        </Space>
      </Card>

      {errorMessage ? (
        <Alert
          type="error"
          showIcon
          message="接口详情加载失败"
          description={errorMessage}
          style={{ marginBottom: 16 }}
        />
      ) : null}

      {loading ? (
        <Card>
          <div style={{ textAlign: 'center', padding: '64px 0' }}>
            <Spin size="large" />
          </div>
        </Card>
      ) : (
        <>
          <Row gutter={[16, 16]}>
            <Col xs={24} lg={12}>
              <Card title="查询参数 (JSON 对象)">
                <Input.TextArea
                  value={queryText}
                  onChange={(event) => setQueryText(event.target.value)}
                  autoSize={{ minRows: 8, maxRows: 16 }}
                  placeholder='例如：{"id":1}'
                />
              </Card>
            </Col>
            <Col xs={24} lg={12}>
              <Card title="请求头 (JSON 对象)">
                <Input.TextArea
                  value={headerText}
                  onChange={(event) => setHeaderText(event.target.value)}
                  autoSize={{ minRows: 8, maxRows: 16 }}
                  placeholder='例如：{"Authorization":"Bearer xxx"}'
                />
              </Card>
            </Col>
            <Col xs={24}>
              <Card title="请求体 (JSON 对象)">
                <Input.TextArea
                  value={bodyText}
                  onChange={(event) => setBodyText(event.target.value)}
                  autoSize={{ minRows: 10, maxRows: 20 }}
                  placeholder='例如：{"name":"test"}'
                />
                <Divider style={{ margin: '16px 0' }} />
                <Space>
                  <Button
                    type="primary"
                    icon={<PlayCircleOutlined />}
                    loading={debugLoading}
                    onClick={onDebug}
                  >
                    调试请求
                  </Button>
                  {responseStatus !== null ? (
                    <Typography.Text type="secondary">
                      状态码：{responseStatus}
                    </Typography.Text>
                  ) : null}
                  {responseDuration !== null ? (
                    <Typography.Text type="secondary">
                      耗时：{responseDuration} ms
                    </Typography.Text>
                  ) : null}
                </Space>
              </Card>
            </Col>
          </Row>

          {debugError ? (
            <Alert
              type="error"
              showIcon
              message="调试失败"
              description={debugError}
              style={{ marginTop: 16 }}
            />
          ) : null}

          <Card title="响应结果" style={{ marginTop: 16 }}>
            <Input.TextArea
              value={responseText}
              readOnly
              autoSize={{ minRows: 10, maxRows: 24 }}
              placeholder="点击“调试请求”后展示响应内容"
            />
          </Card>
        </>
      )}
    </PageContainer>
  );
};

export default InterfaceDetailPage;
