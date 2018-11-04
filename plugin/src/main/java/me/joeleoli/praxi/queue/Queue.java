package me.joeleoli.praxi.queue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.Getter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class Queue {

	@Getter
	private static List<Queue> queues = new ArrayList<>();

	private UUID uuid = UUID.randomUUID();
	private Ladder ladder;
	private boolean ranked;
	private LinkedList<QueuePlayer> players;

	public Queue(Ladder ladder, boolean ranked) {
		this.ladder = ladder;
		this.ranked = ranked;
		this.players = new LinkedList<>();

		queues.add(this);
	}

	public static Queue getByUuid(UUID uuid) {
		for (Queue queue : queues) {
			if (queue.getUuid().equals(uuid)) {
				return queue;
			}
		}

		return null;
	}

	public static Queue getByPredicate(Predicate<Queue> predicate) {
		for (Queue queue : queues) {
			if (predicate.test(queue)) {
				return queue;
			}
		}

		return null;
	}

	public void addPlayer(Player player, int elo) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
		final QueuePlayer queuePlayer = new QueuePlayer(this.uuid, player.getUniqueId());

		if (this.ranked) {
			queuePlayer.setElo(elo);
		}

		praxiPlayer.setState(PlayerState.IN_QUEUE);
		praxiPlayer.setQueuePlayer(queuePlayer);
		praxiPlayer.loadHotbar();

		player.sendMessage(
				Style.YELLOW + "You joined the " + Style.PINK + (this.ranked ? "Ranked" : "Unranked") + " " +
				this.ladder.getName() + Style.YELLOW + " queue.");

		this.players.add(queuePlayer);
	}

	public QueuePlayer removePlayer(QueuePlayer queuePlayer) {
		this.players.remove(queuePlayer);

		final Player player = Bukkit.getPlayer(queuePlayer.getPlayerUuid());

		if (player != null && player.isOnline()) {
			player.sendMessage(
					Style.YELLOW + "You left the " + Style.PINK + (this.ranked ? "Ranked" : "Unranked") + " " +
					this.ladder.getName() + Style.YELLOW + " queue.");
		}

		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(queuePlayer.getPlayerUuid());

		praxiPlayer.setQueuePlayer(null);
		praxiPlayer.setState(PlayerState.IN_LOBBY);
		praxiPlayer.loadHotbar();

		return queuePlayer;
	}

}
