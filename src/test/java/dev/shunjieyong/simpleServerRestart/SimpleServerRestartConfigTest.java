package dev.shunjieyong.simpleServerRestart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleServerRestartConfigTest {

    @Test
    void validatePostLoad_migratesLegacyRestartTime() throws Exception {
        SimpleServerRestartConfig config = new SimpleServerRestartConfig();
        config.restartTime = "14:00";
        config.restartTimes = new String[]{"12:00"};

        config.validatePostLoad();

        assertArrayEquals(new String[]{"14:00"}, config.restartTimes);
        assertEquals("", config.restartTime, "Legacy restartTime should be cleared after migration");
    }

    @Test
    void validatePostLoad_doesNotOverwrite_whenLegacyRestartTimeIsBlank() throws Exception {
        SimpleServerRestartConfig config = new SimpleServerRestartConfig();
        config.restartTime = "";
        config.restartTimes = new String[]{"08:00", "20:00"};

        config.validatePostLoad();

        assertArrayEquals(new String[]{"08:00", "20:00"}, config.restartTimes);
    }

    @Test
    void validatePostLoad_filtersInvalidWarnIntervals() throws Exception {
        SimpleServerRestartConfig config = new SimpleServerRestartConfig();
        config.restartTime = "";
        config.warnPlayers.warnPlayerIntervals = new String[]{"5m", "invalid", "10s", "3h", "abc"};

        config.validatePostLoad();

        // Valid intervals end with m, s, or h; invalid ones become null
        String[] expected = new String[]{"5m", null, "10s", "3h", null};
        assertArrayEquals(expected, config.warnPlayers.warnPlayerIntervals);
    }

    @Test
    void validatePostLoad_allValidIntervals_preserved() throws Exception {
        SimpleServerRestartConfig config = new SimpleServerRestartConfig();
        config.restartTime = "";
        config.warnPlayers.warnPlayerIntervals = new String[]{"1h", "30m", "10m", "5m", "1m", "30s"};

        config.validatePostLoad();

        assertArrayEquals(
            new String[]{"1h", "30m", "10m", "5m", "1m", "30s"},
            config.warnPlayers.warnPlayerIntervals
        );
    }

    @Test
    void defaultConfig_hasExpectedValues() {
        SimpleServerRestartConfig config = new SimpleServerRestartConfig();

        assertEquals(86400, config.secondsTillNextRestart);
        assertArrayEquals(new String[]{"12:00"}, config.restartTimes);
        assertTrue(config.warnPlayers.warnPlayers);
        assertTrue(config.runRestartScript);
        assertFalse(config.rescheduleOnReload);
        assertEquals("The server is restarting...", config.restartKickMessage);
        assertEquals("run.sh", config.restartScriptPath);
        assertTrue(config.stopServer);
    }
}
