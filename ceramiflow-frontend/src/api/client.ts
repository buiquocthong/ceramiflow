import axios from 'axios'

export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

export const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  timeout: 30_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface ApiErrorBody {
  timestamp?: string
  status?: number
  error?: string
  message?: string
  fields?: Record<string, string>
}

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    const data = error.response?.data
    if (data?.fields) {
      return Object.entries(data.fields)
        .map(([field, message]) => `${field}: ${message}`)
        .join(' • ')
    }
    return data?.message || data?.error || error.message
  }
  return error instanceof Error ? error.message : 'Đã xảy ra lỗi không xác định'
}
