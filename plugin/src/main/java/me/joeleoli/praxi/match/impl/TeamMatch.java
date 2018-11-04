package me.joeleoli.praxi.match.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.MatchSnapshot;
import me.joeleoli.praxi.match.MatchState;
import me.joeleoli.praxi.match.MatchTeam;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

@Getter
public class TeamMatch extends Match {

	private MatchTeam teamA;
	private MatchTeam teamB;
	private int teamARoundWins = 0;
	private int teamBRoundWins = 0;

	public TeamMatch(MatchTeam teamA, MatchTeam teamB, Ladder ladder, Arena arena) {
		super(null, ladder, arena, false);

		this.teamA = teamA;
		this.teamB = teamB;
	}

	@Override
	public boolean isDuel() {
		return false;
	}

	@Override
	public boolean isSoloMatch() {
		return false;
	}

	@Override
	public boolean isTeamMatch() {
		return true;
	}

	@Override
	public void onStart() {
	}

	@Override
	public void onEnd() {
		final MatchTeam winningTeam = this.getWinningTeam();
		final MatchTeam losingTeam = this.getOpponentTeam(winningTeam);
		final ChatComponentBuilder winnerInventories = new ChatComponentBuilder("");
		final ChatComponentBuilder loserInventories = new ChatComponentBuilder("");

		winnerInventories
				.append("Winners: ")
				.color(ChatColor.GREEN);
		loserInventories
				.append("Losers: ")
				.color(ChatColor.RED);

		for (MatchPlayer matchPlayer : winningTeam.getTeamPlayers()) {
			final HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, HOVER_TEXT);
			final ClickEvent click =
					new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewinv " + matchPlayer.getUuid().toString());

			winnerInventories
					.append(matchPlayer.getName())
					.color(ChatColor.YELLOW);
			winnerInventories
					.setCurrentHoverEvent(hover)
					.setCurrentClickEvent(click)
					.append(", ")
					.color(ChatColor.YELLOW);
		}

		for (MatchPlayer matchPlayer : losingTeam.getTeamPlayers()) {
			final HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, HOVER_TEXT);
			final ClickEvent click =
					new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewinv " + matchPlayer.getUuid().toString());

