package dev.shunjieyong.simpleServerRestart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RestartServiceTest {

    @ParameterizedTest
    @CsvSource({
        "5s, 5",
        "10s, 10",
        "30s, 30",
        "1s, 1",
        "0s, 0"
    })
    void parseTimeInterval_seconds(String input, long expectedSeconds) {
        Duration result = RestartService.parseTimeInterval(input);
        assertEquals(expectedSeconds, result.getSeconds());
    }

    @ParameterizedTest
    @CsvSource({
        "1m, 60",
        "5m, 300",
        "10m, 600",
        "30m, 1800"
    })
    void parseTimeInterval_minutes(String input, long expectedSeconds) {
        Duration result = RestartService.parseTimeInterval(input);
        assertEquals(expectedSeconds, result.getSeconds());
    }

    @ParameterizedTest
    @CsvSource({
        "1h, 3600",
        "2h, 7200",
        "12h, 43200",
        "24h, 86400"
    })
    void parseTimeInterval_hours(String input, long expectedSeconds) {
        Duration result = RestartService.parseTimeInterval(input);
        assertEquals(expectedSeconds, result.getSeconds());
    }

    @Test
    void parseTimeInterval_plainNumber_treatedAsSeconds() {
        Duration result = RestartService.parseTimeInterval("45");
        assertEquals(45, result.getSeconds());
    }

    @Test
    void parseTimeInterval_trims_whitespace() {
        Duration result = RestartService.parseTimeInterval("  5m  ");
        assertEquals(300, result.getSeconds());
    }

    @Test
    void parseTimeInterval_uppercase_treated_as_lowercase() {
        Duration resultM = RestartService.parseTimeInterval("5M");
        assertEquals(300, resultM.getSeconds());

        Duration resultH = RestartService.parseTimeInterval("2H");
        assertEquals(7200, resultH.getSeconds());

        Duration resultS = RestartService.parseTimeInterval("30S");
        assertEquals(30, resultS.getSeconds());
    }

    @Test
    void parseTimeInterval_invalidNumber_throwsException() {
        assertThrows(NumberFormatException.class, () -> RestartService.parseTimeInterval("abcm"));
    }

    @Test
    void parseTimeInterval_emptyNumericPart_throwsException() {
        assertThrows(NumberFormatException.class, () -> RestartService.parseTimeInterval("m"));
    }
}
