package com.eyezah.mc.egames;

import com.destroystokyo.paper.Title;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import static com.eyezah.mc.egames.eGames.doVerbose;
import static com.eyezah.mc.egames.eGames.getLobbyTime;
import static org.bukkit.Bukkit.getServer;

public abstract class Game {
	protected static Logger logger = getServer().getPluginManager().getPlugin("eGames").getLogger();
	private static List<Game> games = new ArrayList<>();

	public static List<Game> getAllGames() {
		return games;
	}

	public Map<Player, Object> players = new HashMap<>();
	public List<Player> spectators = new ArrayList<>();
	public World world;
	public String gameName;
	public JavaPlugin plugin;
	public final int minPlayers;
	public boolean running = false;
	public boolean cancelled = false;
	public Player sender;

	public enum MessageType {
		CHAT,
		TITLE,
		ACTIONBAR
	}



	public static void vLog(String message) {
		if (doVerbose()) logger.info(message);
	}

	public static Game get(World world) {
		for (Game game : games) {
			if (game.world == world) return game;
		}
		return null;
	}

	public boolean register(Game game) {
		if (get(game.world) != null) return false;
		vLog("Registering new game!");
		games.add(game);
		return true;
	}



	public void unregister() {
		cancelled = true;
		games.remove(this);
		running = false;
		vLog("Game was unregistered!");
		gameEnded();
	}

	public Game(World world, JavaPlugin plugin, String gameName, int minPlayers, Player sender) {
		this.minPlayers = minPlayers;
		this.sender = sender;
		this.world = world;
		if (!register(this)) {
			sender.sendMessage("§cThis world is already being used for a game of " + get(world).gameName + "!");
			vLog("Couldn't register game in " + world.getName() + " because there's already a game in that world!");
			return;
		}
		this.plugin = plugin;
		this.gameName = gameName;
		for (Player player : world.getPlayers()) {
			players.put(player, null);
		}
		messagePlayers("This world is being used for a game of §a" + gameName + "§f!\n§cIf you don't want to participate, join a different world now.", true);

		class PlayerJoinListener implements Listener {
			final World world;
			PlayerJoinListener(World world) {
				this.world = world;
			}

			@EventHandler
			public void onPlayerTeleport(PlayerTeleportEvent playerTeleportEvent) {
				if (cancelled) {
					playerTeleportEvent.getHandlers().unregister(this);
					return;
				}
				Player player = playerTeleportEvent.getPlayer();
				World world = playerTeleportEvent.getTo().getWorld();
				World from = playerTeleportEvent.getFrom().getWorld();
				if (from == world) return;
				if (running) {
					if (from == this.world) {
						if (players.containsKey(player)) {
							removePlayer(player);
						}
					} else if (world == this.world) {
						makeSpectator(player);
					}
				} else {
					if (from == this.world) {
						if (players.containsKey(player)) {
							players.remove(player);
							player.sendMessage("§cYou left " + gameName + "!");
						}
					} else if (world == this.world) {
						if (!players.containsKey(player)) {
							players.put(player, null);
							player.sendMessage("§aYou joined " + gameName + "!");
						}
					}
				}
			}

			@EventHandler
			public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
				if (cancelled) {
					playerQuitEvent.getHandlers().unregister(this);
					return;
				}
				Player player = playerQuitEvent.getPlayer();
				if (!players.containsKey(player)) return;
				if (running) {
					players.remove(player);
				} else {
					removePlayer(player);
				}
			}

			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
				if (cancelled) {
					playerJoinEvent.getHandlers().unregister(this);
					return;
				}
				Player player = playerJoinEvent.getPlayer();
				World world = player.getWorld();
				if (players.containsKey(player) || world != this.world) return;
				if (running) {
					makeSpectator(player);
				} else {
					players.put(player, null);
				}
			}
		}
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(world), plugin);

		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(getLobbyTime() * 1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (cancelled) return;
			if (players.size() < minPlayers) {
				messagePlayers("§cThe game was cancelled because there weren't enough players!", true);
				unregister();
				return;
			}
			start();
		});
		thread.start();
	}

	public void start() {
		messagePlayers("§a" + gameName + " has started!", true);
		running = true;
		tick(1000);
	}

	public void forceStop() {
		vLog("Force stopping game");
		if (!games.contains(this)) return;
		messagePlayers("§c" + gameName + " has been ended!", true);
		unregister();
	}

	public void makeSpectator(Player player) {
		players.remove(player);
		if (!spectators.contains(player)) spectators.add(player);
		Bukkit.getScheduler().runTask(plugin, () -> player.setGameMode(GameMode.SPECTATOR));
	}

	public void messagePlayers(String message, boolean includeSpectators) {
		messagePlayers(Component.text(message), includeSpectators);
	}

	public void messagePlayers(Component message, boolean includeSpectators) {
		for (Player player : players.keySet()) {
			messagePlayer(player, message);
		}
		if (includeSpectators) {
			for (Player player : spectators) {
				messagePlayer(player, message);
			}
		}
	}

	public void messagePlayers(String message, boolean includeSpectators, MessageType messageType) {
		for (Player player : players.keySet()) {
			messagePlayer(player, message, messageType);
		}
		if (includeSpectators) {
			for (Player player : spectators) {
				messagePlayer(player, message, messageType);
			}
		}
	}

	public Player randomPlayer() {
		if (players.size() == 0) return null;
		List<Player> keysAsArray = new ArrayList<Player>(players.keySet());
		return keysAsArray.get(ThreadLocalRandom.current().nextInt(0, keysAsArray.size()));
	}

	public void messagePlayer(Player player, Component message) {
		player.sendMessage(player, message);
	}

	public void messagePlayer(Player player, String message, MessageType messageType) {
		if (messageType == MessageType.CHAT) {
			player.sendMessage(message);
			return;
		}
		if (messageType == MessageType.TITLE) {
			player.sendTitle(new Title(message));
			return;
		}
		if (messageType == MessageType.ACTIONBAR) {
			player.sendActionBar(message);
			return;
		}
	}

	public void tick(int delay) {
		Thread thread = new Thread(() -> {
			boolean runningSuccessful = true;
			while (running && runningSuccessful) {
				runningSuccessful = tickFunction();
				if (runningSuccessful && running) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			vLog("Tick thread was stopped by " + (!runningSuccessful ? "tickFunction()" : "game") + " for world " + world.getName());
			unregister();
		});
		thread.start();
	}

	public boolean tickFunction() {
		vLog("Game didn't override tick method!");
		return false;
	}

	public void removePlayer(Player player) {
		players.remove(player);
		playerRemoved(player);
	}

	public void playerRemoved(Player player) {

	}

	public static Integer parseIntOrNull(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public void gameEnded() {
		vLog("Game ended!");
	}
}
