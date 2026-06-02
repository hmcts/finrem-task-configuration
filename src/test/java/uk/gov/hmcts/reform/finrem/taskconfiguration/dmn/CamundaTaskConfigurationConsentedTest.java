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

class CamundaTaskConfigurationConsentedTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    static void initialization() {
        currentDmnDecisionTable = DmnDecisionTable.WA_TASK_CONFIGURATION_DIVORCE_FINANCIALREMEDYMVP2;
    }

    @Test
    void ifThisTestFailsNeedsUpdatingWithYourChanges() {
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules().size(), is(17));
    }

    @Test
    void givenUnknownTaskTypeShouldReturnEmptyConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "unknownTaskType");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of()));
    }

    @Test
    void givenProcessScannedDocumentsTaskTypeShouldReturnConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "processScannedDocuments");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList(), is(List.of(
            Map.of("name", "workType", "value", "evidence", "canReconfigure", true),
            Map.of("name", "roleCategory", "value", "CTSC", "canReconfigure", true),
            Map.of("name", "description", "value",
                "[Attach scanned document]"
                    + "(/cases/case-details/${[CASE_REFERENCE]}/trigger/attachScannedDocs/attachScannedDocs1)",
                "canReconfigure", true),
            Map.of("name", "title", "value", "Process Scanned Documents", "canReconfigure", true),
            Map.of("name", "calculatedDates", "value", "nextHearingDate,dueDate,priorityDate", "canReconfigure", true),
            Map.of("name", "priorityDateOriginRef", "value", "nextHearingDate,dueDate",
                   "canReconfigure", true),
            Map.of("name", "nextHearingDate", "value", "", "canReconfigure", true),
            Map.of("name", "dueDateIntervalDays", "value", "5", "canReconfigure", true),
            Map.of("name", "dueDateNonWorkingCalendar", "value",
                "https://raw.githubusercontent.com/hmcts/finrem-task-configuration/master/src/main/resources/"
                    + "finrem-specific-holidays.json,https://www.gov.uk/bank-holidays/england-and-wales.json",
                "canReconfigure", true),
            Map.of("name", "dueDateNonWorkingDaysOfWeek", "value", "SATURDAY,SUNDAY", "canReconfigure", true),
            Map.of("name", "dueDateSkipNonWorkingDays", "value", "true", "canReconfigure", true),
            Map.of("name", "dueDateMustBeWorkingDay", "value", "No", "canReconfigure", true),
            Map.of("name", "majorPriority", "value", "5000", "canReconfigure", true),
            Map.of("name", "minorPriority", "value", "500", "canReconfigure", true),
            Map.of("name", "caseName", "value", "", "canReconfigure", true),
            Map.of("name", "region", "value", "", "canReconfigure", true),
            Map.of("name", "location", "value", "", "canReconfigure", true)
        )));
    }

    @Test
    void givenProcessScannedDocumentsWithHearingDateShouldReturnNextHearingDate() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(Map.of("value", Map.of("hearingDate", "2026-05-27")))
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();
        Map<String, Object> nextHearingDateRow = results.stream()
            .filter(r -> "nextHearingDate".equals(r.get("name")))
            .findFirst()
            .orElseThrow();
        assertThat(nextHearingDateRow.get("value"), is("2026-05-27"));
    }

    @Test
    void givenCaseDataShouldReturnCaseNameRegionAndLocation() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "caseNameHmctsInternal", "Applicant v Respondent",
            "caseManagementLocation", Map.of("region", "2", "baseLocation", "366796")
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(valueOf(results, "caseName"), is("Applicant v Respondent"));
        assertThat(valueOf(results, "region"), is("2"));
        assertThat(valueOf(results, "location"), is("366796"));
    }

    private static Object valueOf(List<Map<String, Object>> results, String name) {
        return results.stream()
            .filter(r -> name.equals(r.get("name")))
            .findFirst()
            .orElseThrow()
            .get("value");
    }
}
