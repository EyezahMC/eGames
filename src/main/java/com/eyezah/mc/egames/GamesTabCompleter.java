package com.eyezah.mc.egames;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.eyezah.mc.egames.eGames.getGameProfiles;

public class GamesTabCompleter implements TabCompleter {
	private static final String[] COMMANDS = {"play", "list", "stop"};

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		final List<String> completions = new ArrayList<>();
		if (args.length < 2) {
			StringUtil.copyPartialMatches(args[0], List.of(COMMANDS), completions);
		} else if (args.length == 2 && args[0].equalsIgnoreCase("play")) {
			for (GameProfile gameProfile : getGameProfiles()) {
				completions.add(gameProfile.getTriggerName());
			}
		}
		Collections.sort(completions);
		return completions;
	}
}
