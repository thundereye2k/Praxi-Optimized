package me.joeleoli.praxi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.queue.Queue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class PraxiProvider extends JavaPlugin implements PraxiAPI, Runnable {

	private int inQueues, inFights;
	private Map<UUID, AtomicInteger> queueFightCounts = new HashMap<>();

	@Override
	public void run() {
		int inQueues = 0;
		int inFights = 0;

		for (Player player : Bukkit.getOnlinePlayers()) {
			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer != null) {
				if (praxiPlayer.getState() == PlayerState.IN_QUEUE) {
					inQueues++;
				} else if (praxiPlayer.getState() == PlayerState.IN_MATCH) {
					inFights++;
				}
			}
		}

		this.inQueues = inQueues;
		this.inFights = inFights;

		Map<UUID, AtomicInteger> queueFightCounts = new HashMap<>();

		for (Match match : Match.getMatches()) {
			if (match.getQueueId() != null && (match.isFighting() || match.isStarting())) {
				Queue queue = Queue.getByUuid(match.getQueueId());

				if (queue == null) {
					continue;
				}

				if (queueFightCounts.containsKey(queue.getUuid())) {
					queueFightCounts.get(queue.getUuid())
					                .addAndGet(match.isSoloMatch() ? 2 : match.getMatchPlayers().size());
				} else {
					queueFightCounts.put(
							queue.getUuid(),
							new AtomicInteger(match.isSoloMatch() ? 2 : match.getMatchPlayers().size())
					);
				}
			}
		}

		this.queueFightCounts = queueFightCounts;
	}

	@Override
	public int getQueueingCount() {
		return this.inQueues;
	}

	@Override
	public int getFightingCount() {
		return this.inFights;
	}

	@Override
	public int getFightingCount(Queue queue) {
		if (queue == null) {
			return 0;
		}

		AtomicInteger atomic = this.queueFightCounts.get(queue.getUuid());

		if (atomic == null) {
			return 0;
		} else {
			return atomic.intValue();
		}
	}

}
