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

        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
    }

    @Test
    void ifThisTestFailsNeedsUpdatingWithYourChanges() {
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules())
            .as("Number of defined permission rules has changed.")
            .hasSize(16);
    }

    @Test
    void givenProcessScannedDocumentsTaskTypeShouldNotReturnPermissionsForOtherRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "processScannedDocuments"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<String> rolesWithPermissions = dmnDecisionTableResult.getResultList().stream()
            .map(permission -> permission.get("name").toString())
            .toList();

        assertThat(rolesWithPermissions).isEqualTo(List.of("ctsc", "ctsc-team-leader"));
        assertThat(rolesWithPermissions).doesNotContain(
            "ctsc-admin",
            "hearing-centre-admin",
            "hearing-centre-team-leader",
            "judge",
            "tribunal-caseworker",
            "task-supervisor"
        );
    }

    @Test
    void givenProcessScannedDocumentsTaskTypeShouldReturnPermissionsForCtscRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "processScannedDocuments"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of(
                "name", "ctsc",
                "value", "Read,Own,Claim,CancelOwn,CompleteOwn,Execute",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:ManageScannedDocuments",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "ctsc-team-leader",
                "value", "Read,Manage,Cancel,Complete,Unclaim,Unassign,"
                    + "Claim,Own,Execute,Assign",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:ManageScannedDocuments",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        ));
    }

    @Test
    void givenCheckResponseReceivedTaskTypeShouldNotReturnPermissionsForOtherRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "checkResponseReceived"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<String> rolesWithPermissions = dmnDecisionTableResult.getResultList().stream()
            .map(permission -> permission.get("name").toString())
            .toList();

        assertThat(rolesWithPermissions).isEqualTo(List.of("ctsc", "ctsc-team-leader"));
        assertThat(rolesWithPermissions).doesNotContain(
            "ctsc-admin",
            "hearing-centre-admin",
            "hearing-centre-team-leader",
            "judge",
            "tribunal-caseworker",
            "task-supervisor"
        );
    }

    @Test
    void givenCheckResponseReceivedTaskTypeShouldReturnPermissionsForCtscRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "checkResponseReceived"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of(
                "name", "ctsc",
                "value", "Read,Own,Claim,CancelOwn,CompleteOwn,Execute",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:CheckingApplications",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "ctsc-team-leader",
                "value", "Read,Manage,Cancel,Complete,Unclaim,Unassign,"
                    + "Claim,Own,Execute,Assign",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:CheckingApplications",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        ));
    }

    @Test
    void givenProcessApprovedOrderTaskTypeShouldReturnPermissionsForCtscRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "processApprovedOrder"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of(
                "name", "ctsc",
                "value", "Read,Own,Claim,CancelOwn,CompleteOwn,Execute",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:ProcessApprovedOrders",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "ctsc-team-leader",
                "value", "Read,Manage,Cancel,Complete,Unclaim,Unassign,"
                    + "Claim,Own,Execute,Assign",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:ProcessApprovedOrders",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        ));
    }

    @Test
    void givenCheckHelpWithFeesTaskTypeShouldReturnPermissionsForCtscRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "checkHelpWithFees"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of(
                "name", "ctsc",
                "value", "Read,Own,Claim,CancelOwn,CompleteOwn,Execute",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:CheckingHWF",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "ctsc-team-leader",
                "value", "Read,Manage,Cancel,Complete,Unclaim,Unassign,"
                    + "Claim,Own,Execute,Assign",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:CheckingHWF",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        ));
    }

    @Test
    void givenCheckHelpWithFeesTaskTypeShouldNotGrantUnclaimToCtscCaseworker() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "checkHelpWithFees"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        String ctscPermissions = dmnDecisionTableResult.getResultList().stream()
            .filter(permission -> "ctsc".equals(permission.get("name")))
            .map(permission -> permission.get("value").toString())
            .findFirst()
            .orElseThrow();

        // BA confirmed a CTSC caseworker cannot unclaim a task
        assertThat(ctscPermissions).doesNotContain("Unclaim");
    }

    @Test
    void givenReviewApplicationTaskType_WhenEvaluated_ThenReturnsAuthorisation() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "reviewApplication"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of(
                "name", "leadership-judge",
                "value", "Read,Manage,Cancel,Complete,Unclaim,Unassign,Claim,Own,Execute,Assign",
                "roleCategory", "JUDICIAL",
                "authorisations", "410",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "judge",
                "value", "Read,CancelOwn, CompleteOwn,Claim,Unclaim,Execute",
                "roleCategory", "JUDICIAL",
                "authorisations", "410",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        ));
    }

    @Test
    void givenReviewRefusedOrderTaskTypeShouldReturnPermissionsForCtscRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "reviewRefusedOrder"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of(
                "name", "ctsc",
                "value", "Read,Own,Claim,CancelOwn,CompleteOwn,Execute",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:CheckRefusedOrder",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "ctsc-team-leader",
                "value", "Read,Manage,Cancel,Complete,Unclaim,Unassign,"
                    + "Claim,Own,Execute,Assign",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:CheckRefusedOrder",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        ));
    }

    @Test
    void givenReviewRefusedOrderTaskTypeShouldNotGrantUnclaimToCtscCaseworker() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "reviewRefusedOrder"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        String ctscPermissions = dmnDecisionTableResult.getResultList().stream()
            .filter(permission -> "ctsc".equals(permission.get("name")))
            .map(permission -> permission.get("value").toString())
            .findFirst()
            .orElseThrow();

        // BA confirmed a CTSC caseworker cannot unclaim a task
        assertThat(ctscPermissions).doesNotContain("Unclaim");
    }

    @Test
    void givenCheckApplicationTaskType_whenEvaluated_thenReturnsThePermissionsForCtscRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "checkApplication"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of(
                "name", "ctsc",
                "value", "Read,Own,Claim,CancelOwn,CompleteOwn,Execute",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:CheckingApplications",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "ctsc-team-leader",
                "value", "Read,Manage,Cancel,Complete,Unclaim,Unassign,"
                    + "Claim,Own,Execute,Assign",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:CheckingApplications",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        ));
    }

    @Test
    void givenReviewOrderResponseTaskTypeShouldReturnPermissionsForCtscRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "reviewOrderResponse"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEqualTo(List.of(
            Map.of(
                "name", "ctsc",
                "value", "Read,Own,Claim,CancelOwn,CompleteOwn,Execute",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:CheckOrderResponse",
                "assignmentPriority", 1,
                "autoAssignable", false
            ),
            Map.of(
                "name", "ctsc-team-leader",
                "value", "Read,Manage,Cancel,Complete,Unclaim,Unassign,"
                    + "Claim,Own,Execute,Assign",
                "roleCategory", "CTSC",
                "authorisations", "SKILL:ABA2:CheckOrderResponse",
                "assignmentPriority", 1,
                "autoAssignable", false
            )
        ));
    }

    @Test
    void givenReviewOrderResponseTaskTypeShouldNotReturnPermissionsForOtherRoles() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("taskAttributes", Map.of("taskType", "reviewOrderResponse"));

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        List<String> rolesWithPermissions = dmnDecisionTableResult.getResultList().stream()
            .map(permission -> permission.get("name").toString())
            .toList();

        assertThat(rolesWithPermissions)
            .isEqualTo(List.of("ctsc", "ctsc-team-leader"))
            .doesNotContain(
                "ctsc-admin",
                "hearing-centre-admin",
                "hearing-centre-team-leader",
                "judge",
                "tribunal-caseworker",
                "task-supervisor"
            );
    }
}
