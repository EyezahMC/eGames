package com.eyezah.mc.egames;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class eGames extends JavaPlugin {
	private static List<GameProfile> gameList = new ArrayList<>();
	private static eGames instance;
	private static FileConfiguration config;

	protected static List<GameProfile> getGameProfiles() {
		return gameList;
	}

	public static eGames getInstance() {
		return instance;
	}

	public boolean registerGameProfile(GameProfile gameProfile) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		String triggerName = gameProfile.getTriggerName();
		for (GameProfile compare : gameList) {
			if (compare.getTriggerName().equalsIgnoreCase(triggerName)) return false;
		}
		gameList.add(gameProfile);
		return true;
	}

	public void unregisterGameProfile(GameProfile gameProfile) {
		gameList.remove(gameProfile);
	}

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		config = getConfig();
		this.getCommand("games").setExecutor(new GamesCommand(this));
		this.getCommand("games").setTabCompleter(new GamesTabCompleter());
	}

	public static int getLobbyTime() {
		return config.getInt("lobby-delay");
	}

	public static boolean doVerbose() {
		return config.getBoolean("verbose");
	}

	@Override
	public void onDisable() {
		for (Game game : Game.getAllGames()) {
			game.forceStop();
		}
	}
}
