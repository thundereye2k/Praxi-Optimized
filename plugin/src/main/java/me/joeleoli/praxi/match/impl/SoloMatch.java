package me.joeleoli.praxi.match.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.elo.EloUtil;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.MatchSnapshot;
import me.joeleoli.praxi.match.MatchState;
import me.joeleoli.praxi.match.MatchTeam;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PraxiPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

@Getter
public class SoloMatch extends Match {

	private MatchPlayer playerA;
	private MatchPlayer playerB;
	private int playerARoundWins = 0;
	private int playerBRoundWins = 0;
	private boolean duel;

	public SoloMatch(MatchPlayer playerA, MatchPlayer playerB, Ladder ladder, Arena arena, boolean ranked,
			boolean duel) {
		this(null, playerA, playerB, ladder, arena, ranked, duel);

		this.duel = duel;
	}

	public SoloMatch(UUID queueId, MatchPlayer playerA, MatchPlayer playerB, Ladder ladder, Arena arena, boolean ranked,
			boolean duel) {
		super(queueId, ladder, arena, ranked);

		this.playerA = playerA;
		this.playerB = playerB;
		this.duel = duel;
	}

	@Override
	public boolean isSoloMatch() {
		return true;
	}

	@Override
	public boolean isTeamMatch() {
		return false;
	}

	@Override
	public void onStart() {
		if (this.getTotalRoundWins() == 0) {
			for (MatchPlayer matchPlayer : new MatchPlayer[]{ this.playerA, this.playerB }) {
				final Player player = matchPlayer.toPlayer();
				final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

				praxiPlayer.setState(PlayerState.IN_MATCH);
				praxiPlayer.setMatch(this);
			}
		}
	}

