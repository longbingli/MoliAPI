import { ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
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
import React, { useCallback, useEffect, useState } from 'react';
import { listAppInfoByPage } from '@/services/appInfo';

const SUCCESS_CODE = 0;

const useStyles = createStyles(() => ({
  clickableCard: {
    width: '100%',
    border: 0,
    padding: 0,
    background: 'transparent',
    textAlign: 'left',
    cursor: 'pointer',
    transition: 'transform 0.18s ease, filter 0.18s ease',
    borderRadius: 8,
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

const Home: React.FC = () => {
  const { styles } = useStyles();
  const [searchText, setSearchText] = useState('');
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<API.AppInfoVO[]>([]);
  const [total, setTotal] = useState<number>(0);
  const [errorMessage, setErrorMessage] = useState('');

  const fetchApps = useCallback(async (nextKeyword = '') => {
    setLoading(true);
    setErrorMessage('');
    try {
      const response = await listAppInfoByPage({
        searchText: nextKeyword || undefined,
      });

      if (response.code !== SUCCESS_CODE) {
        throw new Error(response.message || '获取应用列表失败');
      }

      const pageData = response.data as API.PageAppInfoVO | undefined;
      const nextRecords = (pageData?.records ?? []) as API.AppInfoVO[];
      const nextTotal = Number(pageData?.total ?? nextRecords.length ?? 0);

      setRecords(nextRecords);
      setTotal(Number.isNaN(nextTotal) ? nextRecords.length : nextTotal);
    } catch (error) {
      const message =
        error instanceof Error && error.message
          ? error.message
          : '获取应用列表失败';
      setErrorMessage(message);
      setRecords([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchApps('');
  }, [fetchApps]);

  const onSearch = () => {
    const nextKeyword = searchText.trim();
    setKeyword(nextKeyword);
    fetchApps(nextKeyword);
  };

  const onRefresh = () => {
    fetchApps(keyword);
  };

  const goToInterfaceList = (app: API.AppInfoVO) => {
    const appId = app.appId;
    const gatewayHost = app.gatewayHost || app.host;
    if (!appId) {
      return;
    }

    const search = new URLSearchParams();
    if (app.appName) {
      search.set('appName', app.appName);
    }
    if (gatewayHost) {
      search.set('gatewayHost', gatewayHost);
    }
    if (app.description) {
      search.set('appDescription', app.description);
    }
    if (app.deductPoints !== undefined && app.deductPoints !== null) {
      search.set('deductPoints', String(app.deductPoints));
    }
    if (app.totalNum !== undefined && app.totalNum !== null) {
      search.set('totalNum', String(app.totalNum));
    }
    if (app.status !== undefined && app.status !== null) {
      search.set('appStatus', String(app.status));
    }

    const searchText = search.toString();
    history.push(`/apps/${appId}/interfaces${searchText ? `?${searchText}` : ''}`);
  };

  return (
    <PageContainer
      title="接口应用"
      extra={[
        <Button key="refresh" icon={<ReloadOutlined />} onClick={onRefresh}>
          刷新
        </Button>,
      ]}
    >
      <Card style={{ marginBottom: 16 }}>
        <Space size={12} wrap>
          <Input
            value={searchText}
            onChange={(event) => setSearchText(event.target.value)}
            onPressEnter={onSearch}
            placeholder="搜索应用名称或描述"
            prefix={<SearchOutlined />}
            style={{ width: 320 }}
            allowClear
          />
          <Button type="primary" onClick={onSearch}>
            搜索
          </Button>
          <Typography.Text type="secondary">共 {total} 个应用</Typography.Text>
        </Space>
      </Card>

      {errorMessage ? (
        <Alert
          type="error"
          showIcon
          message="列表加载失败"
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
          <Empty description="暂无接口应用" />
        </Card>
      ) : (
        <Row gutter={[16, 16]}>
          {records.map((app) => (
            <Col key={String(app.appId)} xs={24} md={12} lg={8}>
              <button
                type="button"
                className={styles.clickableCard}
                onClick={() => goToInterfaceList(app)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    goToInterfaceList(app);
                  }
                }}
              >
                <Card
                  title={app.appName || `应用 #${app.appId ?? '-'}`}
                  extra={
                    <Tag color={`${app.status ?? ''}` === '1' ? 'green' : 'default'}>
                      {`${app.status ?? ''}` === '1' ? '启用' : '禁用'}
                    </Tag>
                  }
                  style={{ height: '100%' }}
                >
                  <Typography.Paragraph
                    type="secondary"
                    style={{ minHeight: 44, marginBottom: 12 }}
                  >
                    {app.description || '暂无描述'}
                  </Typography.Paragraph>
                  <Typography.Paragraph style={{ marginBottom: 8 }}>
                    <strong>网关地址：</strong>
                    {app.gatewayHost || app.host || '未配置'}
                  </Typography.Paragraph>
                  <Typography.Paragraph style={{ marginBottom: 8 }}>
                    <strong>总调用次数：</strong>
                    {app.totalNum ?? 0}
                  </Typography.Paragraph>
                  <Typography.Paragraph style={{ marginBottom: 0 }}>
                    <strong>调用积分：</strong>
                    {app.deductPoints ?? 0}
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

export default Home;
