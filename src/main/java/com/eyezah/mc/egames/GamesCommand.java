package com.eyezah.mc.egames;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.eyezah.mc.egames.eGames.getGameProfiles;

public class GamesCommand implements CommandExecutor {
	JavaPlugin plugin;
	GamesCommand(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run as a player!");
			return true;
		}
		Player player = (Player) sender;
		if (args[0].equalsIgnoreCase("play")) {
			if (args.length == 1) {
				player.sendMessage("play command");
				return true;
			}
			GameProfile realProfile = null;
			for (GameProfile gameProfile : getGameProfiles()) {
				if (gameProfile.getTriggerName().equalsIgnoreCase(args[1])) {
					realProfile = gameProfile;
					break;
				}
			}
			if (realProfile == null) {
				player.sendMessage("no game");
				return true;
			}
			player.sendMessage("§aLoading " + realProfile.getNiceName() + "!");
			int i = 2;
			String[] newArgs = new String[args.length - 2];
			while (i < args.length) {
				newArgs[i - 2] = args[i];
				i++;
			}
			Class game = realProfile.getGame();
			try {
				Class[] cArg = new Class[4];
				cArg[0] = JavaPlugin.class;
				cArg[1] = World.class;
				cArg[2] = String[].class;
				cArg[3] = Player.class;
				Constructor ct = game.getDeclaredConstructor(cArg);
				ct.newInstance(plugin, player.getWorld(), newArgs, player);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				e.printStackTrace();
				player.sendMessage("§cSomething went wrong when loading " + realProfile.getNiceName() + "!");
			}
			return true;
		} else if (args[0].equalsIgnoreCase("list")) {
			String out = "";
			for (Game game : Game.getAllGames()) {
				out += "\n§f" + game.world.getName() + " - " + game.gameName;
			}
			if (out.equals("")) {
				player.sendMessage("§cThere are no active games right now!");
			} else {
				out = "§aThese games are active:" + out;
				player.sendMessage(out);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("stop")) {
			Game game = Game.get(player.getWorld());
			if (game == null) {
				player.sendMessage("§cThere is no active game in your world right now!");
				return true;
			}
			game.forceStop();
			return true;
		}
		return false;
	}
}
