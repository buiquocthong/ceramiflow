import { useMemo, useState } from 'react'
import { useQueries, useQuery } from '@tanstack/react-query'
import { Card, Input, Select, Space, Table, Typography } from 'antd'
import { SearchOutlined } from '@ant-design/icons'
import PageHeader from '../components/common/PageHeader'
import { getBatchLogs, listBatches } from '../api/batches'
import { formatDateTime } from '../utils/format'

export default function ActivityPage() {
  const [search, setSearch] = useState('')
  const [eventType, setEventType] = useState<string | undefined>()
  const batchesQuery = useQuery({ queryKey: ['batches'], queryFn: listBatches })
  const batches = batchesQuery.data || []
  const logQueries = useQueries({
    queries: batches.map(batch => ({
      queryKey: ['batch-logs', batch.id],
      queryFn: () => getBatchLogs(batch.id),
      staleTime: 10_000,
    })),
  })

  const rows = useMemo(() => batches.flatMap((batch, i) => (logQueries[i]?.data || []).map(log => ({ ...log, batchId: batch.id, batchCode: batch.batchCode }))).sort((a,b) => b.createdAt.localeCompare(a.createdAt)), [batches, logQueries])
  const types = [...new Set(rows.map(r => r.eventType))].sort()
  const filtered = rows.filter(r => {
    const q = search.trim().toLowerCase()
    const matchSearch = !q || [r.batchCode, r.message, r.createdBy, r.eventType].some(v => v?.toLowerCase().includes(q))
    return matchSearch && (!eventType || r.eventType === eventType)
  })

  return (
    <div>
      <PageHeader title="Nhật ký hoạt động" subtitle="Audit trail tổng hợp từ ProductionLog của từng mẻ." />
      <Card>
        <Space wrap className="section-gap-sm">
          <Input prefix={<SearchOutlined />} placeholder="Tìm mẻ, nội dung, người thao tác..." allowClear value={search} onChange={e => setSearch(e.target.value)} style={{ width: 340 }} />
          <Select placeholder="Loại sự kiện" allowClear value={eventType} onChange={setEventType} style={{ width: 220 }} options={types.map(v => ({ value: v, label: v }))} />
        </Space>
        <Table rowKey={r => `${r.batchId}-${r.id}`} loading={batchesQuery.isLoading || logQueries.some(q => q.isLoading)} dataSource={filtered} columns={[
          { title: 'Thời gian', render: (_, r) => formatDateTime(r.createdAt), width: 170 },
          { title: 'Mẻ', dataIndex: 'batchCode', width: 150, render: v => <Typography.Text strong>{v}</Typography.Text> },
          { title: 'Sự kiện', dataIndex: 'eventType', width: 180 },
          { title: 'Trạng thái', render: (_, r) => (r.fromStatus || r.toStatus) ? `${r.fromStatus || '—'} → ${r.toStatus || '—'}` : '—' },
          { title: 'Nội dung', dataIndex: 'message' },
          { title: 'Thực hiện bởi', dataIndex: 'createdBy', width: 140, render: v => v || 'system' },
        ]} />
      </Card>
    </div>
  )
}
