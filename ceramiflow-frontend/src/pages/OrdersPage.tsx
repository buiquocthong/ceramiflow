import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { App, Button, Card, Input, Space, Table, Typography } from 'antd'
import { PlusOutlined, RobotOutlined, SearchOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/common/PageHeader'
import { analyzeOrder, listOrders } from '../api/orders'
import { getApiErrorMessage } from '../api/client'
import { OrderStatusTag, PriorityTag } from '../utils/status'
import { formatDate, formatDateTime } from '../utils/format'

export default function OrdersPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const [search, setSearch] = useState('')
  const { data = [], isLoading } = useQuery({ queryKey: ['orders'], queryFn: listOrders })
  const analyzeMutation = useMutation({
    mutationFn: analyzeOrder,
    onSuccess: (order) => {
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      message.success(`AI đã phân tích ${order.orderCode}`)
      navigate(`/orders/new?orderId=${order.id}`)
    },
    onError: (e) => message.error(getApiErrorMessage(e)),
  })

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return data
    return data.filter(o => [o.orderCode, o.rawDescription, o.specification?.productType].some(v => v?.toLowerCase().includes(q)))
  }, [data, search])

  return (
    <div>
      <PageHeader title="Đơn hàng" subtitle="Tiếp nhận mô tả tự nhiên và kiểm soát kết quả bóc tách của AI." extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/orders/new')}>Tạo đơn</Button>} />
      <Card>
        <Input prefix={<SearchOutlined />} allowClear placeholder="Tìm mã đơn, sản phẩm, mô tả..." value={search} onChange={e => setSearch(e.target.value)} style={{ maxWidth: 420, marginBottom: 16 }} />
        <Table
          rowKey="id"
          loading={isLoading}
          dataSource={filtered}
          scroll={{ x: 1000 }}
          columns={[
            { title: 'Mã đơn', dataIndex: 'orderCode', render: v => <Typography.Text strong>{v}</Typography.Text> },
            { title: 'Sản phẩm', render: (_, r) => r.specification?.productType || <Typography.Text type="secondary">Chưa phân tích</Typography.Text> },
            { title: 'SL', dataIndex: 'quantity', width: 70 },
            { title: 'Ưu tiên', render: (_, r) => <PriorityTag value={r.priority} /> },
            { title: 'Hạn', render: (_, r) => formatDate(r.deadline) },
            { title: 'Trạng thái', render: (_, r) => <OrderStatusTag value={r.status} /> },
            { title: 'Cập nhật', render: (_, r) => formatDateTime(r.updatedAt) },
            {
              title: 'Thao tác', fixed: 'right', width: 210,
              render: (_, r) => (
                <Space>
                  {(r.status === 'CREATED' || r.status === 'EXTRACTION_FAILED') && <Button size="small" icon={<RobotOutlined />} loading={analyzeMutation.isPending && analyzeMutation.variables === r.id} onClick={() => analyzeMutation.mutate(r.id)}>Phân tích AI</Button>}
                  {r.status === 'READY_FOR_REVIEW' && <Button size="small" type="primary" onClick={() => navigate(`/orders/new?orderId=${r.id}`)}>Xác nhận</Button>}
                  {r.status === 'BATCH_CREATED' && <Button size="small" onClick={() => navigate('/production')}>Xem sản xuất</Button>}
                </Space>
              ),
            },
          ]}
        />
      </Card>
    </div>
  )
}
