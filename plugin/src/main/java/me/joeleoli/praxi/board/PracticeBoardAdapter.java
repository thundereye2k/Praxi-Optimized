package me.joeleoli.praxi.board;

import java.util.ArrayList;
import java.util.List;
import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.board.Board;
import me.joeleoli.nucleus.board.BoardAdapter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TimeUtil;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.events.Event;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.player.PracticeSetting;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.queue.Queue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class PracticeBoardAdapter implements BoardAdapter {

	@Override
	public String getTitle(Player player) {
		if (Nucleus.getInstance().getRave() != null) {
			return Nucleus.getInstance().getRave().getRaveTask().getTitle();
		}

		return Style.PINK + Style.BOLD + "MineXD   ";
	}

	@Override
	public List<String> getScoreboard(Player player, Board board) {
		if (Nucleus.getInstance().getRave() != null) {
			return Nucleus.getInstance().getRave().getRaveTask().getLines();
		}

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (!NucleusAPI.<Boolean>getSetting(player, PracticeSetting.SHOW_SCOREBOARD)) {
			return null;
		}

		final List<String> toReturn = new ArrayList<>();

		if (praxiPlayer.isInLobby()) {
			toReturn.add(Style.YELLOW + "Online: " + Style.PINK + Bukkit.getOnlinePlayers().size());
			toReturn.add(Style.YELLOW + "Fighting: " + Style.PINK + Praxi.getInstance().getFightingCount());
			toReturn.add(Style.YELLOW + "Queueing: " + Style.PINK + Praxi.getInstance().getQueueingCount());

			if (praxiPlayer.getParty() != null) {
				toReturn.add(Style.YELLOW + "Your Party: " + Style.PINK + praxiPlayer.getParty().getTeamPlayers().size());
			}

			if (!Praxi.getInstance().getEventManager().getEventCooldown().hasExpired()) {
				toReturn.add(Style.YELLOW + "Event Cooldown: " + Style.PINK + TimeUtil.millisToTimer(
						Praxi.getInstance().getEventManager().getEventCooldown().getRemaining()));
			}
		} else if (praxiPlayer.isInQueue()) {
			final Queue queue = praxiPlayer.getQueuePlayer().getQueue();

			toReturn.add(Style.YELLOW + "Queue:");
			toReturn.add(" " + Style.PINK + (queue.isRanked() ? "Ranked" : "Unranked") + " " +
			             queue.getLadder().getName());
			toReturn.add(Style.YELLOW + "Time:");
			toReturn.add(" " + Style.PINK + TimeUtil.millisToTimer(praxiPlayer.getQueuePlayer().getPassed()));

			if (queue.isRanked()) {
				toReturn.add(Style.YELLOW + "Range:");
				toReturn.add(" " + Style.PINK + praxiPlayer.getQueuePlayer().getMinRange() + " -> " +
				             praxiPlayer.getQueuePlayer().getMaxRange());
			}
		} else if (praxiPlayer.isInMatch()) {
			final Match match = praxiPlayer.getMatch();

			if (match == null) {
				return null;
			}

			if (match.isSoloMatch()) {
				final MatchPlayer opponent = match.getOpponentMatchPlayer(player);

				toReturn.add(Style.YELLOW + "Opponent: " + Style.PINK + opponent.getName());
				toReturn.add(Style.YELLOW + "Duration: " + Style.PINK + match.getDuration());

				if (match.isFighting()) {
					toReturn.add("");
					toReturn.add(Style.YELLOW + "Your Ping: " + Style.PINK + player.getPing() + "ms");
					toReturn.add(Style.YELLOW + "Their Ping: " + Style.PINK + opponent.getPing() + "ms");
				}
			} else if (match.isTeamMatch()) {
				toReturn.add(Style.YELLOW + "Duration: " + Style.PINK + match.getDuration());
				toReturn.add(Style.YELLOW + "Opponents: " + Style.PINK + match.getOpponentsLeft(player) + "/" +
				             match.getOpponentTeam(player).getTeamPlayers().size());

				if (match.getTeam(player).getTeamPlayers().size() >= 8) {
					toReturn.add(Style.YELLOW + "Your Team: " + Style.PINK + match.getTeam(player).getTeamPlayers().size());
				} else {
					toReturn.add("");
					toReturn.add(Style.YELLOW + "Your Team:");

					match.getTeam(player).getTeamPlayers().forEach(teamPlayer -> {
						toReturn.add(" " + (teamPlayer.isDisconnected() || !teamPlayer.isAlive() ? Style.STRIKE_THROUGH
								: "") + teamPlayer.getName());
					});
				}
			}
		} else if (praxiPlayer.isSpectating()) {
			final Match match = praxiPlayer.getMatch();

			toReturn.add(Style.YELLOW + "Ladder: " + Style.PINK + match.getLadder().getName());
			toReturn.add(Style.YELLOW + "Duration: " + Style.PINK + match.getDuration());
			toReturn.add(Style.YELLOW + "Players:");

			if (match.isSoloMatch()) {
				toReturn.add(" " + match.getMatchPlayerA().getName() + Style.GRAY + " (" + match.getMatchPlayerA().getPing() + ")");
				toReturn.add(" " + match.getMatchPlayerB().getName() + Style.GRAY + " (" + match.getMatchPlayerB().getPing() + ")");
			} else {
				toReturn.add(" " + match.getTeamA().getLeader().getName() + "'s Team");
				toReturn.add(" " + match.getTeamA().getLeader().getName() + "'s Team");
			}
		} else if (praxiPlayer.isInEvent()) {
			final Event event = praxiPlayer.getEvent();

			toReturn.add(Style.YELLOW + "Event: " + Style.PINK + "Sumo");

			if (event.isWaiting()) {
				toReturn.add(Style.YELLOW + "Players: " + Style.PINK + event.getEventPlayers().size() + "/" + event.getMaxPlayers());
				toReturn.add("");

				if (event.getCooldown() == null) {
					toReturn.add(Style.GRAY + Style.ITALIC + "Waiting for players...");
				} else {
					toReturn.add(Style.GRAY + Style.ITALIC + "Starting in " +
					             TimeUtil.millisToSeconds(event.getCooldown().getRemaining()) + "s");
				}
			} else {
				toReturn.add(Style.YELLOW + "Remaining: " + Style.PINK + event.getRemainingPlayers() + "/" + event.getMaxPlayers());
				toReturn.add(Style.YELLOW + "Duration: " + Style.PINK + event.getRoundDuration());
				toReturn.add(Style.YELLOW + "Players:");
				toReturn.add(" " + event.getRoundPlayerA().getName() + Style.GRAY + " (" + event.getRoundPlayerA().getPing() + " ms)");
				toReturn.add(" " + event.getRoundPlayerB().getName() + Style.GRAY + " (" + event.getRoundPlayerB().getPing() + " ms)");
			}
		}

		toReturn.add(0, Style.BORDER_LINE_SCOREBOARD);
		toReturn.add("");
		toReturn.add(Style.PINK + "minexd.com");
		toReturn.add(Style.BORDER_LINE_SCOREBOARD);

		return toReturn;
	}

	@Override
	public long getInterval() {
		return 2L;
	}

	@Override
	public void preLoop() {
	}

	@Override
	public void onScoreboardCreate(Player player, Scoreboard scoreboard) {
	}

}
