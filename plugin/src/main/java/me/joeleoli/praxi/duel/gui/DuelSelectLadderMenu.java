package me.joeleoli.praxi.duel.gui;

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

public class DuelSelectLadderMenu extends Menu {

	@Override
	public String getTitle(Player player) {
		return Style.GOLD + Style.BOLD + "Select a ladder";
	}

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		Map<Integer, Button> buttons = new HashMap<>();

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
			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			praxiPlayer.setDuelProcedure(null);
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
		public void clicked(Player player, int i, ClickType clickType, int hb) {
			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			// Update duel procedure
			praxiPlayer.getDuelProcedure().setLadder(this.ladder);

			// Set closed by menu
			Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

			// Force close inventory
			player.closeInventory();

			// Open arena selection menu
			new DuelSelectArenaMenu().openMenu(player);
		}

	}

}
