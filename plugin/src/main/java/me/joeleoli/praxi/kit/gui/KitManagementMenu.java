package me.joeleoli.praxi.kit.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.kit.gui.button.BackButton;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class KitManagementMenu extends Menu {

	private static final Button PLACEHOLDER = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 7, " ");

	private Ladder ladder;

	public KitManagementMenu(Ladder ladder) {
		this.ladder = ladder;

		this.setPlaceholder(true);
		this.setUpdateAfterClick(false);
	}

	@Override
	public String getTitle(Player player) {
		return Style.GOLD + "Viewing " + this.ladder.getName() + " kits";
	}

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		final Map<Integer, Button> buttons = new HashMap<>();
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
		NamedKit[] kits = praxiPlayer.getKits(this.ladder);

		if (kits == null) {
			return buttons;
		}

		int startPos = -1;

		for (int i = 0; i < 4; i++) {
			NamedKit kit = kits[i];
			startPos += 2;

			buttons.put(startPos, kit == null ? new CreateKitButton(i) : new KitDisplayButton(kit));
			buttons.put(startPos + 18, new LoadKitButton(i));
			buttons.put(startPos + 27, kit == null ? PLACEHOLDER : new RenameKitButton(kit));
			buttons.put(startPos + 36, kit == null ? PLACEHOLDER : new DeleteKitButton(kit));
		}

		buttons.put(36, new BackButton(new SelectLadderKitMenu()));

		return buttons;
	}

	@Override
	public void onClose(Player player) {
		if (!this.isClosedByMenu()) {
			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			praxiPlayer.setState(praxiPlayer.getKitEditor().getPreviousState());
			praxiPlayer.getKitEditor().setSelectedLadder(null);
		}
	}

	@AllArgsConstructor
	private class DeleteKitButton extends Button {

		private NamedKit kit;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.STAINED_CLAY)
					.name(Style.RED + Style.BOLD + "Delete")
					.durability(14)
					.lore(Arrays.asList(
							"",
							Style.RED + "Click to delete this kit.",
							Style.RED + "You will " + Style.BOLD + "NOT" + Style.RED + " be able to",
							Style.RED + "recover this kit."
					))
					.build();
		}

		@Override
		public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			praxiPlayer.deleteKit(praxiPlayer.getKitEditor().getSelectedLadder(), this.kit);

			new KitManagementMenu(praxiPlayer.getKitEditor().getSelectedLadder()).openMenu(player);
		}

	}

	@AllArgsConstructor
	private class CreateKitButton extends Button {

		private int index;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.IRON_SWORD)
					.name(Style.GREEN + Style.BOLD + "Create Kit")
					.build();
		}

		@Override
		public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
			final Ladder ladder = praxiPlayer.getKitEditor().getSelectedLadder();

			// TODO: this shouldn't be null but sometimes it is?
			if (ladder == null) {
				player.closeInventory();
				return;
			}

			final NamedKit kit = new NamedKit("Kit " + (this.index + 1));

			if (ladder.getDefaultKit() != null) {
				if (ladder.getDefaultKit().getArmor() != null) {
					kit.setArmor(ladder.getDefaultKit().getArmor());
				}

				if (ladder.getDefaultKit().getContents() != null) {
					kit.setContents(ladder.getDefaultKit().getContents());
				}
			}

			praxiPlayer.replaceKit(ladder, this.index, kit);
			praxiPlayer.getKitEditor().setSelectedKit(kit);

			new KitEditorMenu().openMenu(player);
		}

	}

	@AllArgsConstructor
	private class RenameKitButton extends Button {

		private NamedKit kit;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.SIGN)
					.name(Style.YELLOW + Style.BOLD + "Rename")
					.lore(Arrays.asList(
							"",
							Style.YELLOW + "Click to rename this kit."
					))
					.build();
		}

		@Override
		public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
			Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			praxiPlayer.getKitEditor().setActive(true);
			praxiPlayer.getKitEditor().setRename(true);
			praxiPlayer.getKitEditor().setSelectedKit(this.kit);

			player.closeInventory();
			player.sendMessage(
					Style.YELLOW + "Renaming " + Style.BOLD + this.kit.getName() + Style.YELLOW + "... " + Style.GREEN +
					"Enter the new name now.");
		}

	}

	@AllArgsConstructor
	private class LoadKitButton extends Button {

		private int index;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.BOOK)
					.name(Style.GREEN + Style.BOLD + "Load/Edit")
					.lore(Arrays.asList(
							"",
							Style.YELLOW + "Click to edit this kit."
					))
					.build();
		}

		@Override
		public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			// TODO: this shouldn't be null but sometimes it is?
			if (praxiPlayer.getKitEditor().getSelectedLadder() == null) {
				player.closeInventory();
				return;
			}

			NamedKit kit = praxiPlayer.getKit(praxiPlayer.getKitEditor().getSelectedLadder(), this.index);

			if (kit == null) {
				kit = new NamedKit("Kit " + (this.index + 1));
				kit.setArmor(praxiPlayer.getKitEditor().getSelectedLadder().getDefaultKit().getArmor());
				kit.setContents(praxiPlayer.getKitEditor().getSelectedLadder().getDefaultKit().getContents());

				praxiPlayer.replaceKit(praxiPlayer.getKitEditor().getSelectedLadder(), this.index, kit);
			}

			praxiPlayer.getKitEditor().setSelectedKit(kit);

			new KitEditorMenu().openMenu(player);
		}

	}

	@AllArgsConstructor
	private class KitDisplayButton extends Button {

		private NamedKit kit;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.BOOK)
					.name(Style.GREEN + Style.BOLD + this.kit.getName())
					.build();
		}

	}

}
