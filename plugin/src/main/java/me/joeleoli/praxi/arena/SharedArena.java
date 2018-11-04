package me.joeleoli.praxi.arena;

import lombok.Getter;
import lombok.Setter;
import me.joeleoli.nucleus.config.ConfigCursor;
import me.joeleoli.nucleus.util.LocationUtil;
import me.joeleoli.praxi.Praxi;
import org.bukkit.Location;

@Getter
@Setter
public class SharedArena extends Arena {

	public SharedArena(String name, Location location1, Location location2) {
		super(name, ArenaType.SHARED, location1, location2);
	}

	@Override
	public void save() {
		ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getArenaConfig(), "arenas." + this.name);

		cursor.set(null);
		cursor.set("type", ArenaType.SHARED.name());

		if (this.spawn1 != null) {
			cursor.set("spawn1", LocationUtil.serialize(this.spawn1));
		}

		if (this.spawn2 != null) {
			cursor.set("spawn2", LocationUtil.serialize(this.spawn2));
		}

		cursor.set("cuboid.location1", LocationUtil.serialize(this.getLowerCorner()));
		cursor.set("cuboid.location2", LocationUtil.serialize(this.getUpperCorner()));
		cursor.save();
	}

	@Override
	public void delete() {
		ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getArenaConfig(), "arenas." + this.name);

		cursor.set(null);
		cursor.save();
	}

}
