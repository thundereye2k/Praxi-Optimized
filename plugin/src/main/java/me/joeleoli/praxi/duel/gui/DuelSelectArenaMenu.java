package me.joeleoli.praxi.duel.gui;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class DuelSelectArenaMenu extends Menu {

	@Override
	public String getTitle(Player player) {
		return Style.BLUE + Style.BOLD + "Select an arena";
	}

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		Map<Integer, Button> buttons = new HashMap<>();

		for (Arena arena : Arena.getArenas()) {
			if (!arena.isSetup()) {
				continue;
			}

			if (!arena.getLadders().contains(praxiPlayer.getDuelProcedure().getLadder().getName())) {
				continue;
			}

			if (praxiPlayer.getDuelProcedure().getLadder().isBuild() && arena.getType() == ArenaType.SHARED) {
				continue;
			}

			if (praxiPlayer.getDuelProcedure().getLadder().isBuild() && arena.getType() != ArenaType.STANDALONE) {
				continue;
			}

			if (praxiPlayer.getDuelProcedure().getLadder().isBuild() && arena.isActive()) {
				continue;
			}

			buttons.put(buttons.size(), new SelectArenaButton(arena));
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
	private class SelectArenaButton extends Button {

		private Arena arena;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.PAPER).name(Style.GREEN + Style.BOLD + this.arena.getName()).build();
		}

		@Override
		public void clicked(Player player, int i, ClickType clickType, int hb) {
			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			// Update and request the procedure
			praxiPlayer.getDuelProcedure().setArena(this.arena);
			praxiPlayer.getDuelProcedure().send();

			// Set closed by menu
			Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

			// Force close inventory
			player.closeInventory();
		}

	}

}
