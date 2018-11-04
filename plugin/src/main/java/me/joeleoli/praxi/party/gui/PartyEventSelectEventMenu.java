package me.joeleoli.praxi.party.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.party.PartyEvent;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PartyEventSelectEventMenu extends Menu {

	@Override
	public String getTitle(Player player) {
		return Style.BLUE + Style.BOLD + "Select an event";
	}

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		final Map<Integer, Button> buttons = new HashMap<>();

		buttons.put(3, new SelectEventButton(PartyEvent.FFA));
		buttons.put(5, new SelectEventButton(PartyEvent.SPLIT));

		return buttons;
	}

	@AllArgsConstructor
	private class SelectEventButton extends Button {

		private PartyEvent partyEvent;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(this.partyEvent == PartyEvent.FFA ? Material.QUARTZ : Material.REDSTONE)
					.name(Style.GREEN + Style.BOLD + this.partyEvent.getName())
					.lore(Arrays.asList(
							"",
							Style.YELLOW + "Click here to select " + Style.GREEN + Style.BOLD +
							this.partyEvent.getName() + Style.YELLOW + "."
					))
					.build();
		}

		@Override
		public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer.getParty() == null) {
				player.sendMessage(Style.RED + "You are not in a party.");
				return;
			}

			praxiPlayer.getParty().setSelectedEvent(this.partyEvent);

			new PartyEventSelectLadderMenu().openMenu(player);
		}

	}

}
