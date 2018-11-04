package me.joeleoli.praxi.command;

import java.util.UUID;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.CommandHelp;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TaskUtil;
import me.joeleoli.nucleus.uuid.UUIDCache;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ManagementCommands {

	private static final CommandHelp[] HELP = new CommandHelp[]{
			new CommandHelp("/praxi reload", "Reload the config"),
	};

	@Command(names = { "praxi", "praxi help" }, permissionNode = "praxi.admin")
	public static void help(Player player) {
		for (CommandHelp help : HELP) {
			player.sendMessage(
					Style.YELLOW + help.getSyntax() + Style.GRAY + " - " + Style.PINK + help.getDescription());
		}
	}

	@Command(names = "resetelo", permissionNode = "prax.admin.resetelo")
	public static void resetElo(CommandSender sender, @Parameter(name = "target") String targetName) {
		UUID uuid;

		try {
			uuid = UUID.fromString(targetName);
		} catch (Exception e) {
			uuid = UUIDCache.getUuid(targetName);
		}

		if (uuid == null) {
			sender.sendMessage(
					Style.RED + "Couldn't find a player with the name " + Style.RESET + targetName + Style.RED +
					". Have they joined the network?");
			return;
		}

		PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(uuid);

		if (praxiPlayer.isLoaded()) {
			praxiPlayer.getStatistics().getLadders().values().forEach(stats -> {
				stats.setElo(1000);
			});

			praxiPlayer.save();
		} else {
			TaskUtil.runAsync(() -> {
				praxiPlayer.load();

				praxiPlayer.getStatistics().getLadders().values().forEach(stats -> {
					stats.setElo(1000);
				});

				praxiPlayer.save();
			});
		}

		sender.sendMessage(Style.GREEN + "You reset " + targetName + "'s elo.");
	}

}
