package me.joeleoli.praxi.events.task;

import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.events.Event;
import me.joeleoli.praxi.events.EventState;
import me.joeleoli.praxi.events.EventTask;
import me.joeleoli.praxi.events.impl.SumoEvent;
import org.bukkit.entity.Player;

public class EventRoundStartTask extends EventTask {

	public EventRoundStartTask(Event event) {
		super(event, EventState.ROUND_STARTING);
	}

	@Override
	public void onRun() {
		if (this.getTicks() >= 3) {
			this.getEvent().setEventTask(null);
			this.getEvent().setState(EventState.ROUND_FIGHTING);

			final Player playerA = this.getEvent().getRoundPlayerA().toPlayer();
			final Player playerB = this.getEvent().getRoundPlayerB().toPlayer();

			PlayerUtil.allowMovement(playerA);
			PlayerUtil.allowMovement(playerB);

			((SumoEvent) this.getEvent()).setRoundStart(System.currentTimeMillis());
		} else {
			final int seconds = this.getSeconds();

			this.getEvent().broadcastMessage(
					Style.YELLOW + "The round will start in " + Style.PINK + (seconds) + " second" +
					(seconds == 1 ? "" : "s") + Style.YELLOW + "...");
		}
	}

}
