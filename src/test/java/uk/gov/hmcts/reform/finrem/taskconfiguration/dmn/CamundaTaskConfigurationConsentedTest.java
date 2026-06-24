package uk.gov.hmcts.reform.finrem.taskconfiguration.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.taskconfiguration.DmnDecisionTable;
import uk.gov.hmcts.reform.finrem.taskconfiguration.DmnDecisionTableBaseUnitTest;

import java.time.LocalDate;
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
        assertThat(logic.getRules()).hasSize(20);
    }

    @Test
    void givenUnknownTaskTypeShouldReturnEmptyConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "unknownTaskType");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        assertThat(dmnDecisionTableResult.getResultList()).isEmpty();
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
    void givenProcessScannedDocumentsWithUpcomingHearingShouldReturnNextHearingDate() {
        String upcomingHearing = LocalDate.now().plusDays(30).toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(Map.of("value", Map.of("hearingDate", upcomingHearing)))
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();
        Map<String, Object> nextHearingDateRow = results.stream()
            .filter(r -> "nextHearingDate".equals(r.get("name")))
            .findFirst()
            .orElseThrow();
        assertThat(nextHearingDateRow.get("value")).isEqualTo(upcomingHearing);
    }

    @Test
    void givenMultipleHearingsShouldReturnEarliestUpcomingHearingDate() {
        // Only hearings on or after today are considered (date(hearingDate) >= today()).
        // finrem-cos stores the collection earliest-first and FEEL lists are 1-indexed, so
        // [1] of the upcoming-only filter is the next hearing. A past hearing is skipped.
        String pastHearing = LocalDate.now().minusDays(10).toString();
        String nextUpcomingHearing = LocalDate.now().plusDays(5).toString();
        String laterUpcomingHearing = LocalDate.now().plusDays(20).toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", pastHearing)),
                Map.of("value", Map.of("hearingDate", nextUpcomingHearing)),
                Map.of("value", Map.of("hearingDate", laterUpcomingHearing))
            )
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(nextUpcomingHearing);
    }

    @Test
    void givenOnlyPastHearingsShouldReturnEmptyNextHearingDate() {
        // All hearings are in the past, so none qualify as the next hearing.
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", LocalDate.now().minusDays(20).toString())),
                Map.of("value", Map.of("hearingDate", LocalDate.now().minusDays(5).toString()))
            )
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo("");
    }

    @Test
    void givenHearingEarlierTodayShouldStillReturnTodayAsNextHearingDate() {
        // hearingDate is a date-only CCD field, so a hearing held earlier today is
        // indistinguishable from one later today. With date(hearingDate) >= today() a same-day
        // hearing is intentionally still treated as the next hearing, while an older hearing
        // in the same collection is skipped.
        String today = LocalDate.now().toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", LocalDate.now().minusDays(3).toString())),
                Map.of("value", Map.of("hearingDate", today))
            )
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(today);
    }

    @Test
    void givenSingleHearingDatedTodayShouldReturnTodayAsNextHearingDate() {
        // Boundary case: date(hearingDate) >= today() is inclusive, so a hearing dated exactly
        // today qualifies as the next hearing.
        String today = LocalDate.now().toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(Map.of("value", Map.of("hearingDate", today)))
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(today);
    }

    @Test
    void givenSinglePastHearingShouldReturnEmptyNextHearingDate() {
        // Negative case: a lone past hearing does not qualify as the next hearing.
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", LocalDate.now().minusDays(1).toString()))
            )
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo("");
    }

    @Test
    void givenHearingWithMissingDateShouldBeSkippedAndReturnNextValidHearing() {
        // Edge case: a hearing can exist before its date is set. A missing hearingDate must be
        // skipped without failing evaluation, and the next valid upcoming hearing returned.
        String upcomingHearing = LocalDate.now().plusDays(7).toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of()),
                Map.of("value", Map.of("hearingDate", upcomingHearing))
            )
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(upcomingHearing);
    }

    @Test
    void givenAllHearingsMissingDateShouldReturnEmptyNextHearingDate() {
        // Negative case: no hearing has a date, so there is no next hearing date.
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of()),
                Map.of("value", Map.of())
            )
        ));
        inputVariables.putValue("taskType", "processScannedDocuments");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo("");
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
    void givenCaseDataWithoutMandatoryFieldsShouldReturnEmptyCaseNameRegionAndLocation() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "processScannedDocuments");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        // mandatory fields fall back to empty strings, which fail task initiation
        // downstream in the same way as null
        assertThat(valueOf(results, "caseName")).isEqualTo("");
        assertThat(valueOf(results, "region")).isEqualTo("");
        assertThat(valueOf(results, "location")).isEqualTo("");
    }

    private static Object valueOf(List<Map<String, Object>> results, String name) {
        return results.stream()
            .filter(r -> name.equals(r.get("name")))
            .findFirst()
            .orElseThrow()
            .get("value");
    }
}
