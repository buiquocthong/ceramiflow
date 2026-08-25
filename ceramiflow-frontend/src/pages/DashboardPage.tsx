import { useQuery } from '@tanstack/react-query'
import { Card, Col, Row, Space, Table, Typography, Button, Progress } from 'antd'
import { CheckCircleOutlined, ExperimentOutlined, FireOutlined, OrderedListOutlined, ReloadOutlined, ToolOutlined } from '@ant-design/icons'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/common/PageHeader'
import MetricCard from '../components/dashboard/MetricCard'
import { getDashboardSummary } from '../api/dashboard'
import { listBatches } from '../api/batches'
import { listOrders } from '../api/orders'
import { STAGE_SHORT_LABELS, stageProgress } from '../constants/workflow'
import { BatchStatusTag, PriorityTag, StageTag } from '../utils/status'
import { formatDateTime } from '../utils/format'

export default function DashboardPage() {
  const navigate = useNavigate()
  const summaryQuery = useQuery({ queryKey: ['dashboard-summary'], queryFn: getDashboardSummary })
  const batchesQuery = useQuery({ queryKey: ['batches'], queryFn: listBatches })
  const ordersQuery = useQuery({ queryKey: ['orders'], queryFn: listOrders })

  const summary = summaryQuery.data
  const ordersById = new Map((ordersQuery.data || []).map(o => [o.id, o]))
  const chartData = Object.entries(summary?.stageDistribution || {})
    .filter(([stage]) => stage !== 'COMPLETED')
    .map(([stage, count]) => ({ stage: STAGE_SHORT_LABELS[stage as keyof typeof STAGE_SHORT_LABELS] || stage, count }))
  const recentBatches = [...(batchesQuery.data || [])].sort((a, b) => b.id - a.id).slice(0, 6)

  return (
    <div>
      <PageHeader
        title="Tổng quan sản xuất"
        subtitle="Theo dõi tình trạng xưởng và các mẻ gốm theo thời gian thực."
        extra={<Button icon={<ReloadOutlined />} onClick={() => { summaryQuery.refetch(); batchesQuery.refetch(); }}>Làm mới</Button>}
      />
      <Row gutter={[16, 16]}>
        <Col xs={12} lg={4}><MetricCard title="Đơn hàng" value={summary?.totalOrders} prefix={<OrderedListOutlined />} loading={summaryQuery.isLoading} /></Col>
        <Col xs={12} lg={5}><MetricCard title="Mẻ đang chạy" value={summary?.activeBatches} prefix={<FireOutlined />} loading={summaryQuery.isLoading} /></Col>
        <Col xs={12} lg={5}><MetricCard title="Cần làm lại" value={summary?.reworkBatches} prefix={<ToolOutlined />} loading={summaryQuery.isLoading} /></Col>
        <Col xs={12} lg={5}><MetricCard title="QC lỗi" value={summary?.qcFailures} prefix={<ExperimentOutlined />} loading={summaryQuery.isLoading} /></Col>
        <Col xs={12} lg={5}><MetricCard title="Hoàn thành" value={summary?.completedBatches} prefix={<CheckCircleOutlined />} loading={summaryQuery.isLoading} /></Col>
      </Row>

      <Row gutter={[16, 16]} className="section-gap">
        <Col xs={24} xl={10}>
          <Card title="Phân bố theo công đoạn" className="full-height-card">
            {chartData.length ? (
              <div style={{ width: '100%', height: 320 }}>
                <ResponsiveContainer>
                  <BarChart data={chartData} margin={{ top: 8, right: 8, left: -20, bottom: 40 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="stage" angle={-25} textAnchor="end" interval={0} height={70} tick={{ fontSize: 12 }} />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Bar dataKey="count" name="Số mẻ" fill="#7a4f35" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            ) : <Typography.Text type="secondary">Chưa có dữ liệu sản xuất.</Typography.Text>}
          </Card>
        </Col>
        <Col xs={24} xl={14}>
          <Card title="Mẻ sản xuất gần đây" extra={<Button type="link" onClick={() => navigate('/production')}>Xem Kanban</Button>}>
            <Table
              rowKey="id"
              size="middle"
              pagination={false}
              loading={batchesQuery.isLoading}
              dataSource={recentBatches}
              onRow={record => ({ onClick: () => navigate(`/batches/${record.id}`), className: 'clickable-row' })}
              columns={[
                { title: 'Mẻ', dataIndex: 'batchCode', render: (v) => <Typography.Text strong>{v}</Typography.Text> },
                { title: 'Sản phẩm', render: (_, r) => ordersById.get(r.orderId)?.specification?.productType || '—' },
                { title: 'Ưu tiên', render: (_, r) => { const o=ordersById.get(r.orderId); return o ? <PriorityTag value={o.priority} /> : '—' } },
                { title: 'Công đoạn', render: (_, r) => <StageTag value={r.currentStage} /> },
                { title: 'Tiến độ', width: 150, render: (_, r) => <Progress percent={stageProgress(r.currentStage)} size="small" /> },
                { title: 'Trạng thái', render: (_, r) => <BatchStatusTag value={r.status} /> },
                { title: 'Bắt đầu', render: (_, r) => formatDateTime(r.startedAt) },
              ]}
            />
          </Card>
        </Col>
      </Row>

      <Card className="section-gap" bordered={false}>
        <Space direction="vertical" size={4}>
          <Typography.Text strong>Luồng nghiệp vụ CeramiFlow</Typography.Text>
          <Typography.Text type="secondary">Mô tả đơn hàng → AI bóc tách JSON → người dùng xác nhận → tạo mẻ → workflow xưởng → QC → đóng gói / làm lại → thông báo Telegram.</Typography.Text>
        </Space>
      </Card>
    </div>
  )
}
