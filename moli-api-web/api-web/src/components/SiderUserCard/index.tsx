import { LogoutOutlined, SettingOutlined, UserOutlined } from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import { Avatar, Button, Card, Space, Tag, Tooltip, Typography } from 'antd';
import { createStyles } from 'antd-style';
import React from 'react';
import { flushSync } from 'react-dom';
import { outLogin } from '@/services/ant-design-pro/api';

const useStyles = createStyles(({ token }) => ({
  cardWrap: {
    margin: 12,
  },
  card: {
    background: token.colorBgContainer,
    borderRadius: 12,
  },
  collapsedWrap: {
    margin: 12,
    display: 'flex',
    justifyContent: 'center',
  },
}));

type SiderUserCardProps = {
  collapsed?: boolean;
};

const SiderUserCard: React.FC<SiderUserCardProps> = ({ collapsed }) => {
  const { styles } = useStyles();
  const { initialState, setInitialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;

  if (!currentUser) {
    return null;
  }

  const displayName = currentUser.userName || currentUser.name || '未命名用户';
  const avatarSrc = currentUser.userAvatar || currentUser.avatar;
  const role = currentUser.userRole || currentUser.access || 'user';
  const profile = currentUser.userProfile || '这个人很神秘，暂时没有简介。';

  const loginOut = async () => {
    try {
      await outLogin();
    } finally {
      flushSync(() => {
        setInitialState((state) => ({ ...state, currentUser: undefined }));
      });
      history.replace('/user/login');
    }
  };

  if (collapsed) {
    return (
      <div className={styles.collapsedWrap}>
        <Tooltip title={displayName}>
          <Avatar size={34} src={avatarSrc} icon={<UserOutlined />} />
        </Tooltip>
      </div>
    );
  }

  return (
    <div className={styles.cardWrap}>
      <Card className={styles.card} size="small">
        <Space align="start" size={12}>
          <Avatar size={44} src={avatarSrc} icon={<UserOutlined />} />
          <div>
            <Typography.Text strong>{displayName}</Typography.Text>
            <div style={{ marginTop: 4, marginBottom: 6 }}>
              <Tag color="blue">{role}</Tag>
            </div>
            <Typography.Paragraph type="secondary" ellipsis={{ rows: 2 }} style={{ marginBottom: 8 }}>
              {profile}
            </Typography.Paragraph>
          </div>
        </Space>
        <Space style={{ width: '100%', justifyContent: 'space-between' }}>
          <Button
            type="link"
            size="small"
            icon={<SettingOutlined />}
            onClick={() => history.push('/account/center')}
          >
            个人中心
          </Button>
          <Button type="link" size="small" danger icon={<LogoutOutlined />} onClick={loginOut}>
            退出
          </Button>
        </Space>
      </Card>
    </div>
  );
};

export default SiderUserCard;
