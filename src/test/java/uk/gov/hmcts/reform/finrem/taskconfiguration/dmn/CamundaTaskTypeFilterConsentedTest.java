package uk.gov.hmcts.reform.finrem.taskconfiguration.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.taskconfiguration.DmnDecisionTable;
import uk.gov.hmcts.reform.finrem.taskconfiguration.DmnDecisionTableBaseUnitTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CamundaTaskTypeFilterConsentedTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    static void initialization() {
        currentDmnDecisionTable = DmnDecisionTable.WA_TASK_TYPES_DIVORCE_FINANCIALREMEDYMVP2;
    }

    @Test
    void checkDmnChanged() {
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getInputs()).hasSize(1);
        assertThat(logic.getOutputs()).hasSize(2);
        assertThat(logic.getRules())
            .as("Number of defined task type rules has changed.")
            .hasSize(6);
    }

    @Test
    void givenNoInput_whenEvaluated_thenReturnsAllTaskTypes() {
        VariableMap inputVariables = new VariableMapImpl();

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskTypeId", "processScannedDocuments", "taskTypeName", "Process Scanned Documents"),
            Map.of("taskTypeId", "processApprovedOrder", "taskTypeName", "Process Approved Order"),
            Map.of("taskTypeId", "checkHelpWithFees", "taskTypeName", "Check Help With Fees"),
            Map.of("taskTypeId", "reviewApplication", "taskTypeName", "Review Application"),
            Map.of("taskTypeId", "reviewRefusedOrder", "taskTypeName", "Review Refused Order"),
            Map.of("taskTypeId", "reviewOrderResponse", "taskTypeName", "Review Order Response")

        ));
    }
}
