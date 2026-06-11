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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of()));
    }

    @Test
    void ifThisTestFailsNeedsUpdatingWithYourChanges() {
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules().size(), is(2));
    }

    @Test
    void givenAttachScannedDocsWithUnhandledEvidenceShouldCreateProcessScannedDocumentsTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "attachScannedDocs");
        inputVariables.putValue("postEventState", "");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results.size(), is(1));
        Map<String, Object> result = results.get(0);
        assertThat(result.get("taskId"), is("processScannedDocuments"));
        assertThat(result.get("name"), is("Process Scanned Documents"));
        assertThat(result.get("delayDuration"), is(0));
        assertThat(result.get("processCategories"), is("CHANGE_LATER_PROCESS_CATEGORIES"));

        // delayUntil is a json map; its delayUntil value is now() so it is non-deterministic
        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays"), is("0"));
        assertThat(delayUntil.get("delayUntil"), is(notNullValue()));
    }

    @Test
    void givenAttachScannedDocsWithEvidenceHandledNoShouldCreateProcessScannedDocumentsTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "attachScannedDocs");
        inputVariables.putValue("postEventState", "");
        inputVariables.putValue("additionalData", Map.of("Data", Map.of("evidenceHandled", "No")));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList().size(), is(1));
    }

    @Test
    void givenAttachScannedDocsWithEvidenceHandledYesShouldNotCreateTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "attachScannedDocs");
        inputVariables.putValue("postEventState", "");
        inputVariables.putValue("additionalData", Map.of("Data", Map.of("evidenceHandled", "Yes")));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of()));
    }

    private static Map<String, Object> additionalDataWithPensionDocuments(int numberOfDocuments) {
        List<Map<String, Object>> pensionCollection = IntStream.range(0, numberOfDocuments)
            .mapToObj(i -> Map.<String, Object>of("id", String.valueOf(i)))
            .toList();
        return Map.of("Data", Map.of("pensionCollection", pensionCollection));
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

        assertThat(results.size(), is(1));
        Map<String, Object> result = results.get(0);
        assertThat(result.get("taskId"), is("processApprovedOrder"));
        assertThat(result.get("name"), is("Process Approved Order"));
        assertThat(result.get("delayDuration"), is(0));
        assertThat(result.get("processCategories"), is("CHANGE_LATER_PROCESS_CATEGORIES"));

        @SuppressWarnings("unchecked")
        Map<String, Object> delayUntil = (Map<String, Object>) result.get("delayUntil");
        assertThat(delayUntil.get("delayUntilIntervalDays"), is("0"));
        assertThat(delayUntil.get("delayUntil"), is(notNullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR_approveApplication", "FR_uploadApprovedOrder"})
    void givenTriggerEventWithNoPensionDocumentsShouldNotCreateTask(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);
        inputVariables.putValue("postEventState", "consentOrderApproved");
        inputVariables.putValue("additionalData", additionalDataWithPensionDocuments(0));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"FR_approveApplication", "FR_uploadApprovedOrder"})
    void givenTriggerEventWithNoAdditionalDataShouldNotCreateTask(String eventId) {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", eventId);
        inputVariables.putValue("postEventState", "consentOrderApproved");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of()));
    }

    @Test
    void givenApproveApplicationWithUnexpectedPostStateShouldNotCreateTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "FR_approveApplication");
        inputVariables.putValue("postEventState", "consentOrderMade");
        inputVariables.putValue("additionalData", additionalDataWithPensionDocuments(1));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of()));
    }
}
