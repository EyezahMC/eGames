package com.eyezah.mc.egames;

public class GameProfile {
	private String niceName;
	private String triggerName;
	private String commandUsage;
	private Class game;
	public GameProfile(String niceName, String triggerName, String commandUsage, Class game) {
		this.niceName = niceName;
		this.triggerName = triggerName;
		this.commandUsage = commandUsage;
		this.game = game;
	}

	public String getNiceName() {
		return niceName;
	}

	public String getTriggerName() {
		return triggerName;
	}

	public String getCommandUsage() {
		return commandUsage;
	}

	public Class getGame() {
		return game;
	}
}
