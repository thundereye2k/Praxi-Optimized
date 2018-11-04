package me.joeleoli.praxi.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import me.joeleoli.praxi.cuboid.Cuboid;
import me.joeleoli.praxi.ladder.Ladder;
import org.bukkit.Location;

@Getter
@Setter
public class Arena extends Cuboid {

	@Getter
	private static List<Arena> arenas = new ArrayList<>();

	protected String name;
	protected Location spawn1;
	protected Location spawn2;
	protected boolean active;
	private ArenaType type;
	private List<String> ladders = new ArrayList<>();

	public Arena(String name, ArenaType type, Location location1, Location location2) {
		super(location1, location2);

		this.name = name;
		this.type = type;
	}

	public static Arena getByName(String name) {
		for (Arena arena : arenas) {
			if (arena.getType() != ArenaType.DUPLICATE && arena.getName() != null &&
			    arena.getName().equalsIgnoreCase(name)) {
				return arena;
			}
		}

		return null;
	}

	public static Arena getRandom(Ladder ladder) {
		final List<Arena> _arenas = arenas.stream().filter(arena -> arena.isSetup() &&
		                                                            arena.getLadders().contains(ladder.getName()) &&
		                                                            ((ladder.isBuild() && !arena.isActive() &&
		                                                              (arena.getType() == ArenaType.STANDALONE ||
		                                                               arena.getType() == ArenaType.DUPLICATE)) ||
		                                                             (!ladder.isBuild() &&
		                                                              arena.getType() == ArenaType.SHARED)))
		                                  .collect(Collectors.toList());

		if (_arenas.isEmpty()) {
			return null;
		}

		return _arenas.get(ThreadLocalRandom.current().nextInt(_arenas.size()));
	}

	public int getMaxBuildHeight() {
		int highest = (int) (this.spawn1.getY() >= this.spawn2.getY() ? this.spawn1.getY() : this.spawn2.getY());

		return highest + 5;
	}

	public Location getSpawn1() {
		if (this.spawn1 == null) {
			return null;
		}

		return this.spawn1.clone();
	}

	public Location getSpawn2() {
		if (this.spawn2 == null) {
			return null;
		}

		return this.spawn2.clone();
	}

	public void setActive(boolean active) {
		if (this.type != ArenaType.SHARED) {
			this.active = active;
		}
	}

	public void save() {
	}

	public void delete() {
	}

	public boolean isSetup() {
		return this.spawn1 != null && this.spawn2 != null;
	}

}
