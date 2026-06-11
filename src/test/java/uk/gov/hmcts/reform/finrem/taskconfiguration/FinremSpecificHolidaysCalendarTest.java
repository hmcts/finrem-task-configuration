package uk.gov.hmcts.reform.finrem.taskconfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * The finrem-specific holidays calendar is fetched by URL at runtime by
 * wa-task-management-api (see dueDateNonWorkingCalendar in the task configuration DMN),
 * so a malformed entry would only surface as a production date-calculation failure.
 * This test validates the file shape in CI instead.
 */
class FinremSpecificHolidaysCalendarTest {

    private static final String CALENDAR_FILE = "finrem-specific-holidays.json";

    @Test
    void calendarShouldBeValidJsonWithParseableEventDates() throws Exception {
        InputStream calendarStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(CALENDAR_FILE);
        assertThat(CALENDAR_FILE + " must exist on the classpath", calendarStream, is(notNullValue()));

        JsonNode calendar = new ObjectMapper().readTree(calendarStream);

        JsonNode events = calendar.get("events");
        assertThat("calendar must have an 'events' array", events != null && events.isArray(), is(true));

        for (JsonNode event : events) {
            JsonNode date = event.get("date");
            assertThat("every event must have a 'date': " + event, date != null && date.isTextual(), is(true));
            // throws DateTimeParseException (failing the test) if the date is not valid ISO-8601
            LocalDate.parse(date.asText());
        }
    }
}