	@Override
	public void onEnd() {
		final Player winningPlayer = this.getWinningPlayer();
		final Player losingPlayer = this.getOpponentPlayer(winningPlayer);
		final MatchPlayer winningMatchPlayer = this.getMatchPlayer(winningPlayer);
		final MatchPlayer losingMatchPlayer = this.getMatchPlayer(losingPlayer);
		final HoverEvent winnerHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, HOVER_TEXT);
		final ClickEvent winnerClickEvent =
				new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewinv " + winningPlayer.getUniqueId().toString());
		final HoverEvent loserHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, HOVER_TEXT);
		final ClickEvent loserClickEvent =
				new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewinv " + losingPlayer.getUniqueId().toString());
		final ChatComponentBuilder inventoriesBuilder = new ChatComponentBuilder("");

		inventoriesBuilder
				.append("Winner: ")
				.color(ChatColor.GREEN)
				.append(winningPlayer.getName())
				.color(ChatColor.YELLOW);
		inventoriesBuilder
				.setCurrentHoverEvent(winnerHoverEvent)
				.setCurrentClickEvent(winnerClickEvent)
				.append(" - ")
				.color(ChatColor.GRAY)
				.append("Loser: ")
				.color(ChatColor.RED)
				.append(losingPlayer.getName())
				.color(ChatColor.YELLOW);
		inventoriesBuilder
				.setCurrentHoverEvent(loserHoverEvent)
				.setCurrentClickEvent(loserClickEvent);

		final List<BaseComponent[]> components = new ArrayList<>();

		components.add(new ChatComponentBuilder("").parse("&dPost-Match Inventories &7(click name to view)").create());
		components.add(inventoriesBuilder.create());

		if (this.isRanked()) {
			final int oldWinnerElo = winningMatchPlayer.getElo();
			final int oldLoserElo = losingMatchPlayer.getElo();
			final int newWinnerElo = EloUtil.getNewRating(oldWinnerElo, oldLoserElo, true);
			final int newLoserElo = EloUtil.getNewRating(oldLoserElo, oldWinnerElo, false);

			final PraxiPlayer winningPraxiPlayer = PraxiPlayer.getByUuid(winningPlayer.getUniqueId());
			final PraxiPlayer losingPraxiPlayer = PraxiPlayer.getByUuid(losingPlayer.getUniqueId());

			if (winningPraxiPlayer.isLoaded()) {
				winningPraxiPlayer.getStatistics().getLadderStatistics(this.getLadder()).setElo(newWinnerElo);
			}

			if (losingPraxiPlayer.isLoaded()) {
				losingPraxiPlayer.getStatistics().getLadderStatistics(this.getLadder()).setElo(newLoserElo);
			}

			int winnerEloChange = newWinnerElo - oldWinnerElo;
			int loserEloChange = oldLoserElo - newLoserElo;

			components.add(new ChatComponentBuilder("")
					.parse("&dELO Changes: &a" + winningPlayer.getName() + " +" + winnerEloChange + " (" +
					       newWinnerElo + ") &c" + losingPlayer.getName() + " -" + loserEloChange + " (" + newLoserElo +
					       ")")
					.create());
		}

		components.add(0, new ChatComponentBuilder("").parse(Style.getBorderLine()).create());
		components.add(new ChatComponentBuilder("").parse(Style.getBorderLine()).create());

		for (Player player : new Player[]{ winningPlayer, losingPlayer }) {
			components.forEach(player::sendMessage);
		}

		for (Player player : this.getSpectators()) {
			components.forEach(player::sendMessage);
		}
	}

	@Override
	public boolean canEnd() {
		if (this.getLadder().isSumo()) {
			return this.playerA.isDisconnected()
			       || this.playerB.isDisconnected()
			       || (this.isRanked() ? (this.playerARoundWins == 3 || this.playerBRoundWins == 3)
					: (this.playerARoundWins == 1 || this.playerBRoundWins == 1));
		} else {
			return !this.playerA.isAlive() || !this.playerB.isAlive();
		}
	}

	@Override
	public Player getWinningPlayer() {
		if (this.playerA.isDisconnected() || !this.playerA.isAlive()) {
			return this.playerB.toPlayer();
		} else {
			return this.playerA.toPlayer();
		}
	}

	@Override
	public MatchTeam getWinningTeam() {
		throw new UnsupportedOperationException("Cannot get winning team from a SoloMatch");
	}

	@Override
	public MatchPlayer getMatchPlayerA() {
		return this.playerA;
	}

	@Override
	public MatchPlayer getMatchPlayerB() {
		return this.playerB;
	}

	@Override
	public List<MatchPlayer> getMatchPlayers() {
		throw new UnsupportedOperationException("Cannot get match players from a SoloMatch");
	}

	@Override
	public Player getPlayerA() {
		return this.playerA.toPlayer();
	}

	@Override
	public Player getPlayerB() {
		return this.playerB.toPlayer();
	}

	@Override
	public List<Player> getPlayers() {
		final List<Player> players = new ArrayList<>();

		final Player playerA = this.playerA.toPlayer();
		final Player playerB = this.playerB.toPlayer();

		if (playerA != null) {
			players.add(playerA);
		}

		if (playerB != null) {
			players.add(playerB);
		}

		return players;
	}

	@Override
	public MatchTeam getTeamA() {
		throw new UnsupportedOperationException("Cannot get team from a SoloMatch");
	}

	@Override
	public MatchTeam getTeamB() {
		throw new UnsupportedOperationException("Cannot get team from a SoloMatch");
	}

	@Override
	public MatchTeam getTeam(MatchPlayer matchPlayer) {
		throw new UnsupportedOperationException("Cannot get team from a SoloMatch");
	}

	@Override
	public MatchTeam getTeam(Player player) {
		throw new UnsupportedOperationException("Cannot get team from a SoloMatch");
	}

	@Override
	public MatchPlayer getMatchPlayer(Player player) {
		if (this.playerA.getUuid().equals(player.getUniqueId())) {
			return this.playerA;
		} else if (this.playerB.getUuid().equals(player.getUniqueId())) {
			return this.playerB;
		} else {
			return null;
		}
	}

	@Override
	public int getOpponentsLeft(Player player) {
		throw new UnsupportedOperationException("Cannot get opponents left from a SoloMatch");
	}

	@Override
	public MatchTeam getOpponentTeam(MatchTeam team) {
		throw new UnsupportedOperationException("Cannot get opponent team from a SoloMatch");
	}

	@Override
	public MatchTeam getOpponentTeam(Player player) {
		throw new UnsupportedOperationException("Cannot get opponent team from a SoloMatch");
	}

	@Override
	public Player getOpponentPlayer(Player player) {
		if (player == null) {
			return null;
		}

		if (this.playerA.getUuid().equals(player.getUniqueId())) {
			return this.playerB.toPlayer();
		} else if (this.playerB.getUuid().equals(player.getUniqueId())) {
			return this.playerA.toPlayer();
		} else {
			return null;
		}
	}

	@Override
	public MatchPlayer getOpponentMatchPlayer(Player player) {
		if (this.playerA.getUuid().equals(player.getUniqueId())) {
			return this.playerB;
		} else if (this.playerB.getUuid().equals(player.getUniqueId())) {
			return this.playerA;
		} else {
			return null;
		}
	}

	@Override
	public int getTotalRoundWins() {
		return this.playerARoundWins + this.playerBRoundWins;
	}

	@Override
	public int getRoundWins(MatchPlayer matchPlayer) {
		if (this.playerA.equals(matchPlayer)) {
			return this.playerARoundWins;
		} else if (this.playerB.equals(matchPlayer)) {
			return this.playerBRoundWins;
		} else {
			return -1;
		}
	}

	@Override
	public int getRoundWins(MatchTeam matchTeam) {
		throw new UnsupportedOperationException("Cannot get team round wins from SoloMatch");
	}

	@Override
	public int getRoundsNeeded(MatchPlayer matchPlayer) {
		if (this.playerA.equals(matchPlayer)) {
			return 3 - this.playerARoundWins;
		} else if (this.playerB.equals(matchPlayer)) {
			return 3 - this.playerBRoundWins;
		} else {
			return -1;
		}
	}

	@Override
	public int getRoundsNeeded(MatchTeam matchTeam) {
		throw new UnsupportedOperationException("Cannot get team round wins from SoloMatch");
	}

	@Override
	public void onDeath(Player player, Player killer) {
		MatchPlayer roundLoser = this.getMatchPlayer(player);
		MatchPlayer roundWinner = this.getOpponentMatchPlayer(player);

		this.getSnapshots().add(new MatchSnapshot(roundLoser, roundWinner));

		PlayerUtil.reset(player);

		if (this.getLadder().isSumo()) {
			if (this.playerA.getUuid().equals(player.getUniqueId())) {
				this.playerBRoundWins++;
			} else {
				this.playerARoundWins++;
			}

			if (this.canEnd()) {
				final String broadcast =
						Style.PINK + roundWinner.getName() + Style.YELLOW + " has " + Style.GREEN + "won" +
						Style.YELLOW + " the match.";

				this.setState(MatchState.ENDING);
				this.broadcastMessage(broadcast);
				this.getOpponentPlayer(player).hidePlayer(player);
				this.getSpectators().forEach(other -> other.hidePlayer(player));
			} else {
				final String broadcast =
						Style.PINK + roundWinner.getName() + Style.YELLOW + " has " + Style.GREEN + "won" +
						Style.YELLOW + " the round, they need " + Style.GOLD + this.getRoundsNeeded(roundWinner) +
						Style.YELLOW + " more to win.";

				this.broadcastMessage(broadcast);
				this.handleStart();
			}
		}
	}

	@Override
	public void onRespawn(Player player) {
		if (this.getLadder().isSumo() && !this.isEnding()) {
			player.teleport(this.getArena().getSpawn1());
			this.getOpponentPlayer(player).teleport(this.getArena().getSpawn2());
		} else {
			player.teleport(player.getLocation().clone().add(0, 3, 0));
		}
	}

}
