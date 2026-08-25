import { Empty, Button } from 'antd'
import type { ReactNode } from 'react'

export default function EmptyState({ description, action, actionLabel }: { description: string; action?: () => void; actionLabel?: ReactNode }) {
  return (
    <div className="empty-state">
      <Empty description={description}>
        {action && <Button type="primary" onClick={action}>{actionLabel || 'Bắt đầu'}</Button>}
      </Empty>
    </div>
  )
}
