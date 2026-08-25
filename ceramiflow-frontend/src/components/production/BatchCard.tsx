import { Card, Progress, Space, Typography } from 'antd'
import { ClockCircleOutlined, ShoppingOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import type { BatchResponse, OrderResponse } from '../../types/api'
import { BatchStatusTag, PriorityTag } from '../../utils/status'
import { formatDate } from '../../utils/format'
import { stageProgress } from '../../constants/workflow'

export default function BatchCard({ batch, order }: { batch: BatchResponse; order?: OrderResponse }) {
  const navigate = useNavigate()
  const percent = stageProgress(batch.currentStage)

  return (
    <Card hoverable size="small" className="kanban-card" onClick={() => navigate(`/batches/${batch.id}`)}>
      <div className="kanban-card-head">
        <Typography.Text strong>{batch.batchCode}</Typography.Text>
        {order && <PriorityTag value={order.priority} />}
      </div>
      <Typography.Text className="kanban-product" ellipsis={{ tooltip: order?.specification?.productType || order?.rawDescription }}>
        {order?.specification?.productType || 'Sản phẩm gốm'}
      </Typography.Text>
      <Space direction="vertical" size={4} className="kanban-meta">
        <Typography.Text type="secondary"><ShoppingOutlined /> {batch.quantity} sản phẩm</Typography.Text>
        <Typography.Text type="secondary"><ClockCircleOutlined /> Hạn: {formatDate(order?.deadline)}</Typography.Text>
      </Space>
      <Progress percent={percent} size="small" showInfo={false} />
      <div className="kanban-footer"><BatchStatusTag value={batch.status} /><Typography.Text type="secondary">{percent}%</Typography.Text></div>
    </Card>
  )
}
