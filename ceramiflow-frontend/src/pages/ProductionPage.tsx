import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Button, Card, Col, Row, Space, Typography } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import PageHeader from '../components/common/PageHeader'
import EmptyState from '../components/common/EmptyState'
import BatchCard from '../components/production/BatchCard'
import { listBatches } from '../api/batches'
import { listOrders } from '../api/orders'
import { STAGE_LABELS, WORKFLOW_STAGES } from '../constants/workflow'

export default function ProductionPage() {
  const batchesQuery = useQuery({ queryKey: ['batches'], queryFn: listBatches })
  const ordersQuery = useQuery({ queryKey: ['orders'], queryFn: listOrders })
  const ordersById = useMemo(() => new Map((ordersQuery.data || []).map(o => [o.id, o])), [ordersQuery.data])

  const batches = batchesQuery.data || []
  return (
    <div>
      <PageHeader title="Điều phối sản xuất" subtitle="Kanban phản ánh trạng thái backend; không cho phép kéo thả bỏ qua công đoạn." extra={<Button icon={<ReloadOutlined />} onClick={() => batchesQuery.refetch()}>Làm mới</Button>} />
      {!batchesQuery.isLoading && !batches.length ? (
        <Card><EmptyState description="Chưa có mẻ sản xuất nào." /></Card>
      ) : (
        <div className="kanban-scroll">
          <div className="kanban-board">
            {WORKFLOW_STAGES.map(stage => {
              const stageBatches = batches.filter(b => b.currentStage === stage)
              return (
                <div className="kanban-column" key={stage}>
                  <div className="kanban-column-header">
                    <Space><Typography.Text strong>{STAGE_LABELS[stage]}</Typography.Text><span className="count-pill">{stageBatches.length}</span></Space>
                  </div>
                  <div className="kanban-column-body">
                    {stageBatches.map(batch => <BatchCard key={batch.id} batch={batch} order={ordersById.get(batch.orderId)} />)}
                    {!stageBatches.length && <div className="kanban-empty">Không có mẻ</div>}
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}
      <Row gutter={[12,12]} className="section-gap-sm">
        <Col span={24}><Typography.Text type="secondary">Mọi chuyển trạng thái được backend kiểm soát theo state machine: Tạo hình → Sấy & sửa → Vẽ → Tráng men → Chờ lò → Nung → QC → Đóng gói → Hoàn thành.</Typography.Text></Col>
      </Row>
    </div>
  )
}
