import { useMemo, useState } from 'react'
import { Layout, Menu, Typography, Button, Space, Drawer, Grid, Badge } from 'antd'
import {
  AppstoreOutlined,
  DashboardOutlined,
  FileAddOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  OrderedListOutlined,
  ExperimentOutlined,
  HistoryOutlined,
  PlusOutlined,
} from '@ant-design/icons'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getDashboardSummary } from '../../api/dashboard'

const { Header, Sider, Content } = Layout
const { useBreakpoint } = Grid

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const screens = useBreakpoint()
  const location = useLocation()
  const navigate = useNavigate()
  const { data: summary } = useQuery({ queryKey: ['dashboard-summary'], queryFn: getDashboardSummary })
  const isDesktop = !!screens.lg

  const items = useMemo(() => [
    { key: '/dashboard', icon: <DashboardOutlined />, label: 'Tổng quan' },
    { key: '/orders', icon: <OrderedListOutlined />, label: 'Đơn hàng' },
    { key: '/production', icon: <AppstoreOutlined />, label: 'Điều phối sản xuất' },
    { key: '/qc', icon: <ExperimentOutlined />, label: <span>QC {summary?.qcFailures ? <Badge count={summary.qcFailures} size="small" style={{ marginLeft: 6 }} /> : null}</span> },
    { key: '/activity', icon: <HistoryOutlined />, label: 'Nhật ký hoạt động' },
  ], [summary?.qcFailures])

  const selected = location.pathname.startsWith('/batches/') ? ['/production'] :
    location.pathname.startsWith('/orders') ? ['/orders'] : [location.pathname]

  const menu = (
    <Menu
      mode="inline"
      selectedKeys={selected}
      items={items}
      onClick={({ key }) => {
        navigate(key)
        setDrawerOpen(false)
      }}
      className="app-menu"
    />
  )

  return (
    <Layout className="app-shell">
      {isDesktop ? (
        <Sider width={248} collapsedWidth={80} collapsible collapsed={collapsed} trigger={null} className="app-sider">
          <div className={`brand ${collapsed ? 'brand-collapsed' : ''}`}>
            <div className="brand-mark">CF</div>
            {!collapsed && (
              <div>
                <Typography.Text className="brand-name">CeramiFlow</Typography.Text>
                <div className="brand-subtitle">Manufacturing Control</div>
              </div>
            )}
          </div>
          {menu}
        </Sider>
      ) : (
        <Drawer placement="left" width={280} open={drawerOpen} onClose={() => setDrawerOpen(false)} styles={{ body: { padding: 0 } }}>
          <div className="brand mobile-brand">
            <div className="brand-mark">CF</div>
            <div>
              <Typography.Text className="brand-name">CeramiFlow</Typography.Text>
              <div className="brand-subtitle">Manufacturing Control</div>
            </div>
          </div>
          {menu}
        </Drawer>
      )}

      <Layout>
        <Header className="app-header">
          <Space>
            <Button
              type="text"
              icon={isDesktop ? (collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />) : <MenuUnfoldOutlined />}
              onClick={() => isDesktop ? setCollapsed(v => !v) : setDrawerOpen(true)}
            />
            <div className="header-title-wrap">
              <Typography.Text strong className="header-title">Xưởng gốm CeramiFlow</Typography.Text>
              <Typography.Text type="secondary" className="header-subtitle">Điều phối • Giám sát • AI</Typography.Text>
            </div>
          </Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/orders/new')}>
            Tạo đơn mới
          </Button>
        </Header>
        <Content className="app-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
