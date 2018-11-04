package me.joeleoli.praxi.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.uuid.UUIDCache;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.player.gui.PlayerSettingsMenu;
import org.bukkit.entity.Player;

public class PlayerCommands {

	@Command(names = "fly", permissionNode = "praxi.donor.fly")
	public static void fly(Player player) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.isInLobby() || praxiPlayer.isInQueue()) {
			player.setAllowFlight(true);
			player.setFlying(true);
			player.updateInventory();
			player.sendMessage(Style.YELLOW + "You are now flying.");
		} else {
			player.sendMessage(Style.RED + "You cannot fly right now.");
		}
	}

	@Command(names = { "settings", "options" })
	public static void settings(Player player) {
		new PlayerSettingsMenu().openMenu(player);
	}

	@Command(names = { "statistics", "stats" }, async = true)
	public static void statistics(Player player, @Parameter(name = "target", defaultValue = "self") String name) {
		if (name.equalsIgnoreCase("self")) {
			name = player.getName();
		}

		final UUID uuid = UUIDCache.getUuid(name);

		if (uuid == null) {
			player.sendMessage(
					Style.RED + "Couldn't find a player with the name " + Style.RESET + name + Style.RED + ".");
			return;
		}

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(uuid);

		if (!praxiPlayer.isLoaded()) {
			praxiPlayer.load();
		}

		if (praxiPlayer.getName() != null) {
			if (praxiPlayer.getName().equalsIgnoreCase(name)) {
				name = praxiPlayer.getName();
			}
		}

		final List<String> messages = new ArrayList<>();

		praxiPlayer.getStatistics().getLadders().forEach((key, value) -> {
			messages.add(Style.YELLOW + key + Style.GRAY + ": " + Style.PINK + value.getElo() + " ELO");
		});

		messages.add(0, Style.GOLD + Style.BOLD + name + "'s Statistics");
		messages.add(0, Style.getBorderLine());
		messages.add(Style.getBorderLine());
		messages.forEach(player::sendMessage);
	}

}
