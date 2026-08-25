import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { App, Alert, Button, Card, Col, Descriptions, Form, Input, InputNumber, Modal, Progress, Row, Select, Space, Steps, Table, Tabs, Typography } from 'antd'
import { ArrowLeftOutlined, CheckOutlined, ExperimentOutlined, RetweetOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import PageHeader from '../components/common/PageHeader'
import { completeCurrentStep, getBatch, getBatchLogs, getBatchNotifications, startRework, submitQc } from '../api/batches'
import { getOrder } from '../api/orders'
import { getApiErrorMessage } from '../api/client'
import { REWORK_TARGETS, STAGE_LABELS, WORKFLOW_STAGES, stageProgress } from '../constants/workflow'
import { BatchStatusTag, NotificationStatusTag, QcDecisionTag, SeverityTag, StageTag, StepStatusTag } from '../utils/status'
import { formatDate, formatDateTime, formatNumber } from '../utils/format'
import type { QcInspectionPayload, ReworkPayload, StageType } from '../types/api'
import { useOperator } from '../hooks/useOperator'

export default function BatchDetailPage() {
  const { id } = useParams()
  const batchId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const { operator, setOperator } = useOperator()
  const [completeOpen, setCompleteOpen] = useState(false)
  const [qcOpen, setQcOpen] = useState(false)
  const [reworkOpen, setReworkOpen] = useState(false)
  const [completeForm] = Form.useForm<{ operator: string; notes?: string }>()
  const [qcForm] = Form.useForm<QcInspectionPayload>()
  const [reworkForm] = Form.useForm<ReworkPayload>()

  const batchQuery = useQuery({ queryKey: ['batch', batchId], queryFn: () => getBatch(batchId), enabled: !!batchId })
  const orderQuery = useQuery({ queryKey: ['order', batchQuery.data?.orderId], queryFn: () => getOrder(batchQuery.data!.orderId), enabled: !!batchQuery.data?.orderId })
  const logsQuery = useQuery({ queryKey: ['batch-logs', batchId], queryFn: () => getBatchLogs(batchId), enabled: !!batchId })
  const notificationsQuery = useQuery({ queryKey: ['batch-notifications', batchId], queryFn: () => getBatchNotifications(batchId), enabled: !!batchId, refetchInterval: 5000 })
  const batch = batchQuery.data
  const order = orderQuery.data

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['batch', batchId] })
    queryClient.invalidateQueries({ queryKey: ['batch-logs', batchId] })
    queryClient.invalidateQueries({ queryKey: ['batch-notifications', batchId] })
    queryClient.invalidateQueries({ queryKey: ['batches'] })
    queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
  }

  const completeMutation = useMutation({
    mutationFn: (payload: { operator: string; notes?: string }) => completeCurrentStep(batchId, payload),
    onSuccess: (b) => { message.success(`Đã chuyển sang ${STAGE_LABELS[b.currentStage]}`); setCompleteOpen(false); refresh() },
    onError: e => message.error(getApiErrorMessage(e)),
  })
  const qcMutation = useMutation({
    mutationFn: (payload: QcInspectionPayload) => submitQc(batchId, payload),
    onSuccess: (qc) => { message.success(`QC: ${qc.decision} – tỷ lệ lỗi ${qc.defectRate}%`); setQcOpen(false); refresh() },
    onError: e => message.error(getApiErrorMessage(e)),
  })
  const reworkMutation = useMutation({
    mutationFn: (payload: ReworkPayload) => startRework(batchId, payload),
    onSuccess: b => { message.success(`Đã đưa ${b.batchCode} về công đoạn ${STAGE_LABELS[b.currentStage]}`); setReworkOpen(false); refresh() },
    onError: e => message.error(getApiErrorMessage(e)),
  })

  const timelineItems = useMemo(() => {
    if (!batch) return []
    return batch.steps.slice(0, 9).map(step => ({
      title: STAGE_LABELS[step.stepType],
      description: step.completedAt ? `Xong ${formatDateTime(step.completedAt)}` : step.startedAt ? `Bắt đầu ${formatDateTime(step.startedAt)}` : undefined,
      status: step.status === 'COMPLETED' ? 'finish' as const : step.status === 'IN_PROGRESS' || (batch.status === 'REWORK_REQUIRED' && step.stepType === 'QC') ? 'process' as const : step.status === 'REWORK' ? 'error' as const : 'wait' as const,
    }))
  }, [batch])

  if (!batch && batchQuery.isLoading) return <Card loading />
  if (!batch) return <Alert type="error" message="Không tìm thấy mẻ sản xuất" showIcon />

  const canComplete = batch.status === 'ACTIVE' && batch.currentStage !== 'QC' && batch.currentStage !== 'COMPLETED'
  const needsQc = batch.status === 'ACTIVE' && batch.currentStage === 'QC'
  const needsRework = batch.status === 'REWORK_REQUIRED'

  return (
    <div>
      <PageHeader
        title={batch.batchCode}
        subtitle={`${order?.specification?.productType || batch.orderCode} • ${batch.quantity} sản phẩm`}
        extra={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/production')}>Kanban</Button><BatchStatusTag value={batch.status} /></Space>}
      />

      {needsRework && <Alert className="section-gap-sm" type="error" showIcon message="QC phát hiện lỗi – cần chọn công đoạn làm lại" description="Batch tạm dừng cho đến khi người phụ trách xác nhận điểm quay lại trong quy trình." action={<Button danger icon={<RetweetOutlined />} onClick={() => setReworkOpen(true)}>Chọn công đoạn làm lại</Button>} />}

      <Row gutter={[16,16]} className="section-gap-sm">
        <Col xs={24} lg={8}>
          <Card title="Trạng thái hiện tại" className="full-height-card">
            <Space direction="vertical" size={14} className="w-full">
              <StageTag value={batch.currentStage} />
              <Progress percent={stageProgress(batch.currentStage)} />
              <Descriptions column={1} size="small">
                <Descriptions.Item label="Đơn hàng">{batch.orderCode}</Descriptions.Item>
                <Descriptions.Item label="Số lượng">{batch.quantity}</Descriptions.Item>
                <Descriptions.Item label="Bắt đầu">{formatDateTime(batch.startedAt)}</Descriptions.Item>
                <Descriptions.Item label="Dự kiến xong">{formatDateTime(batch.estimatedCompletionAt)}</Descriptions.Item>
                <Descriptions.Item label="Phiên bản dữ liệu">v{batch.version}</Descriptions.Item>
              </Descriptions>
              {canComplete && <Button block size="large" type="primary" icon={<CheckOutlined />} onClick={() => { completeForm.setFieldsValue({ operator }); setCompleteOpen(true) }}>Hoàn thành {STAGE_LABELS[batch.currentStage]}</Button>}
              {needsQc && <Button block size="large" type="primary" icon={<ExperimentOutlined />} onClick={() => { qcForm.setFieldsValue({ operator, quantityInspected: batch.quantity, quantityPassed: batch.quantity, quantityFailed: 0 }); setQcOpen(true) }}>Nhập kết quả QC</Button>}
            </Space>
          </Card>
        </Col>
        <Col xs={24} lg={16}>
          <Card title="Tiến trình công đoạn" className="full-height-card">
            <Steps current={Math.max(0, WORKFLOW_STAGES.indexOf(batch.currentStage))} items={timelineItems} responsive />
          </Card>
        </Col>
      </Row>

      <Tabs className="section-gap" items={[
        {
          key: 'spec', label: 'Thông số sản xuất', children: (
            <Card>
              {order?.specification ? <Descriptions bordered column={{ xs: 1, sm: 2, lg: 3 }}>
                <Descriptions.Item label="Sản phẩm">{order.specification.productType || '—'}</Descriptions.Item>
                <Descriptions.Item label="Ưu tiên">{order.priority}</Descriptions.Item>
                <Descriptions.Item label="Deadline">{formatDate(order.deadline)}</Descriptions.Item>
                <Descriptions.Item label="Loại đất">{order.specification.clayType || '—'}</Descriptions.Item>
                <Descriptions.Item label="Loại men">{order.specification.glazeType || '—'}</Descriptions.Item>
                <Descriptions.Item label="Họa tiết">{order.specification.patternDescription || '—'}</Descriptions.Item>
                <Descriptions.Item label="Kích thước">{formatNumber(order.specification.heightCm, ' cm')} × {formatNumber(order.specification.widthCm, ' cm')}</Descriptions.Item>
                <Descriptions.Item label="Đất ước tính">{formatNumber(order.specification.estimatedClayKg, ' kg')}</Descriptions.Item>
                <Descriptions.Item label="Men ước tính">{formatNumber(order.specification.estimatedGlazeKg, ' kg')}</Descriptions.Item>
                <Descriptions.Item label="Nhiệt độ nung">{formatNumber(order.specification.firingTemperatureC, '°C')}</Descriptions.Item>
                <Descriptions.Item label="Thời gian nung">{formatNumber(order.specification.estimatedFiringHours, ' giờ')}</Descriptions.Item>
                <Descriptions.Item label="Nguồn">{order.specification.source || '—'}</Descriptions.Item>
              </Descriptions> : <Typography.Text type="secondary">Không có specification.</Typography.Text>}
            </Card>
          )
        },
        {
          key: 'steps', label: `Công đoạn (${batch.steps.length})`, children: (
            <Card><Table rowKey="id" pagination={false} dataSource={batch.steps} columns={[
              { title: '#', dataIndex: 'sequence', width: 60 },
              { title: 'Công đoạn', render: (_, r) => <StageTag value={r.stepType} /> },
              { title: 'Trạng thái', render: (_, r) => <StepStatusTag value={r.status} /> },
              { title: 'Bắt đầu', render: (_, r) => formatDateTime(r.startedAt) },
              { title: 'Hoàn thành', render: (_, r) => formatDateTime(r.completedAt) },
              { title: 'Người thao tác', dataIndex: 'operator', render: v => v || '—' },
              { title: 'Ghi chú', dataIndex: 'notes', render: v => v || '—' },
            ]} /></Card>
          )
        },
        {
          key: 'qc', label: `QC (${batch.qcInspections.length})`, children: (
            <Card><Table rowKey="id" pagination={false} locale={{ emptyText: 'Chưa có lần kiểm định QC' }} dataSource={batch.qcInspections} columns={[
              { title: 'Thời gian', render: (_, r) => formatDateTime(r.createdAt) },
              { title: 'Kiểm tra', dataIndex: 'quantityInspected' },
              { title: 'Đạt', dataIndex: 'quantityPassed' },
              { title: 'Lỗi', dataIndex: 'quantityFailed' },
              { title: 'Tỷ lệ lỗi', render: (_, r) => `${r.defectRate}%` },
              { title: 'Loại lỗi', dataIndex: 'defectType', render: v => v || '—' },
              { title: 'Kết luận', render: (_, r) => <QcDecisionTag value={r.decision} /> },
            ]} /></Card>
          )
        },
        {
          key: 'logs', label: 'Nhật ký', children: (
            <Card><Table rowKey="id" loading={logsQuery.isLoading} dataSource={logsQuery.data || []} columns={[
              { title: 'Thời gian', render: (_, r) => formatDateTime(r.createdAt), width: 160 },
              { title: 'Sự kiện', dataIndex: 'eventType', width: 180 },
              { title: 'Chuyển trạng thái', render: (_, r) => r.fromStatus || r.toStatus ? `${r.fromStatus || '—'} → ${r.toStatus || '—'}` : '—' },
              { title: 'Nội dung', dataIndex: 'message' },
              { title: 'Người thực hiện', dataIndex: 'createdBy', render: v => v || 'system' },
            ]} /></Card>
          )
        },
        {
          key: 'notifications', label: `Thông báo (${notificationsQuery.data?.length || 0})`, children: (
            <Card><Alert type="info" showIcon message="Trạng thái gửi Telegram được backend xử lý bất đồng bộ; trang tự làm mới mỗi 5 giây." className="section-gap-sm" />
              <Table rowKey="id" loading={notificationsQuery.isLoading} dataSource={notificationsQuery.data || []} columns={[
                { title: 'Thời gian', render: (_, r) => formatDateTime(r.createdAt), width: 160 },
                { title: 'Mức độ', render: (_, r) => <SeverityTag value={r.severity} /> },
                { title: 'Nội dung', dataIndex: 'message' },
                { title: 'Trạng thái', render: (_, r) => <NotificationStatusTag value={r.status} /> },
                { title: 'Thử gửi', dataIndex: 'attemptCount', width: 90 },
                { title: 'Lỗi', dataIndex: 'lastError', render: v => v ? <Typography.Text type="danger">{v}</Typography.Text> : '—' },
              ]} />
            </Card>
          )
        },
      ]} />

      <Modal title={`Hoàn thành ${STAGE_LABELS[batch.currentStage]}`} open={completeOpen} onCancel={() => setCompleteOpen(false)} onOk={() => completeForm.submit()} confirmLoading={completeMutation.isPending} okText="Xác nhận hoàn thành">
        <Form form={completeForm} layout="vertical" onFinish={v => { setOperator(v.operator); completeMutation.mutate(v) }}>
          <Form.Item name="operator" label="Người thực hiện" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="notes" label="Ghi chú"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>

      <Modal title="Kiểm định chất lượng (QC)" width={720} open={qcOpen} onCancel={() => setQcOpen(false)} onOk={() => qcForm.submit()} confirmLoading={qcMutation.isPending} okText="Gửi kết quả QC">
        <Alert type="warning" showIcon message="Backend tự tính tỷ lệ lỗi và quyết định PASS / REWORK_REQUIRED / REJECT theo ngưỡng cấu hình." className="section-gap-sm" />
        <Form form={qcForm} layout="vertical" onFinish={v => { setOperator(v.operator || operator); qcMutation.mutate(v) }}>
          <Row gutter={12}>
            <Col span={8}><Form.Item name="quantityInspected" label="Số kiểm tra" rules={[{ required: true }]}><InputNumber min={1} max={batch.quantity} className="w-full" /></Form.Item></Col>
            <Col span={8}><Form.Item name="quantityPassed" label="Số đạt" rules={[{ required: true }]}><InputNumber min={0} className="w-full" /></Form.Item></Col>
            <Col span={8}><Form.Item name="quantityFailed" label="Số lỗi" rules={[{ required: true }]}><InputNumber min={0} className="w-full" /></Form.Item></Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}><Form.Item name="defectType" label="Loại lỗi"><Input placeholder="Nứt men, cong vênh..." /></Form.Item></Col>
            <Col span={12}><Form.Item name="severity" label="Mức độ"><Select allowClear options={[{value:'LOW',label:'Thấp'},{value:'MEDIUM',label:'Trung bình'},{value:'HIGH',label:'Cao'},{value:'CRITICAL',label:'Khẩn cấp'}]} /></Form.Item></Col>
          </Row>
          <Form.Item name="operator" label="Người QC"><Input /></Form.Item>
          <Form.Item name="notes" label="Ghi chú"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>

      <Modal title="Chọn công đoạn làm lại" open={reworkOpen} onCancel={() => setReworkOpen(false)} onOk={() => reworkForm.submit()} confirmLoading={reworkMutation.isPending} okText="Bắt đầu làm lại">
        <Form form={reworkForm} layout="vertical" initialValues={{ operator }} onFinish={v => { setOperator(v.operator || operator); reworkMutation.mutate(v) }}>
          <Form.Item name="targetStage" label="Quay lại công đoạn" rules={[{ required: true }]}><Select options={REWORK_TARGETS.map(stage => ({ value: stage, label: STAGE_LABELS[stage] }))} /></Form.Item>
          <Form.Item name="operator" label="Người phụ trách"><Input /></Form.Item>
          <Form.Item name="notes" label="Lý do / hướng xử lý"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
