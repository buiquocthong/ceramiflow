# Thiết lập Telegram Inline Button cho CeramiFlow

## 1. Cấu hình `.env`

```env
TELEGRAM_ENABLED=true
TELEGRAM_BOT_TOKEN=YOUR_BOT_TOKEN
TELEGRAM_CHAT_ID=YOUR_GROUP_CHAT_ID
TELEGRAM_WEBHOOK_SECRET=YOUR_RANDOM_SECRET
```

Không commit `.env` lên GitHub.

## 2. Expose backend local qua HTTPS

Telegram không thể callback vào `localhost`. Ví dụ với ngrok:

```bash
ngrok http 8080
```

Giả sử URL nhận được là:

```text
https://abc123.ngrok-free.app
```

## 3. Set webhook

```bash
curl -X POST "https://api.telegram.org/botYOUR_BOT_TOKEN/setWebhook" \
  -d "url=https://abc123.ngrok-free.app/api/integrations/telegram/webhook" \
  -d "secret_token=YOUR_RANDOM_SECRET"
```

## 4. Kiểm tra

```bash
curl "https://api.telegram.org/botYOUR_BOT_TOKEN/getWebhookInfo"
```

`url` phải trỏ đúng `/api/integrations/telegram/webhook` và `last_error_message` nên trống.

## 5. Demo

- Tạo Order → Analyze AI → Confirm → Create Batch.
- Chờ notification worker tối đa khoảng 5 giây.
- Telegram nhận tin nhắn tạo mẻ kèm nút `✅ Xác nhận hoàn thành Tạo hình mộc`.
- Bấm nút trên Telegram.
- Backend chuyển `FORMING -> DRYING_REPAIR`.
- Tin nhắn mới tự động xuất hiện kèm nút cho `Phơi sấy & sửa mộc`.
- Tiếp tục cho đến `QC`.
- Tại QC, hệ thống không hiển thị nút complete; phải nhập kết quả QC trên Web/API.
- QC PASS chuyển sang `PACKAGING`, và Telegram lại hiển thị nút xác nhận đóng gói.

## 6. Chẩn đoán khi nút bấm không phản hồi

Sau khi backend chạy, mở:

```text
GET http://localhost:8080/api/integrations/telegram/webhook-info
```

Kết quả Telegram phải có `result.url` là URL HTTPS public hiện tại, ví dụ:

```text
https://abc123.ngrok-free.app/api/integrations/telegram/webhook
```

Nếu ngrok được restart thì URL có thể đổi; phải gọi `setWebhook` lại.

Khi bấm nút, console backend phải xuất hiện log `Nhận Telegram callback:`. Nếu không có log này thì Telegram chưa gọi được webhook (URL sai/hết hạn, webhook chưa set, hoặc `last_error_message` trong `getWebhookInfo` đang báo lỗi).

Nếu console có `Telegram webhook bị từ chối`, kiểm tra `TELEGRAM_WEBHOOK_SECRET` phải khớp chính xác giá trị `secret_token` đã dùng lúc `setWebhook`.
