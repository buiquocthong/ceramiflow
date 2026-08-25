# Chạy CeramiFlow Frontend trên Windows

## 1. Yêu cầu

- Node.js 20+ (khuyến nghị 22)
- Backend CeramiFlow đang chạy tại `http://localhost:8080`

## 2. Tạo `.env`

Copy `.env.example` thành `.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 3. Cài dependency

Mở PowerShell tại thư mục frontend:

```powershell
npm install
```

## 4. Chạy development

```powershell
npm run dev
```

Mở `http://localhost:5173`.

## 5. Nếu frontend gọi API bị CORS

Backend hiện cần cho phép `http://localhost:5173`. `CorsConfig` của backend v2 đã cấu hình `http://localhost:*`, nên mặc định không cần sửa.

## 6. Kiểm tra nhanh demo

1. Tạo đơn mới.
2. Phân tích AI.
3. Xác nhận thông số và tạo mẻ.
4. Mở Điều phối sản xuất.
5. Vào batch detail và hoàn thành lần lượt các công đoạn.
6. Ở QC nhập một case lỗi để kiểm tra rework + Telegram.
