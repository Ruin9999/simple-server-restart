package dev.shunjieyong.simpleServerRestart;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = SimpleServerRestart.MOD_ID)
public class SimpleServerRestartConfig implements ConfigData {
    @Comment("Message that will be displayed on the player's screen when getting kicked.")
    String restartKickMessage = "The server is restarting...";

    @Comment("Set to > 0  to enable restart scheduling")
    int secondsTillNextRestart = 86400;
}
