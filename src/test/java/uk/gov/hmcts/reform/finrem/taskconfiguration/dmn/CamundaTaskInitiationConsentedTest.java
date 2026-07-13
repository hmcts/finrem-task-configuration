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
import java.util.stream.IntStream;

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
      
        assertThat(logic.getRules()).hasSize(10);
    }

    @Test
    void givenAttachScannedDocsWithUnhandledEvidenceShouldCreateProcessScannedDocumentsTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "attachScannedDocs");
        inputVariables.putValue("postEventState", "");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.getFirst();

        assertThat(result.get("taskId")).isEqualTo("processScannedDocuments");
        assertThat(result.get("name")).isEqualTo("Process Scanned Documents");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");

        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @Test
    void givenAttachScannedDocsWithEvidenceHandledNoShouldCreateProcessScannedDocumentsTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "attachScannedDocs");
        inputVariables.putValue("postEventState", "");
        inputVariables.putValue("additionalData", Map.of("Data", Map.of("evidenceHandled", "No")));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.getFirst();

        assertThat(result.get("taskId")).isEqualTo("processScannedDocuments");
        assertThat(result.get("name")).isEqualTo("Process Scanned Documents");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");

        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @Test
    void givenAttachScannedDocsWithEvidenceHandledYesShouldNotCreateTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "attachScannedDocs");
        inputVariables.putValue("postEventState", "");
        inputVariables.putValue("additionalData", Map.of("Data", Map.of("evidenceHandled", "Yes")));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();

    }

    @Test
    void givenNonAttachScannedDocsEventWithEvidenceHandledNoShouldNotCreateTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "someOtherEvent");
        inputVariables.putValue("postEventState", "");
        inputVariables.putValue("additionalData", Map.of("Data", Map.of("evidenceHandled", "No")));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    @Test
    void givenNonAttachScannedDocsEventWithEvidenceHandledAbsentShouldNotCreateTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "someOtherEvent");
        inputVariables.putValue("postEventState", "");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    @Test
    void givenTriggerEventReferToJudgeShouldCreateCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_referToJudge");
        inputVariables.putValue("postEventState", "referredToJudge");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.getFirst();

        assertThat(result.get("taskId")).isEqualTo("checkResponseReceived");
        assertThat(result.get("name")).isEqualTo("Check Response Received");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");

        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @Test
    void givenTriggerEventAssignToJudgeShouldCreateCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_assignToJudge");
        inputVariables.putValue("postEventState", "awaitingJudiciaryResponse");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.getFirst();

        assertThat(result.get("taskId")).isEqualTo("checkResponseReceived");
        assertThat(result.get("name")).isEqualTo("Check Response Received");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");

        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @Test
    void givenTriggerEventAwaitingInfoShouldCreateCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_awaitingInfo");
        inputVariables.putValue("postEventState", "awaitingInfo");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.getFirst();

        assertThat(result.get("taskId")).isEqualTo("checkResponseReceived");
        assertThat(result.get("name")).isEqualTo("Check Response Received");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");

        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @Test
    void givenTriggerEventGeneralEmailShouldCreateCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_generalEmail");
        inputVariables.putValue("postEventState", "");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.getFirst();

        assertThat(result.get("taskId")).isEqualTo("checkResponseReceived");
        assertThat(result.get("name")).isEqualTo("Check Response Received");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");

        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @Test
    void givenTriggerEventListForHearingShouldCreateCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_listForHearing");
        inputVariables.putValue("postEventState", "readyForHearing");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.getFirst();

        assertThat(result.get("taskId")).isEqualTo("checkResponseReceived");
        assertThat(result.get("name")).isEqualTo("Check Response Received");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");

        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @Test
    void givenTriggerEventCloseShouldCreateCheckResponseReceivedTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_close");
        inputVariables.putValue("postEventState", "close");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.getFirst();

        assertThat(result.get("taskId")).isEqualTo("checkResponseReceived");
        assertThat(result.get("name")).isEqualTo("Check Response Received");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");

        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR_callbackRejectedOrder","FR_generalOrder"})
    void givenTriggerEventsShouldCreateCheckResponseReceivedTask(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);
        inputVariables.putValue("postEventState", "orderMade");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.getFirst();

        assertThat(result.get("taskId")).isEqualTo("checkResponseReceived");
        assertThat(result.get("name")).isEqualTo("Check Response Received");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");
        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR_approveApplication", "FR_uploadApprovedOrder"})
    void givenTriggerEventWithPensionDocumentsShouldCreateProcessApprovedOrderTask(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);
        inputVariables.putValue("postEventState", "consentOrderApproved");
        inputVariables.putValue("additionalData", additionalDataWithPensionDocuments(1));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(1);
        Map<String, Object> result = results.getFirst();

        assertThat(result.get("taskId")).isEqualTo("processApprovedOrder");
        assertThat(result.get("name")).isEqualTo("Process Approved Order");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");
        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays")).isEqualTo("0");
        assertThat(delayUntil.get("delayUntil")).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR_approveApplication", "FR_uploadApprovedOrder"})
    void givenTriggerEventWithNoPensionDocumentsShouldNotCreateTask(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);
        inputVariables.putValue("postEventState", "consentOrderApproved");
        inputVariables.putValue("additionalData", additionalDataWithPensionDocuments(0));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR_approveApplication", "FR_uploadApprovedOrder"})
    void givenTriggerEventWithAdditionalDataButNoPensionCollectionShouldNotCreateTask(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);
        inputVariables.putValue("postEventState", "consentOrderApproved");
        // additionalData.Data is present but has no pensionCollection key, so the gate falls
        // through its null-guard to 0 documents and no task is created
        inputVariables.putValue("additionalData", Map.of("Data", Map.of("someOtherField", "value")));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR_approveApplication", "FR_uploadApprovedOrder"})
    void givenTriggerEventWithNoAdditionalDataShouldNotCreateTask(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);
        inputVariables.putValue("postEventState", "consentOrderApproved");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR_approveApplication", "FR_uploadApprovedOrder"})
    void givenTriggerEventWithUnexpectedPostStateShouldNotCreateTask(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);
        inputVariables.putValue("postEventState", "consentOrderMade");
        inputVariables.putValue("additionalData", additionalDataWithPensionDocuments(1));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
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
        Map<String, Object> result = results.getFirst();
        assertThat(result.get("taskId")).isEqualTo("checkHelpWithFees");
        assertThat(result.get("name")).isEqualTo("Check Help With Fees");
        assertThat(result.get("delayDuration")).isEqualTo(0);
        assertThat(result.get("processCategories")).isEqualTo("CHANGE_LATER_PROCESS_CATEGORIES");

        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
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

    @Test
    void givenIssueApplicationEventIdAndReferredToJudgeState_whenEvaluated_thenInitiatesReviewApplicationTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_issueApplication");
        inputVariables.putValue("postEventState", "referredToJudge");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList())
            .satisfies(result -> assertThat(result.getFirst().get("delayUntil")).isNotNull())
            .usingRecursiveComparison()
            .ignoringFields("delayUntil")
            .isEqualTo(List.of(
                Map.of(
                    "taskId", "reviewApplication",
                    "name", "Review Application",
                    "delayDuration", 0,
                    "processCategories", "CHANGE_LATER_PROCESS_CATEGORIES"
                )
            ));
    }

    @Test
    void givenIssueApplicationEventIdAndOtherState_whenEvaluated_thenDoesNotInitiateReviewApplicationTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_issueApplication");
        inputVariables.putValue("postEventState", "someState");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    private static Map<String, Object> additionalDataWithPensionDocuments(int numberOfDocuments) {
        List<Map<String, Object>> pensionCollection = IntStream.range(0, numberOfDocuments)
            .mapToObj(i -> Map.<String, Object>of("id", String.valueOf(i)))
            .toList();
        return Map.of("Data", Map.of("pensionCollection", pensionCollection));
    }
}
