import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Alert, Button, Card, Space, Table, Typography } from 'antd'
import { EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/common/PageHeader'
import { listBatches } from '../api/batches'
import { listOrders } from '../api/orders'
import { BatchStatusTag, PriorityTag, StageTag } from '../utils/status'
import { formatDateTime } from '../utils/format'

export default function QcPage() {
  const navigate = useNavigate()
  const batchesQuery = useQuery({ queryKey: ['batches'], queryFn: listBatches })
  const ordersQuery = useQuery({ queryKey: ['orders'], queryFn: listOrders })
  const ordersById = useMemo(() => new Map((ordersQuery.data || []).map(o => [o.id, o])), [ordersQuery.data])
  const items = (batchesQuery.data || []).filter(b => b.currentStage === 'QC' || b.status === 'REWORK_REQUIRED')

  return (
    <div>
      <PageHeader title="Kiểm định chất lượng" subtitle="Xử lý các mẻ đang chờ QC và các trường hợp cần làm lại." />
      <Alert type="info" showIcon className="section-gap-sm" message="Quyết định QC nằm ở backend" description="Frontend chỉ gửi số lượng kiểm tra/đạt/lỗi và mô tả khuyết tật. Backend tính defect rate, áp dụng policy và phát cảnh báo Telegram khi cần." />
      <Card>
        <Table rowKey="id" loading={batchesQuery.isLoading} dataSource={items} locale={{ emptyText: 'Không có mẻ nào cần xử lý QC' }} columns={[
          { title: 'Mẻ', dataIndex: 'batchCode', render: v => <Typography.Text strong>{v}</Typography.Text> },
          { title: 'Sản phẩm', render: (_, r) => ordersById.get(r.orderId)?.specification?.productType || '—' },
          { title: 'Ưu tiên', render: (_, r) => { const o=ordersById.get(r.orderId); return o ? <PriorityTag value={o.priority} /> : '—' } },
          { title: 'Công đoạn', render: (_, r) => <StageTag value={r.currentStage} /> },
          { title: 'Trạng thái', render: (_, r) => <BatchStatusTag value={r.status} /> },
          { title: 'Bắt đầu', render: (_, r) => formatDateTime(r.startedAt) },
          { title: 'Lần QC', render: (_, r) => r.qcInspections.length },
          { title: '', render: (_, r) => <Button type="primary" icon={<EyeOutlined />} onClick={() => navigate(`/batches/${r.id}`)}>{r.status === 'REWORK_REQUIRED' ? 'Xử lý làm lại' : 'Thực hiện QC'}</Button> },
        ]} />
      </Card>
    </div>
  )
}
