package me.joeleoli.praxi.ladder;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import me.joeleoli.nucleus.config.ConfigCursor;
import me.joeleoli.nucleus.util.InventoryUtil;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.kit.Kit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Data
public class Ladder {

	@Getter
	private static List<Ladder> ladders = new ArrayList<>();

	private String name;
	private String displayName;
	private ItemStack displayIcon;
	private Kit defaultKit = new Kit();
	private List<ItemStack> kitEditorItems = new ArrayList<>();
	private boolean enabled, build, sumo, parkour, spleef, regeneration, allowPotionFill;
	private int hitDelay = 20;
	private String kbProfile;

	public Ladder(String name) {
		this.name = name;
		this.displayName = ChatColor.AQUA + this.name;
		this.displayIcon = new ItemStack(Material.DIAMOND_SWORD);

		ladders.add(this);
	}

	public static Ladder getByName(String name) {
		for (Ladder ladder : ladders) {
			if (ladder.getName().equalsIgnoreCase(name)) {
				return ladder;
			}
		}

		return null;
	}

	public ItemStack getDisplayIcon() {
		return this.displayIcon.clone();
	}

	public void save() {
		ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getLadderConfig(), "ladders." + this.name);

		cursor.set("display-name", this.displayName);
		cursor.set("display-icon.material", this.displayIcon.getType().name());
		cursor.set("display-icon.durability", this.displayIcon.getDurability());
		cursor.set("display-icon.amount", this.displayIcon.getAmount());
		cursor.set("enabled", this.enabled);
		cursor.set("build", this.build);
		cursor.set("sumo", this.sumo);
		cursor.set("spleef", this.spleef);
		cursor.set("parkour", this.parkour);
		cursor.set("regeneration", this.regeneration);
		cursor.set("hit-delay", this.hitDelay);

		if (this.displayIcon.hasItemMeta()) {
			final ItemMeta itemMeta = this.displayIcon.getItemMeta();

			if (itemMeta.hasDisplayName()) {
				cursor.set("display-icon.name", itemMeta.getDisplayName());
			}

			if (itemMeta.hasLore()) {
				cursor.set("display-icon.lore", itemMeta.getLore());
			}
		}

		cursor.set("default-kit.armor", InventoryUtil.serializeInventory(this.defaultKit.getArmor()));
		cursor.set("default-kit.contents", InventoryUtil.serializeInventory(this.defaultKit.getContents()));

		cursor.save();
	}

}
