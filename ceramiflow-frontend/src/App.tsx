import { Navigate, Route, Routes } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import { useRealtimeUpdates } from './hooks/useRealtimeUpdates'
import DashboardPage from './pages/DashboardPage'
import OrdersPage from './pages/OrdersPage'
import CreateOrderPage from './pages/CreateOrderPage'
import ProductionPage from './pages/ProductionPage'
import BatchDetailPage from './pages/BatchDetailPage'
import QcPage from './pages/QcPage'
import ActivityPage from './pages/ActivityPage'

export default function App() {
  useRealtimeUpdates()

  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/new" element={<CreateOrderPage />} />
        <Route path="/production" element={<ProductionPage />} />
        <Route path="/batches/:id" element={<BatchDetailPage />} />
        <Route path="/qc" element={<QcPage />} />
        <Route path="/activity" element={<ActivityPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
