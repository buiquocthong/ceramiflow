package com.ceramiflow.service.workflow;
import com.ceramiflow.config.WorkflowProperties; import com.ceramiflow.domain.QcDecision; import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class QcPolicyTest { private final QcPolicy p=new QcPolicy(new WorkflowProperties(3,10)); @Test void twoPercentPasses(){assertEquals(QcDecision.PASS,p.decide(2));} @Test void fivePercentNeedsRework(){assertEquals(QcDecision.REWORK_REQUIRED,p.decide(5));} @Test void twelvePercentRejects(){assertEquals(QcDecision.REJECT,p.decide(12));} }
