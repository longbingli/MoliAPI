import { EditOutlined, UserOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { App, Avatar, Button, Card, Col, Form, Input, Modal, Row, Space, Tag, Typography } from 'antd';
import React, { useMemo, useState } from 'react';
import { flushSync } from 'react-dom';
import { updateMyUser } from '@/services/ant-design-pro/api';

const SUCCESS_CODE = 0;

const AccountCenterPage: React.FC = () => {
  const { initialState, setInitialState } = useModel('@@initialState');
  const { message } = App.useApp();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<API.UserUpdateMyRequest>();

  const currentUser = initialState?.currentUser;
  const displayName = useMemo(
    () => currentUser?.userName || currentUser?.name || '未命名用户',
    [currentUser],
  );
  const avatar = currentUser?.userAvatar || currentUser?.avatar;
  const role = currentUser?.userRole || currentUser?.access || 'user';

  const openModal = () => {
    form.setFieldsValue({
      userName: currentUser?.userName || currentUser?.name || '',
      userAvatar: currentUser?.userAvatar || currentUser?.avatar || '',
      userProfile: currentUser?.userProfile || '',
    });
    setOpen(true);
  };

  const syncCurrentUser = async () => {
    const user = await initialState?.fetchUserInfo?.();
    flushSync(() => {
      setInitialState((state) => ({
        ...state,
        currentUser: user,
      }));
    });
  };

  const onSave = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      const response = await updateMyUser(values);
      if (response.code !== SUCCESS_CODE) {
        throw new Error(response.message || '保存失败，请稍后重试');
      }
      await syncCurrentUser();
      setOpen(false);
      message.success('个人信息已更新');
    } catch (error) {
      if (error instanceof Error && error.message) {
        message.error(error.message);
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <PageContainer title="个人中心" subTitle="管理你的基础资料">
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card
            title="基础信息"
            extra={
              <Button type="primary" icon={<EditOutlined />} onClick={openModal}>
                编辑资料
              </Button>
            }
          >
            <Space size={16} align="start">
              <Avatar size={72} src={avatar} icon={<UserOutlined />} />
              <Space direction="vertical" size={8}>
                <Typography.Title level={4} style={{ margin: 0 }}>
                  {displayName}
                </Typography.Title>
                <Tag color="blue">{role}</Tag>
                <Typography.Text type="secondary">
                  {currentUser?.userProfile || '这个人很神秘，暂时没有简介。'}
                </Typography.Text>
                <Typography.Text type="secondary">
                  注册时间：{currentUser?.createTime || '-'}
                </Typography.Text>
              </Space>
            </Space>
          </Card>
        </Col>
      </Row>

      <Modal
        title="编辑个人资料"
        open={open}
        onCancel={() => setOpen(false)}
        onOk={onSave}
        okText="保存"
        cancelText="取消"
        confirmLoading={submitting}
        destroyOnClose
      >
        <Form<API.UserUpdateMyRequest> form={form} layout="vertical">
          <Form.Item
            label="昵称"
            name="userName"
            rules={[
              { required: true, message: '请输入昵称' },
              { min: 2, max: 20, message: '昵称长度为 2-20 个字符' },
            ]}
          >
            <Input placeholder="请输入昵称" maxLength={20} />
          </Form.Item>
          <Form.Item
            label="头像 URL"
            name="userAvatar"
            rules={[
              { type: 'url', message: '请输入合法 URL 地址' },
            ]}
          >
            <Input placeholder="https://example.com/avatar.png（可选）" />
          </Form.Item>
          <Form.Item
            label="个人简介"
            name="userProfile"
            rules={[{ max: 200, message: '简介最多 200 个字符' }]}
          >
            <Input.TextArea rows={4} maxLength={200} placeholder="介绍一下你自己（可选）" />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default AccountCenterPage;
