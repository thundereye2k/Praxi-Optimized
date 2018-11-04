package me.joeleoli.praxi.command;

import java.util.UUID;
import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.CommandHelp;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.party.Party;
import me.joeleoli.praxi.party.PartyState;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PartyCommands {

	private static final CommandHelp[] HELP = new CommandHelp[]{
			new CommandHelp("/party create", "Create a party"),
			new CommandHelp("/party disband", "Disband your party"),
			new CommandHelp("/party leave", "Leave your party"),
			new CommandHelp("/party join <name>", "Join a party"),
			new CommandHelp("/party kick <player>", "Kick a player from your party"),
			new CommandHelp("/party open", "Make your party open"),
			new CommandHelp("/party close", "Make your party closed"),
	};

	@Command(names = { "party", "party help" })
	public static void help(Player player) {
		for (CommandHelp help : HELP) {
			player.sendMessage(
					Style.YELLOW + help.getSyntax() + Style.GRAY + " - " + Style.PINK + help.getDescription());
		}
	}

	@Command(names = { "p create", "party create" })
	public static void create(Player player) {
		if (NucleusAPI.isFrozen(player)) {
			player.sendMessage(Style.RED + "You cannot create a party while frozen.");
			return;
		}

		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.getParty() != null) {
			player.sendMessage(Style.RED + "You already have a party.");
			return;
		}

		if (!praxiPlayer.isInLobby()) {
			player.sendMessage(Style.RED + "You must be in the lobby to create a party.");
			return;
		}

		praxiPlayer.setParty(new Party(player));
		praxiPlayer.loadHotbar();

		player.sendMessage(Style.YELLOW + "You created a new party.");
	}

	@Command(names = { "p disband", "party disband" })
	public static void disband(Player player) {
		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.getParty() == null) {
			player.sendMessage(Style.RED + "You do not have a party.");
			return;
		}

		if (!praxiPlayer.getParty().isLeader(player.getUniqueId())) {
			player.sendMessage(Style.RED + "You are not the leader of your party.");
			return;
		}

		praxiPlayer.getParty().disband();
	}

	@Command(names = { "p invite", "party invite" })
	public static void invite(Player player, @Parameter(name = "target") Player target) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.getParty() == null) {
			player.sendMessage(Style.RED + "You do not have a party.");
			return;
		}

		if (!praxiPlayer.getParty().canInvite(target)) {
			player.sendMessage(Style.RED + "That player has already been invited to your party.");
			return;
		}

		if (praxiPlayer.getParty().containsPlayer(target)) {
			player.sendMessage(Style.RED + "That player is already in your party.");
			return;
		}

		if (praxiPlayer.getParty().getState() == PartyState.OPEN) {
			player.sendMessage(Style.RED + "The party state is Open. You do not need to invite players.");
			return;
		}

		final PraxiPlayer targetData = PraxiPlayer.getByUuid(target.getUniqueId());

		if (targetData.isBusy()) {
			player.sendMessage(NucleusAPI.getColoredName(target) + Style.RED + " is currently busy.");
			return;
		}

		praxiPlayer.getParty().invite(target);
	}

	@Command(names = { "p join", "party join" })
	public static void join(Player player, @Parameter(name = "target") String targetId) {
		if (NucleusAPI.isFrozen(player)) {
			player.sendMessage(Style.RED + "You cannot join a party while frozen.");
			return;
		}

		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.getParty() != null) {
			player.sendMessage(Style.RED + "You already have a party.");
			return;
		}

		Player target;

		try {
			target = Bukkit.getPlayer(UUID.fromString(targetId));
		} catch (Exception e) {
			target = Bukkit.getPlayer(targetId);
		}

		if (target == null) {
			player.sendMessage(Style.RED + "A player with that name could not be found.");
			return;
		}

		PraxiPlayer targetData = PraxiPlayer.getByUuid(target.getUniqueId());
		Party party = targetData.getParty();

		if (party == null) {
			player.sendMessage(Style.RED + "A party with that name could not be found.");
			return;
		}

		if (party.getState() == PartyState.CLOSED) {
			if (!party.isInvited(player)) {
				player.sendMessage(Style.RED + "You have not been invited to that party.");
				return;
			}
		}

		if (party.getPlayers().size() >= 32) {
			player.sendMessage(Style.RED + "That party is full and cannot hold anymore players.");
			return;
		}

		party.join(player);
	}

	@Command(names = { "p leave", "party leave" })
	public static void leave(Player player) {
		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.getParty() == null) {
			player.sendMessage(Style.RED + "You do not have a party.");
			return;
		}

		if (praxiPlayer.getParty().getLeader().getUuid().equals(player.getUniqueId())) {
			praxiPlayer.getParty().disband();
		} else {
			praxiPlayer.getParty().leave(player, false);
		}
	}

	@Command(names = { "p kick", "party kick" })
	public static void kick(Player player, @Parameter(name = "target") Player target) {
		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.getParty() == null) {
			player.sendMessage(Style.RED + "You do not have a party.");
			return;
		}

		if (!praxiPlayer.getParty().isLeader(player.getUniqueId())) {
			player.sendMessage(Style.RED + "You are not the leader of your party.");
			return;
		}

		if (!praxiPlayer.getParty().containsPlayer(target)) {
			player.sendMessage(Style.RED + "That player is not a member of your party.");
			return;
		}

		if (player.equals(target)) {
			player.sendMessage(Style.RED + "You cannot kick yourself from your party.");
			return;
		}

		praxiPlayer.getParty().leave(target, true);
	}

	@Command(names = { "p close", "party close" })
	public static void open(Player player) {
		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.getParty() == null) {
			player.sendMessage(Style.RED + "You do not have a party.");
			return;
		}

		if (!praxiPlayer.getParty().isLeader(player.getUniqueId())) {
			player.sendMessage(Style.RED + "You are not the leader of your party.");
			return;
		}

		praxiPlayer.getParty().setState(PartyState.CLOSED);
	}

	@Command(names = { "p open", "party open" })
	public static void close(Player player) {
		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.getParty() == null) {
			player.sendMessage(Style.RED + "You do not have a party.");
			return;
		}

		if (!praxiPlayer.getParty().isLeader(player.getUniqueId())) {
			player.sendMessage(Style.RED + "You are not the leader of your party.");
			return;
		}

		praxiPlayer.getParty().setState(PartyState.OPEN);
	}

	@Command(names = { "p info", "party info", "party information" })
	public static void information(Player player) {
		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (!praxiPlayer.isLoaded()) {
			return;
		}

		if (praxiPlayer.getParty() == null) {
			player.sendMessage(Style.RED + "You do not have a party.");
			return;
		}

		praxiPlayer.getParty().sendInformation(player);
	}

}
