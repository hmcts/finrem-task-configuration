package uk.gov.hmcts.reform.finrem.taskconfiguration.dmn;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.taskconfiguration.utils.DmnXmlHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Cross-DMN consistency tests. Each test loads two DMN files and asserts
 * that task type identifiers are spelled the same way everywhere they appear,
 * catching typos that per-DMN tests cannot detect on their own.
 */
class CamundaTaskTypeConsistencyTest {

    private static final String TYPES_DMN       = "wa-task-types-divorce-financialremedymvp2.dmn";
    private static final String INITIATION_DMN  = "wa-task-initiation-divorce-financialremedymvp2.dmn";
    private static final String COMPLETION_DMN  = "wa-task-completion-divorce-financialremedymvp2.dmn";
    private static final String PERMISSIONS_DMN = "wa-task-permissions-divorce-financialremedymvp2.dmn";

    @Test
    void allInitiatedTaskIdsMustBeRegisteredInTaskTypesDmn() throws Exception {
        Set<String> registeredTypes = DmnXmlHelper.extractOutputColumn(TYPES_DMN, "taskTypeId");
        Set<String> initiatedIds    = DmnXmlHelper.extractOutputColumn(INITIATION_DMN, "taskId");

        assertFalse(registeredTypes.isEmpty(), "Task types DMN must have entries");
        assertFalse(initiatedIds.isEmpty(),    "Initiation DMN must produce at least one task");

        List<String> unregistered = initiatedIds.stream()
            .filter(id -> !registeredTypes.contains(id))
            .sorted()
            .toList();

        assertThat("Task IDs in initiation DMN not registered in task types DMN: " + unregistered,
            unregistered, is(List.of()));
    }

    @Test
    void initiatedTaskNamesMustMatchRegisteredTaskTypeNames() throws Exception {
        Map<String, String> registeredNames =
            DmnXmlHelper.extractOutputColumnPair(TYPES_DMN, "taskTypeId", "taskTypeName");
        Map<String, String> initiatedNames  = DmnXmlHelper.extractOutputColumnPair(INITIATION_DMN, "taskId", "name");

        List<String> mismatches = initiatedNames.entrySet().stream()
            .filter(e -> registeredNames.containsKey(e.getKey()))
            .filter(e -> !registeredNames.get(e.getKey()).equals(e.getValue()))
            .map(e -> String.format("'%s': initiation name='%s', types name='%s'",
                e.getKey(), e.getValue(), registeredNames.get(e.getKey())))
            .sorted()
            .toList();

        assertThat("Task display names differ between initiation DMN and types DMN: " + mismatches,
            mismatches, is(List.of()));
    }

    @Test
    void completedTaskTypesMustBeRegisteredInTaskTypesDmn() throws Exception {
        Set<String> registeredTypes = DmnXmlHelper.extractOutputColumn(TYPES_DMN, "taskTypeId");
        Set<String> completedTypes  = DmnXmlHelper.extractOutputColumn(COMPLETION_DMN, "taskType");

        assertFalse(completedTypes.isEmpty(), "Completion DMN must reference at least one task type");

        List<String> unregistered = completedTypes.stream()
            .filter(id -> !registeredTypes.contains(id))
            .sorted()
            .toList();

        assertThat("Task types in completion DMN not registered in task types DMN: " + unregistered,
            unregistered, is(List.of()));
    }

    @Test
    void allRegisteredTaskTypesMustHaveExplicitPermissions() throws Exception {
        Set<String> registeredTypes      = DmnXmlHelper.extractOutputColumn(TYPES_DMN, "taskTypeId");
        Set<String> typesWithPermissions = DmnXmlHelper.extractInputColumn(PERMISSIONS_DMN, 0);

        List<String> missing = registeredTypes.stream()
            .filter(id -> !typesWithPermissions.contains(id))
            .sorted()
            .toList();

        assertThat("Registered task types with no permission rules in permissions DMN: " + missing,
            missing, is(List.of()));
    }
}
