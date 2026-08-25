package com.ceramiflow.service.workflow;

import com.ceramiflow.domain.*;
import com.ceramiflow.dto.*;
import com.ceramiflow.exception.*;
import com.ceramiflow.repository.*;
import com.ceramiflow.service.ai.AIExtractionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductionOrderService {
    private final ProductionOrderRepository orders;
    private final ProductionSpecificationRepository specs;
    private final AIExtractionService ai;
    private static final AtomicLong CODE_SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    public ProductionOrderService(ProductionOrderRepository o, ProductionSpecificationRepository s,
            AIExtractionService a) {
        orders = o;
        specs = s;
        ai = a;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        ProductionOrder o = new ProductionOrder();
        o.setOrderCode("ORD-" + LocalDate.now().toString().replace("-", "") + "-" + CODE_SEQ.incrementAndGet());
        o.setRawDescription(req.description());
        return toResponse(orders.save(o));
    }

    @Transactional
    public OrderResponse analyze(Long id) {
        ProductionOrder o = get(id);
        if (o.getStatus() == OrderStatus.BATCH_CREATED)
            throw new BusinessException("Order already converted to a batch");
        o.setStatus(OrderStatus.AI_ANALYZING);
        orders.save(o);
        try {
            ExtractedSpecDto e = ai.extract(o.getRawDescription());
            ProductionSpecification s = specs.findByOrderId(id).orElseGet(ProductionSpecification::new);
            s.setOrder(o);
            apply(s, e);
            specs.save(s);
            o.setQuantity(e.quantity());
            o.setPriority(PriorityLevel.valueOf(e.priority()));
            if (e.deadlineDays() != null)
                o.setDeadline(LocalDate.now().plusDays(e.deadlineDays()));
            o.setStatus(OrderStatus.READY_FOR_REVIEW);
            return toResponse(orders.save(o));
        } catch (Exception ex) {
            o.setStatus(OrderStatus.EXTRACTION_FAILED);
            orders.save(o);
            throw ex;
        }
    }

    @Transactional
    public OrderResponse confirm(Long id, ConfirmOrderRequest r) {
        ProductionOrder o = get(id);
        if (o.getStatus() != OrderStatus.READY_FOR_REVIEW && o.getStatus() != OrderStatus.EXTRACTION_FAILED)
            throw new BusinessException("Order must be analyzed before confirmation");
        ProductionSpecification s = specs.findByOrderId(id).orElseGet(ProductionSpecification::new);
        s.setOrder(o);
        ExtractedSpecDto d = new ExtractedSpecDto(r.productType(), r.quantity(), r.clayType(), r.glazeType(),
                r.patternDescription(), r.heightCm(), r.widthCm(), r.estimatedClayKg(), r.estimatedGlazeKg(),
                r.firingTemperatureC(), r.estimatedFiringHours(), r.deadlineDays(),
                Optional.ofNullable(r.priority()).orElse("MEDIUM"), false, null, "HUMAN_CONFIRMED");
        apply(s, d);
        specs.save(s);
        o.setQuantity(r.quantity());
        o.setPriority(PriorityLevel.valueOf(Optional.ofNullable(r.priority()).orElse("MEDIUM")));
        if (r.deadlineDays() != null)
            o.setDeadline(LocalDate.now().plusDays(r.deadlineDays()));
        o.setStatus(OrderStatus.READY_FOR_REVIEW);
        return toResponse(orders.save(o));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        return orders.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse find(Long id) {
        return toResponse(get(id));
    }

    ProductionOrder get(Long id) {
        return orders.findById(id).orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    private void apply(ProductionSpecification s, ExtractedSpecDto e) {
        s.setProductType(e.productType());
        s.setClayType(e.clayType());
        s.setGlazeType(e.glazeType());
        s.setPatternDescription(e.patternDescription());
        s.setHeightCm(e.heightCm());
        s.setWidthCm(e.widthCm());
        s.setEstimatedClayKg(e.estimatedClayKg());
        s.setEstimatedGlazeKg(e.estimatedGlazeKg());
        s.setFiringTemperatureC(e.firingTemperatureC());
        s.setEstimatedFiringHours(e.estimatedFiringHours());
        s.setDeadlineDays(e.deadlineDays());
        s.setNeedsReview(e.needsReview());
        s.setReviewNote(e.reviewNote());
        s.setSource(e.source());
    }

    private OrderResponse toResponse(ProductionOrder o) {
        ExtractedSpecDto d = specs.findByOrderId(o.getId())
                .map(s -> new ExtractedSpecDto(s.getProductType(), o.getQuantity(), s.getClayType(), s.getGlazeType(),
                        s.getPatternDescription(), s.getHeightCm(), s.getWidthCm(), s.getEstimatedClayKg(),
                        s.getEstimatedGlazeKg(), s.getFiringTemperatureC(), s.getEstimatedFiringHours(),
                        s.getDeadlineDays(), o.getPriority().name(), s.isNeedsReview(), s.getReviewNote(),
                        s.getSource()))
                .orElse(null);
        return new OrderResponse(o.getId(), o.getOrderCode(), o.getRawDescription(), o.getStatus(), o.getPriority(),
                o.getQuantity(), o.getDeadline(), d, o.getCreatedAt(), o.getUpdatedAt());
    }
}