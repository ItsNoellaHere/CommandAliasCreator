package GitHub.GiGodN.commandAliasCreator.registry;

import GitHub.GiGodN.commandAliasCreator.CommandManagerExt;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {

    private static HashMap<String, String> commandMap;
    private static MinecraftServer server;

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(literal("alias")
            .executes(context -> (help(context)))
            .then(argument("commandName", word())
                .then(argument("command", greedyString())
                    .executes(context -> (updateCommands(context, getString(context, "commandName"), getString(context, "command"))))))
            .then(literal("remove")
                .then(argument("commandName", word())
                    .executes(context -> (removeCommand(getString(context, "commandName"), context))))))
        );
    }

    public static int execute(CommandContext<ServerCommandSource> ctx, String command) {
        ctx.getSource().getMinecraftServer().getCommandManager().execute(ctx.getSource(), command);
        return 1;
    }

    public static void buildServer() {
        ServerLifecycleEvents.SERVER_STARTED.register((serverLocal) -> {
            server = serverLocal;
            try{
                readMaps(getMapPath());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            updateCommands(null, "", "");
            return;
        });
        registerCommands();
    }

    private static int help(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(new LiteralText("Deffault Help Message"), false);
        return 1;
    }

    private static void readMaps(String mapPath) throws Exception {
        File f = new File(mapPath);
        if (!f.exists()) f.createNewFile();
        Scanner s = new Scanner(f);
        if (commandMap == null) commandMap = new HashMap<String, String>();
        while (s.hasNext()) {
            Scanner s1 = new Scanner(s.nextLine());
            commandMap.put(s1.next(), s1.nextLine().trim());
            s1.close();
        }
        s.close();
    }

    private static void writeMaps(String commandName, String command) {
        String mapsPath = getMapPath();
        try{
            readMaps(mapsPath);
            commandMap.put(commandName, command);
            FileWriter fw = new FileWriter(new File(getMapPath()));
            StringBuilder sb = new StringBuilder();
            commandMap.forEach((key, value) -> {sb.append(key + " " + value + "\n");});
            fw.write(sb.toString());
            fw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static String getMapPath() {
        String mapsPath;
        String[] temp;
        if (server.isDedicated()) {
            mapsPath = "world/data/";
        }
        else {
            if (System.getProperty("os.name").contains("Windows")){
                temp = server.getSavePath(WorldSavePath.ROOT).toString().split("\\.\\\\");
            }
            else {
                temp = server.getSavePath(WorldSavePath.ROOT).toString().split("\\./");
            }
            mapsPath = temp[1].replaceAll("\\\\", "/") + "/data/";
        }
        mapsPath += "commandMaps.txt";
        return mapsPath;
    }

    private static int removeCommand(String commandName, CommandContext<ServerCommandSource> ctx) {
        String mapPath = getMapPath();
        String oldCommand = commandMap.get(commandName);
        try{
            commandMap.remove(commandName);
        }
        catch(NullPointerException e) {
            ctx.getSource().sendError(new LiteralText("Command does not exist"));
            return 0;
        }
        String fileContents = "";
        try{
            File f = new File(mapPath);
            Scanner s = new Scanner(f);
            while (s.hasNext()) {
                String temp = s.nextLine();
                if(temp.split(" ")[0].equals(commandName)) continue;
                fileContents += temp;
            }
            s.close();
            FileWriter fw = new FileWriter(f);
            fw.write(fileContents);
            fw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        RootCommandNode oldRoot = server.getCommandManager().getDispatcher().getRoot();
        RootCommandNode newRoot = new RootCommandNode();
        ArrayList<String> commandPath = new ArrayList<String>();
        commandPath.add(commandName);
        for(Object cn : oldRoot.getChildren()){
            if(cn instanceof CommandNode && !(((CommandNode<?>)cn).equals(server.getCommandManager().getDispatcher().findNode(commandPath)))) {
                newRoot.addChild((CommandNode<?>)cn);
            }
        }
        ((CommandManagerExt) server.getCommandManager()).setDispatcher(new CommandDispatcher<ServerCommandSource>(newRoot));
        sendTree();
        ctx.getSource().sendFeedback(new LiteralText("Removed Command: " + commandName), true);
        return 1;
    }

    private static void sendTree() {
        CommandManager cm = server.getCommandManager();
        for(ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            cm.sendCommandTree(p);
        }
    }

    private static int updateCommands(CommandContext<ServerCommandSource> ctx, String commandName, String command) {
        if(ctx != null) writeMaps(commandName, command);
        CommandManager cm = server.getCommandManager();
        commandMap.forEach((commandKey, commandExec) -> {
            cm.getDispatcher().register(literal(commandKey)
                .executes(context -> {return execute(context, commandExec); }));
        });
        sendTree();
        if(ctx != null) ctx.getSource().sendFeedback(new LiteralText("Added Command: " + commandName),true);
        return 1;
    }

}
