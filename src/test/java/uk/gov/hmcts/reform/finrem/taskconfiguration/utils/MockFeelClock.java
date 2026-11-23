package uk.gov.hmcts.reform.finrem.taskconfiguration.utils;

import org.camunda.feel.FeelEngineClock;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;

public class MockFeelClock implements FeelEngineClock {

    private static ZonedDateTime mockedTime = null;

    public static void reset() {
        mockedTime = null;
    }

    public static void setDate(LocalDate currentDate) {
        mockedTime = currentDate.atStartOfDay(ZoneId.of("UTC"));
    }

    @Override
    public ZonedDateTime getCurrentTime() {
        if (mockedTime != null) {
            return mockedTime;
        }

        return ZonedDateTime.now();
    }
}
