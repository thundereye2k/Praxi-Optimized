package me.joeleoli.praxi.command;

import java.io.File;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.CommandHelp;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TaskUtil;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.arena.SharedArena;
import me.joeleoli.praxi.arena.StandaloneArena;
import me.joeleoli.praxi.arena.generator.ArenaGenerator;
import me.joeleoli.praxi.arena.generator.Schematic;
import me.joeleoli.praxi.arena.selection.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommands {

	private static final CommandHelp[] HELP = new CommandHelp[]{
			new CommandHelp("/arena list", "List all arenas"),
			new CommandHelp("/arena create <name> <type>", "Create an arena"),
			new CommandHelp("/arena delete <name>", "Delete an arena"),
			new CommandHelp("/arena setspawn <1:2> <name>", "Set a spawn point"),
	};

	@Command(names = { "arena", "arena help" }, permissionNode = "praxi.arena")
	public static void help(Player player) {
		for (CommandHelp help : HELP) {
			player.sendMessage(
					Style.YELLOW + help.getSyntax() + Style.GRAY + " - " + Style.PINK + help.getDescription());
		}
	}

	@Command(names = "arena wand", permissionNode = "praxi.arena")
	public static void wand(Player player) {
		player.getInventory().addItem(Selection.SELECTION_WAND);
		player.sendMessage(Style.YELLOW + "You have been given the selection wand.");
	}

	@Command(names = "arena save", permissionNode = "praxi.arena")
	public static void save(CommandSender sender) {
		Arena.getArenas().forEach(Arena::save);
		sender.sendMessage(Style.GREEN + "Saved all arenas.");
	}

	@Command(names = "arena list", permissionNode = "praxi.arena")
	public static void list(Player player) {
		player.sendMessage(Style.GOLD + "Arenas:");

		if (Arena.getArenas().isEmpty()) {
			player.sendMessage(Style.GRAY + "There are no arenas.");
			return;
		}

		for (Arena arena : Arena.getArenas()) {
			if (arena.getType() != ArenaType.DUPLICATE) {
				player.sendMessage(Style.GRAY + " - " + (arena.isSetup() ? Style.GREEN : Style.RED) + arena.getName() +
				                   Style.GRAY + " (" + arena.getType().name() + ")");
			}
		}
	}

	@Command(names = "arena create", permissionNode = "praxi.arena")
	public static void create(Player player, @Parameter(name = "name") String name,
			@Parameter(name = "type") ArenaType type) {
		Arena arena = Arena.getByName(name);

		if (arena != null) {
			player.sendMessage(Style.RED + "An arena with that name already exists.");
			return;
		}

		Selection selection = Selection.createOrGetSelection(player);

		if (!selection.isFullObject()) {
			player.sendMessage(Style.RED + "You must have a full selection to create an arena.");
			return;
		}

		if (type == ArenaType.STANDALONE) {
			arena = new StandaloneArena(name, selection.getPoint1(), selection.getPoint2());
		} else {
			arena = new SharedArena(name, selection.getPoint1(), selection.getPoint2());
		}

		arena.save();

		Arena.getArenas().add(arena);

		player.sendMessage(Style.GREEN + "Arena `" + arena.getName() + "` has been created.");
	}

	@Command(names = "arena delete", permissionNode = "praxi.arena")
	public static void delete(Player player, @Parameter(name = "arena") Arena arena) {
		arena.delete();

		Arena.getArenas().remove(arena);

		if (arena instanceof StandaloneArena) {
			Arena.getArenas().removeAll(((StandaloneArena) arena).getDuplicates());
		}

		player.sendMessage(Style.GREEN + "Arena `" + arena.getName() + "` has been deleted.");
	}

	@Command(names = "arena setspawn", permissionNode = "praxi.arena")
	public static void setSpawn(Player player, @Parameter(name = "loc") int loc,
			@Parameter(name = "arena") Arena arena) {
		if (loc == 1) {
			arena.setSpawn1(player.getLocation());
		} else if (loc == 2) {
			arena.setSpawn2(player.getLocation());
		} else {
			player.sendMessage(Style.RED + "Choose position `1` or position `2`.");
			return;
		}

		arena.save();

		player.sendMessage(Style.GREEN + "You set the spawn position " + loc + (loc == 1 ? "st" : "nd") + " for `" +
		                   arena.getName() + "`.");
	}

	@Command(names = "arena generate", permissionNode = "praxi.arena.generate", async = true)
	public static void generate(Player player) {
		File schematicsFolder = new File(Praxi.getInstance().getDataFolder().getPath() + File.separator + "schematics");

		if (!schematicsFolder.exists()) {
			player.sendMessage(Style.RED + "The schematics folder does not exist.");
			return;
		}

		for (File file : schematicsFolder.listFiles()) {
			if (!file.isDirectory()) {
				if (file.getName().contains(".schematic")) {
					final boolean duplicate = file.getName().endsWith("_duplicate.schematic");

					final String name = file.getName()
					                        .replace(".schematic", "")
					                        .replace("_duplicate", "");

					final Arena parent = Arena.getByName(name);

					if (parent != null) {
						if (!(parent instanceof StandaloneArena)) {
							continue;
						}
					}

					TaskUtil.run(() -> {
						try {
							new ArenaGenerator(name, Bukkit.getWorlds().get(0), new Schematic(file),
									duplicate ? (parent != null ? ArenaType.DUPLICATE : ArenaType.STANDALONE)
											: ArenaType.SHARED
							).generate(file, (StandaloneArena) parent);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
			}
		}

		player.sendMessage(Style.GREEN + "Generating arenas... See console for details.");
	}

	@Command(names = "arena tp", permissionNode = "praxi.arena")
	public static void teleport(Player player, @Parameter(name = "arena") Arena arena) {
		if (arena.getSpawn1() != null) {
			player.teleport(arena.getSpawn1());
		} else if (arena.getSpawn2() != null) {
			player.teleport(arena.getSpawn2());
		} else {
			player.teleport(arena.getUpperCorner());
		}

		player.sendMessage(Style.GREEN + "You teleported to " + Style.AQUA + arena.getName() + Style.GREEN + ".");
	}

	@Command(names = "arena genhelper", permissionNode = "praxi.arena.genhelp")
	public static void generatorHelper(Player player) {
		final Block origin = player.getLocation().getBlock();
		final Block up = origin.getRelative(BlockFace.UP);

		origin.setType(Material.SPONGE);
		up.setType(Material.SIGN_POST);

		if (up.getState() instanceof Sign) {
			final Sign sign = (Sign) up.getState();

			sign.setLine(0, ((int) player.getLocation().getPitch()) + "");
			sign.setLine(1, ((int) player.getLocation().getYaw()) + "");
			sign.update();

			player.sendMessage(Style.GREEN + "Generator helper placed.");
		}
	}

}
