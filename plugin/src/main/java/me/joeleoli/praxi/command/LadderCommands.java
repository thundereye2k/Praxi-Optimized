package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.ragespigot.RageSpigot;
import me.joeleoli.ragespigot.knockback.KnockbackProfile;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LadderCommands {

	@Command(names = "ladder enable", permissionNode = "praxi.ladder")
	public static void enable(CommandSender sender, @Parameter(name = "ladder") Ladder ladder) {
		ladder.setEnabled(true);
		sender.sendMessage(Style.GREEN + "You enabled the " + ladder.getDisplayName() + Style.GREEN + " ladder.");
	}

	@Command(names = "ladder disable", permissionNode = "praxi.ladder")
	public static void disable(CommandSender sender, @Parameter(name = "ladder") Ladder ladder) {
		ladder.setEnabled(false);
		sender.sendMessage(Style.GREEN + "You disabled the " + ladder.getDisplayName() + Style.GREEN + " ladder.");
	}

	@Command(names = "ladder sethitdelay", permissionNode = "praxi.ladder")
	public static void setHitDelay(CommandSender sender, @Parameter(name = "ladder") Ladder ladder,
			@Parameter(name = "hitdelay") int hitDelay) {
		if (hitDelay < 0 || hitDelay > 20) {
			sender.sendMessage(Style.RED + "The hit delay must be in the range of 0-20.");
			return;
		}

		ladder.setHitDelay(hitDelay);
		sender.sendMessage(Style.GREEN + "You set the hit delay of " + ladder.getDisplayName() + Style.GREEN + " to: " +
		                   Style.RESET + ladder.getHitDelay());
	}

	@Command(names = "ladder list", permissionNode = "praxi.ladder")
	public static void list(CommandSender sender) {
		sender.sendMessage(Style.GOLD + Style.BOLD + "Ladders:");

		Ladder.getLadders().forEach(ladder -> {
			sender.sendMessage(Style.GRAY + " - " + ladder.getDisplayName());
		});
	}

	@Command(names = "ladder create", permissionNode = "praxi.ladder")
	public static void create(Player player, @Parameter(name = "name") String name) {
		Ladder ladder = Ladder.getByName(name);

		if (ladder != null) {
			player.sendMessage(Style.RED + "A ladder with that name already exists.");
			return;
		}

		ladder = new Ladder(name);
		ladder.save();

		player.sendMessage(
				Style.GREEN + "Created a new ladder named " + Style.AQUA + ladder.getName() + Style.GREEN + ".");
	}

	@Command(names = "ladder setkit", permissionNode = "praxi.ladder")
	public static void setKit(Player player, @Parameter(name = "ladder") Ladder ladder) {
		ladder.getDefaultKit().setArmor(player.getInventory().getArmorContents());
		ladder.getDefaultKit().setContents(player.getInventory().getContents());
		ladder.save();

		player.sendMessage(Style.GREEN + "Updated " + Style.AQUA + ladder.getName() + Style.GREEN + "'s default kit.");
	}

	@Command(names = "ladder loadkit", permissionNode = "praxi.ladder")
	public static void loadKit(Player player, @Parameter(name = "ladder") Ladder ladder) {
		player.getInventory().setArmorContents(ladder.getDefaultKit().getArmor());
		player.getInventory().setContents(ladder.getDefaultKit().getContents());
		player.updateInventory();
		player.sendMessage(Style.GREEN + "Loaded " + Style.AQUA + ladder.getName() + Style.GREEN + "'s default kit.");
	}

	@Command(names = "ladder setdisplayname", permissionNode = "praxi.ladder")
	public static void setDisplayName(CommandSender sender, @Parameter(name = "ladder") Ladder ladder,
			@Parameter(name = "displayName") String displayName) {
		ladder.setDisplayName(Style.translate(displayName));
		ladder.save();

		sender.sendMessage(
				Style.GREEN + "You set " + Style.AQUA + ladder.getName() + "'s display name " + Style.GREEN + "to: " +
				Style.AQUA + Style.translate(displayName));
	}

	@Command(names = "ladder setkbprofile", permissionNode = "praxi.ladder")
	public static void setKnockbackProfile(CommandSender sender, @Parameter(name = "ladder") Ladder ladder,
			@Parameter(name = "profile") String profileName) {
		final KnockbackProfile profile = RageSpigot.INSTANCE.getConfig().getKbProfileByName(profileName);

		if (profile == null) {
			sender.sendMessage(Style.RED + "A knockback profile with that name could not be found.");
			return;
		}

		ladder.setKbProfile(profileName);
		ladder.save();

		sender.sendMessage(
				Style.GREEN + "You set the kb-profile for " + ladder.getDisplayName() + Style.GREEN + " ladder.");
	}

}
