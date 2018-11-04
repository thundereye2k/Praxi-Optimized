package me.joeleoli.praxi.arena.generator;

import com.boydti.fawe.util.TaskManager;
import java.io.File;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.util.TaskUtil;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.arena.SharedArena;
import me.joeleoli.praxi.arena.StandaloneArena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

@AllArgsConstructor
public class ArenaGenerator {

	private String name;
	private World world;
	private Schematic schematic;
	private ArenaType type;

	public void generate(File file, StandaloneArena parentArena) {
		if (Arena.getByName(this.name) != null) {
			this.name = name + Nucleus.RANDOM.nextInt(1000);
		}

		this.log("Generating " + this.type.name() + " " + this.name + " arena...");

		int range = 500;
		int attempts = 0;

		int preciseX = Nucleus.RANDOM.nextInt(range);
		int preciseZ = Nucleus.RANDOM.nextInt(range);

		if (Nucleus.RANDOM.nextBoolean()) {
			preciseX = -preciseX;
		}

		if (Nucleus.RANDOM.nextBoolean()) {
			preciseZ = -preciseZ;
		}

		top:
		while (true) {
			attempts++;

			if (attempts >= 5) {
				preciseX = Nucleus.RANDOM.nextInt(range);
				preciseZ = Nucleus.RANDOM.nextInt(range);

				if (Nucleus.RANDOM.nextBoolean()) {
					preciseX = -preciseX;
				}

				if (Nucleus.RANDOM.nextBoolean()) {
					preciseZ = -preciseZ;
				}

				range += 500;

				this.log("Increased range to: " + range);
			}

			if (this.world.getBlockAt(preciseX, 72, preciseZ) == null) {
				continue;
			}

			final int minX = preciseX - this.schematic.getClipBoard().getWidth() - 200;
			final int maxX = preciseX + this.schematic.getClipBoard().getWidth() + 200;
			final int minZ = preciseZ - this.schematic.getClipBoard().getLength() - 200;
			final int maxZ = preciseZ + this.schematic.getClipBoard().getLength() + 200;
			final int minY = 72;
			final int maxY = 72 + this.schematic.getClipBoard().getHeight();

			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					for (int y = minY; y < maxY; y++) {
						if (this.world.getBlockAt(x, y, z).getType() != Material.AIR) {
							continue top;
						}
					}
				}
			}

			final Location minCorner = new Location(this.world, minX, minY, minZ);
			final Location maxCorner = new Location(this.world, maxX, maxY, maxZ);

			final int finalPreciseX = preciseX;
			final int finalPreciseZ = preciseZ;

			TaskManager.IMP.async(() -> {
				try {
					new Schematic(file).pasteSchematic(this.world, finalPreciseX, 76, finalPreciseZ);
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}

				final Arena arena;

				if (this.type == ArenaType.STANDALONE) {
					arena = new StandaloneArena(this.name, minCorner, maxCorner);

					this.type = ArenaType.DUPLICATE;

					for (int i = 0; i < 5; i++) {
						TaskUtil.run(() -> this.generate(file, (StandaloneArena) arena));
					}
				} else if (this.type == ArenaType.DUPLICATE) {
					arena = new Arena(this.name, ArenaType.DUPLICATE, minCorner, maxCorner);

					parentArena.getDuplicates().add(arena);
				} else {
					arena = new SharedArena(this.name, minCorner, maxCorner);
				}

				helper:
				for (int x = minX; x < maxX; x++) {
					for (int z = minZ; z < maxZ; z++) {
						for (int y = minY; y < maxY; y++) {
							if (this.world.getBlockAt(x, y, z).getType() == Material.SPONGE) {
								final Block origin = this.world.getBlockAt(x, y, z);
								final Block up = origin.getRelative(BlockFace.UP, 1);

								if (up.getState() instanceof Sign) {
									final Sign sign = (Sign) up.getState();
									final float pitch = Float.valueOf(sign.getLine(0));
									final float yaw = Float.valueOf(sign.getLine(1));
									final Location loc =
											new Location(origin.getWorld(), origin.getX(), origin.getY(), origin.getZ(),
													yaw, pitch
											);

									TaskUtil.run(() -> {
										up.setType(Material.AIR);
										origin.setType(origin.getRelative(BlockFace.NORTH).getType());
									});

									if (arena.getSpawn1() == null) {
										arena.setSpawn1(loc);
									} else if (arena.getSpawn2() == null) {
										arena.setSpawn2(loc);
										break helper;
									}
								}
							}
						}
					}
				}

				arena.save();

				Arena.getArenas().add(arena);
			});

			this.log(String.format("Pasted schematic at %1$s, %2$s, %3$s", preciseX, 76, preciseZ));

			return;
		}
	}

	private void log(String message) {
		Nucleus.getInstance().getLogger().info("[ArenaGen] " + message);
	}

}
