import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { Layout, Menu, Avatar, Dropdown, Button } from 'antd';
import { UserOutlined, LogoutOutlined, HomeOutlined, FileTextOutlined, MessageOutlined, BellOutlined } from '@ant-design/icons';
import { useLogout, useAuth } from '@/features/auth';

const { Header, Content, Sider } = Layout;

/**
 * 主布局（社区页面）
 */
export const MainLayout: React.FC = () => {
  const { user, isLoggedIn } = useAuth();
  const { logout } = useLogout();

  // 用户下拉菜单
  const userMenuItems = [
    { key: 'profile', label: <Link to={`/user/${user?.userId}`}>个人主页</Link> },
    { key: 'settings', label: <Link to="/user/settings">设置</Link> },
    { key: 'logout', label: '退出登录', icon: <LogoutOutlined />, danger: true },
  ];

  const handleMenuClick = ({ key }: { key: string }) => {
    if (key === 'logout') {
      logout();
    }
  };

  return (
    <Layout className="min-h-screen">
      {/* 顶部导航 */}
      <Header className="bg-[#1a1a1a] border-b-2 border-[#39ff14] px-4 flex items-center justify-between">
        <Link to="/" className="text-xl font-bold text-[#39ff14] tracking-widest">
          CLXHXH
        </Link>

        {isLoggedIn ? (
          <Dropdown menu={{ items: userMenuItems, onClick: handleMenuClick }} placement="bottomRight">
            <Avatar icon={<UserOutlined />} className="cursor-pointer bg-[#a020f0]" />
          </Dropdown>
        ) : (
          <Link to="/login">
            <Button type="primary" className="font-mono">登录</Button>
          </Link>
        )}
      </Header>

      {/* 侧边栏 */}
      <Layout>
        <Sider width={200} className="bg-[#0a0a0a] border-r-2 border-[#39ff14]/50">
          <Menu
            mode="inline"
            className="bg-transparent border-none"
            items={[
              { key: 'home', icon: <HomeOutlined />, label: <Link to="/">首页</Link> },
              { key: 'posts', icon: <FileTextOutlined />, label: <Link to="/posts">帖子</Link> },
              { key: 'messages', icon: <MessageOutlined />, label: <Link to="/messages">私信</Link> },
              { key: 'notifications', icon: <BellOutlined />, label: <Link to="/notifications">通知</Link> },
            ]}
          />
        </Sider>

        {/* 内容区 */}
        <Content className="p-4 bg-[#0a0a0a]">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;