package GitHub.GiGodN.commandAliasCreator.modMixin;

import GitHub.GiGodN.commandAliasCreator.CommandManagerExt;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin implements CommandManagerExt {

    @Shadow private CommandDispatcher<ServerCommandSource> dispatcher;

    @Accessor("dispatcher")
    public abstract void setDispatcher(CommandDispatcher<ServerCommandSource> dispatcher);

}
