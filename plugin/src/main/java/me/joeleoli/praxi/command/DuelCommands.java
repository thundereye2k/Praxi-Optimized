package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.duel.DuelProcedure;
import me.joeleoli.praxi.duel.DuelRequest;
import me.joeleoli.praxi.duel.gui.DuelSelectLadderMenu;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.impl.SoloMatch;
import me.joeleoli.praxi.player.PracticeSetting;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.player.RematchData;
import org.bukkit.entity.Player;

public class DuelCommands {

	@Command(names = "duel")
	public static void duel(Player player, @Parameter(name = "target") Player target) {
		if (NucleusAPI.isFrozen(player)) {
			player.sendMessage(Style.RED + "You cannot duel while frozen.");
			return;
		}

		if (NucleusAPI.isFrozen(target)) {
			player.sendMessage(Style.RED + "You cannot duel a frozen player.");
			return;
		}

		if (player.getUniqueId().equals(target.getUniqueId())) {
			player.sendMessage(Style.RED + "You cannot duel yourself.");
			return;
		}

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
		final PraxiPlayer targetData = PraxiPlayer.getByUuid(target.getUniqueId());

		if (praxiPlayer.isBusy()) {
			player.sendMessage(Style.RED + "You cannot duel right now.");
			return;
		}

		if (targetData.isBusy()) {
			player.sendMessage(NucleusAPI.getColoredName(target) + Style.RED + " is currently busy.");
			return;
		}

		if (!NucleusAPI.<Boolean>getSetting(target, PracticeSetting.RECEIVE_DUEL_REQUESTS)) {
			player.sendMessage(Style.RED + "That player is not accepting duel requests at the moment.");
			return;
		}

		if (!praxiPlayer.canSendDuelRequest(player)) {
			player.sendMessage(Style.RED + "You have already sent that player a duel request.");
			return;
		}

		DuelProcedure procedure = new DuelProcedure();

		procedure.setSender(player);
		procedure.setTarget(target);

		praxiPlayer.setDuelProcedure(procedure);

		new DuelSelectLadderMenu().openMenu(player);
	}

	@Command(names = "duel accept")
	public static void accept(Player player, @Parameter(name = "target") Player target) {
		if (NucleusAPI.isFrozen(player)) {
			player.sendMessage(Style.RED + "You cannot duel while frozen.");
			return;
		}

		if (NucleusAPI.isFrozen(target)) {
			player.sendMessage(Style.RED + "You cannot duel a frozen player.");
			return;
		}

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
		final PraxiPlayer targetData = PraxiPlayer.getByUuid(target.getUniqueId());

		if (!targetData.isPendingDuelRequest(player)) {
			player.sendMessage(Style.RED + "You do not have a pending duel request from " + NucleusAPI.getColoredName(target) + Style.RED + ".");
			return;
		}

		if (praxiPlayer.isBusy()) {
			player.sendMessage(Style.RED + "You cannot duel right now.");
			return;
		}

		if (targetData.isBusy()) {
			player.sendMessage(NucleusAPI.getColoredName(target) + Style.RED + " is currently busy.");
			return;
		}

		final DuelRequest request = targetData.getSentDuelRequests().get(player.getUniqueId());

		Arena arena = request.getArena();

		if (arena.isActive()) {
			arena = Arena.getRandom(request.getLadder());
		}

		if (arena == null) {
			player.sendMessage(Style.RED + "Tried to start a match but there are no available arenas.");
			return;
		}

		arena.setActive(true);

		Match match = new SoloMatch(new MatchPlayer(player), new MatchPlayer(target), request.getLadder(), arena, false,
				true
		);

		match.handleStart();
	}

	@Command(names = "rematch")
	public static void rematch(Player player) {
		if (NucleusAPI.isFrozen(player)) {
			player.sendMessage(Style.RED + "You cannot duel while frozen.");
			return;
		}

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.getRematchData() == null) {
			player.sendMessage(Style.RED + "You do not have anyone to re-match.");
			return;
		}

		praxiPlayer.refreshHotbar();

		if (praxiPlayer.getRematchData() == null) {
			player.sendMessage(Style.RED + "That player is no longer available.");
			return;
		}

		final RematchData rematchData = praxiPlayer.getRematchData();

		if (rematchData.isReceive()) {
			rematchData.accept();
		} else {
			if (rematchData.isSent()) {
				player.sendMessage(Style.RED + "You have already sent a rematch request to that player.");
				return;
			}

			rematchData.request();
		}
	}

}
