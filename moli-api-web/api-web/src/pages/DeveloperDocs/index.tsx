import { ApiOutlined, KeyOutlined, ReloadOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { Alert, Button, Card, Col, Row, Skeleton, Space, Typography } from 'antd';
import React, { useEffect, useState } from 'react';

type MarkdownBlock =
  | { type: 'heading'; level: number; text: string }
  | { type: 'paragraph'; text: string }
  | { type: 'unordered-list'; items: string[] }
  | { type: 'ordered-list'; items: string[] }
  | { type: 'blockquote'; text: string }
  | { type: 'code'; language: string; content: string };

const DOC_URL = '/docs/molli-api-developer.md';

const parseMarkdown = (markdown: string): MarkdownBlock[] => {
  const lines = markdown.replace(/\r\n/g, '\n').split('\n');
  const blocks: MarkdownBlock[] = [];
  let index = 0;

  while (index < lines.length) {
    const line = lines[index];
    const trimmed = line.trim();

    if (!trimmed) {
      index += 1;
      continue;
    }

    const codeMatch = trimmed.match(/^```(\w+)?$/);
    if (codeMatch) {
      const language = codeMatch[1] || 'text';
      const codeLines: string[] = [];
      index += 1;
      while (index < lines.length && !lines[index].trim().startsWith('```')) {
        codeLines.push(lines[index]);
        index += 1;
      }
      if (index < lines.length) {
        index += 1;
      }
      blocks.push({
        type: 'code',
        language,
        content: codeLines.join('\n'),
      });
      continue;
    }

    const headingMatch = trimmed.match(/^(#{1,6})\s+(.*)$/);
    if (headingMatch) {
      blocks.push({
        type: 'heading',
        level: headingMatch[1].length,
        text: headingMatch[2].trim(),
      });
      index += 1;
      continue;
    }

    if (trimmed.startsWith('>')) {
      const quoteLines: string[] = [];
      while (index < lines.length && lines[index].trim().startsWith('>')) {
        quoteLines.push(lines[index].trim().replace(/^>\s?/, ''));
        index += 1;
      }
      blocks.push({
        type: 'blockquote',
        text: quoteLines.join(' '),
      });
      continue;
    }

    if (/^[-*]\s+/.test(trimmed)) {
      const items: string[] = [];
      while (index < lines.length && /^[-*]\s+/.test(lines[index].trim())) {
        items.push(lines[index].trim().replace(/^[-*]\s+/, ''));
        index += 1;
      }
      blocks.push({ type: 'unordered-list', items });
      continue;
    }

    if (/^\d+\.\s+/.test(trimmed)) {
      const items: string[] = [];
      while (index < lines.length && /^\d+\.\s+/.test(lines[index].trim())) {
        items.push(lines[index].trim().replace(/^\d+\.\s+/, ''));
        index += 1;
      }
      blocks.push({ type: 'ordered-list', items });
      continue;
    }

    const paragraphLines: string[] = [];
    while (index < lines.length) {
      const current = lines[index];
      const currentTrimmed = current.trim();
      if (
        !currentTrimmed ||
        currentTrimmed.startsWith('```') ||
        currentTrimmed.startsWith('>') ||
        /^#{1,6}\s+/.test(currentTrimmed) ||
        /^[-*]\s+/.test(currentTrimmed) ||
        /^\d+\.\s+/.test(currentTrimmed)
      ) {
        break;
      }
      paragraphLines.push(currentTrimmed);
      index += 1;
    }
    blocks.push({
      type: 'paragraph',
      text: paragraphLines.join(' '),
    });
  }

  return blocks;
};

const renderInline = (text: string) => {
  const parts = text.split(/(`[^`]+`|\*\*[^*]+\*\*|\[[^\]]+\]\([^)]+\))/g).filter(Boolean);
  return parts.map((part, index) => {
    if (/^`[^`]+`$/.test(part)) {
      return (
        <Typography.Text code key={`${part}-${index}`}>
          {part.slice(1, -1)}
        </Typography.Text>
      );
    }
    if (/^\*\*[^*]+\*\*$/.test(part)) {
      return (
        <Typography.Text strong key={`${part}-${index}`}>
          {part.slice(2, -2)}
        </Typography.Text>
      );
    }
    const linkMatch = part.match(/^\[([^\]]+)\]\(([^)]+)\)$/);
    if (linkMatch) {
      return (
        <a key={`${part}-${index}`} href={linkMatch[2]} target="_blank" rel="noreferrer">
          {linkMatch[1]}
        </a>
      );
    }
    return <React.Fragment key={`${part}-${index}`}>{part}</React.Fragment>;
  });
};

const slugify = (text: string) =>
  text
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fa5\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-');

