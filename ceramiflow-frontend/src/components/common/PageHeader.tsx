import type { ReactNode } from 'react'
import { Typography } from 'antd'

export default function PageHeader({ title, subtitle, extra }: { title: string; subtitle?: string; extra?: ReactNode }) {
  return (
    <div className="page-header">
      <div>
        <Typography.Title level={2} className="page-title">{title}</Typography.Title>
        {subtitle && <Typography.Text type="secondary">{subtitle}</Typography.Text>}
      </div>
      {extra && <div>{extra}</div>}
    </div>
  )
}
