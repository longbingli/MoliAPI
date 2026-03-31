import { CopyOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { App, Button, Descriptions, Space, Typography } from 'antd';
import React from 'react';

const TokenManagePage: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const { message } = App.useApp();
  const currentUser = initialState?.currentUser;

  const accessKey =
    currentUser?.accessKey ||
    currentUser?.ak ||
    '-';
  const secretKey =
    currentUser?.secretKey ||
    currentUser?.sk ||
    '-';

  const maskedSecretKey =
    secretKey !== '-' && secretKey.length > 10
      ? `${secretKey.slice(0, 4)}****${secretKey.slice(-4)}`
      : secretKey;

  const copyText = async (text: string, label: string) => {
    if (!text || text === '-') {
      message.warning(`${label} 不可用`);
      return;
    }
    try {
      await navigator.clipboard.writeText(text);
      message.success(`${label} 已复制`);
    } catch (_error) {
      message.error(`${label} 复制失败`);
    }
  };

  return (
    <PageContainer title="令牌管理" subTitle="当前仅支持查看 /user/get/login 返回的 AK / SK">
      <div
        style={{
          width: '100%',
          background: '#fff',
          borderRadius: 12,
          padding: 24,
        }}
      >
        <Descriptions column={1} size="small">
          <Descriptions.Item label="Access Key (AK)">
            <Space>
              <Typography.Text code>{accessKey}</Typography.Text>
              <Button type="link" size="small" icon={<CopyOutlined />} onClick={() => copyText(accessKey, 'AK')}>
                复制
              </Button>
            </Space>
          </Descriptions.Item>
          <Descriptions.Item label="Secret Key (SK)">
            <Space>
              <Typography.Text code>{maskedSecretKey}</Typography.Text>
              <Button type="link" size="small" icon={<CopyOutlined />} onClick={() => copyText(secretKey, 'SK')}>
                复制
              </Button>
            </Space>
          </Descriptions.Item>
        </Descriptions>
      </div>
    </PageContainer>
  );
};

export default TokenManagePage;
