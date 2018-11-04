package me.joeleoli.praxi.command;

import java.util.UUID;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.match.MatchSnapshot;
import me.joeleoli.praxi.match.gui.MatchDetailsMenu;
import org.bukkit.entity.Player;

public class MatchCommands {

	@Command(names = { "viewinventory", "viewinv" })
	public static void viewInventory(Player player, @Parameter(name = "id") String id) {
		MatchSnapshot cachedInventory;

		try {
			cachedInventory = MatchSnapshot.getByUuid(UUID.fromString(id));
		} catch (Exception e) {
			cachedInventory = MatchSnapshot.getByName(id);
		}

		if (cachedInventory == null) {
			player.sendMessage(Style.RED + "Couldn't find an inventory for that ID.");
			return;
		}

		new MatchDetailsMenu(cachedInventory).openMenu(player);
	}

}
