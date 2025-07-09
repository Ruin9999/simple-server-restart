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


        // I'll fix this up later to correctly validate the warn intervals.
        warnPlayers.warnPlayerIntervals = Stream.of(warnPlayers.warnPlayerIntervals)
            .map(interval -> {
                char lastChar = interval.charAt(interval.length() - 1);
                if (!(lastChar == 'm' || lastChar == 's' || lastChar == 'h')) return null;
                else return interval;
            })
            .toArray(String[]::new);
    }

}

