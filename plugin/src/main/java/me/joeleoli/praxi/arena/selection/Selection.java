package me.joeleoli.praxi.arena.selection;

import java.util.Arrays;
import lombok.Data;
import lombok.NonNull;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.cuboid.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

@Data
public class Selection {

	public static final ItemStack SELECTION_WAND;
	private static final String SELECTION_METADATA_KEY = "CLAIM_SELECTION";

	static {
		ItemStack itemStack = new ItemStack(Material.GOLD_AXE);
		ItemMeta itemMeta = itemStack.getItemMeta();

		itemMeta.setDisplayName(Style.GOLD + Style.BOLD + "Selection Wand");
		itemMeta.setLore(Arrays.asList(
				Style.YELLOW + "Left-click to set position 1.",
				Style.YELLOW + "Right-click to set position 2."
		));
		itemStack.setItemMeta(itemMeta);

		SELECTION_WAND = itemStack;
	}

	@NonNull
	private Location point1;
	@NonNull
	private Location point2;

	/**
	 * Private, so that we can create a new instance in the Selection#createOrGetSelection method.
	 */
	private Selection() {
	}

	/**
	 * Selections are stored in the player's metadata. This method removes the need to active Bukkit Metadata API calls
	 * all over the place.
	 * <p>
	 * This method can be modified structurally as needed, the plugin only accepts Selection objects via this method.
	 *
	 * @param player the player for whom to grab the Selection object for
	 *
	 * @return selection object, either new or created
	 */
	public static Selection createOrGetSelection(Player player) {
		if (player.hasMetadata(SELECTION_METADATA_KEY)) {
			return (Selection) player.getMetadata(SELECTION_METADATA_KEY).get(0).value();
		}

		Selection selection = new Selection();

		player.setMetadata(SELECTION_METADATA_KEY, new FixedMetadataValue(Praxi.getInstance(), selection));

		return selection;
	}

	/**
	 * @return the cuboid
	 */
	public Cuboid getCuboid() {
		return new Cuboid(point1, point2);
	}

	/**
	 * @return if the Selection can form a full cuboid object
	 */
	public boolean isFullObject() {
		return point1 != null && point2 != null;
	}

	/**
	 * Resets both locations in the Selection
	 */
	public void clear() {
		point1 = null;
		point2 = null;
	}

}
