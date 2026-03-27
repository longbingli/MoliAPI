import { PageContainer } from '@ant-design/pro-components';
import { Card, Col, Row, Typography } from 'antd';
import React from 'react';

const Home: React.FC = () => {
  return (
    <PageContainer title="首页">
      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <Card title="Moli API 开放平台" bordered>
            <Typography.Paragraph>
              当前已完成账号登录、注册、退出登录和登录态校验。
            </Typography.Paragraph>
            <Typography.Paragraph>
              后续可在此页接入应用列表和接口调用入口。
            </Typography.Paragraph>
          </Card>
        </Col>
      </Row>
    </PageContainer>
  );
};

export default Home;
