package dev.shunjieyong.simpleServerRestart;

import java.util.stream.Stream;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = SimpleServerRestart.MOD_ID)
public class SimpleServerRestartConfig implements ConfigData {

    @Comment("Set to > 0  to enable restart scheduling")
    int secondsTillNextRestart = 86400;

    @Comment("The restart times in 24 hour time. Does not work if secondsTilNextRestart > 0. Leave empty to disable.")
    String[] restartTimes = {"12:00"};
    
    @ConfigEntry.Gui.CollapsibleObject
    configWarnPlayers warnPlayers = new configWarnPlayers();
    

    @Comment("Set to true to run a custom restart script, else server restart defaults to just doing /stop.")
    boolean runRestartScript = true;

    @Comment("Set to true to reset the scheduled restart on server reload.")
    boolean rescheduleOnReload = false;

    @Comment("Message that will be displayed on the player's screen when getting kicked.")
    String restartKickMessage = "The server is restarting...";

    @Comment("The restart executable script path. Path defaults to the server root directory.")
    String restartScriptPath = "run.sh";

    @Comment("Whether to stop the server, or let your restart script do it for you. The server will always be stopped if \"runRestartScript\" is false. (Recommended to leave this be unless you know what you're doing.)")
    boolean stopServer = true;

    @ConfigEntry.Gui.Excluded
    String restartTime = "";


    // Collapsible Classes
    public static class configWarnPlayers implements ConfigData {
        @Comment("Whether to warn players of upcoming restarts.")
        boolean warnPlayers = true;
    
        @Comment("The times before a restart at which players should be warned. e.g. 5m, 10s, 5s")
        String[] warnPlayerIntervals = {"5m", "3m", "1m", "30s", "10s", "5s", "4s", "3s", "2s", "1s"};

        @Comment("The message to display when warning players.")
        String warnPlayerMessage = "The server is restarting in %s";
    }


    // Validation
    @Override
    public void validatePostLoad() throws ValidationException {
        if (!restartTime.isBlank()) {
            restartTimes = new String[]{restartTime};
            restartTime = "";
        }


        // FIX: Improved validation with proper null handling and error reporting
        try {
            warnPlayers.warnPlayerIntervals = Stream.of(warnPlayers.warnPlayerIntervals)
                .filter(interval -> interval != null && !interval.trim().isEmpty()) // Filter out null/empty
                .map(interval -> {
                    interval = interval.trim().toLowerCase();
                    
                    // Validate format
                    if (interval.length() == 0) return null;
                    
                    char lastChar = interval.charAt(interval.length() - 1);
                    if (!(lastChar == 'm' || lastChar == 's' || lastChar == 'h')) {
                        // Log invalid interval for debugging
                        SimpleServerRestart.LOGGER.warn("Invalid warning interval format (missing unit): '{}'", interval);
                        return null;
                    }
                    
                    // Validate the numeric part
                    try {
                        String numberPart = interval.substring(0, interval.length() - 1);
                        int value = Integer.parseInt(numberPart);
                        
                        if (value <= 0) {
                            SimpleServerRestart.LOGGER.warn("Invalid warning interval (non-positive value): '{}'", interval);
                            return null;
                        }
                        
                        return interval;
                        
                    } catch (NumberFormatException e) {
                        SimpleServerRestart.LOGGER.warn("Invalid warning interval (invalid number): '{}'", interval);
                        return null;
                    }
                })
                .filter(interval -> interval != null) // Remove null values after validation
                .toArray(String[]::new);
                
        } catch (Exception e) {
            SimpleServerRestart.LOGGER.error("Error validating warning intervals: {}", e.getMessage(), e);
            // Fall back to default intervals
            warnPlayers.warnPlayerIntervals = new String[]{"5m", "1m", "30s", "10s", "5s"};
        }
    }

}

