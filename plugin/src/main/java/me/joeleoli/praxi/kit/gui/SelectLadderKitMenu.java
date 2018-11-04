package me.joeleoli.praxi.kit.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SelectLadderKitMenu extends Menu {

	@Override
	public String getTitle(Player player) {
		return Style.GOLD + Style.BOLD + "Select a ladder";
	}

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		Map<Integer, Button> buttons = new HashMap<>();

		Ladder.getLadders().forEach(ladder -> {
			if (ladder.isEnabled()) {
				buttons.put(buttons.size(), new LadderKitDisplayButton(ladder));
			}
		});

		return buttons;
	}

	@AllArgsConstructor
	private class LadderKitDisplayButton extends Button {

		private Ladder ladder;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(this.ladder.getDisplayIcon())
					.name(Style.PINK + Style.BOLD + this.ladder.getName())
					.lore(Arrays.asList(
							"",
							Style.YELLOW + "Click to select " + Style.PINK + Style.BOLD + this.ladder.getName() +
							Style.YELLOW + "."
					))
					.build();
		}

		@Override
		public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
			player.closeInventory();

			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			praxiPlayer.getKitEditor().setSelectedLadder(this.ladder);
			praxiPlayer.getKitEditor().setPreviousState(praxiPlayer.getState());

			new KitManagementMenu(this.ladder).openMenu(player);
		}

	}
}
