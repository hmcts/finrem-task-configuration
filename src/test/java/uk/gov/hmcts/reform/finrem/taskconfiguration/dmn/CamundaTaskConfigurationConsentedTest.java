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
        assertThat(logic.getRules())
            .as("Number of defined task configuration rules has changed.")
            .hasSize(42);
    }

    @Test
    void givenNoTaskType_whenEvaluated_thenReturnsGenericConfiguration() {
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
                entry("dueDateNonWorkingCalendar", "https://www.gov.uk/bank-holidays/england-and-wales.json"),
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
    void givenUpcomingHearingShouldReturnNextHearingDate() {
        String upcomingHearing = LocalDate.now().plusDays(30).toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(Map.of("value", Map.of("hearingDate", upcomingHearing)))
        ));
        inputVariables.putValue("taskType", "otherTaskType");

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
        // The upcoming-only filter is sorted ascending by hearingDate before taking [1], so the
        // earliest upcoming hearing is returned regardless of the collection's order. Here the
        // input is already earliest-first and a past hearing is skipped.
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
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(nextUpcomingHearing);
    }

    @Test
    void givenUpcomingHearingsInAscendingOrderShouldReturnEarliestUpcomingHearingDate() {
        // Sorting is order-independent: when the upcoming hearings are already ascending, the
        // sort is a no-op and the earliest (first) hearing is still returned.
        String earliest = LocalDate.now().plusDays(3).toString();
        String middle = LocalDate.now().plusDays(15).toString();
        String latest = LocalDate.now().plusDays(40).toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", earliest)),
                Map.of("value", Map.of("hearingDate", middle)),
                Map.of("value", Map.of("hearingDate", latest))
            )
        ));
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(earliest);
    }

    @Test
    void givenUpcomingHearingsPartiallySortedShouldReturnEarliestUpcomingHearingDate() {
        // Half-sorted input: the earliest hearing is not first in the collection, so the sort
        // must reorder the list before [1] to return the correct next hearing.
        String earliest = LocalDate.now().plusDays(4).toString();
        String middle = LocalDate.now().plusDays(12).toString();
        String latest = LocalDate.now().plusDays(25).toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", middle)),
                Map.of("value", Map.of("hearingDate", earliest)),
                Map.of("value", Map.of("hearingDate", latest))
            )
        ));
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(earliest);
    }

    @Test
    void givenUpcomingHearingsInDescendingOrderShouldReturnEarliestUpcomingHearingDate() {
        // Worst case for an unsorted [1]: the collection is fully reversed (latest-first), so
        // without the sort [1] would wrongly return the latest hearing. The sort guarantees the
        // earliest upcoming hearing is returned.
        String earliest = LocalDate.now().plusDays(2).toString();
        String middle = LocalDate.now().plusDays(18).toString();
        String latest = LocalDate.now().plusDays(50).toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", latest)),
                Map.of("value", Map.of("hearingDate", middle)),
                Map.of("value", Map.of("hearingDate", earliest))
            )
        ));
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(earliest);
    }

    @Test
    void givenDuplicateEarliestUpcomingHearingsShouldReturnThatDate() {
        // Tie case: two hearings share the earliest upcoming date. The sort comparator is strict
        // (no swap on equal dates), so evaluation must not error and the shared earliest date is
        // returned regardless of which duplicate the sort settles on first.
        String earliest = LocalDate.now().plusDays(6).toString();
        String later = LocalDate.now().plusDays(21).toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", later)),
                Map.of("value", Map.of("hearingDate", earliest)),
                Map.of("value", Map.of("hearingDate", earliest))
            )
        ));
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(earliest);
    }

    @Test
    void givenMixedPastMissingAndUnsortedUpcomingHearingsShouldReturnEarliestUpcomingHearingDate() {
        // Realistic messy collection: a past hearing, a hearing with no date yet, and upcoming
        // hearings in no particular order. The filter must drop the past and dateless entries and
        // the sort must order the survivors so the earliest upcoming hearing is returned.
        String earliest = LocalDate.now().plusDays(8).toString();
        String latest = LocalDate.now().plusDays(35).toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", latest)),
                Map.of("value", Map.of("hearingDate", LocalDate.now().minusDays(9).toString())),
                Map.of("value", Map.of()),
                Map.of("value", Map.of("hearingDate", earliest))
            )
        ));
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(earliest);
    }

    @Test
    void givenPastHearingsInterleavedBetweenUnsortedUpcomingShouldReturnEarliestUpcoming() {
        // Filtering does not reorder: past hearings interleaved between out-of-order upcoming
        // hearings are removed in place, leaving the upcoming ones still unsorted ([latest,
        // earliest, middle]). The sort must then surface the earliest, which here sits in the
        // middle of both the original and the filtered collection.
        String earliest = LocalDate.now().plusDays(3).toString();
        String middle = LocalDate.now().plusDays(16).toString();
        String latest = LocalDate.now().plusDays(45).toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", latest)),
                Map.of("value", Map.of("hearingDate", LocalDate.now().minusDays(12).toString())),
                Map.of("value", Map.of("hearingDate", earliest)),
                Map.of("value", Map.of("hearingDate", LocalDate.now().minusDays(2).toString())),
                Map.of("value", Map.of("hearingDate", middle))
            )
        ));
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(earliest);
    }

    @Test
    void givenTodayHearingListedLastAmongUpcomingShouldReturnToday() {
        // Boundary + ordering combined: a hearing dated exactly today is the earliest qualifying
        // hearing but appears last in the collection. The inclusive >= today() filter keeps it and
        // the sort surfaces it ahead of the later upcoming hearings.
        String today = LocalDate.now().toString();
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "listForHearings", List.of(
                Map.of("value", Map.of("hearingDate", LocalDate.now().plusDays(14).toString())),
                Map.of("value", Map.of("hearingDate", LocalDate.now().plusDays(2).toString())),
                Map.of("value", Map.of("hearingDate", today))
            )
        ));
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo(today);
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
        inputVariables.putValue("taskType", "otherTaskType");

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
        inputVariables.putValue("taskType", "otherTaskType");

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
        inputVariables.putValue("taskType", "otherTaskType");

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
        inputVariables.putValue("taskType", "otherTaskType");

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
        inputVariables.putValue("taskType", "otherTaskType");

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
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo("");
    }

    @Test
    void givenDueDateConfigurationShouldPrioritiseByNextHearingDateThenDueDate() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        // priorityDate uses nextHearingDate when one is set, otherwise falls back to dueDate
        assertThat(valueOf(results, "calculatedDates")).isEqualTo("nextHearingDate,dueDate,priorityDate");
        assertThat(valueOf(results, "priorityDateOriginRef")).isEqualTo("nextHearingDate,dueDate");
    }

    @Test
    void givenNoHearingCollectionShouldReturnEmptyNextHearingDate() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo("");
    }

    @Test
    void givenEmptyHearingCollectionShouldReturnEmptyNextHearingDate() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of("listForHearings", List.of()));
        inputVariables.putValue("taskType", "otherTaskType");

        List<Map<String, Object>> results = evaluateDmnTable(inputVariables).getResultList();

        assertThat(valueOf(results, "nextHearingDate")).isEqualTo("");
    }

    @Test
    void givenCaseDataWithoutMandatoryFieldsShouldReturnDefaultCaseNameRegionAndLocation() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "otherTaskType");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        // caseName falls back to a default; region and location fall back to empty
        // strings, which fail task initiation downstream in the same way as null
        assertThat(valueOf(results, "caseName")).isEqualTo("Financial Remedy");
        assertThat(valueOf(results, "region")).isEqualTo("");
        assertThat(valueOf(results, "location")).isEqualTo("");
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
            Map.of("name", "calculatedDates", "value", "nextHearingDate,dueDate,priorityDate", "canReconfigure", true),
            Map.of("name", "priorityDateOriginRef", "value", "nextHearingDate,dueDate",
                   "canReconfigure", true),
            Map.of("name", "nextHearingDate", "value", "", "canReconfigure", true),
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
                   "canReconfigure", true),
            Map.of("name", "roleCategory", "value", "CTSC", "canReconfigure", true),
            Map.of("name", "dueDateIntervalDays", "value", "5", "canReconfigure", true),
            Map.of("name", "workType", "value", "evidence", "canReconfigure", true),
            Map.of("name", "description", "value",
                   "[Attach scanned document]"
                       + "(/cases/case-details/${[CASE_REFERENCE]}/trigger/attachScannedDocs/attachScannedDocs1)",
                   "canReconfigure", true),
            Map.of("name", "title", "value", "Process Scanned Documents", "canReconfigure", true)
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
    void givenProcessApprovedOrderTaskTypeShouldReturnConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue(
            "caseData", Map.of(
                "caseNameHmctsInternal", "Tony Stark v Pepper Potts",
                "caseManagementLocation", Map.of("region", "2", "baseLocation", "765324")
            )
        );
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

    @Test
    void givenCheckResponseReceivedTaskTypeShouldReturnConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "caseNameHmctsInternal", "Phoenix Wright v Miles Edgeworth",
            "caseManagementLocation", Map.of("region", "2", "baseLocation", "765324")
        ));
        inputVariables.putValue("taskType", "checkResponseReceived");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(20);

        assertThat(valueOf(results, "workType")).isEqualTo("routine_work");
        assertThat(valueOf(results, "roleCategory")).isEqualTo("CTSC");
        assertThat(valueOf(results, "title")).isEqualTo("Check Response Received");
        assertThat(valueOf(results, "caseManagementCategory")).isEqualTo("FR Consented");
        assertThat(valueOf(results, "dueDateIntervalDays")).isEqualTo("5");
        assertThat(valueOf(results, "dueDateNonWorkingCalendar")).isEqualTo(
            "https://www.gov.uk/bank-holidays/england-and-wales.json");
        assertThat(valueOf(results, "caseName")).isEqualTo("Phoenix Wright v Miles Edgeworth");
        assertThat(valueOf(results, "region")).isEqualTo("2");
        assertThat(valueOf(results, "location")).isEqualTo("765324");

        String description = valueOf(results, "description").toString();
        assertThat(description).contains(
            "[Assign To Judge](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_referToJudge/FR_referToJudge1)");
        assertThat(description).contains(
            "[Create General Order](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_generalOrder/FR_generalOrder1)");
        assertThat(description).contains(
            "[Call back Rejected Order](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_callbackRejectedOrder/FR_callbackRejectedOrder1)");
        assertThat(description).contains(
            "[Awaiting Information](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_awaitingInfo/FR_awaitingInfo1)");
        assertThat(description).contains(
            "[Create General Email](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_generalEmail/FR_generalEmail1)");
        assertThat(description).contains(
            "[List for hearing](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_listForHearing/FR_listForHearing1)");
        assertThat(description).contains(
            "[Close Case](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_close/FR_close1)");
        assertThat(description).contains(
            "[Amended Consent Order](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_amendedConsentOrder/FR_amendedConsentOrder1)");
    }

    @Test
    void givenCheckHelpWithFeesTaskTypeShouldReturnConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "caseNameHmctsInternal", "Bruce Wayne v Selina Kyle",
            "caseManagementLocation", Map.of("region", "2", "baseLocation", "366796")
        ));
        inputVariables.putValue("taskType", "checkHelpWithFees");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(20);

        assertThat(valueOf(results, "workType")).isEqualTo("applications");
        assertThat(valueOf(results, "roleCategory")).isEqualTo("CTSC");
        assertThat(valueOf(results, "title")).isEqualTo("Check Help With Fees");
        assertThat(valueOf(results, "caseManagementCategory")).isEqualTo("FR Consented");
        assertThat(valueOf(results, "caseName")).isEqualTo("Bruce Wayne v Selina Kyle");
        assertThat(valueOf(results, "region")).isEqualTo("2");
        assertThat(valueOf(results, "location")).isEqualTo("366796");

        // SLA = 5 working days (BA confirmed): weekends and gov.uk bank holidays are skipped
        assertThat(valueOf(results, "dueDateIntervalDays")).isEqualTo("5");
        assertThat(valueOf(results, "dueDateSkipNonWorkingDays")).isEqualTo("true");
        assertThat(valueOf(results, "dueDateNonWorkingDaysOfWeek")).isEqualTo("SATURDAY,SUNDAY");
        assertThat(valueOf(results, "dueDateNonWorkingCalendar")).isEqualTo(
            "https://www.gov.uk/bank-holidays/england-and-wales.json");
        assertThat(valueOf(results, "dueDateTime")).isEqualTo("14:00");

        // BA confirmed all three outcome links are shown so the caseworker can choose
        String description = valueOf(results, "description").toString();
        assertThat(description).contains(
            "[HWF Application Accepted](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_HWFDecisionMade/FR_HWFDecisionMade1)");
        assertThat(description).contains(
            "[Fee Account Debited](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_paymentMadeFromHWF/FR_paymentMadeFromHWF1)");
        assertThat(description).contains(
            "[Awaiting Payment Response](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_awaitingPaymentResponseFromHWF/FR_awaitingPaymentResponseFromHWF1)");
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
                entry("dueDateNonWorkingCalendar", "https://www.gov.uk/bank-holidays/england-and-wales.json"),
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
                entry("dueDateIntervalDays", "10"),
                entry(
                    "description",
                    "[Approve Application](/cases/case-details/${[CASE_REFERENCE]}/trigger"
                        + "/FR_approveApplication/FR_approveApplication1)"
                        + "<br>[Upload Approved Order](/cases/case-details/${[CASE_REFERENCE]}/trigger"
                        + "/FR_uploadApprovedOrder/FR_uploadApprovedOrder1)"
                        + "<br>[Application Not Approved]"
                        + "(/cases/case-details/${[CASE_REFERENCE]}/trigger/FR_orderRefusal/FR_orderRefusal1)"
                ),
                entry("title", "Review Application")
            ));
    }

    @Test
    void givenReviewRefusedOrderTaskTypeShouldReturnConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "caseNameHmctsInternal", "Peter Parker v Mary Jane",
            "caseManagementLocation", Map.of("region", "2", "baseLocation", "366796")
        ));
        inputVariables.putValue("taskType", "reviewRefusedOrder");

        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        List<Map<String, Object>> results = dmnDecisionTableResult.getResultList();

        assertThat(results).hasSize(20);
        assertThat(valueOf(results, "workType")).isEqualTo("hearing_work");
        assertThat(valueOf(results, "roleCategory")).isEqualTo("CTSC");
        assertThat(valueOf(results, "title")).isEqualTo("Review Refused Order");
        assertThat(valueOf(results, "description"))
            .isEqualTo("[List For Hearing](/cases/case-details/${[CASE_REFERENCE]}"
                + "/trigger/FR_listForHearing/FR_listForHearing1)");
        assertThat(valueOf(results, "caseManagementCategory")).isEqualTo("FR Consented");
        assertThat(valueOf(results, "caseName")).isEqualTo("Peter Parker v Mary Jane");
        assertThat(valueOf(results, "region")).isEqualTo("2");
        assertThat(valueOf(results, "location")).isEqualTo("366796");

        // SLA = 5 working days: weekends and gov.uk bank holidays are skipped
        assertThat(valueOf(results, "dueDateIntervalDays")).isEqualTo("5");
        assertThat(valueOf(results, "dueDateSkipNonWorkingDays")).isEqualTo("true");
        assertThat(valueOf(results, "dueDateNonWorkingDaysOfWeek")).isEqualTo("SATURDAY,SUNDAY");
        assertThat(valueOf(results, "dueDateNonWorkingCalendar"))
            .isEqualTo("https://www.gov.uk/bank-holidays/england-and-wales.json");
        assertThat(valueOf(results, "dueDateTime")).isEqualTo("14:00");
    }

    @Test
    void givenCheckApplicationTaskType_whenEvaluated_thenReturnsCorrectConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of());
        inputVariables.putValue("taskType", "checkApplication");

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
                entry("dueDateNonWorkingCalendar","https://www.gov.uk/bank-holidays/england-and-wales.json"),
                entry("dueDateNonWorkingDaysOfWeek", "SATURDAY,SUNDAY"),
                entry("dueDateSkipNonWorkingDays", "true"),
                entry("dueDateMustBeWorkingDay", "No"),
                entry("dueDateTime", "14:00"),
                entry("majorPriority", "5000"),
                entry("minorPriority", "500"),
                entry("caseName", "Financial Remedy"),
                entry("region", ""),
                entry("location", ""),
                entry("caseManagementCategory", "FR Consented"),
                entry("roleCategory", "CTSC"),
                entry("dueDateIntervalDays", "5"),
                entry("workType", "applications"),
                entry("description",
                       "[Issue Application]"
                           + "(/cases/case-details/${[CASE_REFERENCE]}/trigger/"
                           + "FR_issueApplication/FR_issueApplication1)"),
                entry("title", "Check Application")
            ));
    }

    @Test
    void givenReviewOrderResponseTaskTypeShouldReturnConfiguration() {
        VariableMap inputVariables = new VariableMapImpl();
        inputVariables.putValue("caseData", Map.of(
            "caseNameHmctsInternal", "Applicant v Respondent",
            "caseManagementLocation", Map.of("region", "2", "baseLocation", "366796")
        ));
        inputVariables.putValue("taskType", "reviewOrderResponse");

        ZonedDateTime beforeEvaluation = ZonedDateTime.now();
        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmnTable(inputVariables);
        ZonedDateTime afterEvaluation = ZonedDateTime.now();
        List<Map<String, Object>> actualResults = dmnDecisionTableResult.getResultList();

        // dueDateOrigin is now() so its value is asserted against the evaluation time below
        List<Map<String, Object>> expectedResults = List.of(

            Map.of("name", "calculatedDates", "value", "nextHearingDate,dueDate,priorityDate", "canReconfigure", true),
            Map.of("name", "priorityDateOriginRef", "value", "nextHearingDate,dueDate",
                   "canReconfigure", true),
            Map.of("name", "nextHearingDate", "value", "", "canReconfigure", true),
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
                   "canReconfigure", true),
            Map.of("name", "roleCategory", "value", "CTSC", "canReconfigure", true),
            Map.of("name", "dueDateIntervalDays", "value", "5", "canReconfigure", true),
            Map.of("name", "title", "value", "Review Order Response", "canReconfigure", true),
            Map.of("name", "description", "value",
                   "[Assign to Judge]"
                       + "(/cases/case-details/${[CASE_REFERENCE]}/trigger/"
                       + "FR_assignToJudgeConsent/FR_assignToJudgeConsent1)",
                   "canReconfigure", true),
            Map.of("name", "workType", "value", "review_case", "canReconfigure", true)

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

    private static Object valueOf(List<Map<String, Object>> results, String name) {
        return results.stream()
            .filter(r -> name.equals(r.get("name")))
            .findFirst()
            .orElseThrow()
            .get("value");
    }
}
