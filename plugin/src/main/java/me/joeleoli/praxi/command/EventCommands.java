package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.cooldown.Cooldown;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.events.Event;
import me.joeleoli.praxi.events.EventState;
import me.joeleoli.praxi.events.impl.SumoEvent;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventCommands {

	@Command(names = "eventmanager cancel", permissionNode = "praxi.event.admin")
	public static void cancel(CommandSender sender) {
		if (Praxi.getInstance().getEventManager().getActiveEvent() == null) {
			sender.sendMessage(Style.RED + "There is no active event.");
			return;
		}

		Praxi.getInstance().getEventManager().getActiveEvent().end();
	}

	@Command(names = "eventmanager cooldown", permissionNode = "praxi.event.admin")
	public static void cooldown(CommandSender sender) {
		if (Praxi.getInstance().getEventManager().getEventCooldown().hasExpired()) {
			sender.sendMessage(Style.RED + "There is no event cooldown active.");
			return;
		}

		sender.sendMessage(Style.GREEN + "You reset the event cooldown.");
		Praxi.getInstance().getEventManager().setEventCooldown(new Cooldown(0));
	}

	@Command(names = "eventmanager setspawn pos", permissionNode = "praxi.event.admin")
	public static void setSpawnPosition(Player player, @Parameter(name = "pos") int position) {
		if (!(position == 1 || position == 2)) {
			player.sendMessage(Style.RED + "The position must be 1 or 2.");
		} else {
			if (position == 1) {
				Praxi.getInstance().getEventManager().setSumoSpawn1(player.getLocation());
			} else {
				Praxi.getInstance().getEventManager().setSumoSpawn2(player.getLocation());
			}

			Praxi.getInstance().getEventManager().save();
			player.sendMessage(Style.GREEN + "Updated event's spawn location " + position + ".");
		}
	}

	@Command(names = "eventmanager setspawn spec", permissionNode = "praxi.event.admin")
	public static void setSpawnSpectator(Player player) {
		Praxi.getInstance().getEventManager().setSumoSpectator(player.getLocation());
		Praxi.getInstance().getEventManager().save();
		player.sendMessage(Style.GREEN + "Updated event's spawn spectator location.");
	}

	@Command(names = { "event host", "host" }, permissionNode = "praxi.event.host")
	public static void hostEvent(Player player) {
		if (Praxi.getInstance().getEventManager().getActiveEvent() != null) {
			player.sendMessage(Style.RED + "There is already an active event.");
			return;
		}

		if (!Praxi.getInstance().getEventManager().getEventCooldown().hasExpired()) {
			player.sendMessage(Style.RED + "There is an event cooldown active.");
			return;
		}

		Praxi.getInstance().getEventManager().setActiveEvent(new SumoEvent(player));

		for (Player other : Praxi.getInstance().getServer().getOnlinePlayers()) {
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(other.getUniqueId());

			if (praxiPlayer.isInLobby()) {
				if (!praxiPlayer.getKitEditor().isActive()) {
					praxiPlayer.loadHotbar();
				}
			}
		}
	}

	@Command(names = { "event join" })
	public static void eventJoin(Player player) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
		final Event activeEvent = Praxi.getInstance().getEventManager().getActiveEvent();

		if (praxiPlayer.isBusy()) {
			player.sendMessage(Style.RED + "You cannot join the event right now.");
			return;
		}

		if (activeEvent == null) {
			player.sendMessage(Style.RED + "There is no active event.");
			return;
		}

		if (activeEvent.getState() != EventState.WAITING) {
			player.sendMessage(Style.RED + "That event is currently on-going and cannot be joined.");
			return;
		}

		Praxi.getInstance().getEventManager().getActiveEvent().handleJoin(player);
	}

	@Command(names = { "event leave" })
	public static void eventLeave(Player player) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
		final Event activeEvent = Praxi.getInstance().getEventManager().getActiveEvent();

		if (activeEvent == null) {
			player.sendMessage(Style.RED + "There is no active event.");
			return;
		}

		if (!praxiPlayer.isInEvent() || !activeEvent.getEventPlayers().containsKey(player.getUniqueId())) {
			player.sendMessage(Style.RED + "You are not apart of the active event.");
			return;
		}

		Praxi.getInstance().getEventManager().getActiveEvent().handleLeave(player);
	}

}
