package GitHub.GiGodN.commandAliasCreator;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public interface CommandManagerExt {

    void setDispatcher(CommandDispatcher<ServerCommandSource> dispatcher);

}
