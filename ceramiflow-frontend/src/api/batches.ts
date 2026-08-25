import { api } from './client'
import type {
  BatchActionPayload,
  BatchResponse,
  NotificationResponse,
  ProductionLogResponse,
  QcInspectionPayload,
  QcInspectionResponse,
  ReworkPayload,
} from '../types/api'

export async function listBatches(): Promise<BatchResponse[]> {
  const { data } = await api.get<BatchResponse[]>('/batches')
  return data
}

export async function getBatch(id: number): Promise<BatchResponse> {
  const { data } = await api.get<BatchResponse>(`/batches/${id}`)
  return data
}

export async function createBatchFromOrder(orderId: number, actor: string): Promise<BatchResponse> {
  const { data } = await api.post<BatchResponse>(`/batches/from-order/${orderId}`, null, {
    params: { actor },
  })
  return data
}

export async function completeCurrentStep(id: number, payload: BatchActionPayload): Promise<BatchResponse> {
  const { data } = await api.post<BatchResponse>(`/batches/${id}/steps/complete`, payload)
  return data
}

export async function submitQc(id: number, payload: QcInspectionPayload): Promise<QcInspectionResponse> {
  const { data } = await api.post<QcInspectionResponse>(`/batches/${id}/qc`, payload)
  return data
}

export async function startRework(id: number, payload: ReworkPayload): Promise<BatchResponse> {
  const { data } = await api.post<BatchResponse>(`/batches/${id}/rework`, payload)
  return data
}

export async function getBatchLogs(id: number): Promise<ProductionLogResponse[]> {
  const { data } = await api.get<ProductionLogResponse[]>(`/batches/${id}/logs`)
  return data
}

export async function getBatchNotifications(id: number): Promise<NotificationResponse[]> {
  const { data } = await api.get<NotificationResponse[]>(`/batches/${id}/notifications`)
  return data
}