const DeveloperDocsPage: React.FC = () => {
  const [markdown, setMarkdown] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadMarkdown = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await fetch(DOC_URL, { cache: 'no-store' });
      if (!response.ok) {
        throw new Error('开发者文档加载失败');
      }
      const text = await response.text();
      setMarkdown(text);
    } catch (fetchError) {
      setError(fetchError instanceof Error ? fetchError.message : '开发者文档加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMarkdown();
  }, []);

  const blocks = parseMarkdown(markdown);
  const toc = blocks
    .filter((block): block is Extract<MarkdownBlock, { type: 'heading' }> => block.type === 'heading' && block.level <= 3)
    .map((block) => ({
      id: slugify(block.text),
      level: block.level,
      text: block.text,
    }));

  return (
    <PageContainer
      title="Molli-API 开发者文档"
      subTitle="Markdown 驱动的接入文档，覆盖网关直调、在线调试与 SDK 调用"
      extra={[
        <Button key="reload" icon={<ReloadOutlined />} onClick={loadMarkdown}>
          刷新文档
        </Button>,
        <Button key="token" icon={<KeyOutlined />} onClick={() => history.push('/account/token')}>
          查看令牌
        </Button>,
        <Button key="home" type="primary" icon={<ApiOutlined />} onClick={() => history.push('/home')}>
          返回首页
        </Button>,
      ]}
    >
      {error ? (
        <Alert
          showIcon
          type="error"
          message="文档加载失败"
          description={error}
          style={{ marginBottom: 16 }}
        />
      ) : null}

      <Row gutter={[16, 16]} align="start">
        <Col xs={24} lg={18}>
          <Card>
            {loading ? (
              <Skeleton active paragraph={{ rows: 12 }} />
            ) : (
              <div style={{ fontSize: 15, lineHeight: 1.9 }}>
                {blocks.map((block, index) => {
                  if (block.type === 'heading') {
                    const levelMap = {
                      1: 2 as const,
                      2: 3 as const,
                      3: 4 as const,
                      4: 5 as const,
                      5: 5 as const,
                      6: 5 as const,
                    };
                    const headingLevel = levelMap[block.level as keyof typeof levelMap] || 5;
                    return (
                      <Typography.Title
                        id={slugify(block.text)}
                        key={`${block.text}-${index}`}
                        level={headingLevel}
                        style={{ marginTop: block.level === 1 ? 0 : 28 }}
                      >
                        {block.text}
                      </Typography.Title>
                    );
                  }

                  if (block.type === 'paragraph') {
                    return (
                      <Typography.Paragraph key={`${block.text}-${index}`}>
                        {renderInline(block.text)}
                      </Typography.Paragraph>
                    );
                  }

                  if (block.type === 'unordered-list') {
                    return (
                      <ul key={`ul-${index}`} style={{ paddingLeft: 20, marginBottom: 16 }}>
                        {block.items.map((item, itemIndex) => (
                          <li key={`${item}-${itemIndex}`} style={{ marginBottom: 8 }}>
                            {renderInline(item)}
                          </li>
                        ))}
                      </ul>
                    );
                  }

                  if (block.type === 'ordered-list') {
                    return (
                      <ol key={`ol-${index}`} style={{ paddingLeft: 20, marginBottom: 16 }}>
                        {block.items.map((item, itemIndex) => (
                          <li key={`${item}-${itemIndex}`} style={{ marginBottom: 8 }}>
                            {renderInline(item)}
                          </li>
                        ))}
                      </ol>
                    );
                  }

                  if (block.type === 'blockquote') {
                    return (
                      <blockquote
                        key={`${block.text}-${index}`}
                        style={{
                          margin: '0 0 16px',
                          padding: '12px 16px',
                          background: '#fafafa',
                          borderLeft: '4px solid #1677ff',
                          borderRadius: 8,
                        }}
                      >
                        <Typography.Text>{block.text}</Typography.Text>
                      </blockquote>
                    );
                  }

                  return (
                    <pre
                      key={`${block.language}-${index}`}
                      style={{
                        marginBottom: 16,
                        padding: 16,
                        background: '#0f172a',
                        color: '#e2e8f0',
                        borderRadius: 12,
                        overflowX: 'auto',
                        fontSize: 13,
                        lineHeight: 1.7,
                      }}
                    >
                      <code>{block.content}</code>
                    </pre>
                  );
                })}
              </div>
            )}
          </Card>
        </Col>
        <Col xs={24} lg={6}>
          <Card title="文档目录">
            <Space direction="vertical" size={10} style={{ width: '100%' }}>
              {toc.map((item) => (
                <a
                  key={item.id}
                  href={`#${item.id}`}
                  style={{
                    paddingLeft: Math.max(item.level - 1, 0) * 12,
                    color: '#1677ff',
                    wordBreak: 'break-word',
                  }}
                >
                  {item.text}
                </a>
              ))}
            </Space>
          </Card>
        </Col>
      </Row>
    </PageContainer>
  );
};

export default DeveloperDocsPage;
