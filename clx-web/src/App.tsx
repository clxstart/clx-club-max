import React from 'react';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider, App as AntApp } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { router } from '@/routes';
import '@/shared/styles/globals.css';

// React Query 客户端
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

// Ant Design 主题配置
const antdTheme = {
  token: {
    colorPrimary: '#ff006e',
    colorBgBase: '#0a0a0a',
    colorTextBase: '#ffffff',
    borderRadius: 0,
    fontFamily: 'JetBrains Mono, monospace',
  },
};

/**
 * 应用入口组件
 */
const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <ConfigProvider theme={antdTheme} locale={zhCN}>
        <AntApp>
          <RouterProvider router={router} />
        </AntApp>
      </ConfigProvider>
    </QueryClientProvider>
  );
};

export default App;