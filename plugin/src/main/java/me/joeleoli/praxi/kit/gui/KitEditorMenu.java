package me.joeleoli.praxi.kit.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.menu.buttons.DisplayButton;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.ItemUtil;
import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TaskUtil;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class KitEditorMenu extends Menu {

	private static final int[] ITEM_POSITIONS = new int[]{
			20, 21, 22, 23, 24, 25, 26, 29, 30, 31, 32, 33, 34, 35, 38, 39, 40, 41, 42, 43, 44, 47, 48, 49, 50, 51, 52,
			53
	};
	private static final int[] BORDER_POSITIONS = new int[]{ 1, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 28, 37, 46 };
	private static final Button BORDER_BUTTON = Button.placeholder(Material.COAL_BLOCK, (byte) 0, " ");

	public KitEditorMenu() {
		this.setUpdateAfterClick(false);
	}

	@Override
	public String getTitle(Player player) {
		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
		return Style.GOLD + "Editing " + Style.AQUA + praxiPlayer.getKitEditor().getSelectedKit().getName();
	}

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
		NamedKit kit = praxiPlayer.getKitEditor().getSelectedKit();
		Map<Integer, Button> buttons = new HashMap<>();

		for (int border : BORDER_POSITIONS) {
			buttons.put(border, BORDER_BUTTON);
		}

		buttons.put(0, new CurrentKitButton());
		buttons.put(2, new SaveButton());
		buttons.put(6, new LoadDefaultKitButton());
		buttons.put(7, new ClearInventoryButton());
		buttons.put(8, new CancelButton());
		buttons.put(18, new ArmorDisplayButton(kit.getArmor()[3]));
		buttons.put(27, new ArmorDisplayButton(kit.getArmor()[2]));
		buttons.put(36, new ArmorDisplayButton(kit.getArmor()[1]));
		buttons.put(45, new ArmorDisplayButton(kit.getArmor()[0]));

		List<ItemStack> items = praxiPlayer.getKitEditor().getSelectedLadder().getKitEditorItems();

		for (int i = 20; i < (praxiPlayer.getKitEditor().getSelectedLadder().getKitEditorItems().size() + 20); i++) {
			buttons.put(ITEM_POSITIONS[i - 20], new InfiniteItemButton(items.get(i - 20)));
		}

		return buttons;
	}

	@Override
	public void onOpen(Player player) {
		if (!this.isClosedByMenu()) {
			PlayerUtil.reset(player);

			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
			praxiPlayer.getKitEditor().setActive(true);

			if (praxiPlayer.getKitEditor().getSelectedKit() != null) {
				player.getInventory().setContents(praxiPlayer.getKitEditor().getSelectedKit().getContents());
			}

			player.updateInventory();
		}
	}

	@Override
	public void onClose(Player player) {
		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
		praxiPlayer.getKitEditor().setActive(false);

		if (!praxiPlayer.isInMatch()) {
			TaskUtil.runLater(praxiPlayer::loadHotbar, 1L);
		}
	}

	@AllArgsConstructor
	private class ArmorDisplayButton extends Button {

		private ItemStack itemStack;

		@Override
		public ItemStack getButtonItem(Player player) {
			if (this.itemStack == null || this.itemStack.getType() == Material.AIR) {
				return new ItemStack(Material.AIR);
			}

			return new ItemBuilder(this.itemStack.clone())
					.name(Style.AQUA + ItemUtil.getName(this.itemStack))
					.lore(Arrays.asList(
							"",
							Style.YELLOW + "This is automatically equipped."
					))
					.build();
		}

	}

	@AllArgsConstructor
	private class CurrentKitButton extends Button {

		@Override
		public ItemStack getButtonItem(Player player) {
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			return new ItemBuilder(Material.NAME_TAG)
					.name(Style.GREEN + Style.BOLD + "Editing: " + Style.AQUA +
					      praxiPlayer.getKitEditor().getSelectedKit().getName())
					.build();
		}

	}

	@AllArgsConstructor
	private class ClearInventoryButton extends Button {

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.STAINED_CLAY)
					.durability(7)
					.name(Style.YELLOW + Style.BOLD + "Clear Inventory")
					.lore(Arrays.asList(
							"",
							Style.YELLOW + "This will clear your inventory",
							Style.YELLOW + "so you can start over."
					))
					.build();
		}

		@Override
		public void clicked(Player player, int i, ClickType clickType, int hb) {
			Button.playNeutral(player);
			player.getInventory().setContents(new ItemStack[36]);
			player.updateInventory();
		}

		@Override
		public boolean shouldUpdate(Player player, int i, ClickType clickType) {
			return true;
		}

	}

	@AllArgsConstructor
	private class LoadDefaultKitButton extends Button {

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.STAINED_CLAY)
					.durability(7)
					.name(Style.YELLOW + Style.BOLD + "Load default kit")
					.lore(Arrays.asList(
							"",
							Style.YELLOW + "Click this to load the default kit",
							Style.YELLOW + "into the kit editing menu."
					))
					.build();
		}

		@Override
		public void clicked(Player player, int i, ClickType clickType, int hb) {
			Button.playNeutral(player);

			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			player.getInventory()
			      .setContents(praxiPlayer.getKitEditor().getSelectedLadder().getDefaultKit().getContents());
			player.updateInventory();
		}

		@Override
		public boolean shouldUpdate(Player player, int i, ClickType clickType) {
			return true;
		}

	}

	@AllArgsConstructor
	private class SaveButton extends Button {

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.STAINED_CLAY)
					.durability(5)
					.name(Style.GREEN + Style.BOLD + "Save")
					.lore(Arrays.asList(
							"",
							Style.YELLOW + "Click this to save your kit."
					))
					.build();
		}

		@Override
		public void clicked(Player player, int i, ClickType clickType, int hb) {
			Button.playNeutral(player);
			player.closeInventory();

			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer.getKitEditor().getSelectedKit() != null) {
				praxiPlayer.getKitEditor().getSelectedKit().setContents(player.getInventory().getContents());
			}

			praxiPlayer.loadHotbar();

			new KitManagementMenu(praxiPlayer.getKitEditor().getSelectedLadder()).openMenu(player);
		}

	}

	@AllArgsConstructor
	private class CancelButton extends Button {

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.STAINED_CLAY)
					.durability(14)
					.name(Style.RED + Style.BOLD + "Cancel")
					.lore(Arrays.asList(
							"",
							Style.YELLOW + "Click this to abort editing your kit,",
							Style.YELLOW + "and return to the kit menu."
					))
					.build();
		}

		@Override
		public void clicked(Player player, int i, ClickType clickType, int hb) {
			Button.playNeutral(player);

			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer.getKitEditor().getSelectedLadder() != null) {
				new KitManagementMenu(praxiPlayer.getKitEditor().getSelectedLadder()).openMenu(player);
			}
		}

	}

	private class InfiniteItemButton extends DisplayButton {

		InfiniteItemButton(ItemStack itemStack) {
			super(itemStack, false);
		}

		@Override
		public void clicked(Player player, int i, ClickType clickType, int hb) {
			final Inventory inventory = player.getOpenInventory().getTopInventory();
			final ItemStack itemStack = inventory.getItem(i);

			inventory.setItem(i, itemStack);

			player.setItemOnCursor(itemStack);
			player.updateInventory();
		}

	}

}