			loserInventories
					.append(matchPlayer.getName())
					.color(ChatColor.YELLOW);
			loserInventories
					.setCurrentHoverEvent(hover)
					.setCurrentClickEvent(click)
					.append(", ")
					.color(ChatColor.YELLOW);
		}

		winnerInventories.getCurrent().setText(winnerInventories.getCurrent().getText().substring(
				0,
				winnerInventories.getCurrent().getText().length() - 2
		));
		loserInventories.getCurrent().setText(loserInventories.getCurrent().getText().substring(
				0,
				loserInventories.getCurrent().getText().length() - 2
		));

		final List<BaseComponent[]> components = new ArrayList<>();

		components.add(new ChatComponentBuilder("").parse(Style.getBorderLine()).create());
		components.add(new ChatComponentBuilder("").parse("&dPost-Match Inventories &7(click name to view)").create());
		components.add(winnerInventories.create());
		components.add(loserInventories.create());
		components.add(new ChatComponentBuilder("").parse(Style.getBorderLine()).create());

		for (Player player : this.getPlayers()) {
			components.forEach(player::sendMessage);
		}

		for (Player player : this.getSpectators()) {
			components.forEach(player::sendMessage);
		}
	}

	@Override
	public boolean canEnd() {
		if (this.getLadder().isSumo()) {
			return (this.teamA.getDeadCount() + this.teamA.getDisconnectedCount()) >= this.teamA.getTeamPlayers().size()
			       ||
			       (this.teamB.getDeadCount() + this.teamB.getDisconnectedCount()) >= this.teamB.getTeamPlayers().size()
			       || this.teamARoundWins == 3
			       || this.teamBRoundWins == 3;
		} else {
			return this.teamA.getAliveTeamPlayers().isEmpty() || this.teamB.getAliveTeamPlayers().isEmpty();
		}
	}

	@Override
	public Player getWinningPlayer() {
		throw new UnsupportedOperationException("Cannot get solo winning player from a TeamMatch");
	}

	@Override
	public MatchTeam getWinningTeam() {
		if (this.getLadder().isSumo()) {
			if (this.teamA.getDisconnectedCount() == this.teamA.getTeamPlayers().size()) {
				return this.teamB;
			} else if (this.teamB.getDisconnectedCount() == this.teamB.getTeamPlayers().size()) {
				return this.teamA;
			}

			return this.teamARoundWins == 3 ? this.teamA : this.teamB;
		} else {
			if (this.teamA.getAliveTeamPlayers().isEmpty()) {
				return this.teamB;
			} else if (this.teamB.getAliveTeamPlayers().isEmpty()) {
				return this.teamA;
			} else {
				return null;
			}
		}
	}

	@Override
	public MatchPlayer getMatchPlayerA() {
		throw new UnsupportedOperationException("Cannot get solo match player from a TeamMatch");
	}

	@Override
	public MatchPlayer getMatchPlayerB() {
		throw new UnsupportedOperationException("Cannot get solo match player from a TeamMatch");
	}

	@Override
	public List<MatchPlayer> getMatchPlayers() {
		List<MatchPlayer> matchPlayers = new ArrayList<>();

		matchPlayers.addAll(this.teamA.getTeamPlayers());
		matchPlayers.addAll(this.teamB.getTeamPlayers());

		return matchPlayers;
	}

	@Override
	public Player getPlayerA() {
		throw new UnsupportedOperationException("Cannot get solo player from a TeamMatch");
	}

	@Override
	public Player getPlayerB() {
		throw new UnsupportedOperationException("Cannot get solo player from a TeamMatch");
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<>();

		this.teamA.getTeamPlayers().forEach(matchPlayer -> {
			Player player = matchPlayer.toPlayer();

			if (player != null) {
				players.add(player);
			}
		});

		this.teamB.getTeamPlayers().forEach(matchPlayer -> {
			Player player = matchPlayer.toPlayer();

			if (player != null) {
				players.add(player);
			}
		});

		return players;
	}

	@Override
	public MatchTeam getTeamA() {
		return this.teamA;
	}

	@Override
	public MatchTeam getTeamB() {
		return this.teamB;
	}

	@Override
	public MatchTeam getTeam(MatchPlayer matchPlayer) {
		for (MatchPlayer teamMatchPlayer : this.teamA.getTeamPlayers()) {
			if (teamMatchPlayer.getUuid().equals(matchPlayer.getUuid())) {
				return this.teamA;
			}
		}

		for (MatchPlayer teamMatchPlayer : this.teamB.getTeamPlayers()) {
			if (teamMatchPlayer.getUuid().equals(matchPlayer.getUuid())) {
				return this.teamB;
			}
		}

		return null;
	}

	@Override
	public MatchTeam getTeam(Player player) {
		for (MatchPlayer teamMatchPlayer : this.teamA.getTeamPlayers()) {
			if (teamMatchPlayer.getUuid().equals(player.getUniqueId())) {
				return this.teamA;
			}
		}

		for (MatchPlayer teamMatchPlayer : this.teamB.getTeamPlayers()) {
			if (teamMatchPlayer.getUuid().equals(player.getUniqueId())) {
				return this.teamB;
			}
		}

		return null;
	}

	@Override
	public MatchPlayer getMatchPlayer(Player player) {
		for (MatchPlayer matchPlayer : this.teamA.getTeamPlayers()) {
			if (matchPlayer.getUuid().equals(player.getUniqueId())) {
				return matchPlayer;
			}
		}

		for (MatchPlayer matchPlayer : this.teamB.getTeamPlayers()) {
			if (matchPlayer.getUuid().equals(player.getUniqueId())) {
				return matchPlayer;
			}
		}

		return null;
	}

	@Override
	public int getOpponentsLeft(Player player) {
		if (this.teamA.containsPlayer(player)) {
			return this.teamB.getAliveCount() - this.teamB.getDisconnectedCount();
		} else if (this.teamB.containsPlayer(player)) {
			return this.teamA.getAliveCount() - this.teamA.getDisconnectedCount();
		} else {
			return -1;
		}
	}

	@Override
	public MatchTeam getOpponentTeam(MatchTeam team) {
		if (this.teamA.equals(team)) {
			return this.teamB;
		} else if (this.teamB.equals(team)) {
			return this.teamA;
		} else {
			return null;
		}
	}

	@Override
	public MatchTeam getOpponentTeam(Player player) {
		if (this.teamA.containsPlayer(player)) {
			return this.teamB;
		} else if (this.teamB.containsPlayer(player)) {
			return this.teamA;
		} else {
			return null;
		}
	}

	@Override
	public Player getOpponentPlayer(Player player) {
		throw new UnsupportedOperationException("Cannot get solo opponent player from TeamMatch");
	}

	@Override
	public MatchPlayer getOpponentMatchPlayer(Player player) {
		throw new UnsupportedOperationException("Cannot get solo opponent match player from TeamMatch");
	}

	@Override
	public int getTotalRoundWins() {
		return this.teamARoundWins + this.teamBRoundWins;
	}

	@Override
	public int getRoundWins(MatchPlayer matchPlayer) {
		throw new UnsupportedOperationException("Cannot get solo round wins from TeamMatch");
	}

	@Override
	public int getRoundWins(MatchTeam matchTeam) {
		if (this.teamA.equals(matchTeam)) {
			return this.teamARoundWins;
		} else if (this.teamB.equals(matchTeam)) {
			return this.teamBRoundWins;
		} else {
			return -1;
		}
	}

	@Override
	public int getRoundsNeeded(MatchPlayer matchPlayer) {
		throw new UnsupportedOperationException("Cannot get solo rounds needed from TeamMatch");
	}

	@Override
	public int getRoundsNeeded(MatchTeam matchTeam) {
		if (this.teamA.equals(matchTeam)) {
			return 3 - this.teamARoundWins;
		} else if (this.teamB.equals(matchTeam)) {
			return 3 - this.teamBRoundWins;
		} else {
			return -1;
		}
	}

	@Override
	public void onDeath(Player player, Player killer) {
		//        TODO: request teams messages directly then request global messages to spectators
		//        MatchTeam roundLoser = this.getOpponentTeam(player);

		this.getSnapshots().add(new MatchSnapshot(this.getMatchPlayer(player)));

		PlayerUtil.reset(player);

		this.getPlayers().forEach(matchPlayer -> {
			matchPlayer.hidePlayer(player);
		});

		if (this.getLadder().isSumo()) {
			final MatchTeam deadTeam = this.getTeam(player);
			final MatchTeam roundWinner = this.getOpponentTeam(deadTeam);
			final int dead = deadTeam.getDisconnectedCount() + deadTeam.getDeadCount();

			if (dead == deadTeam.getTeamPlayers().size()) {
				if (this.teamA.equals(roundWinner)) {
					this.teamARoundWins++;
				} else {
					this.teamBRoundWins++;
				}

				if (this.canEnd()) {
					this.setState(MatchState.ENDING);
					this.getPlayers().forEach(other -> other.hidePlayer(player));
					this.getSpectators().forEach(other -> other.hidePlayer(player));
				} else {
					final String broadcast =
							roundWinner.getLeader().getDisplayName() + Style.YELLOW + "'s team has " + Style.GREEN +
							"won" + Style.YELLOW + " the round, they need " + Style.GOLD +
							this.getRoundsNeeded(roundWinner) + Style.YELLOW + " more to win.";

					this.broadcastMessage(broadcast);
					this.handleStart();
				}
			} else {
				for (Player other : this.getPlayers()) {
					other.hidePlayer(player);
				}

				player.setAllowFlight(true);
				player.setFlying(true);
				player.updateInventory();
			}
		} else {
			if (!this.canEnd()) {
				player.setAllowFlight(true);
				player.setFlying(true);
				player.updateInventory();
			}
		}
	}

	@Override
	public void onRespawn(Player player) {
		if (this.getLadder().isSumo() && !this.isEnding()) {
			for (MatchPlayer matchPlayer : this.teamA.getTeamPlayers()) {
				if (matchPlayer.isDisconnected()) {
					continue;
				}

				final Player toPlayer = matchPlayer.toPlayer();

				if (toPlayer != null && toPlayer.isOnline()) {
					toPlayer.teleport(this.getArena().getSpawn1());
				}
			}

			for (MatchPlayer matchPlayer : this.teamB.getTeamPlayers()) {
				if (matchPlayer.isDisconnected()) {
					continue;
				}

				final Player toPlayer = matchPlayer.toPlayer();

				if (toPlayer != null && toPlayer.isOnline()) {
					toPlayer.teleport(this.getArena().getSpawn2());
				}
			}
		} else {
			player.teleport(player.getLocation().clone().add(0, 3, 0));
		}
	}

}
