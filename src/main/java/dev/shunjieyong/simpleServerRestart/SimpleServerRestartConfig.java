package dev.shunjieyong.simpleServerRestart;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = SimpleServerRestart.MOD_ID)
public class SimpleServerRestartConfig implements ConfigData {
    @Comment("Set to > 0  to enable restart scheduling")
    int secondsTillNextRestart = 86400;

    @Comment("Set to true to run a custom restart script, else server restart defaults to just doing /stop.")
    boolean runRestartScript = false;

    @Comment("Set to true to reset the scheduled restart on server reload.")
    boolean rescheduleOnReload = false;

    @Comment("Message that will be displayed on the player's screen when getting kicked.")
    String restartKickMessage = "The server is restarting...";

    @Comment("The restart executable script path. Path defaults to the server root directory.")
    String restartScriptPath = "start.bat";
}
