import { useEffect, useMemo } from 'react'
import { Alert, Col, Form, Input, InputNumber, Row, Select, Space, Typography } from 'antd'
import type { FormInstance } from 'antd'
import type { ConfirmOrderPayload, ExtractedSpecDto, PriorityLevel } from '../../types/api'

export default function SpecificationForm({ spec, form }: { spec: ExtractedSpecDto; form: FormInstance<ConfirmOrderPayload> }) {
  const initial = useMemo<ConfirmOrderPayload>(() => ({
    productType: spec.productType || '',
    quantity: spec.quantity || 1,
    clayType: spec.clayType,
    glazeType: spec.glazeType,
    patternDescription: spec.patternDescription,
    heightCm: spec.heightCm,
    widthCm: spec.widthCm,
    estimatedClayKg: spec.estimatedClayKg,
    estimatedGlazeKg: spec.estimatedGlazeKg,
    firingTemperatureC: spec.firingTemperatureC,
    estimatedFiringHours: spec.estimatedFiringHours,
    deadlineDays: spec.deadlineDays,
    priority: (spec.priority as PriorityLevel) || 'MEDIUM',
  }), [spec])

  useEffect(() => {
    form.setFieldsValue(initial)
  }, [form, initial])

  return (
    <>
      {spec.needsReview && (
        <Alert
          type="warning"
          showIcon
          className="section-gap"
          message="AI đề nghị kiểm tra lại thông số"
          description={spec.reviewNote || 'Một số dữ liệu là ước tính hoặc chưa đủ chắc chắn. Hãy xác nhận trước khi đưa vào sản xuất.'}
        />
      )}
      <div className="ai-source-row">
        <Typography.Text type="secondary">Nguồn phân tích: </Typography.Text>
        <Typography.Text strong>{spec.source || 'AI / fallback'}</Typography.Text>
      </div>
      <Form form={form} layout="vertical" initialValues={initial} preserve={false}>
        <Row gutter={16}>
          <Col xs={24} md={12}><Form.Item name="productType" label="Loại sản phẩm" rules={[{ required: true }]}><Input placeholder="Bình gốm" /></Form.Item></Col>
          <Col xs={24} md={6}><Form.Item name="quantity" label="Số lượng" rules={[{ required: true }]}><InputNumber min={1} className="w-full" /></Form.Item></Col>
          <Col xs={24} md={6}><Form.Item name="priority" label="Mức ưu tiên"><Select options={['LOW','MEDIUM','HIGH','URGENT'].map(value => ({ value, label: value }))} /></Form.Item></Col>
        </Row>
        <Row gutter={16}>
          <Col xs={24} md={8}><Form.Item name="clayType" label="Loại đất"><Input placeholder="Đất sét / cao lanh..." /></Form.Item></Col>
          <Col xs={24} md={8}><Form.Item name="glazeType" label="Loại men"><Input placeholder="Men lam" /></Form.Item></Col>
          <Col xs={24} md={8}><Form.Item name="patternDescription" label="Họa tiết"><Input placeholder="Hoa sen" /></Form.Item></Col>
        </Row>
        <Row gutter={16}>
          <Col xs={12} md={6}><Form.Item name="heightCm" label="Cao (cm)"><InputNumber min={0.1} className="w-full" /></Form.Item></Col>
          <Col xs={12} md={6}><Form.Item name="widthCm" label="Rộng (cm)"><InputNumber min={0.1} className="w-full" /></Form.Item></Col>
          <Col xs={12} md={6}><Form.Item name="estimatedClayKg" label="Đất ước tính (kg)"><InputNumber min={0} precision={2} className="w-full" /></Form.Item></Col>
          <Col xs={12} md={6}><Form.Item name="estimatedGlazeKg" label="Men ước tính (kg)"><InputNumber min={0} precision={2} className="w-full" /></Form.Item></Col>
        </Row>
        <Row gutter={16}>
          <Col xs={24} md={8}><Form.Item name="firingTemperatureC" label="Nhiệt độ nung (°C)"><InputNumber min={600} max={1500} className="w-full" /></Form.Item></Col>
          <Col xs={24} md={8}><Form.Item name="estimatedFiringHours" label="Thời gian nung ước tính (giờ)"><InputNumber min={0.1} precision={1} className="w-full" /></Form.Item></Col>
          <Col xs={24} md={8}><Form.Item name="deadlineDays" label="Thời hạn (ngày)"><InputNumber min={1} className="w-full" /></Form.Item></Col>
        </Row>
      </Form>
      <Space size={8} wrap>
        <Typography.Text type="secondary">Các số liệu vật tư/thời gian do AI đề xuất chỉ mang tính ước tính và được người dùng xác nhận trước khi tạo mẻ.</Typography.Text>
      </Space>
    </>
  )
}
