package uk.gov.hmcts.reform.finrem.taskconfiguration.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.taskconfiguration.DmnDecisionTable;
import uk.gov.hmcts.reform.finrem.taskconfiguration.DmnDecisionTableBaseUnitTest;

import java.time.ZonedDateTime;
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
        assertThat(logic.getRules().size(), is(20));
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
        inputVariables.putValue("caseData", Map.of(
            "caseNameHmctsInternal", "Applicant v Respondent",
            "caseManagementLocation", Map.of("region", "2", "baseLocation", "366796")
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        ZonedDateTime beforeEvaluation = ZonedDateTime.now();
        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        ZonedDateTime afterEvaluation = ZonedDateTime.now();
        List<Map<String, Object>> actualResults = dmnDecisionTableResult.getResultList();

        // dueDateOrigin is now() so its value is asserted against the evaluation time below
        List<Map<String, Object>> expectedResults = List.of(
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
                "https://www.gov.uk/bank-holidays/england-and-wales.json",
                "canReconfigure", true),
            Map.of("name", "dueDateNonWorkingDaysOfWeek", "value", "SATURDAY,SUNDAY", "canReconfigure", true),
            Map.of("name", "dueDateSkipNonWorkingDays", "value", "true", "canReconfigure", true),
            Map.of("name", "dueDateMustBeWorkingDay", "value", "No", "canReconfigure", true),
            Map.of("name", "dueDateOrigin", "canReconfigure", true),
            Map.of("name", "dueDateTime", "value", "14:00", "canReconfigure", true),
            Map.of("name", "majorPriority", "value", "5000", "canReconfigure", true),
            Map.of("name", "minorPriority", "value", "500", "canReconfigure", true),
            Map.of("name", "caseName", "value", "Applicant v Respondent", "canReconfigure", true),
            Map.of("name", "region", "value", "2", "canReconfigure", true),
            Map.of("name", "location", "value", "366796", "canReconfigure", true),
            Map.of("name", "caseManagementCategory", "value", "FR Consented",
                "canReconfigure", true)
        );

        assertThat(actualResults.size(), is(expectedResults.size()));
        for (int idx = 0; idx < actualResults.size(); idx++) {
            Map<String, Object> actual = actualResults.get(idx);
            Map<String, Object> expected = expectedResults.get(idx);
            assertThat(actual.get("name"), is(expected.get("name")));
            assertThat(actual.get("canReconfigure"), is(expected.get("canReconfigure")));
            if ("dueDateOrigin".equals(expected.get("name"))) {
                ZonedDateTime dueDateOrigin = ZonedDateTime.parse(actual.get("value").toString());
                assertThat("dueDateOrigin should be the time the DMN was evaluated (now())",
                    !dueDateOrigin.isBefore(beforeEvaluation) && !dueDateOrigin.isAfter(afterEvaluation),
                    is(true));
            } else {
                assertThat(actual.get("value"), is(expected.get("value")));
            }
        }
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
    void givenMultipleHearingsShouldReturnFirstHearingDate() {
        // FEEL lists are 1-indexed, so listForHearings[1] is the first element.
        // With several hearings present, only the first hearing's date is used.
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", "2026-05-27")),
                Map.of("value", Map.of("hearingDate", "2026-08-15"))
            )
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate"), is("2026-05-27"));
    }

    @Test
    void givenDueDateConfigurationShouldCountFiveWorkingDaysUsingBankHolidayCalendar() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        // SLA is 5 working days from task creation: weekends and gov.uk bank holidays
        // are skipped when counting the interval
        // (the calculation itself is performed by wa-task-management-api from these attributes)
        assertThat(valueOf(results, "dueDateIntervalDays"), is("5"));
        assertThat(valueOf(results, "dueDateSkipNonWorkingDays"), is("true"));
        assertThat(valueOf(results, "dueDateNonWorkingDaysOfWeek"), is("SATURDAY,SUNDAY"));
        assertThat(valueOf(results, "dueDateNonWorkingCalendar"), is(
            "https://www.gov.uk/bank-holidays/england-and-wales.json"));

        // the resulting due date is allowed to land on a non-working day
        assertThat(valueOf(results, "dueDateMustBeWorkingDay"), is("No"));
        assertThat(valueOf(results, "dueDateTime"), is("14:00"));
    }

    @Test
    void givenDueDateConfigurationShouldPrioritiseByNextHearingDateThenDueDate() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        // priorityDate uses nextHearingDate when one is set, otherwise falls back to dueDate
        assertThat(valueOf(results, "calculatedDates"), is("nextHearingDate,dueDate,priorityDate"));
        assertThat(valueOf(results, "priorityDateOriginRef"), is("nextHearingDate,dueDate"));
    }

    @Test
    void givenNoHearingCollectionShouldReturnEmptyNextHearingDate() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate"), is(""));
    }

    @Test
    void givenEmptyHearingCollectionShouldReturnEmptyNextHearingDate() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of("listForHearings", List.of()));
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate"), is(""));
    }

    @Test
    void givenCaseDataWithoutMandatoryFieldsShouldReturnEmptyCaseNameRegionAndLocation() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "processScannedDocuments");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        // mandatory fields fall back to empty strings, which fail task initiation
        // downstream in the same way as null
        assertThat(valueOf(results, "caseName"), is(""));
        assertThat(valueOf(results, "region"), is(""));
        assertThat(valueOf(results, "location"), is(""));
    }

    private static Object valueOf(List<Map<String, Object>> results, String name) {
        return results.stream()
            .filter(r -> name.equals(r.get("name")))
            .findFirst()
            .orElseThrow()
            .get("value");
    }
}
