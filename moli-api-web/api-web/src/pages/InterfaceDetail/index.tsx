import {
  ArrowLeftOutlined,
  DownOutlined,
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
  Input,
  Row,
  Space,
  Spin,
  Tag,
  Typography,
} from 'antd';
import { createStyles } from 'antd-style';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { getInterfaceInfoById } from '@/services/interfaceInfo';

const SUCCESS_CODE = 0;

type DebugTab =
  | 'params'
  | 'body'
  | 'headers'
  | 'cookies'
  | 'auth'
  | 'pre'
  | 'post'
  | 'settings';

type BodyMode =
  | 'none'
  | 'form-data'
  | 'x-www-form-urlencoded'
  | 'json'
  | 'xml'
  | 'text'
  | 'binary'
  | 'graphql'
  | 'msgpack';

const debugTabs: Array<{ key: DebugTab; label: string; badge?: number }> = [
  { key: 'params', label: 'Params' },
  { key: 'body', label: 'Body', badge: 1 },
  { key: 'headers', label: 'Headers' },
  { key: 'cookies', label: 'Cookies' },
  { key: 'auth', label: 'Auth' },
  { key: 'pre', label: '前置操作' },
  { key: 'post', label: '后置操作', badge: 1 },
  { key: 'settings', label: '设置' },
];

const bodyModes: Array<{ key: BodyMode; label: string }> = [
  { key: 'none', label: 'none' },
  { key: 'form-data', label: 'form-data' },
  { key: 'x-www-form-urlencoded', label: 'x-www-form-urlencoded' },
  { key: 'json', label: 'JSON' },
  { key: 'xml', label: 'XML' },
  { key: 'text', label: 'Text' },
  { key: 'binary', label: 'Binary' },
  { key: 'graphql', label: 'GraphQL' },
  { key: 'msgpack', label: 'msgpack' },
];

