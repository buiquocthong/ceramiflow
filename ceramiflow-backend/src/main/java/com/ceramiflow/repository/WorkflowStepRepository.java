package com.ceramiflow.repository;

import com.ceramiflow.domain.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {
    List<WorkflowStep> findByBatchIdOrderBySequenceAsc(Long batchId);

    Optional<WorkflowStep> findFirstByBatchIdAndStepTypeOrderBySequenceDesc(Long batchId,
            com.ceramiflow.domain.StageType stepType);
}