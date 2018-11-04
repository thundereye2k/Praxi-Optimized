package me.joeleoli.praxi.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.cooldown.Cooldown;
import me.joeleoli.nucleus.player.PlayerInfo;
import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.events.task.EventStartTask;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PraxiPlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public abstract class Event {

	protected static final String EVENT_PREFIX = Style.GOLD + Style.BOLD + "[Event] " + Style.RESET;
	private static final HoverEvent HOVER_EVENT = new HoverEvent(
			HoverEvent.Action.SHOW_TEXT,
			new ChatComponentBuilder("").parse(Style.YELLOW + "Click to join the Sumo event.").create()
	);
	private static final ClickEvent CLICK_EVENT = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join");

	private String name;
	@Setter
	private EventState state = EventState.WAITING;
	private EventTask eventTask;
	private PlayerInfo host;
	private Map<UUID, EventPlayer> eventPlayers = new HashMap<>();
	private int maxPlayers;
	@Setter
	private Cooldown cooldown;

	public Event(String name, PlayerInfo host, int maxPlayers) {
		this.name = name;
		this.host = host;
		this.maxPlayers = maxPlayers;
	}

	public void setEventTask(EventTask task) {
		if (this.eventTask != null) {
			this.eventTask.cancel();
		}

		this.eventTask = task;

		if (this.eventTask != null) {
			this.eventTask.runTaskTimer(Praxi.getInstance(), 0L, 20L);
		}
	}

	public boolean isWaiting() {
		return this.state == EventState.WAITING;
	}

	public boolean isFighting() {
		return this.state == EventState.ROUND_FIGHTING;
	}

	public EventPlayer getEventPlayer(UUID uuid) {
		return this.eventPlayers.get(uuid);
	}

	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<>();

		for (EventPlayer eventPlayer : this.eventPlayers.values()) {
			final Player player = eventPlayer.toPlayer();

			if (player != null) {
				players.add(player);
			}
		}

		return players;
	}

	public int getRemainingPlayers() {
		int remaining = 0;

		for (EventPlayer eventPlayer : this.eventPlayers.values()) {
			if (eventPlayer.getState() == EventPlayerState.WAITING) {
				remaining++;
			}
		}

		return remaining;
	}

	public void handleStart() {
		this.setEventTask(new EventStartTask(this));
	}

	public void handleJoin(Player player) {
		this.eventPlayers.put(player.getUniqueId(), new EventPlayer(player));
		this.broadcastMessage(Style.PINK + player.getName() + Style.YELLOW + " joined the event " + Style.PINK + "(" +
				this.getRemainingPlayers() + "/" + this.getMaxPlayers() + ")");
		this.onJoin(player);

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		praxiPlayer.setEvent(this);
		praxiPlayer.setState(PlayerState.IN_EVENT);
		praxiPlayer.loadHotbar();

		player.teleport(Praxi.getInstance().getEventManager().getSumoSpectator());
	}

	public void handleDeath(Player player) {
		final EventPlayer loser = this.getEventPlayer(player.getUniqueId());

		loser.setState(EventPlayerState.ELIMINATED);

		this.onDeath(player);
	}

	public void handleLeave(Player player) {
		if (this.isFighting(player.getUniqueId())) {
			this.handleDeath(player);
		}

		this.eventPlayers.remove(player.getUniqueId());
		this.onLeave(player);

		this.getPlayers().forEach(otherPlayer -> {
			player.hidePlayer(otherPlayer);
			otherPlayer.hidePlayer(player);
		});

		if (this.state == EventState.WAITING) {
			this.broadcastMessage(Style.PINK + player.getName() + Style.YELLOW + " left the event " + Style.PINK +
					"(" + this.getRemainingPlayers() + "/" + this.getMaxPlayers() + ")");
		}

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		praxiPlayer.setState(PlayerState.IN_LOBBY);
		praxiPlayer.setEvent(null);
		praxiPlayer.loadHotbar();

		PlayerUtil.spawn(player);
	}

	public void end() {
		// Remove active event and set cooldown
		Praxi.getInstance().getEventManager().setActiveEvent(null);
		Praxi.getInstance().getEventManager().setEventCooldown(new Cooldown(60_000L * 3));

		// Cancel any active task
		this.setEventTask(null);

		final Player winner = this.getWinner();
		final List<Player> players = this.getPlayers();

		if (winner == null) {
			PlayerUtil.messageAll(EVENT_PREFIX + Style.YELLOW + "The event has been canceled.");
		} else {
			PlayerUtil.messageAll(EVENT_PREFIX + Style.PINK + winner.getName() + Style.YELLOW + " has won the event!");
		}

		for (EventPlayer eventPlayer : this.eventPlayers.values()) {
			final Player player = eventPlayer.toPlayer();

			if (player != null) {
				final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

				praxiPlayer.setState(PlayerState.IN_LOBBY);
				praxiPlayer.setEvent(null);
				praxiPlayer.loadHotbar();

				PlayerUtil.spawn(player);
			}
		}

		players.forEach(player -> players.forEach(otherPlayer -> {
			player.hidePlayer(otherPlayer);
			otherPlayer.hidePlayer(player);
		}));
	}

	public boolean canEnd() {
		int remaining = 0;

		for (EventPlayer eventPlayer : this.eventPlayers.values()) {
			if (eventPlayer.getState() == EventPlayerState.WAITING) {
				remaining++;
			}
		}

		return remaining == 1;
	}

	public Player getWinner() {
		for (EventPlayer eventPlayer : this.eventPlayers.values()) {
			if (eventPlayer.getState() != EventPlayerState.ELIMINATED) {
				return eventPlayer.toPlayer();
			}
		}

		return null;
	}

	public void announce() {
		BaseComponent[] components = new ChatComponentBuilder("")
				.parse(EVENT_PREFIX + Style.PINK + this.getHost().getName() + Style.YELLOW + " is hosting a " +
				       Style.PINK + this.getName() + " Event " + Style.GRAY + "[Click to join]")
				.attachToEachPart(HOVER_EVENT)
				.attachToEachPart(CLICK_EVENT)
				.create();

		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(components);
		}
	}

	public void broadcastMessage(String message) {
		for (Player player : this.getPlayers()) {
			player.sendMessage(EVENT_PREFIX + message);
		}
	}

	public abstract boolean isSumo();

	public abstract boolean isCorners();

	public abstract void onJoin(Player player);

	public abstract void onLeave(Player player);

	public abstract void onRound();

	public abstract void onDeath(Player player);

	public abstract String getRoundDuration();

	public abstract EventPlayer getRoundPlayerA();

	public abstract EventPlayer getRoundPlayerB();

	public abstract boolean isFighting(UUID uuid);

}
