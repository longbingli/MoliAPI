import { ApiOutlined, BookOutlined, GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import React from 'react';

const Footer: React.FC = () => {
  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      copyright="Moli-API Open Platform"
      links={[
        {
          key: 'moli-api',
          title: 'Moli-API',
          href: '/home',
        },
        {
          key: 'developer-docs',
          title: (
            <>
              <BookOutlined /> 开发者文档
            </>
          ),
          href: '/developer/docs',
        },
        {
          key: 'api-square',
          title: (
            <>
              <ApiOutlined /> 接口广场
            </>
          ),
          href: '/home',
        },
        {
          key: 'github',
          title: <GithubOutlined />,
          href: 'https://github.com',
          blankTarget: true,
        },
      ]}
    />
  );
};

export default Footer;
