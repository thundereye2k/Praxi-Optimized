package me.joeleoli.praxi.match.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.command.CommandHandler;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.menu.buttons.DisplayButton;
import me.joeleoli.nucleus.util.BukkitUtil;
import me.joeleoli.nucleus.util.InventoryUtil;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TimeUtil;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.MatchSnapshot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

@AllArgsConstructor
public class MatchDetailsMenu extends Menu {

	private MatchSnapshot snapshot;

	@Override
	public String getTitle(Player player) {
		return ChatColor.YELLOW + "Snapshot of " + this.snapshot.getMatchPlayer().getName();
	}

	@Override
	public Map<Integer, Button> getButtons(Player player) {
		final Map<Integer, Button> buttons = new HashMap<>();
		final ItemStack[] fixedContents = InventoryUtil.fixInventoryOrder(this.snapshot.getContents());

		for (int i = 0; i < fixedContents.length; i++) {
			final ItemStack itemStack = fixedContents[i];

			if (itemStack == null || itemStack.getType() == Material.AIR) {
				continue;
			}

			buttons.put(i, new DisplayButton(itemStack, true));
		}

		for (int i = 0; i < this.snapshot.getArmor().length; i++) {
			ItemStack itemStack = this.snapshot.getArmor()[i];

			if (itemStack != null && itemStack.getType() != Material.AIR) {
				buttons.put(39 - i, new DisplayButton(itemStack, true));
			}
		}

		int pos = 45;

		buttons.put(pos++, new HealthButton(this.snapshot.getHealth()));
		buttons.put(pos++, new HungerButton(this.snapshot.getHunger()));
		buttons.put(pos++, new EffectsButton(this.snapshot.getEffects()));

		if (this.snapshot.shouldDisplayRemainingPotions()) {
			buttons.put(
					pos++,
					new PotionsButton(this.snapshot.getMatchPlayer().getName(), this.snapshot.getRemainingPotions())
			);
		}

		buttons.put(pos, new StatisticsButton(this.snapshot.getMatchPlayer()));

		if (this.snapshot.getSwitchTo() != null) {
			buttons.put(53, new SwitchInventoryButton(this.snapshot.getSwitchTo()));
		}

		return buttons;
	}

	@Override
	public void onOpen(Player player) {
		player.sendMessage(Style.YELLOW + "You are viewing " + Style.PINK + this.snapshot.getMatchPlayer().getName() +
		                   Style.YELLOW + "'s inventory.");
	}

	@AllArgsConstructor
	private class SwitchInventoryButton extends Button {

		private MatchPlayer switchTo;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.LEVER)
					.name(Style.YELLOW + Style.BOLD + "Opponent's Inventory")
					.lore(Style.YELLOW + "Switch to " + Style.PINK + this.switchTo.getName() + Style.YELLOW +
					      "'s inventory")
					.build();
		}

		@Override
		public void clicked(Player player, int slot, ClickType clickType, int hb) {
			CommandHandler.executeCommand(player, "viewinv " + this.switchTo.getUuid().toString());
		}

	}

	@AllArgsConstructor
	private class HealthButton extends Button {

		private int health;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.MELON)
					.name(Style.YELLOW + Style.BOLD + "Health: " + Style.PINK + this.health + "/10 " + Style.UNICODE_HEART)
					.amount(this.health == 0 ? 1 : this.health)
					.build();
		}

	}

	@AllArgsConstructor
	private class HungerButton extends Button {

		private int hunger;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.COOKED_BEEF)
					.name(Style.YELLOW + Style.BOLD + "Hunger: " + Style.PINK + this.hunger + "/20")
					.amount(this.hunger == 0 ? 1 : this.hunger)
					.build();
		}

	}

	@AllArgsConstructor
	private class EffectsButton extends Button {

		private Collection<PotionEffect> effects;

		@Override
		public ItemStack getButtonItem(Player player) {
			final ItemBuilder builder = new ItemBuilder(Material.POTION)
					.name(Style.YELLOW + Style.BOLD + "Potion Effects");

			if (this.effects.isEmpty()) {
				builder.lore(Style.PINK + "No potion effects");
			} else {
				final List<String> lore = new ArrayList<>();

				this.effects.forEach(effect -> {
					final String name = BukkitUtil.getName(effect.getType()) + " " + (effect.getAmplifier() + 1);
					final String duration = " (" + TimeUtil.millisToTimer((effect.getDuration() / 20) * 1000) + ")";

					lore.add(Style.PINK + name + Style.GRAY + duration);
				});

				builder.lore(lore);
			}

			return builder.build();
		}

	}

	@AllArgsConstructor
	private class PotionsButton extends Button {

		private String name;
		private int potions;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.POTION)
					.durability(16421)
					.amount(this.potions == 0 ? 1 : this.potions)
					.name(Style.YELLOW + Style.BOLD + "Potions")
					.lore(Style.PINK + this.name + Style.YELLOW + " had " + Style.PINK + this.potions + Style.YELLOW +
					      " potion" + (this.potions == 1 ? "" : "s") + " left.")
					.build();
		}

	}

	@AllArgsConstructor
	private class StatisticsButton extends Button {

		private MatchPlayer matchPlayer;

		@Override
		public ItemStack getButtonItem(Player player) {
			return new ItemBuilder(Material.PAPER)
					.name(Style.YELLOW + Style.BOLD + "Statistics")
					.lore(Arrays.asList(
							Style.PINK + "Hits: " + Style.RESET + this.matchPlayer.getHits(),
							Style.PINK + "Longest Combo: " + Style.RESET + this.matchPlayer.getLongestCombo(),
							Style.PINK + "Potions Thrown: " + Style.RESET + this.matchPlayer.getPotionsThrown(),
							Style.PINK + "Potions Missed: " + Style.RESET + this.matchPlayer.getPotionsMissed(),
							Style.PINK + "Potion Accuracy: " + Style.RESET + this.matchPlayer.getPotionAccuracy()
					))
					.build();
		}

	}

}
