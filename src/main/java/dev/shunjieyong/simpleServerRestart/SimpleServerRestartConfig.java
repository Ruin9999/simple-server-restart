package dev.shunjieyong.simpleServerRestart;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = SimpleServerRestart.MOD_ID)
public class SimpleServerRestartConfig implements ConfigData {
    String restartKickMessage = "The server is restarting...";
    int secondsTillNextRestart = 60;
}
