package me.joeleoli.praxi.kit;

import lombok.Data;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Data
public class Kit {

	public static final ItemStack DEFAULT_KIT = new ItemBuilder(Material.BOOK).name(Style.GOLD + "Default Kit").build();

	private ItemStack[] armor;
	private ItemStack[] contents;

	public Kit() {
		this.armor = new ItemStack[4];
		this.contents = new ItemStack[36];
	}

	public Kit(ItemStack[] armor, ItemStack[] contents) {
		this.armor = armor;
		this.contents = contents;
	}

}
