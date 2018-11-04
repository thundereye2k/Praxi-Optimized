package me.joeleoli.praxi.match;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

@Data
public class MatchSnapshot {

	@Getter
	private static Map<UUID, MatchSnapshot> cache = new HashMap<>();

	private MatchPlayer matchPlayer;
	private MatchPlayer switchTo;
	private int health;
	private int hunger;
	private ItemStack[] armor;
	private ItemStack[] contents;
	private Collection<PotionEffect> effects;
	private long created = System.currentTimeMillis();

	public MatchSnapshot(MatchPlayer matchPlayer) {
		this(matchPlayer, null);
	}

	public MatchSnapshot(MatchPlayer matchPlayer, MatchPlayer switchTo) {
		this.matchPlayer = matchPlayer;

		final Player player = this.matchPlayer.toPlayer();

		this.health = player.getHealth() == 0 ? 0 : (int) Math.round(player.getHealth() / 2);
		this.hunger = player.getFoodLevel();
		this.armor = player.getInventory().getArmorContents();
		this.contents = player.getInventory().getContents();
		this.effects = player.getActivePotionEffects();
		this.switchTo = switchTo;
	}

	public static MatchSnapshot getByUuid(UUID uuid) {
		return cache.get(uuid);
	}

	public static MatchSnapshot getByName(String name) {
		for (MatchSnapshot details : cache.values()) {
			if (details.getMatchPlayer().getName().equalsIgnoreCase(name)) {
				return details;
			}
		}

		return null;
	}

	public int getRemainingPotions() {
		int amount = 0;

		for (ItemStack itemStack : this.contents) {
			if (itemStack != null && itemStack.getType() == Material.POTION && itemStack.getDurability() == 16421) {
				amount++;
			}
		}

		return amount;
	}

	public boolean shouldDisplayRemainingPotions() {
		return this.getRemainingPotions() > 0 || this.matchPlayer.getPotionsThrown() > 0 ||
		       this.matchPlayer.getPotionsMissed() > 0;
	}

}
