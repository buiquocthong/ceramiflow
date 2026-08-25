import { Tag } from 'antd'
import type { BatchStatus, NotificationSeverity, NotificationStatus, OrderStatus, PriorityLevel, QcDecision, StageType, StepStatus } from '../types/api'
import { batchStatusLabel, notificationStatusLabel, orderStatusLabel, priorityLabel, qcDecisionLabel, severityLabel } from './format'
import { STAGE_LABELS } from '../constants/workflow'

const priorityColor: Record<PriorityLevel, string> = { LOW: 'default', MEDIUM: 'blue', HIGH: 'orange', URGENT: 'red' }
const orderColor: Record<OrderStatus, string> = { CREATED: 'default', AI_ANALYZING: 'processing', READY_FOR_REVIEW: 'gold', BATCH_CREATED: 'success', EXTRACTION_FAILED: 'error', CANCELLED: 'default' }
const batchColor: Record<BatchStatus, string> = { ACTIVE: 'processing', REWORK_REQUIRED: 'error', COMPLETED: 'success', CANCELLED: 'default' }
const stepColor: Record<StepStatus, string> = { PENDING: 'default', IN_PROGRESS: 'processing', COMPLETED: 'success', SKIPPED: 'default', REWORK: 'error' }
const notificationColor: Record<NotificationStatus, string> = { PENDING: 'processing', SENT: 'success', FAILED: 'error', SKIPPED: 'default' }
const severityColor: Record<NotificationSeverity, string> = { INFO: 'blue', WARNING: 'orange', CRITICAL: 'red' }
const decisionColor: Record<QcDecision, string> = { PASS: 'green', REWORK_REQUIRED: 'orange', REJECT: 'red' }

export const PriorityTag = ({ value }: { value: PriorityLevel }) => <Tag color={priorityColor[value]}>{priorityLabel[value]}</Tag>
export const OrderStatusTag = ({ value }: { value: OrderStatus }) => <Tag color={orderColor[value]}>{orderStatusLabel[value]}</Tag>
export const BatchStatusTag = ({ value }: { value: BatchStatus }) => <Tag color={batchColor[value]}>{batchStatusLabel[value]}</Tag>
export const StepStatusTag = ({ value }: { value: StepStatus }) => <Tag color={stepColor[value]}>{value === 'IN_PROGRESS' ? 'Đang thực hiện' : value === 'COMPLETED' ? 'Hoàn thành' : value === 'REWORK' ? 'Làm lại' : value === 'SKIPPED' ? 'Bỏ qua' : 'Chờ'}</Tag>
export const StageTag = ({ value }: { value: StageType }) => <Tag color={value === 'COMPLETED' ? 'green' : value === 'QC' ? 'purple' : value === 'FIRING' ? 'volcano' : 'geekblue'}>{STAGE_LABELS[value]}</Tag>
export const NotificationStatusTag = ({ value }: { value: NotificationStatus }) => <Tag color={notificationColor[value]}>{notificationStatusLabel[value]}</Tag>
export const SeverityTag = ({ value }: { value: NotificationSeverity }) => <Tag color={severityColor[value]}>{severityLabel[value]}</Tag>
export const QcDecisionTag = ({ value }: { value: QcDecision }) => <Tag color={decisionColor[value]}>{qcDecisionLabel[value]}</Tag>
