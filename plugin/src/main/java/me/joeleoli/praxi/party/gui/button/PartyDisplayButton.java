package me.joeleoli.praxi.party.gui.button;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.team.TeamPlayer;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.party.Party;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class PartyDisplayButton extends Button {

	private Party party;

	@Override
	public ItemStack getButtonItem(Player player) {
		final List<String> lore = new ArrayList<>();
		int added = 0;

		for (TeamPlayer teamPlayer : this.party.getTeamPlayers()) {
			if (added >= 10) {
				break;
			}

			lore.add(Style.GRAY + " - " + Style.RESET + teamPlayer.getDisplayName());

			added++;
		}

		if (this.party.getTeamPlayers().size() != added) {
			lore.add(Style.GRAY + " and " + (this.party.getTeamPlayers().size() - added) + " others...");
		}

		return new ItemBuilder(Material.SKULL_ITEM)
				.amount(this.party.getTeamPlayers().size())
				.durability(3)
				.name(Style.GOLD + this.party.getLeader().getName() + "s Party")
				.lore(lore)
				.build();
	}

}
