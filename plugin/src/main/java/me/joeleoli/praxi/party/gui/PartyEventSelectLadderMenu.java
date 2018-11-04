package me.joeleoli.praxi.party.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.MatchTeam;
import me.joeleoli.praxi.match.impl.TeamMatch;
import me.joeleoli.praxi.party.Party;
import me.joeleoli.praxi.party.PartyEvent;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PartyEventSelectLadderMenu extends Menu {

	@Override
	public String getTitle(Player player) {
		return Style.GOLD + Style.BOLD + "Select a ladder";
	}

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		final Map<Integer, Button> buttons = new HashMap<>();

		for (Ladder ladder : Ladder.getLadders()) {
			if (ladder.isEnabled()) {
				buttons.put(buttons.size(), new SelectLadderButton(ladder));
			}
		}

		return buttons;
	}

	@Override
	public void onClose(Player player) {
		if (!this.isClosedByMenu()) {
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer.getParty() != null) {
				praxiPlayer.getParty().setSelectedEvent(null);
			}
		}
	}

	@AllArgsConstructor
	private class SelectLadderButton extends Button {

		private Ladder ladder;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(this.ladder.getDisplayIcon())
					.name(Style.PINK + Style.BOLD + this.ladder.getName())
					.lore(Arrays.asList(
							"",
							Style.YELLOW + "Click here to select " + Style.PINK + Style.BOLD +
							this.ladder.getName() + Style.YELLOW + "."
					))
					.build();
		}

		@Override
		public void clicked(Player player, int slot, ClickType clickType, int hbSlot) {
			Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

			player.closeInventory();

			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer.getParty() == null) {
				player.sendMessage(Style.RED + "You are not in a party.");
				return;
			}

			if (praxiPlayer.getParty().getSelectedEvent() == null) {
				return;
			}

			if (praxiPlayer.getParty().getTeamPlayers().size() <= 1) {
				player.sendMessage(Style.RED + "You do not have enough players in your party to start an event.");
				return;
			}

			Party party = praxiPlayer.getParty();
			Arena arena = Arena.getRandom(this.ladder);

			if (arena == null) {
				player.sendMessage(Style.RED + "There are no available arenas.");
				return;
			}

			arena.setActive(true);

			Match match;

			if (party.getSelectedEvent() == PartyEvent.FFA) {
				player.sendMessage(Style.RED + "The FFA party event is currently disabled.");
				return;
			} else {
				MatchTeam teamA = new MatchTeam(new MatchPlayer(party.getLeader().toPlayer()));
				MatchTeam teamB = new MatchTeam(new MatchPlayer(party.getPlayers().get(1)));

				final List<Player> players = new ArrayList<>();

				players.addAll(party.getPlayers());

				Collections.shuffle(players);

				// Create match
				match = new TeamMatch(teamA, teamB, this.ladder, arena);

				for (Player other : players) {
					final PraxiPlayer otherData = PraxiPlayer.getByUuid(other.getUniqueId());

					otherData.setState(PlayerState.IN_MATCH);
					otherData.setMatch(match);

					if (teamA.getLeader().getUuid().equals(other.getUniqueId()) ||
					    teamB.getLeader().getUuid().equals(other.getUniqueId())) {
						continue;
					}

					if (teamA.getTeamPlayers().size() > teamB.getTeamPlayers().size()) {
						teamB.getTeamPlayers().add(new MatchPlayer(other));
					} else {
						teamA.getTeamPlayers().add(new MatchPlayer(other));
					}
				}
			}

			// Start match
			match.handleStart();
		}

	}

}
