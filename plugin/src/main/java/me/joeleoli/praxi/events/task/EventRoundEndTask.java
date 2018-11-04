package me.joeleoli.praxi.events.task;

import me.joeleoli.praxi.events.Event;
import me.joeleoli.praxi.events.EventState;
import me.joeleoli.praxi.events.EventTask;

public class EventRoundEndTask extends EventTask {

	public EventRoundEndTask(Event event) {
		super(event, EventState.ROUND_ENDING);
	}

	@Override
	public void onRun() {
		if (this.getTicks() >= 3) {
			if (this.getEvent().canEnd()) {
				this.getEvent().end();
			} else {
				this.getEvent().onRound();
			}
		}
	}

}
