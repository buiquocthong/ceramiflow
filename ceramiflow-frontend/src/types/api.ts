export type PriorityLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
export type OrderStatus = 'CREATED' | 'AI_ANALYZING' | 'READY_FOR_REVIEW' | 'BATCH_CREATED' | 'EXTRACTION_FAILED' | 'CANCELLED'
export type BatchStatus = 'ACTIVE' | 'REWORK_REQUIRED' | 'COMPLETED' | 'CANCELLED'
export type StageType = 'FORMING' | 'DRYING_REPAIR' | 'PAINTING' | 'GLAZING' | 'READY_FOR_KILN' | 'FIRING' | 'QC' | 'PACKAGING' | 'COMPLETED'
export type StepStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED' | 'REWORK'
export type QcDecision = 'PASS' | 'REWORK_REQUIRED' | 'REJECT'
export type NotificationSeverity = 'INFO' | 'WARNING' | 'CRITICAL'
export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'SKIPPED'

export interface ExtractedSpecDto {
  productType: string | null
  quantity: number | null
  clayType: string | null
  glazeType: string | null
  patternDescription: string | null
  heightCm: number | null
  widthCm: number | null
  estimatedClayKg: number | null
  estimatedGlazeKg: number | null
  firingTemperatureC: number | null
  estimatedFiringHours: number | null
  deadlineDays: number | null
  priority: string | null
  needsReview: boolean
  reviewNote: string | null
  source: string | null
}

export interface OrderResponse {
  id: number
  orderCode: string
  rawDescription: string
  status: OrderStatus
  priority: PriorityLevel
  quantity: number
  deadline: string | null
  specification: ExtractedSpecDto | null
  createdAt: string
  updatedAt: string
}

export interface ConfirmOrderPayload {
  productType: string
  quantity: number
  clayType?: string | null
  glazeType?: string | null
  patternDescription?: string | null
  heightCm?: number | null
  widthCm?: number | null
  estimatedClayKg?: number | null
  estimatedGlazeKg?: number | null
  firingTemperatureC?: number | null
  estimatedFiringHours?: number | null
  deadlineDays?: number | null
  priority?: PriorityLevel
}

export interface WorkflowStepResponse {
  id: number
  stepType: StageType
  status: StepStatus
  sequence: number
  startedAt: string | null
  completedAt: string | null
  operator: string | null
  notes: string | null
}

export interface QcInspectionResponse {
  id: number
  quantityInspected: number
  quantityPassed: number
  quantityFailed: number
  defectType: string | null
  severity: string | null
  defectRate: number
  decision: QcDecision
  notes: string | null
  createdAt: string
}

export interface BatchResponse {
  id: number
  batchCode: string
  orderId: number
  orderCode: string
  quantity: number
  status: BatchStatus
  currentStage: StageType
  version: number
  startedAt: string
  estimatedCompletionAt: string | null
  completedAt: string | null
  steps: WorkflowStepResponse[]
  qcInspections: QcInspectionResponse[]
}

export interface BatchActionPayload {
  operator: string
  notes?: string | null
}

export interface QcInspectionPayload {
  quantityInspected: number
  quantityPassed: number
  quantityFailed: number
  defectType?: string | null
  severity?: string | null
  notes?: string | null
  operator?: string | null
}

export interface ReworkPayload {
  targetStage: StageType
  operator?: string | null
  notes?: string | null
}

export interface ProductionLogResponse {
  id: number
  eventType: string
  fromStatus: string | null
  toStatus: string | null
  message: string
  metadata: string | null
  createdBy: string | null
  createdAt: string
}

export interface NotificationResponse {
  id: number
  channel: string
  severity: NotificationSeverity
  message: string
  status: NotificationStatus
  attemptCount: number
  lastError: string | null
  sentAt: string | null
  createdAt: string
}

export interface DashboardSummary {
  totalOrders: number
  activeBatches: number
  completedBatches: number
  reworkBatches: number
  qcFailures: number
  stageDistribution: Record<string, number>
}

export interface BatchChangedEvent {
  batchId: number
  batchCode: string
  previousStatus: string | null
  newStatus: string
  eventType: string
  message: string
}
