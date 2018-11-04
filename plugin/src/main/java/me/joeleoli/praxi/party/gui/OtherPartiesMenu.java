package me.joeleoli.praxi.party.gui;

import java.util.HashMap;
import java.util.Map;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.pagination.PaginatedMenu;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.party.Party;
import me.joeleoli.praxi.party.gui.button.PartyDisplayButton;
import org.bukkit.entity.Player;

public class OtherPartiesMenu extends PaginatedMenu {

	@Override
	public String getPrePaginatedTitle(Player player) {
		return Style.GOLD + "Other Parties";
	}

	@Override
	public Map<Integer, Button> getAllPagesButtons(Player player) {
		Map<Integer, Button> buttons = new HashMap<>();

		Party.getParties().forEach(party -> {
			buttons.put(buttons.size(), new PartyDisplayButton(party));
		});

		return buttons;
	}

}
