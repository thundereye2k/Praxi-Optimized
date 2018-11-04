package me.joeleoli.praxi.queue;

import java.util.UUID;
import lombok.Data;

@Data
public class QueuePlayer {

	private UUID queueUuid;
	private UUID playerUuid;
	private int elo;
	private int range = 25;
	private long start = System.currentTimeMillis();
	private int ticked;

	public QueuePlayer(UUID queueUuid, UUID playerUuid) {
		this.queueUuid = queueUuid;
		this.playerUuid = playerUuid;
	}

	public void tickRange() {
		this.ticked++;

		if (this.ticked >= 20) {
			this.range += 25;
			this.ticked = 0;
		}
	}

	public Queue getQueue() {
		return Queue.getByUuid(this.queueUuid);
	}

	public boolean isInRange(int elo) {
		return elo >= (this.elo - this.range) && elo <= (this.elo + this.range);
	}

	public long getPassed() {
		return System.currentTimeMillis() - this.start;
	}

	public int getMinRange() {
		int min = this.elo - this.range;

		return min < 0 ? 0 : min;
	}

	public int getMaxRange() {
		int max = this.elo + this.range;

		return max > 2500 ? 2500 : max;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof QueuePlayer && ((QueuePlayer) o).getPlayerUuid().equals(this.playerUuid);
	}

}
