import dayjs from 'dayjs'
import type { BatchStatus, NotificationSeverity, NotificationStatus, OrderStatus, PriorityLevel, QcDecision, StageType } from '../types/api'
import { STAGE_LABELS } from '../constants/workflow'

export const formatDateTime = (value?: string | null) => value ? dayjs(value).format('DD/MM/YYYY HH:mm') : '—'
export const formatDate = (value?: string | null) => value ? dayjs(value).format('DD/MM/YYYY') : '—'
export const formatNumber = (value?: number | null, suffix = '') => value == null ? '—' : `${new Intl.NumberFormat('vi-VN').format(value)}${suffix}`

export const priorityLabel: Record<PriorityLevel, string> = {
  LOW: 'Thấp', MEDIUM: 'Trung bình', HIGH: 'Cao', URGENT: 'Khẩn cấp',
}

export const orderStatusLabel: Record<OrderStatus, string> = {
  CREATED: 'Mới tạo',
  AI_ANALYZING: 'AI đang phân tích',
  READY_FOR_REVIEW: 'Chờ xác nhận',
  BATCH_CREATED: 'Đã tạo mẻ',
  EXTRACTION_FAILED: 'Phân tích lỗi',
  CANCELLED: 'Đã hủy',
}

export const batchStatusLabel: Record<BatchStatus, string> = {
  ACTIVE: 'Đang sản xuất',
  REWORK_REQUIRED: 'Cần làm lại',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
}

export const qcDecisionLabel: Record<QcDecision, string> = {
  PASS: 'Đạt',
  REWORK_REQUIRED: 'Cần làm lại',
  REJECT: 'Loại / làm lại',
}

export const notificationStatusLabel: Record<NotificationStatus, string> = {
  PENDING: 'Đang chờ', SENT: 'Đã gửi', FAILED: 'Gửi lỗi', SKIPPED: 'Bỏ qua',
}

export const severityLabel: Record<NotificationSeverity, string> = {
  INFO: 'Thông tin', WARNING: 'Cảnh báo', CRITICAL: 'Khẩn cấp',
}

export const stageLabel = (stage?: StageType | null) => stage ? STAGE_LABELS[stage] : '—'
