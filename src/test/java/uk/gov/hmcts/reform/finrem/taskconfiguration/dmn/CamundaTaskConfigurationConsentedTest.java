package uk.gov.hmcts.reform.finrem.taskconfiguration.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.taskconfiguration.DmnDecisionTable;
import uk.gov.hmcts.reform.finrem.taskconfiguration.DmnDecisionTableBaseUnitTest;

import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class CamundaTaskConfigurationConsentedTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    static void initialization() {
        currentDmnDecisionTable = DmnDecisionTable.WA_TASK_CONFIGURATION_DIVORCE_FINANCIALREMEDYMVP2;
    }

    @Test
    void ifThisTestFailsNeedsUpdatingWithYourChanges() {
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules()).hasSize(20);
    }

    @Test
    void givenNoTaskTypeShouldReturnGenericConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "unknownTaskType");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        Map<String, Object> results = dmnDecisionTableResult.getResultList()
            .stream().collect(Collectors.toMap(map -> (String) map.get("name"), map -> map.get("value")));

        assertThat(results)
            .satisfies(result -> assertThat(result.get("dueDateOrigin")).isNotNull())
            .usingRecursiveComparison()
            .ignoringFields("dueDateOrigin")
            .isEqualTo(Map.ofEntries(
                entry("calculatedDates", "nextHearingDate,dueDate,priorityDate"),
                entry("priorityDateOriginRef", "nextHearingDate,dueDate"),
                entry("nextHearingDate", ""),
                entry("dueDateNonWorkingDaysOfWeek", "SATURDAY,SUNDAY"),
                entry("dueDateSkipNonWorkingDays", "true"),
                entry("dueDateMustBeWorkingDay", "No"),
                entry("dueDateTime", "14:00"),
                entry("majorPriority", "5000"),
                entry("minorPriority", "500"),
                entry("caseName", "Financial Remedy"),
                entry("region", ""),
                entry("location", ""),
                entry("caseManagementCategory", "FR Consented")
            ));
    }

    @Test
    void givenReviewApplicationTaskType_whenEvaluated_ThenReturnsConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue(
            "caseData", Map.of(
                "caseNameHmctsInternal", "Applicant v Respondent",
                "caseManagementLocation", Map.of("region", "2", "baseLocation", "366796")
            )
        );
        inputVariables.putValue("taskType", "reviewApplication");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);

        Map<String, Object> results = dmnDecisionTableResult.getResultList()
            .stream().collect(Collectors.toMap(map -> (String) map.get("name"), map -> map.get("value")));

        assertThat(results)
            .satisfies(result -> assertThat(result.get("dueDateOrigin")).isNotNull())
            .usingRecursiveComparison()
            .ignoringFields("dueDateOrigin")
            .isEqualTo(Map.ofEntries(
                entry("calculatedDates", "nextHearingDate,dueDate,priorityDate"),
                entry("priorityDateOriginRef", "nextHearingDate,dueDate"),
                entry("nextHearingDate", ""),
                entry("dueDateNonWorkingDaysOfWeek", "SATURDAY,SUNDAY"),
                entry("dueDateSkipNonWorkingDays", "true"),
                entry("dueDateMustBeWorkingDay", "No"),
                entry("dueDateTime", "14:00"),
                entry("majorPriority", "5000"),
                entry("minorPriority", "500"),
                entry("caseName", "Applicant v Respondent"),
                entry("region", "2"),
                entry("location", "366796"),
                entry("caseManagementCategory", "FR Consented"),
                entry("workType", "decision_making_work"),
                entry("roleCategory", "JUDICIAL"),
                entry("description",
                      "[Approve Application](/cases/case-details/${[CASE_REFERENCE]}/trigger"
                          + "/FR_approveApplication/FR_approveApplication1)"
                          + "<br>[Upload Approved Order](/cases/case-details/${[CASE_REFERENCE]}/trigger"
                          + "/FR_uploadApprovedOrder/FR_uploadApprovedOrder1)"
                          + "<br>[Application Not Approved]"
                          + "(/cases/case-details/${[CASE_REFERENCE]}/trigger/FR_orderRefusal/FR_orderRefusal1)"),
                entry("title", "Review Application"),
                entry("dueDateIntervalDays", "10"),
                entry("dueDateNonWorkingCalendar", "https://www.gov.uk/bank-holidays/england-and-wales.json")
            ));
    }
}
