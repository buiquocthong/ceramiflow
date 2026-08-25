import { useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { App, Button, Card, Form, Input, Result, Space, Steps, Typography } from 'antd'
import { ArrowLeftOutlined, CheckCircleOutlined, RobotOutlined, SendOutlined } from '@ant-design/icons'
import { useNavigate, useSearchParams } from 'react-router-dom'
import PageHeader from '../components/common/PageHeader'
import SpecificationForm from '../components/orders/SpecificationForm'
import { analyzeOrder, confirmOrder, createOrder, getOrder } from '../api/orders'
import { createBatchFromOrder } from '../api/batches'
import { getApiErrorMessage } from '../api/client'
import type { ConfirmOrderPayload, OrderResponse } from '../types/api'
import { useOperator } from '../hooks/useOperator'

const EXAMPLE = 'Đơn 200 Bình gốm họa tiết sen men lam cao 35cm, yêu cầu nung nhiệt độ cao 1280°C, hoàn thành trong 10 ngày'

export default function CreateOrderPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const [params, setParams] = useSearchParams()
  const orderIdParam = Number(params.get('orderId') || 0)
  const { operator } = useOperator()
  const [description, setDescription] = useState(EXAMPLE)
  const [currentOrder, setCurrentOrder] = useState<OrderResponse | null>(null)
  const [createdBatchId, setCreatedBatchId] = useState<number | null>(null)
  const [specForm] = Form.useForm<ConfirmOrderPayload>()

  const existingOrderQuery = useQuery({
    queryKey: ['order', orderIdParam],
    queryFn: () => getOrder(orderIdParam),
    enabled: orderIdParam > 0,
  })

  useEffect(() => {
    if (existingOrderQuery.data) {
      setCurrentOrder(existingOrderQuery.data)
      setDescription(existingOrderQuery.data.rawDescription)
    }
  }, [existingOrderQuery.data])

  const createMutation = useMutation({ mutationFn: createOrder })
  const analyzeMutation = useMutation({ mutationFn: analyzeOrder })
  const confirmMutation = useMutation({ mutationFn: ({ id, payload }: { id: number; payload: ConfirmOrderPayload }) => confirmOrder(id, payload) })
  const batchMutation = useMutation({ mutationFn: ({ orderId, actor }: { orderId: number; actor: string }) => createBatchFromOrder(orderId, actor) })

  const busy = createMutation.isPending || analyzeMutation.isPending || confirmMutation.isPending || batchMutation.isPending
  const step = createdBatchId ? 2 : currentOrder?.specification ? 1 : 0

  const analyze = async () => {
    if (!description.trim()) return message.warning('Hãy nhập mô tả đơn hàng.')
    try {
      let order = currentOrder
      if (!order) {
        order = await createMutation.mutateAsync(description.trim())
        setCurrentOrder(order)
        setParams({ orderId: String(order.id) }, { replace: true })
      }
      const analyzed = await analyzeMutation.mutateAsync(order.id)
      setCurrentOrder(analyzed)
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      message.success('AI đã bóc tách thông số. Hãy kiểm tra trước khi xác nhận.')
    } catch (e) {
      message.error(getApiErrorMessage(e))
    }
  }

  const confirmAndStart = async () => {
    if (!currentOrder) return
    try {
      const values = await specForm.validateFields()
      const confirmed = await confirmMutation.mutateAsync({ id: currentOrder.id, payload: values })
      setCurrentOrder(confirmed)
      const batch = await batchMutation.mutateAsync({ orderId: currentOrder.id, actor: operator })
      setCreatedBatchId(batch.id)
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      queryClient.invalidateQueries({ queryKey: ['batches'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      message.success(`Đã tạo mẻ ${batch.batchCode} và bắt đầu công đoạn Tạo hình.`)
    } catch (e) {
      message.error(getApiErrorMessage(e))
    }
  }

  const stepItems = useMemo(() => [
    { title: 'Nhập mô tả', description: 'Tiếp nhận đơn hàng' },
    { title: 'AI & xác nhận', description: 'Bóc tách JSON có kiểm duyệt' },
    { title: 'Khởi động mẻ', description: 'Tạo workflow sản xuất' },
  ], [])

  return (
    <div>
      <PageHeader title="Tạo đơn sản xuất" subtitle="AI bóc tách thông số; quyết định cuối cùng vẫn do người vận hành xác nhận." extra={<Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/orders')}>Danh sách đơn</Button>} />
      <Card className="section-gap-sm"><Steps current={step} items={stepItems} responsive /></Card>

      {createdBatchId ? (
        <Card className="section-gap">
          <Result
            status="success"
            icon={<CheckCircleOutlined />}
            title="Mẻ sản xuất đã được khởi tạo"
            subTitle="Workflow đã bắt đầu ở công đoạn Tạo hình. Thông báo Telegram được đưa vào hàng đợi của backend."
            extra={[
              <Button key="batch" type="primary" onClick={() => navigate(`/batches/${createdBatchId}`)}>Theo dõi mẻ</Button>,
              <Button key="new" onClick={() => { setCurrentOrder(null); setCreatedBatchId(null); setDescription(EXAMPLE); setParams({}, { replace: true }); specForm.resetFields(); }}>Tạo đơn khác</Button>,
            ]}
          />
        </Card>
      ) : (
        <>
          <Card title="1. Mô tả đơn hàng" className="section-gap">
            <Typography.Paragraph type="secondary">Có thể nhập tiếng Việt tự nhiên. Hệ thống sẽ trích xuất sản phẩm, số lượng, vật tư, nhiệt độ/thời gian nung, deadline và ưu tiên.</Typography.Paragraph>
            <Input.TextArea rows={6} value={description} onChange={e => setDescription(e.target.value)} disabled={!!currentOrder?.specification} maxLength={5000} showCount />
            <Space className="section-gap-sm" wrap>
              {!currentOrder?.specification && <Button type="primary" icon={<RobotOutlined />} loading={busy} onClick={analyze}>Phân tích với AI</Button>}
              {!currentOrder?.specification && <Button onClick={() => setDescription(EXAMPLE)}>Dùng dữ liệu demo</Button>}
            </Space>
          </Card>

          {currentOrder?.specification && (
            <Card title="2. Kiểm tra thông số AI" className="section-gap" extra={<Typography.Text type="secondary">{currentOrder.orderCode}</Typography.Text>}>
              <SpecificationForm key={`${currentOrder.id}-${currentOrder.updatedAt}`} spec={currentOrder.specification} form={specForm} />
              <Space className="section-gap" wrap>
                <Button type="primary" size="large" icon={<SendOutlined />} loading={busy} onClick={confirmAndStart}>Xác nhận & bắt đầu sản xuất</Button>
                <Button disabled={busy} onClick={() => { setCurrentOrder(null); setParams({}, { replace: true }); specForm.resetFields(); }}>Nhập đơn khác</Button>
              </Space>
            </Card>
          )}
        </>
      )}
    </div>
  )
}
