package com.ceramiflow.service.telegram;

import com.ceramiflow.domain.StageType;

public final class TelegramStageText {
    private TelegramStageText() {
    }

    public static String label(StageType stage) {
        if (stage == null) {
            return "Không xác định";
        }
        return switch (stage) {
            case FORMING -> "Tạo hình mộc";
            case DRYING_REPAIR -> "Phơi sấy & sửa mộc";
            case PAINTING -> "Vẽ họa tiết";
            case GLAZING -> "Tráng men";
            case READY_FOR_KILN -> "Chuẩn bị vào lò";
            case FIRING -> "Nung lò";
            case QC -> "Kiểm định chất lượng (QC)";
            case PACKAGING -> "Đóng gói";
            case COMPLETED -> "Hoàn thành";
        };
    }

    public static String buttonLabel(StageType stage) {
        if (stage == null) {
            return "✅ Xác nhận hoàn thành công đoạn";
        }
        return switch (stage) {
            case FORMING -> "✅ Hoàn thành tạo hình mộc";
            case DRYING_REPAIR -> "✅ Hoàn thành phơi sấy & sửa mộc";
            case PAINTING -> "✅ Hoàn thành vẽ họa tiết";
            case GLAZING -> "✅ Hoàn thành tráng men";
            case READY_FOR_KILN -> "🔥 Xác nhận đã đưa vào lò";
            case FIRING -> "✅ Xác nhận đã nung xong";
            case QC -> "🔎 Ghi nhận kết quả QC trên hệ thống";
            case PACKAGING -> "📦 Xác nhận đã đóng gói";
            case COMPLETED -> "✅ Đã hoàn thành";
        };
    }
}
