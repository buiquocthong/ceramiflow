import type { StageType } from '../types/api'

export const WORKFLOW_STAGES: StageType[] = [
  'FORMING',
  'DRYING_REPAIR',
  'PAINTING',
  'GLAZING',
  'READY_FOR_KILN',
  'FIRING',
  'QC',
  'PACKAGING',
  'COMPLETED',
]

export const REWORK_TARGETS: StageType[] = [
  'FORMING',
  'DRYING_REPAIR',
  'PAINTING',
  'GLAZING',
  'READY_FOR_KILN',
  'FIRING',
]

export const STAGE_LABELS: Record<StageType, string> = {
  FORMING: 'Tạo hình',
  DRYING_REPAIR: 'Phơi sấy & sửa mộc',
  PAINTING: 'Vẽ họa tiết',
  GLAZING: 'Tráng men',
  READY_FOR_KILN: 'Chờ vào lò',
  FIRING: 'Nung lò',
  QC: 'Kiểm định QC',
  PACKAGING: 'Đóng gói',
  COMPLETED: 'Hoàn thành',
}

export const STAGE_SHORT_LABELS: Record<StageType, string> = {
  FORMING: 'Tạo hình',
  DRYING_REPAIR: 'Sấy & sửa',
  PAINTING: 'Vẽ',
  GLAZING: 'Tráng men',
  READY_FOR_KILN: 'Chờ lò',
  FIRING: 'Nung',
  QC: 'QC',
  PACKAGING: 'Đóng gói',
  COMPLETED: 'Hoàn thành',
}

export const stageProgress = (stage: StageType) => {
  const index = WORKFLOW_STAGES.indexOf(stage)
  if (index < 0) return 0
  return Math.round((index / (WORKFLOW_STAGES.length - 1)) * 100)
}
