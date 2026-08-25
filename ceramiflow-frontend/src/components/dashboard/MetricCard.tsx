import type { ReactNode } from 'react'
import { Card, Statistic } from 'antd'

export default function MetricCard({ title, value, prefix, suffix, loading }: { title: string; value?: number; prefix?: ReactNode; suffix?: string; loading?: boolean }) {
  return (
    <Card className="metric-card" loading={loading}>
      <Statistic title={title} value={value ?? 0} prefix={prefix} suffix={suffix} />
    </Card>
  )
}
