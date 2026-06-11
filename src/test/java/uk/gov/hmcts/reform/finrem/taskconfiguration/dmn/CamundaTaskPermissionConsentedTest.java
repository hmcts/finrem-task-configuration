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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
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
        assertThat(logic.getRules().size(), is(4));
    }

    @Test
    void givenProcessApprovedOrderTaskTypeShouldReturnPermissionsForCtscRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "processApprovedOrder"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of(
            Map.of(
                "name", "ctsc-admin",
                "value", "Read,Own,Claim,Unclaim,CancelOwn,CompleteOwn,Execute",
                "roleCategory", "CTSC",
                "authorisations", "FR_Processing_Orders",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "ctsc-team-leader",
                "value", "Read,Manage,Cancel,CancelOwn,Complete,CompleteOwn,Unclaim,Unassign,"
                    + "Claim,Own,Execute,Assign",
                "roleCategory", "CTSC",
                "authorisations", "FR_Processing_Orders",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        )));
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

    @Test
    void givenProcessScannedDocumentsTaskTypeShouldNotReturnPermissionsForOtherRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "processScannedDocuments"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<String> rolesWithPermissions = dmnDecisionTableResult.getResultList().stream()
            .map(permission -> permission.get("name").toString())
            .toList();

        assertThat(rolesWithPermissions, is(List.of("ctsc-admin", "ctsc-team-leader")));
        assertThat(rolesWithPermissions, not(hasItems(
            "ctsc",
            "hearing-centre-admin",
            "hearing-centre-team-leader",
            "judge",
            "tribunal-caseworker",
            "task-supervisor"
        )));
    }
}
