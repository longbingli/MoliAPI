import { LockOutlined, UserOutlined } from '@ant-design/icons';
import type { ProFormInstance } from '@ant-design/pro-components';
import { LoginForm, ProFormCheckbox, ProFormText } from '@ant-design/pro-components';
import { Helmet, SelectLang, useModel } from '@umijs/max';
import { Alert, App, Tabs } from 'antd';
import { createStyles } from 'antd-style';
import React, { useRef, useState } from 'react';
import { flushSync } from 'react-dom';
import { Footer } from '@/components';
import { login, register } from '@/services/ant-design-pro/api';
import Settings from '../../../../config/defaultSettings';

const SUCCESS_CODE = 0;

type LoginTab = 'login' | 'register';

const useStyles = createStyles(({ token }) => {
  return {
    lang: {
      width: 42,
      height: 42,
      lineHeight: '42px',
      position: 'fixed',
      right: 16,
      borderRadius: token.borderRadius,
      ':hover': {
        backgroundColor: token.colorBgTextHover,
      },
    },
    container: {
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      overflow: 'auto',
      backgroundImage:
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
    },
  };
});

const Lang = () => {
  const { styles } = useStyles();

  return (
    <div className={styles.lang} data-lang>
      {SelectLang && <SelectLang />}
    </div>
  );
};

const Login: React.FC = () => {
  const [type, setType] = useState<LoginTab>('login');
  const [errorMessage, setErrorMessage] = useState<string>();
  const { initialState, setInitialState } = useModel('@@initialState');
  const { styles } = useStyles();
  const { message } = App.useApp();
  const formRef = useRef<ProFormInstance | null>(null);

  const fetchUserInfo = async () => {
    const userInfo = await initialState?.fetchUserInfo?.();
    if (userInfo) {
      flushSync(() => {
        setInitialState((s) => ({
          ...s,
          currentUser: userInfo,
        }));
      });
    }
  };

  const handleLogin = async (values: API.UserLoginRequest) => {
    const response = await login(values);
    if (response.code !== SUCCESS_CODE) {
      throw new Error(response.message || '登录失败，请重试');
    }

    message.success('登录成功');
    await fetchUserInfo();
    const urlParams = new URL(window.location.href).searchParams;
    window.location.href = urlParams.get('redirect') || '/';
  };

  const handleRegister = async (values: API.UserRegisterRequest) => {
    const response = await register(values);
    if (response.code !== SUCCESS_CODE) {
      throw new Error(response.message || '注册失败，请重试');
    }

    message.success('注册成功，请登录');
    setType('login');
    setErrorMessage(undefined);
    formRef.current?.setFieldsValue({
      userAccount: values.userAccount,
      userPassword: undefined,
      checkPassword: undefined,
      autoLogin: true,
    });
  };

  const handleSubmit = async (values: API.UserLoginRequest | API.UserRegisterRequest) => {
    setErrorMessage(undefined);
    try {
      if (type === 'login') {
        await handleLogin(values as API.UserLoginRequest);
        return;
      }
      await handleRegister(values as API.UserRegisterRequest);
    } catch (error) {
      const fallbackMessage = type === 'login' ? '登录失败，请重试' : '注册失败，请重试';
      const currentErrorMessage =
        error instanceof Error && error.message ? error.message : fallbackMessage;
      setErrorMessage(currentErrorMessage);
      message.error(currentErrorMessage);
    }
  };

  return (
    <div className={styles.container}>
      <Helmet>
        <title>
          用户登录
          {Settings.title && ` - ${Settings.title}`}
        </title>
      </Helmet>
      <Lang />
      <div
        style={{
          flex: '1',
          padding: '32px 0',
        }}
      >
        <LoginForm
          formRef={formRef}
          contentStyle={{
            minWidth: 280,
            maxWidth: '75vw',
          }}
          logo={<img alt="logo" src="/logo.svg" />}
          title="Moli API"
          subTitle="API 开放平台"
          initialValues={{
            autoLogin: true,
          }}
          submitter={{
            searchConfig: {
              submitText: type === 'login' ? '登录' : '注册',
            },
          }}
          onFinish={async (values) => {
            await handleSubmit(values as API.UserLoginRequest | API.UserRegisterRequest);
          }}
        >
          <Tabs
            activeKey={type}
            onChange={(activeKey) => {
              setType(activeKey as LoginTab);
              setErrorMessage(undefined);
            }}
            centered
            items={[
              {
                key: 'login',
                label: '账号登录',
              },
              {
                key: 'register',
                label: '账号注册',
              },
            ]}
          />

          {errorMessage ? (
            <Alert
              style={{
                marginBottom: 24,
              }}
              message={errorMessage}
              type="error"
              showIcon
            />
          ) : null}

          <ProFormText
            name="userAccount"
            fieldProps={{
              size: 'large',
              prefix: <UserOutlined />,
            }}
            placeholder="请输入账号"
            rules={[
              {
                required: true,
                message: '请输入账号',
              },
              {
                min: 4,
                message: '账号至少 4 位',
              },
            ]}
          />

          <ProFormText.Password
            name="userPassword"
            fieldProps={{
              size: 'large',
              prefix: <LockOutlined />,
            }}
            placeholder={type === 'login' ? '请输入密码' : '请输入密码（至少 8 位）'}
            rules={[
              {
                required: true,
                message: '请输入密码',
              },
              {
                min: 8,
                message: '密码至少 8 位',
              },
            ]}
          />

          {type === 'register' ? (
            <ProFormText.Password
              name="checkPassword"
              fieldProps={{
                size: 'large',
                prefix: <LockOutlined />,
              }}
              placeholder="请再次输入密码"
              rules={[
                {
                  required: true,
                  message: '请再次输入密码',
                },
                {
                  validator: async (_, value) => {
                    const password = formRef.current?.getFieldValue('userPassword');
                    if (!value || !password || value === password) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('两次输入的密码不一致'));
                  },
                },
              ]}
            />
          ) : null}

          {type === 'login' ? (
            <div
              style={{
                marginBottom: 24,
              }}
            >
              <ProFormCheckbox noStyle name="autoLogin">
                自动登录
              </ProFormCheckbox>
            </div>
          ) : null}
        </LoginForm>
      </div>
      <Footer />
    </div>
  );
};

export default Login;
