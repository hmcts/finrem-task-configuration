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

import static org.hamcrest.CoreMatchers.is;
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
        assertThat(logic.getRules().size(), is(1));
    }

    @Test
    void givenAttachScannedDocsWithUnhandledEvidenceShouldCreateProcessScannedDocumentsTask() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("eventId", "attachScannedDocs");
        inputVariables.putValue("postEventState", "");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of(
            Map.of(
                "taskId", "processScannedDocuments",
                "name", "Process Scanned Documents",
                "delayDuration", 0,
                "processCategories", "CHANGE_LATER_PROCESS_CATEGORIES"
            )
        )));
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
}