const parseJsonObject = (
  value: string,
  fieldName: string,
): Record<string, any> => {
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

const splitUrl = (url?: string) => {
  if (!url) {
    return {
      baseUrl: 'http://localhost:8101/api',
      path: '/',
    };
  }

  try {
    const target = new URL(url);
    return {
      baseUrl: `${target.protocol}//${target.host}`,
      path: `${target.pathname}${target.search}` || '/',
    };
  } catch (_error) {
    return {
      baseUrl: 'http://localhost:8101/api',
      path: url,
    };
  }
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

const useStyles = createStyles(() => ({
  requestCard: {
    borderRadius: 14,
    border: '1px solid #e5e7eb',
    overflow: 'hidden',
    background: '#ffffff',
  },
  requestTop: {
    display: 'grid',
    gridTemplateColumns: '100px 1fr 220px',
    gap: 10,
    alignItems: 'center',
    padding: 12,
    borderBottom: '1px solid #eef1f5',
    '@media (max-width: 900px)': {
      gridTemplateColumns: '1fr',
    },
  },
  methodBtn: {
    height: 44,
    borderRadius: 10,
    border: '1px solid #ffd1b8',
    background: '#fff3ec',
    color: '#fa541c',
    fontWeight: 700,
    fontSize: 30,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    fontFamily: "'Chakra Petch', 'PingFang SC', sans-serif",
  },
  urlBar: {
    height: 44,
    borderRadius: 10,
    border: '1px solid #e5e7eb',
    background: '#f7f9fc',
    display: 'grid',
    gridTemplateColumns: '200px 1fr',
    overflow: 'hidden',
    '@media (max-width: 900px)': {
      gridTemplateColumns: '1fr',
      height: 'auto',
    },
  },
  baseInput: {
    border: 0,
    borderRight: '1px solid #e5e7eb',
    borderRadius: 0,
    fontSize: 24,
    color: '#4b5563',
    '@media (max-width: 900px)': {
      borderRight: 'none',
      borderBottom: '1px solid #e5e7eb',
    },
  },
  pathInput: {
    border: 0,
    borderRadius: 0,
    fontSize: 28,
    color: '#111827',
  },
  sendBtn: {
    height: 44,
    borderRadius: 12,
    fontWeight: 700,
    fontSize: 23,
    background: 'linear-gradient(90deg, #f472b6 0%, #ec4899 100%)',
    border: 'none',
    boxShadow: '0 8px 20px rgba(236, 72, 153, 0.26)',
    '&:hover': {
      filter: 'brightness(1.04)',
    },
    '&:active': {
      transform: 'translateY(1px)',
    },
    '&:disabled': {
      filter: 'grayscale(0.2)',
      opacity: 0.75,
    },
  },
  tabs: {
    display: 'flex',
    gap: 26,
    padding: '14px 12px 0',
    borderBottom: '1px solid #eef1f5',
    overflowX: 'auto',
    whiteSpace: 'nowrap',
  },
  tabItem: {
    border: 'none',
    background: 'transparent',
    color: '#475569',
    padding: '0 0 12px',
    fontSize: 30,
    cursor: 'pointer',
    position: 'relative',
    display: 'inline-flex',
    alignItems: 'center',
    gap: 6,
    '&[data-active="true"]': {
      color: '#ec4899',
      fontWeight: 600,
    },
    '&[data-active="true"]::after': {
      content: '""',
      position: 'absolute',
      left: 0,
      right: 0,
      bottom: 0,
      height: 2,
      background: '#ec4899',
      borderRadius: 2,
    },
  },
  tabBadge: {
    color: '#06b6d4',
    fontSize: 23,
    fontWeight: 600,
  },
  bodyModes: {
    display: 'flex',
    gap: 18,
    padding: '12px',
    borderBottom: '1px solid #eef1f5',
    overflowX: 'auto',
    whiteSpace: 'nowrap',
  },
  modeItem: {
    border: 'none',
    background: 'transparent',
    padding: '4px 0',
    color: '#334155',
    cursor: 'pointer',
    fontSize: 23,
    borderRadius: 999,
    '&[data-active="true"]': {
      background: '#2f80ed',
      color: '#fff',
      padding: '4px 12px',
    },
  },
  editorWrap: {
    position: 'relative',
    padding: 12,
    background: '#fbfcfe',
  },
  editorToolbar: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 10,
    gap: 10,
    flexWrap: 'wrap',
  },
  tinyBtn: {
    borderRadius: 8,
  },
  editorBox: {
    border: '1px solid #e5e7eb',
    borderRadius: 10,
    background: '#fff',
    overflow: 'hidden',
    display: 'grid',
    gridTemplateColumns: '50px 1fr',
  },
  lineNo: {
    background: '#f8fafc',
    borderRight: '1px solid #e5e7eb',
    color: '#2563eb',
    fontFamily: 'Consolas, Menlo, monospace',
    fontSize: 23,
    lineHeight: '30px',
    padding: '10px 8px',
    userSelect: 'none',
  },
  editorArea: {
    border: 0,
    fontFamily: 'Consolas, Menlo, monospace',
    fontSize: 25,
    lineHeight: '30px',
    minHeight: 240,
    borderRadius: 0,
    resize: 'vertical',
  },
  responseArea: {
    fontFamily: 'Consolas, Menlo, monospace',
    fontSize: 22,
  },
}));

const InterfaceDetailPage: React.FC = () => {
  const { styles } = useStyles();
  const { appId, interfaceId } = useParams<{
    appId: string;
    interfaceId: string;
  }>();
  const location = useLocation();
  const query = useMemo(
    () => new URLSearchParams(location.search),
    [location.search],
  );

  const appName = query.get('appName') || `应用 #${appId}`;

  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [interfaceInfo, setInterfaceInfo] =
    useState<API.InterfaceInfoVO | null>(null);

  const [method, setMethod] = useState('POST');
  const [baseUrl, setBaseUrl] = useState('http://localhost:8101/api');
  const [path, setPath] = useState('/appInfo/list/page/vo');

  const [activeTab, setActiveTab] = useState<DebugTab>('body');
  const [bodyMode, setBodyMode] = useState<BodyMode>('json');

  const [queryText, setQueryText] = useState('{}');
  const [headerText, setHeaderText] = useState('{}');
  const [bodyText, setBodyText] = useState('{}');

  const [debugLoading, setDebugLoading] = useState(false);
  const [debugError, setDebugError] = useState('');
  const [responseStatus, setResponseStatus] = useState<number | null>(null);
  const [responseDuration, setResponseDuration] = useState<number | null>(null);
  const [responseText, setResponseText] = useState('');

  const currentEditorText =
    activeTab === 'params'
      ? queryText
      : activeTab === 'headers'
        ? headerText
        : bodyText;

  const setCurrentEditorText = (value: string) => {
    if (activeTab === 'params') {
      setQueryText(value);
      return;
    }
    if (activeTab === 'headers') {
      setHeaderText(value);
      return;
    }
    setBodyText(value);
  };

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

      const detail = response.data as API.InterfaceInfoVO;
      setInterfaceInfo(detail);
      setMethod((detail.method || 'POST').toUpperCase());

      const split = splitUrl(detail.url);
      setBaseUrl(split.baseUrl);
      setPath(split.path);

      const defaultHeaders = detail.requestHeader
        ? prettyJson(detail.requestHeader)
        : '{}';
      setHeaderText(defaultHeaders);

      if (!detail.method || ['POST', 'PUT', 'PATCH', 'DELETE'].includes(detail.method.toUpperCase())) {
        setActiveTab('body');
      }
    } catch (error) {
      const message =
        error instanceof Error && error.message
          ? error.message
          : '获取接口详情失败';
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
    const full = `${baseUrl}${path}`;
    if (!baseUrl.trim() || !path.trim()) {
      setDebugError('请先填写完整请求地址');
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

      const targetUrl = new URL(full);
      Object.entries(queryParams).forEach(([key, value]) => {
        if (value !== undefined && value !== null && `${value}`.length > 0) {
          targetUrl.searchParams.set(key, String(value));
        }
      });

      const requestInit: RequestInit = {
        method: method.toUpperCase(),
        headers,
      };

      if (!['GET', 'HEAD'].includes(method.toUpperCase()) && bodyMode !== 'none') {
        if (bodyMode === 'json') {
          requestInit.body = JSON.stringify(body);
          if (!headers['Content-Type']) {
            (requestInit.headers as Record<string, string>)['Content-Type'] =
              'application/json';
          }
        } else {
          requestInit.body = bodyText;
        }
      }

      const start = performance.now();
      const response = await fetch(targetUrl.toString(), requestInit);
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

  const editorLineNo = useMemo(() => {
    const lines = Math.max(currentEditorText.split('\n').length, 8);
    return Array.from({ length: lines }, (_, i) => i + 1).join('\n');
  }, [currentEditorText]);

  return (
    <PageContainer
      title="接口详情"
      subTitle={
        interfaceInfo?.name ||
        query.get('interfaceName') ||
        `接口 #${interfaceId}`
      }
      extra={[
        <Button
          key="back"
          icon={<ArrowLeftOutlined />}
          onClick={() =>
            history.push(
              `/apps/${appId}/interfaces?appName=${encodeURIComponent(appName)}`,
            )
          }
        >
          返回接口列表
        </Button>,
        <Button
          key="refresh"
          icon={<ReloadOutlined />}
          onClick={fetchInterfaceInfo}
        >
          刷新详情
        </Button>,
      ]}
    >
      <Card style={{ marginBottom: 16 }}>
        <Space direction="vertical" style={{ width: '100%' }} size={8}>
          <Typography.Text type="secondary">应用：{appName}</Typography.Text>
          <Typography.Text>
            <strong>描述：</strong>
            {interfaceInfo?.description || '暂无描述'}
          </Typography.Text>
          <Typography.Text>
            <strong>方法：</strong>
            <Tag color="blue">{method}</Tag>
          </Typography.Text>
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
          <Card className={styles.requestCard} bodyStyle={{ padding: 0 }}>
            <div className={styles.requestTop}>
              <button
                type="button"
                className={styles.methodBtn}
                onClick={() => {
                  const cycle = ['GET', 'POST', 'PUT', 'DELETE'];
                  const next = cycle[(cycle.indexOf(method) + 1) % cycle.length];
                  setMethod(next);
                }}
              >
                {method}
                <DownOutlined style={{ fontSize: 12 }} />
              </button>

              <div className={styles.urlBar}>
                <Input
                  value={baseUrl}
                  onChange={(event) => setBaseUrl(event.target.value)}
                  className={styles.baseInput}
                />
                <Input
                  value={path}
                  onChange={(event) => setPath(event.target.value)}
                  className={styles.pathInput}
                />
              </div>

              <Button
                type="primary"
                className={styles.sendBtn}
                icon={<PlayCircleOutlined />}
                loading={debugLoading}
                onClick={onDebug}
                disabled={!baseUrl.trim() || !path.trim()}
              >
                发送
              </Button>
            </div>

            <div className={styles.tabs}>
              {debugTabs.map((tab) => (
                <button
                  key={tab.key}
                  type="button"
                  className={styles.tabItem}
                  data-active={activeTab === tab.key}
                  onClick={() => setActiveTab(tab.key)}
                >
                  {tab.label}
                  {tab.badge ? (
                    <span className={styles.tabBadge}>{tab.badge}</span>
                  ) : null}
                </button>
              ))}
            </div>

            <div className={styles.bodyModes}>
              {bodyModes.map((mode) => (
                <button
                  key={mode.key}
                  type="button"
                  className={styles.modeItem}
                  data-active={bodyMode === mode.key}
                  onClick={() => setBodyMode(mode.key)}
                >
                  {mode.label}
                </button>
              ))}
            </div>

            <div className={styles.editorWrap}>
              <div className={styles.editorToolbar}>
                <Space>
                  <Button size="small" className={styles.tinyBtn}>
                    自动生成
                  </Button>
                  <Button size="small" className={styles.tinyBtn}>
                    动态值
                  </Button>
                </Space>
                <Typography.Text type="secondary">application/json</Typography.Text>
              </div>

              <div className={styles.editorBox}>
                <Input.TextArea
                  value={editorLineNo}
                  readOnly
                  autoSize={{ minRows: 8, maxRows: 16 }}
                  className={styles.lineNo}
                />
                <Input.TextArea
                  value={currentEditorText}
                  onChange={(event) => setCurrentEditorText(event.target.value)}
                  className={styles.editorArea}
                  autoSize={{ minRows: 8, maxRows: 16 }}
                />
              </div>
            </div>
          </Card>

          {debugError ? (
            <Alert
              type="error"
              showIcon
              message="调试失败"
              description={debugError}
              style={{ marginTop: 16 }}
            />
          ) : null}

          <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
            <Col xs={24} md={8}>
              <Card>
                <Typography.Text type="secondary">状态码</Typography.Text>
                <Typography.Title level={3} style={{ margin: '8px 0 0' }}>
                  {responseStatus ?? '-'}
                </Typography.Title>
              </Card>
            </Col>
            <Col xs={24} md={8}>
              <Card>
                <Typography.Text type="secondary">耗时</Typography.Text>
                <Typography.Title level={3} style={{ margin: '8px 0 0' }}>
                  {responseDuration !== null ? `${responseDuration} ms` : '-'}
                </Typography.Title>
              </Card>
            </Col>
            <Col xs={24} md={8}>
              <Card>
                <Typography.Text type="secondary">接口状态</Typography.Text>
                <Typography.Title level={3} style={{ margin: '8px 0 0' }}>
                  {`${interfaceInfo?.status ?? ''}` === '1' ? '开启' : '关闭'}
                </Typography.Title>
              </Card>
            </Col>
          </Row>

          <Card title="响应结果" style={{ marginTop: 16 }}>
            <Input.TextArea
              value={responseText}
              readOnly
              autoSize={{ minRows: 12, maxRows: 28 }}
              className={styles.responseArea}
              placeholder="点击“发送”后展示响应内容"
            />
          </Card>
        </>
      )}
    </PageContainer>
  );
};

export default InterfaceDetailPage;
