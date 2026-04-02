import {
  ApiOutlined,
  ArrowRightOutlined,
  BugOutlined,
  KeyOutlined,
  LinkOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history, useModel } from '@umijs/max';
import {
  Alert,
  Button,
  Card,
  Col,
  Descriptions,
  Divider,
  Row,
  Space,
  Steps,
  Table,
  Tag,
  Tabs,
  Typography,
} from 'antd';
import React from 'react';

const accessHeaderData = [
  {
    key: 'accessKey',
    name: 'accessKey',
    required: '是',
    desc: '用户的 Access Key，可在令牌管理页查看。',
  },
  {
    key: 'nonce',
    name: 'nonce',
    required: '是',
    desc: '随机数，网关会做基础合法性校验。',
  },
  {
    key: 'timestamp',
    name: 'timestamp',
    required: '是',
    desc: 'Unix 秒级时间戳，超过 5 分钟会被判定为非法请求。',
  },
  {
    key: 'body',
    name: 'body',
    required: '否',
    desc: '请求体原始字符串，签名时会参与计算。',
  },
  {
    key: 'sign',
    name: 'sign',
    required: '是',
    desc: '使用 Secret Key 对 body 进行签名后的结果。',
  },
];

const forwardRequestData = [
  {
    key: 'method',
    field: 'method',
    type: 'string',
    required: '是',
    desc: '支持 GET / POST / PUT / DELETE / PATCH。',
  },
  {
    key: 'path',
    field: 'path',
    type: 'string',
    required: '是',
    desc: '要调试的接口路径，例如 /api/poetry/random。',
  },
  {
    key: 'queryParams',
    field: 'queryParams',
    type: 'object',
    required: '否',
    desc: '查询参数对象，会自动拼接到 URL 上。',
  },
  {
    key: 'headers',
    field: 'headers',
    type: 'object',
    required: '否',
    desc: '自定义透传请求头，系统会自动过滤签名相关头。',
  },
  {
    key: 'body',
    field: 'body',
    type: 'string',
    required: '否',
    desc: '原始请求体字符串，非 GET 请求时可传入 JSON 或文本。',
  },
];

const responseData = [
  {
    key: 'statusCode',
    field: 'statusCode',
    type: 'number',
    desc: '下游接口返回的 HTTP 状态码。',
  },
  {
    key: 'durationMs',
    field: 'durationMs',
    type: 'number',
    desc: '本次调用耗时，单位毫秒。',
  },
  {
    key: 'body',
    field: 'body',
    type: 'string',
    desc: '下游接口返回的原始响应体。',
  },
  {
    key: 'headers',
    field: 'headers',
    type: 'object',
    desc: '下游接口响应头，已过滤空 key，便于前端展示。',
  },
];

const errorCodeData = [
  { key: '0', code: 0, meaning: '请求成功' },
  { key: '40000', code: 40000, meaning: '请求参数错误' },
  { key: '40100', code: 40100, meaning: '未登录或鉴权失败' },
  { key: '40300', code: 40300, meaning: '请求被拒绝，例如时间戳非法或接口已关闭' },
  { key: '40400', code: 40400, meaning: '接口不存在或下游服务未配置' },
  { key: '50000', code: 50000, meaning: '系统内部错误' },
];

const curlExample = `curl --request GET "http://localhost:8101/api/poetry/random" \\
  --header "accessKey: your_access_key" \\
  --header "nonce: 1234" \\
  --header "timestamp: 1711996800" \\
  --header "body: " \\
  --header "sign: your_sign"`;

const forwardExample = `POST /api/invoke/forward
Content-Type: application/json

{
  "method": "GET",
  "path": "/api/poetry/random",
  "queryParams": {},
  "headers": {
    "Accept": "application/json"
  },
  "body": ""
}`;

const signExample = `const body = "";
const nonce = "1234";
const timestamp = String(Math.floor(Date.now() / 1000));
const sign = SignUtils.getSign(body, secretKey);`;

