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

class CamundaTaskInitiationConsentedTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    static void initialization() {
        currentDmnDecisionTable = DmnDecisionTable.WA_TASK_INITIATION_DIVORCE_FINANCIALREMEDYMVP2;
    }

    @Test
    void givenUnknownEventShouldReturnEmptyResult() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "unknownEvent");
        inputVariables.putValue("postEventState", "");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    @Test
    void ifThisTestFailsNeedsUpdatingWithYourChanges() {
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules()).hasSize(1);
    }

    @Test
    void givenCaseSubmissionWithHelpWithFeesYesShouldCreateCheckHelpWithFeesTask() {
        // BA confirmed the Case Submission event (FR_applicationPaymentSubmission ->
        // applicationSubmitted) triggers the task, gated by helpWithFeesQuestion = "Yes"
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_applicationPaymentSubmission");
        inputVariables.putValue("postEventState", "applicationSubmitted");
        inputVariables.putValue("additionalData", Map.of("Data", Map.of("helpWithFeesQuestion", "Yes")));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.get(0);
        assertThat(result.get("taskId")).isEqualTo("checkHelpWithFees");
        assertThat(result.get("name")).isEqualTo("Check Help With Fees");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");

        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @Test
    void givenCaseSubmissionWithHelpWithFeesNoShouldNotCreateTask() {
        // A non-HWF submission reaches the same post state, so the helpWithFeesQuestion gate is
        // what distinguishes it - "No" must not create the task
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_applicationPaymentSubmission");
        inputVariables.putValue("postEventState", "applicationSubmitted");
        inputVariables.putValue("additionalData", Map.of("Data", Map.of("helpWithFeesQuestion", "No")));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    @Test
    void givenCaseSubmissionWithHelpWithFeesAbsentShouldNotCreateTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_applicationPaymentSubmission");
        inputVariables.putValue("postEventState", "applicationSubmitted");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    @Test
    void givenCaseSubmissionWithHelpWithFeesYesButUnexpectedPostStateShouldNotCreateTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_applicationPaymentSubmission");
        inputVariables.putValue("postEventState", "caseAdded");
        inputVariables.putValue("additionalData", Map.of("Data", Map.of("helpWithFeesQuestion", "Yes")));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }
}
