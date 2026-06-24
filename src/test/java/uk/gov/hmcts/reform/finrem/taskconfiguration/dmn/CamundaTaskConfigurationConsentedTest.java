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

import static org.assertj.core.api.Assertions.assertThat;

class CamundaTaskConfigurationConsentedTest extends DmnDecisionTableBaseUnitTest {

    @BeforeAll
    static void initialization() {
        currentDmnDecisionTable = DmnDecisionTable.WA_TASK_CONFIGURATION_DIVORCE_FINANCIALREMEDYMVP2;
    }

    @Test
    void ifThisTestFailsNeedsUpdatingWithYourChanges() {
        DmnDecisionTableImpl logic = (DmnDecisionTableImpl) decision.getDecisionLogic();
        assertThat(logic.getRules()).hasSize(24);
    }

    @Test
    void givenUnknownTaskTypeShouldReturnOnlyGenericConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "unknownTaskType");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        // generic rules apply to every task type, so an unknown type still returns the 14
        // generic attributes but none of the task-specific rows (roleCategory/workType/title/
        // description/dueDateIntervalDays/dueDateNonWorkingCalendar). roleCategory is scoped to
        // the CTSC task types, so it is not emitted for an unknown task type.
        List<Object> names = results.stream().map(r -> r.get("name")).toList();
        assertThat(results).hasSize(14);
        assertThat(names).contains("caseManagementCategory");
        assertThat(names).doesNotContain("roleCategory", "workType", "title", "description",
            "dueDateIntervalDays", "dueDateNonWorkingCalendar");
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

        // generic rules (apply to all tasks) are emitted first in RULE ORDER, then the
        // task-specific rows for processScannedDocuments
        // dueDateOrigin is now() so its value is asserted against the evaluation time below
        List<Map<String, Object>> expectedResults = List.of(
            Map.of("name", "roleCategory", "value", "CTSC", "canReconfigure", true),
            Map.of("name", "calculatedDates", "value", "nextHearingDate,dueDate,priorityDate", "canReconfigure", true),
            Map.of("name", "priorityDateOriginRef", "value", "nextHearingDate,dueDate",
                   "canReconfigure", true),
            Map.of("name", "nextHearingDate", "value", "", "canReconfigure", true),
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
                "canReconfigure", true),
            // dueDateIntervalDays is shared by both tasks (one combined rule), emitted before
            // the task-specific rows
            Map.of("name", "dueDateIntervalDays", "value", "5", "canReconfigure", true),
            Map.of("name", "workType", "value", "evidence", "canReconfigure", true),
            Map.of("name", "title", "value", "Process Scanned Documents", "canReconfigure", true),
            Map.of("name", "description", "value",
                "[Attach scanned document]"
                    + "(/cases/case-details/${[CASE_REFERENCE]}/trigger/attachScannedDocs/attachScannedDocs1)",
                "canReconfigure", true),
            Map.of("name", "dueDateNonWorkingCalendar", "value",
                "https://www.gov.uk/bank-holidays/england-and-wales.json",
                "canReconfigure", true)
        );

        assertThat(actualResults).hasSameSizeAs(expectedResults);
        for (int idx = 0; idx < actualResults.size(); idx++) {
            Map<String, Object> actual = actualResults.get(idx);
            Map<String, Object> expected = expectedResults.get(idx);
            assertThat(actual.get("name")).isEqualTo(expected.get("name"));
            assertThat(actual.get("canReconfigure")).isEqualTo(expected.get("canReconfigure"));
            if ("dueDateOrigin".equals(expected.get("name"))) {
                ZonedDateTime dueDateOrigin = ZonedDateTime.parse(actual.get("value").toString());
                assertThat(!dueDateOrigin.isBefore(beforeEvaluation) && !dueDateOrigin.isAfter(afterEvaluation))
                    .as("dueDateOrigin should be the time the DMN was evaluated (now())")
                    .isTrue();
            } else {
                assertThat(actual.get("value")).isEqualTo(expected.get("value"));
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
        assertThat(nextHearingDateRow.get("value")).isEqualTo("2026-05-27");
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

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo("2026-05-27");
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
        assertThat(valueOf(results, "dueDateIntervalDays")).isEqualTo("5");
        assertThat(valueOf(results, "dueDateSkipNonWorkingDays")).isEqualTo("true");
        assertThat(valueOf(results, "dueDateNonWorkingDaysOfWeek")).isEqualTo("SATURDAY,SUNDAY");
        assertThat(valueOf(results, "dueDateNonWorkingCalendar")).isEqualTo(
            "https://www.gov.uk/bank-holidays/england-and-wales.json");

        // the resulting due date is allowed to land on a non-working day
        assertThat(valueOf(results, "dueDateMustBeWorkingDay")).isEqualTo("No");
        assertThat(valueOf(results, "dueDateTime")).isEqualTo("14:00");
    }

    @Test
    void givenDueDateConfigurationShouldPrioritiseByNextHearingDateThenDueDate() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        // priorityDate uses nextHearingDate when one is set, otherwise falls back to dueDate
        assertThat(valueOf(results, "calculatedDates")).isEqualTo("nextHearingDate,dueDate,priorityDate");
        assertThat(valueOf(results, "priorityDateOriginRef")).isEqualTo("nextHearingDate,dueDate");
    }

    @Test
    void givenNoHearingCollectionShouldReturnEmptyNextHearingDate() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo("");
    }

    @Test
    void givenEmptyHearingCollectionShouldReturnEmptyNextHearingDate() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of("listForHearings", List.of()));
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo("");
    }

    @Test
    void givenCaseDataWithoutMandatoryFieldsShouldDefaultCaseNameAndEmptyRegionAndLocation() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "processScannedDocuments");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        // caseName falls back to a default; region and location fall back to empty
        // strings, which fail task initiation downstream in the same way as null
        assertThat(valueOf(results, "caseName")).isEqualTo("Financial Remedy");
        assertThat(valueOf(results, "region")).isEqualTo("");
        assertThat(valueOf(results, "location")).isEqualTo("");
    }

    @Test
    void givenProcessApprovedOrderTaskTypeShouldReturnConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "caseNameHmctsInternal", "Tony Stark v Pepper Potts",
            "caseManagementLocation", Map.of("region", "2", "baseLocation", "765324")
        ));
        inputVariables.putValue("taskType", "processApprovedOrder");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(20);
        assertThat(valueOf(results, "workType")).isEqualTo("routine_work");
        assertThat(valueOf(results, "roleCategory")).isEqualTo("CTSC");
        assertThat(valueOf(results, "title")).isEqualTo("Process Approved Order");
        assertThat(valueOf(results, "description"))
            .isEqualTo("[Amended Consent Order](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_amendedConsentOrder/FR_amendedConsentOrder1)");
        assertThat(valueOf(results, "caseManagementCategory")).isEqualTo("FR Consented");
        assertThat(valueOf(results, "dueDateIntervalDays")).isEqualTo("5");
        assertThat(valueOf(results, "dueDateNonWorkingCalendar")).isEqualTo(
            "https://www.gov.uk/bank-holidays/england-and-wales.json");
        assertThat(valueOf(results, "caseName")).isEqualTo("Tony Stark v Pepper Potts");
        assertThat(valueOf(results, "region")).isEqualTo("2");
        assertThat(valueOf(results, "location")).isEqualTo("765324");
    }

    private static Object valueOf(List<Map<String, Object>> results, String name) {
        return results.stream()
            .filter(r -> name.equals(r.get("name")))
            .findFirst()
            .orElseThrow()
            .get("value");
    }
}
