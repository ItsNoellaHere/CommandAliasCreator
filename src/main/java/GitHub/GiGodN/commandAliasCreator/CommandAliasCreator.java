package GitHub.GiGodN.commandAliasCreator;

import GitHub.GiGodN.commandAliasCreator.registry.ModCommands;
import net.fabricmc.api.ModInitializer;

public class CommandAliasCreator implements ModInitializer {

    public static final String MOD_ID = "commandaliascreator";

    @Override
    public void onInitialize() {
        ModCommands.buildServer();
    }
}
