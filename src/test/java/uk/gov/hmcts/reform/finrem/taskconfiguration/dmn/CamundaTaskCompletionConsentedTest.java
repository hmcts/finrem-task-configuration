package uk.gov.hmcts.reform.finrem.taskconfiguration.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

        assertThat(logic.getRules()).hasSize(6);
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

    @ParameterizedTest
    @ValueSource(strings = {"FR_referToJudge", "FR_assignToJudge", "FR_generalOrder", "FR_callbackRejectedOrder",
        "FR_awaitingInfo","FR_generalEmail","FR_listForHearing", "FR_close"})
    void givenCheckResponseEvents_whenEvaluated_thenCompletesTask(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkResponseReceived", "completionMode", "Auto")
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR_HWFDecisionMade", "FR_paymentMadeFromHWF", "FR_awaitingPaymentResponseFromHWF"})
    void givenHelpWithFeesEvents_whenEvaluated_thenCompletesTask(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkHelpWithFees", "completionMode", "Auto")
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = { "FR_approveApplication", "FR_uploadApprovedOrder", "FR_orderRefusal" })
    void givenReviewApplicationTaskIds_whenDMNIsEvaluated_thenTaskIsCompleted(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "reviewApplication", "completionMode", "Auto")
        ));
    }

    @Test
    void givenListForHearingShouldAutoCompleteReviewRefusedOrderTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_listForHearing");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "reviewRefusedOrder", "completionMode", "Auto")
        ));
    }

    @Test
    void givenIssueApplicationEvent_whenEvaluated_thenCompletesCheckApplicationTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_issueApplication");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of("taskType", "checkApplication", "completionMode", "Auto")
        ));
    }
}
