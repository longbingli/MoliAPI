import { ArrowLeftOutlined, DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history, useLocation, useParams } from '@umijs/max';
import {
  Alert,
  App,
  Button,
  Form,
  Input,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { addInterfaceInfo, deleteInterfaceInfo, editInterfaceInfo, listInterfaceInfoByPage } from '@/services/interfaceInfo';

const SUCCESS_CODE = 0;

type InterfaceFormValues = {
  id?: number | string;
  name: string;
  description?: string;
  url: string;
  method: string;
  status: number;
  requestHeader?: string;
  responseHeader?: string;
  requestParams?: string;
  requestExample?: string;
  responseParams?: string;
  returnFormat?: string;
};

const InterfaceManagePage: React.FC = () => {
  const { message } = App.useApp();
  const { appId } = useParams<{ appId: string }>();
  const location = useLocation();
  const [form] = Form.useForm<InterfaceFormValues>();

  const query = useMemo(() => new URLSearchParams(location.search), [location.search]);
  const appName = query.get('appName') || (appId ? `应用 #${appId}` : '未知应用');

  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [records, setRecords] = useState<API.InterfaceInfoVO[]>([]);
  const [searchText, setSearchText] = useState('');
  const [keyword, setKeyword] = useState('');
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<API.InterfaceInfoVO | null>(null);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  const fetchInterfaces = useCallback(
    async (params?: { current?: number; pageSize?: number; search?: string }) => {
      if (!appId) {
        return;
      }
      const nextCurrent = params?.current ?? pagination.current;
      const nextPageSize = params?.pageSize ?? pagination.pageSize;
      const nextSearch = params?.search ?? keyword;

      setLoading(true);
      setErrorMessage('');
      try {
        const response = await listInterfaceInfoByPage({
          appId,
          current: nextCurrent,
          pageSize: nextPageSize,
          searchText: nextSearch || undefined,
          sortField: 'updateTime',
          sortOrder: 'descend',
        });

        if (response.code !== SUCCESS_CODE) {
          throw new Error(response.message || '获取接口列表失败');
        }

        const pageData = response.data as API.PageInterfaceInfoVO | undefined;
        const total = Number(pageData?.total ?? 0);
        setRecords((pageData?.records ?? []) as API.InterfaceInfoVO[]);
        setPagination({
          current: nextCurrent,
          pageSize: nextPageSize,
          total: Number.isNaN(total) ? 0 : total,
        });
      } catch (error) {
        const msg = error instanceof Error ? error.message : '获取接口列表失败';
        setErrorMessage(msg);
        setRecords([]);
        setPagination((prev) => ({ ...prev, total: 0 }));
      } finally {
        setLoading(false);
      }
    },
    [appId, keyword, pagination.current, pagination.pageSize],
  );

  useEffect(() => {
    fetchInterfaces({ current: 1, pageSize: 10, search: '' });
  }, [fetchInterfaces]);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({
      method: 'POST',
      status: 0,
      requestHeader: '',
      responseHeader: '',
      requestParams: '',
      requestExample: '',
      responseParams: '',
      returnFormat: 'JSON',
    });
    setOpen(true);
  };

  const openEdit = (item: API.InterfaceInfoVO) => {
    setEditing(item);
    form.setFieldsValue({
      id: item.id,
      name: item.name || '',
      description: item.description || '',
      url: item.url || '',
      method: item.method || 'POST',
      status: Number(item.status ?? 0),
      requestHeader: item.requestHeader || '',
      responseHeader: item.responseHeader || '',
      requestParams: item.requestParams || '',
      requestExample: item.requestExample || '',
      responseParams: item.responseParams || '',
      returnFormat: item.returnFormat || 'JSON',
    });
    setOpen(true);
  };

  const onSubmit = async () => {
    if (!appId) {
      message.error('应用 ID 无效');
      return;
    }
    try {
      const values = await form.validateFields();
      setSubmitting(true);

      const body = {
        id: values.id,
        appId,
        name: values.name,
        description: values.description,
        url: values.url,
        method: values.method,
        status: values.status,
        requestHeader: values.requestHeader,
        responseHeader: values.responseHeader,
        requestParams: values.requestParams,
        requestExample: values.requestExample,
        responseParams: values.responseParams,
        returnFormat: values.returnFormat,
      };

      const response = editing
        ? await editInterfaceInfo(body as API.InterfaceInfoEditRequest)
        : await addInterfaceInfo(body as API.InterfaceInfoAddRequest);

      if (response.code !== SUCCESS_CODE) {
        throw new Error(response.message || (editing ? '编辑接口失败' : '创建接口失败'));
      }

      message.success(editing ? '接口已更新' : '接口已创建');
      setOpen(false);
      await fetchInterfaces({ current: pagination.current, pageSize: pagination.pageSize });
    } catch (error) {
      if (error instanceof Error && error.message) {
        message.error(error.message);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const onDelete = async (item: API.InterfaceInfoVO) => {
    if (!item.id) {
      return;
    }
    try {
      const response = await deleteInterfaceInfo({ id: item.id });
      if (response.code !== SUCCESS_CODE) {
        throw new Error(response.message || '删除接口失败');
      }
      message.success('接口已删除');
      await fetchInterfaces({ current: pagination.current, pageSize: pagination.pageSize });
    } catch (error) {
      const msg = error instanceof Error ? error.message : '删除接口失败';
      message.error(msg);
    }
  };

  const statusTag = (value?: number | string) => {
    const status = String(value ?? '0');
    return <Tag color={status === '0' ? 'green' : 'default'}>{status === '0' ? '启用' : '禁用'}</Tag>;
  };

  const columns = useMemo(
    () => [
      {
        title: '接口名称',
        dataIndex: 'name',
        key: 'name',
        render: (_: unknown, item: API.InterfaceInfoVO) => item.name || `接口 #${item.id ?? '-'}`,
      },
      {
        title: '方法',
        dataIndex: 'method',
        key: 'method',
        width: 100,
        render: (value: string) => <Tag color="blue">{value || '-'}</Tag>,
      },
      {
        title: '地址',
        dataIndex: 'url',
        key: 'url',
      },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 100,
        render: (value: number | string) => statusTag(value),
      },
      {
        title: '更新时间',
        dataIndex: 'updateTime',
        key: 'updateTime',
        width: 200,
        render: (value: string) => value || '-',
      },
      {
        title: '操作',
        key: 'action',
        width: 240,
        render: (_: unknown, item: API.InterfaceInfoVO) => (
          <Space>
            <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(item)}>
              编辑
            </Button>
            <Popconfirm title="确认删除该接口吗？" onConfirm={() => onDelete(item)}>
              <Button type="link" danger icon={<DeleteOutlined />}>
                删除
              </Button>
            </Popconfirm>
            <Button
              type="link"
              onClick={() => {
                if (!item.id) {
                  return;
                }
                const params = new URLSearchParams();
                params.set('appName', appName);
                if (item.name) {
                  params.set('interfaceName', item.name);
                }
                history.push(`/apps/${appId}/interfaces/${item.id}?${params.toString()}`);
              }}
            >
              查看
            </Button>
          </Space>
        ),
      },
    ],
    [appId, appName],
  );

  return (
    <PageContainer
      title="接口管理"
      subTitle={`${appName}（ID: ${appId || '-'}）`}
      extra={[
        <Button key="back" icon={<ArrowLeftOutlined />} onClick={() => history.push('/manage/apis')}>
          返回应用管理
        </Button>,
        <Button key="new" type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          新建接口
        </Button>,
      ]}
    >
      <Space style={{ marginBottom: 16 }} wrap>
        <Input
          value={searchText}
          onChange={(event) => setSearchText(event.target.value)}
          onPressEnter={() => {
            const next = searchText.trim();
            setKeyword(next);
            fetchInterfaces({ current: 1, pageSize: pagination.pageSize, search: next });
          }}
          placeholder="搜索接口名称、描述或地址"
          prefix={<SearchOutlined />}
          style={{ width: 320 }}
          allowClear
        />
        <Button
          type="primary"
          onClick={() => {
            const next = searchText.trim();
            setKeyword(next);
            fetchInterfaces({ current: 1, pageSize: pagination.pageSize, search: next });
          }}
        >
          搜索
        </Button>
        <Typography.Text type="secondary">共 {pagination.total} 个接口</Typography.Text>
      </Space>

      {errorMessage ? (
        <Alert showIcon type="error" message="加载失败" description={errorMessage} style={{ marginBottom: 16 }} />
      ) : null}

      <Table<API.InterfaceInfoVO>
        rowKey={(item) => String(item.id)}
        columns={columns}
        dataSource={records}
        loading={loading}
        pagination={{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: true,
        }}
        onChange={(nextPagination) => {
          fetchInterfaces({
            current: nextPagination.current,
            pageSize: nextPagination.pageSize,
          });
        }}
      />

      <Modal
        open={open}
        title={editing ? '编辑接口' : '新建接口'}
        onCancel={() => setOpen(false)}
        onOk={onSubmit}
        okText={editing ? '保存' : '创建'}
        cancelText="取消"
        confirmLoading={submitting}
        width={860}
        destroyOnClose
      >
        <Form<InterfaceFormValues> form={form} layout="vertical">
          <Form.Item name="id" hidden>
            <Input />
          </Form.Item>
          <Form.Item
            label="接口名称"
            name="name"
            rules={[
              { required: true, message: '请输入接口名称' },
              { max: 50, message: '接口名称最多 50 个字符' },
            ]}
          >
            <Input placeholder="例如：获取天气" maxLength={50} />
          </Form.Item>
          <Form.Item label="接口描述" name="description" rules={[{ max: 200, message: '描述最多 200 个字符' }]}>
            <Input.TextArea rows={2} maxLength={200} placeholder="请输入接口描述（可选）" />
          </Form.Item>
          <Space style={{ display: 'flex' }} align="start" size={12}>
            <Form.Item
              label="请求方法"
              name="method"
              rules={[{ required: true, message: '请选择请求方法' }]}
              style={{ minWidth: 160 }}
            >
              <Select
                options={['GET', 'POST', 'PUT', 'DELETE', 'PATCH'].map((item) => ({ label: item, value: item }))}
              />
            </Form.Item>
            <Form.Item
              label="状态"
              name="status"
              rules={[{ required: true, message: '请选择状态' }]}
              style={{ minWidth: 140 }}
            >
              <Select
                options={[
                  { label: '启用', value: 0 },
                  { label: '禁用', value: 1 },
                ]}
              />
            </Form.Item>
            <Form.Item
              label="返回格式"
              name="returnFormat"
              rules={[{ max: 20, message: '返回格式最多 20 字符' }]}
              style={{ flex: 1 }}
            >
              <Input placeholder="例如：JSON" maxLength={20} />
            </Form.Item>
          </Space>
          <Form.Item label="接口地址" name="url" rules={[{ required: true, message: '请输入接口地址' }]}>
            <Input placeholder="例如：/weather/today 或 https://api.example.com/weather" />
          </Form.Item>
          <Form.Item label="请求头" name="requestHeader">
            <Input.TextArea rows={3} placeholder='可填 JSON 字符串，如 {"Content-Type":"application/json"}' />
          </Form.Item>
          <Form.Item label="响应头" name="responseHeader">
            <Input.TextArea rows={3} placeholder='可填 JSON 字符串，如 {"content-type":"application/json"}' />
          </Form.Item>
          <Form.Item label="请求参数" name="requestParams">
            <Input.TextArea rows={3} placeholder="请求参数说明或 JSON 示例" />
          </Form.Item>
          <Form.Item label="请求示例" name="requestExample">
            <Input.TextArea rows={3} placeholder="请求示例（可选）" />
          </Form.Item>
          <Form.Item label="响应参数" name="responseParams">
            <Input.TextArea rows={3} placeholder="响应参数说明（可选）" />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default InterfaceManagePage;