const DeveloperDocsPage: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;

  return (
    <PageContainer
      title="Molli-API 开发者文档"
      subTitle="围绕 AK / SK 鉴权、网关转发、在线调试和积分计费的接入指南"
      extra={[
        <Button key="token" icon={<KeyOutlined />} onClick={() => history.push('/account/token')}>
          查看令牌
        </Button>,
        <Button key="home" type="primary" icon={<ApiOutlined />} onClick={() => history.push('/home')}>
          返回应用广场
        </Button>,
      ]}
    >
      <Alert
        showIcon
        type="info"
        message="当前平台接入方式"
        description="Molli-API 通过网关统一鉴权和转发请求，用户可在前端查看自己的 AK / SK，并通过在线调试工具或自定义客户端发起调用。调用成功后会累计接口次数，并按应用配置扣减积分。"
        style={{ marginBottom: 16 }}
      />

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} lg={14}>
          <Card
            title={
              <Space>
                <SafetyCertificateOutlined />
                快速开始
              </Space>
            }
          >
            <Steps
              direction="vertical"
              size="small"
              items={[
                {
                  title: '登录平台并获取令牌',
                  description: '在“令牌管理”页查看当前账号的 Access Key 和 Secret Key。',
                },
                {
                  title: '选择要调用的接口',
                  description: '在首页进入应用和接口详情页，确认接口状态为开启，记录请求路径与调用积分。',
                },
                {
                  title: '按规则构造签名请求',
                  description: '请求头需带 accessKey、nonce、timestamp、body、sign，sign 由 body 和 Secret Key 计算得出。',
                },
                {
                  title: '通过网关或在线调试发起调用',
                  description: '成功调用后会统计次数，并按所属应用的 deductPoints 扣减积分。',
                },
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card
            title={
              <Space>
                <KeyOutlined />
                当前账号
              </Space>
            }
          >
            <Descriptions column={1} size="small">
              <Descriptions.Item label="用户">
                {currentUser?.userName || currentUser?.name || '未登录'}
              </Descriptions.Item>
              <Descriptions.Item label="角色">
                <Tag color="blue">{currentUser?.userRole || currentUser?.access || 'user'}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="当前积分">
                <Tag color="gold">{currentUser?.points ?? 0}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Access Key">
                <Typography.Text code copyable>
                  {currentUser?.accessKey || currentUser?.ak || '-'}
                </Typography.Text>
              </Descriptions.Item>
              <Descriptions.Item label="Secret Key">
                <Typography.Text type="secondary">
                  出于安全考虑，建议前往令牌管理页查看并复制完整 SK。
                </Typography.Text>
              </Descriptions.Item>
            </Descriptions>
            <Divider style={{ margin: '16px 0' }} />
            <Space wrap>
              <Button icon={<ArrowRightOutlined />} onClick={() => history.push('/account/token')}>
                前往令牌管理
              </Button>
              <Button icon={<LinkOutlined />} onClick={() => history.push('/manage/apis')}>
                前往接口管理
              </Button>
            </Space>
          </Card>
        </Col>
      </Row>

      <Card style={{ marginBottom: 16 }}>
        <Tabs
          items={[
            {
              key: 'auth',
              label: '鉴权与签名',
              children: (
                <Space direction="vertical" size={16} style={{ width: '100%' }}>
                  <Typography.Paragraph>
                    所有通过网关直接调用的请求都需要签名。网关会校验用户、时间戳、随机数和签名结果，通过后才会转发到真实下游服务。
                  </Typography.Paragraph>
                  <Table
                    rowKey="name"
                    pagination={false}
                    size="small"
                    dataSource={accessHeaderData}
                    columns={[
                      { title: '请求头', dataIndex: 'name' },
                      { title: '必填', dataIndex: 'required', width: 80 },
                      { title: '说明', dataIndex: 'desc' },
                    ]}
                  />
                  <Card size="small" title="签名示例">
                    <Typography.Paragraph>
                      平台当前签名核心是使用 <Typography.Text code>body</Typography.Text> 和{' '}
                      <Typography.Text code>secretKey</Typography.Text> 计算 <Typography.Text code>sign</Typography.Text>。
                    </Typography.Paragraph>
                    <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{signExample}</pre>
                  </Card>
                  <Card size="small" title="直连网关调用示例">
                    <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{curlExample}</pre>
                  </Card>
                </Space>
              ),
            },
            {
              key: 'debug',
              label: '在线调试',
              children: (
                <Space direction="vertical" size={16} style={{ width: '100%' }}>
                  <Typography.Paragraph>
                    前端在线调试使用后端统一转发接口 <Typography.Text code>/api/invoke/forward</Typography.Text>，系统会自动补齐当前用户的签名头，适合在网页中快速验证接口可用性。
                  </Typography.Paragraph>
                  <Table
                    rowKey="field"
                    pagination={false}
                    size="small"
                    dataSource={forwardRequestData}
                    columns={[
                      { title: '字段', dataIndex: 'field' },
                      { title: '类型', dataIndex: 'type', width: 120 },
                      { title: '必填', dataIndex: 'required', width: 80 },
                      { title: '说明', dataIndex: 'desc' },
                    ]}
                  />
                  <Card size="small" title="调试请求示例">
                    <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{forwardExample}</pre>
                  </Card>
                </Space>
              ),
            },
            {
              key: 'response',
              label: '返回结果',
              children: (
                <Space direction="vertical" size={16} style={{ width: '100%' }}>
                  <Typography.Paragraph>
                    在线调试会返回下游接口的状态码、耗时、响应体和响应头，前端会自动格式化 JSON 内容，方便直接排查。
                  </Typography.Paragraph>
                  <Table
                    rowKey="field"
                    pagination={false}
                    size="small"
                    dataSource={responseData}
                    columns={[
                      { title: '字段', dataIndex: 'field' },
                      { title: '类型', dataIndex: 'type', width: 120 },
                      { title: '说明', dataIndex: 'desc' },
                    ]}
                  />
                </Space>
              ),
            },
            {
              key: 'points',
              label: '积分与统计',
              children: (
                <Space direction="vertical" size={16} style={{ width: '100%' }}>
                  <Typography.Paragraph>
                    请求经网关校验通过且下游响应成功后，会执行调用统计逻辑：接口调用次数加一、所属应用总调用次数加一，并按应用配置的调用积分扣减当前用户积分。
                  </Typography.Paragraph>
                  <Descriptions bordered size="small" column={1}>
                    <Descriptions.Item label="接口状态">
                      平台统一约定：<Typography.Text code>0 = 开启</Typography.Text>，<Typography.Text code>1 = 关闭</Typography.Text>。
                    </Descriptions.Item>
                    <Descriptions.Item label="计费时机">
                      仅在网关转发成功且下游返回成功状态时进行统计与扣分。
                    </Descriptions.Item>
                    <Descriptions.Item label="积分不足">
                      当用户积分不足时，扣分会失败，调用统计也不会作为成功计费完成。
                    </Descriptions.Item>
                    <Descriptions.Item label="调试同步">
                      在线调试成功后，前端会主动刷新当前登录用户信息，积分展示会同步更新。
                    </Descriptions.Item>
                  </Descriptions>
                </Space>
              ),
            },
            {
              key: 'errors',
              label: '错误码',
              children: (
                <Table
                  rowKey="code"
                  pagination={false}
                  size="small"
                  dataSource={errorCodeData}
                  columns={[
                    { title: '错误码', dataIndex: 'code', width: 120 },
                    { title: '含义', dataIndex: 'meaning' },
                  ]}
                />
              ),
            },
          ]}
        />
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card
            title={
              <Space>
                <BugOutlined />
                调试建议
              </Space>
            }
          >
            <Typography.Paragraph>
              先在接口详情页使用在线调试确认下游接口本身可用，再用自己的客户端接入网关。
            </Typography.Paragraph>
            <Typography.Paragraph>
              如果出现 401 或 403，请优先核对 Access Key、Secret Key、timestamp、nonce 和 sign。
            </Typography.Paragraph>
            <Typography.Paragraph style={{ marginBottom: 0 }}>
              如果调用成功但积分未变化，请重点检查网关服务是否已重启到最新版本。
            </Typography.Paragraph>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card
            title={
              <Space>
                <ApiOutlined />
                推荐接入流程
              </Space>
            }
          >
            <Typography.Paragraph>1. 在首页选择应用和接口，确认调用积分与接口状态。</Typography.Paragraph>
            <Typography.Paragraph>2. 在令牌管理页复制 AK / SK。</Typography.Paragraph>
            <Typography.Paragraph>3. 在接口详情页用在线调试跑通一遍请求。</Typography.Paragraph>
            <Typography.Paragraph style={{ marginBottom: 0 }}>
              4. 将签名逻辑接入自己的前端、后端或 SDK 客户端，正式走网关调用。
            </Typography.Paragraph>
          </Card>
        </Col>
      </Row>
    </PageContainer>
  );
};

export default DeveloperDocsPage;
