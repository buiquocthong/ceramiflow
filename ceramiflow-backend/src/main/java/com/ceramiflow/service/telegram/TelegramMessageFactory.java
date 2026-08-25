package com.ceramiflow.service.telegram;

import com.ceramiflow.domain.*;
import org.springframework.stereotype.Component;

@Component
public class TelegramMessageFactory {

    public String batchCreated(ProductionBatch batch) {
        return "🟦 Mẻ gốm " + batch.getBatchCode() + " đã được khởi tạo.\n"
                + "• Số lượng: " + batch.getQuantity() + " sản phẩm\n"
                + "• Công đoạn hiện tại: " + TelegramStageText.label(StageType.FORMING) + "\n\n"
                + "Khi hoàn tất công đoạn, thợ hoặc quản lý có thể bấm nút xác nhận ngay bên dưới.";
    }

    public String stageTransition(ProductionBatch batch, StageType from, StageType to, Integer firingTemperatureC) {
        if (to == StageType.COMPLETED) {
            return "🎉 Mẻ gốm " + batch.getBatchCode() + " đã hoàn thành toàn bộ quy trình sản xuất.\n"
                    + "• Công đoạn cuối: " + TelegramStageText.label(from) + "\n"
                    + "• Trạng thái: Hoàn thành";
        }

        StringBuilder message = new StringBuilder()
                .append("✅ Mẻ gốm ").append(batch.getBatchCode())
                .append(" đã hoàn thành công đoạn ").append(TelegramStageText.label(from)).append(".\n")
                .append("➡️ Chuyển sang: ").append(TelegramStageText.label(to)).append(".");

        if (to == StageType.FIRING && firingTemperatureC != null) {
            message.append("\n🔥 Nhiệt độ nung dự kiến: ").append(firingTemperatureC).append("°C.");
        }

        if (to == StageType.QC) {
            message.append("\n\nVui lòng thực hiện kiểm định trên hệ thống để ghi nhận số lượng đạt, lỗi và quyết định xử lý.");
        } else {
            message.append("\n\nKhi hoàn tất công đoạn mới, có thể bấm nút xác nhận bên dưới.");
        }
        return message.toString();
    }

    public String qcPassed(ProductionBatch batch, QcInspection inspection) {
        return "✅ Mẻ gốm " + batch.getBatchCode() + " đã đạt kiểm định chất lượng.\n"
                + "• Đã kiểm tra: " + inspection.getQuantityInspected() + " sản phẩm\n"
                + "• Không đạt: " + inspection.getQuantityFailed() + " sản phẩm\n"
                + "• Tỷ lệ lỗi: " + inspection.getDefectRate() + "%\n"
                + "➡️ Mẻ được chuyển sang công đoạn " + TelegramStageText.label(StageType.PACKAGING) + ".";
    }

    public String qcAlert(ProductionBatch batch, QcInspection inspection) {
        String defect = inspection.getDefectType() == null || inspection.getDefectType().isBlank()
                ? "Chưa ghi rõ loại lỗi"
                : inspection.getDefectType();
        String severity = inspection.getSeverity() == null || inspection.getSeverity().isBlank()
                ? "Chưa xác định"
                : inspection.getSeverity();

        return "🚨 CẢNH BÁO QC – Mẻ gốm " + batch.getBatchCode() + "\n"
                + "• Không đạt: " + inspection.getQuantityFailed() + "/" + inspection.getQuantityInspected()
                + " sản phẩm\n"
                + "• Tỷ lệ lỗi: " + inspection.getDefectRate() + "%\n"
                + "• Lỗi phát hiện: " + defect + "\n"
                + "• Mức độ: " + severity + "\n"
                + "• Kết quả: " + vietnameseDecision(inspection.getDecision()) + "\n\n"
                + "Quản lý cần chọn công đoạn phù hợp để xử lý lại trước khi tiếp tục sản xuất.";
    }

    public String reworkStarted(ProductionBatch batch, StageType target, String notes) {
        StringBuilder message = new StringBuilder()
                .append("♻️ Mẻ gốm ").append(batch.getBatchCode()).append(" bắt đầu xử lý lại.\n")
                .append("• Quay về công đoạn: ").append(TelegramStageText.label(target));
        if (notes != null && !notes.isBlank()) {
            message.append("\n• Ghi chú: ").append(notes);
        }
        message.append("\n\nKhi hoàn tất công đoạn xử lý lại, có thể bấm nút xác nhận bên dưới.");
        return message.toString();
    }

    private String vietnameseDecision(QcDecision decision) {
        if (decision == null) {
            return "Chưa xác định";
        }
        return switch (decision) {
            case PASS -> "Đạt";
            case REWORK_REQUIRED -> "Cần xử lý lại";
            case REJECT -> "Không đạt / cần xử lý";
        };
    }
}
