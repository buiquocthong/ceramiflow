package com.ceramiflow.service.workflow;

import com.ceramiflow.config.WorkflowProperties;
import com.ceramiflow.domain.*;
import com.ceramiflow.dto.*;
import com.ceramiflow.exception.*;
import com.ceramiflow.repository.*;
import com.ceramiflow.service.notification.NotificationService;
import com.ceramiflow.service.realtime.BatchChangedEvent;
import com.ceramiflow.service.telegram.TelegramMessageFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class ProductionBatchService {
    private final ProductionBatchRepository batches;
    private final ProductionOrderRepository orders;
    private final ProductionSpecificationRepository specs;
    private final WorkflowStepRepository steps;
    private final QcInspectionRepository qcRepo;
    private final ProductionLogRepository logs;
    private final NotificationRepository notifications;
    private final WorkflowStateMachine stateMachine;
    private final WorkflowProperties props;
    private final QcPolicy qcPolicy;
    private final AuditService audit;
    private final NotificationService notification;
    private final TelegramMessageFactory telegramMessages;
    private final ApplicationEventPublisher events;

    public ProductionBatchService(ProductionBatchRepository b, ProductionOrderRepository o,
            ProductionSpecificationRepository s, WorkflowStepRepository ws, QcInspectionRepository q,
            ProductionLogRepository l, NotificationRepository n, WorkflowStateMachine sm, WorkflowProperties p,
            QcPolicy qp, AuditService a, NotificationService no, TelegramMessageFactory tm, ApplicationEventPublisher e) {
        batches = b;
        orders = o;
        specs = s;
        steps = ws;
        qcRepo = q;
        logs = l;
        notifications = n;
        stateMachine = sm;
        props = p;
        qcPolicy = qp;
        audit = a;
        notification = no;
        telegramMessages = tm;
        events = e;
    }

    @Transactional
    public BatchResponse createFromOrder(Long orderId, String actor) {
        if (batches.findByOrderId(orderId).isPresent())
            throw new BusinessException("A batch already exists for order " + orderId);
        ProductionOrder o = orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        ProductionSpecification sp = specs.findByOrderId(orderId).orElseThrow(
                () -> new BusinessException("Order must have a reviewed specification before batch creation"));
        if (o.getStatus() != OrderStatus.READY_FOR_REVIEW)
            throw new BusinessException("Order is not ready for batch creation");
        ProductionBatch b = new ProductionBatch();
        b.setBatchCode("GOM-" + o.getOrderCode().substring(Math.max(0, o.getOrderCode().length() - 8)));
        b.setOrder(o);
        b.setQuantity(o.getQuantity());
        if (o.getDeadline() != null)
            b.setEstimatedCompletionAt(o.getDeadline().atTime(17, 0));
        b = batches.save(b);
        int seq = 1;
        for (StageType st : List.of(StageType.FORMING, StageType.DRYING_REPAIR, StageType.PAINTING, StageType.GLAZING,
                StageType.READY_FOR_KILN, StageType.FIRING, StageType.QC, StageType.PACKAGING, StageType.COMPLETED)) {
            WorkflowStep w = new WorkflowStep();
            w.setBatch(b);
            w.setStepType(st);
            w.setSequence(seq++);
            if (st == StageType.FORMING) {
                w.setStatus(StepStatus.IN_PROGRESS);
                w.setStartedAt(LocalDateTime.now());
                w.setOperator(actor);
            }
            steps.save(w);
        }
        o.setStatus(OrderStatus.BATCH_CREATED);
        orders.save(o);
        audit.log(b, "BATCH_CREATED", null, StageType.FORMING.name(), "Batch created and forming started", null, actor);
        notification.enqueue(b, NotificationSeverity.INFO, telegramMessages.batchCreated(b));
        events.publishEvent(new BatchChangedEvent(b.getId(), b.getBatchCode(), null, StageType.FORMING.name(),
                "BATCH_CREATED", "Batch created"));
        return toResponse(b);
    }

    @Transactional
    public BatchResponse completeCurrentStep(Long id, BatchActionRequest req) {
        ProductionBatch b = get(id);
        ensureActive(b);
        StageType current = b.getCurrentStage();
        if (current == StageType.QC)
            throw new BusinessException("QC must be completed by submitting a QC inspection");
        StageType next = stateMachine.next(current);
        WorkflowStep currentStep = currentStep(b, current);
        currentStep.setStatus(StepStatus.COMPLETED);
        currentStep.setCompletedAt(LocalDateTime.now());
        currentStep.setOperator(req.operator());
        currentStep.setNotes(req.notes());
        steps.save(currentStep);
        if (next == StageType.COMPLETED) {
            WorkflowStep end = currentStep(b, StageType.COMPLETED);
            end.setStatus(StepStatus.COMPLETED);
            end.setStartedAt(LocalDateTime.now());
            end.setCompletedAt(LocalDateTime.now());
            steps.save(end);
            b.setCurrentStage(StageType.COMPLETED);
            b.setStatus(BatchStatus.COMPLETED);
            b.setCompletedAt(LocalDateTime.now());
        } else {
            WorkflowStep nextStep = currentStep(b, next);
            nextStep.setStatus(StepStatus.IN_PROGRESS);
            nextStep.setStartedAt(LocalDateTime.now());
            steps.save(nextStep);
            b.setCurrentStage(next);
        }
        batches.saveAndFlush(b);
        audit.log(b, "WORKFLOW_TRANSITION", current.name(), next.name(), "Stage completed: " + current + " → " + next,
                null, req.operator());
        NotificationSeverity sev = next == StageType.FIRING ? NotificationSeverity.WARNING : NotificationSeverity.INFO;
        Integer firingTemperature = specs.findByOrderId(b.getOrder().getId())
                .map(ProductionSpecification::getFiringTemperatureC)
                .orElse(null);
        String telegramMessage = telegramMessages.stageTransition(b, current, next, firingTemperature);
        notification.enqueue(b, sev, telegramMessage);
        events.publishEvent(new BatchChangedEvent(b.getId(), b.getBatchCode(), current.name(), next.name(),
                "WORKFLOW_TRANSITION", "Đã hoàn thành " + current + " và chuyển sang " + next));
        return toResponse(b);
    }

    @Transactional
    public BatchResponse completeCurrentStepFromTelegram(Long id, StageType expectedStage, String actor) {
        ProductionBatch b = get(id);
        ensureActive(b);

        if (b.getCurrentStage() != expectedStage) {
            throw new BusinessException("Batch stage has already changed from " + expectedStage + " to " + b.getCurrentStage());
        }
        if (expectedStage == StageType.QC) {
            throw new BusinessException("QC must be completed by submitting a QC inspection");
        }

        return completeCurrentStep(id, new BatchActionRequest(actor, "Xác nhận hoàn thành công đoạn từ Telegram"));
    }

    @Transactional
    public QcInspectionResponse inspect(Long id, QcInspectionRequest r) {
        ProductionBatch b = get(id);
        ensureActive(b);
        if (b.getCurrentStage() != StageType.QC)
            throw new BusinessException("Batch is not in QC stage");
        if (r.quantityPassed() + r.quantityFailed() != r.quantityInspected())
            throw new BusinessException("quantityPassed + quantityFailed must equal quantityInspected");
        if (r.quantityInspected() > b.getQuantity())
            throw new BusinessException("quantityInspected cannot exceed batch quantity");
        double rate = r.quantityFailed() * 100.0 / r.quantityInspected();
        QcDecision decision = qcPolicy.decide(rate);
        QcInspection q = new QcInspection();
        q.setBatch(b);
        q.setQuantityInspected(r.quantityInspected());
        q.setQuantityPassed(r.quantityPassed());
        q.setQuantityFailed(r.quantityFailed());
        q.setDefectType(r.defectType());
        q.setSeverity(r.severity());
        q.setDefectRate(Math.round(rate * 100.0) / 100.0);
        q.setDecision(decision);
        q.setNotes(r.notes());
        qcRepo.save(q);
        WorkflowStep qc = currentStep(b, StageType.QC);
        qc.setCompletedAt(LocalDateTime.now());
        qc.setOperator(r.operator());
        qc.setNotes(r.notes());
        if (decision == QcDecision.PASS) {
            qc.setStatus(StepStatus.COMPLETED);
            WorkflowStep packaging = currentStep(b, StageType.PACKAGING);
            packaging.setStatus(StepStatus.IN_PROGRESS);
            packaging.setStartedAt(LocalDateTime.now());
            steps.save(packaging);
            b.setCurrentStage(StageType.PACKAGING);
            b.setStatus(BatchStatus.ACTIVE);
            audit.log(b, "QC_PASS", "QC", "PACKAGING", "QC passed with defect rate " + q.getDefectRate() + "%", null,
                    r.operator());
            notification.enqueue(b, NotificationSeverity.INFO, telegramMessages.qcPassed(b, q));
            events.publishEvent(
                    new BatchChangedEvent(b.getId(), b.getBatchCode(), "QC", "PACKAGING", "QC_PASS", "QC passed"));
        } else {
            qc.setStatus(StepStatus.REWORK);
            b.setStatus(BatchStatus.REWORK_REQUIRED);
            audit.log(b, "QC_" + decision.name(), "QC", "REWORK_REQUIRED",
                    "QC detected defects. Rate " + q.getDefectRate() + "%", null, r.operator());
            notification.enqueue(b, NotificationSeverity.CRITICAL, telegramMessages.qcAlert(b, q));
            events.publishEvent(new BatchChangedEvent(b.getId(), b.getBatchCode(), "QC", "REWORK_REQUIRED", "QC_ALERT",
                    "QC defects require action"));
        }
        steps.save(qc);
        batches.saveAndFlush(b);
        return toQc(q);
    }

    @Transactional
    public BatchResponse rework(Long id, ReworkRequest r) {
        ProductionBatch b = get(id);
        if (b.getStatus() != BatchStatus.REWORK_REQUIRED)
            throw new BusinessException("Batch is not waiting for rework");
        stateMachine.validateReworkTarget(r.targetStage());
        int seq = steps.findByBatchIdOrderBySequenceAsc(id).stream().mapToInt(WorkflowStep::getSequence).max().orElse(0)
                + 1;
        WorkflowStep w = new WorkflowStep();
        w.setBatch(b);
        w.setStepType(r.targetStage());
        w.setStatus(StepStatus.IN_PROGRESS);
        w.setSequence(seq);
        w.setStartedAt(LocalDateTime.now());
        w.setOperator(r.operator());
        w.setNotes(r.notes());
        steps.save(w);
        StageType from = b.getCurrentStage();
        b.setCurrentStage(r.targetStage());
        b.setStatus(BatchStatus.ACTIVE);
        batches.saveAndFlush(b);
        audit.log(b, "REWORK_STARTED", from.name(), r.targetStage().name(), "Rework resumed at " + r.targetStage(),
                null, r.operator());
        notification.enqueue(b, NotificationSeverity.WARNING,
                telegramMessages.reworkStarted(b, r.targetStage(), r.notes()));
        events.publishEvent(new BatchChangedEvent(b.getId(), b.getBatchCode(), from.name(), r.targetStage().name(),
                "REWORK_STARTED", "Rework started"));
        return toResponse(b);
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> list() {
        return batches.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BatchResponse find(Long id) {
        return toResponse(get(id));
    }

    @Transactional(readOnly = true)
    public List<ProductionLogResponse> logs(Long id) {
        get(id);
        return logs.findByBatchIdOrderByCreatedAtDesc(id).stream()
                .map(l -> new ProductionLogResponse(l.getId(), l.getEventType(), l.getFromStatus(), l.getToStatus(),
                        l.getMessage(), l.getMetadata(), l.getCreatedBy(), l.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> notifications(Long id) {
        get(id);
        return notifications.findByBatchIdOrderByCreatedAtDesc(id).stream()
                .map(n -> new NotificationResponse(n.getId(), n.getChannel(), n.getSeverity(), n.getMessage(),
                        n.getStatus(), n.getAttemptCount(), n.getLastError(), n.getSentAt(), n.getCreatedAt()))
                .toList();
    }

    private void ensureActive(ProductionBatch b) {
        if (b.getStatus() != BatchStatus.ACTIVE)
            throw new BusinessException("Batch is not active: " + b.getStatus());
        if (b.getCurrentStage() == StageType.COMPLETED)
            throw new BusinessException("Batch already completed");
    }

    private ProductionBatch get(Long id) {
        return batches.findById(id).orElseThrow(() -> new NotFoundException("Batch not found: " + id));
    }

    private WorkflowStep currentStep(ProductionBatch b, StageType type) {
        return steps.findFirstByBatchIdAndStepTypeOrderBySequenceDesc(b.getId(), type)
                .orElseThrow(() -> new BusinessException("Workflow step not found: " + type));
    }

    private BatchResponse toResponse(ProductionBatch b) {
        var ws = steps.findByBatchIdOrderBySequenceAsc(b.getId()).stream()
                .map(w -> new WorkflowStepResponse(w.getId(), w.getStepType(), w.getStatus(), w.getSequence(),
                        w.getStartedAt(), w.getCompletedAt(), w.getOperator(), w.getNotes()))
                .toList();
        var qs = qcRepo.findByBatchIdOrderByCreatedAtDesc(b.getId()).stream().map(this::toQc).toList();
        return new BatchResponse(b.getId(), b.getBatchCode(), b.getOrder().getId(), b.getOrder().getOrderCode(),
                b.getQuantity(), b.getStatus(), b.getCurrentStage(), b.getVersion(), b.getStartedAt(),
                b.getEstimatedCompletionAt(), b.getCompletedAt(), ws, qs);
    }

    private QcInspectionResponse toQc(QcInspection q) {
        return new QcInspectionResponse(q.getId(), q.getQuantityInspected(), q.getQuantityPassed(),
                q.getQuantityFailed(), q.getDefectType(), q.getSeverity(), q.getDefectRate(), q.getDecision(),
                q.getNotes(), q.getCreatedAt());
    }
}