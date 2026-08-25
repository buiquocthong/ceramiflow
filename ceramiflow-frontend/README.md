# CeramiFlow Frontend

React + TypeScript frontend cho **Hệ Thống Điều Phối & Giám Sát Quy Trình Sản Xuất Xưởng Gốm**.

Frontend được thiết kế bám trực tiếp API của `ceramiflow-backend-v2` và ưu tiên logic vận hành, khả năng quan sát workflow, QC/rework, realtime và trạng thái Telegram hơn hiệu ứng trang trí.

## Tech stack

- React 18 + TypeScript
- Vite
- Ant Design
- TanStack Query
- Axios
- React Router
- Recharts
- Server-Sent Events (SSE)

## Tính năng

### Dashboard

- Tổng đơn hàng
- Mẻ đang hoạt động
- Mẻ cần rework
- QC failures
- Mẻ hoàn thành
- Biểu đồ phân bố công đoạn
- Danh sách mẻ gần đây

### Order intake + AI

Flow:

```text
Natural-language description
        ↓
POST /api/orders
        ↓
POST /api/orders/{id}/analyze
        ↓
AI structured specification
        ↓
Human review/edit
        ↓
POST /api/orders/{id}/confirm
        ↓
POST /api/batches/from-order/{id}
```

AI output được hiển thị dưới dạng form có thể kiểm tra/chỉnh sửa trước khi tạo mẻ.

### Production Kanban

Các cột:

```text
FORMING
→ DRYING_REPAIR
→ PAINTING
→ GLAZING
→ READY_FOR_KILN
→ FIRING
→ QC
→ PACKAGING
→ COMPLETED
```

Không hỗ trợ drag-and-drop tùy ý vì business rule được backend state machine kiểm soát.

### Batch detail

- Progress
- Workflow timeline
- Production specification
- Workflow steps
- QC history
- Production logs
- Telegram notification status
- Complete stage action
- QC form
- Rework action

### QC

Frontend gửi:

- quantityInspected
- quantityPassed
- quantityFailed
- defectType
- severity
- notes
- operator

Backend chịu trách nhiệm tính `defectRate` và quyết định `PASS / REWORK_REQUIRED / REJECT`.

### Realtime

Kết nối:

```text
GET /api/stream
```

Frontend lắng nghe event `batch-updated`, tự invalidate dữ liệu dashboard/batch/log và hiển thị thông báo.

Notification status được refresh 5 giây/lần vì Telegram delivery worker cập nhật bất đồng bộ và hiện backend không publish SSE riêng cho notification delivery.

## Chạy project

### 1. Backend

Đảm bảo Spring Boot chạy tại:

```text
http://localhost:8080
```

### 2. Frontend env

Copy:

```bash
cp .env.example .env
```

Nội dung mặc định:

```env
VITE_API_BASE_URL=http://localhost:8080
```

### 3. Install & run

```bash
npm install
npm run dev
```

Mở:

```text
http://localhost:5173
```

### Production build

```bash
npm run build
npm run preview
```

## Demo flow đề xuất

1. Mở **Tạo đơn mới**.
2. Nhập: `Đơn 200 Bình gốm họa tiết sen men lam cao 35cm, yêu cầu nung nhiệt độ cao 1280°C, hoàn thành trong 10 ngày`.
3. **Phân tích với AI**.
4. Review thông số AI → **Xác nhận & bắt đầu sản xuất**.
5. Mở Batch Detail.
6. Complete tuần tự các stage đến FIRING; theo dõi Telegram notification.
7. Sang QC, nhập `200 inspected / 190 passed / 10 failed` + `Nứt men`.
8. Backend tính defect rate 5% và chuyển batch thành `REWORK_REQUIRED` theo policy hiện tại.
9. Chọn công đoạn làm lại, chạy tiếp đến QC.
10. QC PASS → PACKAGING → COMPLETED.
11. Dashboard/Kanban cập nhật qua SSE.

## API contract sử dụng

### Orders

- `GET /api/orders`
- `GET /api/orders/{id}`
- `POST /api/orders`
- `POST /api/orders/{id}/analyze`
- `POST /api/orders/{id}/confirm`

### Batches

- `GET /api/batches`
- `GET /api/batches/{id}`
- `POST /api/batches/from-order/{orderId}?actor=...`
- `POST /api/batches/{id}/steps/complete`
- `POST /api/batches/{id}/qc`
- `POST /api/batches/{id}/rework`
- `GET /api/batches/{id}/logs`
- `GET /api/batches/{id}/notifications`

### Dashboard / realtime

- `GET /api/dashboard/summary`
- `GET /api/stream`

## Lưu ý backend hiện tại

`OrderStatus` chưa có trạng thái `CONFIRMED`; endpoint `/confirm` giữ order ở `READY_FOR_REVIEW` để `/batches/from-order/{id}` có thể tạo batch. Frontend bám đúng behavior này.

Trang Activity Logs hiện tổng hợp client-side bằng cách lấy logs của từng batch vì backend chưa có global `/api/logs` endpoint. Đây là phù hợp cho working MVP; nếu dữ liệu lớn nên bổ sung API phân trang ở backend.
