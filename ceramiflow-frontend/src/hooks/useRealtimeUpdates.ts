import { useEffect } from 'react'
import { App } from 'antd'
import { useQueryClient } from '@tanstack/react-query'
import { API_BASE_URL } from '../api/client'
import type { BatchChangedEvent } from '../types/api'

export function useRealtimeUpdates() {
  const queryClient = useQueryClient()
  const { message } = App.useApp()

  useEffect(() => {
    const source = new EventSource(`${API_BASE_URL}/api/stream`)

    const onBatchUpdated = (raw: MessageEvent<string>) => {
      try {
        const event = JSON.parse(raw.data) as BatchChangedEvent
        queryClient.invalidateQueries({ queryKey: ['batches'] })
        queryClient.invalidateQueries({ queryKey: ['batch', event.batchId] })
        queryClient.invalidateQueries({ queryKey: ['batch-logs', event.batchId] })
        queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
        queryClient.invalidateQueries({ queryKey: ['orders'] })
        if (event.eventType === 'QC_ALERT') {
          message.warning(`${event.batchCode}: QC phát hiện lỗi cần xử lý`)
        } else {
          message.info(`${event.batchCode}: ${event.message}`)
        }
      } catch {
        // Ignore malformed SSE events; the next query refresh will recover state.
      }
    }

    source.addEventListener('batch-updated', onBatchUpdated as EventListener)
    return () => source.close()
  }, [message, queryClient])
}
