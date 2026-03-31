import {
  ApiOutlined,
  ArrowLeftOutlined,
  LinkOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history, useLocation, useParams } from '@umijs/max';
import {
  Alert,
  Button,
  Card,
  Col,
  Empty,
  Input,
  Row,
  Space,
  Spin,
  Tag,
  Typography,
} from 'antd';
import { createStyles } from 'antd-style';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { listInterfaceInfoByPage } from '@/services/interfaceInfo';

const SUCCESS_CODE = 0;

const useStyles = createStyles(() => ({
  clickableCard: {
    width: '100%',
    border: 0,
    padding: 0,
    background: 'transparent',
    textAlign: 'left',
    cursor: 'pointer',
    borderRadius: 8,
    transition: 'transform 0.18s ease, filter 0.18s ease',
    touchAction: 'manipulation',
    '&:hover': {
      transform: 'translateY(-2px)',
      filter: 'brightness(1.02)',
    },
    '&:active': {
      transform: 'translateY(0) scale(0.995)',
    },
    '&:focus-visible': {
      outline: '2px solid #1677ff',
      outlineOffset: 2,
    },
  },
}));

const InterfaceListPage: React.FC = () => {
  const { styles } = useStyles();
  const { appId } = useParams<{ appId: string }>();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [records, setRecords] = useState<API.InterfaceInfoVO[]>([]);
  const [searchText, setSearchText] = useState('');
  const [keyword, setKeyword] = useState('');

  const query = useMemo(() => new URLSearchParams(location.search), [location.search]);
  const appName = query.get('appName') || `应用 #${appId}`;
  const appGatewayHost = query.get('gatewayHost') || query.get('host') || '';
  const appDescription = query.get('appDescription') || '';
  const appDeductPoints = query.get('deductPoints') || '';
  const appTotalNum = query.get('totalNum') || '';
  const appStatus = query.get('appStatus') || '';

  const fetchInterfaces = useCallback(
    async (nextKeyword = '') => {
      if (!appId) {
        return;
      }
      setLoading(true);
      setErrorMessage('');
      try {
        const response = await listInterfaceInfoByPage({
          appId,
          searchText: nextKeyword || undefined,
          sortField: 'updateTime',
          sortOrder: 'descend',
        });

        if (response.code !== SUCCESS_CODE) {
          throw new Error(response.message || '获取接口列表失败');
        }

        const pageData = response.data as API.PageInterfaceInfoVO | undefined;
        setRecords((pageData?.records ?? []) as API.InterfaceInfoVO[]);
      } catch (error) {
        const message =
          error instanceof Error && error.message
            ? error.message
            : '获取接口列表失败';
        setErrorMessage(message);
        setRecords([]);
      } finally {
        setLoading(false);
      }
    },
    [appId],
  );

  useEffect(() => {
    fetchInterfaces('');
  }, [fetchInterfaces]);

  const onSearch = () => {
    const nextKeyword = searchText.trim();
    setKeyword(nextKeyword);
    fetchInterfaces(nextKeyword);
  };

  const onRefresh = () => {
    fetchInterfaces(keyword);
  };

  const goToDetail = (item: API.InterfaceInfoVO) => {
    const interfaceId = item.id;
    if (!interfaceId || !appId) {
      return;
    }

    const params = new URLSearchParams();
    params.set('appName', appName);
    if (appGatewayHost) {
      params.set('gatewayHost', appGatewayHost);
    }
    if (item.name) {
      params.set('interfaceName', item.name);
    }
    if (appDescription) {
      params.set('appDescription', appDescription);
    }
    if (appDeductPoints) {
      params.set('deductPoints', appDeductPoints);
    }
    if (appTotalNum) {
      params.set('totalNum', appTotalNum);
    }
    if (appStatus) {
      params.set('appStatus', appStatus);
    }

    const search = params.toString();
    history.push(
      `/apps/${appId}/interfaces/${interfaceId}${search ? `?${search}` : ''}`,
    );
  };

  return (
    <PageContainer
      title="接口列表"
      subTitle={appName}
      extra={[
        <Button key="back" icon={<ArrowLeftOutlined />} onClick={() => history.push('/home')}>
          返回首页
        </Button>,
        <Button key="refresh" icon={<ReloadOutlined />} onClick={onRefresh}>
          刷新
        </Button>,
      ]}
    >
      <Card style={{ marginBottom: 16 }}>
        <Space direction="vertical" style={{ width: '100%' }} size={12}>
          <Typography.Text type="secondary">应用 ID：{appId}</Typography.Text>
          <Typography.Text type="secondary">网关地址：{appGatewayHost || '未传递'}</Typography.Text>
          <Space wrap>
            <Input
              value={searchText}
              onChange={(event) => setSearchText(event.target.value)}
              onPressEnter={onSearch}
              placeholder="搜索接口名称或描述"
              style={{ width: 320 }}
              allowClear
            />
            <Button type="primary" onClick={onSearch}>
              搜索
            </Button>
          </Space>
        </Space>
      </Card>

      {errorMessage ? (
        <Alert
          type="error"
          showIcon
          message="接口列表加载失败"
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
      ) : records.length === 0 ? (
        <Card>
          <Empty description="暂无接口" />
        </Card>
      ) : (
        <Row gutter={[16, 16]}>
          {records.map((item) => (
            <Col key={String(item.id)} xs={24} md={12} lg={8}>
              <button
                type="button"
                className={styles.clickableCard}
                onClick={() => goToDetail(item)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    goToDetail(item);
                  }
                }}
              >
                <Card
                  title={item.name || `接口 #${item.id ?? '-'}`}
                  extra={
                    <Tag color={`${item.status ?? ''}` === '1' ? 'green' : 'default'}>
                      {`${item.status ?? ''}` === '1' ? '开启' : '关闭'}
                    </Tag>
                  }
                  style={{ height: '100%' }}
                >
                  <Typography.Paragraph type="secondary" style={{ minHeight: 44 }}>
                    {item.description || '暂无描述'}
                  </Typography.Paragraph>
                  <Typography.Paragraph style={{ marginBottom: 8 }}>
                    <ApiOutlined /> 方法：{item.method || '未知'}
                  </Typography.Paragraph>
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    <LinkOutlined /> 地址：{item.url || '未配置'}
                  </Typography.Paragraph>
                </Card>
              </button>
            </Col>
          ))}
        </Row>
      )}
    </PageContainer>
  );
};

export default InterfaceListPage;
