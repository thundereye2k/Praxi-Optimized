package me.joeleoli.praxi.kit.gui.button;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;


@AllArgsConstructor
public class BackButton extends Button {

	private Menu back;

	@Override
	public ItemStack getButtonItem(Player player) {
		return new ItemBuilder(Material.REDSTONE).name(Style.RED + Style.BOLD + "Back").lore(Arrays
				.asList(Style.RED + "Click here to return to", Style.RED + "the previous menu.")).build();
	}

	@Override
	public void clicked(Player player, int i, ClickType clickType, int hb) {
		Button.playNeutral(player);

		this.back.openMenu(player);
	}

}
