import { api } from './client'
import type { DashboardSummary } from '../types/api'

export async function getDashboardSummary(): Promise<DashboardSummary> {
  const { data } = await api.get<DashboardSummary>('/dashboard/summary')
  return data
}
