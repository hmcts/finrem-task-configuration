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

class CamundaTaskCompletionConsentedTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    static void initialization() {
        currentDmnDecisionTable = DmnDecisionTable.WA_TASK_COMPLETION_DIVORCE_FINANCIALREMEDYMVP2;
    }

    @Test
    void givenUnknownEventShouldReturnEmptyResult() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "unknownEvent");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    @Test
    void ifThisTestFailsNeedsUpdatingWithYourChanges() {
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules()).hasSize(10);
    }

    @Test
    void givenAttachScannedDocsShouldAutoCompleteProcessScannedDocumentsTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "attachScannedDocs");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "processScannedDocuments", "completionMode", "Auto")

        ));
    }

    @Test
    void givenAmendedConsentOrderShouldAutoCompleteProcessApprovedOrderTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_amendedConsentOrder");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "processApprovedOrder", "completionMode", "Auto")
        ));
    }

    @Test
    void givenReferToJudgeShouldAutoCompleteCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_referToJudge");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkResponseReceived", "completionMode", "Auto")
        ));
    }

    @Test
    void givenAssignToJudgeShouldAutoCompleteCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_assignToJudge");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkResponseReceived", "completionMode", "Auto")
        ));
    }

    @Test
    void givenGeneralOrderShouldAutoCompleteCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_generalOrder");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkResponseReceived", "completionMode", "Auto")
        ));
    }

    @Test
    void givenCallbackRejectedOrderShouldAutoCompleteCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_callbackRejectedOrder");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkResponseReceived", "completionMode", "Auto")
        ));
    }

    @Test
    void givenAwaitingInfoShouldAutoCompleteCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_awaitingInfo");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkResponseReceived", "completionMode", "Auto")
        ));
    }

    @Test
    void givenGeneralEmailShouldAutoCompleteCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_generalEmail");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkResponseReceived", "completionMode", "Auto")
        ));
    }

    @Test
    void givenListForHearingShouldAutoCompleteCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_listForHearing");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkResponseReceived", "completionMode", "Auto")
        ));
    }

    @Test
    void givenCloseShouldAutoCompleteCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_close");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkResponseReceived", "completionMode", "Auto")
        ));
    }
}
