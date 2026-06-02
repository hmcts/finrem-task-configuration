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

class CamundaTaskPermissionConsentedTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    static void initialization() {
        currentDmnDecisionTable = DmnDecisionTable.WA_TASK_PERMISSIONS_DIVORCE_FINANCIALREMEDYMVP2;
    }

    @Test
    void givenUnknownTaskTypeShouldReturnEmptyPermissions() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "unknownTaskType"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of()));
    }

    @Test
    void ifThisTestFailsNeedsUpdatingWithYourChanges() {
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules().size(), is(2));
    }

    @Test
    void givenProcessScannedDocumentsTaskTypeShouldReturnPermissionsForCtscRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "processScannedDocuments"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of(
            Map.of(
                "name", "ctsc-admin",
                "value", "Read,Own,Claim,Unclaim,CancelOwn,CompleteOwn,Execute",
                "roleCategory", "CTSC",
                "authorisations", "FR_Managing_ScannedDocs",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "ctsc-team-leader",
                "value", "Read,Manage,Cancel,Complete,Unclaim,Unassign,"
                    + "Claim,Own,Execute,Assign",
                "roleCategory", "CTSC",
                "authorisations", "FR_Managing_ScannedDocs",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        )));
    }
}
