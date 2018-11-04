package me.joeleoli.praxi.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import me.joeleoli.nucleus.config.ConfigCursor;
import me.joeleoli.nucleus.util.LocationUtil;
import me.joeleoli.praxi.Praxi;
import org.bukkit.Location;

@Getter
public class StandaloneArena extends Arena {

	private List<Arena> duplicates = new ArrayList<>();

	public StandaloneArena(String name, Location location1, Location location2) {
		super(name, ArenaType.STANDALONE, location1, location2);
	}

	@Override
	public void save() {
		ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getArenaConfig(), "arenas." + this.name);

		cursor.set(null);
		cursor.set("type", ArenaType.STANDALONE.name());
		cursor.set("spawn1", LocationUtil.serialize(this.spawn1));
		cursor.set("spawn2", LocationUtil.serialize(this.spawn2));
		cursor.set("cuboid.location1", LocationUtil.serialize(this.getLowerCorner()));
		cursor.set("cuboid.location2", LocationUtil.serialize(this.getUpperCorner()));
		cursor.set("ladders", this.getLadders());

		if (!this.duplicates.isEmpty()) {
			AtomicInteger i = new AtomicInteger();

			this.duplicates.forEach(duplicate -> {
				cursor.setPath("arenas." + this.name + ".duplicates." + i.intValue());
				cursor.set("cuboid.location1", LocationUtil.serialize(duplicate.getLowerCorner()));
				cursor.set("cuboid.location2", LocationUtil.serialize(duplicate.getUpperCorner()));
				cursor.set("spawn1", LocationUtil.serialize(duplicate.getSpawn1()));
				cursor.set("spawn2", LocationUtil.serialize(duplicate.getSpawn2()));

				i.getAndIncrement();
			});
		}

		cursor.save();
	}

	@Override
	public void delete() {
		ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getArenaConfig(), "arenas." + this.name);

		cursor.set(null);
		cursor.save();
	}

}
