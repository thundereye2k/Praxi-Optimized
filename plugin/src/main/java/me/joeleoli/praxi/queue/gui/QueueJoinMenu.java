package me.joeleoli.praxi.queue.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.queue.Queue;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class QueueJoinMenu extends Menu {

	private boolean ranked;

	@Override
	public String getTitle(Player player) {
		return Style.GOLD + "Join " + (this.ranked ? "Ranked" : "Unranked") + " Queue";
	}

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		Map<Integer, Button> buttons = new HashMap<>();

		int i = 0;

		for (Queue queue : Queue.getQueues()) {
			if (queue.isRanked() == this.ranked) {
				buttons.put(i++, new SelectLadderButton(queue));
			}
		}

		return buttons;
	}

	@AllArgsConstructor
	private class SelectLadderButton extends Button {

		private Queue queue;

		@Override
		public ItemStack getButtonItem(Player player) {
			final List<String> lore = new ArrayList<>();

			lore.add(Style.YELLOW + "Fighting: " + Style.RESET + Praxi.getInstance().getFightingCount(this.queue));
			lore.add(Style.YELLOW + "Queueing: " + Style.RESET + this.queue.getPlayers().size());
			lore.add("");
			lore.add(Style.YELLOW + "Click here to select " + Style.PINK + Style.BOLD +
			         this.queue.getLadder().getName() + Style.YELLOW + ".");

			return new ItemBuilder(this.queue.getLadder().getDisplayIcon())
					.name(Style.PINK + Style.BOLD + this.queue.getLadder().getName()).lore(lore)
					.build();
		}

		@Override
		public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer == null) {
				return;
			}

			if (NucleusAPI.isFrozen(player)) {
				player.sendMessage(Style.RED + "You cannot queue while frozen.");
				return;
			}

			if (praxiPlayer.isBusy()) {
				player.sendMessage(Style.RED + "You cannot queue right now.");
				return;
			}

			player.closeInventory();

			this.queue.addPlayer(
					player,
					!this.queue.isRanked() ? 0 : praxiPlayer.getStatistics().getElo(this.queue.getLadder())
			);
		}

	}
}
