import { api } from './client'
import type { ConfirmOrderPayload, OrderResponse } from '../types/api'

export async function listOrders(): Promise<OrderResponse[]> {
  const { data } = await api.get<OrderResponse[]>('/orders')
  return data
}

export async function getOrder(id: number): Promise<OrderResponse> {
  const { data } = await api.get<OrderResponse>(`/orders/${id}`)
  return data
}

export async function createOrder(description: string): Promise<OrderResponse> {
  const { data } = await api.post<OrderResponse>('/orders', { description })
  return data
}

export async function analyzeOrder(id: number): Promise<OrderResponse> {
  const { data } = await api.post<OrderResponse>(`/orders/${id}/analyze`)
  return data
}

export async function confirmOrder(id: number, payload: ConfirmOrderPayload): Promise<OrderResponse> {
  const { data } = await api.post<OrderResponse>(`/orders/${id}/confirm`, payload)
  return data
}
