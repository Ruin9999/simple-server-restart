package dev.shunjieyong.simpleServerRestart;

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
    
    @ConfigEntry.Gui.Excluded
    String restartTime = "";

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


    // Update outdated fields
    @Override
    public void validatePostLoad() throws ValidationException {
        if (!restartTime.isBlank()) {
            restartTimes = new String[]{restartTime};
            restartTime = "";
        }
    }

}

