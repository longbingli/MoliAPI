import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import {
  Alert,
  App,
  Button,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { addAppInfo, deleteAppInfo, editAppInfo, listMyAppInfoByPage } from '@/services/appInfo';

const SUCCESS_CODE = 0;

type AppFormValues = {
  appId?: string | number;
  appName: string;
  description?: string;
  gatewayHost?: string;
  deductPoints?: number;
  status: number;
};

const ApiManagePage: React.FC = () => {
  const { message } = App.useApp();
  const [form] = Form.useForm<AppFormValues>();
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [records, setRecords] = useState<API.AppInfoVO[]>([]);
  const [searchText, setSearchText] = useState('');
  const [keyword, setKeyword] = useState('');
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<API.AppInfoVO | null>(null);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  const fetchApps = useCallback(
    async (params?: { current?: number; pageSize?: number; search?: string }) => {
      const nextCurrent = params?.current ?? pagination.current;
      const nextPageSize = params?.pageSize ?? pagination.pageSize;
      const nextSearch = params?.search ?? keyword;

      setLoading(true);
      setErrorMessage('');
      try {
        const response = await listMyAppInfoByPage({
          current: nextCurrent,
          pageSize: nextPageSize,
          searchText: nextSearch || undefined,
          sortField: 'updateTime',
          sortOrder: 'descend',
        });

        if (response.code !== SUCCESS_CODE) {
          throw new Error(response.message || '获取应用列表失败');
        }

        const pageData = response.data as API.PageAppInfoVO | undefined;
        const total = Number(pageData?.total ?? 0);

        setRecords((pageData?.records ?? []) as API.AppInfoVO[]);
        setPagination({
          current: nextCurrent,
          pageSize: nextPageSize,
          total: Number.isNaN(total) ? 0 : total,
        });
      } catch (error) {
        const msg = error instanceof Error ? error.message : '获取应用列表失败';
        setErrorMessage(msg);
        setRecords([]);
        setPagination((prev) => ({ ...prev, total: 0 }));
      } finally {
        setLoading(false);
      }
    },
    [keyword, pagination.current, pagination.pageSize],
  );

  useEffect(() => {
    fetchApps({ current: 1, pageSize: 10, search: '' });
  }, [fetchApps]);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ status: 1, deductPoints: 0 });
    setOpen(true);
  };

  const openEdit = (item: API.AppInfoVO) => {
    setEditing(item);
    form.setFieldsValue({
      appId: item.appId,
      appName: item.appName || '',
      description: item.description || '',
      gatewayHost: item.gatewayHost || item.host || '',
      deductPoints: Number(item.deductPoints ?? 0),
      status: Number(item.status ?? 1),
    });
    setOpen(true);
  };

  const onSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      const body = {
        appId: values.appId,
        appName: values.appName,
        description: values.description,
        host: values.gatewayHost,
        deductPoints: values.deductPoints ?? 0,
        status: values.status,
      };

      const response = editing
        ? await editAppInfo(body as API.AppInfoEditRequest)
        : await addAppInfo(body as API.AppInfoAddRequest);

      if (response.code !== SUCCESS_CODE) {
        throw new Error(response.message || (editing ? '编辑应用失败' : '创建应用失败'));
      }

      message.success(editing ? '应用已更新' : '应用已创建');
      setOpen(false);
      await fetchApps({ current: pagination.current, pageSize: pagination.pageSize });
    } catch (error) {
      if (error instanceof Error && error.message) {
        message.error(error.message);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const onDelete = async (item: API.AppInfoVO) => {
    if (!item.appId) {
      return;
    }
    try {
      const response = await deleteAppInfo({ id: item.appId });
      if (response.code !== SUCCESS_CODE) {
        throw new Error(response.message || '删除应用失败');
      }
      message.success('应用已删除');
      await fetchApps({ current: pagination.current, pageSize: pagination.pageSize });
    } catch (error) {
      const msg = error instanceof Error ? error.message : '删除应用失败';
      message.error(msg);
    }
  };

  const statusTag = (value?: number | string) => {
    const status = String(value ?? '0');
    return <Tag color={status === '1' ? 'green' : 'default'}>{status === '1' ? '启用' : '禁用'}</Tag>;
  };

  const columns = useMemo(
    () => [
      {
        title: '应用名称',
        dataIndex: 'appName',
        key: 'appName',
        render: (_: unknown, item: API.AppInfoVO) => item.appName || `应用 #${item.appId ?? '-'}`,
      },
      {
        title: '网关地址',
        dataIndex: 'gatewayHost',
        key: 'gatewayHost',
        render: (_: unknown, item: API.AppInfoVO) => item.gatewayHost || item.host || '-',
      },
      {
        title: '调用积分',
        dataIndex: 'deductPoints',
        key: 'deductPoints',
        width: 110,
        render: (value: number) => value ?? 0,
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
        width: 260,
        render: (_: unknown, item: API.AppInfoVO) => (
          <Space>
            <Button
              type="link"
              onClick={() => {
                if (!item.appId) {
                  return;
                }
                const search = new URLSearchParams();
                if (item.appName) {
                  search.set('appName', item.appName);
                }
                history.push(`/manage/apis/${item.appId}?${search.toString()}`);
              }}
            >
              进入应用
            </Button>
            <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(item)}>
              编辑
            </Button>
            <Popconfirm title="确认删除该应用吗？" onConfirm={() => onDelete(item)}>
              <Button type="link" danger icon={<DeleteOutlined />}>
                删除
              </Button>
            </Popconfirm>
          </Space>
        ),
      },
    ],
    [],
  );

  return (
    <PageContainer
      title="接口管理"
      subTitle="先创建应用，再进入应用管理接口"
      extra={[
        <Button key="new" type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          创建应用
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
            fetchApps({ current: 1, pageSize: pagination.pageSize, search: next });
          }}
          placeholder="搜索应用名称或描述"
          prefix={<SearchOutlined />}
          style={{ width: 320 }}
          allowClear
        />
        <Button
          type="primary"
          onClick={() => {
            const next = searchText.trim();
            setKeyword(next);
            fetchApps({ current: 1, pageSize: pagination.pageSize, search: next });
          }}
        >
          搜索
        </Button>
        <Typography.Text type="secondary">共 {pagination.total} 个应用</Typography.Text>
      </Space>

      {errorMessage ? (
        <Alert showIcon type="error" message="加载失败" description={errorMessage} style={{ marginBottom: 16 }} />
      ) : null}

      <Table<API.AppInfoVO>
        rowKey={(item) => String(item.appId)}
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
          fetchApps({
            current: nextPagination.current,
            pageSize: nextPagination.pageSize,
          });
        }}
      />

      <Modal
        open={open}
        title={editing ? '编辑应用' : '创建应用'}
        onCancel={() => setOpen(false)}
        onOk={onSubmit}
        okText={editing ? '保存' : '创建'}
        cancelText="取消"
        confirmLoading={submitting}
        destroyOnClose
      >
        <Form<AppFormValues> form={form} layout="vertical">
          <Form.Item name="appId" hidden>
            <Input />
          </Form.Item>
          <Form.Item
            label="应用名称"
            name="appName"
            rules={[
              { required: true, message: '请输入应用名称' },
              { max: 30, message: '应用名称最多 30 个字符' },
            ]}
          >
            <Input placeholder="例如：天气查询应用" maxLength={30} />
          </Form.Item>
          <Form.Item label="应用描述" name="description" rules={[{ max: 200, message: '描述最多 200 个字符' }]}>
            <Input.TextArea rows={3} maxLength={200} placeholder="请输入应用描述（可选）" />
          </Form.Item>
          <Form.Item label="网关地址" name="gatewayHost" rules={[{ required: true, message: '请输入网关地址' }]}>
            <Input placeholder="例如：http://localhost:8090" />
          </Form.Item>
          <Form.Item label="调用积分" name="deductPoints" rules={[{ required: true, message: '请输入调用积分' }]}>
            <InputNumber min={0} precision={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="状态" name="status" rules={[{ required: true, message: '请选择状态' }]}>
            <Select
              options={[
                { label: '启用', value: 1 },
                { label: '禁用', value: 0 },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default ApiManagePage;
