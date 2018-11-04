package me.joeleoli.praxi.events;

import lombok.Getter;
import lombok.Setter;
import me.joeleoli.nucleus.config.ConfigCursor;
import me.joeleoli.nucleus.cooldown.Cooldown;
import me.joeleoli.nucleus.util.LocationUtil;
import me.joeleoli.praxi.Praxi;
import org.bukkit.Location;

@Getter
@Setter
public class EventManager {

	private Event activeEvent;
	private Cooldown eventCooldown = new Cooldown(0);
	private Location sumoSpectator, sumoSpawn1, sumoSpawn2;
	private String sumoKbProfile;

	public void setActiveEvent(Event event) {
		if (this.activeEvent != null) {
			this.activeEvent.setEventTask(null);
		}

		if (event == null) {
			this.activeEvent = null;
			return;
		}

		this.activeEvent = event;
		this.activeEvent.handleStart();
		this.activeEvent.handleJoin(event.getHost().toPlayer());
	}

	public void load() {
		ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getMainConfig(), "event");

		if (cursor.exists("sumo.spectator")) {
			this.sumoSpectator = LocationUtil.deserialize(cursor.getString("sumo.spectator"));
		}

		if (cursor.exists("sumo.spawn1")) {
			this.sumoSpawn1 = LocationUtil.deserialize(cursor.getString("sumo.spawn1"));
		}

		if (cursor.exists("sumo.spawn2")) {
			this.sumoSpawn2 = LocationUtil.deserialize(cursor.getString("sumo.spawn2"));
		}

		if (cursor.exists("sumo.kb-profile")) {
			this.sumoKbProfile = cursor.getString("sumo.kb-profile");
		}
	}

	public void save() {
		ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getMainConfig(), "event");

		if (this.sumoSpectator != null) {
			cursor.set("sumo.spectator", LocationUtil.serialize(this.sumoSpectator));
		}

		if (this.sumoSpawn1 != null) {
			cursor.set("sumo.spawn1", LocationUtil.serialize(this.sumoSpawn1));
		}

		if (this.sumoSpawn2 != null) {
			cursor.set("sumo.spawn2", LocationUtil.serialize(this.sumoSpawn2));
		}

		if (this.sumoKbProfile != null) {
			cursor.set("sumo.kb-profile", this.sumoKbProfile);
		}

		cursor.save();
	}

}
